package me.urakalee.markdown

import me.urakalee.markdown.handler.*

/**
 * @author Uraka.Lee
 */
enum class Mark(val type: MarkType, val pattern: Regex, val defaultMark: String, val handler: MarkHandler) {

    NONE(MarkType.ANY, Regex(""), "", NoneHandler),

    H(MarkType.PRECEDING, Regex("#+"), "#".repeat(HeaderHandler.MIN_LEVEL), HeaderHandler),
    //region list
    LI(MarkType.PRECEDING, Regex("[-*]"), "-", ListHandler),
    LO(MarkType.PRECEDING, Regex("\\d\\."), "1.", ListHandler),
    LA(MarkType.PRECEDING, Regex("[a-z]\\."), "a.", ListHandler),
    //endregion
    TD(MarkType.PRECEDING, Regex("- \\[[x ]]", RegexOption.IGNORE_CASE), TodoHandler.UNCHECKED, TodoHandler),
    QT(MarkType.PRECEDING, Regex(">"), ">", QuoteHandler), // TODO: multi-level

    STRIKE(MarkType.TEXT, Regex("~~.*~~"), "~~", StrikeHandler);

    fun isList(): Boolean {
        return this == LI || this == LO || this == LA
    }

    companion object {

        fun fromString(s: String): Mark {
            return values().filter {
                it.type == MarkType.PRECEDING
            }.firstOrNull {
                s.matches(it.pattern)
            } ?: NONE
        }

        fun handlePrecedingMark(inputMark: Mark, source: String): String {
            return Mark.fromString(source).let {
                it.handler.handleMark(inputMark, source, it)
            }
        }

        fun handleTextMarkLongClick(inputMark: Mark, source: String): String {
            return inputMark.handler.handleMarkLongClick(inputMark, source, NONE)
        }
    }
}

enum class MarkType {
    ANY,
    PRECEDING,
    TEXT
}
