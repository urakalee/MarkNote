package me.urakalee.next2.notelist

import android.content.Context
import android.view.View
import android.widget.ImageView
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

/**
 * @author Uraka.Lee
 */
class NotesAdapter(private val context: Context, data: List<NotesAdapter.MultiItem>)
    : BaseMultiItemQuickAdapter<NotesAdapter.MultiItem, BaseViewHolder>(data),
        BubbleTextGetter {

    private val accentColor: Int
    private val isDarkTheme: Boolean

    init {
        addItemType(MultiItem.ITEM_TYPE_NOTE, R.layout.item_note)
        addItemType(MultiItem.ITEM_TYPE_NOTEBOOK, R.layout.item_notebook)

        accentColor = ColorUtils.accentColor(context)
        isDarkTheme = ColorUtils.isDarkTheme(context)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItem) {
        if (isDarkTheme) helper.itemView.setBackgroundResource(R.color.dark_theme_background)
        when (helper.itemViewType) {
            MultiItem.ITEM_TYPE_NOTE -> convertNote(helper, item.note)
            MultiItem.ITEM_TYPE_NOTEBOOK -> convertNotebook(helper, item.notebook)
        }
        helper.addOnClickListener(R.id.iv_more)
    }

    private fun convertNote(holder: BaseViewHolder, note: Note) {
        holder.itemView.setBackgroundColor(PalmApp.getColorCompact(
                if (isDarkTheme)
                    R.color.dark_theme_background
                else
                    R.color.light_theme_background)
        )
        holder.setText(R.id.tv_note_title, note.title)
        holder.setText(R.id.tv_content, note.previewContent)
        holder.setText(R.id.tv_time, note.createTimeStr)
        holder.setTextColor(R.id.tv_time, accentColor)
        if (note.previewImage != null) {
            holder.getView<View>(R.id.iv_image).visibility = View.VISIBLE
            val thumbnailUri = FileHelper.getThumbnailUri(context, note.previewImage)
            Glide.with(PalmApp.getContext())
                    .load(thumbnailUri)
                    .centerCrop()
                    .crossFade()
                    .into(holder.getView<View>(R.id.iv_image) as ImageView)
        } else {
            holder.getView<View>(R.id.iv_image).visibility = View.GONE
        }
    }

    private fun convertNotebook(helper: BaseViewHolder, notebook: Notebook) {
        val notebookColor = notebook.color
        helper.setText(R.id.tv_note_title, notebook.title)
        val str = context.resources.getQuantityString(R.plurals.notes_number, notebook.count, notebook.count)
        helper.setText(R.id.tv_added_time, str)
        helper.setImageDrawable(R.id.iv_icon, ColorUtils.tintDrawable(
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
