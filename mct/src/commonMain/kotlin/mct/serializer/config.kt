@file:OptIn(ExperimentalNbtApi::class)

package mct.serializer

import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.*

val Json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

private val CommonNbt= Nbt {
    variant = NbtVariant.Java
    ignoreUnknownKeys = true
    compression = NbtCompression.None
}

val NbtZlib = Nbt(CommonNbt) {
    compression = NbtCompression.Zlib
}

val NbtGzip = Nbt(CommonNbt) {
    compression = NbtCompression.Gzip
}


val NbtNone = Nbt(CommonNbt) {
    compression = NbtCompression.None
}


val Snbt = StringifiedNbt {
    prettyPrint = true
    prettyPrintIndent = "  "
}