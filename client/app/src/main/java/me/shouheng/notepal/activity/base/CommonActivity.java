package me.shouheng.notepal.activity.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import me.urakalee.next2.base.activity.BaseActivity;

/**
 * Created by wang shouheng on 2017/12/21.
 */
@SuppressLint("Registered")
public abstract class CommonActivity<T extends ViewDataBinding>
        extends BaseActivity implements ColorChooserDialog.ColorCallback {

    private T binding;

    protected abstract int getLayoutResId();

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        if (getLayoutResId() <= 0) {
            throw new AssertionError("Subclass must provide a valid layout resource id");
        }

        binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutResId(), null, false);

        beforeSetContentView();

        setContentView(binding.getRoot());

        doCreateView(savedInstanceState);
    }

    protected void beforeSetContentView() {
    }

    protected abstract void doCreateView(Bundle savedInstanceState);

    protected final T getBinding() {
        return binding;
    }

    protected Fragment getCurrentFragment(@IdRes int resId) {
        return getSupportFragmentManager().findFragmentById(resId);
    }

    protected <M extends Activity> void startActivity(Class<M> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    protected <M extends Activity> void startActivityForResult(Class<M> activityClass, int requestCode) {
        startActivityForResult(new Intent(this, activityClass), requestCode);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }
}
