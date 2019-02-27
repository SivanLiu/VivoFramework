package com.vivo.common;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Base64;
import android.util.Log;
import com.vivo.content.Weather.HourData;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class VivoCloudData {
    private static final String TAG = ToolUtils.makeTag("CloudData");
    private static boolean isDebug = true;
    private static VivoCloudData sInstance;
    private final Uri URI_MTMP = Uri.parse("content://com.bbk.iqoo.logsystem.info/mtmp");
    private Context mContext;
    private final String mDefLocation = "-1&&-1&&-1&&-1&&-1&&-1&&-1";
    private final int mVersion = 1;

    private VivoCloudData(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public String getDefLocation() {
        return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
    }

    public static VivoCloudData getInstance(Context context) {
        if (sInstance == null) {
            synchronized (VivoCloudData.class) {
                if (sInstance == null) {
                    sInstance = new VivoCloudData(context);
                }
            }
        }
        return sInstance;
    }

    public int getVersion() {
        return 1;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean status) {
        isDebug = status;
    }

    public String getLocation() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(this.URI_MTMP, new String[]{"m_value"}, "m_key=?", new String[]{"btloc"}, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            String string = cursor.getString(0);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Exception e) {
            Log.w(TAG, "Get loc failed.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String initOneData(String eventId, JSONObject dt) {
        if (ToolUtils.isEmpty(eventId) || ToolUtils.isEmpty(dt)) {
            if (isDebug) {
                Log.d(TAG, "eventId = " + eventId + ", dt = " + dt);
            } else {
                Log.e(TAG, "Waring, invalid id or dt!!!");
            }
            return null;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("eventId", eventId);
            data.put("dt", dt);
            return data.toString();
        } catch (Throwable e) {
            Log.w(TAG, e);
            return null;
        }
    }

    public static String initOneData(String eventId, JSONObject dt, String fullhash, String logpath) {
        if (ToolUtils.isEmpty(eventId) || ToolUtils.isEmpty(dt) || ToolUtils.isEmpty(fullhash) || ToolUtils.isEmpty(logpath)) {
            if (isDebug) {
                Log.d(TAG, "eventId = " + eventId + ", dt = " + dt + ", fullhash = " + fullhash + ", logpath = " + logpath);
            } else {
                Log.e(TAG, "Waring, invalid id/dt/hash/path!!!");
            }
            return null;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("eventId", eventId);
            data.put("dt", dt);
            data.put("fullhash", fullhash);
            data.put("logpath", logpath);
            return data.toString();
        } catch (Throwable e) {
            Log.w(TAG, e);
            return null;
        }
    }

    public void sendData(int moduleId, ArrayList<String> data) {
        if (moduleId <= 0 || ToolUtils.isEmpty((List) data)) {
            if (isDebug) {
                Log.d(TAG, "moduleId = " + moduleId + ", data = " + data);
            } else {
                Log.e(TAG, "Waring, invalid info!!!");
            }
            return;
        }
        Intent it = new Intent("com.vivo.intent.action.CLOUD_DIAGNOSIS");
        it.putExtra("attr", 1);
        it.putExtra("module", moduleId);
        it.putStringArrayListExtra("data", data);
        it.setPackage("com.bbk.iqoo.logsystem");
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL, "com.bbk.iqoo.logsystem.permission.SIGN_OR_SYSTEM");
    }

    public void sendData(String tag, long time) {
        if (ToolUtils.isEmpty(tag) || time <= 0) {
            if (isDebug) {
                Log.d(TAG, "tag = " + tag + ", time = " + time);
            } else {
                Log.e(TAG, "Dropbox, invalid info!!!");
            }
            return;
        }
        Intent it = new Intent("com.vivo.intent.action.CLOUD_DIAGNOSIS");
        it.putExtra("attr", 2);
        it.putExtra("tag", tag);
        it.putExtra(HourData.TIME, time);
        it.setPackage("com.bbk.iqoo.logsystem");
        this.mContext.sendBroadcastAsUser(it, UserHandle.ALL, "com.bbk.iqoo.logsystem.permission.SIGN_OR_SYSTEM");
    }

    public String doRsaEncrypt(String source) {
        try {
            byte[] plain = source.getBytes(Charset.forName("UTF-8"));
            Class<?> KeyStore = Class.forName("android.security.KeyStore");
            Method getInstance = KeyStore.getMethod("getInstance", new Class[0]);
            if (getInstance == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            Object mKeyStore = getInstance.invoke(null, new Object[0]);
            if (mKeyStore == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            Method vivoRSAEncrypt = KeyStore.getMethod("vivoRSAEncrypt", new Class[]{byte[].class});
            if (vivoRSAEncrypt == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            byte[] encrypted = (byte[]) vivoRSAEncrypt.invoke(mKeyStore, new Object[]{plain});
            if (encrypted != null) {
                return new String(Base64.encode(encrypted, 0)).replace("\n", "");
            }
            return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
        } catch (Exception e) {
            Log.w(TAG, "Encrypt failed.", e);
        }
    }

    public String doAesEncrypt(String source) {
        try {
            byte[] plain = source.getBytes(Charset.forName("UTF-8"));
            Class<?> KeyStore = Class.forName("android.security.KeyStore");
            Method getInstance = KeyStore.getMethod("getInstance", new Class[0]);
            if (getInstance == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            Object mKeyStore = getInstance.invoke(null, new Object[0]);
            if (mKeyStore == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            Method vivoAESEncrypt = KeyStore.getMethod("vivoAESEncrypt", new Class[]{byte[].class});
            if (vivoAESEncrypt == null) {
                return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
            }
            byte[] encrypted = (byte[]) vivoAESEncrypt.invoke(mKeyStore, new Object[]{plain});
            if (encrypted != null) {
                return new String(Base64.encode(encrypted, 0)).replace("\n", "");
            }
            return "-1&&-1&&-1&&-1&&-1&&-1&&-1";
        } catch (Exception e) {
            Log.w(TAG, "Encrypt failed.", e);
        }
    }
}
