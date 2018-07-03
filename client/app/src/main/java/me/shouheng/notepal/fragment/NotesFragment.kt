package me.shouheng.notepal.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import me.shouheng.notepal.R
import me.shouheng.notepal.activity.ContentActivity
import me.shouheng.notepal.adapter.NotesAdapter
import me.shouheng.notepal.adapter.NotesAdapter.MultiItem
import me.shouheng.notepal.databinding.FragmentNotesBinding
import me.shouheng.notepal.dialog.NotebookEditDialog
import me.shouheng.notepal.dialog.picker.NotebookPickerDialog
import me.shouheng.notepal.fragment.base.BaseFragment
import me.shouheng.notepal.listener.OnMainActivityInteraction
import me.shouheng.notepal.model.Category
import me.shouheng.notepal.model.Note
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.model.enums.Status
import me.shouheng.notepal.util.AppWidgetUtils
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ToastUtils
import me.shouheng.notepal.util.preferences.NotePreferences
import me.shouheng.notepal.util.preferences.UserPreferences
import me.shouheng.notepal.viewmodel.NoteViewModel
import me.shouheng.notepal.viewmodel.NotebookViewModel
import me.shouheng.notepal.widget.tools.CustomItemAnimator
import me.shouheng.notepal.widget.tools.DividerItemDecoration


class NotesFragment : BaseFragment<FragmentNotesBinding>(), OnMainActivityInteraction {

    private var status: Status? = null
    var notebook: Notebook? = null
        private set
    var category: Category? = null
        private set
    var isTopStack = true
        private set

    private var scrollListener: RecyclerView.OnScrollListener? = null

    private var dialog: NotebookEditDialog? = null

    private var adapter: NotesAdapter? = null
    private var noteViewModel: NoteViewModel? = null
    private var notebookViewModel: NotebookViewModel? = null

