package mct.region

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import mct.MCTWorkspace
import mct.pointer.DataPointer
import mct.region.anvil.model.ChunkDataKind
import mct.serializer.Snbt
import net.benwoodworth.knbt.*
import mct.ReplacementGroup.Region as ReplacementGroup

context(_: Raise<BackfillError>)
suspend fun MCTWorkspace.backfillRegion(replacementGroups: Flow<ReplacementGroup>) = coroutineScope {
    replacementGroups.collect { group ->
        val dimension = dimensions[group.dimension]
            ?: raise(BackfillError.DimensionNotFound(group.dimension))
        val mgr = when (group.kind) {
            ChunkDataKind.Terrain -> dimension.regionRawMgr
            ChunkDataKind.Entities -> dimension.entitiesRawMgr
            ChunkDataKind.Poi -> dimension.poiRawMgr
        }
        if (mgr == null) return@collect
        launch {
            recover({
                launch {
                    mgr.modify(group.coord) { region ->
                        val chunks = region.chunks.toMutableList()
                        group.replacements.groupBy { it.index }
                            .forEach { (index, replacements) ->
                                replacements.forEach { replacement -> // FIXME: Use a more efficient algorithm
                                    val chunk = chunks[index] ?: return@forEach
                                    chunks[index] = chunk.modify {
                                        it.transform(replacement.pointer, replacement.replacement)
                                    }
                                }
                            }
                        region.modifyChunks(chunks)
                    }
                }
            }, {
                raise(BackfillError.Internal(it))
            })
        }
    }
}


private fun NbtTag.transform(pointer: DataPointer, replacement: String): NbtTag = when (pointer) {
    is DataPointer.List -> {
        if (this !is NbtList<*>) return this
        if (size <= pointer.point) return this
        val transformed = toMutableList()
        transformed[pointer.point] = transformed[pointer.point].transform(pointer.value, replacement)
        transformed.toNbtList()
    }

    is DataPointer.Map -> {
        if (this !is NbtCompound) return this
        if (!containsKey(pointer.point)) return this
        val transformed = toMutableMap()
        transformed[pointer.point] = transformed[pointer.point]!!.transform(pointer.value, replacement)
        NbtCompound(transformed)
    }

    DataPointer.Terminator -> {
        return Snbt.decodeFromString<NbtTag>(replacement)
    }
}

private fun List<NbtTag>.toNbtList() = buildNbtList<NbtTag> {
    this@toNbtList.forEach {
        @Suppress(
            "CAST_NEVER_SUCCEEDS", "UNCHECKED_CAST",
            "UPPER_BOUND_VIOLATED_IN_TYPE_OPERATOR_OR_PARAMETER_BOUNDS_WARNING"
        )
        fun <T> cast() = this as NbtListBuilder<T>
        when (it) {
            is NbtByte -> cast<NbtByte>().add(it)
            is NbtByteArray -> cast<NbtByteArray>().add(it)
            is NbtCompound -> cast<NbtCompound>().add(it)
            is NbtDouble -> cast<NbtDouble>().add(it)
            is NbtFloat -> cast<NbtFloat>().add(it)
            is NbtInt -> cast<NbtInt>().add(it)
            is NbtIntArray -> cast<NbtIntArray>().add(it)
            is NbtList<*> -> cast<NbtList<*>>().add(it)
            is NbtLong -> cast<NbtLong>().add(it)
            is NbtLongArray -> cast<NbtLongArray>().add(it)
            is NbtShort -> cast<NbtShort>().add(it)
            is NbtString -> cast<NbtString>().add(it)
        }
    }
}