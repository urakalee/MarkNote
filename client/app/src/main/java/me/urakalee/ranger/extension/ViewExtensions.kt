@file:JvmName("ViewExtensions")

package me.urakalee.ranger.extension

import android.support.annotation.Px
import android.support.v4.app.FragmentPagerAdapter
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*

/**
 * @author Uraka.Lee
 */
fun makeFragmentTag(containerId: Int, position: Int): String {
    return "android:switcher:$containerId:$position"
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

fun TextView.setFakeBold(fakeBold: Boolean = true) {
    paint.isFakeBoldText = fakeBold
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

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun View.updatePadding(
        @Px left: Int = paddingLeft,
        @Px top: Int = paddingTop,
        @Px right: Int = paddingRight,
        @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
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
