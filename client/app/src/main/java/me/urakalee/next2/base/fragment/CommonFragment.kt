package me.urakalee.next2.base.fragment

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.shouheng.notepal.R
import me.shouheng.notepal.util.ColorUtils

/**
 * @author Uraka.Lee
 */
abstract class CommonFragment : Fragment() {

    //region

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layoutResId <= 0) {
            throw AssertionError("Subclass must provide a valid layout resource id")
        }
        val root = inflater.inflate(layoutResId, container, false)
        if (isDarkTheme) {
            root.setBackgroundResource(R.color.dark_theme_background)
        }
        return root
    }

    protected abstract val layoutResId: Int

    protected val isDarkTheme: Boolean
        get() = ColorUtils.isDarkTheme(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        afterViewCreated(savedInstanceState)
    }

    protected abstract fun afterViewCreated(savedInstanceState: Bundle?)

    open fun onBackPressed() {
        activity?.finish()
    }

    //endregion

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = color
        }
    }

    protected fun primaryColor(): Int {
        return ColorUtils.primaryColor(context)
    }

    protected fun accentColor(): Int {
        return ColorUtils.accentColor(context)
    }
}
