package me.urakalee.next2.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog.ColorCallback
import kotlinx.android.synthetic.main.activity_note.*
import me.shouheng.notepal.R
import me.shouheng.notepal.config.Constants
import me.shouheng.notepal.fragment.base.BaseModelFragment
import me.shouheng.notepal.util.FragmentHelper
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ToastUtils
import me.urakalee.next2.base.activity.CommonActivity
import me.urakalee.next2.base.fragment.CommonFragment
import me.urakalee.next2.fragment.NoteEditFragment
import me.urakalee.next2.fragment.NoteViewFragment
import me.urakalee.next2.model.Note
import me.urakalee.ranger.extension.getFromBundle
import me.urakalee.ranger.extension.hasExtraInBundle
import me.urakalee.ranger.extension.makeFragmentTag
import me.urakalee.ranger.extension.putToBundle

/**
 * @author Uraka.Lee
 */
class NoteActivity : CommonActivity(),
        ColorCallback {

    private var note: Note? = null

    override val layoutResId: Int
        get() = R.layout.activity_note

    //region lifecycle

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(BUNDLE_KEY_NOTE, note)
    }

    override fun onBackPressed() {
        // TODO: getCurrentFragment in pager
        val currentFragment = getCurrentFragment()
        if (currentFragment is CommonFragment) {
            currentFragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
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
        val fragment = intent.getStringExtra(Constants.EXTRA_FRAGMENT)
        if (fragment == null) {
            ToastUtils.makeToast(R.string.content_failed_to_parse_intent)
            LogUtils.d("Failed to handle intent : $intent")
            return false
        }
        return when (fragment) {
            Constants.VALUE_FRAGMENT_NOTE -> {
                handleNoteIntent()
            }
            else -> {
                ToastUtils.makeToast(R.string.content_failed_to_parse_intent)
                false
            }
        }
    }

    private fun handleNoteIntent(): Boolean {
        if (intent.hasExtraInBundle(Constants.EXTRA_MODEL)) {
            note = note ?: intent.getFromBundle(Constants.EXTRA_MODEL)
            note?.let {
                return true
            }
        }
        ToastUtils.makeToast(R.string.content_failed_to_parse_intent)
        LogUtils.d("Failed to resolve note intent : $intent")
        return false
    }

    private fun toNoteFragment(note: Note, requestCode: Int?, isEdit: Boolean) {
        val action = if (intent.action.isNullOrBlank()) null else intent.action
        var fragment: Fragment?
        if (isEdit) {
            fragment = supportFragmentManager.findFragmentByTag(TAG_NOTE_FRAGMENT)
            if (fragment == null) {
                fragment = NoteEditFragment.newInstance(note, requestCode, action)
            } else {
                fragment.arguments?.putBoolean(NoteEditFragment.KEY_ARGS_RESTORE, true)
            }
            FragmentHelper.replace(this, fragment, R.id.fragment_container, TAG_NOTE_FRAGMENT)
        } else {
            val isPreview = intent.getBooleanExtra(Constants.EXTRA_IS_PREVIEW, false)
            fragment = NoteViewFragment.newInstance(note, isPreview, requestCode)
            FragmentHelper.replace(this, fragment, R.id.fragment_container)
        }
    }

    private fun configToolbar() {
        toolbar.bringToFront()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!isDarkTheme) toolbar.popupTheme = R.style.AppTheme_PopupOverlay
        setStatusBarColor(resources.getColor(if (isDarkTheme) R.color.dark_theme_foreground else R.color.md_grey_500))
    }

    private var pagerAdapter: FragmentPagerAdapter? = null

    private fun configPager() {
        val requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, -1)
        val action = if (intent.action.isNullOrBlank()) null else intent.action
        val isEdit = (intent.getStringExtra(Constants.EXTRA_START_TYPE) == Constants.VALUE_START_EDIT)

        val noteEditFragmentIndex = 0
        val noteEditFragmentTag = makeFragmentTag(pager.id, noteEditFragmentIndex)
        val noteViewFragmentIndex = 1
        val noteViewFragmentTag = makeFragmentTag(pager.id, noteViewFragmentIndex)

        var noteEditFragment = supportFragmentManager.findFragmentByTag(noteEditFragmentTag)
        if (noteEditFragment == null) {
            noteEditFragment = NoteEditFragment.newInstance(note!!, if (requestCode == -1) null else requestCode, action)
        } else {
            noteEditFragment.arguments?.putBoolean(NoteEditFragment.KEY_ARGS_RESTORE, true)
        }

        var noteViewFragment = supportFragmentManager.findFragmentByTag(noteViewFragmentTag)
        if (noteViewFragment == null) {
            noteViewFragment = Fragment()
        }

        val pageMap = listOf(
                noteEditFragment,
                noteViewFragment
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
    }

    private fun getCurrentFragment(): Fragment? {
        return pagerAdapter?.getItem(pager.currentItem)
    }

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        val fragment = getCurrentFragment(R.id.fragment_container)
        if (fragment is BaseModelFragment<*, *>) {
            fragment.onColorSelection(selectedColor)
        }
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}

    companion object {

        private const val BUNDLE_KEY_NOTE = "key_bundle_note"
        private const val TAG_NOTE_FRAGMENT = "note_fragment_tag"

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
            val intent = Intent(context, ContentActivity::class.java)
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_VIEW)
            intent.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_NOTE)
            return intent
        }

        private fun noteEditIntent(context: Context?, note: Note, requestCode: Int): Intent {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_EDIT)
            intent.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_NOTE)
            return intent
        }

        // endregion

        fun resolveAction(activity: Activity, note: Note, action: String, requestCode: Int) {
            val intent = Intent(activity, NoteActivity::class.java)
            intent.action = action
            intent.putToBundle(Constants.EXTRA_MODEL, note)
            intent.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_NOTE)
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode)
            intent.putExtra(Constants.EXTRA_START_TYPE, Constants.VALUE_START_EDIT)
            activity.startActivity(intent)
        }
    }
}
