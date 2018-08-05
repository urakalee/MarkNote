package me.shouheng.notepal.fragment.setting;

import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.listener.OnSettingsChangedListener;
import me.shouheng.notepal.listener.SettingChangeType;

/**
 * Created by shouh on 2018/4/4.
 */
public abstract class BaseFragment extends PreferenceFragmentCompat {

    public Preference findPreference(@StringRes int keyRes) {
        return super.findPreference(PalmApp.getStringCompact(keyRes));
    }

    protected void notifyDashboardChanged(SettingChangeType changeType) {
        if (getActivity() != null && getActivity() instanceof OnSettingsChangedListener) {
            ((OnSettingsChangedListener) getActivity()).onSettingChanged(changeType);
        }
    }
}
