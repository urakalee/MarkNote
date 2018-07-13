package me.urakalee.next2.storage

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.provider.BaseStore
import java.util.*

/**
 * Created by wangshouheng on 2017/8/19.
 */
class NotebookStore private constructor() : BaseStore<Notebook>(PalmApp.getContext()) {

    override fun onCreate(db: SQLiteDatabase) {}

    override fun afterDBCreated(db: SQLiteDatabase) {}

    public override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    public override fun fillModel(model: Notebook, cursor: Cursor) {}

    override fun fillContentValues(values: ContentValues, model: Notebook) {}

    @Synchronized
    override fun get(whereSQL: String?, orderSQL: String?): List<Notebook> {
        return notebooks
    }

    @Synchronized
    override fun get(whereSQL: String?, orderSQL: String?, status: ItemStatus?, exclude: Boolean): List<Notebook> {
        return notebooks
    }

    /**
     * Get notebooks of given status. Here are mainly two cases match:
     * 1).Notes count of given notebook > 0;
     * 2).The notebook itself is in given status.
     *
     * @return the notebooks
     */
    private val notebooks: List<Notebook>
        get() {
            val notebookRoot = storageRoot()
            val notebooks = LinkedList<Notebook>()
            for (file in listDirs(notebookRoot, false)) {
                val notebook = Notebook()
                notebook.title = file.name
                notebooks.add(notebook)
            }
            return notebooks
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
