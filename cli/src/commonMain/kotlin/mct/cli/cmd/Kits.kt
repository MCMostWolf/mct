package mct.cli.cmd

import arrow.core.raise.Raise
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import mct.DatapackExtractionGroup
import mct.MCTError
import mct.RegionExtractionGroup
import mct.cli.BaseCommand
import mct.cli.WorkspaceCommand
import mct.cli.path
import mct.kit.exportRegionSnbt
import mct.kit.replaceSimply
import mct.serializer.MCTJson
import mct.util.io.readText
import mct.util.io.writeText
import mct.util.unreachable
import okio.FileSystem


class Kit : SuspendingCliktCommand(name = "kit") {
    init {
        subcommands(ExportSnbt(), Ciallo())
    }

    override fun help(context: Context) = "Some helpful tool"

    override suspend fun run() = Unit
}

private class ExportSnbt : WorkspaceCommand(
    name = "export-snbt",
    help = "A tool helping you extract all nbt from region files"
) {
    val output by option("--output", "-o", help = "The dir where the extracted snbt will be placed").path().required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        workspace.exportRegionSnbt(output)
    }
}

private class Ciallo : BaseCommand(name = "ciallo") {
    val input by option(
        "--input",
        "-i",
        help = "The path to what you want to replace extractions with a specified string"
    ).path().required()
    val output by option("--output", "-o", help = "The output path").path().required()
    val replacement by option(
        "--replacement",
        "-r",
        help = "The replacement which will replace extraction"
    ).default("{CIALLO powered by MCT}")
    val kind by option(help = "The kind of extractions").choice("datapack", "region").required()

    context(_: Raise<MCTError>, fs: FileSystem)
    override suspend fun App() {
        when (kind) {
            "datapack" -> {
                val extractionGroups = MCTJson.decodeFromString<List<DatapackExtractionGroup>>(input.readText())
                val cialloized = extractionGroups.replaceSimply { replacement }
                output.writeText(MCTJson.encodeToString(cialloized))
            }

            "region" -> {
                val extractionGroups = MCTJson.decodeFromString<List<RegionExtractionGroup>>(input.readText())
                val cialloized = extractionGroups.replaceSimply { replacement }
                output.writeText(MCTJson.encodeToString(cialloized))
            }

            else -> unreachable
        }

    }
}

