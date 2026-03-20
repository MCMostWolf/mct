package mct.cli.region


import arrow.core.raise.Raise
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import mct.MCTError
import mct.cli.PrettyJson
import mct.cli.WorkspaceCommand
import mct.region.extractFromRegion
import okio.FileSystem
import okio.Path.Companion.toPath

val RegionCmd: SuspendingCliktCommand = Region()
    .subcommands(RegionExtract())

private class Region : SuspendingCliktCommand(name = "region") {
    override suspend fun run() {
        echo("Some operation about region.")
    }
}

private class RegionExtract : WorkspaceCommand(name = "extract") {
    val output by option().required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        val extractions = workspace.extractFromRegion()
        env.fs.write(output.toPath()) {
            val result = PrettyJson.encodeToString(extractions)
            writeUtf8(result)
        }
    }
}


