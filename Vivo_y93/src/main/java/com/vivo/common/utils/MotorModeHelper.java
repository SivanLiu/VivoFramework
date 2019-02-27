package com.vivo.common.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.vivo.services.security.client.VivoPermissionManager;
import java.util.ArrayList;
import java.util.HashMap;

public class MotorModeHelper {
    private static final String FLAG_ALL_CLASS = ".*";
    private static final String MOTOR_MODE_ENABLE = "motor_mode_enabled";
    private static final String TAG = "MotorModeHelper";
    public static final int TYPE_NEED_INFO = 0;
    private static MotorModeHelper sInstance = null;
    private static boolean sLogEnable;
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final HandlerThread sWorkerThread = new HandlerThread("motor-mode-loader", 10);
    private Context mContext;
    private String[] mDefaultWhiteListConfig = new String[]{"com.android.incallui/.*", "com.android.systemui/.*", "com.android.dialer/.*", "com.android.server.telecom/.*", "com.vivo.motormode/.*", "com.android.camera/.*", "com.android.contacts/.*", "com.android.mms/.*", "com.android.settings/.*", "com.android.bbksoundrecorder/.*"};
    private HashMap<String, ArrayList<String>> mDefaultWhiteMap = new HashMap();
    private Object mLock = new Object();
    private boolean mMotorModeEnable = false;
    private ContentObserver mMotorModeObserver = new ContentObserver(sWorker) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            MotorModeHelper.this.checkMotorMode(MotorModeHelper.this.mContext);
        }
    };
    private int mType = 0;

    class IllegalConfigException extends RuntimeException {
        public IllegalConfigException(String detailMessage) {
            super(detailMessage);
        }

        public IllegalConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalConfigException(MotorModeHelper this$0, Throwable cause) {
            String str = null;
            MotorModeHelper.this = this$0;
            if (cause != null) {
                str = cause.toString();
            }
            super(str, cause);
        }
    }

    static {
        boolean z;
        sWorkerThread.start();
        if (SystemProperties.get(VivoPermissionManager.KEY_VIVO_LOG_CTRL, "no").equals("yes")) {
            z = true;
        } else {
            z = Build.TYPE.equals("eng");
        }
        sLogEnable = z;
    }

    private void checkMotorMode(Context context) {
        boolean z = false;
        int enable = System.getInt(context.getContentResolver(), MOTOR_MODE_ENABLE, 0);
        Log.i(TAG, "checkMotorMode------enable=" + enable);
        if (enable > 0) {
            z = true;
        }
        this.mMotorModeEnable = z;
    }

    private MotorModeHelper(Context context, int type) {
        if (sLogEnable) {
            Log.d(TAG, "init type = " + type);
        }
        this.mContext = context;
        this.mType = type;
        context.getContentResolver().registerContentObserver(System.getUriFor(MOTOR_MODE_ENABLE), true, this.mMotorModeObserver);
        if (type == 0) {
            loadData(this.mContext);
        }
    }

    public static MotorModeHelper getInstance(Context context, int type) {
        if (sInstance == null) {
            sInstance = new MotorModeHelper(context, type);
        }
        return sInstance;
    }

    private void loadData(Context context) {
        sWorker.post(new Runnable() {
            public void run() {
                MotorModeHelper.this.loadFactoryDefaultConifg();
            }
        });
    }

    public boolean isMotorModeEnable() {
        return this.mMotorModeEnable;
    }

    public boolean filter(String packageName, String className, String callingPackage) {
        long startTime = System.currentTimeMillis();
        if (!isMotorModeEnable()) {
            return true;
        }
        if (packageName == null || className == null) {
            Log.w(TAG, "packageName or className is null");
            return true;
        }
        boolean result;
        if (sLogEnable) {
            Log.d(TAG, "filter : pkgName = " + packageName + "; clsName = " + className + "; callPkg = " + callingPackage);
        }
        synchronized (this.mLock) {
            if (this.mDefaultWhiteMap.containsKey(packageName)) {
                ArrayList<String> clsNames = (ArrayList) this.mDefaultWhiteMap.get(packageName);
                if (clsNames == null) {
                }
                if (clsNames.contains(packageName + FLAG_ALL_CLASS)) {
                    result = true;
                } else if (clsNames.contains(className)) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
            if (!result && sLogEnable) {
                Log.d(TAG, "fun : " + packageName + "/" + className + " is disabled under motor mode");
            }
        }
        return result;
    }

    private void loadFactoryDefaultConifg() {
        this.mDefaultWhiteMap.clear();
        for (String parserWhiteStr : this.mDefaultWhiteListConfig) {
            try {
                parserWhiteStr(parserWhiteStr);
            } catch (IllegalConfigException ex) {
                Log.e(TAG, "loadFactoryDefaultConifg Error : " + ex);
            }
        }
    }

    private void parserWhiteStr(String str) throws IllegalConfigException {
        int sep = str.indexOf(47);
        if (sep < 0 || sep + 1 >= str.length()) {
            throw new IllegalConfigException("config item is : " + str);
        }
        String pkg = str.substring(0, sep);
        String cls = str.substring(sep + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        ArrayList<String> clsNames;
        if (pkg == null || cls == null) {
            throw new IllegalConfigException("config item is : " + str);
        } else if (this.mDefaultWhiteMap.containsKey(pkg)) {
            clsNames = (ArrayList) this.mDefaultWhiteMap.get(pkg);
            if (clsNames == null) {
                clsNames = new ArrayList();
                this.mDefaultWhiteMap.put(pkg, clsNames);
            }
            clsNames.add(cls);
        } else {
            clsNames = new ArrayList();
            clsNames.add(cls);
            this.mDefaultWhiteMap.put(pkg, clsNames);
        }
    }

    public void destory() {
        if (!(this.mMotorModeObserver == null || this.mContext == null)) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mMotorModeObserver);
        }
        synchronized (this.mLock) {
            this.mDefaultWhiteMap.clear();
        }
    }
}
