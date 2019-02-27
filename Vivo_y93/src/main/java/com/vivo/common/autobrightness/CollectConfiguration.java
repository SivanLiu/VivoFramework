package com.vivo.common.autobrightness;

import android.os.SystemProperties;
import android.util.Slog;
import com.vivo.common.provider.Calendar.Events;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class CollectConfiguration {
    private static final Pattern PA_REX_ABLCONFIG = Pattern.compile(REX_ABLCONFIG);
    private static final Pattern PA_REX_PARAMETER = Pattern.compile(REX_PARAMETER);
    private static final Pattern PA_REX_PUBLIC_STATIC = Pattern.compile(REX_PUBLIC_STATIC);
    private static final Pattern PA_REX_VOID = Pattern.compile(REX_VOID);
    private static final String REX_ABLCONFIG = ".*\\.AblConfig\\.";
    private static final String REX_PARAMETER = "\\(\\w\\w*";
    private static final String REX_PUBLIC_STATIC = "^public static ";
    private static final String REX_VOID = " void ";
    private static final String TAG = "CollectConfiguration";
    private static ArrayList<Config> mConfigurations = new ArrayList();
    private static CollectConfiguration mInstance = null;
    private static Object mLock = new Object();

    private static class Config {
        public String key;
        public String value;

        public Config(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String toString() {
            return "[" + this.key + "]:[" + this.value + "];\n";
        }
    }

    public static void log(String msg) {
        Slog.d(TAG, msg);
    }

    private CollectConfiguration() {
        updateStaticConfiguration();
    }

    public static CollectConfiguration getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new CollectConfiguration();
        return mInstance;
    }

    public void addConfiguration(String key, String value) {
        synchronized (mLock) {
            if (key == null) {
                Slog.w(TAG, "AutoBrightnessWARNING:addConfiguration: key is null!");
                return;
            }
            mConfigurations.add(new Config(key, value));
        }
    }

    private void updateStaticConfiguration() {
        Method[] methods = AblConfig.class.getMethods();
        addConfiguration(AblConfig.PROP_PRODUCT_MODEL, SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown"));
        addConfiguration(AblConfig.PROP_PRODUCT_VERSION, SystemProperties.get(AblConfig.PROP_PRODUCT_VERSION, "unkown"));
        for (Method m : methods) {
            invokeMethod(m);
        }
    }

    private void invokeMethod(Method m) {
        if (m != null) {
            String mName = m.toString();
            boolean ps = PA_REX_PUBLIC_STATIC.matcher(mName).find();
            boolean nonV = PA_REX_VOID.matcher(mName).find() ^ 1;
            boolean abl = PA_REX_ABLCONFIG.matcher(mName).find();
            boolean noPa = PA_REX_PARAMETER.matcher(mName).find() ^ 1;
            String mInfo = "[ps:" + ps + " nonV:" + nonV + " abl:" + abl + " noPa:" + noPa + "] ";
            if (ps && nonV && abl && noPa) {
                log("invoked:" + mInfo + mName);
                try {
                    addConfiguration(cutName(mName), Events.DEFAULT_SORT_ORDER + m.invoke(null, null));
                } catch (Exception e) {
                }
            } else {
                log("not invoked:" + mInfo + mName);
            }
        }
    }

    private static String cutName(String name) {
        return name.replaceAll(REX_ABLCONFIG, Events.DEFAULT_SORT_ORDER).replaceAll("^is", Events.DEFAULT_SORT_ORDER).replaceAll("^get", Events.DEFAULT_SORT_ORDER).replaceAll("\\W", Events.DEFAULT_SORT_ORDER);
    }

    public String toString() {
        String ret = "{\n";
        synchronized (mLock) {
            if (mConfigurations.size() > 0) {
                ret = ret + "mConfigurations size=" + mConfigurations.size() + "\n";
                for (int i = 0; i < mConfigurations.size(); i++) {
                    ret = ret + ((Config) mConfigurations.get(i)).toString();
                }
                ret = ret + "}\n";
            } else {
                ret = ret + "mConfigurations IS EMPTY!";
            }
        }
        return ret;
    }

    public void dump(PrintWriter pw) {
        pw.write(toString());
    }
}
