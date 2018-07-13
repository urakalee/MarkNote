package me.shouheng.notepal.fragment.base;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.ViewDataBinding;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.polaric.colorful.PermissionUtils;

import java.util.LinkedList;
import java.util.List;

import me.shouheng.notepal.R;
import me.shouheng.notepal.activity.base.CommonActivity;
import me.shouheng.notepal.dialog.CategoryEditDialog;
import me.shouheng.notepal.dialog.picker.CategoryPickerDialog;
import me.shouheng.notepal.manager.LocationManager;
import me.shouheng.notepal.model.Category;
import me.shouheng.notepal.model.Location;
import me.shouheng.notepal.model.Model;
import me.shouheng.notepal.model.ModelFactory;
import me.shouheng.notepal.provider.CategoryStore;
import me.shouheng.notepal.util.NetworkUtils;
import me.shouheng.notepal.util.ToastUtils;
import me.shouheng.notepal.util.ViewUtils;
import me.shouheng.notepal.viewmodel.CategoryViewModel;
import me.shouheng.notepal.widget.FlowLayout;

/**
 * Created by wangshouheng on 2017/9/3.
 */
public abstract class BaseModelFragment<T extends Model, V extends ViewDataBinding> extends BaseFragment<V> {

    // region Base logic about category

    private List<Category> allCategories;
    private List<Category> cs = new LinkedList<>();

    private CategoryEditDialog categoryEditDialog;

    /**
     * Call this method and override {@link #onGetSelectedCategories(List)} to implement
     * the logic of getting categories.
     *
     * @param selected selected categories
     */
    protected void showCategoriesPicker(List<Category> selected) {
        List<Category> all = getAllCategories();

        // try to avoid NPE
        if (selected == null) selected = new LinkedList<>();
        cs = selected;

        for (Category c : selected) {
            for (Category a : all) {
                if (c.getCode() == a.getCode()) {
                    a.setSelected(true);
                }
            }
        }

        CategoryPickerDialog dialog = CategoryPickerDialog.newInstance(all);
        dialog.setOnConfirmClickListener(this::onGetSelectedCategories);
        dialog.setOnAddClickListener(this::showCategoryEditor);
        dialog.show(getFragmentManager(), "CATEGORY_PICKER");
    }

    private void showCategoryEditor() {
        categoryEditDialog = CategoryEditDialog.newInstance(ModelFactory.getCategory(), this::saveCategory);
        categoryEditDialog.show(getFragmentManager(), "CATEGORY_EDIT_DIALOG");
    }

    private void saveCategory(Category category) {
        ViewModelProviders.of(this).get(CategoryViewModel.class)
                .saveModel(category).observe(this, categoryResource -> {
            if (categoryResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (categoryResource.status) {
                case SUCCESS:
                    ToastUtils.makeToast(R.string.text_save_successfully);
                    allCategories.add(0, category);
                    showCategoriesPicker(cs);
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    break;
            }
        });
    }

    protected void onGetSelectedCategories(List<Category> categories) {
    }

    private List<Category> getAllCategories() {
        if (allCategories == null) {
            allCategories = CategoryStore.getInstance(getContext()).get(null, null);
        }
        return allCategories;
    }

    /**
     * Call this method and override {@link #getTagsLayout()} to implement the logic of showing tags.
     *
     * @param stringTags tags string
     */
    protected void addTagsToLayout(String stringTags) {
        if (getTagsLayout() == null) return;
        getTagsLayout().removeAllViews();
        if (TextUtils.isEmpty(stringTags)) return;
        String[] tags = stringTags.split(CategoryViewModel.CATEGORY_SPLIT);
        for (String tag : tags) addTagToLayout(tag);
    }

    protected FlowLayout getTagsLayout() {
        return null;
    }

    private void addTagToLayout(String tag) {
        if (getTagsLayout() == null) return;

        int margin = ViewUtils.dp2Px(getContext(), 2f);
        int padding = ViewUtils.dp2Px(getContext(), 5f);
        TextView tvLabel = new TextView(getContext());
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(tvLabel.getLayoutParams());
        params.setMargins(margin, margin, margin, margin);
        tvLabel.setLayoutParams(params);
        tvLabel.setPadding(padding, 0, padding, 0);
        tvLabel.setBackgroundResource(R.drawable.label_background);
        tvLabel.setText(tag);

        getTagsLayout().addView(tvLabel);
    }

    // endregion
    // region location

    protected void tryToLocate() {
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            ToastUtils.makeToast(R.string.check_network_availability);
            return;
        }
        if (getActivity() != null) {
            PermissionUtils.checkLocationPermission((CommonActivity) getActivity(), this::baiduLocate);
        }
    }

    private void baiduLocate() {
        ToastUtils.makeToast(R.string.trying_to_get_location);
        LocationManager.getInstance().locate();
    }

    protected void onGetLocation(Location location) {
    }

    // endregion

    public void onColorSelection(@ColorInt int i) {
        if (categoryEditDialog != null) {
            categoryEditDialog.updateUIBySelectedColor(i);
        }
    }
}
