package me.shouheng.notepal.util.preferences;

import android.content.Context;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.R;

/**
 * Created by Wang Shouheng on 2017/12/5.
 */
public class PersistPreferences extends BasePreferences {

    private static PersistPreferences sInstance;

    public static PersistPreferences getInstance() {
        if (sInstance == null) {
            synchronized (PersistPreferences.class) {
                if (sInstance == null) {
                    sInstance = new PersistPreferences(PalmApp.getContext());
                }
            }
        }
        return sInstance;
    }

    private PersistPreferences(Context context) {
        super(context);
    }

    public void setAttachmentFilePath(String filePath) {
        putString(R.string.key_attachment_file_path, filePath);
    }

    public String getAttachmentFilePath() {
        return getString(R.string.key_attachment_file_path, "");
    }
}
