package me.shouheng.notepal.fragment.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.webkit.WebView;

import me.shouheng.notepal.R;
import me.shouheng.notepal.listener.OnAttachingFileListener;
import me.shouheng.notepal.model.Attachment;
import me.shouheng.notepal.util.AttachmentHelper;
import me.shouheng.notepal.util.FileHelper;
import me.shouheng.notepal.util.ScreenShotHelper;
import me.urakalee.next2.base.activity.BaseActivity;
import me.urakalee.next2.support.permission.PermissionUtils;

/**
 * Created by wang shouheng on 2017/12/29.
 */
public abstract class BaseFragment<V extends ViewDataBinding> extends CommonFragment<V>
        implements OnAttachingFileListener {

    // region Capture

    protected void createWebCapture(WebView webView, FileHelper.OnSavedToGalleryListener listener) {
        assert getActivity() != null;
        PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () -> {
            final ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle(R.string.capturing);
            pd.setCancelable(false);
            pd.show();

            new Handler().postDelayed(() -> doCapture(webView, pd, listener), 500);
        });
    }

    private void doCapture(WebView webView, ProgressDialog pd, FileHelper.OnSavedToGalleryListener listener) {
        ScreenShotHelper.shotWebView(webView, listener);
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    // endregion
    // region Attachment

    /**
     * This method will called when the attachment is sure usable. For the check logic, you may refer
     * to {@link BaseFragment#onAttachingFileFinished(Attachment)}
     *
     * @param attachment the usable attachment
     */
    protected void onGetAttachment(@NonNull Attachment attachment) {
    }

    protected void onFailedGetAttachment(Attachment attachment) {
    }

    @Override
    public void onAttachingFileErrorOccurred(Attachment attachment) {
        onFailedGetAttachment(attachment);
    }

    @Override
    public void onAttachingFileFinished(Attachment attachment) {
        if (AttachmentHelper.checkAttachment(attachment)) {
            onGetAttachment(attachment);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentHelper.resolveFragmentResult(this, requestCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // endregion
}
