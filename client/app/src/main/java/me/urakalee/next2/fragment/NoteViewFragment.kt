package me.urakalee.next2.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.kennyc.bottomsheet.BottomSheet
import com.kennyc.bottomsheet.BottomSheetListener
import kotlinx.android.synthetic.main.fragment_note_view.*
import me.shouheng.notepal.PalmApp
import me.shouheng.notepal.R
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.config.Constants.*
import me.shouheng.notepal.dialog.OpenResolver
import me.shouheng.notepal.model.Attachment
import me.shouheng.notepal.model.ModelFactory
import me.shouheng.notepal.provider.CategoryStore
import me.shouheng.notepal.util.*
import me.shouheng.notepal.viewmodel.CategoryViewModel
import me.urakalee.next2.activity.NoteActivity
import me.urakalee.next2.base.fragment.BaseFragment
import me.urakalee.next2.model.Note
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.*

/**
 * @author Uraka.Lee
 */
class NoteViewFragment : BaseFragment() {

    private var note: Note? = null
    private var noteTitle: String = "无标题"
        get() = note?.title ?: field
    private var noteContent: String = ""
        get() = note?.content ?: field
    private var isPreview = false

    private var content: String? = null
    private var tags: String? = null

    private var isContentChanged = false

    override val layoutResId: Int
        get() = R.layout.fragment_note_view

    //region lifecycle

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onBackPressed() {
        if (!isPreview && isContentChanged) {
            val argsNonNull = arguments ?: return
            if (argsNonNull.containsKey(Constants.EXTRA_REQUEST_CODE)) {
                val intent = Intent()
                intent.putExtra(Constants.EXTRA_MODEL, note as Serializable)
                if (argsNonNull.containsKey(Constants.EXTRA_POSITION)) {
                    intent.putExtra(Constants.EXTRA_POSITION, argsNonNull.getInt(Constants.EXTRA_POSITION, 0))
                }
                activity?.setResult(Activity.RESULT_OK, intent)
            }
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_FOR_EDIT -> if (resultCode == Activity.RESULT_OK) {
                isContentChanged = true
                note = data?.getSerializableExtra(Constants.EXTRA_MODEL) as? Note
                tags = note?.tagsName
                refreshLayout()
            }
        }
    }

    private fun refreshLayout() {
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = noteTitle

        mdView.parseMarkdown(noteContent)
    }

    //endregion
    //region init

    override fun afterViewCreated(savedInstanceState: Bundle?) {
        if (!handleArguments()) {
            activity?.finish()
            return
        }

        configToolbar()

        configViews()
    }

    private fun handleArguments(): Boolean {
        val argsNonNull = arguments ?: return false

        note = argsNonNull.getSerializable(Constants.EXTRA_MODEL) as? Note
        if (note == null) {
            ToastUtils.makeToast(R.string.text_no_such_note)
            return false
        }

        val categories = CategoryStore.getInstance(context).getCategories(note)
        tags = CategoryViewModel.getTagsName(categories)

        isPreview = argsNonNull.getBoolean(EXTRA_IS_PREVIEW)
        if (!isPreview) {
            val noteNonNull = note!!
            val noteFile = noteNonNull.file
            LogUtils.d("noteFile: $noteFile")
            if (noteFile == null) {
                ToastUtils.makeToast(R.string.note_failed_to_get_note_content)
                return false
            }
            try {
                content = FileUtils.readFileToString(noteFile, "utf-8")
            } catch (e: IOException) {
                LogUtils.d("IOException: $e")
                ToastUtils.makeToast(R.string.note_failed_to_read_file)
            }
            noteNonNull.content = content
        }

        return true
    }

    private fun configToolbar() {
        val actionBarNonNull = (activity as? AppCompatActivity)?.supportActionBar ?: return
        actionBarNonNull.title = noteTitle
    }

