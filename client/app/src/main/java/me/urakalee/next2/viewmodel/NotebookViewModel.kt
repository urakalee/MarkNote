package me.urakalee.next2.viewmodel

import android.arch.lifecycle.LiveData
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.data.Resource
import me.shouheng.notepal.repository.NotebookRepository
import me.shouheng.notepal.viewmodel.BaseViewModel

/**
 * @author Uraka.Lee
 */
class NotebookViewModel : BaseViewModel<Notebook>() {

    override fun getRepository(): NotebookRepository {
        return NotebookRepository()
    }

    fun list(): LiveData<Resource<List<Notebook>>> {
        return repository.get(null, null)
    }

    fun delete(notebook: Notebook): LiveData<Resource<Notebook>> {
        return repository.delete(notebook)
    }
}
