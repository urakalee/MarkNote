@file:JvmName("ViewExtensions")

package me.urakalee.ranger.extension

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*

/**
 * Created by meng on 2017/11/21.
 */
fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun TextView.setFakeBold(fakeBold: Boolean = true) {
    paint.isFakeBoldText = fakeBold
}

fun View.setLeftPadding(leftPadding: Int) {
    setPadding(leftPadding, paddingTop, paddingRight, paddingBottom)
}

fun View.setTopPadding(topPadding: Int) {
    setPadding(paddingLeft, topPadding, paddingRight, paddingBottom)
}

fun View.setRightPadding(rightPadding: Int) {
    setPadding(paddingLeft, paddingTop, rightPadding, paddingBottom)
}

fun View.setBottomPadding(bottomPadding: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, bottomPadding)
}

fun View.setLeftMargin(leftMargin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        (layoutParams as ViewGroup.MarginLayoutParams).leftMargin = leftMargin
    }
}

fun View.setTopMargin(topMargin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        (layoutParams as ViewGroup.MarginLayoutParams).topMargin = topMargin
    }
}

fun View.setRightMargin(rightMargin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        (layoutParams as ViewGroup.MarginLayoutParams).rightMargin = rightMargin
    }
}

fun View.setBottomMargin(bottomMargin: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = bottomMargin
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    })
}

fun ListView.addHideableHeaderView(headerView: View): View {
    val containerView = FrameLayout(context).apply {
        layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(headerView)
    }
    addHeaderView(containerView)
    return containerView
}

inline fun View.setIntervalClickListener(crossinline action: (View) -> Unit, interval: Long = 1000L) {
    var lastTimestamp = 0L
    setOnClickListener {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastTimestamp < interval) {
            return@setOnClickListener
        }
        lastTimestamp = currentTimestamp
        action.invoke(it)
    }
}

inline fun View.whenSizeIsAvailable(crossinline action: (width: Int, height: Int) -> Unit) {
    if (width > 0 || height > 0) {
        action.invoke(width, height)
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                if (width > 0 || height > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    action.invoke(width, height)
                }
            }
        })
    }
}

/*
 * From https://github.com/android/android-ktx
 */

inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

inline var View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

inline var View.isGone: Boolean
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

/*
 * end
 */
