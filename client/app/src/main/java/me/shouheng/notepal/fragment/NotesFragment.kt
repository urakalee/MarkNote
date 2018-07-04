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
import me.shouheng.notepal.model.data.LoadStatus
import me.shouheng.notepal.model.enums.ItemStatus
import me.shouheng.notepal.util.AppWidgetUtils
import me.shouheng.notepal.util.LogUtils
import me.shouheng.notepal.util.ToastUtils
import me.shouheng.notepal.util.preferences.UserPreferences
import me.shouheng.notepal.viewmodel.NoteViewModel
import me.shouheng.notepal.viewmodel.NotebookViewModel
import me.shouheng.notepal.widget.tools.CustomItemAnimator
import me.shouheng.notepal.widget.tools.DividerItemDecoration
import me.urakalee.ranger.extension.isVisible


class NotesFragment : BaseFragment<FragmentNotesBinding>(),
        OnMainActivityInteraction {

    companion object {

        private val ARG_NOTEBOOK = "arg_notebook"
        private val ARG_CATEGORY = "arg_category"
        private val ARG_STATUS = "arg_status"

        private val REQUEST_NOTE_VIEW = 0x0010
        private val REQUEST_NOTE_EDIT = 0x0011

        fun newInstance(status: ItemStatus): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(notebook: Notebook, status: ItemStatus): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_NOTEBOOK, notebook)
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(category: Category, status: ItemStatus): NotesFragment {
            val args = Bundle()
            args.putSerializable(ARG_CATEGORY, category)
            args.putSerializable(ARG_STATUS, status)
            val fragment = NotesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var status: ItemStatus? = null
    var notebook: Notebook? = null
        private set
    var category: Category? = null
        private set
    var isTopStack = true // 笔记本列表时为 false
        private set

    private var userPreferences: UserPreferences? = null

    private var noteViewModel: NoteViewModel? = null
    private var notebookViewModel: NotebookViewModel? = null

    private var adapter: NotesAdapter? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_notes
    }

    //region lifecycle

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        if (!isTopStack && activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onActivityAttached(isTopStack)
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onActivityAttached(isTopStack)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NOTE_VIEW, REQUEST_NOTE_EDIT -> {
                    reload()
                    onDataChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //endregion
    //region init

    override fun doCreateView(savedInstanceState: Bundle?) {
        userPreferences = UserPreferences.getInstance()

        handleArguments()

        configToolbar()

        configList()
    }

    private fun handleArguments() {
        val argumentsNonNull = arguments ?: return
        if (argumentsNonNull.containsKey(ARG_NOTEBOOK)) {
            isTopStack = false
            notebook = argumentsNonNull.get(ARG_NOTEBOOK) as Notebook
        }
        if (argumentsNonNull.containsKey(ARG_CATEGORY)) {
            isTopStack = false
            category = argumentsNonNull.get(ARG_CATEGORY) as Category
        }
        if (argumentsNonNull.containsKey(ARG_STATUS)) {
            status = argumentsNonNull.get(ARG_STATUS) as ItemStatus
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

    private fun configList() {
        val contextNotNull = context ?: return

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        notebookViewModel = ViewModelProviders.of(this).get(NotebookViewModel::class.java)

        binding.emptyView.setSubTitle(noteViewModel?.getEmptySubTitle(status))

        adapter = NotesAdapter(contextNotNull, emptyList<MultiItem>())
        adapter?.setOnItemClickListener { adapter, _, position ->
            val item = adapter.data[position] as MultiItem
            if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                ContentActivity.viewNote(this@NotesFragment, item.note, false, REQUEST_NOTE_VIEW)
            } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                if (activity != null && activity is OnNotesInteractListener) {
                    (activity as OnNotesInteractListener).onNotebookSelected(item.notebook)
                }
            }
        }
        adapter?.setOnItemLongClickListener { adapter, view, position ->
            val item = adapter.data[position] as MultiItem
            if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                popNoteMenu(view, item)
            } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                popNotebookMenu(view, item, position)
            }
            true
        }
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as MultiItem
            when (view.id) {
                R.id.iv_more ->
                    if (item.itemType == MultiItem.ITEM_TYPE_NOTE) {
                        popNoteMenu(view, item)
                    } else if (item.itemType == MultiItem.ITEM_TYPE_NOTEBOOK) {
                        popNotebookMenu(view, item, position)
                    }
            }
        }

        binding.listView.addItemDecoration(DividerItemDecoration(contextNotNull, DividerItemDecoration.VERTICAL_LIST, isDarkTheme))
        binding.listView.itemAnimator = CustomItemAnimator()
        binding.listView.layoutManager = LinearLayoutManager(contextNotNull)
        scrollListener?.let {
            binding.listView.addOnScrollListener(it)
        }
        binding.listView.setEmptyView(binding.emptyView)
        binding.listView.adapter = adapter

        binding.fastScroller.setRecyclerView(binding.listView)
        binding.fastScroller.isVisible = userPreferences?.fastScrollerEnabled() ?: false

        reload()
    }

    var scrollListener: RecyclerView.OnScrollListener? = null

    //endregion
    // region Note & Notebook Menus and Dialogs

    private fun popNoteMenu(view: View, multiItem: MultiItem) {
        val contextNonNull = context ?: return
        val popupMenu = PopupMenu(contextNonNull, view)
        popupMenu.inflate(R.menu.pop_menu)
        configPopMenu(popupMenu)
        showMoveItem(popupMenu, true)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_trash -> update(multiItem.note, ItemStatus.TRASHED)
                R.id.action_archive -> update(multiItem.note, ItemStatus.ARCHIVED)
                R.id.action_move -> moveNote(multiItem.note)
                R.id.action_edit -> ContentActivity.editNote(this, multiItem.note, REQUEST_NOTE_EDIT)
                R.id.action_move_out -> update(multiItem.note, ItemStatus.NORMAL)
                R.id.action_delete -> update(multiItem.note, ItemStatus.DELETED)
            }
            true
        }
        popupMenu.show()
    }

    private fun moveNote(note: Note) {
        val fragmentNonNull = fragmentManager ?: return
        NotebookPickerDialog.newInstance().setOnItemSelectedListener { dialog, toBook, _ ->
            if (toBook.code == note.parentCode) return@setOnItemSelectedListener
            note.parentCode = toBook.code
            note.treePath = toBook.treePath + "|" + note.code
            update(note)
            dialog.dismiss()
        }.show(fragmentNonNull, "Notebook picker")
    }

    private fun popNotebookMenu(view: View, multiItem: MultiItem, position: Int) {
        val contextNonNull = context ?: return
        val popupMenu = PopupMenu(contextNonNull, view)
        popupMenu.inflate(R.menu.pop_menu)
        configPopMenu(popupMenu)
        showMoveItem(popupMenu, false)
        popupMenu.setOnMenuItemClickListener { item ->
            val statusNonNull = status ?: return@setOnMenuItemClickListener true
            when (item.itemId) {
                R.id.action_trash -> update(multiItem.notebook, statusNonNull, ItemStatus.TRASHED)
                R.id.action_archive -> update(multiItem.notebook, statusNonNull, ItemStatus.ARCHIVED)
                R.id.action_edit -> editNotebook(position, multiItem.notebook)
                R.id.action_move_out -> update(multiItem.notebook, statusNonNull, ItemStatus.NORMAL)
                R.id.action_delete -> deleteNotebook(multiItem.notebook)
            }
            true
        }
        popupMenu.show()
    }

    private fun editNotebook(position: Int, notebook: Notebook) {
        val contextNonNull = context ?: return
        val fragmentNonNull = fragmentManager ?: return
        dialog = NotebookEditDialog.newInstance(contextNonNull, notebook) { categoryName, notebookColor ->
            notebook.title = categoryName
            notebook.color = notebookColor
            update(position, notebook)
        }
        dialog?.show(fragmentNonNull, "Notebook Editor")
    }

    fun setSelectedColor(color: Int) {
        dialog?.updateUIBySelectedColor(color)
    }

    private var dialog: NotebookEditDialog? = null

    private fun deleteNotebook(notebook: Notebook) {
        val contextNonNull = context ?: return
        MaterialDialog.Builder(contextNonNull)
                .title(R.string.text_warning)
                .content(R.string.msg_when_delete_notebook)
                .positiveText(R.string.text_delete_still)
                .negativeText(R.string.text_give_up)
                .onPositive { _, _ ->
                    val statusNonNull = status ?: return@onPositive
                    update(notebook, statusNonNull, ItemStatus.DELETED)
                }
                .show()
    }

    private fun configPopMenu(popupMenu: PopupMenu) {
        popupMenu.menu.findItem(R.id.action_move_out).isVisible = status == ItemStatus.ARCHIVED || status == ItemStatus.TRASHED
        popupMenu.menu.findItem(R.id.action_edit).isVisible = status == ItemStatus.ARCHIVED || status == ItemStatus.NORMAL
        popupMenu.menu.findItem(R.id.action_move).isVisible = status == ItemStatus.NORMAL
        popupMenu.menu.findItem(R.id.action_trash).isVisible = status == ItemStatus.NORMAL || status == ItemStatus.ARCHIVED
        popupMenu.menu.findItem(R.id.action_archive).isVisible = status == ItemStatus.NORMAL
        popupMenu.menu.findItem(R.id.action_delete).isVisible = status == ItemStatus.TRASHED
    }

    private fun showMoveItem(popupMenu: PopupMenu, show: Boolean) {
        popupMenu.menu.findItem(R.id.action_move).isVisible = show
    }

    // endregion
    // region ViewModel interaction

    fun reload() {
        if (activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).onNoteLoadStateChanged(LoadStatus.LOADING)
        }

        noteViewModel
                ?.getMultiItems(category, status, notebook)
                ?.observe(this, Observer { multiItemResource ->
                    if (activity == null) {
                        return@Observer
                    }
                    if (multiItemResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_load_data)
                        return@Observer
                    }
                    if (activity is OnNotesInteractListener) {
                        (activity as OnNotesInteractListener).onNoteLoadStateChanged(multiItemResource.status)
                    }
                    when (multiItemResource.status) {
                        LoadStatus.SUCCESS -> adapter?.setNewData(multiItemResource.data)
                        LoadStatus.LOADING -> {
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_load_data)
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
                    when (noteResource.status) {
                        LoadStatus.SUCCESS -> {
                            ToastUtils.makeToast(R.string.moved_successfully)
                            reload()
                            onDataChanged()
                        }
                        LoadStatus.LOADING -> {
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(note: Note, toStatus: ItemStatus) {
        noteViewModel
                ?.update(note, toStatus)
                ?.observe(this, Observer { noteResource ->
                    if (noteResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (noteResource.status) {
                        LoadStatus.SUCCESS -> {
                            reload()
                            onDataChanged()
                        }
                        LoadStatus.LOADING -> {
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(position: Int, notebook: Notebook) {
        notebookViewModel
                ?.update(notebook)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        LoadStatus.SUCCESS -> {
                            adapter?.notifyItemChanged(position)
                            ToastUtils.makeToast(R.string.moved_successfully)
                        }
                        LoadStatus.LOADING -> {
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    private fun update(notebook: Notebook, fromStatus: ItemStatus, toStatus: ItemStatus) {
        notebookViewModel
                ?.update(notebook, fromStatus, toStatus)
                ?.observe(this, Observer { notebookResource ->
                    if (notebookResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                        return@Observer
                    }
                    when (notebookResource.status) {
                        LoadStatus.SUCCESS -> {
                            reload()
                            onDataChanged()
                        }
                        LoadStatus.LOADING -> {
                        }
                        LoadStatus.FAILED -> ToastUtils.makeToast(R.string.text_failed_to_modify_data)
                    }
                })
    }

    // endregion

    override fun onDataSetChanged() {
        reload()
    }

    override fun onFastScrollerChanged() {
        binding.fastScroller.isVisible = userPreferences?.fastScrollerEnabled() ?: false
    }

    private fun onDataChanged() {
        // Notify app widget that the list is changed.
        AppWidgetUtils.notifyAppWidgets()
        // Notify the attached activity that the list is changed.
        if (activity is OnNotesInteractListener) {
            (activity as OnNotesInteractListener).markNoteDataChanged()
        }
    }

    //region menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                LogUtils.d("onOptionsItemSelected")
                if (isTopStack) {
                    return super.onOptionsItemSelected(item)
                } else {
                    activity?.onBackPressed()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    interface OnNotesInteractListener {

        /**
         * On the notebook is selected, this method will be called.
         *
         * @param notebook notebook selected
         */
        @JvmDefault
        fun onNotebookSelected(notebook: Notebook) {
        }

        /**
         * When the fragment is attached to the activity, will call this method to lock the drawer
         *
         * @param isTopStack whether current fragment is the top stack of all fragments
         */
        @JvmDefault
        fun onActivityAttached(isTopStack: Boolean) {
        }

        @JvmDefault
        fun markNoteDataChanged() {
        }

        @JvmDefault
        fun onNoteLoadStateChanged(status: LoadStatus) {
        }
    }
}
