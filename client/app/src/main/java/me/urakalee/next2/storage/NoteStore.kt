package me.urakalee.next2.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.model.Note
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.provider.BaseStore
import me.shouheng.notepal.provider.schema.NoteSchema
import java.io.File
import java.util.*

/**
 * @author Uraka.Lee
 */
class NoteStore private constructor(context: Context) : BaseStore<Note>(context) {

    override fun afterDBCreated(db: SQLiteDatabase) {}

    public override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1, 2 -> {
                db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_IMAGE + " TEXT")
                db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_CONTENT + " TEXT")
            }
            4 -> db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_CONTENT + " TEXT")
            5 -> {
                // 判断指定的两个列是否存在，如果不存在的话就创建列
                var cursor: Cursor? = null
                try {
                    cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 0 ", null)
                    val isExist = cursor != null && cursor.getColumnIndex(NoteSchema.PREVIEW_IMAGE) != -1
                    if (!isExist) {
                        db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_IMAGE + " TEXT")
                    }
                } finally {
                    if (null != cursor && !cursor.isClosed) {
                        closeCursor(cursor)
                    }
                }
            }
        }
    }

    public override fun fillModel(note: Note, cursor: Cursor) {
        note.parentCode = cursor.getLong(cursor.getColumnIndex(NoteSchema.PARENT_CODE))
        note.title = cursor.getString(cursor.getColumnIndex(NoteSchema.TITLE))
        note.contentCode = cursor.getLong(cursor.getColumnIndex(NoteSchema.CONTENT_CODE))
        note.tags = cursor.getString(cursor.getColumnIndex(NoteSchema.TAGS))
        note.treePath = cursor.getString(cursor.getColumnIndex(NoteSchema.TREE_PATH))
        val preUri = cursor.getString(cursor.getColumnIndex(NoteSchema.PREVIEW_IMAGE))
        note.previewImage = if (TextUtils.isEmpty(preUri)) null else Uri.parse(preUri)
        note.previewContent = cursor.getString(cursor.getColumnIndex(NoteSchema.PREVIEW_CONTENT))
    }

    override fun fillContentValues(values: ContentValues, note: Note) {
        values.put(NoteSchema.PARENT_CODE, note.parentCode)
        values.put(NoteSchema.TITLE, note.title)
        values.put(NoteSchema.CONTENT_CODE, note.contentCode)
        values.put(NoteSchema.TAGS, note.tags)
        values.put(NoteSchema.TREE_PATH, note.treePath)
        val uri = note.previewImage
        values.put(NoteSchema.PREVIEW_IMAGE, uri?.toString())
        values.put(NoteSchema.PREVIEW_CONTENT, note.previewContent)
    }

    fun getNotes(notebook: Notebook): List<Note> {
        val noteRoot = File(storageRoot(), notebook.title)
        val notes = LinkedList<Note>()
        for (file in listFiles(noteRoot)) {
            val note = Note()
            note.title = file.name
            notes.add(note)
        }
        return notes
    }

    @Synchronized
    override fun saveOrUpdate(note: Note) {
        val noteFile = noteFile(note) ?: return
        noteFile.writeText(note.content)
    }

    private fun isNewModel(note: Note): Boolean {
        val noteFile = noteFile(note)
        return noteFile?.isFile ?: false
    }

    private fun noteFile(note: Note): File? {
        val notebookNonNull = note.notebook ?: return null
        val noteRoot = File(storageRoot(), notebookNonNull.title)
        return File(noteRoot, note.title)
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