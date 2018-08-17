@file:JvmName("ContextUtils")

package me.urakalee.ranger.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * @author Uraka.Lee
 */
fun Context.showSoftKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.showSoftInput(view, 0)
}

fun Context.hideSoftKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.toggleSoftKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.toggleSoftInputFromWindow(view.windowToken, 0, InputMethodManager.HIDE_NOT_ALWAYS)
}
