package com.vivo.common.fingerprinthook;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;

public class FingerprintWakeHookCollectData {
    public static final String ALIPAY_LABEL_ID = "10732";
    public static final String EVENT_ID = "1073";
    public static final String FAIL = "fail";
    public static final String FP_TYPE = "fp_type";
    public static final String FP_TYPE_FPC = "fpc";
    public static final String FP_TYPE_GOODIX = "goodix";
    public static final String IDENTIFY = "identify";
    public static final String KEYGUARD_LABEL_ID = "10731";
    private static final String PROP_FINGER_TYPE = "persist.sys.fptype";
    public static final String SPEED = "speed";
    public static final String SUCCESS = "success";
    private static final String TAG = FingerprintWakeHookCollectData.class.getSimpleName();
    private static FingerprintWakeHookCollectData mInstance = null;
    private Context mContext;
    private VivoCollectData mVivoCollectData = new VivoCollectData(this.mContext);

    public FingerprintWakeHookCollectData(Context context) {
        this.mContext = context;
    }

    public static FingerprintWakeHookCollectData getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FingerprintWakeHookCollectData(context);
        }
        return mInstance;
    }

    public void writeData(final String eventId, final String label, final HashMap<String, String> params) {
        if (params == null) {
            Log.w(TAG, "writeData(): params is null");
            return;
        }
        try {
            new Thread(new Runnable() {
                public void run() {
                    HashMap<String, String> mParams = new HashMap();
                    mParams.put(FingerprintWakeHookCollectData.SPEED, (String) params.get(FingerprintWakeHookCollectData.SPEED));
                    if (FingerprintWakeHookCollectData.this.mVivoCollectData.getControlInfo(eventId)) {
                        FingerprintWakeHookCollectData.this.mVivoCollectData.writeData(eventId, label, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, mParams);
                    } else {
                        Log.w(FingerprintWakeHookCollectData.TAG, "writeData(): getControlInfo is false");
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "writeData()： write data failed.");
        }
    }

    private String getFpType() {
        int fp_type = SystemProperties.getInt(PROP_FINGER_TYPE, -1);
        Log.i(TAG, "getFpType(): fp_type: " + fp_type);
        if (1 == fp_type) {
            return FP_TYPE_FPC;
        }
        if (2 == fp_type) {
            return FP_TYPE_GOODIX;
        }
        return "no_type";
    }
}
