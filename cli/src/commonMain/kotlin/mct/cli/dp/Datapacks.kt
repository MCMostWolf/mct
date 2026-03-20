package mct.cli.dp

import arrow.core.raise.Raise
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.flow.toList
import mct.MCTError
import mct.ReplacementGroup
import mct.cli.PrettyJson
import mct.cli.WorkspaceCommand
import mct.dp.backfillDatapack
import mct.dp.extractFromDatapack
import mct.serializer.MCTJson
import mct.util.io.readText
import okio.FileSystem
import okio.Path.Companion.toPath

val DatapackCmd: SuspendingCliktCommand = Datapack()
    .subcommands(ExtractDatapack())

private class Datapack : SuspendingCliktCommand(name = "db") {
    override suspend fun run() {
        echo("Some operation about datapacks.")
    }
}

private class ExtractDatapack : WorkspaceCommand(name = "extract") {
    val output by option().required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        val extractions = workspace.extractFromDatapack().toList()
        env.fs.write(output.toPath()) {
            val result = PrettyJson.encodeToString(extractions)
            writeUtf8(result)
        }
    }
}


private class BackfillDatapack : WorkspaceCommand(name = "extract") {
    val replacements by option().required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        val replacementGroups: List<ReplacementGroup.Datapack> =
            MCTJson.decodeFromString(replacements.toPath().readText())
        workspace.backfillDatapack(replacementGroups)
    }
}
