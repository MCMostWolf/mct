package mct.kit

import mct.Extraction
import mct.ExtractionGroup
import mct.Replacement
import mct.ReplacementGroup

typealias TranslationMapping = Map<String, String>

fun List<ExtractionGroup<*>>.exportIntoPool(): List<String> =
    flatMap { it.extractions.map { it.content } }.distinct()

fun List<ExtractionGroup<*>>.replace(mapping: TranslationMapping): List<ReplacementGroup<*>> =
    map {
        when (it) {
            is ExtractionGroup.Datapack -> ReplacementGroup.Datapack(
                source = it.source,
                path = it.path,
                replacements = it.extractions.mapNotNull {
                    when (it) {
                        is Extraction.Datapack.MCFunction -> Replacement.Datapack.MCFunction(
                            indices = it.indices,
                            replacement = mapping[it.content] ?: return@mapNotNull null
                        )

                        is Extraction.Datapack.MCJson -> Replacement.Datapack.MCJson(
                            pointer = it.pointer,
                            replacement = mapping[it.content] ?: return@mapNotNull null
                        )
                    }
                }
            )

            is ExtractionGroup.Region -> ReplacementGroup.Region(
                dimension = it.dimension,
                kind = it.kind,
                coord = it.coord,
                replacements = it.extractions.mapNotNull {
                    Replacement.Region(
                        index = it.index,
                        pointer = it.pointer,
                        replacement = mapping[it.content] ?: return@mapNotNull null
                    )
                }
            )
        }
    }

inline fun List<ExtractionGroup<*>>.replaceSimply(mapping: TranslationMapping): List<ReplacementGroup<*>> =
    replaceSimply { mapping.entries.fold(it) { ace, (k, v) -> ace.replace(k, v) } }


fun List<ExtractionGroup<*>>.replaceSimply(replace: (String) -> String?): List<ReplacementGroup<*>> = map {
    when (it) {
        is ExtractionGroup.Datapack -> ReplacementGroup.Datapack(
            source = it.source,
            path = it.path,
            replacements = it.extractions.mapNotNull { extraction ->
                when (extraction) {
                    is Extraction.Datapack.MCFunction -> Replacement.Datapack.MCFunction(
                        indices = extraction.indices,
                        replacement = replace(extraction.content) ?: return@mapNotNull null
                    )

                    is Extraction.Datapack.MCJson -> Replacement.Datapack.MCJson(
                        pointer = extraction.pointer,
                        replacement = replace(extraction.content) ?: return@mapNotNull null
                    )
                }

            })

        is ExtractionGroup.Region -> ReplacementGroup.Region(
            dimension = it.dimension,
            kind = it.kind,
            coord = it.coord,
            replacements = it.extractions.mapNotNull {
                Replacement.Region(
                    index = it.index,
                    pointer = it.pointer,
                    replacement = replace(it.content) ?: return@mapNotNull null
                )
            }
        )
    }
}