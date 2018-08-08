package me.shouheng.notepal.activity;

import android.support.v4.app.Fragment;

import me.shouheng.notepal.R;
import me.shouheng.notepal.activity.base.BaseListActivity;
import me.shouheng.notepal.fragment.CategoriesFragment;
import me.urakalee.next2.notelist.NotesFragment;
import me.shouheng.notepal.model.Category;
import me.shouheng.notepal.model.Notebook;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.util.FragmentHelper;

public class ArchiveActivity extends BaseListActivity {

    @Override
    protected CharSequence getActionbarTitle() {
        return getString(R.string.drawer_menu_archive);
    }

    @Override
    protected Fragment getNotesFragment() {
        return NotesFragment.Companion.newInstance(ItemStatus.ARCHIVED);
    }

    @Override
    protected Fragment getCategoryFragment() {
        return CategoriesFragment.newInstance(ItemStatus.ARCHIVED);
    }

    @Override
    public void onCategorySelected(Category category) {
        NotesFragment notesFragment = NotesFragment.Companion.newInstance(category, ItemStatus.ARCHIVED);
        FragmentHelper.replaceWithCallback(this, notesFragment, R.id.fragment_container);
    }

    @Override
    public void onNotebookSelected(Notebook notebook) {
        NotesFragment notesFragment = NotesFragment.Companion.newInstance(notebook, ItemStatus.ARCHIVED);
        FragmentHelper.replaceWithCallback(this, notesFragment, R.id.fragment_container);
    }
}
