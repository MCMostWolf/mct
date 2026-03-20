package mct.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import kotlinx.serialization.json.Json
import mct.cli.dp.DatapackCmd
import mct.cli.kits.KitCmd
import mct.cli.region.RegionCmd
import mct.serializer.MCTJson

val PrettyJson = Json(MCTJson) {
    prettyPrint = true
    prettyPrintIndent = "  "
}

suspend fun main(args: Array<String>) = MCT()
    .subcommands(DatapackCmd, RegionCmd, KitCmd)
    .main(args)


class MCT : SuspendingCliktCommand() {
    init {
        versionOption("SNAPSHOT")
    }

    override suspend fun run() = Unit
}

