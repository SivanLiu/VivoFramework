package com.vivo.common.widget.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class PhotoActivity extends Activity {
    private int mAppWidgetId = -1;
    public int mLayoutId = 0;
    public String mWidgetTag = "PhotoActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppWidgetId = getIntent().getIntExtra("appWidgetId", -1);
        if (this.mAppWidgetId == -1) {
            setResult(0);
            finish();
            return;
        }
        Intent intent = new Intent("com.android.gallery3d.app.AlbumPicker.PICK_VIEW");
        intent.setType("image/*");
        intent.setComponent(ComponentName.unflattenFromString("com.android.gallery3d/.app.AlbumPicker"));
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 0 || data == null) {
            finish();
            return;
        }
        String id = data.getStringExtra("bucketId");
        Intent result = new Intent();
        result.setAction("albuminfo");
        result.putExtra("widget_id", this.mWidgetTag);
        result.putExtra("albumid", id);
        sendBroadcast(result);
        AppWidgetManager.getInstance(this).updateAppWidget(this.mAppWidgetId, new RemoteViews(getPackageName(), this.mLayoutId));
        Intent intent = new Intent();
        intent.putExtra("appWidgetId", this.mAppWidgetId);
        setResult(-1, intent);
        finish();
    }
}