    private fun configViews() {
        mdView.fastScrollDelegate.setThumbDrawable(PalmApp.getDrawableCompact(
                if (isDarkTheme) R.drawable.fast_scroll_bar_dark else R.drawable.fast_scroll_bar_light))
        mdView.fastScrollDelegate.setThumbSize(16, 40)
        mdView.fastScrollDelegate.setThumbDynamicHeight(false)
        mdView.setHtmlResource(isDarkTheme)
        mdView.parseMarkdown(noteContent)
        mdView.setOnImageClickedListener { url, urls ->
            val attachments = ArrayList<Attachment>()
            var clickedAttachment: Attachment? = null
            for (item in urls) {
                val attachment = getAttachmentFormUrl(item)
                attachments.add(attachment)
                if (item == url) clickedAttachment = attachment
            }
            AttachmentHelper.resolveClickEvent(context, clickedAttachment, attachments, noteTitle)
        }
        mdView.setOnAttachmentClickedListener { url ->
            if (!url.isNullOrBlank()) {
                val uri = Uri.parse(url)

                // Open the http or https link from chrome tab.
                if (SCHEME_HTTP.equals(uri.scheme, true)
                        || SCHEME_HTTPS.equals(uri.scheme, true)) {
                    IntentUtils.openWebPage(context, url)
                    return@setOnAttachmentClickedListener
                }

                // Open the files of given format.
                if (url.endsWith(_3GP) || url.endsWith(_MP4)) {
                    startActivity(uri, VIDEO_MIME_TYPE)
                } else if (url.endsWith(_PDF)) {
                    startActivity(uri, PDF_MIME_TYPE)
                } else {
                    val fragmentNonNull = fragmentManager ?: return@setOnAttachmentClickedListener
                    OpenResolver.newInstance { mimeType ->
                        startActivity(uri, mimeType.mimeType)
                    }.show(fragmentNonNull, "OPEN RESOLVER")
                }
            }
        }
    }

    private fun getAttachmentFormUrl(url: String): Attachment {
        val uri = Uri.parse(url)
        val attachment = ModelFactory.getAttachment()
        attachment.uri = uri
        attachment.mineType = Constants.MIME_TYPE_IMAGE
        return attachment
    }

