package me.urakalee.next2.base.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Handler
import android.webkit.WebView
import me.shouheng.notepal.R
import me.shouheng.notepal.listener.OnAttachingFileListener
import me.shouheng.notepal.model.Attachment
import me.shouheng.notepal.util.AttachmentHelper
import me.shouheng.notepal.util.FileHelper
import me.shouheng.notepal.util.ScreenShotHelper
import org.polaric.colorful.BaseActivity
import org.polaric.colorful.PermissionUtils

/**
 * @author Uraka.Lee
 */
abstract class BaseFragment : CommonFragment(), OnAttachingFileListener {

    // region Capture

    protected fun createWebCapture(webView: WebView, listener: FileHelper.OnSavedToGalleryListener) {
        val activityNonNull = activity as? BaseActivity ?: return
        PermissionUtils.checkStoragePermission(activityNonNull) {
            val progressDialog = ProgressDialog(activityNonNull).apply {
                setTitle(R.string.capturing)
                setCancelable(false)
                show()
            }
            Handler().postDelayed({ doCapture(webView, progressDialog, listener) }, 500)
        }
    }

    private fun doCapture(webView: WebView, progressDialog: ProgressDialog,
                          listener: FileHelper.OnSavedToGalleryListener) {
        ScreenShotHelper.shotWebView(webView, listener)
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    // endregion
    // region Attachment

    /**
     * This method will called when the attachment is sure usable. For the check logic, you may refer
     * to [BaseFragment.onAttachingFileFinished]
     *
     * @param attachment the usable attachment
     */
    protected open fun onGetAttachment(attachment: Attachment) {}

    protected open fun onFailedGetAttachment(attachment: Attachment) {}

    override fun onAttachingFileErrorOccurred(attachment: Attachment) {
        onFailedGetAttachment(attachment)
    }

    override fun onAttachingFileFinished(attachment: Attachment) {
        if (AttachmentHelper.checkAttachment(attachment)) {
            onGetAttachment(attachment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentHelper.resolveFragmentResult<BaseFragment>(this, requestCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // endregion
}
