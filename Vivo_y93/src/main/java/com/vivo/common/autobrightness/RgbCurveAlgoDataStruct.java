package com.vivo.common.autobrightness;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.vivo.common.provider.Weather;
import java.lang.reflect.Array;

public class RgbCurveAlgoDataStruct {
    private static final int ALS_CHANGE_THREASH_HIGH = 9;
    private static final int ALS_CHANGE_THREASH_LOW = 5;
    private static final int ALS_NUM = 25;
    private static final int ALS_TOTAL_NUM = 12;
    private static final int GLIMMER_LIGHT_THREASH = 20;
    private static final double GRAVITE_VALUE = 9.8d;
    private static final int LIGHT_LIGHT_THREASH = 2000;
    private static final int MAX_ALS = 5001;
    private static final int NORMAL_LIGHT_THREASH = 600;
    private static final int NO_LIGHT_THREASH = 0;
    private static final int SHINING_LIGHT_THREASH = 5001;
    private static final int SLOW_NORMAL_LIGHT_THREASH = 300;
    private static final int STEPCOUNT_THREASH = 10;
    private static final String TAG = "RgbCurveAlgoDataStruct";
    private final int MSG_USER_CALC_MOTION = 1;
    private int mAlsCount = -1;
    private int mAlsValueChangeCount = 0;
    private int mChangePhoneStatus;
    private Handler mDataCalacHandler;
    private LuxValue mLuxValue = new LuxValue(this, null);
    private int mMaxAlsValue = -1;
    private int mMeanAlsValue = -1;
    private int mMinAlsValue = -1;
    private PhoneStatus mPhoneStatus = new PhoneStatus(this, null);
    private int mPriveStepCountValue;

