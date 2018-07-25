package me.urakalee.markdown.handler

import me.urakalee.markdown.Mark
import me.urakalee.markdown.MarkHandler

/**
 * @author Uraka.Lee
 */
object QuoteHandler : MarkHandler {

    override fun handleQuote(inputMark: Mark, source: String, sourceMark: Mark): String {
        return when (sourceMark) {
            Mark.QT -> {
                ""
            }
            else -> {
                super.handleQuote(inputMark, source, sourceMark)
            }
        }
    }
}