package me.urakalee.next2.base.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.annotation.ColorInt
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import me.shouheng.notepal.R
import me.shouheng.notepal.dialog.CategoryEditDialog
import me.shouheng.notepal.dialog.picker.CategoryPickerDialog
import me.shouheng.notepal.model.Category
import me.shouheng.notepal.model.Model
import me.shouheng.notepal.model.ModelFactory
import me.shouheng.notepal.model.data.LoadStatus
import me.shouheng.notepal.provider.CategoryStore
import me.shouheng.notepal.util.ToastUtils
import me.shouheng.notepal.util.ViewUtils
import me.shouheng.notepal.viewmodel.CategoryViewModel
import me.shouheng.notepal.widget.FlowLayout

/**
 * @author Uraka.Lee
 */
abstract class BaseModelFragment<T : Model> : BaseFragment() {

    // region Base logic about category

    private var allCategories: MutableList<Category>? = null
    private var categories: List<Category> = listOf()

    private var categoryEditDialog: CategoryEditDialog? = null

    protected open val tagsLayout: FlowLayout?
        get() = null

    /**
     * Call this method and override [.onGetSelectedCategories] to implement
     * the logic of getting categories.
     *
     * @param selected selected categories
     */
    protected fun showCategoriesPicker(selected: List<Category>?) {
        val allCategories = getAllCategories()
        val selectedCategories = selected ?: listOf()
        categories = selectedCategories

        for (category in selectedCategories) {
            for (item in allCategories) {
                if (category.code == item.code) {
                    item.isSelected = true
                }
            }
        }

        val dialog = CategoryPickerDialog.newInstance(allCategories)
        dialog.setOnConfirmClickListener { this.onGetSelectedCategories(it) }
        dialog.setOnAddClickListener { this.showCategoryEditor() }
        dialog.show(fragmentManager, "CATEGORY_PICKER")
    }

    private fun getAllCategories(): List<Category> {
        if (allCategories == null) {
            allCategories = CategoryStore.getInstance(context).get(null, null)
        }
        return allCategories ?: listOf()
    }

    protected open fun onGetSelectedCategories(categories: List<Category>) {}

    private fun showCategoryEditor() {
        categoryEditDialog = CategoryEditDialog.newInstance(ModelFactory.getCategory()) { this.saveCategory(it) }
        categoryEditDialog?.show(fragmentManager, "CATEGORY_EDIT_DIALOG")
    }

    private fun saveCategory(category: Category) {
        ViewModelProviders.of(this).get(CategoryViewModel::class.java)
                .saveModel(category)
                .observe(this, Observer { categoryResource ->
                    if (categoryResource == null) {
                        ToastUtils.makeToast(R.string.text_error_when_save)
                        return@Observer
                    }
                    when (categoryResource.status) {
                        LoadStatus.SUCCESS -> {
                            ToastUtils.makeToast(R.string.text_save_successfully)
                            allCategories?.add(0, category)
                            showCategoriesPicker(categories)
                        }
                        LoadStatus.FAILED -> {
                            ToastUtils.makeToast(R.string.text_error_when_save)
                        }
                        else -> {
                            // pass
                        }
                    }
                })
    }

    /**
     * Call this method and override [.getTagsLayout] to implement the logic of showing tags.
     *
     * @param stringTags tags string
     */
    protected fun addTagsToLayout(stringTags: String) {
        if (tagsLayout == null) return
        tagsLayout!!.removeAllViews()
        if (TextUtils.isEmpty(stringTags)) return
        val tags = stringTags.split(CategoryViewModel.CATEGORY_SPLIT.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (tag in tags) addTagToLayout(tag)
    }

    private fun addTagToLayout(tag: String) {
        if (tagsLayout == null) return

        val margin = ViewUtils.dp2Px(context!!, 2f)
        val padding = ViewUtils.dp2Px(context!!, 5f)
        val label = TextView(context)
        label.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val params = ViewGroup.MarginLayoutParams(label.layoutParams as LinearLayout.LayoutParams)
        params.setMargins(margin, margin, margin, margin)
        label.layoutParams = params
        label.setPadding(padding, 0, padding, 0)
        label.setBackgroundResource(R.drawable.label_background)
        label.text = tag

        tagsLayout!!.addView(label)
    }

    // endregion

    fun onColorSelection(@ColorInt i: Int) {
        if (categoryEditDialog != null) {
            categoryEditDialog!!.updateUIBySelectedColor(i)
        }
    }
}
