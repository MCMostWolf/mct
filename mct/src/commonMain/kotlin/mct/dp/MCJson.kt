package mct.dp

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import mct.pointer.*
import mct.Extraction.Datapack.MCJson as Extraction
import mct.ExtractionGroup.Datapack as ExtractionGroup

internal val MCJson = Json {
    ignoreUnknownKeys = true
    allowTrailingComma = true
    isLenient = true
}

internal val extractFromMCJson = Extractor("MCJson", ".json") { env, zfs, zpath, path ->
    val text = zfs.read(zpath) { readUtf8() }
    extractTextMCJ(
        text,
        source = path.name,
        path = zpath.normalized().toString(),
    )
}

context(_: Raise<MCJsonExtractError>)
internal fun extractTextMCJ(
    json: String,
    source: String,
    path: String,
    patterns: Set<DataPointerPattern>? = BuiltinPatterns
): ExtractionGroup = try {
    val standard = standardizeMCJson(json)
    val jsonElement = MCJson.decodeFromString<JsonElement>(standard)

    val extractions = jsonElement.extractTextMCJ()
        .filterPointer(patterns)
        .map { (pointer, content) ->
            Extraction(
                pointer,
                content = content
            )
        }.toList()
    ExtractionGroup(
        source = source,
        path = path,
        extractions = extractions
    )
} catch (e: SerializationException) {
    raise(MCJsonExtractError.JsonSyntaxError(e))
}

internal fun JsonElement.extractTextMCJ(): Sequence<DataPointerWithValue> = when (this) {
    is JsonArray -> asSequence().withIndex().flatMap { (index, element) ->
        element.extractTextMCJ().map { it.markArray(index) }
    }

    is JsonObject -> asSequence().flatMap { (key, value) ->
        value.extractTextMCJ().map {
            it.markMap(key)
        }
    }

    is JsonPrimitive if isString -> sequenceOf(DataPointer.Terminator to content)
    JsonNull -> emptySequence()
    else -> emptySequence()
}

internal fun standardizeMCJson(mcjson: String): String {
    val chars = mcjson.toCharArray()
    val result = StringBuilder(mcjson.length)
    var i = 0
    var inSingleQuote = false
    var inDoubleQuote = false
    while (i < mcjson.length) {
        val c = chars[i]
        when (c) {
            '\'' if !inDoubleQuote -> {
                inSingleQuote = !inSingleQuote
                result.append('"')
            }

            '"' if inSingleQuote -> {
                result.append("\\\"")
                inDoubleQuote = !inDoubleQuote
            }

            '\\' if inSingleQuote && i + 1 < mcjson.length && chars[i + 1] == '\'' -> {
                result.append('\'')
            }

            else -> result.append(c)
        }
        i++
    }
    return result.toString()
}