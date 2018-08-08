package me.urakalee.next2.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.provider.BaseStore
import me.urakalee.markdown.action.DayOneStrategy.Companion.removePrecedingMark
import me.urakalee.next2.model.Note
import java.io.File

/**
 * @author Uraka.Lee
 */
class NoteStore private constructor(context: Context) : BaseStore<Note>(context) {

    override fun isDbStore(): Boolean {
        return false
    }

    override fun onCreate(db: SQLiteDatabase?) {
        throw UnsupportedOperationException()
    }

    override fun afterDBCreated(db: SQLiteDatabase) {
        throw UnsupportedOperationException()
    }

    public override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw UnsupportedOperationException()
    }

    public override fun fillModel(note: Note, cursor: Cursor) {
        throw UnsupportedOperationException()
    }

    override fun fillContentValues(values: ContentValues, note: Note) {
        throw UnsupportedOperationException()
    }

    fun getNotes(notebook: Notebook): List<Note> {
        return listNote(notebook)
    }

    /**
     * @return note with previewContent
     */
    fun getNotePreview(note: Note) {
        val noteFile = note.file ?: return
        val firstLine = noteFile.readLines().firstOrNull() ?: return
        note.previewContent = removePrecedingMark(firstLine)
    }

    override fun saveModel(note: Note) {
        val noteFile = noteFile(note) ?: return
        val content = note.content ?: throw IllegalArgumentException("content is null")
        // TODO: 检查重名, 如果有, 则修改 title, 并通知上层更新
        when {
            note.isNewNote -> {
                if (noteFile.exists()) {
                    throw RuntimeException("${note.title} already exists")
                }
                noteFile.parentFile.mkdirs()
                noteFile.writeText(content)
                note.finishNew()
            }
            note.needRename() -> {
                if (noteFile.exists()) {
                    throw RuntimeException("${note.title} already exists")
                }
                noteFile.writeText(content)
                note.originFile!!.delete()
                note.finishRename()
            }
            else -> {
                noteFile.writeText(content)
            }
        }
    }

    override fun update(model: Note) {
        throw UnsupportedOperationException()
    }

    override fun update(model: Note, toStatus: ItemStatus) {
        when (toStatus) {
            ItemStatus.DELETED -> {
                delete(model)
            }
            else ->
                throw UnsupportedOperationException()
        }
    }

    private fun delete(note: Note) {
        val noteFile = noteFile(note) ?: return
        if (noteFile.isFile) {
            if (noteFile.delete()) {
                if (isDirEmpty(noteFile.parentFile)) {
                    noteFile.parentFile.delete()
                }
            } else {
                throw RuntimeException("delete note failed")
            }
        } else {
            throw IllegalArgumentException("note is not file")
        }
    }

    private fun noteFile(note: Note): File? {
        val notebookNonNull = note.notebook ?: throw IllegalArgumentException("notebook is null")
        if (note.isNewNote) {
            note.generateTimePath()
        }
        if (note.timePath == null) {
            throw IllegalArgumentException("note without time-path")
        }
        val noteRoot = File(storageRoot(), notebookNonNull.title)
        val timeRoot = File(noteRoot, note.timePath)
        if (note.needRename()) {
            val originFile = File(timeRoot, note.originFileName)
            if (!originFile.isFile) {
                throw IllegalArgumentException("origin note not exists")
            }
            note.originFile = originFile
        }
        return File(timeRoot, note.fileName)
    }

    companion object {

        private var me: NoteStore? = null

        fun getInstance(): NoteStore {
            if (me == null) {
                synchronized(NoteStore::class.java) {
                    if (me == null) {
                        me = NoteStore(PalmApp.getContext())
                    }
                }
            }
            return me!!
        }
    }
}
