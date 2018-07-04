package me.shouheng.notepal.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.async.NormalAsyncTask;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.data.Resource;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.BaseStore;
import me.shouheng.notepal.provider.NotebookStore;

/**
 * Created by Employee on 2018/3/13. */
public class NotebookRepository extends BaseRepository<Notebook> {

    @Override
    protected BaseStore<Notebook> getStore() {
        return NotebookStore.getInstance(PalmApp.getContext());
    }

    public LiveData<Resource<Notebook>> update(Notebook model, ItemStatus fromStatus, ItemStatus toStatus) {
        MutableLiveData<Resource<Notebook>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((NotebookStore) getStore()).update(model, fromStatus, toStatus);
            return model;
        }).execute();
        return result;
    }
}
