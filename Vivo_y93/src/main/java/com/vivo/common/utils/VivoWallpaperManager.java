package com.vivo.common.utils;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.FtBuild;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.Events;
import com.vivo.services.daemon.VivoDmServiceProxy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoWallpaperManager {
    private static final String KEY_TYPE_LOCKSCREEN = "vivo_type_lockscreen_wallpaper";
    private static final String LIVE_WALLPAPER_PATH = "/data/bbkcore/background/livewallpaper.png";
    private static final String PROP_LOCK_WALLPAPER = "ro.config.lock.wallpaper";
    private static final ArrayList<String> SPECIAL_PROJ_NAME = new ArrayList();
    static final String TAG = "VivoWallpaperManager";
    private static final int TYPE_LIVE_WALLPAPER = 1;
    private static final int TYPE_STILL_WALLPAPER = 0;
    private static VivoWallpaperManager mInstance = null;
    private final String ACTION_SET_LOCKBG = "com.android.intent.bbk_setlockbg_return";
    private final boolean DEBUG = true;
    private final String EXTRA_SET_LOCKBG = "isSet";
    private final String KEY_CURLOCKID = "lock_screen_theme_id";
    private final String KEY_HOLIDAY_ENABLE = "change_holiday_wallpaper_enable";
    private final String KEY_TRADITIONLOCKID = "lock_screen_theme_tradition_id";
    private final String LOCK_DIR = "/data/bbkcore/";
    private final String LOCK_PATH = "/data/bbkcore/lockscreen";
    private final String LOCK_PATH_OLD = "/data/data/com.android.settings/lock/lockScreenWallpaper";
    private final int MSG_LOCKPAPAER_CHANGE = ExceptionCode.NET_UNCONNECTED;
    private OnCheckedChangeLockbgListener mCheckedChangeLockbgListener;
    private Context mContext = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == ExceptionCode.NET_UNCONNECTED) {
                synchronized (VivoWallpaperManager.this.mLock) {
                    VivoWallpaperManager.this.changeCurLockBitmap();
                }
            }
        }
    };
    private Bitmap mHolidayBitmap = null;
    private int mHolidayId = -1;
    private Object mLock = new Object();
    private Bitmap mLockScreenBackBitmap = null;
    private Bitmap mLockScreenBitmap = null;
    private Resources mResources = null;
    private BroadcastReceiver mSetLockbgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.intent.bbk_setlockbg_return")) {
                boolean isSet = intent.getBooleanExtra("isSet", false);
                if (VivoWallpaperManager.this.mCheckedChangeLockbgListener != null) {
                    VivoWallpaperManager.this.mCheckedChangeLockbgListener.onCheckedChanged(isSet);
                }
                VivoWallpaperManager.this.unregisterSetLockbgDialogReceiver();
            }
        }
    };
    private Bitmap mSuperPowerBitmap = null;
    private boolean mSupportCache = false;
    private BroadcastReceiver mUpdateLockbgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.mediatek.lockscreen.action.WALLPAPER_SET")) {
                boolean complete = intent.getBooleanExtra("com.mediatek.lockscreen.extra.COMPLETE", true);
                VivoWallpaperManager.this.mHandler.removeMessages(ExceptionCode.NET_UNCONNECTED);
                VivoWallpaperManager.this.mHandler.sendEmptyMessage(ExceptionCode.NET_UNCONNECTED);
            }
        }
    };
    private WallpaperManager mWallpaperManager;

    public interface OnCheckedChangeLockbgListener {
        void onCheckedChanged(boolean z);
    }

    static {
        SPECIAL_PROJ_NAME.add("PD1708F_EX");
        SPECIAL_PROJ_NAME.add("PD1708");
    }

    public VivoWallpaperManager(Context context) {
        this.mContext = context;
        this.mResources = this.mContext.getResources();
    }

    public static VivoWallpaperManager getInstance(Context context) {
        if (mInstance == null) {
            Log.v(TAG, "mInstance is null, need to create an new instance");
            mInstance = new VivoWallpaperManager(context);
        }
        return mInstance;
    }

    private void updateLockBg() {
        registerUpdateLockbgReceiver();
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0077 A:{Catch:{ FileNotFoundException -> 0x007b }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLockScreen(int resId) throws NotFoundException, IOException {
        Throwable th;
        if (resId != -1) {
            Log.v(TAG, "setLockScreen by resId : " + resId);
            try {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(new File("/data/bbkcore/lockscreen"), 939524096);
                if (fd != null) {
                    Runtime rt = Runtime.getRuntime();
                    FileOutputStream fos = null;
                    try {
                        String[] cmds = new String[]{"sh", "-c", "chmod 777 /data/bbkcore/lockscreen"};
                        FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                        try {
                            saveFile(this.mResources.openRawResource(resId), fos2);
                            rt.exec(cmds);
                            putIntToSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 0);
                            FileDescriptor wallpaperFd = fos2.getFD();
                            if (wallpaperFd != null) {
                                wallpaperFd.sync();
                            }
                            notifyCallbacksLocked();
                            if (fos2 != null) {
                                fos2.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fos = fos2;
                            if (fos != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (fos != null) {
                            fos.close();
                        }
                        throw th;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x006a A:{Catch:{ FileNotFoundException -> 0x006e }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLockScreen(Bitmap bitmap) throws NotFoundException, IOException {
        Throwable th;
        if (bitmap != null) {
            Log.v(TAG, "setLockScreen by bitmap : ");
            try {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(new File("/data/bbkcore/lockscreen"), 939524096);
                if (fd != null) {
                    Runtime rt = Runtime.getRuntime();
                    FileOutputStream fos = null;
                    try {
                        String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL);
                        String[] cmds = new String[]{"sh", "-c", "chmod 777 /data/bbkcore/lockscreen"};
                        FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                        try {
                            bitmap.compress(CompressFormat.PNG, 90, fos2);
                            rt.exec(cmds);
                            putIntToSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 0);
                            FileDescriptor wallpaperFd = fos2.getFD();
                            if (wallpaperFd != null) {
                                wallpaperFd.sync();
                            }
                            notifyCallbacksLocked();
                            if (fos2 != null) {
                                fos2.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fos = fos2;
                            if (fos != null) {
                                fos.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (fos != null) {
                        }
                        throw th;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x005f A:{Catch:{ Exception -> 0x0063 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void setStream(InputStream data) throws IOException {
        Throwable th;
        if (data != null) {
            Log.v(TAG, "setLockScreen by stream ");
            try {
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(new File("/data/bbkcore/lockscreen"), 939524096);
                if (fd != null) {
                    Runtime rt = Runtime.getRuntime();
                    FileOutputStream fos = null;
                    try {
                        String[] cmds = new String[]{"sh", "-c", "chmod 777 /data/bbkcore/lockscreen"};
                        FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                        try {
                            saveFile(data, fos2);
                            rt.exec(cmds);
                            putIntToSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 0);
                            FileDescriptor wallpaperFd = fos2.getFD();
                            if (wallpaperFd != null) {
                                wallpaperFd.sync();
                            }
                            notifyCallbacksLocked();
                            if (fos2 != null) {
                                fos2.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            fos = fos2;
                            if (fos != null) {
                                fos.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (fos != null) {
                        }
                        throw th;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getLockScreen(boolean showHoliday) {
        Bitmap bmp = null;
        int resId = getHolidayPaperResId(showHoliday);
        if (resId != -1) {
            Log.v(TAG, "getLockScreen resId = " + resId + ",mHolidayId = " + this.mHolidayId);
            if (this.mHolidayId != resId) {
                if (!(this.mHolidayBitmap == null || (this.mHolidayBitmap.isRecycled() ^ 1) == 0)) {
                    Log.v(TAG, "recycle holiday old bitmap");
                    this.mHolidayBitmap = null;
                }
                this.mHolidayBitmap = BitmapFactory.decodeResource(this.mResources, resId);
                if (this.mHolidayBitmap != null) {
                    this.mHolidayId = resId;
                }
            }
            if (this.mHolidayBitmap != null && this.mHolidayBitmap.isRecycled()) {
                Log.v(TAG, "getLockScreen mHolidayBitmap has been recycled.");
                this.mHolidayBitmap = BitmapFactory.decodeResource(this.mResources, resId);
                if (this.mHolidayBitmap != null) {
                    this.mHolidayId = resId;
                }
            }
            bmp = this.mHolidayBitmap;
            if (bmp == null) {
                Log.v(TAG, "getLockScreen bmp null.");
            }
            if (!(bmp == null || this.mContext == null || getIntFromSettings(this.mContext, "last_holiday_res_id", -1) == resId)) {
                notifyCallbacksLocked();
                putIntToSettings(this.mContext, "last_holiday_res_id", resId);
            }
        } else if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            if (this.mSuperPowerBitmap == null || this.mSuperPowerBitmap.isRecycled()) {
                this.mSuperPowerBitmap = BitmapFactory.decodeResource(this.mResources, 50463676);
            }
            if (!(this.mSuperPowerBitmap == null || (this.mSuperPowerBitmap.isRecycled() ^ 1) == 0)) {
                if (!(this.mContext == null || getIntFromSettings(this.mContext, "last_holiday_res_id", -1) == 50463676)) {
                    notifyCallbacksLocked();
                    putIntToSettings(this.mContext, "last_holiday_res_id", 50463676);
                }
                return this.mSuperPowerBitmap;
            }
        } else if (!(this.mContext == null || getIntFromSettings(this.mContext, "last_holiday_res_id", -1) == -1)) {
            notifyCallbacksLocked();
            putIntToSettings(this.mContext, "last_holiday_res_id", -1);
        }
        if (bmp == null) {
            if (!(this.mHolidayBitmap == null || (this.mHolidayBitmap.isRecycled() ^ 1) == 0)) {
                Log.v(TAG, "set null holiday bitmap, because isn't holiday.");
                this.mHolidayBitmap = null;
                this.mHolidayId = -1;
            }
            if (!(this.mSuperPowerBitmap == null || (this.mSuperPowerBitmap.isRecycled() ^ 1) == 0)) {
                Log.v(TAG, "set null superpower bitmap, because isn't superpower mode.");
                this.mSuperPowerBitmap = null;
            }
            synchronized (this.mLock) {
                bmp = getCurLockBitmap();
            }
        }
        return bmp;
    }

    private void saveFile(InputStream data, FileOutputStream fos) throws IOException {
        byte[] buffer = new byte[GestureConstants.IO_BUFFER_SIZE];
        while (true) {
            int amt = data.read(buffer);
            if (amt > 0) {
                fos.write(buffer, 0, amt);
            } else {
                return;
            }
        }
    }

    public int getHolidayPaperResId(boolean showHoliday) {
        if (FtBuild.isOverSeas()) {
            return -1;
        }
        if (showHoliday || System.getInt(this.mContext.getContentResolver(), "change_holiday_wallpaper_enable", 1) == 1) {
            File file = new File("/data/bbkcore/lockscreen");
            int resId = Lunar.getDefault().getHolidayId(file.lastModified());
            if (resId != -1) {
                Lunar.LastDayIsHoliday = true;
                file.setLastModified(1);
                Log.d(TAG, "getHolidayPaper success");
            } else {
                Log.d(TAG, "getHolidayPaper fail");
            }
            return resId;
        }
        Log.d(TAG, "change_holiday_wallpaper_enable != 1");
        return -1;
    }

    private Bitmap getCurLockBitmap() {
        Bitmap lockscreen = null;
        if (this.mSupportCache) {
            if (this.mLockScreenBackBitmap != null) {
                Bitmap tempBitmap = this.mLockScreenBitmap;
                this.mLockScreenBitmap = this.mLockScreenBackBitmap;
                this.mLockScreenBackBitmap = tempBitmap;
                if (!(this.mLockScreenBackBitmap == null || (this.mLockScreenBackBitmap.isRecycled() ^ 1) == 0)) {
                    Log.v(TAG, "recycle original front bitmap");
                }
                this.mLockScreenBackBitmap = null;
            }
            if (this.mLockScreenBitmap != null && this.mLockScreenBitmap.isRecycled()) {
                Log.v(TAG, "mLockScreenBitmap have recycle,get again.");
                this.mLockScreenBitmap = null;
            }
            if (this.mLockScreenBitmap != null) {
                Log.v(TAG, "reuse cache");
                lockscreen = this.mLockScreenBitmap;
            }
        }
        if (lockscreen == null) {
            File file;
            if (isLiveWallpaper()) {
                file = new File(LIVE_WALLPAPER_PATH);
            } else {
                file = new File("/data/bbkcore/lockscreen");
            }
            if (file == null || (file.exists() ^ 1) != 0) {
                file = new File("/data/data/com.android.settings/lock/lockScreenWallpaper");
            }
            if (file != null && file.exists()) {
                try {
                    Log.v(TAG, "isLiveWallpaper = " + isLiveWallpaper());
                    if (isLiveWallpaper()) {
                        lockscreen = getCurrentLockscreenLiveWallPaper(this.mContext);
                    } else {
                        lockscreen = getCurrentLockscreenLocked(this.mContext);
                    }
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "No memory load current wallpaper", e);
                }
            }
            if (lockscreen == null) {
                try {
                    lockscreen = getDefaultLockscreenLocked(this.mContext);
                } catch (OutOfMemoryError e2) {
                    Log.w(TAG, "No memory load default wallpaper", e2);
                }
            }
            if (this.mSupportCache) {
                this.mLockScreenBitmap = lockscreen;
            }
        }
        return lockscreen;
    }

    public void changeCurLockBitmap() {
        Log.v(TAG, "lockscreen wallpaper changed, forget mLockScreenBitmap");
        this.mLockScreenBitmap = null;
    }

    public void setOnCheckedChangeLockbgListener(OnCheckedChangeLockbgListener listener) {
        this.mCheckedChangeLockbgListener = listener;
    }

    public void showDialog() {
        Log.v(TAG, "showDialog");
        if (System.getInt(this.mContext.getContentResolver(), "lock_screen_theme_id", 100) < 0) {
            System.putInt(this.mContext.getContentResolver(), "lock_screen_theme_id", System.getInt(this.mContext.getContentResolver(), "lock_screen_theme_tradition_id", 0));
            if (this.mCheckedChangeLockbgListener != null) {
                this.mCheckedChangeLockbgListener.onCheckedChanged(true);
            }
        } else if (this.mCheckedChangeLockbgListener != null) {
            this.mCheckedChangeLockbgListener.onCheckedChanged(true);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void registerUpdateLockbgReceiver() {
        if (!this.mSupportCache) {
            this.mSupportCache = true;
            this.mContext.registerReceiver(this.mUpdateLockbgReceiver, new IntentFilter("com.mediatek.lockscreen.action.WALLPAPER_SET"));
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void unregisterUpdateLockbgReceiver() {
        if (this.mSupportCache) {
            this.mSupportCache = false;
            this.mContext.unregisterReceiver(this.mUpdateLockbgReceiver);
        }
    }

    private void registerSetLockbgDialogReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.intent.bbk_setlockbg_return");
        this.mContext.registerReceiver(this.mSetLockbgReceiver, filter);
    }

    private void unregisterSetLockbgDialogReceiver() {
        this.mContext.unregisterReceiver(this.mSetLockbgReceiver);
    }

    public boolean isLockScreenWhiteStyle(int x, int y, int width, int height) {
        Log.v(TAG, "isLockScreenWhiteStyle == " + x + ", " + y + ", " + width + ", " + height);
        Bitmap lockscreen = getLockScreen(false);
        if (lockscreen == null) {
            Log.v(TAG, "Fail to getLockScreen!!!");
            return false;
        }
        Bitmap bm = checkWallpaperSize(lockscreen, x, y, width, height);
        if (bm == null) {
            Log.v(TAG, "Fail to createBitmap!!!");
            return false;
        }
        boolean isWite = BitmapUtils.isBitmapWhiteStyle(bm);
        Log.v(TAG, "isLockScreenWhiteStyle==result:" + isWite);
        return isWite;
    }

    public boolean isWallpaperWhiteStyle(int x, int y, int width, int height) {
        Log.v(TAG, "isWallpaperWhiteStyle == " + x + ", " + y + ", " + width + ", " + height);
        Bitmap wallpaperBitmap = null;
        if (this.mWallpaperManager == null) {
            this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService("wallpaper");
        }
        if (this.mWallpaperManager == null) {
            Log.v(TAG, "Fail to get wallpaper service");
            return false;
        }
        Log.v(TAG, " isWallpaperWhiteStyle getWallpaperInfo() = " + this.mWallpaperManager.getWallpaperInfo());
        if (this.mWallpaperManager.getWallpaperInfo() == null) {
            Log.v(TAG, "isWallpaperWhiteStyle==static wallpaper");
            wallpaperBitmap = this.mWallpaperManager.getBitmap();
        } else {
            Log.v(TAG, "isWallpaperWhiteStyle==live wallpaper");
            File file = new File(LIVE_WALLPAPER_PATH);
            if (file == null || (file.exists() ^ 1) != 0) {
                Log.v(TAG, "Fail to get background of live wallpaper");
            } else {
                try {
                    wallpaperBitmap = BitmapFactory.decodeFile(LIVE_WALLPAPER_PATH);
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "Can't decode file", e);
                }
            }
        }
        if (wallpaperBitmap == null) {
            Log.v(TAG, "Fail to get wallpaper!!!");
            return false;
        }
        Bitmap bm = checkWallpaperSize(wallpaperBitmap, x, y, width, height);
        if (bm == null) {
            Log.v(TAG, "Fail to createBitmap!!!");
            return false;
        }
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(metric);
        boolean isWite = BitmapUtils.isBitmapWhiteStyle(bm, 0, 0, width, height, metric.density);
        Log.v(TAG, "isWallpaperWhiteStyle == :" + isWite);
        return isWite;
    }

    public int getKeyguardGrayValue(int x, int y, int width, int height) {
        Log.v(TAG, "getKeyguardGrayValue == " + x + ", " + y + ", " + width + ", " + height);
        int value = 0;
        Bitmap lockscreen = getLockScreen(false);
        if (lockscreen == null) {
            Log.v(TAG, "Fail to getLockScreen!!!");
            return 0;
        }
        Bitmap bm = checkWallpaperSize(lockscreen, x, y, width, height);
        if (bm != null) {
            value = BitmapUtils.getGrayValue(bm);
        }
        Log.v(TAG, "getKeyguardGrayValue=" + value);
        return value;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public Bitmap checkWallpaperSize(Bitmap wallpaperBitmap, int x, int y, int width, int height) {
        Bitmap bm = null;
        try {
            if (width == wallpaperBitmap.getWidth() && height == wallpaperBitmap.getHeight()) {
                bm = wallpaperBitmap;
                return bm;
            }
            int screenWidth = this.mContext.getResources().getDisplayMetrics().widthPixels;
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
            int screenHeight = metrics.heightPixels;
            Log.i(TAG, "checkWallpaperSize  screenWidth:" + screenWidth + " screenHeight:" + screenHeight + " wWidth:" + wallpaperBitmap.getWidth() + " wHeight:" + wallpaperBitmap.getHeight());
            if (screenWidth > wallpaperBitmap.getWidth() || screenHeight > wallpaperBitmap.getHeight()) {
                Log.v(TAG, "Need scale up wallpaper");
                float xScale = ((float) screenWidth) / ((float) wallpaperBitmap.getWidth());
                float yScale = ((float) screenHeight) / ((float) wallpaperBitmap.getHeight());
                Matrix matrix = new Matrix();
                matrix.setScale(xScale, yScale);
                Log.v(TAG, "Befor scaling, old bitmap width: " + wallpaperBitmap.getWidth() + " , height: " + wallpaperBitmap.getHeight() + " xScale:" + xScale + " yScale:" + yScale);
                wallpaperBitmap = Bitmap.createBitmap(wallpaperBitmap, 0, 0, wallpaperBitmap.getWidth(), wallpaperBitmap.getHeight(), matrix, true);
                Log.v(TAG, "After scaling, new bitmap width: " + wallpaperBitmap.getWidth() + " , height: " + wallpaperBitmap.getHeight());
            }
            if (x + width > wallpaperBitmap.getWidth() || y + height > wallpaperBitmap.getHeight()) {
                Log.v(TAG, "params error for createBitmap");
                return bm;
            } else if (wallpaperBitmap.isRecycled()) {
                Log.v(TAG, "wallpaperBitmap is Recycled ");
                return null;
            } else {
                bm = Bitmap.createBitmap(wallpaperBitmap, x, y, width, height);
                return bm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void clear() throws IOException {
        File file = new File("/data/bbkcore/lockscreen");
        if (file == null || (file.exists() ^ 1) != 0) {
            Log.v(TAG, "Lockscreen wallpaper file do not exist, so no need to reset.");
            return;
        }
        Log.v(TAG, "clear lockscreen wallpaper");
        file.delete();
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private Bitmap getCurrentLockscreenLocked(Context context) {
        File file;
        try {
            file = new File("/data/bbkcore/lockscreen");
            if (file != null) {
                int callingUid = Binder.getCallingUid();
                Log.i(TAG, "getCurrentLockscreenLocked callingUid:" + callingUid + " callingPid:" + Binder.getCallingPid() + " LOCK_PATH:" + "/data/bbkcore/lockscreen");
                Log.i(TAG, Events.DEFAULT_SORT_ORDER + file + " " + file.lastModified() + " canWrite:" + file.canWrite() + " exists:" + file.exists() + " canRead:" + file.canRead() + " canExecute:" + file.canExecute());
                if (!(file.canRead() && (file.canWrite() ^ 1) == 0)) {
                    try {
                        VivoDmServiceProxy vivoDmSrvProxy = VivoDmServiceProxy.asInterface(ServiceManager.getService("vivo_daemon.service"));
                        Log.d(TAG, "vivoDmSrvProxy = " + vivoDmSrvProxy);
                        if (vivoDmSrvProxy != null) {
                            if (SystemProperties.get("persist.vivo.vivo_daemon", Events.DEFAULT_SORT_ORDER) != Events.DEFAULT_SORT_ORDER) {
                                Log.d(TAG, "result001:" + vivoDmSrvProxy.runShellWithResult("mqhE7R6FjIP566u6ICQnZBt26WdNpilleZNeypBmjKIHq6TobRfVGEEpBXVnUuTKJKVOF1EhMZKhczVIeVAUUYbZPyJzkLCSQ56jUK8JMMcWz2WgGbZ3zjmBFz654jX0PyNG36gQq50bL1gZ4HNN+F4VKur7RdfWd5VRHpSn+ZvyEl9gTdwpVtyj8xUE8RFiGC6Vc5CT7I8tNwMFMS9hUMpoKVhc81eXsP53PLmYl5eS3mXZJBMtSj7RjiYffasy6P0heIuYuzeEA/bcAvRz8epoLzCLApFuU8j9+nHvRu1v+obq0EhbBrR87xw4EdB2ASkB5U6wA0s1UEXxxxkW3A=="));
                            } else {
                                Log.d(TAG, "result002:" + vivoDmSrvProxy.runShellWithResult("chmod 777 /data/bbkcore/lockscreen"));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "get vivo_daemon.service failed");
                    }
                }
                Log.i(TAG, Events.DEFAULT_SORT_ORDER + file + " " + file.lastModified() + " canWrite:" + file.canWrite() + " exists:" + file.exists() + " canRead:" + file.canRead() + " canExecute:" + file.canExecute());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        ParcelFileDescriptor fd = null;
        try {
            file = new File("/data/bbkcore/lockscreen");
            if (file != null && file.exists()) {
                if (!(file.canRead() && (file.canWrite() ^ 1) == 0)) {
                    try {
                        Runtime.getRuntime().exec("chmod 777 /data/bbkcore/lockscreen");
                    } catch (IOException e3) {
                        Log.w(TAG, "IOException", e3);
                    }
                }
                fd = ParcelFileDescriptor.open(file, 805306368);
            }
        } catch (FileNotFoundException e4) {
            Log.w(TAG, "Error setting wallpaper", e4);
        } catch (Exception e22) {
            e22.printStackTrace();
        }
        if (fd != null) {
            try {
                Log.v(TAG, "getCurrentLockscreenLocked");
                Bitmap bm = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options());
                try {
                    fd.close();
                } catch (IOException e5) {
                }
                return bm;
            } catch (OutOfMemoryError e6) {
                Log.w(TAG, "Can't decode file", e6);
                try {
                    fd.close();
                } catch (IOException e7) {
                }
            } catch (Throwable th) {
                try {
                    fd.close();
                } catch (IOException e8) {
                }
                throw th;
            }
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private Bitmap getCurrentLockscreenLiveWallPaper(Context context) {
        Log.v(TAG, "start getCurrentLockscreenLiveWallPaper");
        ParcelFileDescriptor fd = null;
        try {
            File file = new File(LIVE_WALLPAPER_PATH);
            if (file != null && file.exists()) {
                try {
                    Runtime.getRuntime().exec("chmod 777 /data/bbkcore/background/livewallpaper.png");
                } catch (IOException e) {
                    Log.w(TAG, "IOException", e);
                }
                fd = ParcelFileDescriptor.open(file, 805306368);
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "Error setting wallpaper", e2);
        }
        if (fd != null) {
            try {
                Log.v(TAG, "getCurrentLockscreenLiveWallPaper");
                Bitmap bm = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options());
                try {
                    fd.close();
                } catch (IOException e3) {
                }
                return bm;
            } catch (OutOfMemoryError e4) {
                Log.w(TAG, "Can't decode file", e4);
                try {
                    fd.close();
                } catch (IOException e5) {
                }
            } catch (Throwable th) {
                try {
                    fd.close();
                } catch (IOException e6) {
                }
                throw th;
            }
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private Bitmap getDefaultLockscreenLocked(Context context) {
        InputStream is = getSpecificWallpaper();
        if (is == null) {
            is = context.getResources().openRawResource(17366784);
        }
        if (is != null) {
            try {
                Log.v(TAG, "getDefaultLockscreenLocked");
                Bitmap bm = BitmapFactory.decodeStream(is, null, new Options());
                try {
                    is.close();
                } catch (IOException e) {
                }
                return bm;
            } catch (OutOfMemoryError e2) {
                Log.w(TAG, "Can't decode stream", e2);
                try {
                    is.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return null;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public Drawable getDefaultLockscreenDrawable() {
        Bitmap bm = getDefaultLockscreenLocked(this.mContext);
        if (bm != null) {
            Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
            dr.setDither(false);
            return dr;
        } else if (this.mResources != null) {
            return this.mResources.getDrawable(17366784);
        } else {
            return null;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void notifyCallbacksLocked() {
        if (this.mContext != null) {
            Intent intent = new Intent();
            intent.setAction("com.mediatek.lockscreen.action.WALLPAPER_SET.DONE");
            try {
                this.mContext.sendBroadcast(intent);
            } catch (Exception e) {
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private static int getIntFromSettings(Context context, String key, int defValue) {
        int result = defValue;
        if (context == null || key == null) {
            Log.v(TAG, "params errors");
            return defValue;
        }
        try {
            result = System.getInt(context.getContentResolver(), key, defValue);
        } catch (Exception e) {
            Log.w(TAG, "Can't get int from settings : " + key, e);
            result = defValue;
        }
        return result;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private static void putIntToSettings(Context context, String key, int value) {
        if (context == null || key == null) {
            Log.v(TAG, "params errors");
            return;
        }
        try {
            System.putInt(context.getContentResolver(), key, value);
        } catch (Exception e) {
            Log.w(TAG, "Can't write to settings : " + key, e);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public void setWallpaperComponent(ComponentName name) {
        Log.v(TAG, "setWallpaperComponent name=" + name);
        putIntToSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 1);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public boolean isLiveWallpaper() {
        Log.v(TAG, "isLiveWallpaper");
        boolean isLive = false;
        if (getHolidayPaperResId(false) != -1) {
            Log.v(TAG, "isLiveWallpaper == is holiday");
            return false;
        } else if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            Log.v(TAG, "isLiveWallpaper == is super power save");
            return false;
        } else {
            int type = getIntFromSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 0);
            Log.v(TAG, "getTypeOfLockscreen() = " + type);
            if (isHomeUsingLivewalpaper()) {
                isLive = 1 == type;
            } else {
                putIntToSettings(this.mContext, KEY_TYPE_LOCKSCREEN, 0);
            }
            return isLive;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean isHomeUsingLivewalpaper() {
        Log.v(TAG, "isHomeUsingLivewalpaper");
        boolean isLive = false;
        if (this.mWallpaperManager == null) {
            this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService("wallpaper");
        }
        if (this.mWallpaperManager != null) {
            WallpaperInfo info = this.mWallpaperManager.getWallpaperInfo();
            Log.v(TAG, "info = " + info);
            isLive = info != null;
        }
        Log.v(TAG, "isHomeUsingLivewalpaper isLive = " + isLive);
        return isLive;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private InputStream getSpecificWallpaper() {
        File file;
        String wallpaperFilePath;
        String path = SystemProperties.get(PROP_LOCK_WALLPAPER);
        if (!TextUtils.isEmpty(path)) {
            file = new File(path);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (Exception e) {
                }
            }
        }
        String prop = SystemProperties.get("persist.sys.theme.color", "undefine");
        Log.d("mutiTheme", "propLock = " + prop);
        if (!"undefine".equals(prop)) {
            wallpaperFilePath = "/system/etc/custom/papercolor/" + prop + "/default_lockscreen.png";
            Log.d("mutiTheme", "wallpaperFilePathLock = " + wallpaperFilePath);
            if (!TextUtils.isEmpty(wallpaperFilePath)) {
                file = new File(wallpaperFilePath);
                if (file.exists()) {
                    try {
                        return new FileInputStream(file);
                    } catch (IOException e2) {
                    }
                }
            }
        }
        String areaProp = SystemProperties.get("persist.sys.vivo.product.cust", "undefine");
        Log.d("mutiTheme", "areaPropLock = " + areaProp);
        if (!"undefine".equals(areaProp)) {
            wallpaperFilePath = "/system/etc/custom/area/" + areaProp + "/default_lockscreen.png";
            Log.d("mutiTheme", "wallpaperFilePathLock = " + wallpaperFilePath);
            if (!TextUtils.isEmpty(wallpaperFilePath)) {
                file = new File(wallpaperFilePath);
                if (file.exists()) {
                    try {
                        return new FileInputStream(file);
                    } catch (IOException e3) {
                    }
                }
            }
        }
        String deviceName = SystemProperties.get("ro.vivo.project", "unknow");
        InputStream wallpaperStrem;
        if (deviceName != null && "PD1624F_EX".equalsIgnoreCase(deviceName)) {
            try {
                String lcmIDStr = readLcmIDFromLocalFile();
                if (lcmIDStr != null && lcmIDStr.equals("01")) {
                    wallpaperFilePath = "system/etc/wallpaper_black_default_belt.png";
                    if (checkFileIsValid("system/etc/wallpaper_black_default_belt.png")) {
                        wallpaperStrem = generateCustomDefaultLockScreen("system/etc/wallpaper_black_default_belt.png");
                        if (wallpaperStrem != null) {
                            Log.i(TAG, "Device is black, will return black lockscreen!");
                            return wallpaperStrem;
                        }
                    }
                }
            } catch (Exception e4) {
                Log.w(TAG, "custom default lockscreen catch exception " + e4.toString());
            }
        } else if (checkIsNeedConfigSpecialWallpaper(deviceName)) {
            try {
                wallpaperStrem = handleCreateDefaultWallpaper(readLcmIDFromLocalFile());
                if (wallpaperStrem != null) {
                    return wallpaperStrem;
                }
            } catch (Exception e42) {
                e42.printStackTrace();
            }
        }
        Log.i(TAG, "return original default lockscreen");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0075 A:{SYNTHETIC, Splitter: B:35:0x0075} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007e A:{SYNTHETIC, Splitter: B:40:0x007e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readLcmIDFromLocalFile() {
        IOException e;
        Throwable th;
        File lcmFile = new File("sys/lcm/lcm_color");
        if (lcmFile != null && lcmFile.exists()) {
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(lcmFile));
                String lcmIDStr = null;
                String readStr = null;
                if (reader2 != null) {
                    while (true) {
                        try {
                            readStr = reader2.readLine();
                            if (readStr == null) {
                                break;
                            } else if (readStr != null && readStr.length() > 0) {
                                lcmIDStr = readStr;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            reader = reader2;
                            try {
                                e.printStackTrace();
                                if (reader != null) {
                                }
                                return "unknow";
                            } catch (Throwable th2) {
                                th = th2;
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e3) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            reader = reader2;
                            if (reader != null) {
                            }
                            throw th;
                        }
                    }
                }
                Log.i(TAG, " lcmIDStr:" + lcmIDStr + " readStr:" + readStr);
                if (lcmIDStr != null && lcmIDStr.length() > 0) {
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e4) {
                        }
                    }
                    return lcmIDStr;
                } else if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (IOException e6) {
                e = e6;
                e.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e7) {
                    }
                }
                return "unknow";
            }
        }
        return "unknow";
    }

    private static boolean checkFileIsValid(String wallpaperPath) {
        if (wallpaperPath == null) {
            return false;
        }
        File wallpaperFile = new File(wallpaperPath);
        if (wallpaperFile == null || !wallpaperFile.exists()) {
            return false;
        }
        return true;
    }

    private static InputStream generateCustomDefaultLockScreen(String wallpaperPath) {
        File wallpaperFile = new File(wallpaperPath);
        if (wallpaperFile != null && wallpaperFile.exists()) {
            try {
                return new FileInputStream(wallpaperFile);
            } catch (IOException e) {
                Log.e(TAG, "generate default wallpaper catch exception " + e.toString());
            }
        }
        return null;
    }

    private static boolean checkIsNeedConfigSpecialWallpaper(String projectName) {
        if (projectName != null) {
            try {
                if (!projectName.equals(Events.DEFAULT_SORT_ORDER)) {
                    for (String temp : SPECIAL_PROJ_NAME) {
                        if (temp.equalsIgnoreCase(projectName)) {
                            Log.i(TAG, Events.DEFAULT_SORT_ORDER + projectName + ", will config special default lockscreen.");
                            return true;
                        }
                    }
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static InputStream handleCreateDefaultWallpaper(String lcmID) {
        if (lcmID == null || lcmID.equals(Events.DEFAULT_SORT_ORDER)) {
            return null;
        }
        String wallpaperFilePath = null;
        if (lcmID.equals("00") || lcmID.equals("01") || lcmID.equals("02")) {
            wallpaperFilePath = "system/etc/custom/panel_color/default_lockscreen_" + lcmID + ".png";
        } else {
            Log.w(TAG, "Current id " + lcmID + " is not support.");
        }
        Log.i(TAG, "config default lockscreen " + wallpaperFilePath + " for lcmId " + lcmID);
        if (checkFileIsValid(wallpaperFilePath)) {
            InputStream wallpaperStrem = generateCustomDefaultLockScreen(wallpaperFilePath);
            if (wallpaperStrem != null) {
                Log.i(TAG, "Custom device lockscreen ");
                return wallpaperStrem;
            }
        }
        return null;
    }
}
