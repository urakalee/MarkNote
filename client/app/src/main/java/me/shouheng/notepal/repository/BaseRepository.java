package me.shouheng.notepal.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.notepal.async.NormalAsyncTask;
import me.shouheng.notepal.model.Model;
import me.shouheng.notepal.model.data.Resource;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.BaseStore;

/**
 * Created by wang shouheng on 2018/3/13.
 */
public abstract class BaseRepository<T extends Model> {

    protected abstract BaseStore<T> getStore();

    public LiveData<Resource<T>> get(long code) {
        MutableLiveData<Resource<T>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> getStore().get(code)).execute();
        return result;
    }

    public LiveData<Resource<List<T>>> get(String whereSQL, String orderSQL) {
        MutableLiveData<Resource<List<T>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> getStore().get(whereSQL, orderSQL)).execute();
        return result;
    }

    public LiveData<Resource<T>> saveModel(T model) {
        MutableLiveData<Resource<T>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            getStore().saveModel(model);
            return model;
        }).execute();
        return result;
    }

    public LiveData<Resource<T>> update(T model) {
        MutableLiveData<Resource<T>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            getStore().update(model);
            return model;
        }).execute();
        return result;
    }

    public LiveData<Resource<T>> update(T model, ItemStatus toStatus) {
        MutableLiveData<Resource<T>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            getStore().update(model, toStatus);
            return model;
        }).execute();
        return result;
    }
}
