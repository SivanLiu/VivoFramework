package com.vivo.common.autobrightness;

import com.vivo.common.provider.Calendar.Events;

public class AutobrightInfo {
    public static final String KEY_BRIGHTNESS = "brightness";
    public static final String KEY_CAMERA_LEVEL = "cameralevel";
    public static final String KEY_DATE = "date";
    public static final String KEY_DELAY_TIME = "delaytime";
    public static final String KEY_LIGHT_LEVEL = "lightlevel";
    public static final String KEY_LIGHT_LUX = "lightlux";
    public static final String KEY_PROXIMITY = "proximity";
    public static final String KEY_REASON = "reason";
    public static final String KEY_SCREEN_LEVEL = "screenlevel";
    public static final String KEY_SIMPLE_BRIGHTNESS = "brl";
    public static final String KEY_SIMPLE_CAMERA_LEVEL = "cal";
    public static final String KEY_SIMPLE_DATE = "dat";
    public static final String KEY_SIMPLE_DELAY_TIME = "det";
    public static final String KEY_SIMPLE_LIGHT_LEVEL = "lil";
    public static final String KEY_SIMPLE_LIGHT_LUX = "lux";
    public static final String KEY_SIMPLE_PROXIMITY = "prox";
    public static final String KEY_SIMPLE_REASON = "rea";
    public static final String KEY_SIMPLE_SCREEN_LEVEL = "scl";
    public static final String KEY_SIMPLE_VERSION = "ver";
    public static final String KEY_VERSION = "version";
    private static final String PKG_UNKOWN = "unknown";
    public static final int REASON_FORCE = 2;
    public static final int REASON_MANUAL = 1;
    private static final String TAG = "AutobrightInfo";
    public static final int VERSION_1ST = 1;
    public static final int VERSION_2ND = 2;
    public float mAngleX;
    public float mAngleY;
    public float mAngleZ;
    public int mBrightness;
    public int mCameraLevel;
    public int mChangeDownLux;
    public int mChangeUpLux;
    public String mDate;
    public int mDelayTime;
    public float mDriverLuxLock;
    public String mForegroundPkg;
    public int mLightLevel;
    public int mLightLux;
    public int mMotionState;
    public int mPhoneStatus;
    public int mPowerPercent;
    public int mPrivBrightness;
    public boolean mProximity;
    public int mReason;
    public float mRecitfiedLuxLock;
    public int mScreenLevel;
    public int mStepCount;
    public float mUnderDispalyDriverLux;
    public float mUnderDispalyRecitfiedLux;
    public boolean mUnderDisplayThreshChanged;
    public int mVersion;
    public boolean mWifiStatus;

    public AutobrightInfo() {
        this.mPhoneStatus = -1;
        this.mStepCount = -1;
        this.mBrightness = -1;
        this.mLightLevel = 0;
        this.mCameraLevel = 3;
        this.mScreenLevel = 7;
        this.mLightLux = 10;
        this.mDelayTime = 2500;
        this.mReason = -1;
        this.mVersion = getVersion();
        this.mDate = PKG_UNKOWN;
        this.mProximity = false;
        this.mWifiStatus = false;
        this.mPowerPercent = -1;
        this.mForegroundPkg = PKG_UNKOWN;
        this.mChangeUpLux = 0;
        this.mChangeDownLux = 0;
        this.mDriverLuxLock = 0.0f;
        this.mRecitfiedLuxLock = 0.0f;
        this.mUnderDispalyDriverLux = 0.0f;
        this.mUnderDispalyRecitfiedLux = 0.0f;
        this.mUnderDisplayThreshChanged = true;
        this.mAngleX = 0.0f;
        this.mAngleY = 0.0f;
        this.mAngleZ = 0.0f;
        this.mMotionState = 0;
    }

    public AutobrightInfo(AutobrightInfo info) {
        this.mPhoneStatus = -1;
        this.mStepCount = -1;
        copyFrom(info);
    }

