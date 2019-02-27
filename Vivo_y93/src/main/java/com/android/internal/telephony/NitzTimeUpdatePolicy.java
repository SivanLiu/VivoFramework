package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.VivoBigDataManager.MODULE_TAG;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class NitzTimeUpdatePolicy {
    public static final String ACTION_NTP_TIMES = "vivo.android.intent.action.NTP_TIMES";
    private static final long DAY_IN_MILLIS = 86400000;
    private static final long HOUR_MIN_IN_MILLIS = 3600000;
    static final String LOG_TAG = "NitzTimeUpdatePolicy";
    private static final int MAX_COLLECT_COUNT = 50;
    private static final long MIN_IN_MILLIS = 60000;
    private static NitzTimeUpdatePolicy sInstance;
    private String KEY_NTPTIME = "ntpTime";
    private String KEY_NTPTIME_REfERENCE = "ntpTimeReference";
    private Context mContext;
    private int mCurrentCount = 0;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (NitzTimeUpdatePolicy.ACTION_NTP_TIMES.equals(intent.getAction())) {
                long ntpTime = intent.getLongExtra(NitzTimeUpdatePolicy.this.KEY_NTPTIME, -1);
                long cachedNtpTimeReference = intent.getLongExtra(NitzTimeUpdatePolicy.this.KEY_NTPTIME_REfERENCE, -1);
                Rlog.v(NitzTimeUpdatePolicy.LOG_TAG, "ntp time update,ntpTime:" + ntpTime + " cachedNtpTimeReference:" + cachedNtpTimeReference);
                NitzTimeUpdatePolicy.this.mIsNtpChecked = false;
                if (NitzTimeUpdatePolicy.this.isNtpTimeValid(ntpTime, cachedNtpTimeReference)) {
                    NitzTimeUpdatePolicy.this.mIsNtpTimeUpdate = true;
                    NitzTimeUpdatePolicy.this.mNtpSaveTime = ntpTime;
                    NitzTimeUpdatePolicy.this.mNtpSavedAtTime = cachedNtpTimeReference;
                    if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
                        for (int i = 0; i < NitzTimeUpdatePolicy.this.mPhoneCount; i++) {
                            if (NitzTimeUpdatePolicy.this.mWaitCheckDataConnect[i] == ENUM_WAIT_STATE.WAIT_NTP) {
                                NitzTimeUpdatePolicy.this.mWaitCheckDataConnect[i] = ENUM_WAIT_STATE.COMPLETE;
                                NitzTimeUpdatePolicy.this.collectErrorTime(i);
                            }
                        }
                        return;
                    }
                    return;
                }
                Rlog.v(NitzTimeUpdatePolicy.LOG_TAG, "ntp is invalid");
                NitzTimeUpdatePolicy.this.mIsNtpTimeUpdate = false;
                NitzTimeUpdatePolicy.this.mNtpSaveTime = -1;
                NitzTimeUpdatePolicy.this.mNtpSavedAtTime = -1;
            }
        }
    };
    private boolean mIsNtpChecked = false;
    private boolean mIsNtpTimeUpdate = false;
    private NitzTimeInfo[] mNitzTimeInfo;
    private long mNitzUpdatedAtTime;
    private int mNitzUpdatedPhoneId;
    private long mNtpSaveTime;
    private long mNtpSavedAtTime;
    private int mPhoneCount;
    private long mPreDayCollectTime = 0;
    private UserLoc[] mUserLoc;
    private ENUM_WAIT_STATE[] mWaitCheckDataConnect;

    private enum ENUM_WAIT_STATE {
        INIT,
        WAIT_LOC,
        WAIT_NTP,
        COMPLETE
    }

    private static class NitzTimeInfo {
        public boolean mIsCdma = false;
        public long mSavedAtTime = -1;
        public long mSavedTime = -1;
        public String mSavedTimeZone = "";

        public long getCurrentTimeMillis() {
            if (this.mSavedTime == -1 || this.mSavedAtTime == -1) {
                return -1;
            }
            return this.mSavedTime + (SystemClock.elapsedRealtime() - this.mSavedAtTime);
        }

        public int getPriority() {
            if (this.mSavedTime <= 0 || this.mSavedAtTime <= 0) {
                return 0;
            }
            if (this.mIsCdma) {
                return 2;
            }
            return 1;
        }

        public String toString() {
            return "mSavedTime:" + this.mSavedTime + " mSavedAtTime:" + this.mSavedAtTime + " mSavedTimeZone:" + this.mSavedTimeZone + " mIsCdma:" + this.mIsCdma;
        }
    }

    private static class UserLoc {
        public CellLocation mCellLoc;
        public boolean mIsCdma;
        public String mPlmn;

        /* synthetic */ UserLoc(UserLoc -this0) {
            this();
        }

        private UserLoc() {
            this.mPlmn = "";
            this.mIsCdma = false;
            this.mCellLoc = null;
        }
    }

    public static synchronized NitzTimeUpdatePolicy getInstance(Context context) {
        NitzTimeUpdatePolicy nitzTimeUpdatePolicy;
        synchronized (NitzTimeUpdatePolicy.class) {
            if (sInstance == null) {
                sInstance = new NitzTimeUpdatePolicy(context);
            }
            nitzTimeUpdatePolicy = sInstance;
        }
        return nitzTimeUpdatePolicy;
    }

    public NitzTimeUpdatePolicy(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPhoneCount = TelephonyManager.from(context).getSimCount();
        this.mNitzTimeInfo = new NitzTimeInfo[this.mPhoneCount];
        if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
            this.mWaitCheckDataConnect = new ENUM_WAIT_STATE[this.mPhoneCount];
            this.mUserLoc = new UserLoc[this.mPhoneCount];
        }
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mNitzTimeInfo[i] = new NitzTimeInfo();
            if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
                this.mWaitCheckDataConnect[i] = ENUM_WAIT_STATE.INIT;
                this.mUserLoc[i] = new UserLoc();
            }
        }
        registerBroadcast();
    }

    /* JADX WARNING: Missing block: B:27:0x00c1, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setNitzTimeInfo(int phoneId, long time, long receivedTime, boolean isCdma) {
        if (phoneId >= 0) {
            if (phoneId < this.mPhoneCount) {
                NitzTimeInfo timeInfo = new NitzTimeInfo();
                timeInfo.mSavedTime = time;
                timeInfo.mSavedAtTime = receivedTime;
                timeInfo.mIsCdma = isCdma;
                if (this.mNitzTimeInfo[phoneId] != null && this.mNitzUpdatedPhoneId == phoneId && timeInfo.getPriority() > this.mNitzTimeInfo[phoneId].getPriority()) {
                    this.mNitzUpdatedPhoneId = -1;
                    this.mNitzUpdatedAtTime = -1;
                }
                this.mNitzTimeInfo[phoneId] = timeInfo;
                Rlog.v(LOG_TAG, "setNitzTimeInfo,phoneId:" + phoneId + " mSavedTime:" + time + " mSavedAtTime:" + receivedTime + " isCdma:" + isCdma);
                if (!(this.mIsNtpChecked || !this.mIsNtpTimeUpdate || isNtpTimeValid(this.mNtpSaveTime, this.mNtpSavedAtTime))) {
                    Rlog.v(LOG_TAG, "ntp is invalid,reset it");
                    this.mIsNtpTimeUpdate = false;
                    this.mNtpSaveTime = -1;
                    this.mNtpSavedAtTime = -1;
                }
                if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
                    this.mWaitCheckDataConnect[phoneId] = ENUM_WAIT_STATE.WAIT_LOC;
                }
            }
        }
        Rlog.v(LOG_TAG, "setNitzTimeInfo,invalid phoneId " + phoneId);
    }

    /* JADX WARNING: Missing block: B:13:0x0033, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setNitzTimeInfo(int phoneId, String timezone) {
        if (phoneId >= 0) {
            if (phoneId < this.mPhoneCount) {
                if (TextUtils.isEmpty(timezone)) {
                    this.mNitzTimeInfo[phoneId].mSavedTimeZone = "";
                } else {
                    this.mNitzTimeInfo[phoneId].mSavedTimeZone = timezone;
                }
            }
        }
        Rlog.v(LOG_TAG, "setNitzTimeInfo,invalid phoneId " + phoneId);
    }

    public synchronized void setUpdateAtTime(int phoneId) {
        this.mNitzUpdatedPhoneId = phoneId;
        this.mNitzUpdatedAtTime = SystemClock.elapsedRealtime();
    }

    public synchronized boolean shouldUpdateTime(int phoneId, boolean isRevertTime) {
        if (phoneId >= 0) {
            if (phoneId < this.mPhoneCount) {
                if (this.mIsNtpTimeUpdate) {
                    Rlog.v(LOG_TAG, "has ntp time,skip");
                    return false;
                }
                long currentTime = SystemClock.elapsedRealtime();
                if (isRevertTime || phoneId != this.mNitzUpdatedPhoneId || currentTime - this.mNitzUpdatedAtTime >= HOUR_MIN_IN_MILLIS) {
                    NitzTimeInfo otherPhoneNtizInfo = null;
                    int priority = -1;
                    int otherPhonePriority = -1;
                    if (this.mNitzTimeInfo[phoneId] != null) {
                        priority = this.mNitzTimeInfo[phoneId].getPriority();
                    }
                    for (int i = 0; i < this.mPhoneCount; i++) {
                        if (i != phoneId) {
                            otherPhoneNtizInfo = this.mNitzTimeInfo[i];
                            break;
                        }
                    }
                    if (otherPhoneNtizInfo != null) {
                        otherPhonePriority = otherPhoneNtizInfo.getPriority();
                    }
                    if (!(priority == -1 || otherPhonePriority == -1)) {
                        if (priority < otherPhonePriority) {
                            Rlog.v(LOG_TAG, "has higer priority time,skip update");
                            return false;
                        } else if (priority == otherPhonePriority) {
                            if (isRevertTime) {
                                if (!(this.mNitzUpdatedPhoneId == -1 || this.mNitzUpdatedPhoneId == phoneId)) {
                                    Rlog.v(LOG_TAG, "use other phone to update time");
                                    return false;
                                }
                            } else if (this.mNitzUpdatedAtTime != -1 && currentTime - this.mNitzUpdatedAtTime < HOUR_MIN_IN_MILLIS) {
                                Rlog.v(LOG_TAG, "update time is in hour,skip1");
                                return false;
                            }
                        }
                    }
                    return true;
                }
                Rlog.v(LOG_TAG, "update time is in hour,skip");
                return false;
            }
        }
        Rlog.v(LOG_TAG, "sholdUpdateTime,invalid phoneId " + phoneId);
        return false;
    }

    /* JADX WARNING: Missing block: B:12:0x005a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x00a6, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void checkCollectErrorTime(int phoneId, String plmn, CellLocation cellLocation, boolean isCdma) {
        if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
            Rlog.v(LOG_TAG, "phoneId:" + phoneId + " plmn:" + plmn + " isCdma:" + isCdma + " state:" + this.mWaitCheckDataConnect[phoneId]);
            if (!TextUtils.isEmpty(plmn) && !"00000".equals(plmn)) {
                if (this.mWaitCheckDataConnect[phoneId] == ENUM_WAIT_STATE.WAIT_LOC) {
                    this.mUserLoc[phoneId].mPlmn = plmn;
                    if (isCdma) {
                        CdmaCellLocation cellLoc = (CdmaCellLocation) cellLocation;
                        CdmaCellLocation cdmaCellLoc = new CdmaCellLocation();
                        cdmaCellLoc.setCellLocationData(cellLoc.getBaseStationId(), cellLoc.getBaseStationLatitude(), cellLoc.getBaseStationLongitude(), cellLoc.getSystemId(), cellLoc.getNetworkId());
                        this.mUserLoc[phoneId].mCellLoc = cdmaCellLoc;
                        this.mUserLoc[phoneId].mIsCdma = true;
                    } else {
                        GsmCellLocation cellLoc2 = (GsmCellLocation) cellLocation;
                        GsmCellLocation gsmCellLoc = new GsmCellLocation();
                        gsmCellLoc.setLacAndCid(cellLoc2.getLac(), cellLoc2.getCid());
                        this.mUserLoc[phoneId].mCellLoc = gsmCellLoc;
                        this.mUserLoc[phoneId].mIsCdma = false;
                    }
                    if (this.mIsNtpTimeUpdate) {
                        this.mWaitCheckDataConnect[phoneId] = ENUM_WAIT_STATE.COMPLETE;
                        collectErrorTime(phoneId);
                    } else {
                        this.mWaitCheckDataConnect[phoneId] = ENUM_WAIT_STATE.WAIT_NTP;
                    }
                }
            }
        }
    }

    private synchronized void collectErrorTime(int phoneId) {
        if (VivoBigDataManager.isModuleEnable(MODULE_TAG.NITZ_TAG)) {
            Rlog.v(LOG_TAG, "state:" + this.mWaitCheckDataConnect[phoneId] + " phoneId:" + phoneId);
            if (this.mWaitCheckDataConnect[phoneId] == ENUM_WAIT_STATE.COMPLETE) {
                this.mWaitCheckDataConnect[phoneId] = ENUM_WAIT_STATE.INIT;
                NitzTimeInfo timeInfo = this.mNitzTimeInfo[phoneId];
                long currentTime = SystemClock.elapsedRealtime();
                long ntpTime = getCurrentNtpTimeMillis();
                long nitzTime = timeInfo.getCurrentTimeMillis();
                Rlog.v(LOG_TAG, "ntpTime:" + ntpTime + " nitzTime:" + nitzTime);
                if (Math.abs(ntpTime - nitzTime) > MIN_IN_MILLIS) {
                    boolean shouldConnect;
                    if (this.mPreDayCollectTime == 0) {
                        this.mPreDayCollectTime = currentTime;
                        shouldConnect = true;
                    } else if (currentTime - this.mPreDayCollectTime > DAY_IN_MILLIS) {
                        this.mPreDayCollectTime = currentTime;
                        shouldConnect = true;
                        this.mCurrentCount = 0;
                    } else if (this.mCurrentCount >= 50) {
                        shouldConnect = false;
                    } else {
                        shouldConnect = true;
                        this.mCurrentCount++;
                    }
                    if (shouldConnect) {
                        try {
                            int sidlac;
                            int nidcid;
                            String plmn = this.mUserLoc[phoneId].mPlmn;
                            HashMap<String, String> data = new HashMap();
                            int bid = -1;
                            if (this.mUserLoc[phoneId].mIsCdma) {
                                CdmaCellLocation cdmaLoc = this.mUserLoc[phoneId].mCellLoc;
                                sidlac = cdmaLoc.getSystemId();
                                nidcid = cdmaLoc.getNetworkId();
                                bid = cdmaLoc.getBaseStationId();
                            } else {
                                GsmCellLocation gsmLoc = this.mUserLoc[phoneId].mCellLoc;
                                sidlac = gsmLoc.getLac();
                                nidcid = gsmLoc.getCid();
                            }
                            data.put("plmn", plmn);
                            data.put("sidlac", String.valueOf(sidlac));
                            data.put("nidcid", String.valueOf(nidcid));
                            data.put("bid", String.valueOf(bid));
                            data.put("ntp", String.valueOf(ntpTime));
                            data.put("nitzTime", String.valueOf(nitzTime));
                            Rlog.v(LOG_TAG, "plmn:" + plmn + " sidlac:" + sidlac + " nidcid:" + nidcid + " bid:" + bid);
                            VivoBigDataManager.collectData(this.mContext, MODULE_TAG.NITZ_TAG, data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NTP_TIMES);
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
    }

    private void unregisterBroadcast() {
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    protected void finalize() throws Throwable {
        unregisterBroadcast();
        super.finalize();
    }

    private synchronized boolean isNtpTimeValid(long ntpSaveTime, long ntpSaveAtTime) {
        this.mIsNtpChecked = true;
        if (ntpSaveTime == -1 || ntpSaveAtTime == -1) {
            return false;
        }
        if (this.mNitzTimeInfo != null) {
            NitzTimeInfo highPriorityNitzInfo = null;
            for (int i = 0; i < this.mPhoneCount; i++) {
                NitzTimeInfo nitzInfo = this.mNitzTimeInfo[i];
                if (nitzInfo != null && nitzInfo.mSavedTime > 0 && nitzInfo.mSavedAtTime > 0) {
                    if (highPriorityNitzInfo == null) {
                        highPriorityNitzInfo = nitzInfo;
                    } else if (nitzInfo.getPriority() > highPriorityNitzInfo.getPriority()) {
                        highPriorityNitzInfo = nitzInfo;
                    }
                }
            }
            if (highPriorityNitzInfo != null && Math.abs(highPriorityNitzInfo.getCurrentTimeMillis() - (ntpSaveTime + (SystemClock.elapsedRealtime() - ntpSaveAtTime))) > DAY_IN_MILLIS) {
                Rlog.v(LOG_TAG, "diff time is more than day,set invalid");
                return false;
            }
        }
        return true;
    }

    private long getCurrentNtpTimeMillis() {
        if (this.mIsNtpTimeUpdate) {
            return this.mNtpSaveTime + (SystemClock.elapsedRealtime() - this.mNtpSavedAtTime);
        }
        return -1;
    }
}
