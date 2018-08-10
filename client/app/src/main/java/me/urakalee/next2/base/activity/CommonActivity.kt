package me.urakalee.next2.base.activity

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import me.shouheng.notepal.activity.base.ThemedActivity

/**
 * @author Uraka.Lee
 */
abstract class CommonActivity : ThemedActivity(),
        ColorChooserDialog.ColorCallback {

    protected abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        if (layoutResId <= 0) {
            throw AssertionError("Subclass must provide a valid layout resource id")
        }

        beforeSetContentView()

        setContentView(layoutResId)

        doCreateView(savedInstanceState)
    }

    protected fun beforeSetContentView() {}

    protected abstract fun doCreateView(savedInstanceState: Bundle?)

    fun superOnBackPressed() {
        super.onBackPressed()
    }

    protected fun getCurrentFragment(@IdRes resId: Int): Fragment {
        return supportFragmentManager.findFragmentById(resId)
    }

    /**
     * Register your events here to receive the color selection message.
     */
    override fun onColorSelection(dialog: ColorChooserDialog, selectedColor: Int) {}

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}
}
