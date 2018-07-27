package me.shouheng.notepal.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import me.shouheng.notepal.model.Category;
import me.urakalee.next2.model.Note;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.data.Resource;
import me.shouheng.notepal.provider.BaseStore;
import me.urakalee.next2.storage.NoteStore;

/**
 * Created by wang shouheng on 2018/3/13.
 */
public class NoteRepository extends BaseRepository<Note> {

    @Override
    protected BaseStore<Note> getStore() {
        return NoteStore.Companion.getInstance();
    }

    public LiveData<Resource<List<Note>>> get(@NonNull Notebook notebook) {
        MutableLiveData<Resource<List<Note>>> result = new MutableLiveData<>();
        new NoteLoadTask(result, notebook, null).execute();
        return result;
    }

    private static class NoteLoadTask extends AsyncTask<Void, Integer, List<Note>> {

        private MutableLiveData<Resource<List<Note>>> result;
        private Notebook notebook;
        private Category category;

        NoteLoadTask(MutableLiveData<Resource<List<Note>>> result,
                @NonNull Notebook notebook, @Nullable Category category) {
            this.result = result;
            this.notebook = notebook;
            this.category = category;
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            return NoteStore.Companion.getInstance().getNotes(notebook);
        }

        @Override
        protected void onPostExecute(List<Note> dataList) {
            result.setValue(Resource.success(dataList));
        }
    }
}
