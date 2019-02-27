package com.qti.location.sdk.collection;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.vivo.common.VivoCollectData;
import com.vivo.common.VivoCollectFile;
import java.util.ArrayList;
import java.util.Collections;

public class IZatTrialCollect {
    private static boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String KEY_LOST = "l:";
    private static final String KEY_NAVIGATING = "n:";
    private static final String KEY_RESUME = "r:";
    private static final String KEY_START = "s:";
    private static final String KEY_STOP = "p:";
    private static final int MSG_LOST_STAR = 4;
    private static final int MSG_REPORT_LOCATION = 3;
    private static final int MSG_RESUME_STAR = 5;
    private static final int MSG_START_NAVIGATING = 1;
    private static final int MSG_STOP_NAVIGATING = 2;
    private static final String SEP = ",";
    private static final String TAG = "IZatTrialCollect";
    private int MAX_SAVE_RECORD_LINE_COUNT = 1800;
    private int mAverageCn0 = -1;
    private long mCn0Time = -1;
    private EZ mEZ = new EZ();
    private MyHandler mHandler;
    private boolean mHasLost = false;
    private long mLostTime = -1;
    private String mPackages = "";
    private ArrayList<String> mRecordList = new ArrayList(this.MAX_SAVE_RECORD_LINE_COUNT);
    private long mResumeTime = -1;
    private long mStartNavigatingTime = -1;
    private long mStopNavigatingTime = -1;
    private VivoCollectData mVivoCollectData;

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    String content = msg.obj;
                    IZatTrialCollect.this.mRecordList.add(content);
                    if (IZatTrialCollect.DEBUG) {
                        Log.d(IZatTrialCollect.TAG, "handleMessage content=\"" + content + "\"");
                        break;
                    }
                    break;
            }
            if (msg.what == 2 || IZatTrialCollect.this.mRecordList.size() >= IZatTrialCollect.this.MAX_SAVE_RECORD_LINE_COUNT) {
                IZatTrialCollect.this.writeContent();
            }
        }
    }

    public IZatTrialCollect(Context context, Looper looper) {
        this.mHandler = new MyHandler(looper);
        this.mVivoCollectData = new VivoCollectData(context);
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    private void writeContent() {
        if (this.mVivoCollectData.getControlInfo("1404")) {
            String allRecordContent = "";
            for (String rec : this.mRecordList) {
                allRecordContent = allRecordContent + rec + "\r\n";
            }
            String info = this.mEZ.encrypt(allRecordContent);
            if (DEBUG) {
                Log.d(TAG, "writeContent size=" + this.mRecordList.size() + " len1=" + allRecordContent.length() + " len2=" + info.length());
            }
            VivoCollectFile.writeData("1404", "1404_1", info, false, null);
        } else if (DEBUG) {
            Log.d(TAG, "writeContent control=false;");
        }
        this.mRecordList.clear();
    }

    public void onSetRequest(String request, String source) {
        if (source != null) {
            if (source.length() < 15) {
                if (DEBUG) {
                    Log.d(TAG, "onSetRequest source:" + source + " length not enough");
                }
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "onSetRequest request:" + request + " source:" + source);
            }
            this.mPackages = "";
            String[] arr = source.replace("WorkSource{", "").replace(",}", "").replace("}", "").split(SEP);
            if (arr != null && arr.length > 0) {
                for (String up : arr) {
                    if (up.contains(" ")) {
                        String[] p = up.split(" ");
                        if (p.length > 1 && ("".equals(p[1]) ^ 1) != 0) {
                            this.mPackages += p[1] + SEP;
                        }
                    }
                }
            }
        }
    }

    private long toSecond(long time) {
        return time / 1000;
    }

    public void onStartNavigating(long time) {
        this.mStartNavigatingTime = time;
        this.mHandler.obtainMessage(1, KEY_START + toSecond(time) + SEP + this.mPackages).sendToTarget();
    }

    public void onStopNavigating(long time) {
        if (this.mHasLost) {
            this.mHandler.obtainMessage(5, KEY_RESUME + toSecond(this.mLostTime) + SEP + toSecond(time) + SEP + toSecond(time - this.mLostTime)).sendToTarget();
        }
        this.mHasLost = false;
        this.mStopNavigatingTime = time;
        this.mHandler.obtainMessage(2, KEY_STOP + toSecond(this.mStartNavigatingTime) + SEP + toSecond(this.mStopNavigatingTime) + SEP + toSecond(this.mStopNavigatingTime - this.mStartNavigatingTime)).sendToTarget();
    }

    public void onReportLocation(Location loc) {
        if (this.mHasLost) {
            this.mHasLost = false;
            String info = KEY_RESUME;
            long now = System.currentTimeMillis();
            this.mHandler.obtainMessage(5, info + toSecond(this.mLostTime) + SEP + toSecond(now) + SEP + toSecond(now - this.mLostTime)).sendToTarget();
            return;
        }
        this.mHandler.obtainMessage(3, KEY_NAVIGATING + loc.getLatitude() + SEP + loc.getLongitude() + SEP + loc.getAccuracy() + SEP + loc.getSpeed() + SEP + toSecond(loc.getTime()) + SEP + this.mAverageCn0 + SEP + toSecond(this.mCn0Time)).sendToTarget();
    }

    private void calcAverageCn0(int count, int[] mSvidWithFlags, float[] cn0) {
        ArrayList<Float> usedSnrsList = new ArrayList();
        int usedSnrsCount = 0;
        float sum = 0.0f;
        try {
            usedSnrsList.clear();
            int i = 0;
            while (i < count) {
                boolean isUsed = false;
                if ((mSvidWithFlags[i] & 4) != 0) {
                    isUsed = true;
                }
                if (isUsed && cn0[i] > 0.0f) {
                    usedSnrsList.add(Float.valueOf(cn0[i]));
                    usedSnrsCount++;
                }
                i++;
            }
            Collections.sort(usedSnrsList);
            int tempCount = usedSnrsCount >= 4 ? 4 : usedSnrsCount;
            for (i = 1; i <= tempCount; i++) {
                sum += ((Float) usedSnrsList.get(usedSnrsCount - i)).floatValue();
            }
            if (tempCount == 0 || sum == 0.0f) {
                this.mAverageCn0 = -1;
            } else {
                this.mAverageCn0 = (int) (sum / ((float) tempCount));
            }
            this.mCn0Time = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpdateSvStatus(int count, int[] svmSvidWithFlagss, float[] cn0) {
        calcAverageCn0(count, svmSvidWithFlagss, cn0);
    }

    public void onLost() {
        this.mHasLost = true;
        this.mLostTime = System.currentTimeMillis();
        this.mHandler.obtainMessage(4, KEY_LOST + toSecond(this.mLostTime)).sendToTarget();
    }
}
