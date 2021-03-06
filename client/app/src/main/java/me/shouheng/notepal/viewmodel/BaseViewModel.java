package me.shouheng.notepal.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import me.shouheng.notepal.model.Model;
import me.shouheng.notepal.model.data.Resource;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.repository.BaseRepository;

/**
 * Created by shouh on 2018/3/17.
 */
public abstract class BaseViewModel<T extends Model> extends ViewModel {

    protected abstract BaseRepository<T> getRepository();

    public LiveData<Resource<T>> get(long code) {
        return getRepository().get(code);
    }

    public LiveData<Resource<T>> saveModel(T model) {
        return getRepository().saveModel(model);
    }

    public LiveData<Resource<T>> update(T model) {
        return getRepository().update(model);
    }

    public LiveData<Resource<T>> update(T model, ItemStatus toStatus) {
        return getRepository().update(model, toStatus);
    }
}
