package mct

import arrow.core.getOrElse
import arrow.core.raise.either
import com.goncalossilva.resources.Resource
import korlibs.io.lang.unreachable
import mct.util.io.openZipReadWrite
import mct.util.io.useAsync
import okio.Path.Companion.toPath

private val resource = Resource("TestMap.zip")

suspend fun TestMapWorkspace() = resource.readBytes().openZipReadWrite().useAsync {
    val env = Env(
        fs = it,
        logger = Logger.Console()
    )
    either {
        MCTWorkspace("MCT Test".toPath(), env)
    }.getOrElse { unreachable }
}
