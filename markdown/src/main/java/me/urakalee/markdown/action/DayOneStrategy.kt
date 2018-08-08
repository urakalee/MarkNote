package me.urakalee.markdown.action

import android.widget.EditText
import me.urakalee.markdown.Indent
import me.urakalee.markdown.Mark
import me.urakalee.markdown.handler.TodoHandler
import me.urakalee.ranger.extension.isIndent
import me.urakalee.ranger.extension.selectedLine
import my.shouheng.palmmarkdown.strategy.DefaultStrategy

/**
 * @author Uraka.Lee
 */
class DayOneStrategy : DefaultStrategy() {

    override fun h1(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        handlePrecedingMark(source, Mark.H, selectionStart, selectionEnd, editor)
    }

    override fun list(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText) {
        handlePrecedingMark(source, Mark.LI, selectionStart, selectionEnd, editor)
    }

    override fun todo(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText) {
        handlePrecedingMark(source, Mark.TD, selectionStart, selectionEnd, editor)
    }

    override fun indent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        val (targetLine, start, end) = source.selectedLine(selectionStart, selectionEnd)
        // parse 出前面格式(header, list, task, quote)之外的文字
        val (_, indent, content) = detectPrecedingMark(targetLine)
        // XXX: 目前只把 4 个空格当 indent
        val firstNonIndent = start + indent.length
        val originalIndentLength = indent.length
        indent.indent()
        editor.text?.replace(start, firstNonIndent, indent.content)
        editor.setSelection(end + indent.length - originalIndentLength) // 简单处理, 光标放在行尾
    }

    override fun dedent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        val (targetLine, start, end) = source.selectedLine(selectionStart, selectionEnd)
        // parse 出前面格式(header, list, task, quote)之外的文字
        val (_, indent, content) = detectPrecedingMark(targetLine)
        // XXX: 目前只把 4 个空格当 indent
        val firstNonIndent = start + indent.length
        val originalIndentLength = indent.length
        indent.dedent()
        editor.text?.replace(start, firstNonIndent, indent.content)
        editor.setSelection(end + indent.length - originalIndentLength) // 简单处理, 光标放在行尾
    }

    override fun quote(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        handlePrecedingMark(source, Mark.QT, selectionStart, selectionEnd, editor)
    }

    private fun handlePrecedingMark(source: String, inputMark: Mark,
                                    selectionStart: Int, selectionEnd: Int, editor: EditText) {
        val (targetLine, start, end) = source.selectedLine(selectionStart, selectionEnd)
        val (precedingMarkSource, indent, _) = detectPrecedingMark(targetLine)
        val newMark = Mark.handlePrecedingMark(inputMark, precedingMarkSource)
        val firstNonIndent = start + indent.length
        // 如果 newMark.isEmpty, 一定是删除原有标记, 也就是说 mark 有效且后面有空格, length + 1
        val replaceLength = if (newMark.isEmpty()) precedingMarkSource.length + 1 else precedingMarkSource.length
        editor.text?.replace(firstNonIndent, firstNonIndent + replaceLength, newMark)
        editor.setSelection(end + newMark.length - replaceLength) // 简单处理, 光标放在行尾
    }

    companion object {

        /**
         * 前缀空格会被当做 indent
         *
         * mark
         * - "": 没有找到标签(没有找到字符后面的空格) -> 加标记+空格
         * - mark: 标记 -> 处理标记
         * - ???: 非标记文本, 有空格 -> 加标记+空格
         *
         * @return mark, indent, content
         */
        fun detectPrecedingMark(line: String): Triple<String, Indent, String> {
            // 找到 indent
            val firstNonIndent = line.indexOfFirst {
                !it.isIndent()
            }.let {
                if (it == -1) line.length else it // -1 则全是 indent
            }
            val indent = Indent(if (firstNonIndent == 0) null else line.substring(0 until firstNonIndent))
            // 在 indent 后面找 mark
            val firstBlank = line.indexOf(' ', firstNonIndent)
            if (firstBlank == -1) {
                return Triple("", indent, line)
            } else {
                val mark = line.substring(firstNonIndent until firstBlank)
                if (mark == "-") {
                    val todo = detectTodo(line, indent)
                    if (todo != null) {
                        return todo
                    }
                }
                val content = line.substring(firstBlank + 1)
                return Triple(mark, indent, content)
            }
        }

        fun removePrecedingMark(line: String): String {
            return detectPrecedingMark(line).third
        }

        /**
         * detect [Mark.TD] if start with '-'
         */
        private fun detectTodo(line: String, indent: Indent): Triple<String, Indent, String>? {
            val endIndex = indent.length + Mark.TD.defaultMark.length + 1 // +1 blank
            if (endIndex > line.length) return null // not long enough, so no TD mark
            if (line[endIndex - 1] != ' ') return null // no following blank, pass
            val mark = line.substring(indent.length until endIndex - 1)
            val content = line.substring(endIndex)
            return TodoHandler.handleTodo(mark,
                    {
                        Triple(TodoHandler.UNCHECKED, indent, content)
                    },
                    {
                        Triple(TodoHandler.CHECKED, indent, content)
                    },
                    {
                        null
                    })
        }

    }
}