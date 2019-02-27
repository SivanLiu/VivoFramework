package com.vivo.common.autobrightness;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.vivo.common.VivoCollectData;
import com.vivo.services.DeviceParaProvideService;
import java.util.ArrayList;
import java.util.HashMap;

public class CollectUseData {
    private static final String COLOR_BLACK = "bl";
    private static final String COLOR_GRAY = "gr";
    private static final String COLOR_WITHE = "wh";
    public static final String EVENTID_AUTOBRIGHTNESS = "1005";
    private static final String KEY_COLOR = "cl";
    public static final String LABEL_AUTO_BRIGHT_APPLY_HISTORY = "10057";
    public static final String LABEL_USE_DURATION = "10051";
    public static final String LABLE_CAMERA_USE_TIME = "10053";
    public static final String LABLE_MEMORY_PARAMTER = "10052";
    public static final String LABLE_USER_BRIGHTNESS = "10054";
    public static final String LABLE_USER_MODIFY_INFO = "10055";
    public static final String LABLE_USER_MODIFY_INFO_ARG = "10056";
    private static final String LCM_COLOR_PATH = "/sys/lcm/lcm_color";
    private static final String LCM_ID_PATH = "/sys/lcm/lcm_id";
    private static final int LIGHT_LUX_COUNT_TIMEOUT = 2000;
    private static final int MSG_BIRGHTNESS_MANUAL_HAPPENED = 2;
    private static final int MSG_BIRGHTNESS_MANUAL_TIMEUP = 3;
    private static final int MSG_GET_LCM_COLOR = 4;
    private static final int MSG_WRITE_TO_DB = 1;
    private static final String TAG = "CollectUseData";
    private static CollectUseData mInstance;
    private Context mContext;
    private ArrayList<DataParameter> mDataParameterList = new ArrayList(2);
    private CollectHandler mHandler;
    public String mLcmColor = COLOR_WITHE;
    private ArrayList<Float> mLightBuffer = new ArrayList(10);
    private Sensor mLightSensor = null;
    private SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (CollectUseData.this.mLock) {
                CollectUseData.this.mLightBuffer.add(Float.valueOf(event.values[0]));
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Object mLock = new Object();
    private int mManualBrightness = -1;
    private SensorManager mSensorManager;
    private VivoCollectData mVivoCollectData;

    private class CollectHandler extends Handler {
        public CollectHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Object -get5;
            switch (msg.what) {
                case 1:
                    if (AblConfig.isDebug()) {
                        Log.d(CollectUseData.TAG, "handleMessage MSG_WRITE_TO_DB");
                    }
                    -get5 = CollectUseData.this.mLock;
                    synchronized (-get5) {
                        while (CollectUseData.this.mDataParameterList.size() >= 1) {
                            CollectUseData.this.writeDatabaseUnlock((DataParameter) CollectUseData.this.mDataParameterList.get(0));
                            CollectUseData.this.mDataParameterList.remove(0);
                        }
                        break;
                    }
                case 2:
                    if (AblConfig.isDebug()) {
                        Log.d(CollectUseData.TAG, "handleMessage MSG_BIRGHTNESS_MANUAL_HAPPENED");
                    }
                    -get5 = CollectUseData.this.mLock;
                    synchronized (-get5) {
                        CollectUseData.this.mLightBuffer.clear();
                        CollectUseData.this.mSensorManager.registerListener(CollectUseData.this.mListener, CollectUseData.this.mLightSensor, 3, CollectUseData.this.mHandler);
                        break;
                    }
                case 3:
                    if (AblConfig.isDebug()) {
                        Log.d(CollectUseData.TAG, "handleMessage MSG_BIRGHTNESS_MANUAL_TIMEUP");
                    }
                    float luxAverage = -1.0f;
                    CollectUseData.this.mSensorManager.unregisterListener(CollectUseData.this.mListener);
                    synchronized (CollectUseData.this.mLock) {
                        if (CollectUseData.this.mLightBuffer.size() > 0) {
                            int size = CollectUseData.this.mLightBuffer.size();
                            float sum = 0.0f;
                            for (int i = 0; i < size; i++) {
                                sum += ((Float) CollectUseData.this.mLightBuffer.get(i)).floatValue();
                            }
                            luxAverage = sum / ((float) size);
                        }
                    }
                    HashMap<String, String> map = new HashMap(3);
                    map.put("setbright", String.valueOf(CollectUseData.this.mManualBrightness));
                    map.put("autobright", String.valueOf(-1));
                    map.put("autoinfo", "AutoBrightnesOff-Invalid:lux=" + ((int) luxAverage) + ";");
                    CollectUseData.this.sendDataParameter(new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, CollectUseData.LABLE_USER_BRIGHTNESS, System.currentTimeMillis(), -1, 0, 1, map));
                    return;
                case 4:
                    CollectUseData.this.mLcmColor = CollectUseData.this.getLcmColor();
                    return;
                default:
                    return;
            }
        }
    }

    public CollectUseData(Context context, Looper looper) {
        this.mContext = context;
        this.mVivoCollectData = new VivoCollectData(this.mContext);
        this.mHandler = new CollectHandler(looper);
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (this.mSensorManager != null) {
            this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        }
        this.mHandler.sendEmptyMessage(4);
    }

    public static CollectUseData getInstance(Context context, Looper looper) {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new CollectUseData(context, looper);
        return mInstance;
    }

    private void writeDatabaseUnlock(DataParameter data) {
        this.mVivoCollectData.writeData(data.eventId, data.label, data.startTime, data.endTime, data.duration, data.useNum, data.params);
        if (AblConfig.isDebug()) {
            Log.d(TAG, "writeDatabaseUnlock: " + data.toString());
        }
    }

    public void sendDataParameter(DataParameter data) {
        if (data == null) {
            Slog.d(TAG, "seedDataParameter data is null.");
            return;
        }
        if (AblConfig.isDebug()) {
            if (data.params == null) {
                Slog.d(TAG, "sendDataParameter params is null!");
            } else {
                Slog.d(TAG, "sendDataParameter params: " + data.params.toString());
            }
        }
        synchronized (this.mLock) {
            this.mDataParameterList.add(new DataParameter(data));
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void notifyBrightnessChanged(int brightness) {
        this.mManualBrightness = brightness;
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessageDelayed(2, 2000);
        this.mHandler.sendEmptyMessageDelayed(3, 4000);
    }

    private String getLcmColor() {
        String model = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, "unkown").toLowerCase();
        String data = DeviceParaProvideService.readKernelData(LCM_ID_PATH);
        if (model == null || data == null) {
            return COLOR_WITHE;
        }
        String ret = COLOR_WITHE;
        if (model.startsWith("pd1610")) {
            if (data.trim().equals("22")) {
                ret = COLOR_BLACK;
            }
        } else if (model.startsWith("pd1624") || model.equals("pd1616") || model.equals("vtd1703f_ex")) {
            if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("01")) {
                ret = COLOR_BLACK;
            }
        } else if (model.startsWith("pd1635") || model.equals("pd1616b") || model.startsWith("pd1619")) {
            if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("02")) {
                ret = COLOR_BLACK;
            }
        } else if (DeviceParaProvideService.readKernelData(LCM_COLOR_PATH).trim().equals("01")) {
            ret = COLOR_BLACK;
        }
        return ret;
    }
}
