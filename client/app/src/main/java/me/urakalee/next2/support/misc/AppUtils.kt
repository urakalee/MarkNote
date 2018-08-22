package me.urakalee.next2.support.misc

import android.content.Context

/**
 * @author Uraka.Lee
 */
fun getPackageName(context: Context?): String? {
    return context?.packageManager?.getPackageInfo(context.packageName, 0)?.packageName
}
