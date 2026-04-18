package mct.cli

import okio.FileSystem

expect val SystemFileSystem: FileSystem

expect fun envvar(name: String): String?