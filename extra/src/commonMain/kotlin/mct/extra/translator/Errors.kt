package mct.extra.translator

import mct.MCTError

sealed class TranslateError : MCTError {
    data class ModelNotFound(val mode: String) : TranslateError() {
        override val message = "Model $mode not found"
    }
    data class IllegalUrl(override val message: String) : TranslateError()
}