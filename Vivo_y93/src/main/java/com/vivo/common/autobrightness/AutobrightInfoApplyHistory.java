package com.vivo.common.autobrightness;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Slog;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AutobrightInfoApplyHistory {
    private static final int MAX_INFO_SIZE = 20;
    private static final String TAG = "AutobrightInfoApplyHistory";
    private CollectUseData mCollectUseData = null;
    private Handler mHandler;
    private ArrayList<Info> mInfoList = new ArrayList(20);
    private Object mLock = new Object();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            synchronized (AutobrightInfoApplyHistory.this.mLock) {
                if (AutobrightInfoApplyHistory.this.mInfoList.size() > 0) {
                    if (AblConfig.isDebug()) {
                        Slog.e(AutobrightInfoApplyHistory.TAG, "mRunnable size=" + AutobrightInfoApplyHistory.this.mInfoList.size());
                    }
                    AutobrightInfoApplyHistory.this.infoListToJsonLocked();
                    AutobrightInfoApplyHistory.this.mInfoList.clear();
                }
            }
        }
    };

    private class Info {
        public static final int ARG_LINE = 7;
        private static final String KEY_ARG = "arg";
        public int[] arg = new int[7];
        public String mForegroundPkg = "unknown";
        final /* synthetic */ AutobrightInfoApplyHistory this$0;
        public long timeStamp;

        public Info(AutobrightInfoApplyHistory this$0, long timeStamp, AutobrightInfo abInfo) {
            int i = 1;
            this.this$0 = this$0;
            this.timeStamp = timeStamp;
            this.arg[0] = abInfo.mLightLux;
            this.arg[1] = abInfo.mPrivBrightness;
            this.arg[2] = abInfo.mBrightness;
            this.arg[3] = abInfo.mPhoneStatus;
            this.arg[4] = abInfo.mStepCount;
            int[] iArr = this.arg;
            if (!abInfo.mWifiStatus) {
                i = 0;
            }
            iArr[5] = i;
            this.arg[6] = abInfo.mPowerPercent;
            this.mForegroundPkg = abInfo.mForegroundPkg;
        }

        public JSONObject toJsonObject() {
            JSONObject obj = new JSONObject();
            try {
                JSONArray phonearg = new JSONArray();
                phonearg.put(this.timeStamp);
                phonearg.put(this.mForegroundPkg);
                for (int put : this.arg) {
                    phonearg.put(put);
                }
                obj.put("arg", phonearg);
                return obj;
            } catch (JSONException e) {
                Slog.e(AutobrightInfoApplyHistory.TAG, "info toString e:", e);
                return null;
            }
        }
    }

    public AutobrightInfoApplyHistory(Context context, Looper looper) {
        this.mHandler = new Handler(looper);
        this.mCollectUseData = CollectUseData.getInstance(context, looper);
    }

    private void infoListToJsonLocked() {
        JSONArray jArr = new JSONArray();
        for (Info info : this.mInfoList) {
            JSONObject obj = info.toJsonObject();
            if (obj != null) {
                jArr.put(obj);
            }
        }
        HashMap<String, String> map = new HashMap(3);
        map.put("list", jArr.toString());
        long now = System.currentTimeMillis();
        this.mCollectUseData.sendDataParameter(new DataParameter(CollectUseData.EVENTID_AUTOBRIGHTNESS, CollectUseData.LABEL_AUTO_BRIGHT_APPLY_HISTORY, ((Info) this.mInfoList.get(0)).timeStamp * 1000, now, now - (((Info) this.mInfoList.get(0)).timeStamp * 1000), 1, map));
    }

    public void onNewInfoApplied(AutobrightInfo abInfo) {
        Info info = new Info(this, System.currentTimeMillis() / 1000, abInfo);
        synchronized (this.mLock) {
            if (AblConfig.isDebug()) {
                Slog.d(TAG, "onNewInfoApplied got new info.");
            }
            if (this.mInfoList.size() >= 20) {
                this.mInfoList.remove(0);
            }
            this.mInfoList.add(info);
        }
    }

    public void saveApplyToDb() {
        this.mHandler.post(this.mRunnable);
    }
}