    public boolean compare(AutobrightInfo other) {
        boolean ret = true;
        if (other == null) {
            return false;
        }
        if (other.mBrightness != this.mBrightness || other.mScreenLevel != this.mScreenLevel) {
            ret = false;
        } else if (other.mDelayTime < this.mDelayTime || other.mDelayTime == 0) {
            ret = false;
        } else if (this.mProximity && (other.mProximity ^ 1) != 0) {
            ret = false;
        }
        return ret;
    }

    public boolean equals(AutobrightInfo other) {
        if (other != null && this.mScreenLevel == other.mScreenLevel && this.mBrightness == other.mBrightness && this.mLightLux == other.mLightLux && this.mReason == other.mReason && this.mDate.equals(other.mDate)) {
            return true;
        }
        return false;
    }

    public void copyFrom(AutobrightInfo a) {
        if (a != null) {
            this.mBrightness = a.mBrightness;
            this.mLightLevel = a.mLightLevel;
            this.mCameraLevel = a.mCameraLevel;
            this.mScreenLevel = a.mScreenLevel;
            this.mLightLux = a.mLightLux;
            this.mDelayTime = a.mDelayTime;
            this.mReason = a.mReason;
            this.mVersion = a.mVersion;
            this.mDate = a.mDate;
            this.mProximity = a.mProximity;
            this.mWifiStatus = a.mWifiStatus;
            this.mPowerPercent = a.mPowerPercent;
            this.mForegroundPkg = a.mForegroundPkg;
            this.mPrivBrightness = a.mPrivBrightness;
            this.mPhoneStatus = a.mPhoneStatus;
            this.mChangeUpLux = a.mChangeUpLux;
            this.mChangeDownLux = a.mChangeDownLux;
            this.mDriverLuxLock = a.mDriverLuxLock;
            this.mRecitfiedLuxLock = a.mRecitfiedLuxLock;
            this.mUnderDispalyDriverLux = a.mUnderDispalyDriverLux;
            this.mUnderDispalyRecitfiedLux = a.mUnderDispalyRecitfiedLux;
            this.mUnderDisplayThreshChanged = a.mUnderDisplayThreshChanged;
            this.mAngleX = a.mAngleX;
            this.mAngleY = a.mAngleY;
            this.mAngleZ = a.mAngleZ;
            this.mMotionState = a.mMotionState;
        }
    }

    private int getVersion() {
        return 1;
    }

    public void reset() {
        this.mBrightness = -1;
        this.mLightLevel = 0;
        this.mCameraLevel = 3;
        this.mScreenLevel = 7;
        this.mLightLux = 10;
        this.mDelayTime = 2500;
        this.mProximity = false;
    }

    public String toString() {
        return "brightness=" + this.mBrightness + ";" + KEY_LIGHT_LEVEL + "=" + this.mLightLevel + ";" + KEY_CAMERA_LEVEL + "=" + this.mCameraLevel + ";" + KEY_SCREEN_LEVEL + "=" + this.mScreenLevel + ";" + KEY_LIGHT_LUX + "=" + this.mLightLux + ";" + KEY_DELAY_TIME + "=" + this.mDelayTime + ";" + KEY_REASON + "=" + this.mReason + ";" + KEY_VERSION + "=" + this.mVersion + ";" + "date" + "=" + this.mDate + ";" + "proximity" + "=" + this.mProximity + ";";
    }

    public String toSimpleString() {
        return "brl=" + this.mBrightness + ";" + KEY_SIMPLE_LIGHT_LEVEL + "=" + this.mLightLevel + ";" + KEY_SIMPLE_CAMERA_LEVEL + "=" + this.mCameraLevel + ";" + KEY_SIMPLE_SCREEN_LEVEL + "=" + this.mScreenLevel + ";" + KEY_SIMPLE_LIGHT_LUX + "=" + this.mLightLux + ";" + KEY_SIMPLE_DELAY_TIME + "=" + this.mDelayTime + ";" + KEY_SIMPLE_REASON + "=" + this.mReason + ";" + KEY_SIMPLE_VERSION + "=" + this.mVersion + ";" + KEY_SIMPLE_DATE + "=" + this.mDate + ";" + KEY_SIMPLE_PROXIMITY + "=" + this.mProximity + ";";
    }

