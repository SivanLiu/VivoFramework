package com.vivo.common.widget.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;

public class PhotoProvider extends AppWidgetProvider {
    public int mLayoutId = 0;
    public int mViewId = 0;
    public String mWidgetTag = "PhotoActivity";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("albuminfo")) {
            String widgetId = intent.getStringExtra("widget_id");
            if (widgetId != null && (widgetId.equals(this.mWidgetTag) ^ 1) == 0) {
                String mAlbumId = intent.getStringExtra("albumid");
                RemoteViews views = new RemoteViews(context.getPackageName(), this.mLayoutId);
                Editor editor = context.getSharedPreferences("imagephoto", 2).edit();
                editor.putString("photouri", mAlbumId);
                editor.commit();
                views.setString(this.mViewId, "updatePhotoAlbumList", mAlbumId);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass())), views);
            } else {
                return;
            }
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String albumid = context.getSharedPreferences("imagephoto", 2).getString("photouri", null);
        if (albumid != null) {
            RemoteViews views = new RemoteViews(context.getPackageName(), this.mLayoutId);
            views.setString(this.mViewId, "updatePhotoAlbumList", albumid);
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
