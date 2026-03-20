package mct.cli.kits


import arrow.core.raise.Raise
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import mct.MCTError
import mct.cli.WorkspaceCommand
import mct.kit.exportRegionSnbt
import okio.FileSystem
import okio.Path.Companion.toPath

val KitCmd: SuspendingCliktCommand = Kit()
    .subcommands(ExportSnbt())

private class Kit : SuspendingCliktCommand(name = "kit") {
    override suspend fun run() {
        echo("Some operation about region.")
    }
}

private class ExportSnbt : WorkspaceCommand(name = "export-snbt") {
    val output by option().required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        workspace.exportRegionSnbt(output.toPath())
    }
}


