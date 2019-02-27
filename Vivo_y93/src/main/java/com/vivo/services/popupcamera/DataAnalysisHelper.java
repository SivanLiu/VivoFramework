package com.vivo.services.popupcamera;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import com.vivo.common.VivoCollectData;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class DataAnalysisHelper {
    private static final String COLLECT_DATA_EVENTID = "1032";
    private static final String COLLECT_DATA_FOR_MISC_AND_MEMS = "10322";
    private static final int GATHER_THRES = 20;
    private static final long HALF_A_DAY_INTERVAL = 43200000;
    private static final String KEY_DROP_CLOSE_COUNT = "dcc";
    private static final String KEY_POP_JAM_COUNT = "pojc";
    private static final String KEY_POP_OK_COUNT = "pooc";
    private static final String KEY_PRESS_COUNT = "psc";
    private static final String KEY_PUSH_JAM_COUNT = "pujc";
    private static final String KEY_PUSH_OK_COUNT = "puoc";
    private static final String KEY_VIB_WORKED_COUNT = "vwc";
    private static final String POPUP_CAMERA_VIBRATOR_USED_STATS = "pcvb_ustats";
    private static final int POPUP_CAMERA_VIB_EVENT = 7;
    private static final String TAG = "PopupCameraManagerService";
    private static DataAnalysisHelper mInstance = null;
    private static Object mLock = new Object();
    private boolean isBootCompleted = false;
    private Context mContext;
    private int mDropCloseCount = 0;
    private Handler mHandler;
    private int mPopupJammedCount = 0;
    private int mPopupOkCount = 0;
    private int mPressedCount = 0;
    private int mPushJammedCount = 0;
    private int mPushOkCount = 0;
    private long mStartCollectTime = -1;
    private int mStatusTotalCount = 0;
    private HashMap<String, Integer> mVibUsedStatsMap;
    private int mVibWorkCount = 0;
    private VivoCollectData mVivoCollectData = null;

    private class PopupCameraVibStatusSnap {
        private static final String TAG_EVENT = "eve";
        private static final String TAG_REASON = "rea";
        private static final String TAG_STATISTICS = "sts";
        private static final String TAG_TYPE = "typ";
        public int event;
        public int reason;
        public String statistics;
        public int type;

        public PopupCameraVibStatusSnap(int event, int type, int reason, String statistics) {
            this.event = event;
            this.type = type;
            this.reason = reason;
            this.statistics = statistics;
        }

        public String toString() {
            JSONObject obj = new JSONObject();
            try {
                obj.put(TAG_EVENT, this.event);
                obj.put(TAG_TYPE, this.type);
                obj.put(TAG_REASON, this.reason);
                obj.put(TAG_STATISTICS, this.statistics);
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(DataAnalysisHelper.TAG, "PopupCameraVibStatusSnap toString FAILED");
                return null;
            }
        }
    }

    public DataAnalysisHelper(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mVivoCollectData = new VivoCollectData(this.mContext);
        this.mVibUsedStatsMap = new HashMap();
        this.mVibUsedStatsMap.put(KEY_PUSH_OK_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_POP_OK_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_PUSH_JAM_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_POP_JAM_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_PRESS_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_DROP_CLOSE_COUNT, Integer.valueOf(0));
        this.mVibUsedStatsMap.put(KEY_VIB_WORKED_COUNT, Integer.valueOf(0));
        this.mStartCollectTime = SystemClock.elapsedRealtime();
    }

    public static DataAnalysisHelper getInstance(Context context, Handler handler) {
        DataAnalysisHelper dataAnalysisHelper;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataAnalysisHelper(context, handler);
            }
            dataAnalysisHelper = mInstance;
        }
        return dataAnalysisHelper;
    }

    public void gatherCounts(int type) {
        switch (type) {
            case 1:
                this.mPushOkCount++;
                this.mVibWorkCount++;
                break;
            case 2:
                this.mPopupOkCount++;
                this.mVibWorkCount++;
                break;
            case 3:
                this.mPushJammedCount++;
                this.mVibWorkCount += 3;
                break;
            case 4:
                this.mPopupJammedCount++;
                this.mVibWorkCount += 3;
                break;
            case 5:
                this.mPressedCount++;
                break;
            case 18:
                this.mDropCloseCount++;
                break;
        }
        this.mStatusTotalCount++;
        long current = SystemClock.elapsedRealtime();
        if (this.mStatusTotalCount >= 20 || current - this.mStartCollectTime > HALF_A_DAY_INTERVAL) {
            try {
                updateCounts();
                JSONObject jobj = new JSONObject(this.mVibUsedStatsMap);
                String jsonStr = jobj != null ? jobj.toString() : "null";
                if (jsonStr != null && ("".equals(jsonStr) ^ 1) != 0) {
                    System.putString(this.mContext.getContentResolver(), POPUP_CAMERA_VIBRATOR_USED_STATS, jsonStr);
                    Message msg = this.mHandler.obtainMessage(17);
                    msg.obj = new PopupCameraVibStatusSnap(7, 0, 0, jsonStr);
                    this.mHandler.sendMessageDelayed(msg, 2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void storeRecord(String info) {
        if (info != null) {
            if (this.mVivoCollectData.getControlInfo(COLLECT_DATA_EVENTID)) {
                HashMap<String, String> params = new HashMap();
                params.put("info", info);
                Log.d(TAG, "vbsr info:" + info);
                this.mVivoCollectData.writeData(COLLECT_DATA_EVENTID, COLLECT_DATA_FOR_MISC_AND_MEMS, System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
            } else {
                Log.e(TAG, "sr not collect.");
            }
        }
    }

    private void updateCounts() {
        int tmpPushOkCount = ((Integer) this.mVibUsedStatsMap.get(KEY_PUSH_OK_COUNT)).intValue();
        int tmpPopupOkCount = ((Integer) this.mVibUsedStatsMap.get(KEY_POP_OK_COUNT)).intValue();
        int tmpPushJammedCount = ((Integer) this.mVibUsedStatsMap.get(KEY_PUSH_JAM_COUNT)).intValue();
        int tmpPopupJammedCount = ((Integer) this.mVibUsedStatsMap.get(KEY_POP_JAM_COUNT)).intValue();
        int tmpPressCount = ((Integer) this.mVibUsedStatsMap.get(KEY_PRESS_COUNT)).intValue();
        int tmpDropCloseCount = ((Integer) this.mVibUsedStatsMap.get(KEY_DROP_CLOSE_COUNT)).intValue();
        int tmpVibWorkCount = ((Integer) this.mVibUsedStatsMap.get(KEY_VIB_WORKED_COUNT)).intValue();
        if (this.mPushOkCount != 0) {
            this.mVibUsedStatsMap.put(KEY_PUSH_OK_COUNT, Integer.valueOf(tmpPushOkCount + this.mPushOkCount));
        }
        if (this.mPopupOkCount != 0) {
            this.mVibUsedStatsMap.put(KEY_POP_OK_COUNT, Integer.valueOf(tmpPopupOkCount + this.mPopupOkCount));
        }
        if (this.mPushJammedCount != 0) {
            this.mVibUsedStatsMap.put(KEY_PUSH_JAM_COUNT, Integer.valueOf(tmpPushJammedCount + this.mPushJammedCount));
        }
        if (this.mPopupJammedCount != 0) {
            this.mVibUsedStatsMap.put(KEY_POP_JAM_COUNT, Integer.valueOf(tmpPopupJammedCount + this.mPopupJammedCount));
        }
        if (this.mPressedCount != 0) {
            this.mVibUsedStatsMap.put(KEY_PRESS_COUNT, Integer.valueOf(tmpPressCount + this.mPressedCount));
        }
        if (this.mDropCloseCount != 0) {
            this.mVibUsedStatsMap.put(KEY_DROP_CLOSE_COUNT, Integer.valueOf(tmpDropCloseCount + this.mDropCloseCount));
        }
        if (this.mVibWorkCount != 0) {
            this.mVibUsedStatsMap.put(KEY_VIB_WORKED_COUNT, Integer.valueOf(tmpVibWorkCount + this.mVibWorkCount));
        }
    }

    private HashMap<String, Integer> jsonStringToMap(String jsonString) {
        HashMap<String, Integer> map = new HashMap();
        try {
            JSONObject jObject = new JSONObject(jsonString);
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                map.put(key, Integer.valueOf(jObject.getInt(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void notifyShutdownBroadcast() {
        if (this.isBootCompleted) {
            updateCounts();
            JSONObject obj = new JSONObject(this.mVibUsedStatsMap);
            String jsonStr = obj != null ? obj.toString() : "null";
            Log.d(TAG, "shutdown jsonStr = " + jsonStr);
            if (jsonStr != null && ("".equals(jsonStr) ^ 1) != 0) {
                System.putString(this.mContext.getContentResolver(), POPUP_CAMERA_VIBRATOR_USED_STATS, jsonStr);
            }
        }
    }

    public void notifyBootCompletedBroadcast() {
        String jsonStr = System.getStringForUser(this.mContext.getContentResolver(), POPUP_CAMERA_VIBRATOR_USED_STATS, -2);
        if (jsonStr == null || ("".equals(jsonStr) ^ 1) == 0) {
            Log.e(TAG, "bootcompleted jsonStr is null or empty, mvbs = " + (this.mVibUsedStatsMap != null ? this.mVibUsedStatsMap.toString() : "null"));
        } else {
            Log.d(TAG, "bootcompleted jsonStr = " + jsonStr);
            this.mVibUsedStatsMap.putAll(jsonStringToMap(jsonStr));
            Log.d(TAG, "bootcompleted mvbs = " + (this.mVibUsedStatsMap != null ? this.mVibUsedStatsMap.toString() : "null"));
        }
        this.isBootCompleted = true;
    }

    public void handleDataCollect(Object obj) {
        storeRecord(((PopupCameraVibStatusSnap) obj).toString());
        this.mPushOkCount = 0;
        this.mPopupOkCount = 0;
        this.mPushJammedCount = 0;
        this.mPopupJammedCount = 0;
        this.mPressedCount = 0;
        this.mDropCloseCount = 0;
        this.mStatusTotalCount = 0;
        this.mVibWorkCount = 0;
        this.mStartCollectTime = SystemClock.elapsedRealtime();
    }
}
