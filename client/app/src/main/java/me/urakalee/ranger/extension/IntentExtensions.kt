@file:JvmName("IntentExtensions")

package me.urakalee.ranger.extension

import android.content.Intent
import android.os.Bundle
import java.io.Serializable

/**
 * @author Uraka.Lee
 */
const val ARG_BUNDLE = "ARG_BUNDLE"

fun Intent.hasExtraInBundle(key: String): Boolean {
    return this.getBundleExtra(me.urakalee.ranger.extension.ARG_BUNDLE)?.containsKey(key) ?: false
}

fun <T : Serializable> Intent.putToBundle(key: String, value: T) {
    val bundle = this.getBundleExtra(me.urakalee.ranger.extension.ARG_BUNDLE) ?: Bundle()
    bundle.putSerializable(key, value)
    this.putExtra(me.urakalee.ranger.extension.ARG_BUNDLE, bundle)
}

inline fun <reified T> Intent.getFromBundle(key: String, defaultValue: T? = null): T? {
    val bundle = this.getBundleExtra(me.urakalee.ranger.extension.ARG_BUNDLE)
    return bundle?.get(key) as? T ?: defaultValue // XXX: use reified to avoid warning
}