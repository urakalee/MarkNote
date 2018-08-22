package me.urakalee.next2.viewmodel;

import android.arch.lifecycle.LiveData
import android.support.annotation.StringRes
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.R
import me.shouheng.notepal.model.Category
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.data.Resource
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.repository.NoteRepository
import me.shouheng.notepal.viewmodel.BaseViewModel
import me.urakalee.next2.model.Note

/**
 * @author Uraka.Lee
 */
class NoteViewModel : BaseViewModel<Note>() {

    override fun getRepository(): NoteRepository {
        return NoteRepository()
    }

    fun list(notebook: Notebook, category: Category?): LiveData<Resource<List<Note>>> {
        return repository.get(notebook)
    }

    fun getEmptySubTitle(status: ItemStatus?): String {
        @StringRes val resId = when (status) {
            ItemStatus.TRASHED -> R.string.notes_list_empty_sub_trashed
            ItemStatus.ARCHIVED -> R.string.notes_list_empty_sub_archived
            else -> R.string.notes_list_empty_sub_normal
        }
        return PalmApp.getContext().getString(resId)
    }
}
