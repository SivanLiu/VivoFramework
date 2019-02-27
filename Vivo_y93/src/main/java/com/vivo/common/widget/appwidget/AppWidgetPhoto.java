package com.vivo.common.widget.appwidget;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.vivo.common.provider.MusicStore.BucketColumns;
import com.vivo.services.vivodevice.VivoDeviceNative;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class AppWidgetPhoto {
    private static final boolean DEBUG = SystemProperties.get("ro.build.type").equals("eng");
    private static final String TAG = "AppWidgetPhoto";
    private static final int UPDATE_FOR_ALBUM_ID_CHANGE = 0;
    private static final int UPDATE_FOR_ALBUM_LIST_CHANGE = 1;
    private static final int UPDATE_FOR_NEXT_IMAGE = 2;
    private static final int UPDATE_FOR_PREV_IMAGE = 3;
    private String mAlbumId = null;
    private Bitmap mBitmap;
    private OnAlbumInfoChange mCallback;
    private Canvas mCanvas = null;
    private Context mContext = null;
    private Bitmap mDefaultBitmap = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            AppWidgetPhoto.this.logd("handle message  is :" + what);
            if (what == 1) {
                AppWidgetPhoto.this.mCallback.onUpdateAlbum(AppWidgetPhoto.this.mBitmap);
            }
            if (what == 2) {
                AppWidgetPhoto.this.mCallback.nextAlbum(AppWidgetPhoto.this.mBitmap);
            }
            if (what == 3) {
                AppWidgetPhoto.this.mCallback.prevAlbum(AppWidgetPhoto.this.mBitmap);
            }
        }
    };
    private int mHeight;
    private int mIndex = 0;
    private int[] mList = null;
    private Paint mPaint = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AppWidgetPhoto.this.logd("onReceive    action:" + action + "     mStorageMode:" + AppWidgetPhoto.this.mStorageMode);
            if (action.equals("android.intent.action.MEDIA_SCANNER_FINISHED")) {
                if (AppWidgetPhoto.this.mStorageMode) {
                    AppWidgetPhoto.this.mStorageMode = false;
                    AppWidgetPhoto.this.mCallback.onStorageModeChange(false);
                    return;
                }
                AppWidgetPhoto.this.update();
            } else if (action.equals("com.photos.update") || "android.intent.action.MEDIA_BAD_REMOVAL".equals(action) || "android.intent.action.MEDIA_REMOVED".equals(action)) {
                if (!AppWidgetPhoto.this.mStorageMode) {
                    AppWidgetPhoto.this.update();
                }
            } else if ("android.intent.action.MEDIA_SHARED".equals(action)) {
                AppWidgetPhoto.this.mStorageMode = true;
                AppWidgetPhoto.this.mCallback.onStorageModeChange(true);
            }
        }
    };
    private Bitmap mShapeBitmap = null;
    private boolean mStorageMode = false;
    private String mWidgetTag;
    private int mWidth;
    private Handler mWorkHandler;
    private HandlerThread mWorkThread;
    private Runnable nextRunnable = new Runnable() {
        public void run() {
            AppWidgetPhoto.this.mBitmap = AppWidgetPhoto.this.loadImage();
            AppWidgetPhoto.this.mHandler.sendEmptyMessage(2);
        }
    };
    private Runnable prevRunnable = new Runnable() {
        public void run() {
            AppWidgetPhoto.this.mBitmap = AppWidgetPhoto.this.loadImage();
            AppWidgetPhoto.this.mHandler.sendEmptyMessage(3);
        }
    };
    private Runnable updateRunnable = new Runnable() {
        public void run() {
            int[] list = AppWidgetPhoto.getImageList(AppWidgetPhoto.this.mContext, AppWidgetPhoto.this.mAlbumId);
            if (!AppWidgetPhoto.this.isEqualList(list, AppWidgetPhoto.this.mList)) {
                AppWidgetPhoto.this.mList = list;
                AppWidgetPhoto.this.mBitmap = AppWidgetPhoto.this.loadImage();
                AppWidgetPhoto.this.mHandler.sendEmptyMessage(1);
            }
        }
    };

    public interface OnAlbumInfoChange {
        void nextAlbum(Bitmap bitmap);

        void onStorageModeChange(boolean z);

        void onUpdateAlbum(Bitmap bitmap);

        void prevAlbum(Bitmap bitmap);
    }

    public AppWidgetPhoto(Context context, OnAlbumInfoChange callback, Bitmap defaultBitmap, Bitmap shape, String tag) {
        this.mWidgetTag = tag;
        this.mContext = context;
        this.mShapeBitmap = shape;
        this.mWidth = shape.getWidth();
        this.mHeight = shape.getHeight();
        this.mCanvas = new Canvas();
        this.mPaint = new Paint(1);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        this.mDefaultBitmap = getShapeBitmap(defaultBitmap);
        this.mCallback = callback;
        this.mStorageMode = getStorageState(context);
    }

    public AppWidgetPhoto(Context context, OnAlbumInfoChange callback, Bitmap defaultBitmap, int width, int height, String tag) {
        this.mWidgetTag = tag;
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;
        this.mCanvas = new Canvas();
        this.mDefaultBitmap = getScaleBitmap(defaultBitmap);
        this.mCallback = callback;
        this.mStorageMode = getStorageState(context);
    }

    private void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, str);
        }
    }

    public void register() {
        logd("register receiver");
        this.mWorkThread = new HandlerThread("AppWidgetPhoto#Backgroundwork");
        this.mWorkThread.start();
        this.mWorkHandler = new Handler(this.mWorkThread.getLooper());
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.photos.update");
        this.mContext.registerReceiver(this.mReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
        filter2.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        filter2.addAction("android.intent.action.MEDIA_REMOVED");
        filter2.addAction("android.intent.action.MEDIA_SHARED");
        filter2.addDataScheme("file");
        this.mContext.registerReceiver(this.mReceiver, filter2);
    }

    public void unregister() {
        try {
            logd("unregister receiver");
            this.mWorkThread.getLooper().quit();
            this.mContext.unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
        }
    }

    private void update() {
        this.mWorkHandler.post(this.updateRunnable);
    }

    public Bitmap getDefaultBitamp() {
        return this.mDefaultBitmap;
    }

    public void updateAlbumList(String albumId) {
        this.mAlbumId = albumId;
        this.mWorkHandler.post(this.updateRunnable);
    }

    private boolean isEqualList(int[] srcList, int[] dstList) {
        if (srcList == null && dstList == null) {
            return true;
        }
        return false;
    }

    public boolean noImages() {
        return this.mList == null || this.mList.length == 0;
    }

    public void nextImage() {
        this.mIndex++;
        this.mIndex = this.mIndex > this.mList.length + -1 ? 0 : this.mIndex;
        this.mWorkHandler.post(this.nextRunnable);
    }

    public void prevImage() {
        this.mIndex--;
        this.mIndex = this.mIndex < 0 ? this.mList.length - 1 : this.mIndex;
        this.mWorkHandler.post(this.prevRunnable);
    }

    public void startAlbumActivity() {
        if (this.mAlbumId == null || this.mList == null || this.mIndex >= this.mList.length || this.mIndex < 0) {
            pickImageFile();
            return;
        }
        Uri uri = Uri.parse("content://media/external/images/media/" + this.mList[this.mIndex]);
        if (isValidDataUri(uri)) {
            displayPhotos(uri);
        } else {
            pickImageFile();
        }
    }

    private void displayPhotos(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        intent.putExtra("fromwidget", true);
        intent.putExtra("widget_id", this.mWidgetTag);
        intent.setDataAndType(uri, "image/*");
        intent.putExtras(new Bundle());
        intent.setComponent(ComponentName.unflattenFromString("com.android.gallery3d/.app.Gallery"));
        this.mContext.startActivity(intent);
    }

    private void pickImageFile() {
        Intent intent = new Intent("com.android.gallery3d.app.AlbumPicker.PICK_VIEW");
        intent.addFlags(GestureConstants.IO_BUFFER_SIZE);
        intent.addFlags(268435456);
        intent.putExtra("widget_id", this.mWidgetTag);
        intent.setType("image/*");
        intent.setComponent(ComponentName.unflattenFromString("com.android.gallery3d/.app.AlbumPicker"));
        intent.putExtra("fromwidget", true);
        this.mContext.startActivity(intent);
    }

    private boolean isValidDataUri(Uri dataUri) {
        if (dataUri == null) {
            return false;
        }
        try {
            this.mContext.getContentResolver().openAssetFileDescriptor(dataUri, "r").close();
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private Bitmap loadImage() {
        try {
            this.mIndex = this.mIndex > this.mList.length + -1 ? 0 : this.mIndex;
            Bitmap bitmap = getCorrectlyOrientedImage(this.mContext, Uri.parse("content://media/external/images/media/" + this.mList[this.mIndex]), this.mWidth, this.mHeight);
            return this.mShapeBitmap == null ? getScaleBitmap(bitmap) : getShapeBitmap(bitmap);
        } catch (Exception e) {
            return this.mDefaultBitmap;
        }
    }

    private Bitmap getScaleBitmap(Bitmap bitmap) {
        int dstW;
        int dstH;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (h / w >= 20 || w / h >= 20) {
            dstW = this.mWidth;
            dstH = this.mHeight;
        } else {
            dstW = this.mWidth;
            dstH = (dstW * h) / w;
            if (dstH < this.mHeight) {
                dstH = this.mHeight;
                dstW = (w * dstH) / h;
            }
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true);
        Bitmap btm = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
        this.mCanvas.setBitmap(btm);
        if (dstH > this.mHeight) {
            this.mCanvas.drawBitmap(Bitmap.createBitmap(bitmap, 0, (dstH - this.mHeight) / 2, dstW, this.mHeight), 0.0f, 0.0f, null);
        } else {
            this.mCanvas.drawBitmap(Bitmap.createBitmap(bitmap, (dstW - this.mWidth) / 2, 0, this.mWidth, dstH), 0.0f, 0.0f, null);
        }
        return btm;
    }

    private Bitmap getShapeBitmap(Bitmap bitmap) {
        bitmap = getScaleBitmap(bitmap);
        Bitmap btm = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
        this.mCanvas.setBitmap(btm);
        this.mCanvas.drawBitmap(this.mShapeBitmap, 0.0f, 0.0f, null);
        this.mCanvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mPaint);
        return btm;
    }

    private static boolean getStorageState(Context context) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService("storage");
        try {
            Method sMethodgetVolumePaths = mStorageManager.getClass().getDeclaredMethod("getVolumePaths", new Class[0]);
            Method sMethodgetVolumeState = mStorageManager.getClass().getDeclaredMethod("getVolumeState", new Class[]{String.class});
            try {
                String[] paths = (String[]) sMethodgetVolumePaths.invoke(mStorageManager, new Object[0]);
                int length = paths.length;
                int i = 0;
                while (i < length) {
                    String path = paths[i];
                    if (path.contains("/sdcard0") || path.contains("/sdcard")) {
                        return !((String) sMethodgetVolumeState.invoke(mStorageManager, new Object[]{path})).equals("mounted");
                    } else {
                        i++;
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    private static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri, int width, int height) throws IOException {
        int rotatedWidth;
        int rotatedHeight;
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        Options dbo = new Options();
        dbo.inJustDecodeBounds = true;
        Bitmap srcBitmap = BitmapFactory.decodeStream(is, null, dbo);
        dbo.inJustDecodeBounds = false;
        is.close();
        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{VivoDeviceNative.ORIENTATION}, null, null, null);
        if (cursor.getCount() != 1) {
        }
        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }
        is = context.getContentResolver().openInputStream(photoUri);
        int be = (int) Math.max((float) (rotatedWidth / width), ((float) rotatedHeight) / (((float) height) * 1.9f));
        if (be <= 1) {
            be = 1;
        }
        dbo.inSampleSize = be;
        srcBitmap = BitmapFactory.decodeStream(is, null, dbo);
        is.close();
        if (orientation <= 0) {
            return srcBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) orientation);
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

    private static int[] getImageList(Context context, String albumid) {
        if (albumid == null) {
            return null;
        }
        int[] list = null;
        try {
            Cursor cursorImages = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_id", BucketColumns.BUCKET_NAME}, "bucket_id=" + albumid, null, "datetaken DESC, _id DESC");
            if (cursorImages != null && cursorImages.moveToFirst()) {
                int[] ids = new int[cursorImages.getCount()];
                int ctr = 0;
                while (!Thread.interrupted()) {
                    ids[ctr] = (int) cursorImages.getLong(0);
                    ctr++;
                    if (!cursorImages.moveToNext()) {
                        cursorImages.close();
                        list = ids;
                    }
                }
                cursorImages.close();
                return null;
            }
            if (!cursorImages.isClosed()) {
                cursorImages.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
