package me.shouheng.notepal.fragment.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import me.shouheng.notepal.R;
import me.shouheng.notepal.activity.FabSortActivity;
import me.shouheng.notepal.activity.MenuSortActivity;
import me.shouheng.notepal.listener.OnFragmentDestroyListener;
import me.shouheng.notepal.listener.SettingChangeType;

public class SettingsPreferences extends BaseFragment {

    private final int REQUEST_CODE_FAB_SORT = 0x0001;

    public static SettingsPreferences newInstance() {
        Bundle args = new Bundle();
        SettingsPreferences fragment = new SettingsPreferences();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configToolbar();

        addPreferencesFromResource(R.xml.preferences_preferences);

        findPreference(R.string.key_custom_fab).setOnPreferenceClickListener(preference -> {
            FabSortActivity.start(SettingsPreferences.this, REQUEST_CODE_FAB_SORT);
            return true;
        });
        findPreference(R.string.key_note_editor_menu_sort).setOnPreferenceClickListener(preference -> {
            MenuSortActivity.start(SettingsPreferences.this, 1);
            return true;
        });
        findPreference(R.string.key_fast_scroller).setOnPreferenceClickListener(preference -> {
            notifyDashboardChanged(SettingChangeType.FAST_SCROLLER);
            return false;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_preferences);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FAB_SORT:
                    notifyDashboardChanged(SettingChangeType.FAB);
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof OnFragmentDestroyListener) {
            ((OnFragmentDestroyListener) getActivity()).onFragmentDestroy();
        }
    }
}
