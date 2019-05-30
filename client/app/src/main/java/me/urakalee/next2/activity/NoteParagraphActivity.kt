package me.urakalee.next2.activity

import android.app.Activity
import android.content.Intent
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.util.ToastUtils
import me.urakalee.next2.model.Note
import me.urakalee.ranger.extension.putToBundle

class NoteParagraphActivity : NoteActivity() {

    private var startLineIndex = -1
    private var paragraphSize = 0

    private lateinit var originalNoteContent: String

    override fun handleIntent(): Boolean {
        if (!super.handleIntent()) {
            return false
        }
        // 获得 paragraph 行信息
        startLineIndex = intent.getIntExtra(ARG_START_LINE_INDEX, startLineIndex)
        if (startLineIndex < 0) {
            ToastUtils.makeToast("Invalid startLineIndex: $startLineIndex")
            return false
        }
        paragraphSize = intent.getIntExtra(ARG_PARAGRAPH_SIZE, paragraphSize)
        if (paragraphSize <= 0) {
            ToastUtils.makeToast("Invalid paragraphSize: $paragraphSize")
            return false
        }
        // 替换 note.content 为 paragraph 的内容
        originalNoteContent = note.content ?: return false
        note.content = originalNoteContent.lines()
            .subList(startLineIndex, startLineIndex + paragraphSize).joinToString("\n")
        return true
    }

    private var paragraphNoteContent: String? = null

    override fun beforePersist() {
        paragraphNoteContent = note.content
        val lines = originalNoteContent.lines()
        val before = lines.subList(0, startLineIndex)
        val after = lines.subList(startLineIndex + paragraphSize, lines.size)
        note.content = before.joinToString("\n") + "\n" +
            paragraphNoteContent + "\n" + after.joinToString("\n")
    }

    override fun afterPersist() {
        note.content = paragraphNoteContent
    }

    override val supportEditParagraph = false

    companion object {

        private val TAG: String = NoteParagraphActivity::class.java.canonicalName

        val ARG_START_LINE_INDEX = "$TAG.ARG_START_LINE_INDEX"
        val ARG_PARAGRAPH_SIZE = "$TAG.ARG_PARAGRAPH_SIZE"

        fun editNote(activity: Activity?, note: Note, startLineIndex: Int, paragraphSize: Int, requestCode: Int = -1) {
            val activityNotNull = activity ?: return
            val intent = Intent(activityNotNull, NoteParagraphActivity::class.java)
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_EDIT)
            intent.putExtra(ARG_START_LINE_INDEX, startLineIndex)
            intent.putExtra(ARG_PARAGRAPH_SIZE, paragraphSize)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}