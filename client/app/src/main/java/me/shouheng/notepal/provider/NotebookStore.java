package me.shouheng.notepal.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.helper.StoreHelper;
import me.shouheng.notepal.provider.helper.TimelineHelper;
import me.shouheng.notepal.provider.schema.BaseSchema;
import me.shouheng.notepal.provider.schema.NoteSchema;
import me.shouheng.notepal.provider.schema.NotebookSchema;
import me.urakalee.next2.storage.FileStorage;

/**
 * Created by wangshouheng on 2017/8/19.
 */
public class NotebookStore extends BaseStore<Notebook> {

    private static NotebookStore sInstance = null;

    public static NotebookStore getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NotebookStore.class) {
                if (sInstance == null) {
                    sInstance = new NotebookStore(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private NotebookStore(Context context) {
        super(context);
    }

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {
    }

    @Override
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void fillModel(Notebook model, Cursor cursor) {
        model.setTitle(cursor.getString(cursor.getColumnIndex(NotebookSchema.TITLE)));
        model.setColor(cursor.getInt(cursor.getColumnIndex(NotebookSchema.COLOR)));
        model.setTreePath(cursor.getString(cursor.getColumnIndex(NotebookSchema.TREE_PATH)));
        int noteCount;
        if ((noteCount = cursor.getColumnIndex(NotebookSchema.COUNT)) != -1) {
            model.setCount(cursor.getInt(noteCount));
        }
    }

    @Override
    protected void fillContentValues(ContentValues values, Notebook model) {
        values.put(NotebookSchema.TITLE, model.getTitle());
        values.put(NotebookSchema.COLOR, model.getColor());
        values.put(NotebookSchema.TREE_PATH, model.getTreePath());
    }

    /**
     * Try not to use this method to update notebook`s status. Since it only update the notebook
     * itself. To update the notebook, you should use {@link #update(Notebook, ItemStatus, ItemStatus)}
     * method which will update the notebooks and notes associated as well.
     *
     * @param model    the notebook to update
     * @param toStatus the given status to update to
     */
    @Deprecated
    @Override
    public synchronized void update(Notebook model, ItemStatus toStatus) {
        super.update(model, toStatus);
    }

    /**
     * @param model      notebook to update
     * @param fromStatus the status of the notebook list, Note: this status differs from the status
     *                   of given notebook. Because, for example, the notebook in archive that showed
     *                   to the user may not in {@link ItemStatus#ARCHIVED} state. The list may include
     *                   the notebook of status {@link ItemStatus#NORMAL} too.
     * @param toStatus   the status to update to
     */
    public synchronized void update(Notebook model, ItemStatus fromStatus, ItemStatus toStatus) {
        if (model == null || toStatus == null) return;
        TimelineHelper.addTimeLine(model, StoreHelper.getStatusOperation(toStatus));
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {

            /*
             * Update current notebook itself OF GIVEN STATUS. */
            database.execSQL(" UPDATE " + tableName
                            + " SET " + BaseSchema.STATUS + " = " + toStatus.id + " , " + BaseSchema.LAST_MODIFIED_TIME
                            + " = ? "
                            + " WHERE " + BaseSchema.CODE + " = " + model.getCode()
                            + " AND " + BaseSchema.USER_ID + " = " + userId,
                    new String[]{String.valueOf(System.currentTimeMillis())});

            /*
             * Update the status of all associated notebooks OF GIVEN STATUS. */
            database.execSQL(" UPDATE " + tableName
                            + " SET " + BaseSchema.STATUS + " = " + toStatus.id + " , " + BaseSchema.LAST_MODIFIED_TIME
                            + " = ? "
                            + " WHERE " + NotebookSchema.TREE_PATH + " LIKE '" + model.getTreePath() + "'||'%'"
                            + " AND " + BaseSchema.USER_ID + " = " + userId
                            + " AND " + BaseSchema.STATUS + " = " + fromStatus.id,
                    new String[]{String.valueOf(System.currentTimeMillis())});

            /*
             * Update the status of all associated notes OF GIVEN STATUS. */
            database.execSQL(" UPDATE " + NoteSchema.TABLE_NAME
                            + " SET " + BaseSchema.STATUS + " = " + toStatus.id + " , " + BaseSchema.LAST_MODIFIED_TIME
                            + " = ? "
                            + " WHERE " + NoteSchema.TREE_PATH + " LIKE '" + model.getTreePath() + "'||'%'"
                            + " AND " + BaseSchema.USER_ID + " = " + userId
                            + " AND " + BaseSchema.STATUS + " = " + fromStatus.id,
                    new String[]{String.valueOf(System.currentTimeMillis())});

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    @Override
    public synchronized List<Notebook> get(String whereSQL, String orderSQL) {
        return getNotebooks(null, null, null);
    }

    @Override
    public synchronized List<Notebook> get(String whereSQL, String orderSQL, ItemStatus status, boolean exclude) {
        return getNotebooks(null, null, null);
    }

    @Override
    public synchronized List<Notebook> get(String whereSQL, String[] whereArgs, String orderSQL) {
        return getNotebooks(null, null, null);
    }

    /**
     * Get notebooks of given status. Here are mainly two cases match:
     * 1).Notes count of given notebook > 0;
     * 2).The notebook itself is in given status.
     *
     * @param whereSQL where SQL
     * @param orderSQL order SQL
     * @return the notebooks
     */
    private List<Notebook> getNotebooks(String whereSQL, String orderSQL, ItemStatus status) {
        File notebookRoot = FileStorage.storageRoot();
        List<Notebook> notebooks = new LinkedList<>();
        if (!notebookRoot.isDirectory()) {
            return notebooks;
        }
        for (File file : FileStorage.listDirs(notebookRoot, false)) {
            Notebook notebook = new Notebook();
            notebook.setTitle(file.getName());
            notebooks.add(notebook);
        }
        /*
        Cursor cursor = null;
        List<Notebook> notebooks;
        SQLiteDatabase database = getWritableDatabase();
        try {
            cursor = database.rawQuery(" SELECT *, " + getNotesCount(status)
                            + " FROM " + tableName
                            + " WHERE " + NotebookSchema.USER_ID + " = ? "
                            + " AND ( " + NotebookSchema.STATUS + " = " + status.id + " OR " + NotebookSchema.COUNT +
                             " > 0 ) "
                            + (TextUtils.isEmpty(whereSQL) ? "" : " AND " + whereSQL)
                            + " GROUP BY " + NotebookSchema.CODE
                            + (TextUtils.isEmpty(orderSQL) ? "" : " ORDER BY " + orderSQL),
                    new String[]{String.valueOf(userId)});
            notebooks = getList(cursor);
        } finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        */
        return notebooks;
    }

    @Override
    public synchronized void saveModel(Notebook notebook) {
        File dir = FileStorage.getFile(notebook.getTitle());
        if (notebook.needCreate()) {
            if (dir.exists()) {
                throw new RuntimeException("Target exists");
            }
            dir.mkdir();
        } else if (notebook.needRename()) {
            FileStorage.ensureMoveDir(notebook.getOriginTitle(), notebook.getTitle());
            notebook.reset();
        } else {
            FileStorage.ensureDir(dir);
        }
    }
}
