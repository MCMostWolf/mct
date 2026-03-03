package mct.dp

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.context.accumulate
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import mct.util.bottom
import mct.util.peek
import mct.util.pop
import mct.util.push
import mct.Extraction.Datapack.MCFunction as Extraction
import mct.ExtractionGroup.Datapack as ExtractionGroup


internal val extractFromMCFunction = Extractor("MCFunction", ".mcfunction") { env, zfs, zpath, path ->
    val text = zfs.read(zpath) { readUtf8() }
    recover({
        extractTextMCF(
            text,
            source = path.name,
            path = zpath.normalized().toString(),
        )
    }) {
        raise(MCFunctionExtractError.ParseFailure(it))
    }
}

private fun filterText(string: String): Boolean =
    string.trimStart().run {
        startsWith('"') || startsWith('\'')
                || contains("\"text\"") // text compound
    }

context(_: Raise<NonEmptyList<MCFunctionParseError>>)
internal fun extractTextMCF(
    mcf: String,
    source: String,
    path: String,
): ExtractionGroup = accumulate {
    val extractions = mutableListOf<Extraction>()
    var row = 0

    line@ fun handleLine(lineStartLine: Int, line: String) {
        val parseContextProvider = ParseContextProvider {
            ParseContext(source, path, row, line)
        }
        context(parseContextProvider) {
            val chars = line.toCharArray()
            val buffer = StringBuilder()
            val stateStack = ArrayDeque<State>().apply { push(RootState) }
            for ((col, c) in chars.withIndex()) {
                val peekedState = stateStack.peek()
                check(stateStack.bottom() == RootState) {
                    "Fatal error due to the RootState being replaced."
                }
                if (peekedState == RootState && buffer.isEmpty()) {
                    if (c == '#') return@line // skip comments
                    if (c == ' ') continue
                }

                val submit = k@{
                    val str = buffer.toString()
                    if (!filterText(str)) return@k
                    val nextCol = col + 1 // adding 1 skips the current char
                    val extractStartCol = nextCol - str.length
                    extractions += Extraction(
                        indices = (lineStartLine + extractStartCol)..lineStartLine + col,
                        content = str
                    )
                    buffer.clear()
                }

                if (peekedState is QuoteState) {
                    if (peekedState.isEscaped) {
                        buffer.append(c)
                        peekedState.isEscaped = false
                        continue
                    }
                    if (c == '\\') {
                        peekedState.isEscaped = true
                        buffer.append(c)
                        continue
                    }

                    buffer.append(c)
                    if (c == peekedState.char) {
                        stateStack.pop()

                        if (stateStack.peek() == RootState) {
                            submit()
                        }
                    }
                    continue
                }

                buffer.append(c)
                if (c == ' ' && peekedState == RootState) { // cmd argument
                    buffer.clear()
                    continue
                }

                when (c) {
                    '\'' -> stateStack.push(QuoteState.SingleQuote())
                    '\"' -> stateStack.push(QuoteState.DoubleQuote())

                    '{' -> stateStack.push(BracketState.Curly)
                    '[' -> stateStack.push(BracketState.Square)
                    '}', ']' -> {
                        val excepted = if (c == '}') BracketState.Curly else BracketState.Square
                        ensure(peekedState == excepted) {
                            MCFunctionParseError.Unmatched.make(
                                excepted.right.toString(),
                                c.toString()
                            )
                        }

                        stateStack.pop()
                        if (stateStack.peek() == RootState) {
                            submit()
                        }
                    }
                }
                continue
            }

            val peekedState = stateStack.peek()
            if (peekedState != RootState) {
                val operator = when (peekedState) {
                    is QuoteState -> peekedState.char.toString()
                    is BracketState -> "${peekedState.left}${peekedState.right}"
                }
                raise(MCFunctionParseError.Unterminated.make(operator))
            }
        }
    }


    val line = StringBuilder()
    var lastC: Char? = null
    var lineStart = 0
    for ((charOffset, c) in mcf.toCharArray().withIndex()) {
        if (c == '\n') {
            handleLine(lineStart, line.toString())
            line.clear()
            lineStart = charOffset + 1
            row++
            lastC = c
            continue
        }
        if (lastC == '\r') {
            handleLine(lineStart, line.toString())
            line.clear()
            lineStart = charOffset
            row++
            line.append(c)
            lastC = c
            continue
        }

        if (c == '\r') {
            lastC = c
            continue
        }

        line.append(c)

        lastC = c
    }
    handleLine(lineStart, line.toString())

    ExtractionGroup(source, path, extractions)
}

internal data class ParseContext(
    val source: String,
    val path: String,
    val row: Int,
    val content: String
)

internal fun interface ParseContextProvider {
    fun current(): ParseContext
}

internal sealed interface State

internal data object RootState : State

internal sealed class QuoteState(val char: Char, var isEscaped: Boolean = false) : State {
    class SingleQuote : QuoteState('\'')
    class DoubleQuote : QuoteState('\"')

    override fun toString(): String {
        return "QuoteState(char=$char, isEscaped=$isEscaped)"
    }
}

internal enum class BracketState(val left: Char, val right: Char) : State {
    //    Parenthesis,
    Square('[', ']'),
    Curly('{', '}'),
}
