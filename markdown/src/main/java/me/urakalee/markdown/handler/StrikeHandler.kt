package me.urakalee.markdown.handler

import me.urakalee.markdown.Mark
import me.urakalee.markdown.MarkHandler

/**
 * @author Uraka.Lee
 */
object StrikeHandler : MarkHandler {

    fun handleLongClick(source: String): String {
        val mark = Mark.STRIKE.defaultMark
        return if (source.startsWith(mark) && source.endsWith(mark)) {
            source.substring(mark.length, source.length - mark.length)
        } else {
            "$mark$source$mark"
        }
    }
}