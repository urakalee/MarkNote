package me.shouheng.notepal;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;
import me.shouheng.notepal.model.Model;

/**
 * TODO All the todo items in later version:
 * <p>
 * 2. Enable copy link logic when the server is ready. {@link me.shouheng.notepal.util.ModelHelper#copyLink(Activity, Model)};
 * 3. Add Google Drive logic, check if the file has backup time in google drive;
 * 6. Modify import from external logic, since current logic did nothing according to the db version and change,
 * You may also research the performance when the db version is different.
 * 7. Refine NoteViewFragment performance;
 * 8. Add sortable selections in list fragment.
 * 11. Statistic;
 * 12. Calendar + Timeline;
 * 14. Multiple platform statistics and user trace;
 * 21. Share html and associated resources, note content and resources.
 * <p>
 * 不要让用户做太多的选择！
 * 只要一个主线功能就行！
 * <p>
 * Official account:
 * 1. Contact email: shouheng2015@gmail.com
 * 2. Fabric: shouheng2015@gmail.com
 * 3. One Drive: w_shouheng@163.com
 * <p>
 * 重点：
 * 1.自动刷新到新的笔记历史栈里面，防止数据丢失；
 * 2.笔记编辑界面底部的按钮可以自定义，现在的按钮位置需要调整；
 * <p>
 * Created by wangshouheng on 2017/2/26.
 */
public class PalmApp extends Application {

    private static PalmApp mInstance;

    public static synchronized PalmApp getContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        LeakCanary.install(this);

        Fabric.with(this, new Crashlytics());
        Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build();
        Fabric.with(fabric);
    }

    public static String getStringCompact(@StringRes int resId) {
        return PalmApp.getContext().getString(resId);
    }

    @ColorInt
    public static int getColorCompact(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PalmApp.getContext().getColor(colorRes);
        } else {
            return PalmApp.getContext().getResources().getColor(colorRes);
        }
    }

    public static Drawable getDrawableCompact(@DrawableRes int resId) {
        return getContext().getDrawable(resId);
    }
}
