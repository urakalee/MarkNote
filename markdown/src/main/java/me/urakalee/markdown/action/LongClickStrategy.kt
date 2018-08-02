package me.urakalee.markdown.action

import android.widget.EditText
import me.urakalee.markdown.Mark
import me.urakalee.markdown.action.DayOneStrategy.Companion.detectPrecedingMark
import me.urakalee.ranger.extension.selectedLine

class LongClickStrategy : ActionStrategy {

    override fun h1(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun todo(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun indent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dedent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun quote(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bold(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun italic(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun codeBlock(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun strike(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        val (targetLine, start, end) = source.selectedLine(selectionStart, selectionEnd)
        val (precedingMarkSource, indent, content) = detectPrecedingMark(targetLine)
        val newContent = Mark.handleTextMarkLongClick(Mark.STRIKE, content)
        val markLength = if (precedingMarkSource.isEmpty()) 0 else precedingMarkSource.length + 1
        val contentStart = start + indent.length + markLength
        editor.text?.replace(contentStart, contentStart + content.length, newContent)
        editor.setSelection(end + newContent.length - content.length) // 简单处理, 光标放在行尾
    }

    override fun horizontalLine(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun xml(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun link(source: String, selectionStart: Int, selectionEnd: Int, title: String, link: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun table(source: String, selectionStart: Int, selectionEnd: Int, rows: Int, cols: Int, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun image(source: String, selectionStart: Int, selectionEnd: Int, title: String, imgUri: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mark(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mathJax(source: String, selectionStart: Int, selectionEnd: Int, exp: String, isSingleLine: Boolean, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sub(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sup(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun footNote(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}