package me.shouheng.notepal.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.MenuRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.kennyc.bottomsheet.menu.BottomSheetMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.R;
import me.urakalee.next2.support.theme.AccentColor;
import me.urakalee.next2.support.theme.ThemeColor;

/**
 * Created by wangshouheng on 2017/3/31.
 */
public class ColorUtils {

    private static Integer primaryColor;
    private static Integer accentColor;

    private static final int DEFAULT_COLOR_ALPHA = 50;

    public static boolean isDarkTheme(Context context) {
        return false;
    }

    public static int primaryColor(Context context) {
        if (primaryColor == null) {
            ThemeColor primaryColor = ThemeColor.Companion.getDefaultColor();
            ColorUtils.primaryColor = context.getResources().getColor(primaryColor.getColorRes());
        }
        return primaryColor;
    }

    public static int accentColor(Context context) {
        if (accentColor == null) {
            AccentColor accentColor = AccentColor.Companion.getDefaultColor();
            ColorUtils.accentColor = context.getResources().getColor(accentColor.getColorRes());
        }
        return accentColor;
    }

    public static String getColorName(int color) {
        return "#" + getHexString(Color.red(color)) + getHexString(Color.green(color)) + getHexString(Color.blue(color));
    }

    private static String getHexString(int i) {
        String s = Integer.toHexString(i).toUpperCase();
        return s.length() == 1 ? "0" + s : s;
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color));
        return wrappedDrawable;
    }

    public static int calStatusBarColor(int color) {
        return calStatusBarColor(color, DEFAULT_COLOR_ALPHA);
    }

    public static int calStatusBarColor(int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

    public static int parseColor(String colorHex, int defaultValue) {
        try {
            return Color.parseColor(colorHex);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void addRipple(View view) {
        Drawable drawable;
        if (PalmUtils.isLollipop() && (drawable = PalmApp.getDrawableCompact(R.drawable.ripple)) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setForeground(drawable);
            } else {
                try {
                    Method method = View.class.getMethod("setForeground", Drawable.class);
                    if (method != null) {
                        method.invoke(view, drawable);
                    }
                } catch (NoSuchMethodException e) {
                    LogUtils.e("NoSuchMethodException" + e);
                } catch (IllegalAccessException e) {
                    LogUtils.e("IllegalAccessException" + e);
                } catch (InvocationTargetException e) {
                    LogUtils.e("InvocationTargetException" + e);
                }
            }
        }
    }

    public static BottomSheetMenu getThemedBottomSheetMenu(Context context, @MenuRes int menuRes) {
        int tintColor = PalmApp.getColorCompact(isDarkTheme(context) ?
                R.color.dark_theme_image_tint_color : R.color.light_theme_image_tint_color);
        BottomSheetMenu menu = new BottomSheetMenu(context);
        new MenuInflater(context).inflate(menuRes, menu);
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable drawable = menuItem.getIcon();
            if (drawable != null) {
                menuItem.setIcon(ColorUtils.tintDrawable(drawable, tintColor));
            }
        }
        return menu;
    }
}
