package mct.util.io

import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import korlibs.io.stream.AsyncStreamBase
import okio.BufferedSink
import okio.BufferedSource
import okio.FileHandle

fun BufferedSource.asAsyncInputStream(): AsyncInputStream = BufferedSourceWrappingAsyncInputStream(this)
fun BufferedSink.asAsyncOutputStream(): AsyncOutputStream = BufferedSinkWrappingAsyncOutputStream(this)
fun FileHandle.asAsyncStreamBase(): AsyncStreamBase = FileHandleWrappingAsyncStreamBase(this)


class BufferedSourceWrappingAsyncInputStream(
    val source: BufferedSource
) : AsyncInputStream {
    override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int =
        source.read(buffer, offset, len)

    override suspend fun close() = source.close()
}


class BufferedSinkWrappingAsyncOutputStream(
    val sink: BufferedSink
) : AsyncOutputStream {
    override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
        sink.write(buffer, offset, len)
    }

    override suspend fun close() = sink.close()
}


class FileHandleWrappingAsyncStreamBase(
    val handle: FileHandle
) : AsyncStreamBase() {
    override suspend fun write(position: Long, buffer: ByteArray, offset: Int, len: Int) =
        handle.write(position, buffer, offset, len)

    override suspend fun close() = handle.close()

    override suspend fun getLength(): Long = handle.size()

    override suspend fun read(
        position: Long,
        buffer: ByteArray,
        offset: Int,
        len: Int
    ): Int = handle.read(position, buffer, offset, len)

    override suspend fun setLength(value: Long) = handle.resize(value)
}