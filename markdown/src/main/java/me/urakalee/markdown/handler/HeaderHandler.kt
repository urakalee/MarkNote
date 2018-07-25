package me.urakalee.markdown.handler

import me.urakalee.markdown.Mark
import me.urakalee.markdown.MarkHandler

/**
 * @author Uraka.Lee
 */
object HeaderHandler : MarkHandler {

    const val MIN_LEVEL = 2
    const val MAX_LEVEL = 4

    override fun handleHeader(inputMark: Mark, source: String, sourceMark: Mark): String {
        return if (source.length < MAX_LEVEL) "$source#" else ""
    }
}