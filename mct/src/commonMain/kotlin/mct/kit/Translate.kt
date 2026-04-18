package mct.kit

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mct.*
import mct.serializer.MCTJson
import mct.serializer.Snbt
import mct.text.TextCompound


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


fun List<ExtractionGroup>.exportIntoPool(simply: Boolean): TranslationPool =
    flatMapTo(mutableSetOf()) { it.extractions.map { if (!simply) it.content else trySimply(it.content) } }


inline fun List<ExtractionGroup>.replaceSimply(mapping: TranslationMapping): List<ReplacementGroup> =
    replaceSimply { mapping.entries.fold(it) { ace, (k, v) -> ace.replace(k, v) } }

inline fun List<ExtractionGroup>.replaceSimply(replace: (String) -> String?): List<ReplacementGroup> =
    replace(
        mcfReplace = { replace(it.content) },
        mcjReplace = { replace(it.content) },
        regionReplace = { replace(it.content) }
    )


fun List<ExtractionGroup>.replace(mapping: TranslationMapping) = replace(
    mcfReplace = { mapping[it.content] },
    mcjReplace = { mapping[it.content] },
    regionReplace = { mapping[it.content] }
)

inline fun List<ExtractionGroup>.replace(
    mcfReplace: (DatapackExtraction.MCFunction) -> String?,
    mcjReplace: (DatapackExtraction.MCJson) -> String?,
    regionReplace: (RegionExtraction) -> String?,
) =
    map {
        when (it) {
            is DatapackExtractionGroup -> DatapackReplacementGroup(
                source = it.source,
                path = it.path,
                replacements = it.extractions.mapNotNull { extraction ->
                    when (extraction) {
                        is DatapackExtraction.MCFunction -> DatapackReplacement.MCFunction(
                            indices = extraction.indices,
                            replacement = mcfReplace(extraction) ?: return@mapNotNull null,
                        )

                        is DatapackExtraction.MCJson -> DatapackReplacement.MCJson(
                            pointer = extraction.pointer,
                            replacement = mcjReplace(extraction) ?: return@mapNotNull null
                        )
                    }

                })

            is RegionExtractionGroup -> RegionReplacementGroup(
                dimension = it.dimension,
                kind = it.kind,
                coord = it.coord,
                replacements = it.extractions.mapNotNull {
                    RegionReplacement(
                        index = it.index,
                        pointer = it.pointer,
                        kind = it.kind,
                        replacement = regionReplace(it) ?: return@mapNotNull null
                    )
                }
            )
        }
    }
