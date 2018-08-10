package me.urakalee.next2.storage

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.provider.BaseStore

/**
 * Created by wangshouheng on 2017/8/19.
 */
class NotebookStore private constructor() : BaseStore<Notebook>(PalmApp.getContext()) {

    override fun onCreate(db: SQLiteDatabase) {
        throw UnsupportedOperationException()
    }

    override fun afterDBCreated(db: SQLiteDatabase) {
        throw UnsupportedOperationException()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw UnsupportedOperationException()
    }

    override fun fillModel(model: Notebook, cursor: Cursor) {
        throw UnsupportedOperationException()
    }

    override fun fillContentValues(values: ContentValues, model: Notebook) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    override fun get(whereSQL: String?, orderSQL: String?): List<Notebook> {
        return notebooks
    }

    @Synchronized
    override fun get(whereSQL: String?, orderSQL: String?, status: ItemStatus?, exclude: Boolean): List<Notebook> {
        return notebooks
    }

    private val notebooks: List<Notebook>
        get() {
            return listNotebook()
        }

    @Synchronized
    override fun saveModel(notebook: Notebook) {
        val dir = getFile(notebook.title)
        when {
            notebook.needCreate() -> {
                if (dir.exists()) {
                    throw RuntimeException("Target exists")
                }
                dir.mkdir()
            }
            notebook.needRename() -> {
                ensureMoveDir(notebook.originTitle, notebook.title)
                notebook.reset()
            }
            else -> {
                ensureDir(dir)
            }
        }
    }

    @Synchronized
    override fun update(model: Notebook) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    override fun update(model: Notebook, toStatus: ItemStatus) {
        throw UnsupportedOperationException()
    }

    @Synchronized
    fun delete(notebook: Notebook) {
        val dir = getFile(notebook.title)
        if (isDirEmpty(dir)) {
            dir.delete()
        }
    }

    companion object {

        private var me: NotebookStore? = null

        val instance: NotebookStore
            get() {
                if (me == null) {
                    synchronized(NotebookStore::class.java) {
                        if (me == null) {
                            me = NotebookStore()
                        }
                    }
                }
                return me!!
            }
    }
}
