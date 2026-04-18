package mct.cli.translator

data class TranslateResponse(val texts: List<String>, val terms: Set<Term>)

interface Translator {
    suspend fun translate(sources: List<String>): TranslateResponse
}