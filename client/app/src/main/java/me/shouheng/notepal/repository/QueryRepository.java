package me.shouheng.notepal.repository;

import android.support.annotation.MainThread;

import java.util.List;

import me.urakalee.next2.model.Note;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.schema.BaseSchema;
import me.shouheng.notepal.provider.schema.NoteSchema;
import me.shouheng.notepal.util.LogUtils;
import me.shouheng.notepal.util.tools.SearchConditions;
import me.urakalee.next2.storage.NoteStore;

/**
 * Created by WngShhng on 2017/12/11.
 */
public class QueryRepository {

    private SearchConditions conditions;

    private NoteStore noteStore;

    public QueryRepository(SearchConditions conditions) {
        this.conditions = conditions;
        LogUtils.d(conditions);
        noteStore = NoteStore.Companion.getInstance();
    }

    @MainThread
    public List<Note> getNotes(String queryString) {
        return noteStore.get(getNoteQuerySQL(queryString), NoteSchema.ADDED_TIME + " DESC ");
    }

    private String getNoteQuerySQL(String queryString) {
        return (conditions.isIncludeTags() ?
                " ( " + NoteSchema.TITLE + " LIKE '%'||'" + queryString + "'||'%' "
                        + " OR " + NoteSchema.TAGS + " LIKE '%'||'" + queryString + "'||'%' ) "
                : NoteSchema.TITLE + " LIKE '%'||'" + queryString + "'||'%'"
        ) + getQueryConditions();
    }

    private String getQueryConditions() {
        // should not query the deleted item out
        return (conditions.isIncludeArchived() ? "" : " AND " + BaseSchema.STATUS + " != " + ItemStatus.ARCHIVED.id)
                + (conditions.isIncludeTrashed() ? "" : " AND " + BaseSchema.STATUS + " != " + ItemStatus.TRASHED.id)
                + " AND " + BaseSchema.STATUS + " != " + ItemStatus.DELETED.id;
    }

    public void setConditions(SearchConditions conditions) {
        this.conditions = conditions;
    }
}