    private fun startActivity(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, mimeType)
        val contextNonNull = context ?: return
        if (IntentUtils.isAvailable(contextNonNull, intent, null)) {
            startActivity(intent)
        } else {
            ToastUtils.makeToast(R.string.activity_not_found_to_resolve)
        }
    }

    //endregion
    //region menu

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (!isPreview) {
            inflater?.inflate(R.menu.note_view_menu, menu)
            menu?.findItem(R.id.action_find)?.let {
                initSearchView(it.actionView as SearchView)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSearchView(searchView: SearchView?) {
        searchView?.queryHint = getString(R.string.text_find_in_page)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                mdView.findAllAsync(query)
                (activity as? AppCompatActivity)?.startSupportActionMode(ActionModeCallback())
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val contextNonNull = context ?: return super.onOptionsItemSelected(item)
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.action_edit -> {
                note?.let {
                    NoteActivity.editNote(this, it, REQUEST_FOR_EDIT)
                }
            }
            R.id.action_share -> share()
            R.id.action_labs -> ModelHelper.showLabels(context, tags)
            R.id.action_copy_link -> {
                note?.let {
                    ModelHelper.copyLink(contextNonNull, it)
                }
            }
            R.id.action_copy_content -> {
                ModelHelper.copyToClipboard(contextNonNull, content)
                ToastUtils.makeToast(R.string.content_was_copied_to_clipboard)
            }
            R.id.action_statistic -> {
                note?.let {
                    it.content = content
                    ModelHelper.showStatistic(contextNonNull, it)
                }
            }
            R.id.action_export -> export()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun share() {
        val activityNonNull = activity ?: return
        BottomSheet.Builder(activityNonNull)
                .setStyle(if (isDarkTheme) R.style.BottomSheet_Dark else R.style.BottomSheet)
                .setMenu(ColorUtils.getThemedBottomSheetMenu(context, R.menu.share))
                .setTitle(R.string.text_share)
                .setListener(object : BottomSheetListener {

                    override fun onSheetShown(bottomSheet: BottomSheet, o: Any?) {}

                    override fun onSheetItemSelected(bottomSheet: BottomSheet, menuItem: MenuItem, o: Any?) {
                        when (menuItem.itemId) {
                            R.id.action_share_text -> ModelHelper.share(context, noteTitle, content, ArrayList())
                            R.id.action_share_html -> outHtml(true)
                            R.id.action_share_image -> createWebCapture(mdView, FileHelper.OnSavedToGalleryListener {
                                ModelHelper.shareFile(context, it, Constants.MIME_TYPE_IMAGE)
                            })
                        }
                    }

                    override fun onSheetDismissed(bottomSheet: BottomSheet, o: Any?, i: Int) {}
                })
                .show()
    }

    private fun export() {
        val activityNonNull = activity ?: return
        BottomSheet.Builder(activityNonNull)
                .setStyle(if (isDarkTheme) R.style.BottomSheet_Dark else R.style.BottomSheet)
                .setMenu(ColorUtils.getThemedBottomSheetMenu(context, R.menu.export))
                .setTitle(R.string.text_export)
                .setListener(object : BottomSheetListener {

                    override fun onSheetShown(bottomSheet: BottomSheet, o: Any?) {}

                    override fun onSheetItemSelected(bottomSheet: BottomSheet, menuItem: MenuItem, o: Any?) {
                        when (menuItem.itemId) {
                            R.id.export_html ->
                                outHtml(false)
                            R.id.capture -> createWebCapture(mdView, FileHelper.OnSavedToGalleryListener {
                                ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), it.path))
                            })
                            R.id.print -> PrintUtils.print(context, mdView, note)
                            R.id.export_text -> outText(false)
                        }
                    }

                    override fun onSheetDismissed(bottomSheet: BottomSheet, o: Any?, i: Int) {}
                })
                .show()
    }

    private fun outHtml(isShare: Boolean) {
        mdView.outHtml { html ->
            try {
                val exportDir = FileHelper.getHtmlExportDir()
                val outFile = File(exportDir, FileHelper.getDefaultFileName(Constants.EXPORTED_HTML_EXTENSION))
                FileUtils.writeStringToFile(outFile, html, "utf-8")
                if (isShare) {
                    // Share, do share option
                    ModelHelper.shareFile(context, outFile, Constants.MIME_TYPE_HTML)
                } else {
                    // Not share, just show a message
                    ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), outFile.path))
                }
            } catch (e: IOException) {
                ToastUtils.makeToast(R.string.failed_to_create_file)
            }
        }
    }

    private fun outText(isShare: Boolean) {
        try {
            val exDir = FileHelper.getTextExportDir()
            val outFile = File(exDir, FileHelper.getDefaultFileName(Constants.EXPORTED_TEXT_EXTENSION))
            FileUtils.writeStringToFile(outFile, noteContent, "utf-8")
            if (isShare) {
                // Share, do share option
                ModelHelper.shareFile(context, outFile, Constants.MIME_TYPE_FILES)
            } else {
                // Not share, just show a message
                ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), outFile.path))
            }
        } catch (e: IOException) {
            ToastUtils.makeToast(R.string.failed_to_create_file)
        }

    }

    //endregion

    private inner class ActionModeCallback : ActionMode.Callback {

        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            actionMode.menuInflater.inflate(R.menu.note_find_action, menu)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.action_close -> actionMode.finish()
                R.id.action_next -> mdView.findNext(true)
                R.id.action_last -> mdView.findNext(false)
            }
            return true
        }

        override fun onDestroyActionMode(actionMode: ActionMode) {
            mdView.clearMatches()
        }
    }

    companion object {

        private const val REQUEST_FOR_EDIT = 0x01

        fun newInstance(note: Note, isPreview: Boolean, requestCode: Int?): NoteViewFragment {
            val args = Bundle()
            args.putSerializable(Constants.EXTRA_MODEL, note)
            requestCode?.let {
                args.putInt(Constants.EXTRA_REQUEST_CODE, it)
            }
            args.putBoolean(Constants.EXTRA_IS_PREVIEW, isPreview)
            val fragment = NoteViewFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
