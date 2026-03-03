package mct.dp

import arrow.core.NonEmptyList
import kotlinx.serialization.SerializationException
import mct.MCTError


sealed interface DBError : MCTError

sealed interface BackfillError : DBError {}

sealed interface ExtractError : DBError {
    data class JsonSyntaxError(val exception: SerializationException) : ExtractError{
        override val message = exception.message ?: "<null>"
    }
}
sealed interface MCFunctionExtractError : ExtractError {
    data class ParseFailure(val errors: NonEmptyList<MCFunctionParseError>) : MCFunctionExtractError {
        override val message = errors.joinToString()
    }
}
sealed interface MCJsonExtractError : ExtractError {
    data class JsonSyntaxError(val exception: SerializationException) : MCJsonExtractError{
        override val message = exception.message ?: "<null>"
    }
}



sealed interface MCFunctionParseError : MCTError {
    val line: Int

    data class Unmatched(
        override val line: Int,
        val excepted: String,
        val actual: String,
    ) : MCFunctionParseError {
        companion object {
            context(parseContextProvider: ParseContextProvider)
            internal fun make(excepted: String, actual: String) =
                Unmatched(parseContextProvider.current().row, excepted, actual)
        }

        override val message = "The excepted terminator is $excepted but actual $actual"
    }

    data class Unterminated(
        override val line: Int,
        val operator: String,
    ) : MCFunctionParseError {
        companion object {
            context(parseContextProvider: ParseContextProvider)
            internal fun make(operator: String) =
                Unterminated(parseContextProvider.current().row, operator)
        }

        override val message = "The operator '$operator' hasn't been closed correctly"
    }
}
