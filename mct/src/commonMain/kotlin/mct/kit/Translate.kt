package mct.kit

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mct.*
import mct.serializer.MCTJson
import mct.serializer.Snbt
import mct.text.TextCompound
import kotlin.jvm.JvmName


typealias TranslationMapping = Map<String, String>
typealias TranslationPool = Set<String>

private fun trySimply(text: String): String = runCatching {
    val tc = MCTJson.decodeFromString<TextCompound>(text)
    MCTJson.encodeToString(tc)
}.getOrElse {
    runCatching {
        val tc = Snbt.decodeFromString<TextCompound>(text)
        Snbt.encodeToString(tc)
    }.getOrElse {
        text
    }
}


fun List<ExtractionGroup<*>>.exportIntoPool(simply: Boolean): TranslationPool =
    flatMapTo(mutableSetOf()) { it.extractions.map { if (!simply) it.content else trySimply(it.content) } }


fun List<ExtractionGroup<*>>.replace(mapping: TranslationMapping): List<ReplacementGroup<*>> =
    map {
        when (it) {
            is DatapackExtractionGroup -> DatapackReplacementGroup(
                source = it.source,
                path = it.path,
                replacements = it.extractions.mapNotNull {
                    when (it) {
                        is DatapackExtraction.MCFunction -> DatapackReplacement.MCFunction(
                            indices = it.indices,
                            replacement = mapping[it.content] ?: return@mapNotNull null
                        )

                        is DatapackExtraction.MCJson -> DatapackReplacement.MCJson(
                            pointer = it.pointer,
                            replacement = mapping[it.content] ?: return@mapNotNull null
                        )
                    }
                }
            )

            is RegionExtractionGroup -> RegionReplacementGroup(
                dimension = it.dimension,
                kind = it.kind,
                coord = it.coord,
                replacements = it.extractions.mapNotNull {
                    RegionReplacement(
                        index = it.index,
                        pointer = it.pointer,
                        replacement = mapping[it.content] ?: return@mapNotNull null
                    )
                }
            )
        }
    }

@JvmName($$"replaceSimply$DatapackExtractionGroup")
inline fun List<DatapackExtractionGroup>.replaceSimply(mapping: TranslationMapping): List<DatapackReplacementGroup> =
    replaceSimply { mapping.entries.fold(it) { ace, (k, v) -> ace.replace(k, v) } }


@JvmName($$"replaceSimply$DatapackExtractionGroup")
inline fun List<DatapackExtractionGroup>.replaceSimply(replace: (String) -> String?) = map {
    DatapackReplacementGroup(
        source = it.source,
        path = it.path,
        replacements = it.extractions.mapNotNull { extraction ->
            when (extraction) {
                is DatapackExtraction.MCFunction -> DatapackReplacement.MCFunction(
                    indices = extraction.indices,
                    replacement = replace(extraction.content) ?: return@mapNotNull null
                )

                is DatapackExtraction.MCJson -> DatapackReplacement.MCJson(
                    pointer = extraction.pointer,
                    replacement = replace(extraction.content) ?: return@mapNotNull null
                )
            }

        })
}

@JvmName($$"replaceSimply$RegionExtractionGroup")
inline fun List<RegionExtractionGroup>.replaceSimply(mapping: TranslationMapping): List<RegionReplacementGroup> =
    replaceSimply { mapping.entries.fold(it) { ace, (k, v) -> ace.replace(k, v) } }

@JvmName($$"replaceSimply$RegionExtractionGroup")
inline fun List<RegionExtractionGroup>.replaceSimply(replace: (String) -> String?) = map {
    RegionReplacementGroup(
        dimension = it.dimension,
        kind = it.kind,
        coord = it.coord,
        replacements = it.extractions.mapNotNull {
            RegionReplacement(
                index = it.index,
                pointer = it.pointer,
                replacement = replace(it.content) ?: return@mapNotNull null
            )
        }
    )
}