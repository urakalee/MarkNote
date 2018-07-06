@file:JvmName("DimenExtensions")

package me.urakalee.ranger.extension

import me.shouheng.notepal.PalmApp

/**
 * @author Uraka.Lee
 */
val Int.dp: Int
    inline get() = (this * PalmApp.getContext().resources.displayMetrics.density).toInt()

val Float.dp: Float
    inline get() = this * PalmApp.getContext().resources.displayMetrics.density
