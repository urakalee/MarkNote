package me.shouheng.notepal.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.notepal.model.Note;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.provider.schema.NoteSchema;
import me.urakalee.next2.storage.FileStorage;


/**
 * Created by wangshouheng on 2017/5/12.
 */
public class NotesStore extends BaseStore<Note> {

    private static NotesStore sInstance = null;

    public static NotesStore getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (NotesStore.class) {
                if (sInstance == null) {
                    sInstance = new NotesStore(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private NotesStore(final Context context) {
        super(context);
    }

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {
    }

    @Override
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
            case 2:
                db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_IMAGE + " TEXT");
            case 4:
                db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_CONTENT + " TEXT");
                break;
            case 5:
                // 判断指定的两个列是否存在，如果不存在的话就创建列
                Cursor cursor = null;
                try {
                    cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0 ", null);
                    boolean isExist = cursor != null && cursor.getColumnIndex(NoteSchema.PREVIEW_IMAGE) != -1;
                    if (!isExist) {
                        db.execSQL("ALTER TABLE gt_note ADD COLUMN " + NoteSchema.PREVIEW_IMAGE + " TEXT");
                    }
                } finally {
                    if (null != cursor && !cursor.isClosed()) {
                        closeCursor(cursor);
                    }
                }
                break;
        }
    }

    @Override
    public void fillModel(Note note, Cursor cursor) {
        note.setParentCode(cursor.getLong(cursor.getColumnIndex(NoteSchema.PARENT_CODE)));
        note.setTitle(cursor.getString(cursor.getColumnIndex(NoteSchema.TITLE)));
        note.setContentCode(cursor.getLong(cursor.getColumnIndex(NoteSchema.CONTENT_CODE)));
        note.setTags(cursor.getString(cursor.getColumnIndex(NoteSchema.TAGS)));
        note.setTreePath(cursor.getString(cursor.getColumnIndex(NoteSchema.TREE_PATH)));
        String preUri = cursor.getString(cursor.getColumnIndex(NoteSchema.PREVIEW_IMAGE));
        note.setPreviewImage(TextUtils.isEmpty(preUri) ? null : Uri.parse(preUri));
        note.setPreviewContent(cursor.getString(cursor.getColumnIndex(NoteSchema.PREVIEW_CONTENT)));
    }

    @Override
    protected void fillContentValues(ContentValues values, Note note) {
        values.put(NoteSchema.PARENT_CODE, note.getParentCode());
        values.put(NoteSchema.TITLE, note.getTitle());
        values.put(NoteSchema.CONTENT_CODE, note.getContentCode());
        values.put(NoteSchema.TAGS, note.getTags());
        values.put(NoteSchema.TREE_PATH, note.getTreePath());
        Uri uri = note.getPreviewImage();
        values.put(NoteSchema.PREVIEW_IMAGE, uri == null ? null : uri.toString());
        values.put(NoteSchema.PREVIEW_CONTENT, note.getPreviewContent());
    }

    public List<Note> getNotes(Notebook notebook) {
        File noteRoot = new File(FileStorage.storageRoot(), notebook.getTitle());
        List<Note> notes = new LinkedList<>();
        if (!noteRoot.isDirectory()) {
            return notes;
        }
        for (File file : FileStorage.listFiles(noteRoot)) {
            Note note = new Note();
            note.setTitle(file.getName());
            notes.add(note);
        }
        return notes;
    }
}
