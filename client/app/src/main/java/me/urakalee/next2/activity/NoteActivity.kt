package me.urakalee.next2.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog.ColorCallback
import com.balysv.materialmenu.MaterialMenuDrawable
import kotlinx.android.synthetic.main.activity_note.*
import me.shouheng.notepal.R
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.fragment.base.BaseModelFragment
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ToastUtils
import me.urakalee.next2.base.activity.CommonActivity
import me.urakalee.next2.base.fragment.CommonFragment
import me.urakalee.next2.fragment.NoteEditFragment
import me.urakalee.next2.fragment.NoteNextFragment
import me.urakalee.next2.fragment.NoteViewFragment
import me.urakalee.next2.model.Note
import me.urakalee.ranger.extension.*
import org.apache.commons.io.FileUtils
import java.io.IOException

/**
 * @author Uraka.Lee
 */
class NoteActivity : CommonActivity(),
        ColorCallback {

    private lateinit var materialMenu: MaterialMenuDrawable

    private var note: Note? = null
    /**
     * Field remark that is the content changed.
     */
    private var contentChanged: Boolean = false
        set(value) {
            when (value) {
                true -> {
                    if (!contentChanged) {
                        field = true
                        materialMenu.animateIconState(MaterialMenuDrawable.IconState.CHECK)
                    }
                }
                false -> {
                    field = false
                    materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW)
                }
            }
        }
    /**
     * Have we ever saved or updated the content.
     */
    private var savedOrUpdated: Boolean = false


    override val layoutResId: Int
        get() = R.layout.activity_note

    //region lifecycle

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(BUNDLE_KEY_NOTE, note)
    }

    override fun onBackPressed() {
        if (contentChanged) {
            MaterialDialog.Builder(this)
                    .title(R.string.text_tips)
                    .content(R.string.text_save_or_discard)
                    .positiveText(R.string.text_save)
                    .negativeText(R.string.text_give_up)
                    .onPositive { _, _ ->
                        noteEditFragment?.saveOrUpdateData {
                            setResult()
                        }
                    }
                    .onNegative { _, _ ->
                        super.onBackPressed()
                    }
                    .show()
        } else {
            setResult()
        }
        // TODO: getCurrentFragment in pager
        val currentFragment = getCurrentFragment()
        if (currentFragment is CommonFragment) {
            currentFragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    private fun setResult() {
        // The model didn't change.
        if (!savedOrUpdated) {
            super.onBackPressed()
            return
        }

        var requestCode: Int? = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, -1)
        requestCode = if (requestCode == -1) null else requestCode
        // If has request code, return it, otherwise just go back
        requestCode?.let { req ->
            val intent = Intent()
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, req)
            intent.putExtra(Constants.EXTRA_MODEL, note!!)
            var position: Int? = intent.getIntExtra(Constants.EXTRA_POSITION, -1)
            position = if (position == -1) null else position
            position?.let { pos ->
                intent.putExtra(Constants.EXTRA_POSITION, pos)
            }
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    //endregion
    //region init

    override fun doCreateView(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            note = savedInstanceState.get(BUNDLE_KEY_NOTE) as? Note
        }

        if (!handleIntent()) {
            finish()
            return
        }
        configToolbar()
        configPager()
    }

    private fun handleIntent(): Boolean {
        if (intent.hasExtraInBundle(Constants.EXTRA_MODEL)) {
            note = note ?: intent.getFromBundle(Constants.EXTRA_MODEL)
            note?.let {
                if (!it.isNewNote && it.content == null) {
                    val noteFile = it.file
                    try {
                        it.content = FileUtils.readFileToString(noteFile, "utf-8")
                    } catch (e: IOException) {
                        LogUtils.d("IOException: $e")
                        ToastUtils.makeToast(R.string.note_failed_to_read_file)
                        return false
                    }
                }
                return true
            }
        }
        ToastUtils.makeToast(R.string.content_failed_to_parse_intent)
        LogUtils.d("Failed to resolve note intent : $intent")
        return false
    }

    private fun configToolbar() {
        toolbar.bringToFront()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!isDarkTheme) toolbar.popupTheme = R.style.AppTheme_PopupOverlay
        setStatusBarColor(resources.getColor(if (isDarkTheme) R.color.dark_theme_foreground else R.color.md_grey_500))

        materialMenu = MaterialMenuDrawable(this, primaryColor(), MaterialMenuDrawable.Stroke.THIN)
        materialMenu.iconState = MaterialMenuDrawable.IconState.ARROW
        toolbar.navigationIcon = materialMenu
    }

    private var pagerAdapter: FragmentPagerAdapter? = null
    private var noteEditFragment: NoteEditFragment? = null
    private var noteViewFragment: NoteViewFragment? = null
    private var noteNextFragment: NoteNextFragment? = null

    private fun configPager() {
        val isEdit = (intent.getStringExtra(Constants.EXTRA_START_TYPE) == Constants.VALUE_START_EDIT)

        val noteEditFragmentIndex = 0
        val noteEditFragmentTag = makeFragmentTag(pager.id, noteEditFragmentIndex)
        val noteViewFragmentIndex = 1
        val noteViewFragmentTag = makeFragmentTag(pager.id, noteViewFragmentIndex)
        val noteNextFragmentIndex = 2
        val noteNextFragmentTag = makeFragmentTag(pager.id, noteNextFragmentIndex)

        getOrCreateNoteEditFragment(noteEditFragmentTag)
        getOrCreateNoteViewFragment(noteViewFragmentTag)
        getOrCreateNoteNextFragment(noteNextFragmentTag)

        val pageMap = listOf(
                noteEditFragment!!,
                noteViewFragment!!,
                noteNextFragment!!
        )
        pagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int): Fragment {
                return pageMap[position]
            }

            override fun getCount(): Int {
                return pageMap.size
            }
        }
        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    noteViewFragmentIndex -> {
                        if (noteEditFragment?.isAdded == true) {
                            hideSoftKeyboard(pager)
                            noteViewFragment?.refreshData()
                        }
                    }
                }
            }
        })
        pager.currentItem = if (isEdit) noteEditFragmentIndex else noteViewFragmentIndex
    }

    //region fragments

    private fun getOrCreateNoteEditFragment(tag: String) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null || fragment !is NoteEditFragment) {
            fragment = NoteEditFragment()
        } else {
            fragment.arguments?.putBoolean(NoteEditFragment.KEY_ARGS_RESTORE, true)
        }
        fragment.delegate = object : NoteEditFragment.NoteEditFragmentDelegate {

            override fun getNote(): Note {
                return note!!
            }

            override fun getAction(): String? {
                return if (intent.action.isNullOrBlank()) null else intent.action
            }

            override fun isContentChanged(): Boolean {
                return this@NoteActivity.contentChanged
            }

            override fun setContentChanged(contentChanged: Boolean) {
                this@NoteActivity.contentChanged = contentChanged
                if (!contentChanged) {
                    savedOrUpdated = true
                }
            }
        }
        noteEditFragment = fragment
    }

    private fun getOrCreateNoteViewFragment(tag: String) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null || fragment !is NoteViewFragment) {
            fragment = NoteViewFragment()
        }
        fragment.delegate = object : NoteViewFragment.NoteViewFragmentDelegate {

            override fun getNote(): Note {
                return note!!
            }
        }
        noteViewFragment = fragment

    }

    private fun getOrCreateNoteNextFragment(tag: String) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null || fragment !is NoteNextFragment) {
            fragment = NoteNextFragment()
        }
        fragment.delegate = object : NoteNextFragment.NoteNextFragmentDelegate {

            override fun getNote(): Note {
                return note!!
            }
        }
        noteNextFragment = fragment
    }

    //endregion

    private fun getCurrentFragment(): Fragment? {
        return pagerAdapter?.getItem(pager.currentItem)
    }

    //region menu

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (contentChanged) noteEditFragment?.saveOrUpdateData(null)
                else setResult()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        val fragment = getCurrentFragment(R.id.fragment_container)
        if (fragment is BaseModelFragment<*, *>) {
            fragment.onColorSelection(selectedColor)
        }
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}

    companion object {

        private const val BUNDLE_KEY_NOTE = "key_bundle_note"

        // region edit and view note

        fun editNote(fragment: Fragment, note: Note, requestCode: Int) {
            fragment.startActivityForResult(noteEditIntent(fragment.context, note, requestCode), requestCode)
        }

        fun editNote(activity: Activity, note: Note, requestCode: Int) {
            activity.startActivityForResult(noteEditIntent(activity, note, requestCode), requestCode)
        }

        fun viewNote(fragment: Fragment, note: Note, isPreview: Boolean, requestCode: Int) {
            val intent = noteViewIntent(fragment.context, note, requestCode)
            intent.putExtra(Constants.EXTRA_IS_PREVIEW, isPreview)
            fragment.startActivityForResult(intent, requestCode)
        }

        fun viewNote(activity: Activity, note: Note, requestCode: Int) {
            activity.startActivityForResult(noteViewIntent(activity, note, requestCode), requestCode)
        }

        private fun noteViewIntent(context: Context?, note: Note, requestCode: Int): Intent {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_VIEW)
            return intent
        }

        private fun noteEditIntent(context: Context?, note: Note, requestCode: Int): Intent {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_EDIT)
            return intent
        }

        // endregion

        fun resolveAction(activity: Activity, note: Note, action: String, requestCode: Int) {
            val intent = Intent(activity, NoteActivity::class.java)
            intent.action = action
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_EDIT)
            activity.startActivity(intent)
        }
    }
}
