package me.urakalee.markdown.action

import android.widget.EditText

/**
 * @author Uraka.Lee
 */
interface ActionStrategy {

    fun h1(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun list(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText)

    fun todo(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText)

    fun indent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun dedent(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun quote(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun bold(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun italic(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun codeBlock(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun strike(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun horizontalLine(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun xml(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun link(source: String, selectionStart: Int, selectionEnd: Int, title: String, link: String, editor: EditText)

    fun table(source: String, selectionStart: Int, selectionEnd: Int, rows: Int, cols: Int, editor: EditText)

    fun image(source: String, selectionStart: Int, selectionEnd: Int, title: String, imgUri: String, editor: EditText)

    fun mark(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun mathJax(source: String, selectionStart: Int, selectionEnd: Int, exp: String, isSingleLine: Boolean, editor: EditText)

    fun sub(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun sup(source: String, selectionStart: Int, selectionEnd: Int, selection: String, editor: EditText)

    fun footNote(source: String, selectionStart: Int, selectionEnd: Int, editor: EditText)
}
