package me.urakalee.next2.notelist

import android.content.Context
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.R
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.util.ColorUtils
import me.shouheng.notepal.util.FileHelper
import me.shouheng.notepal.widget.tools.BubbleTextGetter
import me.urakalee.next2.model.Note
import me.urakalee.next2.storage.NoteStore

/**
 * @author Uraka.Lee
 */
class NotesAdapter(private val context: Context, data: List<NotesAdapter.MultiItem>)
    : BaseMultiItemQuickAdapter<NotesAdapter.MultiItem, BaseViewHolder>(data),
        BubbleTextGetter {

    private val accentColor: Int
    private val isDarkTheme: Boolean

    init {
        addItemType(MultiItem.ITEM_TYPE_NOTE, R.layout.notes_item_note)
        addItemType(MultiItem.ITEM_TYPE_NOTEBOOK, R.layout.notes_item_notebook)

        accentColor = ColorUtils.accentColor(context)
        isDarkTheme = ColorUtils.isDarkTheme(context)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItem) {
        if (isDarkTheme) helper.itemView.setBackgroundResource(R.color.dark_theme_background)
        when (helper.itemViewType) {
            MultiItem.ITEM_TYPE_NOTE -> convertNote(helper, item.note)
            MultiItem.ITEM_TYPE_NOTEBOOK -> convertNotebook(helper, item.notebook)
        }
        helper.addOnClickListener(R.id.btnMore)
    }

    private fun convertNote(holder: BaseViewHolder, note: Note) {
        holder.itemView.setBackgroundColor(PalmApp.getColorCompact(
                if (isDarkTheme)
                    R.color.dark_theme_background
                else
                    R.color.light_theme_background)
        )
        holder.setText(R.id.noteTitle, note.title)
        if (note.previewContent == null) {
            holder.setText(R.id.noteContent, "")
            LoadPreviewContentTask(holder, note).execute()
        } else {
            holder.setText(R.id.noteContent, note.previewContent)
        }
        holder.setText(R.id.noteContent, note.previewContent)
        holder.setText(R.id.noteTime, note.createTimeStr)
        holder.setTextColor(R.id.noteTime, accentColor)
        if (note.previewImage != null) {
            holder.getView<View>(R.id.noteImage).visibility = View.VISIBLE
            val thumbnailUri = FileHelper.getThumbnailUri(context, note.previewImage)
            Glide.with(PalmApp.getContext())
                    .load(thumbnailUri)
                    .centerCrop()
                    .crossFade()
                    .into(holder.getView<View>(R.id.noteImage) as ImageView)
        } else {
            holder.getView<View>(R.id.noteImage).visibility = View.GONE
        }
    }

    private class LoadPreviewContentTask(
            val holder: BaseViewHolder,
            val note: Note) : AsyncTask<Void, Void, Note>() {

        override fun doInBackground(vararg params: Void?): Note {
            NoteStore.getInstance().getNotePreview(note)
            return note
        }

        override fun onPostExecute(result: Note) {
            if (holder.getView<TextView>(R.id.noteTitle).text == note.title) {
                holder.setText(R.id.noteContent, note.previewContent)
            }
        }
    }

    private fun convertNotebook(holder: BaseViewHolder, notebook: Notebook) {
        val notebookColor = notebook.color
        holder.setText(R.id.notebookTitle, notebook.title)
        val str = context.resources.getQuantityString(R.plurals.notes_number, notebook.count, notebook.count)
        holder.setText(R.id.notebookTime, str)
        holder.setImageDrawable(R.id.notebookIcon, ColorUtils.tintDrawable(
                context.resources.getDrawable(R.drawable.ic_folder_black_24dp), notebookColor))
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return ""
    }

    class MultiItem : MultiItemEntity {

        private var itemType: Int = 0

        lateinit var note: Note

        lateinit var notebook: Notebook

        constructor(note: Note) {
            this.note = note
            this.itemType = ITEM_TYPE_NOTE
        }

        constructor(notebook: Notebook) {
            this.notebook = notebook
            this.itemType = ITEM_TYPE_NOTEBOOK
        }

        override fun getItemType(): Int {
            return itemType
        }

        companion object {

            const val ITEM_TYPE_NOTE = 0
            const val ITEM_TYPE_NOTEBOOK = 1
        }
    }
}
