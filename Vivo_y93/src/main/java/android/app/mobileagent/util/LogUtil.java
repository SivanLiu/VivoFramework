package android.app.mobileagent.util;

import android.hardware.SystemSensorManager;
import android.os.SystemProperties;
import android.util.Log;

public class LogUtil {
    private static boolean sLogD;
    private static boolean sLogE;
    private static boolean sLogI;
    private static boolean sLogW;

    static {
        init();
    }

    public static void d(String s, String s2) {
        if (sLogD) {
            Log.d(s, s2);
        }
    }

    public static void e(String s, String s2) {
        if (sLogE) {
            Log.e(s, getTrace() + "::" + s2);
        }
    }

    private static String getTrace() {
        Thread currentThread = Thread.currentThread();
        if (currentThread == null) {
            return "";
        }
        StackTraceElement[] stackTrace = currentThread.getStackTrace();
        if (stackTrace == null) {
            return "";
        }
        if (stackTrace.length - 1 < 4) {
            return "";
        }
        String className = stackTrace[4].getClassName();
        int lastIndex = className.lastIndexOf(".");
        String substring = className;
        if (lastIndex != -1) {
            substring = className.substring(lastIndex + 1);
        }
        int lineNumber = stackTrace[4].getLineNumber();
        String methodName = stackTrace[4].getMethodName();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(substring);
        sb.append(":");
        sb.append(methodName);
        sb.append("():");
        sb.append(lineNumber);
        sb.append("line]");
        return sb.toString();
    }

    public static void i(String s, String s2) {
        if (sLogI) {
            Log.i(s, s2);
        }
    }

    private static void init() {
        boolean b = false;
        if ("yes".equals(SystemProperties.get(SystemSensorManager.KEY_VIVO_LOG_CTRL, "")) || "yes".equals(SystemProperties.get("persist.sys.ailog.ctrl", ""))) {
            b = true;
        }
        sLogD = b;
        sLogI = b;
        sLogW = true;
        sLogE = true;
    }

    public static void w(String s, String s2) {
        if (sLogW) {
            Log.w(s, getTrace() + "::" + s2);
        }
    }
}
