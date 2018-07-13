package me.urakalee.next2.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.RelativeLayout
import com.balysv.materialmenu.MaterialMenuDrawable
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.R
import me.shouheng.notepal.activity.MenuSortActivity
import me.shouheng.notepal.async.CreateAttachmentTask
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.databinding.FragmentNoteBinding
import me.shouheng.notepal.dialog.AttachmentPickerDialog
import me.shouheng.notepal.dialog.LinkInputDialog
import me.shouheng.notepal.dialog.MathJaxEditor
import me.shouheng.notepal.dialog.TableInputDialog
import me.shouheng.notepal.dialog.picker.NotebookPickerDialog
import me.shouheng.notepal.fragment.base.BaseModelFragment
import me.shouheng.notepal.model.Attachment
import me.shouheng.notepal.model.Category
import me.shouheng.notepal.model.Location
import me.shouheng.notepal.model.Note
import me.shouheng.notepal.model.data.LoadStatus
import me.shouheng.notepal.model.enums.ModelType
import me.shouheng.notepal.util.*
import me.shouheng.notepal.util.preferences.UserPreferences
import me.shouheng.notepal.viewmodel.AttachmentViewModel
import me.shouheng.notepal.viewmodel.BaseViewModel
import me.shouheng.notepal.viewmodel.CategoryViewModel
import me.shouheng.notepal.viewmodel.LocationViewModel
import me.shouheng.notepal.widget.FlowLayout
import me.shouheng.notepal.widget.MDItemView
import me.urakalee.next2.activity.ContentActivity
import me.urakalee.next2.viewmodel.NoteViewModel
import me.urakalee.next2.viewmodel.NotebookViewModel
import me.urakalee.ranger.extension.dp
import me.urakalee.ranger.extension.isGone
import me.urakalee.ranger.extension.isVisible
import my.shouheng.palmmarkdown.tools.MarkdownFormat
import org.polaric.colorful.BaseActivity
import org.polaric.colorful.PermissionUtils

/**
 * @author Uraka.Lee
 */
class NoteEditFragment : BaseModelFragment<Note, FragmentNoteBinding>() {

    private var materialMenu: MaterialMenuDrawable? = null

    private var note: Note? = null
    private var categories: List<Category>? = null

