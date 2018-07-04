package me.urakalee.next2.viewmodel;

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.StringRes
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.R
import me.shouheng.notepal.adapter.NotesAdapter
import me.shouheng.notepal.async.ResourceAsyncTask
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.model.*
import me.shouheng.notepal.model.data.Resource
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.model.enums.ModelType
import me.shouheng.notepal.provider.AttachmentsStore
import me.shouheng.notepal.provider.NotesStore
import me.shouheng.notepal.repository.NoteRepository
import me.shouheng.notepal.util.FileHelper
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ModelHelper
import me.shouheng.notepal.util.preferences.NotePreferences
import me.shouheng.notepal.viewmodel.BaseViewModel
import org.apache.commons.io.FileUtils
import java.io.IOException

/**
 * @author Uraka.Lee
 */
class NoteViewModel : BaseViewModel<Note>() {

    override fun getRepository(): NoteRepository {
        return NoteRepository()
    }

    fun list(notebook: Notebook): LiveData<Resource<List<Note>>> {
        return repository.get(null, null)
    }

    fun getMultiItems(status: ItemStatus, notebook: Notebook?, category: Category?): LiveData<Resource<List<NotesAdapter.MultiItem>>> {
        return repository.getMultiItems(status, notebook, category)
    }

    fun getEmptySubTitle(status: ItemStatus?): String {
        @StringRes val resId = when (status) {
            ItemStatus.TRASHED -> R.string.notes_list_empty_sub_trashed
            ItemStatus.ARCHIVED -> R.string.notes_list_empty_sub_archived
            else -> R.string.notes_list_empty_sub_normal
        }
        return PalmApp.getContext().getString(resId)
    }

    fun saveSnagging(note: Note, snagging: MindSnagging, attachment: Attachment?): LiveData<Resource<Note>> {
        val result = MutableLiveData<Resource<Note>>()
        ResourceAsyncTask(result) {
            var content = snagging.content
            if (attachment != null) {
                // Save attachment
                attachment.modelCode = note.code
                attachment.modelType = ModelType.NOTE
                AttachmentsStore.getInstance(PalmApp.getContext()).saveModel(attachment)

                // prepare note content
                if (Constants.MIME_TYPE_IMAGE.equals(attachment.mineType, ignoreCase = true)
                        || Constants.MIME_TYPE_SKETCH.equals(attachment.mineType, ignoreCase = true)) {
                    content = content + "![](" + snagging.picture + ")"
                } else {
                    content = content + "[](" + snagging.picture + ")"
                }
            }

            // Prepare note info
            note.content = content
            note.title = ModelHelper.getNoteTitle(snagging.content, snagging.content)
            note.previewImage = snagging.picture
            note.previewContent = ModelHelper.getNotePreview(snagging.content)

            // Create note file and attach to note
            val extension = NotePreferences.getInstance().noteFileExtension
            val noteFile = FileHelper.createNewAttachmentFile(PalmApp.getContext(), extension)
            try {
                // Create note content attachment
                val atFile = ModelFactory.getAttachment()
                FileUtils.writeStringToFile(noteFile!!, note.content, "utf-8")
                atFile.uri = FileHelper.getUriFromFile(PalmApp.getContext(), noteFile)
                atFile.size = FileUtils.sizeOf(noteFile)
                atFile.path = noteFile.path
                atFile.name = noteFile.name
                atFile.modelType = ModelType.NOTE
                atFile.modelCode = note.code
                AttachmentsStore.getInstance(PalmApp.getContext()).saveModel(atFile)

                note.contentCode = atFile.code
            } catch (e: IOException) {
                LogUtils.e(e)
                return@ResourceAsyncTask Resource.error(e.message, null)
            }

            NotesStore.getInstance(PalmApp.getContext()).saveModel(note)

            // Return value
            Resource.success(note)
        }.execute()
        return result
    }
}
