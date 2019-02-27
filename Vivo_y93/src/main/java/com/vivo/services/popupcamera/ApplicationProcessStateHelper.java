package com.vivo.services.popupcamera;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.os.RemoteException;
import java.util.List;

public class ApplicationProcessStateHelper {
    private static String SYSTEM_GALLERY_ACTIVITY = "com.android.gallery3d.app.Gallery";

    public static class ApplicationProcessStatus {
        public boolean isAppForeground = false;
        public boolean isInGalleryActivity = false;
    }

    public static ApplicationProcessStatus isApplicationProcessForeground(String packageName) {
        ApplicationProcessStatus ret = new ApplicationProcessStatus();
        try {
            List<RunningTaskInfo> tasks = ActivityManagerNative.getDefault().getTasks(1, 0);
            if (!(tasks == null || (tasks.isEmpty() ^ 1) == 0 || tasks.get(0) == null)) {
                ComponentName topActivity = ((RunningTaskInfo) tasks.get(0)).topActivity;
                if (topActivity.getPackageName().equals(packageName)) {
                    ret.isAppForeground = true;
                }
                if (topActivity.getClassName().equals(SYSTEM_GALLERY_ACTIVITY)) {
                    ret.isInGalleryActivity = true;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
