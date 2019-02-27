package android.app.anr;

import android.app.anr.IANRManager.Stub;
import android.content.Context;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;

public class ANRManager {
    public static final String ANR_SERVICE = "vivo_anrmanager";
    private static final boolean FORCE_ENABLE = SystemProperties.getBoolean("persist.anr.enableAll", false);
    private static final String[] MONITOR_PACKAGE = new String[]{"com.android.systemui", "com.bbk.launcher2", "com.vivo.upslide", "com.vivo.gallery", "com.android.bbkmusic", "com.vivo.daemonService", "com.android.camera"};
    private static final String TAG = "ANRManager";
    private static ANRManager sManager;
    private String mPackageName;
    private int mPid = Process.myPid();
    private IANRManager mService;

    public static ANRManager getDefault(Context context) {
        if (sManager == null) {
            sManager = new ANRManager(context);
        }
        return sManager;
    }

    public static boolean isEnableMonitor(String name) {
        if (FORCE_ENABLE) {
            return true;
        }
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        for (String pkg : MONITOR_PACKAGE) {
            if (pkg.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private ANRManager(Context context) {
        this.mPackageName = context.getPackageName();
        if (this.mPackageName == null) {
            this.mPackageName = "unknown";
        }
        this.mService = Stub.asInterface(ServiceManager.getService(ANR_SERVICE));
        Slog.e(TAG, "mPackageName = " + this.mPackageName + ", Get anr service = " + this.mService);
    }

    public void appLooperBlocked(String msg, int totaltime, String binderCall) {
        if (this.mService != null) {
            try {
                this.mService.appLooperBlocked(this.mPackageName, this.mPid, msg, totaltime, binderCall);
            } catch (Exception e) {
                Slog.e(TAG, "Exception logging message, msg = " + msg, e);
            }
        }
    }

    public void appBinderTimeout(String service, int totaltime) {
        if (this.mService != null) {
            try {
                this.mService.appBinderTimeout(this.mPackageName, this.mPid, service, totaltime);
            } catch (Exception e) {
                Slog.e(TAG, "Exception logging binder timeout, service = " + service, e);
            }
        }
    }

    public String getAllMessages(int pid) {
        if (this.mService == null) {
            return "";
        }
        try {
            return this.mService.getAllMessages(pid);
        } catch (Exception e) {
            Slog.e(TAG, "Exception getting message");
            return "";
        }
    }

    public String[] getAllBinders(int pid) {
        if (this.mService == null) {
            return new String[0];
        }
        try {
            return this.mService.getAllBinders(pid);
        } catch (Exception e) {
            Slog.e(TAG, "Exception getting binders");
            return new String[0];
        }
    }

    public void clearAllMessages(int pid) {
        if (this.mService != null) {
            try {
                this.mService.clearAllMessages(pid);
            } catch (Exception e) {
                Slog.e(TAG, "Exception clearing messages");
            }
        }
    }
}
