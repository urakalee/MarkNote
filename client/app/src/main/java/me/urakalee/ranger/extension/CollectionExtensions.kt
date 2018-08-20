@file:JvmName("CollectionExtensions")

package me.urakalee.ranger.extension

/**
 * @author Uraka.Lee
 */
fun Collection<*>?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * @param range use [start..end)
 *
 * @return removed
 */
fun <T> MutableList<T>.removeRange(range: IntRange): MutableList<T> {
    val startIndex = range.start
    val endIndex = range.last

    if (startIndex < 0) {
        throw IndexOutOfBoundsException("startIndex $startIndex < 0")
    }
    if (startIndex > size) {
        throw IndexOutOfBoundsException("startIndex $startIndex > size $size")
    }
    if (endIndex < 0) {
        throw IndexOutOfBoundsException("endIndex $endIndex < 0")
    }
    if (endIndex > size) {
        throw IndexOutOfBoundsException("endIndex $endIndex > size $size")
    }
    if (startIndex > endIndex) {
        throw IndexOutOfBoundsException("startIndex $startIndex > endIndex $endIndex")
    }

    if (startIndex == endIndex) {
        return mutableListOf()
    }

    val removed = mutableListOf<T>()
    val filtered = filterIndexed { index, t ->
        if (index in startIndex until endIndex) {
            removed.add(t)
            false
        } else {
            true
        }
    }
    clear()
    addAll(filtered)

    return removed
}