    private var noteViewModel: NoteViewModel? = null
    private var notebookViewModel: NotebookViewModel? = null
    private var categoryViewModel: CategoryViewModel? = null
    private var attachmentViewModel: AttachmentViewModel? = null
    private var locationViewModel: LocationViewModel? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_note
    }

    //region lifecycle

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_MENU_SORT -> addFormatMenus()
            }
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }

    //endregion
    //region init

    override fun doCreateView(savedInstanceState: Bundle?) {
        initViewModels()

        if (!handleArguments()) {
            activity?.finish()
            return
        }

        configToolbar()

        // Notify that the content is changed if the note fragment is called from sharing and other third part
        // The code must be here since the material menu might be null.
        if (arguments != null && arguments!!.getBoolean(EXTRA_IS_THIRD_PART)) {
            setContentChanged()
        }

        // Sync methods. Note that the other data may not be fetched for current.
        configMain(note!!)

        configDrawer(note!!)
    }

    private fun initViewModels() {
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        notebookViewModel = ViewModelProviders.of(this).get(NotebookViewModel::class.java)
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        attachmentViewModel = ViewModelProviders.of(this).get(AttachmentViewModel::class.java)
    }

    private fun handleArguments(): Boolean {
        note = arguments?.get(Constants.EXTRA_MODEL) as? Note
        if (note == null) {
            ToastUtils.makeToast(R.string.text_no_such_note)
            activity?.finish()
            return false
        }

        // Handle arguments for intent from third part
        if (arguments?.getBoolean(EXTRA_IS_THIRD_PART) == true) {
            handleThirdPart()
        } else {
            val action = arguments?.getString(EXTRA_ACTION)
            when (action) {
                Constants.ACTION_ADD_SKETCH ->
                    PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.sketch(this) }
                Constants.ACTION_TAKE_PHOTO ->
                    PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.capture(this) }
                Constants.ACTION_ADD_FILES ->
                    PermissionUtils.checkStoragePermission(activity as? BaseActivity) { AttachmentHelper.pickFiles(this) }
                else ->
                    // The cases above is new model, don't need to fetch data.
                    fetchData(note!!)
            }
        }

        return true
    }

    private fun handleThirdPart() {
        if (activity !is OnNoteInteractListener) return

        val intent = (activity as OnNoteInteractListener).intentForThirdPart

        val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        note?.title = title

        var content = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!content.isNullOrEmpty()) {
            content = content.replace("\t", TAB_REPLACEMENT)
        }
        note?.content = content

        // Single attachment data
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        // Due to the fact that Google Now passes intent as text but with
        // audio recording attached the case must be handled in specific way
        if (uri != null && Constants.INTENT_GOOGLE_NOW != intent.action) {
            CreateAttachmentTask(this, uri, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        // Multiple attachment data
        val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        if (uris != null) {
            for (item in uris) {
                CreateAttachmentTask(this, item, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }
    }

    private fun configToolbar() {
        val activityNonNull = activity ?: return
        val contextNonNull = context ?: return

        materialMenu = MaterialMenuDrawable(contextNonNull, primaryColor(), MaterialMenuDrawable.Stroke.THIN)
        materialMenu?.iconState = MaterialMenuDrawable.IconState.ARROW
        binding?.main?.toolbar?.navigationIcon = materialMenu
        (activityNonNull as AppCompatActivity).setSupportActionBar(binding?.main?.toolbar)
        val actionBar = activityNonNull.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = ""
        setStatusBarColor(resources.getColor(if (isDarkTheme) R.color.dark_theme_foreground else R.color.md_grey_500))
    }

    //endregion
    // region fetch data

    private fun fetchData(note: Note) {
        fetchNoteContent(note)
        fetchNotebook(note)
        fetchCategories(note)
        fetchLocation(note)
    }

    private fun fetchNoteContent(note: Note) {
        // 恢复现场，不需要重新加载数据
        if (!note.content.isNullOrEmpty()
                && arguments?.getBoolean(KEY_ARGS_RESTORE) == true) {
            return
        }

        attachmentViewModel
                ?.readNoteContent(note)
                ?.observe(this, Observer { contentResource ->
                    if (contentResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_load_data)
                        return@Observer
                    }
                    when (contentResource.status) {
                        LoadStatus.SUCCESS -> {
                            note.content = contentResource.data
                            binding?.main?.etContent?.tag = true
                            binding?.main?.etContent?.setText(note.content)
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.note_failed_to_read_file)
                        else -> {
                            // pass
                        }
                    }
                })
    }

    private fun fetchNotebook(note: Note) {
        notebookViewModel
                ?.get(note.parentCode)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_load_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        LoadStatus.SUCCESS -> if (notebookResource.data != null) {
                            binding?.main?.tvFolder?.text = notebookResource.data?.title
                            notebookResource.data?.color?.let {
                                binding?.main?.tvFolder?.setTextColor(it)
                            }
                        }
                        else -> {
                            // pass
                        }
                    }
                })
    }

    private fun fetchCategories(note: Note) {
        categoryViewModel
                ?.getCategories(note)
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

    private fun fetchLocation(note: Note) {
        locationViewModel
                ?.getLocation(note)
                ?.observe(this, Observer { locationResource ->
                    if (locationResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_load_data)
                        return@Observer
                    }
                    when (locationResource.status) {
                        LoadStatus.SUCCESS -> showLocationInfo(locationResource.data)
                        else -> {
                            // pass
                        }
                    }
                })
    }

    // endregion
    // region Config main board

    private fun configMain(note: Note) {
        binding?.main?.etTitle?.setText(note.title)
        binding?.main?.etTitle?.setTextColor(primaryColor())
        binding?.main?.etTitle?.addTextChangedListener(titleWatcher)

        binding?.main?.etContent?.setText(note.content)
        binding?.main?.etContent?.addTextChangedListener(contentWatcher)

        binding?.main?.llFolder?.setOnClickListener { showNotebookPicker() }

        binding?.main?.rlBottomEditors?.isGone = true

        val ids = intArrayOf(R.id.iv_redo, R.id.iv_undo, R.id.iv_insert_picture, R.id.iv_insert_link, R.id.iv_table)
        for (id in ids) {
            binding.root.findViewById<View>(id).setOnClickListener({ this.onBottomBarClick(it) })
        }

        addFormatMenus()

        binding?.main?.ivEnableFormat?.setOnClickListener { toggleFormatBar() }
        binding?.main?.ivSetting?.setOnClickListener { MenuSortActivity.start(this@NoteEditFragment, REQ_MENU_SORT) }

        binding?.main?.fssv?.fastScrollDelegate?.setThumbSize(16, 40)
        binding?.main?.fssv?.fastScrollDelegate?.setThumbDynamicHeight(false)
        if (context != null) {
            binding?.main?.fssv?.fastScrollDelegate?.setThumbDrawable(
                    PalmApp.getDrawableCompact(
                            if (isDarkTheme) R.drawable.fast_scroll_bar_dark
                            else R.drawable.fast_scroll_bar_light
                    ))
        }
    }

    private fun showNotebookPicker() {
        fragmentManager?.let {
            NotebookPickerDialog.newInstance().setOnItemSelectedListener { dialog, value, _ ->
                note?.parentCode = value.code
                note?.treePath = value.treePath + "|" + value.code
                binding?.main?.tvFolder?.text = value.title
                binding?.main?.tvFolder?.setTextColor(value.color)
                setContentChanged()
                dialog.dismiss()
            }.show(it, "NOTEBOOK_PICKER")
        }
    }

    private fun onBottomBarClick(v: View) {
        when (v.id) {
            R.id.iv_undo -> binding?.main?.etContent?.undo()
            R.id.iv_redo -> binding?.main?.etContent?.redo()
            R.id.iv_insert_picture -> showAttachmentPicker()
            R.id.iv_insert_link -> showLinkEditor()
            R.id.iv_table -> showTableEditor()
        }
    }

    private fun addFormatMenus() {
        binding?.main?.llContainer?.removeAllViews()
        val padding = 12.dp
        val markdownFormats = UserPreferences.getInstance().markdownFormats
        for (markdownFormat in markdownFormats) {
            val mdItemView = MDItemView(context)
            mdItemView.markdownFormat = markdownFormat
            mdItemView.setPadding(padding, padding, padding, padding)
            binding?.main?.llContainer?.addView(mdItemView)
            mdItemView.setOnClickListener {
                if (markdownFormat == MarkdownFormat.CHECKBOX
                        || markdownFormat == MarkdownFormat.CHECKBOX_OUTLINE) {
                    binding?.main?.etContent?.addCheckbox("", markdownFormat == MarkdownFormat.CHECKBOX)
                } else if (markdownFormat == MarkdownFormat.MATH_JAX) {
                    showMarkJaxEditor()
                } else {
                    binding?.main?.etContent?.addEffect(markdownFormat)
                }
            }
        }
    }

    private fun showMarkJaxEditor() {
        fragmentManager?.let {
            MathJaxEditor.newInstance { exp, isSingleLine ->
                binding?.main?.etContent?.addMathJax(exp, isSingleLine)
            }.show(it, "MATH JAX EDITOR")
        }
    }

    private fun toggleFormatBar() {
        val rlBottomEditors = binding?.main?.rlBottomEditors ?: return
        val ivEnableFormat = binding?.main?.ivEnableFormat ?: return
        val rlBottomVisible = rlBottomEditors.isVisible
        rlBottomEditors.isGone = rlBottomVisible // toggle
        ivEnableFormat.setImageDrawable(
                ColorUtils.tintDrawable(
                        resources.getDrawable(R.drawable.ic_text_format_black_24dp),
                        if (rlBottomVisible) Color.WHITE else primaryColor()
                ))
        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ivEnableFormat.height * if (rlBottomVisible) 1 else 2)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        binding?.main?.rlBottom?.layoutParams = params
    }

    private fun showAttachmentPicker() {
        fragmentManager?.let {
            AttachmentPickerDialog.Builder(this)
                    .setRecordVisible(false)
                    .setVideoVisible(false)
                    .setAddLinkVisible(true)
                    .setFilesVisible(true)
                    .setOnAddNetUriSelectedListener({ this.addImageLink() })
                    .build()
                    .show(it, "Attachment picker")
        }
    }

    private fun addImageLink() {
        fragmentManager?.let {
            LinkInputDialog.getInstance { title, link ->
                binding?.main?.etContent?.addLinkEffect(MarkdownFormat.ATTACHMENT, title, link)
            }.show(it, "Link Image")
        }
    }

    private fun showLinkEditor() {
        fragmentManager?.let {
            LinkInputDialog.getInstance { title, link ->
                binding?.main?.etContent?.addLinkEffect(MarkdownFormat.LINK, title, link)
            }.show(it, "LINK INPUT")
        }
    }

    private fun showTableEditor() {
        fragmentManager?.let {
            TableInputDialog.getInstance { rowsStr, colsStr ->
                val rows = StringUtils.parseInteger(rowsStr, 3)
                val cols = StringUtils.parseInteger(colsStr, 3)
                binding?.main?.etContent?.addTableEffect(rows, cols)
            }.show(it, "TABLE INPUT")
        }
    }

    // endregion
    // region Config drawer board

    private fun configDrawer(note: Note) {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding?.drawer?.drawerToolbar?.setNavigationOnClickListener { binding.drawerLayout.closeDrawer(GravityCompat.END) }
        if (isDarkTheme) {
            binding?.drawer?.drawerToolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            binding?.drawer?.root?.setBackgroundResource(R.color.dark_theme_background)
        }

        updateCharsInfo()
        binding?.drawer?.tvTimeInfo?.text = ModelHelper.getTimeInfo(note)

        binding?.drawer?.flLabels?.setOnClickListener { showCategoriesPicker(categories) }
        binding?.drawer?.tvAddLabels?.setOnClickListener { showCategoriesPicker(categories) }

        binding?.drawer?.tvAddLocation?.setOnClickListener { tryToLocate() }

        binding?.drawer?.tvCopyLink?.setOnClickListener {
            activity?.let {
                ModelHelper.copyLink(it, note)
            }
        }

        binding?.drawer?.tvCopyText?.setOnClickListener {
            note.content = binding?.main?.etContent?.text.toString()
            activity?.let {
                ModelHelper.copyToClipboard(it, binding?.main?.etContent?.text.toString())
                ToastUtils.makeToast(R.string.content_was_copied_to_clipboard)
            }
        }

        binding?.drawer?.tvAddToHomeScreen?.setOnClickListener { addShortcut() }

        binding?.drawer?.tvStatistics?.setOnClickListener { showStatistics() }
    }

    private fun updateCharsInfo() {
        val charsInfo = getString(R.string.text_chars_number) + " : " + binding?.main?.etContent?.text.toString().length
        binding?.drawer?.tvCharsInfo?.text = charsInfo
    }

    private fun showStatistics() {
        val noteNonNull = note ?: return
        noteNonNull.content = binding?.main?.etContent?.text.toString()
        context?.let {
            ModelHelper.showStatistic(it, noteNonNull)
        }
    }

    // endregion
    //region override BaseModelFragment

    override fun setContentChanged() {
        if (!isContentChanged) {
            super.setContentChanged()
            materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.CHECK)
        }
    }

    override fun getModel(): Note? {
        return note
    }

    override fun getViewModel(): BaseViewModel<Note>? {
        return noteViewModel
    }

    override fun beforeSaveOrUpdate(handler: BaseModelFragment.BeforePersistEventHandler) {
        val noteNonNull = note ?: return
        val noteContent = binding?.main?.etContent?.text.toString()
        noteNonNull.content = noteContent

        // Get note title from title editor or note content
        val inputTitle = binding?.main?.etTitle?.text.toString()
        noteNonNull.title = ModelHelper.getNoteTitle(inputTitle, noteContent)

        // Get preview image from note content
        noteNonNull.previewImage = ModelHelper.getNotePreviewImage(noteContent)

        noteNonNull.previewContent = ModelHelper.getNotePreview(noteContent)

        attachmentViewModel
                ?.writeNoteContent(noteNonNull)
                ?.observe(this, Observer { attachmentResource ->
                    if (attachmentResource == null) {
                        ToastUtils.makeToast(R.string.text_error_when_save)
                        return@Observer
                    }
                    when (attachmentResource.status) {
                        LoadStatus.SUCCESS -> {
                            attachmentResource.data?.code?.let {
                                noteNonNull.contentCode = it
                            }
                            handler.onGetEventResult(true)
                        }
                        LoadStatus.FAILED -> {
                            ToastUtils.makeToast(R.string.text_error_when_save)
                            handler.onGetEventResult(false)
                        }
                        else -> {
                            // pass
                        }
                    }
                })
    }

    override fun afterSaveOrUpdate() {
        super.afterSaveOrUpdate()
        materialMenu!!.animateIconState(MaterialMenuDrawable.IconState.ARROW)
        note?.content = binding?.main?.etContent?.text.toString()

        val args = arguments ?: return
        if (args.getBoolean(EXTRA_IS_THIRD_PART)
                || args.getString(EXTRA_ACTION) == Constants.ACTION_ADD_SKETCH
                || args.getString(EXTRA_ACTION) == Constants.ACTION_TAKE_PHOTO
                || args.getString(EXTRA_ACTION) == Constants.ACTION_ADD_FILES) {
            sendNoteChangeBroadcast()
        }
    }

    private fun sendNoteChangeBroadcast() {
        val intent = Intent(Constants.ACTION_NOTE_CHANGE_BROADCAST)
        context?.sendBroadcast(intent)
    }

    override fun onGetSelectedCategories(categories: List<Category>) {
        val noteNonNull = note ?: return
        this.categories = categories
        noteNonNull.tags = CategoryViewModel.getTags(categories)
        val tagsName = CategoryViewModel.getTagsName(categories)
        noteNonNull.tagsName = tagsName
        addTagsToLayout(tagsName)
        setContentChanged()
    }

    override fun getTagsLayout(): FlowLayout? {
        return binding?.drawer?.flLabels
    }

    override fun onGetLocation(location: Location) {
        val note = note ?: return
        location.modelCode = note.code
        location.modelType = ModelType.NOTE
        showLocationInfo(location)
        locationViewModel?.saveModel(location)
    }

    /**
     * Show location information, if the location is null, hide the widget else show it.
     *
     * @param location location info
     */
    private fun showLocationInfo(location: Location?) {
        if (location == null) {
            binding?.drawer?.tvLocationInfo?.isGone = true
            return
        }
        binding?.drawer?.tvLocationInfo?.isVisible = true
        binding?.drawer?.tvLocationInfo?.text = ModelHelper.getFormatLocation(location)
    }

    //endregion
    //region override BaseFragment

    override fun onGetAttachment(attachment: Attachment) {
        val note = note ?: return
        attachment.modelCode = note.code
        attachment.modelType = ModelType.NOTE
        attachmentViewModel
                ?.saveModel(attachment)
                ?.observe(this, Observer { LogUtils.d(it) })

        val context = context ?: return
        var title = FileHelper.getNameFromUri(context, attachment.uri)
        title = if (title.isNullOrEmpty()) getString(R.string.text_attachment) else title

        if (Constants.MIME_TYPE_IMAGE.equals(attachment.mineType, true)
                || Constants.MIME_TYPE_SKETCH.equals(attachment.mineType, true)) {
            binding?.main?.etContent?.addLinkEffect(MarkdownFormat.ATTACHMENT, title, attachment.uri.toString())
        } else {
            binding?.main?.etContent?.addLinkEffect(MarkdownFormat.LINK, title, attachment.uri.toString())
        }
    }

    override fun onFailedGetAttachment(attachment: Attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment)
    }

    //endregion
    //region menu

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.note_editor_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        if (isDarkTheme) {
            menu?.findItem(R.id.action_more)?.setIcon(R.drawable.ic_more_vert_white)
            menu?.findItem(R.id.action_preview)?.setIcon(R.drawable.ic_visibility_white_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (isContentChanged) saveOrUpdateData(null)
                else setResult()
            }
            R.id.action_more -> binding.drawerLayout.openDrawer(GravityCompat.END, true)
            R.id.action_preview -> {
                note?.let {
                    it.title = binding?.main?.etTitle?.text.toString()
                    it.content = binding?.main?.etContent?.text.toString()
                    ContentActivity.viewNote(this, it, true, 0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    private val titleWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            note?.title = editable.toString()
            setContentChanged()
            updateCharsInfo()
        }
    }

    private val contentWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            // Ignore the text change if the tag is true
            if (binding?.main?.etContent?.tag != null
                    || binding?.main?.etContent?.tag is Boolean && binding?.main?.etContent?.tag as Boolean) {
                binding?.main?.etContent?.tag = null
            } else {
                note?.content = s.toString()
                setContentChanged()
                updateCharsInfo()
            }
        }
    }

    interface OnNoteInteractListener {
        val intentForThirdPart: Intent
    }

    companion object {

        const val KEY_ARGS_RESTORE = "key_args_restore"

        private const val EXTRA_IS_THIRD_PART = "extra_is_third_part"
        private const val EXTRA_ACTION = "extra_action"

        private const val REQ_MENU_SORT = 0x0101

        private const val TAB_REPLACEMENT = "    "

        fun newInstance(note: Note, requestCode: Int?, isThirdPart: Boolean, action: String?): NoteEditFragment {
            val args = Bundle()
            args.putBoolean(EXTRA_IS_THIRD_PART, isThirdPart)
            args.putSerializable(Constants.EXTRA_MODEL, note)
            if (requestCode != null) args.putInt(Constants.EXTRA_REQUEST_CODE, requestCode)
            if (action != null) args.putString(EXTRA_ACTION, action)
            val fragment = NoteEditFragment()
            fragment.arguments = args
            return fragment
        }
    }
}