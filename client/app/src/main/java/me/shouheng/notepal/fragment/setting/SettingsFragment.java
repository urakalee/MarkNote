package me.shouheng.notepal.fragment.setting;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import me.shouheng.notepal.R;
import me.shouheng.notepal.config.Constants;
import me.shouheng.notepal.dialog.FeedbackDialog;
import me.shouheng.notepal.dialog.NoticeDialog;
import me.shouheng.notepal.model.Feedback;
import me.shouheng.notepal.util.IntentUtils;

/**
 * Created by wang shouheng on 2017/12/21.
 */
public class SettingsFragment extends BaseFragment {

    /**
     * Used to transfer click message to the activity.
     */
    private Preference.OnPreferenceClickListener listener = preference -> {
        if (getActivity() != null && getActivity() instanceof OnPreferenceClickListener) {
            ((OnPreferenceClickListener) getActivity()).onPreferenceClick(preference.getKey());
        }
        return true;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setPreferenceClickListeners();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    private void setPreferenceClickListeners() {
        findPreference(R.string.key_feedback).setOnPreferenceClickListener(preference -> {
            showFeedbackEditor();
            return true;
        });
        findPreference(R.string.key_user_guide).setOnPreferenceClickListener(preference -> {
            IntentUtils.openWiki(getActivity());
            return true;
        });
        findPreference(R.string.key_support_develop).setOnPreferenceClickListener(preference -> {
            NoticeDialog.newInstance().show((getActivity()).getSupportFragmentManager(), "Notice");
            return true;
        });

        findPreference(R.string.key_preferences).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_setup_dashboard).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_data_backup).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_data_security).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_about).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_key_note_settings).setOnPreferenceClickListener(listener);
    }

    private void showFeedbackEditor() {
        FeedbackDialog.newInstance(getActivity(), (dialog, feedback) -> sendFeedback(feedback))
                .show((getActivity()).getSupportFragmentManager(), "Feedback Editor");
    }

    private void sendFeedback(Feedback feedback) {
        String subject = String.format(Constants.DEVELOPER_EMAIL_PREFIX, feedback.getFeedbackType().name());
        String body = feedback.getQuestion() + Constants.DEVELOPER_EMAIL_EMAIL_PREFIX + feedback.getEmail();
        IntentUtils.sendEmail(getActivity(), subject, body);
    }

    public interface OnPreferenceClickListener {
        void onPreferenceClick(String key);
    }
}
