package me.shouheng.notepal.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import java.util.Arrays;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.R;
import me.shouheng.notepal.widget.desktop.ListWidgetProvider;

/**
 * Created by wang shouheng on 2018/1/25.*/
public class AppWidgetUtils {

    public static void notifyAppWidgets() {
        // Home widgets
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(PalmApp.getContext());
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(PalmApp.getContext(), ListWidgetProvider.class));
        LogUtils.d("Notifies AppWidget data changed for widgets " + Arrays.toString(ids));
        widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }
}