    private class DataCalacHandler extends Handler {
        public DataCalacHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.e(RgbCurveAlgoDataStruct.TAG, "handleMessage msg is NULL");
                return;
            }
            switch (msg.what) {
                case 1:
                    RgbCurveAlgoDataStruct.this.calcPhoneMoioneStatus();
                    break;
            }
        }
    }

    public enum LightType {
        NO_LIGHT,
        GLIMMER_LIGHT,
        NORMAL_LIGHT,
        LIGHT_LIGHT,
        SHINING_LIGHT,
        GLIMMER_TO_NO_LIGHT,
        NORMAL_TO_NO_LIGHT,
        LIGHT_TO_NO_LIGHT,
        SHINING_TO_NO_LIGHT,
        NO_TO_GLIMMER_LIGHT,
        NORMAL_TO_GLIMMER_LIGHT,
        LIGHT_TO_GLIMMER_LIGHT,
        SHINING_TO_GLIMMER_LIGHT,
        NO_TO_NORMAL_LIGHT,
        GLIMMER_TO_NORMAL_LIGHT,
        LIGHT_TO_NORMAL_LIGHT,
        SHINING_TO_NORMAL_LIGHT,
        NO_TO_LIGHT_LIGHT,
        GLIMMER_TO_LIGHT_LIGHT,
        NORMAL_TO_LIGHT_LIGHT,
        SHINING_TO_LIGHT_LIGHT,
        NO_TO_SHINING_LIGHT,
        GLIMMER_TO_SHINING_LIGHT,
        NORMAL_TO_SHINING_LIGHT,
        LIGHT_TO_SHINING_LIGHT
    }

    private class LuxValue {
        public int[][] mAlsFeature;
        public boolean mAlsFeatureFullFlag;
        public int mAlsFeatureLocation;
        public boolean mGreaterFifty;
        public int[][] mLux;
        public int mLuxLocation;

        /* synthetic */ LuxValue(RgbCurveAlgoDataStruct this$0, LuxValue -this1) {
            this();
        }

        private LuxValue() {
            this.mLux = (int[][]) Array.newInstance(Integer.TYPE, new int[]{25, 2});
            this.mAlsFeature = (int[][]) Array.newInstance(Integer.TYPE, new int[]{12, 5});
        }
    }

    private class PhoneStatus {
        public int mPhoneMotionStatus;
        public boolean mPhoneStatusFull;
        public int mPhoneStatusLocation;
        public int[][] mPhoneStatusValue;
        public long mStartTime;
        public long mStepCountStartTime;
        public int mStepCountValue;

        /* synthetic */ PhoneStatus(RgbCurveAlgoDataStruct this$0, PhoneStatus -this1) {
            this();
        }

        private PhoneStatus() {
            this.mPhoneStatusValue = (int[][]) Array.newInstance(Integer.TYPE, new int[]{20, 2});
        }
    }

    public RgbCurveAlgoDataStruct(Looper looper) {
        clearParam();
        this.mDataCalacHandler = new DataCalacHandler(looper);
    }

    public void clearParam() {
        this.mLuxValue.mLuxLocation = -1;
        this.mLuxValue.mGreaterFifty = false;
        this.mPhoneStatus.mPhoneStatusFull = false;
        this.mPhoneStatus.mPhoneStatusLocation = -1;
        setParamToZeros(this.mLuxValue.mLux);
        setParamToZeros(this.mLuxValue.mAlsFeature);
        setParamToZeros(this.mPhoneStatus.mPhoneStatusValue);
        this.mPhoneStatus.mStepCountValue = 0;
        this.mPhoneStatus.mPhoneMotionStatus = 0;
        this.mPhoneStatus.mStepCountStartTime = SystemClock.uptimeMillis();
        this.mAlsValueChangeCount = 0;
        this.mLuxValue.mAlsFeatureFullFlag = false;
        this.mLuxValue.mAlsFeatureLocation = -1;
    }

    private void setParamToZeros(int[] param) {
        int m = param.length;
        for (int i = 0; i < m; i++) {
            param[i] = 0;
        }
    }

    private void setParamToZeros(double[] param) {
        int m = param.length;
        for (int i = 0; i < m; i++) {
            param[i] = 0.0d;
        }
    }

    private void setParamToZeros(double[][] param) {
        int m = param[0].length;
        for (double[] dArr : param) {
            for (int j = 0; j < m; j++) {
                dArr[j] = 0.0d;
            }
        }
    }

    private void setParamToZeros(int[][] param) {
        int m = param[0].length;
        for (int[] iArr : param) {
            for (int j = 0; j < m; j++) {
                iArr[j] = 0;
            }
        }
    }

    public int[] judgeAlsChange() {
        int[] temp = new int[2];
        int tempPhoneStatusLocation = this.mPhoneStatus.mPhoneStatusLocation;
        if (this.mPhoneStatus.mPhoneMotionStatus == 1) {
            temp[0] = judgePhoneStatusChange(Weather.WEATHERVERSION_ROM_4_5);
            temp[1] = 1;
            if (tempPhoneStatusLocation != -1) {
                if (this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 1 || this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 2) {
                    temp[1] = 1;
                } else {
                    temp[1] = 3;
                }
            }
        } else if (judgePhoneStatusChange(8000) > 1) {
            temp[1] = 5;
        } else if (tempPhoneStatusLocation != -1) {
            if (this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 1 || this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 2) {
                temp[1] = 0;
            } else if (this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 3) {
                temp[1] = 2;
            } else {
                temp[1] = 4;
            }
        }
        return temp;
    }

    public int getPhoneMontionStatus() {
        return this.mPhoneStatus.mPhoneMotionStatus;
    }

    private int judgePhoneStatusChange(int time) {
        int tempPhoneStatusCount = 0;
        int tempPhoneStatusLocation = this.mPhoneStatus.mPhoneStatusLocation;
        long tempTime = SystemClock.uptimeMillis();
        if (tempPhoneStatusLocation == -1) {
            return -1;
        }
        int tempTotalTime = (int) (tempTime - this.mPhoneStatus.mStartTime);
        int tempPhoneStatus = this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0];
        while (tempTotalTime < time) {
            tempTotalTime += this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][1];
            tempPhoneStatusLocation--;
            if (tempPhoneStatusLocation < 0) {
                if (!this.mPhoneStatus.mPhoneStatusFull) {
                    break;
                }
                tempPhoneStatusLocation = this.mPhoneStatus.mPhoneStatusValue.length - 1;
            }
            Slog.e(TAG, "zyl judgePhoneStatusChange tempPhoneStatus = " + tempPhoneStatus + " mPhoneStatus = " + this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0]);
            if (!((tempPhoneStatus == 1 && this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 2) || ((tempPhoneStatus == 2 && this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0] == 1) || tempPhoneStatus == this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0]))) {
                tempPhoneStatusCount++;
            }
            tempPhoneStatus = this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatusLocation][0];
        }
        return tempPhoneStatusCount;
    }

    private void calcPhoneMoioneStatus() {
        long tempTime = SystemClock.uptimeMillis();
        if (this.mPhoneStatus.mPhoneMotionStatus != 0) {
            if (tempTime - this.mPhoneStatus.mStepCountStartTime >= 3000) {
                if (this.mPhoneStatus.mStepCountValue >= 2) {
                    this.mPhoneStatus.mPhoneMotionStatus = 1;
                } else {
                    this.mPhoneStatus.mPhoneMotionStatus = 0;
                }
                this.mPhoneStatus.mStepCountValue = 0;
                this.mPhoneStatus.mStepCountStartTime = tempTime;
            }
            if (this.mPhoneStatus.mPhoneMotionStatus == 1) {
                this.mDataCalacHandler.removeMessages(1);
                this.mDataCalacHandler.sendEmptyMessageDelayed(1, 4000);
            }
        } else if (this.mPhoneStatus.mStepCountValue >= 10) {
            if (tempTime - this.mPhoneStatus.mStepCountStartTime <= 10000 || tempTime - this.mPhoneStatus.mStepCountStartTime >= 30000) {
                this.mPhoneStatus.mPhoneMotionStatus = 1;
            } else {
                this.mPhoneStatus.mPhoneMotionStatus = 0;
            }
            this.mPhoneStatus.mStepCountValue = 0;
            this.mPhoneStatus.mStepCountStartTime = tempTime;
        }
    }

    public void pushStepCountData(int value) {
        calcPhoneMoioneStatus();
        Slog.e(TAG, "pushStepCountData mPriveStepCountValue = " + this.mPriveStepCountValue + " mStepCountValue = " + this.mPhoneStatus.mStepCountValue + " value = " + value);
        this.mPhoneStatus.mStepCountValue = (this.mPhoneStatus.mStepCountValue + value) - this.mPriveStepCountValue;
        this.mPriveStepCountValue = value;
    }

    public void pushLuxData(int value) {
        int tempPhoneStatus = this.mPhoneStatus.mPhoneStatusLocation;
        this.mLuxValue.mLuxLocation++;
        if (this.mLuxValue.mLuxLocation >= this.mLuxValue.mLux.length) {
            this.mLuxValue.mLuxLocation = 0;
            calcAlsChangeCount(this.mLuxValue.mLux.length);
            evaluateAls();
        }
        this.mLuxValue.mLux[this.mLuxValue.mLuxLocation][0] = value;
        if (tempPhoneStatus != -1) {
            this.mLuxValue.mLux[this.mLuxValue.mLuxLocation][1] = this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatus][0];
        } else {
            this.mLuxValue.mLux[this.mLuxValue.mLuxLocation][1] = -1;
        }
    }

    public int[] popStepCount() {
        temp = new int[3];
        long tempTime = SystemClock.uptimeMillis();
        temp[0] = this.mPriveStepCountValue;
        temp[1] = this.mPhoneStatus.mStepCountValue;
        temp[2] = ((int) (tempTime - this.mPhoneStatus.mStepCountStartTime)) / Weather.WEATHERVERSION_ROM_2_0;
        return temp;
    }

    public int popAlsValueChangeCount() {
        return this.mAlsValueChangeCount;
    }

    public int popPhoneMotionStatus() {
        return this.mPhoneStatus.mPhoneMotionStatus;
    }

    public void pushPhoneStatus(int status) {
        int tempLocation = this.mPhoneStatus.mPhoneStatusLocation;
        if (tempLocation == -1 || status != this.mPhoneStatus.mPhoneStatusValue[tempLocation][0]) {
            long tempTime = SystemClock.uptimeMillis();
            this.mPhoneStatus.mPhoneStatusLocation++;
            if (this.mPhoneStatus.mPhoneStatusLocation >= this.mPhoneStatus.mPhoneStatusValue.length) {
                this.mPhoneStatus.mPhoneStatusFull = true;
                this.mPhoneStatus.mPhoneStatusLocation = 0;
            }
            this.mPhoneStatus.mPhoneStatusValue[this.mPhoneStatus.mPhoneStatusLocation][1] = (int) (tempTime - this.mPhoneStatus.mStartTime);
            this.mPhoneStatus.mPhoneStatusValue[this.mPhoneStatus.mPhoneStatusLocation][0] = status;
            this.mPhoneStatus.mStartTime = tempTime;
        }
    }

    public int[] popPhoneStatus() {
        int[] temp = new int[]{-1, -1};
        int tempPhoneStatus = this.mPhoneStatus.mPhoneStatusLocation;
        if (tempPhoneStatus == -1) {
            return temp;
        }
        temp[1] = this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatus][0];
        tempPhoneStatus--;
        if (tempPhoneStatus < 0 && this.mPhoneStatus.mPhoneStatusFull) {
            temp[0] = this.mPhoneStatus.mPhoneStatusValue[this.mPhoneStatus.mPhoneStatusValue.length - 1][0];
        } else if (tempPhoneStatus < 0) {
            temp[0] = -1;
        } else {
            temp[0] = this.mPhoneStatus.mPhoneStatusValue[tempPhoneStatus][0];
        }
        return temp;
    }

    private void evaluateAls() {
        int tempLocation = this.mLuxValue.mLuxLocation;
        int n = this.mLuxValue.mLux.length;
        int phoneStatusCount = 0;
        int minAlsValue = 5001;
        int maxAlsValue = -1;
        int alsCount = 0;
        int sumAlsValue = 0;
        if (tempLocation != -1) {
            int i = tempLocation;
            int privePhoneStatus = this.mLuxValue.mLux[tempLocation][1];
            for (int j = 0; j < n; j++) {
                sumAlsValue += this.mLuxValue.mLux[i][0];
                alsCount++;
                if (this.mLuxValue.mLux[i][0] < minAlsValue) {
                    minAlsValue = this.mLuxValue.mLux[i][0];
                }
                if (this.mLuxValue.mLux[i][0] > maxAlsValue) {
                    maxAlsValue = this.mLuxValue.mLux[i][0];
                }
                if (!(privePhoneStatus == this.mLuxValue.mLux[i][1] || this.mLuxValue.mLux[i][1] == -1 || privePhoneStatus == -1 || this.mLuxValue.mLux[i][1] * privePhoneStatus == 2)) {
                    phoneStatusCount++;
                }
                i--;
                if (i >= 0 || !this.mLuxValue.mGreaterFifty) {
                    if (i < 0) {
                        break;
                    }
                }
                i = n - 1;
                if (i == tempLocation) {
                    break;
                }
            }
            int meanAlsValue = sumAlsValue / alsCount;
            i = this.mLuxValue.mAlsFeatureLocation + 1;
            if (i >= 12) {
                i = 0;
                this.mLuxValue.mAlsFeatureFullFlag = true;
            }
            this.mLuxValue.mAlsFeature[i][0] = meanAlsValue;
            this.mLuxValue.mAlsFeature[i][1] = maxAlsValue;
            this.mLuxValue.mAlsFeature[i][2] = minAlsValue;
            this.mLuxValue.mAlsFeature[i][3] = phoneStatusCount;
            this.mLuxValue.mAlsFeature[i][4] = alsCount;
            this.mLuxValue.mAlsFeatureLocation = i;
        }
    }

    private boolean evaluateLightCondition() {
        int tempLocation = this.mLuxValue.mAlsFeatureLocation;
        int n = this.mLuxValue.mAlsFeature.length;
        int minAlsValue = 5001;
        int maxAlsValue = -1;
        int alsCount = 0;
        int sumAlsCount = 0;
        int sumAlsValue = 0;
        if (tempLocation == -1) {
            return false;
        }
        int i = tempLocation;
        for (int j = 0; j < n; j++) {
            sumAlsValue += this.mLuxValue.mAlsFeature[i][0];
            alsCount++;
            sumAlsCount += this.mLuxValue.mAlsFeature[i][4];
            if (this.mLuxValue.mAlsFeature[i][2] < minAlsValue) {
                minAlsValue = this.mLuxValue.mAlsFeature[i][2];
            }
            if (this.mLuxValue.mAlsFeature[i][1] > maxAlsValue) {
                maxAlsValue = this.mLuxValue.mAlsFeature[i][1];
            }
            i--;
            if (i >= 0 || !this.mLuxValue.mAlsFeatureFullFlag) {
                if (i < 0) {
                    break;
                }
            }
            i = n - 1;
            if (i == tempLocation) {
                break;
            }
        }
        int meanAlsValue = sumAlsValue / alsCount;
        this.mMaxAlsValue = maxAlsValue;
        this.mMinAlsValue = minAlsValue;
        this.mMeanAlsValue = meanAlsValue;
        this.mAlsCount = sumAlsCount;
        return true;
    }

    public int[] evaluateChangeDownAls() {
        int[] iArr = new int[6];
        iArr = new int[]{-1, -1, -1, -1, -1, -1};
        int tempLocation = this.mLuxValue.mLuxLocation;
        int n = this.mLuxValue.mLux.length;
        int phoneStatusCount = 0;
        int count = 0;
        int sum = 0;
        int alsCount = 0;
        boolean downFlag = false;
        boolean upFlag = false;
        int statuscount = 0;
        if (tempLocation == -1) {
            return iArr;
        }
        boolean alsValueFlag = evaluateLightCondition();
        int i = tempLocation;
        int privePhoneStatus = this.mLuxValue.mLux[tempLocation][1];
        int priveAlsValue = this.mLuxValue.mLux[tempLocation][0];
        if (!alsValueFlag) {
            this.mMaxAlsValue = -1;
            this.mMinAlsValue = 5001;
            this.mAlsCount = 0;
        }
        for (int j = 0; j < n; j++) {
            int threshold;
            sum += this.mLuxValue.mLux[i][0];
            alsCount++;
            if (priveAlsValue <= 300) {
                threshold = 20;
            } else if (priveAlsValue <= 30) {
                threshold = 6;
            } else if (priveAlsValue <= 15) {
                threshold = 4;
            } else if (priveAlsValue <= 6) {
                threshold = 3;
            } else {
                threshold = 100;
            }
            int diffvalue = priveAlsValue - this.mLuxValue.mLux[i][0];
            if (this.mLuxValue.mLux[i][0] > this.mMaxAlsValue) {
                this.mMaxAlsValue = this.mLuxValue.mLux[i][0];
            }
            if (this.mLuxValue.mLux[i][0] < this.mMinAlsValue) {
                this.mMinAlsValue = this.mLuxValue.mLux[i][0];
            }
            if (diffvalue < (-threshold) && (downFlag ^ 1) != 0) {
                downFlag = true;
                upFlag = false;
                count++;
            } else if (diffvalue > threshold && (upFlag ^ 1) != 0) {
                downFlag = false;
                upFlag = true;
                count++;
            }
            if (!(privePhoneStatus == this.mLuxValue.mLux[i][1] || this.mLuxValue.mLux[i][1] == -1 || privePhoneStatus == -1 || this.mLuxValue.mLux[i][1] * privePhoneStatus == 2 || statuscount > 10)) {
                phoneStatusCount++;
                statuscount++;
            }
            privePhoneStatus = this.mLuxValue.mLux[i][1];
            priveAlsValue = this.mLuxValue.mLux[i][0];
            i--;
            if (i >= 0 || !this.mLuxValue.mGreaterFifty) {
                if (i < 0) {
                    break;
                }
            }
            i = n - 1;
            if (i == tempLocation) {
                break;
            }
        }
        if (alsValueFlag) {
            if (this.mAlsCount + alsCount != 0) {
                iArr[0] = ((this.mMeanAlsValue * this.mAlsCount) + sum) / (this.mAlsCount + alsCount);
            } else {
                iArr[0] = -1;
            }
        } else if (alsCount != 0) {
            iArr[0] = sum / alsCount;
        } else {
            iArr[0] = -1;
        }
        iArr[1] = count;
        iArr[2] = phoneStatusCount;
        iArr[3] = this.mMaxAlsValue;
        iArr[4] = this.mMinAlsValue;
        iArr[5] = this.mAlsCount + alsCount;
        Slog.d(TAG, "changals " + iArr[0] + " " + iArr[1] + " " + iArr[2] + " " + iArr[3] + " " + iArr[4] + " " + iArr[5]);
        return iArr;
    }

    private void calcAlsChangeCount(int length) {
        int count = 0;
        int templength = length;
        boolean darkFlag = false;
        boolean lightFlag = false;
        if (this.mLuxValue.mGreaterFifty) {
            length = this.mLuxValue.mLux.length;
        }
        if (length >= this.mLuxValue.mLux.length) {
            length = this.mLuxValue.mLux.length;
        }
        for (int j = 0; j < length; j++) {
            if (this.mLuxValue.mLux[j][0] <= 3 && (darkFlag ^ 1) != 0) {
                darkFlag = true;
                lightFlag = false;
                count++;
            } else if (this.mLuxValue.mLux[j][0] >= 8 && (lightFlag ^ 1) != 0) {
                darkFlag = false;
                lightFlag = true;
                count++;
            }
        }
        this.mAlsValueChangeCount = count;
    }
}
