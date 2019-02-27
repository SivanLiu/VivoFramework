package org.ifaa.android.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import vivo.content.res.VivoThemeZipFile;

public class AlipayIFAAManager extends IFAAManagerV3 {
    private static final int ALIPAY_INVOKECMD = 666;
    public static final int CMD_GET_SENSOR_LOCATION = 1;
    public static final int DEFAULT_SENSOR_HEIGHT = 190;
    public static final int DEFAULT_SENSOR_WIDTH = 190;
    public static final int DEFAULT_SENSOR_X = 445;
    public static final int DEFAULT_SENSOR_Y = 1829;
    private static final boolean IS_UDFINGER = SystemProperties.get(PROP_FINGERPRINT_TYPE, VivoThemeZipFile.PKG_VIVO).startsWith("udfp_");
    public static final String KEY_GET_SENSOR_LOCATION = "org.ifaa.ext.key.GET_SENSOR_LOCATION";
    private static final String PROP_FINGERPRINT_TYPE = "persist.sys.fptype";
    private static final String TAG = "AlipayIFAAManager";
    private Context mContext;

    public AlipayIFAAManager(Context context) {
        this.mContext = context;
    }

    public String getExtInfo(int authType, String keyExtInfo) {
        Log.i(TAG, "getExtInfo enter");
        if (!IS_UDFINGER || authType != 1 || !keyExtInfo.equals("org.ifaa.ext.key.GET_SENSOR_LOCATION")) {
            return null;
        }
        JSONObject udfp = new JSONObject();
        try {
            udfp.put("type", 0);
            JSONObject extinfo = new JSONObject();
            extinfo.put("startX", DEFAULT_SENSOR_X);
            extinfo.put("startY", getStartY());
            extinfo.put("width", 190);
            extinfo.put("height", 190);
            extinfo.put("navConflict", IS_UDFINGER);
            udfp.put("fullView", extinfo);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "getExtInfo fail");
        }
        return udfp.toString();
    }

    private int getStartY() {
        if ((Build.DEVICE.contains("1805") | Build.DEVICE.contains("1806")) != 0) {
            return 1918;
        }
        if ((Build.DEVICE.contains("1809") | Build.DEVICE.contains("1804")) != 0) {
            return 1909;
        }
        return DEFAULT_SENSOR_Y;
    }

    public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
    }

    public int getSupportBIOTypes(Context context) {
        Log.i(TAG, "getSupportBIOTypes enter");
        if (IS_UDFINGER) {
            return 17;
        }
        return 1;
    }

    public int startBIOManager(Context context, int authType) {
        Log.i(TAG, "startBIOManager enter");
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setAction("android.settings.SETTINGS");
            intent.addFlags(268435456);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public byte[] processCmdV2(Context context, byte[] param) {
        try {
            IBinder alipayService = ServiceManager.getService("alipay_service");
            if (alipayService != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                byte[] result = null;
                data.writeByteArray(param);
                alipayService.transact(ALIPAY_INVOKECMD, data, reply, 0);
                if (reply.readInt() == 0) {
                    result = reply.createByteArray();
                }
                reply.recycle();
                data.recycle();
                return result;
            }
        } catch (RemoteException ex) {
            Log.i(TAG, "Failed to call alipay service", ex);
        }
        return null;
    }

    public String getDeviceModel() {
        return VivoModelConfig.getDeviceModel();
    }

    public int getVersion() {
        Log.i(TAG, "getVersion enter");
        return 2;
    }
}
