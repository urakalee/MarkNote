package me.urakalee.next2.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.note_drawer_edit.*
import kotlinx.android.synthetic.main.note_fragment_edit.*
import kotlinx.android.synthetic.main.note_include_edit.*
import me.shouheng.notepal.R
import me.shouheng.notepal.activity.MenuSortActivity
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.dialog.AttachmentPickerDialog
import me.shouheng.notepal.dialog.LinkInputDialog
import me.shouheng.notepal.dialog.MathJaxEditor
import me.shouheng.notepal.dialog.TableInputDialog
import me.shouheng.notepal.dialog.picker.NotebookPickerDialog
import me.shouheng.notepal.model.Attachment
import me.shouheng.notepal.model.Category
import me.shouheng.notepal.model.data.LoadStatus
import me.shouheng.notepal.model.enums.ModelType
import me.shouheng.notepal.util.AttachmentHelper
import me.shouheng.notepal.util.FileHelper
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ModelHelper
import me.shouheng.notepal.util.StringUtils
import me.shouheng.notepal.util.ToastUtils
import me.shouheng.notepal.util.preferences.UserPreferences
import me.shouheng.notepal.viewmodel.AttachmentViewModel
import me.shouheng.notepal.viewmodel.CategoryViewModel
import me.shouheng.notepal.widget.FlowLayout
import me.shouheng.notepal.widget.MDItemView
import me.urakalee.next2.base.activity.BaseActivity
import me.urakalee.next2.base.fragment.BaseModelFragment
import me.urakalee.next2.config.FeatureConfig
import me.urakalee.next2.model.Note
import me.urakalee.next2.support.permission.PermissionUtils
import me.urakalee.next2.viewmodel.NoteViewModel
import me.urakalee.next2.viewmodel.NotebookViewModel
import me.urakalee.ranger.extension.dp
import me.urakalee.ranger.extension.pixel
import my.shouheng.palmmarkdown.tools.MarkdownFormat

/**
 * @author Uraka.Lee
 */
class NoteEditFragment : BaseModelFragment<Note>() {

    private var categories: List<Category>? = null

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var notebookViewModel: NotebookViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var attachmentViewModel: AttachmentViewModel

    override val layoutResId: Int
        get() = R.layout.note_fragment_edit

