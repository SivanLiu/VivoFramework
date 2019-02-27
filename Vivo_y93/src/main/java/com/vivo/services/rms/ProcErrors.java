package com.vivo.services.rms;

import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Slog;
import com.vivo.statistics.sdk.GatherManager;

public class ProcErrors {
    private static final int EX_TYPE_ANR = 2;
    private static final int EX_TYPE_CRASH = 3;
    private static final int EX_TYPE_DEXCRASH = 4;
    private static ProcErrors INSTANCE = null;
    private static final long INTERVAL_TIME = 5000;
    private static final String MC_ACTION = "com.vivo.perfdiagnosis.MSG_CENTER";
    private static final int MSG_ANR = 0;
    private static final int MSG_CRASH = 1;
    private static final String TAG = "AppErrors";
    private Handler mHandler;
    private long mLastAnrTime = 0;
    private long mLastCrashTime = 0;

    final class MyHandler extends Handler {
        MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 0:
                    String str = (String) msg.obj;
                    if (msg.arg1 != 0) {
                        z = false;
                    }
                    broadcast(2, str, z);
                    return;
                case 1:
                    broadcast(msg.arg1 == 0 ? 3 : 4, (String) msg.obj, true);
                    return;
                default:
                    return;
            }
        }

        private void broadcast(int excepType, String procName, boolean background) {
            Intent intent = new Intent(ProcErrors.MC_ACTION);
            intent.setPackage("com.vivo.abe");
            intent.putExtra("excepType", excepType);
            intent.putExtra("procName", procName);
            intent.putExtra("background", background);
            RMInjectorImpl.self().getContext().sendBroadcast(intent);
        }
    }

    public static final ProcErrors getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProcErrors();
        }
        return INSTANCE;
    }

    private ProcErrors() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
    }

    public void onProcAnr(String pkgName, String procName, String activity, String reason) {
        long time = System.currentTimeMillis();
        try {
            PackageInfo pkgInfo = RMInjectorImpl.self().getContext().getPackageManager().getPackageInfo(pkgName, 0);
            GatherManager.getInstance().gather("anr", new Object[]{pkgName, procName, pkgInfo.versionName, Integer.valueOf(pkgInfo.versionCode), activity, Integer.valueOf(getAnrReason(reason)), Long.valueOf(time)});
        } catch (Exception e) {
        }
        if (time - this.mLastAnrTime > INTERVAL_TIME) {
            this.mLastAnrTime = time;
            Message msg = Message.obtain();
            msg.what = 0;
            msg.arg1 = TextUtils.isEmpty(activity) ? 1 : 0;
            msg.obj = procName;
            this.mHandler.sendMessage(msg);
        }
    }

    public void onProcCrash(String pkgName, String procName, CrashInfo crashInfo) {
        int i = 1;
        long time = System.currentTimeMillis();
        if (time - this.mLastCrashTime > INTERVAL_TIME) {
            this.mLastCrashTime = time;
            Message msg = Message.obtain();
            msg.what = 1;
            if (!isDexCrash(pkgName, crashInfo)) {
                i = 0;
            }
            msg.arg1 = i;
            msg.obj = pkgName;
            this.mHandler.sendMessage(msg);
        }
    }

    private boolean isDexCrash(String packageName, CrashInfo crashInfo) {
        if (!(packageName == null || crashInfo == null)) {
            try {
                if (crashInfo.exceptionMessage != null && crashInfo.exceptionMessage.contains("Didn't find class")) {
                    Slog.w(TAG, packageName + " crash because " + crashInfo.exceptionMessage);
                    return true;
                } else if (crashInfo.exceptionClassName != null && (crashInfo.exceptionClassName.contains("Native crash") || crashInfo.exceptionClassName.contains("ClassNotFoundException"))) {
                    Slog.w(TAG, packageName + " crash because " + crashInfo.exceptionClassName);
                    return true;
                } else if (crashInfo.throwClassName != null && crashInfo.throwClassName.contains("BaseDexClassLoader")) {
                    return true;
                } else {
                    if (crashInfo.throwMethodName != null && crashInfo.throwMethodName.contains("findClass")) {
                        Slog.w(TAG, packageName + " crash because " + crashInfo.throwMethodName);
                        return true;
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception in NeedDexopt" + e);
            }
        }
        return false;
    }

    private int getAnrReason(String reason) {
        if (reason == null) {
            return 0;
        }
        if (reason.contains("Input dispatching timed out")) {
            return 1;
        }
        if (reason.contains("Broadcast of Intent")) {
            return 2;
        }
        if (reason.contains("Executing service")) {
            return 3;
        }
        if (reason.contains("ContentProvider not responding")) {
            return 4;
        }
        return 0;
    }
}