    public String versionToString(int ver) {
        switch (ver) {
            case 1:
                return "VERSION_1ST";
            case 2:
                return "VERSION_2ND";
            default:
                return "UN_SURPPORT:" + ver;
        }
    }

    public String reasonToString(int reason) {
        switch (reason) {
            case 1:
                return "REASON_MANUAL";
            case 2:
                return "REASON_FORCE";
            default:
                return "REASON_UNKOWN:" + reason;
        }
    }

    public static AutobrightInfo stringToObject(String info) {
        if (info == null || Events.DEFAULT_SORT_ORDER.equals(info)) {
            return null;
        }
        ParamParser parser = new ParamParser(info);
        AutobrightInfo abInfo = new AutobrightInfo();
        String strBri = parser.get(KEY_BRIGHTNESS);
        String strLightLv = parser.get(KEY_LIGHT_LEVEL);
        String strCamLv = parser.get(KEY_CAMERA_LEVEL);
        String strScrLv = parser.get(KEY_SCREEN_LEVEL);
        String strLightLx = parser.get(KEY_LIGHT_LUX);
        String strDelay = parser.get(KEY_DELAY_TIME);
        String strReason = parser.get(KEY_REASON);
        String strVersion = parser.get(KEY_VERSION);
        String strDate = parser.get("date");
        String strProximity = parser.get("proximity");
        if (strBri == null || (Events.DEFAULT_SORT_ORDER.equals(strBri) ^ 1) == 0) {
            return null;
        }
        try {
            abInfo.mBrightness = Integer.valueOf(strBri).intValue();
            if (strLightLv == null || (Events.DEFAULT_SORT_ORDER.equals(strLightLv) ^ 1) == 0) {
                return null;
            }
            try {
                abInfo.mLightLevel = Integer.valueOf(strLightLv).intValue();
                if (strCamLv == null || (Events.DEFAULT_SORT_ORDER.equals(strCamLv) ^ 1) == 0) {
                    return null;
                }
                try {
                    abInfo.mCameraLevel = Integer.valueOf(strCamLv).intValue();
                    if (strScrLv == null || (Events.DEFAULT_SORT_ORDER.equals(strScrLv) ^ 1) == 0) {
                        return null;
                    }
                    try {
                        abInfo.mScreenLevel = Integer.valueOf(strScrLv).intValue();
                        if (strLightLx == null || (Events.DEFAULT_SORT_ORDER.equals(strLightLx) ^ 1) == 0) {
                            return null;
                        }
                        try {
                            abInfo.mLightLux = Integer.valueOf(strLightLx).intValue();
                            if (strDelay == null || (Events.DEFAULT_SORT_ORDER.equals(strDelay) ^ 1) == 0) {
                                return null;
                            }
                            try {
                                abInfo.mDelayTime = Integer.valueOf(strDelay).intValue();
                                if (strReason == null || (Events.DEFAULT_SORT_ORDER.equals(strReason) ^ 1) == 0) {
                                    return null;
                                }
                                try {
                                    abInfo.mReason = Integer.valueOf(strReason).intValue();
                                    if (strVersion == null || (Events.DEFAULT_SORT_ORDER.equals(strVersion) ^ 1) == 0) {
                                        return null;
                                    }
                                    try {
                                        abInfo.mVersion = Integer.valueOf(strVersion).intValue();
                                        if (strDate == null || (Events.DEFAULT_SORT_ORDER.equals(strDate) ^ 1) == 0) {
                                            return null;
                                        }
                                        abInfo.mDate = strDate;
                                        if (strProximity == null || !"true".equals(strProximity)) {
                                            abInfo.mProximity = false;
                                        } else {
                                            abInfo.mProximity = true;
                                        }
                                        return abInfo;
                                    } catch (Exception e) {
                                        return null;
                                    }
                                } catch (Exception e2) {
                                    return null;
                                }
                            } catch (Exception e3) {
                                return null;
                            }
                        } catch (Exception e4) {
                            return null;
                        }
                    } catch (Exception e5) {
                        return null;
                    }
                } catch (Exception e6) {
                    return null;
                }
            } catch (Exception e7) {
                return null;
            }
        } catch (Exception e8) {
            return null;
        }
    }
}
