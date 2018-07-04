package me.shouheng.notepal.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.LinkedList;
import java.util.List;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.adapter.NotesAdapter;
import me.shouheng.notepal.adapter.NotesAdapter.MultiItem;
import me.shouheng.notepal.model.Category;
import me.shouheng.notepal.model.Note;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.data.Resource;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.BaseStore;
import me.shouheng.notepal.provider.NotesStore;
import me.shouheng.notepal.provider.helper.ArchiveHelper;
import me.shouheng.notepal.provider.helper.NotebookHelper;
import me.shouheng.notepal.provider.helper.TrashHelper;

/**
 * Created by wang shouheng on 2018/3/13.
 */
public class NoteRepository extends BaseRepository<Note> {

    @Override
    protected BaseStore<Note> getStore() {
        return NotesStore.getInstance(PalmApp.getContext());
    }

    public LiveData<Resource<List<MultiItem>>> getMultiItems(
            ItemStatus status, @Nullable Notebook notebook, @Nullable Category category) {
        MutableLiveData<Resource<List<MultiItem>>> result = new MutableLiveData<>();
        new NoteLoadTask(result, status, notebook, category).execute();
        return result;
    }

    public static class NoteLoadTask extends AsyncTask<Void, Integer, List<NotesAdapter.MultiItem>> {

        private MutableLiveData<Resource<List<MultiItem>>> result;
        private ItemStatus status;
        private Notebook notebook;
        private Category category;

        NoteLoadTask(MutableLiveData<Resource<List<MultiItem>>> result,
                ItemStatus status, Notebook notebook, Category category) {
            this.result = result;
            this.status = status;
            this.notebook = notebook;
            this.category = category;
        }

        @Override
        protected List<NotesAdapter.MultiItem> doInBackground(Void... voids) {
            List<NotesAdapter.MultiItem> dataList = new LinkedList<>();
            for (Object obj : getNotesAndNotebooks(status, notebook, category)) {
                if (obj instanceof Note) {
                    dataList.add(new NotesAdapter.MultiItem((Note) obj));
                } else if (obj instanceof Notebook) {
                    dataList.add(new NotesAdapter.MultiItem((Notebook) obj));
                }
            }
            return dataList;
        }

        @Override
        protected void onPostExecute(List<NotesAdapter.MultiItem> dataList) {
            result.setValue(Resource.success(dataList));
        }
    }

    @WorkerThread
    private static List getNotesAndNotebooks(
            ItemStatus status, @Nullable Notebook notebook, @Nullable Category category) {
        if (category != null) {
            return status == ItemStatus.ARCHIVED ?
                    ArchiveHelper.getNotebooksAndNotes(PalmApp.getContext(), category) :
                    status == ItemStatus.TRASHED ?
                            TrashHelper.getNotebooksAndNotes(PalmApp.getContext(), category) :
                            NotebookHelper.getNotesAndNotebooks(PalmApp.getContext(), category);
        } else {
            return status == ItemStatus.ARCHIVED ?
                    ArchiveHelper.getNotebooksAndNotes(PalmApp.getContext(), notebook) :
                    status == ItemStatus.TRASHED ?
                            TrashHelper.getNotebooksAndNotes(PalmApp.getContext(), notebook) :
                            NotebookHelper.getNotesAndNotebooks(PalmApp.getContext(), notebook);
        }
    }
}
