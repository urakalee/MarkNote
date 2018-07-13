package me.shouheng.notepal.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import me.shouheng.notepal.async.NormalAsyncTask;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.data.Resource;
import me.urakalee.next2.storage.NotebookStore;

/**
 * Created by Employee on 2018/3/13.
 */
public class NotebookRepository extends BaseRepository<Notebook> {

    @Override
    protected NotebookStore getStore() {
        return NotebookStore.Companion.getInstance();
    }

    public LiveData<Resource<Notebook>> delete(Notebook notebook) {
        MutableLiveData<Resource<Notebook>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            getStore().delete(notebook);
            return notebook;
        }).execute();
        return result;
    }
}