    //region lifecycle

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_MENU_SORT -> renderFormatBar()
            }
        }
    }

    //endregion
    //region init

    override fun afterViewCreated(savedInstanceState: Bundle?) {
        initViewModels()

        configMain()

        configDrawer()

        initData()
    }

    private fun initViewModels() {
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        notebookViewModel = ViewModelProviders.of(this).get(NotebookViewModel::class.java)
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        attachmentViewModel = ViewModelProviders.of(this).get(AttachmentViewModel::class.java)
    }

    private fun initData() {
        val action = delegate.getAction()
        when (action) {
            Constants.ACTION_ADD_SKETCH ->
                PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.sketch(this) }
            Constants.ACTION_TAKE_PHOTO ->
                PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.capture(this) }
            Constants.ACTION_ADD_FILES ->
                PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.pickFiles(this) }
            else ->
                // The cases above is new model, don't need to fetch data.
                fetchData()
        }
    }

    //endregion
    // region fetch data

    private fun fetchData() {
        val note = delegate.getNote()
        fetchCategories(note)
        fetchAttachment(note)
    }

    private fun fetchCategories(note: Note) {
        categoryViewModel.getCategories(note)
            ?.observe(this, Observer { listResource ->
                if (listResource == null) {
                    ToastUtils.makeToast(R.string.text_failed_to_load_data)
                    return@Observer
                }
                when (listResource.status) {
                    LoadStatus.SUCCESS -> {
                        categories = listResource.data
                        addTagsToLayout(CategoryViewModel.getTagsName(listResource.data))
                    }
                    else -> {
                        // pass
                    }
                }
            })
    }

    private fun fetchAttachment(note: Note) {
        // 恢复现场，不需要重新加载数据
        if (!note.content.isNullOrEmpty()
            && arguments?.getBoolean(KEY_ARGS_RESTORE) == true) {
            return
        }

        attachmentViewModel.readNoteContent(note)
            ?.observe(this, Observer { contentResource ->
                if (contentResource == null) {
                    ToastUtils.makeToast(R.string.text_failed_to_load_data)
                    return@Observer
                }
                when (contentResource.status) {
                    LoadStatus.SUCCESS -> {
                        note.content = contentResource.data
                        noteContent.tag = true
                        noteContent.setText(note.content)
                    }
                    LoadStatus.FAILED -> ToastUtils.makeToast(R.string.note_failed_to_read_file)
                    else -> {
                        // pass
                    }
                }
            })
    }

    // endregion
    // region Config main board

    private fun configMain() {
        val note = delegate.getNote()

        noteTitle.setText(note.title)
        noteTitle.setTextColor(primaryColor())
        noteTitle.addTextChangedListener(titleWatcher)

        notebook.setOnClickListener { showNotebookPicker() }
        notebook.isEnabled = FeatureConfig.MOVE_NOTE
        note.notebook?.let {
            notebookName.text = it.title
        }

        noteContent.setText(note.content)
        noteContent.addTextChangedListener(contentWatcher)

        renderFormatBar()

        btnSetting.setOnClickListener { MenuSortActivity.start(this@NoteEditFragment, REQ_MENU_SORT) }
    }

    private fun showNotebookPicker() {
        fragmentManager?.let {
            NotebookPickerDialog.newInstance().setOnItemSelectedListener { dialog, value, _ ->
                notebookName.text = value.title
                notebookName.setTextColor(value.color)
                delegate.setContentChanged(true)
                dialog.dismiss()
            }.show(it, "NOTEBOOK_PICKER")
        }
    }

    private fun renderFormatBar() {
        markContainer.removeAllViews()
        val verticalPadding = 12.dp
        val horizontalPadding = 6.dp
        val itemHeight = R.dimen.note_bottom_menu_height.pixel
        val layoutParams = ViewGroup.LayoutParams(itemHeight - (verticalPadding - horizontalPadding) * 2, itemHeight)
        val markdownFormats = UserPreferences.getInstance().markdownFormats
        for (markdownFormat in markdownFormats) {
            val mdItemView = MDItemView(context)
            mdItemView.markdownFormat = markdownFormat
            mdItemView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            markContainer.addView(mdItemView, layoutParams)
            mdItemView.setOnClickListener {
                if (markdownFormat == MarkdownFormat.MATH_JAX) {
                    showMathJaxEditor()
                } else {
                    noteContent.addEffect(markdownFormat)
                }
            }
            mdItemView.setOnLongClickListener {
                noteContent.addLongClickEffect(markdownFormat)
                true
            }
        }
    }

    private fun showMathJaxEditor() {
        fragmentManager?.let {
            MathJaxEditor.newInstance { exp, isSingleLine ->
                noteContent.addMathJax(exp, isSingleLine)
            }.show(it, "MATH JAX EDITOR")
        }
    }

    // endregion
    // region Config drawer board

    private fun configDrawer() {
        val note = delegate.getNote()

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        drawerToolbar.setNavigationOnClickListener { drawerLayout.closeDrawer(GravityCompat.END) }

        updateCharsInfo()
        timeInfo.text = ModelHelper.getTimeInfo(note)

        labelsLayout.setOnClickListener { showCategoriesPicker(categories) }
        addLabel.setOnClickListener { showCategoriesPicker(categories) }

        val views = listOf(insertFile, insertLink, insertTable)
        for (view in views) {
            view.setOnClickListener { this.onDrawerClick(it) }
        }

        copyLink.setOnClickListener {
            activity?.let {
                ModelHelper.copyLink(it, note)
            }
        }

        copyText.setOnClickListener {
            note.content = noteContent.text.toString()
            activity?.let {
                ModelHelper.copyToClipboard(it, noteContent.text.toString())
                ToastUtils.makeToast(R.string.content_was_copied_to_clipboard)
            }
        }
    }

    private fun updateCharsInfo() {
        val charsInfo = getString(R.string.text_chars_number) + " : " + noteContent.text.toString().length
        wordCount.text = charsInfo
    }

    private fun onDrawerClick(v: View) {
        when (v) {
            insertFile -> showAttachmentPicker()
            insertLink -> showLinkEditor()
            insertTable -> showTableEditor()
//            undo -> noteContent.undo()
//            redo -> noteContent.redo()
        }
    }

    private fun showAttachmentPicker() {
        fragmentManager?.let {
            AttachmentPickerDialog.Builder(this)
                .setRecordVisible(false)
                .setVideoVisible(false)
                .setAddLinkVisible(true)
                .setFilesVisible(true)
                .setOnAddNetUriSelectedListener { this.addImageLink() }
                .build()
                .show(it, "Attachment picker")
        }
    }

    private fun addImageLink() {
        fragmentManager?.let {
            LinkInputDialog.getInstance { title, link ->
                noteContent.addLinkEffect(MarkdownFormat.ATTACHMENT, title, link)
            }.show(it, "Link Image")
        }
    }

    private fun showLinkEditor() {
        fragmentManager?.let {
            LinkInputDialog.getInstance { title, link ->
                noteContent.addLinkEffect(MarkdownFormat.LINK, title, link)
            }.show(it, "LINK INPUT")
        }
    }

    private fun showTableEditor() {
        fragmentManager?.let {
            TableInputDialog.getInstance { rowsStr, colsStr ->
                val rows = StringUtils.parseInteger(rowsStr, 3)
                val cols = StringUtils.parseInteger(colsStr, 3)
                noteContent.addTableEffect(rows, cols)
            }.show(it, "TABLE INPUT")
        }
    }

    // endregion

    // endregion
    //region override BaseModelFragment

    override fun onGetSelectedCategories(categories: List<Category>) {
        val note = delegate.getNote()

        this.categories = categories
        note.tags = CategoryViewModel.getTags(categories)
        val tagsName = CategoryViewModel.getTagsName(categories)
        note.tagsName = tagsName
        addTagsToLayout(tagsName)

        delegate.setContentChanged(true)
    }

    override val tagsLayout: FlowLayout?
        get() = labelsLayout

    //endregion
    //region override BaseFragment

    override fun onGetAttachment(attachment: Attachment) {
        val note = delegate.getNote()

        attachment.modelCode = note.code
        attachment.modelType = ModelType.NOTE
        attachmentViewModel.saveModel(attachment)
            ?.observe(this, Observer { LogUtils.d(it) })

        var title = FileHelper.getNameFromUri(attachment.uri)
        title = if (title.isNullOrEmpty()) getString(R.string.text_attachment) else title

        if (Constants.MIME_TYPE_IMAGE.equals(attachment.mineType, true)
            || Constants.MIME_TYPE_SKETCH.equals(attachment.mineType, true)) {
            noteContent.addLinkEffect(MarkdownFormat.ATTACHMENT, title, attachment.uri.toString())
        } else {
            noteContent.addLinkEffect(MarkdownFormat.LINK, title, attachment.uri.toString())
        }
    }

    override fun onFailedGetAttachment(attachment: Attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment)
    }

    //endregion
    //region content

    /**
     * HACK: 避免在 next 模式下保存之后, 回到 edit 模式时, 导致多余的 contentChange
     */
    private var triggeredByRefresh = false

    fun refreshData() {
        if (noteContent.text.toString() != delegate.getNote().content) {
            triggeredByRefresh = true
            noteContent.setText(delegate.getNote().content)
        }
    }

    /**
     * @param inEditNote: 使用 NoteEditFragment 里 edit-text 中的内容; 如果在其他 fragment 里, 应该直接用 note 中的内容
     */
    fun saveOrUpdateData(inEditNote: Boolean = true, afterPersist: ((Boolean) -> Unit) = {}) {
        beforeSaveOrUpdate(inEditNote) { writeAttachmentSuccess ->
            if (writeAttachmentSuccess) {
                doPersist {
                    delegate.afterPersist()
                    afterPersist.invoke(it)
                }
            } else {
                delegate.afterPersist()
            }
        }
    }

    private fun beforeSaveOrUpdate(inEditNote: Boolean, afterWriteAttachment: ((Boolean) -> Unit)?) {
        val note = delegate.getNote()

        if (inEditNote) {
            // Get note title from title editor or note content
            val noteContent = noteContent.text.toString()
            note.content = noteContent
            val inputTitle = noteTitle.text.toString()
            note.title = ModelHelper.getNoteTitle(inputTitle, noteContent)

            // Get preview image from note content
            note.previewImage = ModelHelper.getNotePreviewImage(noteContent)
            note.previewContent = ModelHelper.getNotePreview(noteContent)
        } else {
            // Get preview image from note content
            note.previewImage = ModelHelper.getNotePreviewImage(note.content)
            note.previewContent = ModelHelper.getNotePreview(note.content)
        }

        delegate.beforePersist()
        attachmentViewModel.writeAttachment(note)
            ?.observe(this, Observer { attachmentResource ->
                if (attachmentResource == null) {
                    ToastUtils.makeToast(R.string.text_error_when_save)
                    return@Observer
                }
                when (attachmentResource.status) {
                    LoadStatus.SUCCESS -> {
                        attachmentResource.data?.code?.let {
                            note.attachmentCode = it
                        }
                        afterWriteAttachment?.invoke(true)
                    }
                    LoadStatus.FAILED -> {
                        ToastUtils.makeToast(R.string.text_error_when_save)
                        afterWriteAttachment?.invoke(false)
                    }
                    else -> {
                        // pass
                    }
                }
            })
    }

    /**
     * Save the model to db if it is new, otherwise update the existed one.
     */
    private fun doPersist(handler: ((Boolean) -> Unit)?) {
        noteViewModel.saveModel(delegate.getNote())
            ?.observe(this, Observer { resource ->
                if (resource == null) {
                    ToastUtils.makeToast(R.string.text_error_when_save)
                    return@Observer
                }
                when (resource.status) {
                    LoadStatus.SUCCESS -> {
                        ToastUtils.makeToast(R.string.text_save_successfully)
                        delegate.setContentChanged(false)
                        afterSaveOrUpdate()
                        handler?.invoke(true)
                    }
                    LoadStatus.FAILED -> {
                        ToastUtils.makeToast(R.string.text_error_when_save)
                        handler?.invoke(false)
                    }
                    else -> {
                        // pass
                    }
                }
            })
    }

    private fun afterSaveOrUpdate() {
        val action = delegate.getAction() ?: return
        if (action == Constants.ACTION_ADD_SKETCH
            || action == Constants.ACTION_TAKE_PHOTO
            || action == Constants.ACTION_ADD_FILES) {
            sendNoteChangeBroadcast()
        }
    }

    private fun sendNoteChangeBroadcast() {
        val intent = Intent(Constants.ACTION_NOTE_CHANGE_BROADCAST)
        context?.sendBroadcast(intent)
    }

    //endregion
    //region menu

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_more -> drawerLayout.openDrawer(GravityCompat.END, true)
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    private val titleWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            delegate.getNote().title = s.toString()
            delegate.setContentChanged(true)
            updateCharsInfo()
        }
    }

    private val contentWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            // Ignore the text change if the tag is true
            if (noteContent.tag != null
                || noteContent.tag is Boolean && noteContent.tag as Boolean) {
                noteContent.tag = null
            } else {
                delegate.getNote().content = s.toString()
                if (!triggeredByRefresh) {
                    delegate.setContentChanged(true)
                }
                triggeredByRefresh = false
                updateCharsInfo()
            }
        }
    }

    lateinit var delegate: NoteEditFragmentDelegate

    interface NoteEditFragmentDelegate {

        fun getNote(): Note

        fun getAction(): String?

        fun beforePersist()

        fun afterPersist()

        fun setContentChanged(contentChanged: Boolean)

        fun saveIfNeed()

        val supportEditParagraph: Boolean
    }

    companion object {

        const val KEY_ARGS_RESTORE = "key_args_restore"

        private const val REQ_MENU_SORT = 0x0101
    }
}
