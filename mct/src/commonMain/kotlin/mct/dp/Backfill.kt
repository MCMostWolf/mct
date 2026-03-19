package mct.dp

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import mct.MCTWorkspace
import mct.dp.mcjson.MCJson
import mct.dp.mcjson.standardizeMCJson
import mct.pointer.DataPointer
import mct.util.io.openZipReadWrite
import mct.util.io.use2
import okio.BufferedSource
import okio.Path.Companion.toPath
import kotlin.jvm.JvmName
import mct.Replacement.Datapack as Replacement
import mct.ReplacementGroup.Datapack as ReplacementGroup


suspend fun MCTWorkspace.backfillDatapack(replacementGroups: Iterable<ReplacementGroup>) = coroutineScope {
    replacementGroups.groupBy {
        datapackDir / it.source
    }.forEach { (dbPath, replacementGroup) ->
        launch {
            fs.openZipReadWrite(dbPath).use2 { zfs ->
                replacementGroup.forEach { replacementGroup ->
                    val path = replacementGroup.path.toPath()
                    val origin = zfs.read(path, BufferedSource::readUtf8)
                    val handled = origin.backfill(replacementGroup.replacements)
                    zfs.write(path) {
                        writeUtf8(handled)
                    }
                }
            }
        }
    }
}

internal fun String.backfill(extractions: List<Replacement>): String {
    val mcfunction = mutableListOf<Replacement.MCFunction>()
    val mcjson = mutableListOf<Replacement.MCJson>()
    extractions.forEach { replacement ->
        when (replacement) {
            is Replacement.MCFunction -> mcfunction.add(replacement)
            is Replacement.MCJson -> mcjson.add(replacement)
        }
    }
    if (mcfunction.isNotEmpty()) return backfill(mcfunction)
    if (mcjson.isNotEmpty()) return backfill(mcjson)
    return this
}

@JvmName($$"backfill$MCFunction")
internal fun String.backfill(replacements: List<Replacement.MCFunction>) =
    replacements
        .sortedByDescending { it.indices.first }
        .fold(StringBuilder(this)) { ace, e ->
            ace.replaceRange(e.indices, e.replacement) as StringBuilder
        }.toString()

@JvmName($$"backfill$MCJson")
internal fun String.backfill(extractions: List<Replacement.MCJson>): String {
    val standardizedJson = standardizeMCJson(this)
    val jsonElement = MCJson.decodeFromString<JsonElement>(standardizedJson)
    val backfilledJsonElement = extractions.fold(jsonElement) { acc, e ->
        acc.transform(e.pointer, e.replacement)
    }
    return MCJson.encodeToString(backfilledJsonElement)
}


private fun JsonElement.transform(pointer: DataPointer, replacement: String): JsonElement = when (pointer) {
    is DataPointer.List -> {
        if (this !is JsonArray) return this
        if (size <= pointer.point) return this
        val transformed = toMutableList()
        transformed[pointer.point] = transformed[pointer.point].transform(pointer.value, replacement)
        JsonArray(transformed)
    }

    is DataPointer.Map -> {
        if (this !is JsonObject) return this
        if (!containsKey(pointer.point)) return this
        val transformed = toMutableMap()
        transformed[pointer.point] = transformed[pointer.point]!!.transform(pointer.value, replacement)
        JsonObject(transformed)
    }

    DataPointer.Terminator -> {
        return JsonPrimitive(replacement)
    }
}
