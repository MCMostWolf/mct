package mct.dp

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.nullable
import kotlinx.coroutines.flow.*
import mct.Env
import mct.MCTWorkspace
import mct.util.io.ROOT
import mct.util.io.endsWith
import mct.util.io.openZipReadWrite
import mct.util.io.use2
import okio.FileSystem
import okio.Path
import mct.ExtractionGroup.Datapack as ExtractionGroup

fun MCTWorkspace.extractFromDatapack(): Flow<ExtractionGroup> {
    return fs.listRecursively(datapackDir)
        .filter { it.endsWith(".zip") }
        .asFlow().flatMapMerge { path ->
            println(path)
            fs.openZipReadWrite(path).use2 { zfs ->
                zfs.listRecursively(Path.ROOT)
                    .asFlow()
                    .mapNotNull { zpath ->
                        nullable { zpath to EXTRACTORS.find { zpath.endsWith(it.targetExtension) }.bind() }
                    }
                    .flatMapMerge { (zpath, extractor) ->
                        either {
                            env.logger.debug {
                                "Extracting $zpath via $extractor"
                            }
                            flowOf(extractor.extract(env, zfs, zpath, path))
                        }.getOrElse { error ->
                            env.logger.error { error.message }
                            emptyFlow()
                        }
                    }
                    .filter { it.extractions.isNotEmpty() }
            }
        }
}

private val EXTRACTORS = listOf(
    extractFromMCFunction,
    extractFromMCJson
)

internal interface Extractor {
    val targetExtension: String

    context(_: Raise<ExtractError>)
    fun extract(
        env: Env,
        zfs: FileSystem,
        zpath: Path,
        path: Path
    ): ExtractionGroup
}

internal fun Extractor(
    name: String,
    targetExtension: String,
    extract: context(Raise<ExtractError>) (
        env: Env,
        zfs: FileSystem,
        zpath: Path,
        path: Path
    ) -> ExtractionGroup
) = object : Extractor {
    override val targetExtension = targetExtension

    context(_: Raise<ExtractError>)
    override fun extract(
        env: Env,
        zfs: FileSystem,
        zpath: Path,
        path: Path
    ): ExtractionGroup = extract(env, zfs, zpath, path)

    override fun toString() = "Extractor($name)"
}