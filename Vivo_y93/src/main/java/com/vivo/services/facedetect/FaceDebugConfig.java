package com.vivo.services.facedetect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemProperties;
import com.vivo.services.epm.config.Switch;

public class FaceDebugConfig {
    private static final String BBKLOG_ACTION = "android.vivo.bbklog.action.CHANGED";
    private static final String BBKLOG_STATUS = "adblog_status";
    public static boolean DEBUG;
    private static boolean DEBUG_BBK_LOG = "yes".equals(SystemProperties.get("persist.sys.log.ctrl", "no"));
    public static final boolean DEBUG_TIME = ("0".equals(SystemProperties.get("persist.facedetect.debug.level", "0")) ^ 1);

    static {
        boolean z;
        if (DEBUG_TIME) {
            z = true;
        } else {
            z = DEBUG_BBK_LOG;
        }
        DEBUG = z;
    }

    public static void listenDebugProp(Context ctx, Handler handler) {
        IntentFilter bbklogFilter = new IntentFilter();
        bbklogFilter.addAction(BBKLOG_ACTION);
        ctx.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                FaceDebugConfig.DEBUG_BBK_LOG = Switch.SWITCH_ATTR_VALUE_ON.equals(intent.getStringExtra(FaceDebugConfig.BBKLOG_STATUS));
                FaceDebugConfig.DEBUG = !FaceDebugConfig.DEBUG_TIME ? FaceDebugConfig.DEBUG_BBK_LOG : true;
            }
        }, bbklogFilter, null, handler);
    }
}