    private var userPreferences: UserPreferences? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_notes
    }

    override fun doCreateView(savedInstanceState: Bundle?) {
        userPreferences = UserPreferences.getInstance()

        handleArguments()

        configToolbar()

        configNotesList()
    }

    private fun handleArguments() {
        val args = arguments ?: return
        if (args.containsKey(ARG_NOTEBOOK)) {
            isTopStack = false
            notebook = args.get(ARG_NOTEBOOK) as Notebook
        }
        if (args.containsKey(ARG_CATEGORY)) {
            isTopStack = false
            category = args.get(ARG_CATEGORY) as Category
        }
        if (args.containsKey(ARG_STATUS)) {
            status = arguments!!.get(ARG_STATUS) as Status
        } else {
            throw IllegalArgumentException("status required")
        }
    }

    private fun configToolbar() {
        if (activity != null) {
            val actionBar = (activity as AppCompatActivity).supportActionBar
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_notes)
                actionBar.setDisplayHomeAsUpEnabled(true)
                val subTitle = if (notebook != null) notebook!!.title else if (category != null) category!!.name else null
                actionBar.subtitle = subTitle
                actionBar.setHomeAsUpIndicator(if (isTopStack) R.drawable.ic_menu_white else R.drawable.ic_arrow_back_white_24dp)
            }
        }
    }

    // region Config Notes List
    private fun configNotesList() {
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        notebookViewModel = ViewModelProviders.of(this).get(NotebookViewModel::class.java)

        binding.ivEmpty.setSubTitle(noteViewModel!!.getEmptySubTitle(status))

        adapter = NotesAdapter(context, emptyList<MultiItem>())
        adapter!!.setOnItemClickListener { adapter, view, position ->
            val item = adapter.data[position] as MultiItem
            if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                ContentActivity.viewNote(this@NotesFragment, item.note, false, REQUEST_NOTE_VIEW)
            } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                if (activity != null && activity is OnNotesInteractListener) {
                    (activity as OnNotesInteractListener).onNotebookSelected(item.notebook)
                }
            }
        }
        adapter!!.setOnItemLongClickListener { adapter, view, position ->
            val item = adapter.data[position] as MultiItem
            if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                popNoteMenu(view, item)
            } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                popNotebookMenu(view, item, position)
            }
            true
        }
        adapter!!.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as MultiItem
            when (view.id) {
                R.id.iv_more -> if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                    popNoteMenu(view, item)
                } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                    popNotebookMenu(view, item, position)
                }
            }
        }

        binding.rvNotes.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL_LIST, isDarkTheme))
        binding.rvNotes.itemAnimator = CustomItemAnimator()
        binding.rvNotes.layoutManager = LinearLayoutManager(context)
        if (scrollListener != null) binding.rvNotes.addOnScrollListener(scrollListener)
        binding.rvNotes.setEmptyView(binding.ivEmpty)
        binding.rvNotes.adapter = adapter

        binding.fastscroller.setRecyclerView(binding.rvNotes)
        binding.fastscroller.visibility = if (userPreferences!!.fastScrollerEnabled()) View.VISIBLE else View.GONE

        reload()
    }
    // endregion

    // region Note & Notebook Pop Menus
    private fun popNoteMenu(v: View, multiItem: MultiItem) {
        val popupM = PopupMenu(context!!, v)
        popupM.inflate(R.menu.pop_menu)
        configPopMenu(popupM)
        popupM.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_trash -> update(multiItem.note, Status.TRASHED)
                R.id.action_archive -> update(multiItem.note, Status.ARCHIVED)
                R.id.action_move -> moveNote(multiItem.note)
                R.id.action_edit -> ContentActivity.editNote(this, multiItem.note, REQUEST_NOTE_EDIT)
                R.id.action_move_out -> update(multiItem.note, Status.NORMAL)
                R.id.action_delete -> update(multiItem.note, Status.DELETED)
            }
            true
        }
        popupM.show()
    }

    private fun popNotebookMenu(v: View, multiItem: MultiItem, position: Int) {
        val popupM = PopupMenu(context!!, v)
        popupM.inflate(R.menu.pop_menu)
        configPopMenu(popupM)
        popupM.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_trash -> update(multiItem.notebook, status, Status.TRASHED)
                R.id.action_archive -> update(multiItem.notebook, status, Status.ARCHIVED)
                R.id.action_move -> moveNotebook(multiItem.notebook)
                R.id.action_edit -> editNotebook(position, multiItem.notebook)
                R.id.action_move_out -> update(multiItem.notebook, status, Status.NORMAL)
                R.id.action_delete -> showDeleteMsgDialog(multiItem.notebook, position)
            }
            true
        }
        popupM.show()
    }

    private fun editNotebook(position: Int, notebook: Notebook) {
        dialog = NotebookEditDialog.newInstance(context, notebook
        ) { categoryName, notebookColor ->
            notebook.title = categoryName
            notebook.color = notebookColor
            update(notebook, position)
        }
        dialog!!.show(fragmentManager!!, "Notebook Editor")
    }

    private fun moveNote(note: Note) {
        NotebookPickerDialog.newInstance().setOnItemSelectedListener { dialog, toBook, position ->
            if (toBook.code == note.parentCode) return@setOnItemSelectedListener
            note.parentCode = toBook.code
            note.treePath = toBook.treePath + "|" + note.code
            update(note)
            dialog.dismiss()
        }.show(fragmentManager!!, "Notebook picker")
    }

    private fun moveNotebook(nb: Notebook) {
        NotebookPickerDialog.newInstance().setOnItemSelectedListener { dialog, toBook, position ->
            if (toBook.code == nb.parentCode) return@setOnItemSelectedListener
            move(nb, toBook)
            dialog.dismiss()
        }.show(fragmentManager!!, "Notebook picker")
    }

    fun setSelectedColor(color: Int) {
        if (dialog != null) dialog!!.updateUIBySelectedColor(color)
    }

    private fun configPopMenu(popupMenu: PopupMenu) {
        popupMenu.menu.findItem(R.id.action_move_out).isVisible = status == Status.ARCHIVED || status == Status.TRASHED
        popupMenu.menu.findItem(R.id.action_edit).isVisible = status == Status.ARCHIVED || status == Status.NORMAL
        popupMenu.menu.findItem(R.id.action_move).isVisible = status == Status.NORMAL
        popupMenu.menu.findItem(R.id.action_trash).isVisible = status == Status.NORMAL || status == Status.ARCHIVED
        popupMenu.menu.findItem(R.id.action_archive).isVisible = status == Status.NORMAL
        popupMenu.menu.findItem(R.id.action_delete).isVisible = status == Status.TRASHED
    }

    private fun showDeleteMsgDialog(nb: Notebook, position: Int) {
        MaterialDialog.Builder(context!!)
                .title(R.string.text_warning)
                .content(R.string.msg_when_delete_notebook)
                .positiveText(R.string.text_delete_still)
                .negativeText(R.string.text_give_up)
                .onPositive { materialDialog, dialogAction -> update(nb, status, Status.DELETED) }
                .show()
    }
    // endregion

    fun setScrollListener(scrollListener: RecyclerView.OnScrollListener) {
        this.scrollListener = scrollListener
    }

    // region ViewModel interaction
    fun reload() {
        if (activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onNoteLoadStateChanged(
                    me.shouheng.notepal.model.data.Status.LOADING)
        }

        noteViewModel
                ?.getMultiItems(category, status, notebook)
                ?.observe(this, Observer { multiItemResource ->
                    if (multiItemResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_load_data)
                        return@Observer
                    }
                    if (activity is OnNotesInteractListener) {
                        (activity as OnNotesInteractListener).onNoteLoadStateChanged(multiItemResource!!.status)
                    }
                    when (multiItemResource!!.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> adapter!!.setNewData(multiItemResource!!.data)
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_load_data)
                    }
                })
    }

    private fun update(note: Note) {
        noteViewModel
                ?.update(note)
                ?.observe(this, Observer { noteResource ->
                    if (noteResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (noteResource!!.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> {
                            ToastUtils.makeToast(R.string.moved_successfully)
                            reload()
                            notifyDataChanged()
                        }
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(note: Note, toStatus: Status) {
        noteViewModel
                ?.update(note, toStatus)
                ?.observe(this, Observer { noteResource ->
                    if (noteResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (noteResource.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> {
                            reload()
                            notifyDataChanged()
                        }
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(notebook: Notebook, position: Int) {
        notebookViewModel
                ?.update(notebook)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> {
                            adapter!!.notifyItemChanged(position)
                            ToastUtils.makeToast(R.string.moved_successfully)
                        }
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun move(notebook: Notebook, toNotebook: Notebook) {
        notebookViewModel
                ?.move(notebook, toNotebook)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> {
                            ToastUtils.makeToast(R.string.moved_successfully)
                            reload()
                            notifyDataChanged()
                        }
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(notebook: Notebook, fromStatus: Status?, toStatus: Status) {
        notebookViewModel
                ?.update(notebook, fromStatus, toStatus)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        me.shouheng.notepal.model.data.Status.SUCCESS -> {
                            reload()
                            notifyDataChanged()
                        }
                        me.shouheng.notepal.model.data.Status.LOADING -> {
                        }
                        me.shouheng.notepal.model.data.Status.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }
    // endregion

    private fun notifyDataChanged() {

        /*
         * Notify app widget that the list is changed. */
        AppWidgetUtils.notifyAppWidgets(context)

        /*
         * Notify the attached activity that the list is changed. */
        if (activity != null && activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onNoteDataChanged()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        if (!isTopStack && activity != null && activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onActivityAttached(isTopStack)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.capture, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (NotePreferences.getInstance().isNoteExpanded) {
            // disable list capture when the note list is expanded
            menu!!.findItem(R.id.action_capture).isVisible = false
        } else {
            menu!!.findItem(R.id.action_capture).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                LogUtils.d("onOptionsItemSelected")
                if (isTopStack) {
                    return super.onOptionsItemSelected(item)
                } else {
                    if (activity != null) {
                        activity!!.onBackPressed()
                    }
                    return true
                }
            }
            R.id.action_capture -> createScreenCapture(binding.rvNotes)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (activity != null && activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onActivityAttached(isTopStack)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NOTE_VIEW, REQUEST_NOTE_EDIT -> {
                    reload()
                    notifyDataChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDataSetChanged() {
        reload()
    }

    override fun onFastScrollerChanged() {
        binding.fastscroller.visibility = if (userPreferences!!.fastScrollerEnabled()) View.VISIBLE else View.GONE
    }

    interface OnNotesInteractListener {

        /**
         * On the notebook is selected, this method will be called.
         *
         * @param notebook notebook selected
         */
        @JvmDefault
        fun onNotebookSelected(notebook: Notebook) {}

        /**
         * When the fragment is attached to the activity, will call this method to lock the drawer
         *
         * @param isTopStack whether current fragment is the top stack of all fragments
         */
        @JvmDefault
        fun onActivityAttached(isTopStack: Boolean) {}

        @JvmDefault
        fun onNoteDataChanged() {}

        @JvmDefault
        fun onNoteLoadStateChanged(status: me.shouheng.notepal.model.data.Status) {}
    }

    companion object {

        private val ARG_NOTEBOOK = "arg_notebook"
        private val ARG_CATEGORY = "arg_category"
        private val ARG_STATUS = "arg_status"

        private val REQUEST_NOTE_VIEW = 0x0010
        private val REQUEST_NOTE_EDIT = 0x0011

        fun newInstance(status: Status): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(notebook: Notebook, status: Status): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_NOTEBOOK, notebook)
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(category: Category, status: Status): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_CATEGORY, category)
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
