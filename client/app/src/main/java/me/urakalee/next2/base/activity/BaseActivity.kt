package me.urakalee.next2.base.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import me.shouheng.notepal.util.ColorUtils
import me.urakalee.next2.support.misc.getPackageName
import me.urakalee.next2.support.permission.PermissionUtils

/**
 * @author Uraka.Lee
 */
abstract class BaseActivity : AppCompatActivity() {

    //region permission

    private var onGetPermissionCallback: PermissionUtils.OnGetPermissionCallback? = null

    fun setOnGetPermissionCallback(onGetPermissionCallback: PermissionUtils.OnGetPermissionCallback) {
        this.onGetPermissionCallback = onGetPermissionCallback
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (onGetPermissionCallback != null) {
                onGetPermissionCallback!!.onGetPermission()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Add array length check logic to avoid ArrayIndexOutOfBoundsException
                if (permissions.isNotEmpty() && !shouldShowRequestPermissionRationale(permissions[0])) {
                    showPermissionSettingDialog(requestCode)
                } else {
                    makeToast(this, getToastMessage(requestCode))
                }
            } else {
                makeToast(this, getToastMessage(requestCode))
            }
        }
    }

    private fun showPermissionSettingDialog(requestCode: Int) {
        val permissionName = PermissionUtils.getPermissionName(this, requestCode)
        val msg = String.format(getString(org.polaric.colorful.R.string.set_permission_in_setting), permissionName)
        AlertDialog.Builder(this)
                .setTitle(org.polaric.colorful.R.string.setting_permission)
                .setMessage(msg)
                .setPositiveButton(org.polaric.colorful.R.string.to_set) { dialog, which -> toSetPermission() }
                .setNegativeButton(org.polaric.colorful.R.string.text_cancel, null)
                .create()
                .show()
    }

    private fun toSetPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", getPackageName(this@BaseActivity), null)
        intent.data = uri
        startActivity(intent)
    }

    /**
     * Get the permission toast message according to request code. If the permission name can be found,
     * we will show the permission name in the message, otherwise show the default message.
     *
     * @param requestCode the request code
     * @return the message to toast
     */
    private fun getToastMessage(requestCode: Int): String {
        val permissionName = PermissionUtils.getPermissionName(this, requestCode)
        val defName = getString(org.polaric.colorful.R.string.permission_default_permission_name)
        return if (defName == permissionName) {
            getString(org.polaric.colorful.R.string.permission_denied_try_again_after_set)
        } else {
            String.format(getString(org.polaric.colorful.R.string.permission_denied_try_again_after_set_given_permission), permissionName)
        }
    }

    //endregion
    //region colorful

    protected fun setTranslucentStatusBar() {
        val mContentView = findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT);
        // set child View not fill the system window
        val mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            mChildView.fitsSystemWindows = false
        }
        // First translucent status bar.
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // After LOLLIPOP just set LayoutParams.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT
        // must call requestApplyInsets, otherwise it will have space in screen bottom
        if (mChildView != null) {
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }

    protected fun primaryColor(): Int {
        return ColorUtils.primaryColor(this)
    }

    protected fun accentColor(): Int {
        return ColorUtils.accentColor(this)
    }

    //endregion

    companion object {

        private var toast: Toast? = null

        private fun makeToast(context: Context, @StringRes msgRes: Int) {
            if (toast == null) {
                toast = Toast.makeText(context.applicationContext, msgRes, Toast.LENGTH_SHORT)
                toast!!.show()
            } else {
                toast!!.setText(msgRes)
                toast!!.show()
            }
        }

        private fun makeToast(context: Context, msg: String) {
            if (toast == null) {
                toast = Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT)
                toast!!.show()
            } else {
                toast!!.setText(msg)
                toast!!.show()
            }
        }
    }
}
