package com.qti.location.sdk.collection;

import android.os.SystemProperties;
import android.util.Log;
import com.qti.debugreport.IZatLocationReport.IzatLocationSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IZatGpsLocData {
    private static final int ALMANAC_MASK = 1;
    private static final int BDS_ALMANAC_MASK = 4;
    private static final int BDS_EPHEMERIS_MASK = 3;
    private static final int BDS_SV_PRN_MAX = 235;
    private static final int BDS_SV_PRN_MIN = 201;
    private static final int BDS_USED_FOR_FIX_MASK = 5;
    public static final String BOOT_TYPE_HOT = "hot";
    public static final String BOOT_TYPE_UNKOWN = "unkown";
    public static final String BOOT_TYPE_WARM = "warm";
    private static final long CN0_LOW_LIMIT = 20;
    private static final int DATA_VERSION = 2;
    private static boolean DEBUG = SystemProperties.getBoolean(PROP_DEBUG, false);
    private static final int EPHEMERIS_MASK = 0;
    private static final int FIRST_FIX_SUCCESS = 2;
    private static final int FIX_FAILED = 1;
    private static final int GAL_ALMANAC_MASK = 7;
    private static final int GAL_EPHEMERIS_MASK = 6;
    private static final int GAL_SV_PRN_MAX = 336;
    private static final int GAL_SV_PRN_MIN = 301;
    private static final int GAL_USED_FOR_FIX_MASK = 8;
    private static final int GLONASS_ALMANAC_MASK = 1;
    private static final int GLONASS_EPHEMERIS_MASK = 0;
    private static final int GLONASS_SV_PRN_MAX = 96;
    private static final int GLONASS_SV_PRN_MIN = 65;
    private static final int GLONASS_USED_FOR_FIX_MASK = 2;
    private static final int GPS_SV_PRN_MAX = 32;
    private static final int GPS_SV_PRN_MIN = 1;
    public static final String KEY_ACCELEMETER_INFO = "acc";
    private static final String KEY_AVERAGE_CN0 = "avgCN0";
    private static final String KEY_DURATION = "dur";
    private static final String KEY_FIRST_CN0 = "fCN0";
    private static final String KEY_FIRST_CN0_TIMESTAMP = "fCN0Ts";
    private static final String KEY_FIRST_GPS = "fGpsLoc";
    private static final String KEY_GPS_BOOT_TYPE = "boot";
    private static final String KEY_JAMMER_PERCENT = "jamPct";
    private static final String KEY_LOCATION_CACHE = "locCache";
    private static final String KEY_LOCATION_SUCCESS = "succ";
    private static final String KEY_LOSTRESUME_START = "lrstart";
    private static final String KEY_LOSTRESUME_TIME = "lrtime";
    private static final String KEY_NETWORK_CONNECT = "net";
    private static final String KEY_NLP_INJECT_LOCATION = "nlpInLoc";
    private static final String KEY_NLP_INJECT_TIME = "nlpInTs";
    private static final String KEY_NLP_INJECT_TYPE = "nlpInType";
    private static final String KEY_NLP_PASSIVE_LOCATION = "nlpPaLoc";
    private static final String KEY_NLP_PASSIVE_TIME = "nlpPaTs";
    private static final String KEY_PACKAGE_NAMES = "pkg";
    public static final String KEY_PHONE_POSE = "pose";
    private static final String KEY_RECORD_TYPE = "type";
    private static final String KEY_SAP_VALID = "sap";
    private static final String KEY_START_TIME = "sta";
    private static final String KEY_SV_USED_COUNT = "svCnt";
    private static final String KEY_TTFF = "ttff";
    private static final String KEY_VERSION = "ver";
    private static final String KEY_XTRA_VALID = "xtra";
    private static final int MISSING_FIX = 3;
    public static final String PROP_DEBUG = "persist.vivo.gnss.data";
    private static final int RE_FIX_SUCCESS = 4;
    private static final long SAP_VALID_TIME_LIMIT = 180000;
    private static final String TAG = "IZatGpsLocData";
    public static final String TYPE_CN0LOW = "cn0low";
    public static final String TYPE_IN5S = "i5s";
    public static final String TYPE_LOST = "lost";
    public static final String TYPE_LOSTRESUME = "lostresume";
    public static final String TYPE_OVER5S = "o5s";
    public static final String TYPE_SPEEDAB = "speedab";
    public static final String TYPE_STOP = "stop";
    private static final int USED_FOR_FIX_MASK = 2;
    private CachedInteger mAverageSnrs = new CachedInteger();
    private String mBootType = BOOT_TYPE_UNKOWN;
    private CachedLocation mCachedLocation = new CachedLocation();
    private int mCurrentIndex = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat mDateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private double mFirstGpsLatitude = 0.0d;
    private double mFirstGpsLongitude = 0.0d;
    private float mFirstGpsSpeed = 0.0f;
    private int mFirstValidSnr = -1;
    private long mFirstValidSnrTime = 0;
    private CachedInteger mInsEngaged = new CachedInteger();
    private int mJammerTimeCount = 0;
    private boolean mLocationMissing = false;
    private boolean mLocationSuccess = false;
    private long mLostResumeStart = -1;
    private long mLostResumeTime = -1;
    private boolean mNetworkConnect = false;
    private double mNlpInjectLatitude = 0.0d;
    private long mNlpInjectLocationTime = 0;
    private double mNlpInjectLongitude = 0.0d;
    private String mNlpInjectType = getSourceType(IzatLocationSource.POSITION_SOURCE_UNKNOWN);
    private double mNlpPassiveLatitude = 0.0d;
    private long mNlpPassiveLocationTime = 0;
    private double mNlpPassiveLongitude = 0.0d;
    private JSONArray mPcakges = new JSONArray();
    private IzatLocationSource mReportInjectionSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
    private long mSapTime = 0;
    private long mStartNavigatingTime = 0;
    private CachedInteger mSvCount = new CachedInteger();
    private long mTimeToFirstFix = 0;
    private CachedInteger mUsedSvCount = new CachedInteger();
    private boolean mXtraValid = false;

    public IZatGpsLocData() {
        this.mDateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        resetInfo(0);
    }

    public void setLostResumeTime(long lrtime) {
        this.mLostResumeTime = lrtime;
    }

    public void setLostResumeStart(long lrstart) {
        this.mLostResumeStart = lrstart;
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public void startNavigating(long fixRequestTime) {
        resetInfo(fixRequestTime);
    }

    private void resetInfo(long fixRequestTime) {
        this.mStartNavigatingTime = fixRequestTime;
        this.mNlpPassiveLocationTime = 0;
        this.mFirstValidSnrTime = -1;
        this.mFirstValidSnr = -1;
        this.mNlpInjectLocationTime = 0;
        this.mNlpInjectLongitude = 0.0d;
        this.mNlpInjectLatitude = 0.0d;
        this.mTimeToFirstFix = 0;
        this.mFirstGpsLongitude = 0.0d;
        this.mFirstGpsLatitude = 0.0d;
        this.mFirstGpsSpeed = 0.0f;
        this.mLostResumeTime = -1;
        this.mLostResumeStart = -1;
        this.mCurrentIndex = 0;
        this.mNetworkConnect = false;
        this.mXtraValid = false;
        this.mBootType = BOOT_TYPE_UNKOWN;
        this.mLocationSuccess = false;
        this.mLocationMissing = false;
        this.mJammerTimeCount = 0;
        this.mSapTime = 0;
        this.mSvCount.reset();
        this.mUsedSvCount.reset();
        this.mAverageSnrs.reset();
        this.mInsEngaged.reset();
        this.mReportInjectionSource = IzatLocationSource.POSITION_SOURCE_UNKNOWN;
        this.mNlpInjectType = getSourceType(this.mReportInjectionSource);
    }

    public void onNlpPassiveLocation(long time, double longitude, double latitude) {
        if (this.mNlpPassiveLocationTime == 0) {
            this.mNlpPassiveLocationTime = time - this.mStartNavigatingTime;
            this.mNlpPassiveLongitude = longitude;
            this.mNlpPassiveLatitude = latitude;
        }
    }

    private String getSourceType(IzatLocationSource type) {
        if (type == IzatLocationSource.POSITION_SOURCE_CPI) {
            return "CPI";
        }
        if (type == IzatLocationSource.POSITION_SOURCE_REFERENCE_LOCATION) {
            return "REFERENCE";
        }
        if (type == IzatLocationSource.POSITION_SOURCE_TLE) {
            return "TLE";
        }
        return "UNKOWN";
    }

    public void onNlpInjectLocation(IzatLocationSource source, long time, double longitude, double latitude) {
        this.mNlpInjectType = getSourceType(source);
        this.mNlpInjectLocationTime = time;
        this.mNlpInjectLongitude = (longitude * 180.0d) / 3.141592653589793d;
        this.mNlpInjectLatitude = (latitude * 180.0d) / 3.141592653589793d;
    }

    public void onReportLocation(boolean hasLatLong, long time, double latitude, double longitude, float speed) {
        if (hasLatLong && this.mTimeToFirstFix == 0) {
            this.mTimeToFirstFix = time - this.mStartNavigatingTime;
            this.mLocationSuccess = true;
            this.mFirstGpsLongitude = longitude;
            this.mFirstGpsLatitude = latitude;
            this.mFirstGpsSpeed = speed;
        }
        this.mCachedLocation.add(latitude, longitude, time, hasLatLong, speed);
    }

    public void setXtraValid(boolean valid) {
        this.mXtraValid = valid;
    }

    public void setBootType(String type) {
        this.mBootType = type;
    }

    public void setJammer(int pgaGain, long gpsBPAmpI, long pgsBPAmpQ) {
        if (pgaGain == -12) {
            this.mJammerTimeCount++;
            return;
        }
        if (gpsBPAmpI > 500 && pgsBPAmpQ > 500) {
            this.mJammerTimeCount++;
        }
    }

    public void setSap(boolean valid) {
        if (valid && this.mLocationSuccess) {
            long now = System.currentTimeMillis();
            if (this.mSapTime == 0 && this.mStartNavigatingTime > 0 && now - this.mStartNavigatingTime > SAP_VALID_TIME_LIMIT) {
                this.mSapTime = now - this.mStartNavigatingTime;
            }
        }
    }

    public void setNetwork(boolean data) {
        this.mNetworkConnect = data;
        if (DEBUG) {
            Log.d(TAG, "setNetwork data:" + data);
        }
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
            JSONArray jarray = new JSONArray();
            String[] arr = source.replace("WorkSource{", "").replace(",}", "").replace("}", "").split(",");
            if (arr != null && arr.length > 0) {
                for (String up : arr) {
                    if (up.contains(" ")) {
                        String[] p = up.split(" ");
                        if (p.length > 1 && ("".equals(p[1]) ^ 1) != 0) {
                            jarray.put(p[1]);
                        }
                    }
                }
            }
            if (jarray.length() > 0) {
                this.mPcakges = jarray;
            }
        }
    }

    public void getAverageSnrs(int count, int[] mSvidWithFlags, float[] cn0) {
        this.mSvCount.add(count);
        ArrayList<Float> usedSnrsList = new ArrayList();
        int usedSnrsCount = 0;
        float sum = 0.0f;
        float average = 0.0f;
        boolean first = false;
        try {
            usedSnrsList.clear();
            int i = 0;
            while (i < count) {
                boolean isUsed = false;
                if ((mSvidWithFlags[i] & 4) != 0) {
                    isUsed = true;
                }
                if (isUsed && cn0[i] > 0.0f) {
                    if (this.mFirstValidSnrTime == -1) {
                        this.mFirstValidSnrTime = System.currentTimeMillis() - this.mStartNavigatingTime;
                        first = true;
                        if (DEBUG) {
                            Log.v(TAG, "mFirstValidSnrTime:" + this.mFirstValidSnrTime);
                        }
                    }
                    usedSnrsList.add(Float.valueOf(cn0[i]));
                    usedSnrsCount++;
                }
                i++;
            }
            this.mUsedSvCount.add(usedSnrsCount);
            Collections.sort(usedSnrsList);
            int tempCount = usedSnrsCount >= 4 ? 4 : usedSnrsCount;
            for (i = 1; i <= tempCount; i++) {
                sum += ((Float) usedSnrsList.get(usedSnrsCount - i)).floatValue();
            }
            if (!(tempCount == 0 || sum == 0.0f)) {
                average = sum / ((float) tempCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "getAverageSnrs, sv count:" + count + ", usedSnrsCount:" + usedSnrsCount + ", sum:" + sum + ", average:" + average);
        }
        if (first) {
            this.mFirstValidSnr = (int) average;
        }
        this.mAverageSnrs.add((int) average);
    }

    public boolean decideCn0Low() {
        int[] avgTemp = this.mAverageSnrs.getBuffer();
        int size = this.mAverageSnrs.getBufferMax();
        int index = this.mAverageSnrs.getIndex() - 1;
        if (this.mCurrentIndex != ((index + size) - 10) % size) {
            return false;
        }
        int count = 0;
        boolean decide = false;
        for (int i = 0; i < 10; i++) {
            if (((long) avgTemp[index]) < CN0_LOW_LIMIT && avgTemp[index] != -1) {
                count++;
            }
            index = ((index + size) - 1) % size;
        }
        if (count >= GAL_EPHEMERIS_MASK) {
            decide = true;
        }
        this.mCurrentIndex = ((index + size) + 10) % size;
        return decide;
    }

    private String formatGmtDate(long time) {
        if (time < 0) {
            time = 0;
        }
        return this.mDateFormatGmt.format(new Date(time));
    }

    public JSONObject getJson(String type) {
        long now = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        long duration = now - this.mStartNavigatingTime;
        try {
            obj.put(KEY_RECORD_TYPE, type);
            obj.put(KEY_START_TIME, this.mDateFormat.format(new Date(this.mStartNavigatingTime)));
            obj.put(KEY_NLP_PASSIVE_TIME, formatGmtDate(this.mNlpPassiveLocationTime));
            obj.put(KEY_NLP_PASSIVE_LOCATION, String.valueOf(this.mNlpPassiveLongitude) + "," + String.valueOf(this.mNlpPassiveLatitude));
            obj.put(KEY_NLP_INJECT_TYPE, this.mNlpInjectType);
            obj.put(KEY_NLP_INJECT_TIME, this.mDateFormat.format(new Date(this.mNlpInjectLocationTime)));
            obj.put(KEY_NLP_INJECT_LOCATION, String.valueOf(this.mNlpInjectLongitude) + "," + String.valueOf(this.mNlpInjectLatitude));
            obj.put(KEY_LOCATION_SUCCESS, this.mLocationSuccess);
            obj.put(KEY_TTFF, formatGmtDate(this.mTimeToFirstFix));
            obj.put(KEY_FIRST_GPS, String.valueOf(this.mFirstGpsLongitude) + "," + String.valueOf(this.mFirstGpsLatitude) + "," + String.valueOf(this.mFirstGpsSpeed));
            obj.put(KEY_SAP_VALID, formatGmtDate(this.mSapTime));
            obj.put(KEY_XTRA_VALID, this.mXtraValid);
            obj.put(KEY_FIRST_CN0_TIMESTAMP, formatGmtDate(this.mFirstValidSnrTime));
            obj.put(KEY_FIRST_CN0, this.mFirstValidSnr);
            obj.put(KEY_LOSTRESUME_TIME, this.mLostResumeTime);
            obj.put(KEY_LOSTRESUME_START, this.mDateFormat.format(new Date(this.mLostResumeStart)));
            obj.put(KEY_AVERAGE_CN0, this.mAverageSnrs.getLast());
            double pct = (((double) this.mJammerTimeCount) * 100.0d) / ((double) ((1000 + duration) / 1000));
            try {
                pct = Double.valueOf(String.format("%.2f", new Object[]{Double.valueOf(pct)})).doubleValue();
            } catch (NumberFormatException e) {
            }
            obj.put(KEY_JAMMER_PERCENT, pct);
            obj.put(KEY_SV_USED_COUNT, this.mUsedSvCount.getLast());
            obj.put(KEY_LOCATION_CACHE, this.mCachedLocation.getJsonArray());
            obj.put(KEY_GPS_BOOT_TYPE, this.mBootType);
            obj.put(KEY_PACKAGE_NAMES, this.mPcakges);
            obj.put(KEY_NETWORK_CONNECT, this.mNetworkConnect);
            obj.put(KEY_DURATION, formatGmtDate(duration));
            obj.put(KEY_VERSION, 2);
            if (DEBUG) {
                Log.d(TAG, "getJson tooks:" + (System.currentTimeMillis() - now));
            }
            return obj;
        } catch (JSONException e2) {
            e2.printStackTrace();
            return null;
        }
    }
}
