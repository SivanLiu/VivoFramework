package com.vivo.services.motion;

import android.util.Log;
import com.vivo.services.DeviceParaProvideService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ProximityThresholdCal {
    private static String PS_BASE_VALUE_PATH = "sys/devices/platform/als_ps/driver/ps_base_value";
    private static String TAG = "ProximityThresholdCal";
    private static DeviceParaProvideService mDeviceParaProvideService = null;
    private static int mProximityCloseThreshold = -1;
    private static ProximityPara mProximityPara1 = null;
    private static ProximityPara mProximityPara2 = null;
    private static ProximityPara mProximityPara3 = null;
    private static int mProximityParaBase = -1;

    private class ProximityPara {
        private int mAway;
        private int mCalDown;
        private int mCalUp;
        private int mClose;

        /* synthetic */ ProximityPara(ProximityThresholdCal this$0, ProximityPara -this1) {
            this();
        }

        private ProximityPara() {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00ca A:{SYNTHETIC, Splitter: B:27:0x00ca} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00f0 A:{SYNTHETIC, Splitter: B:33:0x00f0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readPsBaseValueFromFile() {
        FileNotFoundException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(PS_BASE_VALUE_PATH)));
            try {
                String tempString = reader2.readLine();
                reader2.close();
                mProximityParaBase = Integer.valueOf(tempString).intValue();
                Log.d(TAG, "readPsBaseValueFromFile:" + mProximityParaBase);
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Log.d(TAG, "the readFile is:" + e1.getMessage());
                    }
                }
                return true;
            } catch (Exception e2) {
                try {
                    Log.d(TAG, "reader.readLine():" + e2.getMessage());
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e12) {
                            Log.d(TAG, "the readFile is:" + e12.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = reader2;
                    try {
                        Log.d(TAG, "the readFile is:" + e.getMessage());
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e122) {
                                Log.d(TAG, "the readFile is:" + e122.getMessage());
                            }
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e1222) {
                            Log.d(TAG, "the readFile is:" + e1222.getMessage());
                        }
                    }
                    throw th;
                }
                return false;
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            Log.d(TAG, "the readFile is:" + e.getMessage());
            if (reader != null) {
            }
            return false;
        }
    }

    public ProximityThresholdCal() {
        if (mDeviceParaProvideService == null) {
            mDeviceParaProvideService = new DeviceParaProvideService();
        }
        if (mProximityPara1 == null) {
            mProximityPara1 = new ProximityPara(this, null);
            mProximityPara1.mCalDown = mDeviceParaProvideService.getPsOneStepMinValue();
            mProximityPara1.mCalUp = mDeviceParaProvideService.getPsOneStepMaxValue();
            mProximityPara1.mClose = mDeviceParaProvideService.getPsOneStepCloseValue();
            mProximityPara1.mAway = mDeviceParaProvideService.getPsOneStepAwayValue();
        }
        if (mProximityPara2 == null) {
            mProximityPara2 = new ProximityPara(this, null);
            mProximityPara2.mCalDown = mDeviceParaProvideService.getPsSecStepMinValue();
            mProximityPara2.mCalUp = mDeviceParaProvideService.getPsSecStepMaxValue();
            mProximityPara2.mClose = mDeviceParaProvideService.getPsSecStepCloseValue();
            mProximityPara2.mAway = mDeviceParaProvideService.getPsSecStepAwayValue();
        }
        if (mProximityPara3 == null) {
            mProximityPara3 = new ProximityPara(this, null);
            mProximityPara3.mCalDown = mDeviceParaProvideService.getPsThrStepMinValue();
            mProximityPara3.mCalUp = mDeviceParaProvideService.getPsThrStepMaxValue();
            mProximityPara3.mClose = mDeviceParaProvideService.getPsThrStepCloseValue();
            mProximityPara3.mAway = mDeviceParaProvideService.getPsThrStepAwayValue();
        }
        if (mProximityParaBase < 0 && !readPsBaseValueFromFile()) {
            mProximityParaBase = mDeviceParaProvideService.getPsBaseValue();
        }
    }

    public int GetProximityCloseThreshold() {
        float temCloseValue = -1.0f;
        float temAwayValue = -1.0f;
        if (mProximityCloseThreshold < 0) {
            if (mProximityParaBase > mProximityPara1.mCalDown && mProximityParaBase <= mProximityPara1.mCalUp) {
                temCloseValue = (((float) mProximityPara1.mClose) / 100.0f) * ((float) mProximityParaBase);
                temAwayValue = (((float) mProximityPara1.mAway) / 100.0f) * ((float) mProximityParaBase);
            } else if (mProximityParaBase > mProximityPara2.mCalDown && mProximityParaBase <= mProximityPara2.mCalUp) {
                temCloseValue = (((float) mProximityPara2.mClose) / 100.0f) * ((float) mProximityParaBase);
                temAwayValue = (((float) mProximityPara2.mAway) / 100.0f) * ((float) mProximityParaBase);
            } else if (mProximityParaBase > mProximityPara3.mCalDown && mProximityParaBase <= mProximityPara3.mCalUp) {
                temCloseValue = (((float) mProximityPara3.mClose) / 100.0f) * ((float) mProximityParaBase);
                temAwayValue = (((float) mProximityPara3.mAway) / 100.0f) * ((float) mProximityParaBase);
            }
            if (temCloseValue >= 0.0f && temAwayValue >= 0.0f) {
                mProximityCloseThreshold = (int) (((temCloseValue - temAwayValue) / 10.0f) + temAwayValue);
            }
        }
        return mProximityCloseThreshold;
    }
}
