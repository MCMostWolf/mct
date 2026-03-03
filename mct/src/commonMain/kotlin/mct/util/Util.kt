package mct.util

import kotlinx.serialization.encodeToString
import mct.serializer.Snbt
import net.benwoodworth.knbt.NbtTag

inline fun <T> ArrayDeque<T>.top() = last()
inline fun <T> ArrayDeque<T>.bottom() = first()
inline fun <T> ArrayDeque<T>.peek() = last()
inline fun <T> ArrayDeque<T>.pop() = removeLast()
inline fun <T> ArrayDeque<T>.push(element: T) = addLast(element)
inline fun <T> ArrayDeque<T>.peekOrNull() = lastOrNull()
inline fun <T> ArrayDeque<T>.popOrNull() = removeLastOrNull()


fun NbtTag.toSnbt(): String = Snbt.encodeToString(this)