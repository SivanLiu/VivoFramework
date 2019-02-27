package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemProperties;
import android.security.KeyStore;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import org.json.JSONObject;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class CollectonUtils {
    public static boolean DBG = false;
    protected static final String LOG_TAG = "CollectionUtils";
    protected static final String SPRE_FILE_NAME = "vivo_data_collection";
    protected static final String VERSION_API_VivoCloudData_v4 = "V4v";
    protected static final String VERSION_API_v3 = "V3";
    protected static final String VERSION_API_v4 = "V4";
    protected static String VERSION_IMS_COLLECT = "persist.vivo.ims.collect.flag";
    protected static final int VERSION_TELECOM_MODULE_v4 = 600;
    protected static String devProp = "persist.vivo.dev.network.flag";
    public static boolean isDevMode;
    protected static String mOperatorApiVersion = VERSION_API_v3;
    protected static String testProp = VivoNetLowlatency.SYSTEMPROPERTIES_PRINT_LOG_FLAG;
    protected String DEBUG_FILENAME = "collection";
    protected long LOG_UPLOAD_INTERVAL = SystemProperties.getLong("persist.sys.network.log.upload", 18000000);
    protected String SysVersion = SystemProperties.get("ro.vivo.product.version", "-1");
    protected String Version = SystemProperties.get("ro.build.version.release", "-1");
    private LocationManager locationManager;
    protected Context mContext;
    private VivoMassExceptionAPI mOperatorAPI;
    protected String proVersion = SystemProperties.get("ro.product.model.bbk", "-1");

    static {
        boolean z;
        boolean z2 = true;
        if (SystemProperties.getInt(testProp, 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        DBG = z;
        if (SystemProperties.getInt(devProp, 0) != 1) {
            z2 = false;
        }
        isDevMode = z2;
    }

    public CollectonUtils(Context context) {
        this.mContext = context;
    }

    protected boolean isWriteToDataBase(int type, long count, int interval, CollectionBean item, Queue<CollectionBean> queue) {
        try {
            logd("isWriteToDataBase-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.isWriteToDataBaseAPI(type, count, interval, item, queue);
            }
            logd("isWriteToDataBase:mOperatorAPI is null pls check it");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String buildContent(Queue<CollectionBean> queu) {
        try {
            logd("buildContent-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.buildContentAPI(queu);
            }
            logd("buildContent-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected ArrayList<String> buildArrayListContent(Queue<CollectionBean> queu) {
        try {
            logd("buildArrayListContent-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.buildArrayListContentAPI(queu);
            }
            logd("buildArrayListContent-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean writeFile(String content, String name) {
        try {
            logd("writeFile-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.writeFileAPI(content, name);
            }
            logd("writeFile-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String getExceptionType() {
        try {
            logd("getExceptionTypeAPI-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getExceptionTypeAPI();
            }
            logd("getExceptionTypeAPI-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getExceptionSubType() {
        try {
            logd("getExceptionSubTypeAPI-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getExceptionSubTypeAPI();
            }
            logd("getExceptionSubTypeAPI-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getCurrentModuleId() {
        try {
            logd("writeFile-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getCurrentModuleIdAPI();
            }
            logd("getCurrentModuleEventId-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getCurrentModuleEventId(int event) {
        try {
            logd("getCurrentModuleEventId-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getCurrentModuleSubEventIdAPI(event);
            }
            logd("getCurrentModuleEventId-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected void uploadLog(int moduleId, String param1, String param2, String content, String eventId, JSONObject dt, String fullhash, String logpath) {
        try {
            logd("uploadLogAPI-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                this.mOperatorAPI.uploadLogAPI(moduleId, param1, param2, content, eventId, dt, fullhash, logpath);
            } else {
                logd("uploadLog-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void uploadLog(String eventId, String eventType, String content) {
        Intent it = new Intent(VivoMassExceptionAPI.ACTION_LOG_UPLOAD);
        it.setPackage("com.vivo.networkimprove");
        it.putExtra("eventId", eventId);
        it.putExtra("eventType", eventType);
        this.mContext.sendBroadcast(it);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0043 A:{SYNTHETIC, Splitter: B:24:0x0043} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0037 A:{SYNTHETIC, Splitter: B:17:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x004c A:{SYNTHETIC, Splitter: B:29:0x004c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readToString(String fileName) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        String encoding = "utf-8";
        File file = new File(fileName);
        byte[] filecontent = new byte[Long.valueOf(file.length()).intValue()];
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(file);
            try {
                in2.read(filecontent);
                in2.close();
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Exception e3) {
                    }
                }
                in = in2;
            } catch (FileNotFoundException e4) {
                e = e4;
                in = in2;
                e.printStackTrace();
                if (in != null) {
                }
                return new String(filecontent, encoding);
            } catch (IOException e5) {
                e2 = e5;
                in = in2;
                try {
                    e2.printStackTrace();
                    if (in != null) {
                    }
                    return new String(filecontent, encoding);
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e6) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                if (in != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e8) {
                }
            }
            return new String(filecontent, encoding);
        } catch (IOException e9) {
            e2 = e9;
            e2.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e10) {
                }
            }
            return new String(filecontent, encoding);
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e11) {
            Rlog.e(LOG_TAG, "The OS does not support " + encoding);
            e11.printStackTrace();
            return null;
        }
    }

    private void deleteOldOrBigFile(File[] files) {
        if (files != null) {
            int count = files.length;
            boolean isDeleted = false;
            long[] createTime = new long[count];
            for (int i = 0; i < count; i++) {
                if (files[i].length() > 5242880) {
                    isDeleted = true;
                    logd("deleted file for too big :" + files[i].getName());
                    files[i].delete();
                } else {
                    try {
                        createTime[i] = Long.parseLong(files[i].getName().substring(0, files[i].getName().indexOf(".")));
                    } catch (Exception e) {
                        e.printStackTrace();
                        createTime[i] = System.currentTimeMillis();
                    }
                }
            }
            if (!isDeleted && count >= 5) {
                Arrays.sort(createTime);
                File file = new File(files[0].getParent() + File.separator + createTime[0] + ".log");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    protected String getCurrrentSignal() {
        try {
            logd("getCurrrentSignal-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getCurrrentSignalAPI();
            }
            logd("getCurrrentSignal-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getCurrrentRsrp() {
        try {
            logd("getCurrrentRsrp-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getCurrrentRsrpAPI();
            }
            logd("getCurrrentRsrp-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String getCurrrentRsrq() {
        try {
            logd("getCurrrentRsrq-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getCurrrentRsrqAPI();
            }
            logd("getCurrrentRsrq-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected void initRelativeParam(Context context) {
        try {
            logd("init-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                this.mOperatorAPI.initAPI(context);
            } else {
                logd("init-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void disposeRelativeParam(Context context) {
        try {
            logd("disposeAPI-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                this.mOperatorAPI.disposeAPI(context);
            } else {
                logd("disposeAPI-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setSignalStrengthsChanged(int rsrp, int rsrq, int asu) {
        try {
            if (this.mOperatorAPI != null) {
                this.mOperatorAPI.setSignalStrengthsChangedAPI(rsrp, rsrq, asu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        try {
            logd("getCurrrentRsrq-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.getVersionAPI();
            }
            logd("getCurrrentRsrq-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return mOperatorApiVersion;
        } catch (Exception e) {
            e.printStackTrace();
            return mOperatorApiVersion;
        }
    }

    public boolean isDebug() {
        try {
            logd("isDebug-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                return this.mOperatorAPI.isDebugAPI();
            }
            logd("isDebug-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setDebug(boolean debug) {
        try {
            logd("setDebug-" + mOperatorApiVersion);
            if (this.mOperatorAPI != null) {
                this.mOperatorAPI.setDebugAPI(debug);
            } else {
                logd("setDebug-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getLocation() {
        String temp = "-1";
        try {
            LocationManager locationManager = (LocationManager) this.mContext.getSystemService("location");
            logd("getLocation temp 1");
            Location location = locationManager.getLastKnownLocation("gps");
            if (location == null) {
                location = locationManager.getLastKnownLocation("network");
            }
            logd("ss1803 getLocation temp 2");
            if (location == null) {
                temp = "-1";
                logd("getLocation location is null");
            } else {
                temp = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
                logd("getLocation str   temp=" + temp);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        logd("ss1803 getLocation temp=" + temp);
        String temp_after = changeString(temp);
        logd("ss getLocation temp=" + temp + ",temp_after=" + temp_after + ",temp_after_length=" + temp_after.length());
        return temp_after;
    }

    protected String changeString(String databef) {
        String result = "-1";
        try {
            byte[] plainBuffer = databef.getBytes("utf-8");
            byte[] cipherBuffer = new byte[]{(byte) 0};
            KeyStore keystore = KeyStore.getInstance();
            if (keystore != null) {
                Method methodRSAEncrypt = KeyStore.class.getMethod("vivoRSAEncrypt", new Class[]{byte[].class});
                if (methodRSAEncrypt != null) {
                    cipherBuffer = (byte[]) methodRSAEncrypt.invoke(keystore, new Object[]{plainBuffer});
                }
            }
            return new String(Base64.encode(cipherBuffer, 0));
        } catch (Exception e) {
            System.out.println(e.toString());
            return result;
        }
    }

    protected boolean writeToDatabase(Queue<CollectionBean> queue, String name) {
        logd("writeToDatabase-" + mOperatorApiVersion + " name " + name);
        if (this.mOperatorAPI != null) {
            return this.mOperatorAPI.writeToDatabaseAPI(queue, name);
        }
        logd("writeToDatabase-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
        return false;
    }

    protected void logd(String s) {
        if (DBG) {
            Rlog.d(LOG_TAG, s);
        }
    }

    protected void loge(String s) {
        if (DBG) {
            Rlog.e(LOG_TAG, s);
        }
    }

    protected void setOperatorAPI(Context context, VivoMassExceptionAPI operator) {
        if (operator != null) {
            this.mOperatorAPI = operator;
            initRelativeParam(context);
            this.mOperatorAPI.setDebugAPI(DBG);
            return;
        }
        loge("setOperatorAPI-" + mOperatorApiVersion + ":mOperatorAPI is null pls check it");
    }

    protected VivoMassExceptionAPI getOperatorAPI() {
        return this.mOperatorAPI;
    }

    protected void setOperatorApiVersion(String version) {
        String tmpVersion = SystemProperties.get(VERSION_IMS_COLLECT, "");
        if (TextUtils.isEmpty(tmpVersion)) {
            mOperatorApiVersion = version;
        } else {
            mOperatorApiVersion = tmpVersion;
        }
    }

    protected String getOperatorApiVersion() {
        return mOperatorApiVersion;
    }
}
