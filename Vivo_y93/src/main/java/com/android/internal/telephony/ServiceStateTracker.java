package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.ServiceStateTable;
import android.telephony.CarrierConfigManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthLte;
import android.telephony.FtTelephony;
import android.telephony.FtTelephonyAdapter;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.CustomPlmnOperatorOverride.OperatorName;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.VivoNetLowlatency.Level;
import com.android.internal.telephony.VivoNetLowlatency.Radio;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ServiceStateTracker extends Handler {
    /* renamed from: -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static final /* synthetic */ int[] f3xba025bb = null;
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    public static final int CS_REJECT_CAUSE_ENABLED = 2001;
    public static final int CS_REJECT_CAUSE_NOTIFICATION = 111;
    private static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_GET_CDMA_LTELOC_DONE = 59;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_MCC_CHANGED = 56;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NOTIFY_IROAMING_DIALOG = 55;
    protected static final int EVENT_NOTIFY_SIGNAL_STRENGTH_CALL_END = 54;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_POWER_FROM_CARRIER = 51;
    protected static final int EVENT_RADIO_POWER_OFF_DONE = 53;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RIL_RECONNECTION = 57;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_POLL = 58;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_NOT_INSERTED = 52;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    protected static final String[] GMT_COUNTRY_CODES = new String[]{"bf", "ci", "eh", "fo", "gb", "gh", "gm", "gn", "gw", "ie", "lr", "is", "ma", "ml", "mr", "pt", "sl", "sn", "st", "tg"};
    private static final int INVALID_LTE_EARFCN = -1;
    public static final String INVALID_MCC = "000";
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 10000;
    private static final String LOG_TAG = "SST";
    private static final int MAX_NITZ_YEAR = 2037;
    public static final int MS_PER_HOUR = 3600000;
    public static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    public static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
    private static final int POLL_PERIOD_MILLIS = 3000;
    private static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected static final String[] SINGLE_TIMEZONE_COUNTRY_CODES = new String[]{"cn"};
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final boolean VDBG = false;
    public static final String WAKELOCK_TAG = "ServiceStateTracker";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String operatorNumericSave1 = " ";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String operatorNumericSave2 = " ";
    private static HandlerThread sHandlerThread;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String simNumericSave1 = " ";
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String simNumericSave2 = " ";
    private boolean DIsScreenOn = true;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private final String SIGNAL_THRESHOLD_XML_PATH = "/data/misc/radio/networkstate/signal_config.xml";
    private int csDropScreenon = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int cs_out_of_service_msg_count = 0;
    private boolean mAlarmSwitch = false;
    private final LocalLog mAttachLog = new LocalLog(10);
    protected RegistrantList mAttachedRegistrants = new RegistrantList();
    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.LOG_TAG, "Auto time state changed");
            ServiceStateTracker.this.revertToNitzTime();
        }
    };
    private ContentObserver mAutoTimeZoneObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.LOG_TAG, "Auto time zone state changed");
            ServiceStateTracker.this.revertToNitzTimeZone();
        }
    };
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private long mCSOutOfServiceTime = 0;
    private CarrierServiceStateTracker mCSST;
    protected final SpListener mCallStateSpListener = new SpListener();
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mCdmaBssWorking = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mCdmaDbm = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mCdmaDbmAvg = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mCdmaDownCounter = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mCdmaDownSum = 0;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    public CellLocation mCdmaLteCellLoc;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mCdmaRxPowerThresh = new int[]{-106, -99, -89};
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public CellLocation mCellLoc;
    private CommandsInterface mCi;
    private Handler mCollectionThreadHandler;
    private ContentResolver mCr;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected CollectionBean mCsOutOfService;
    private String mCurDataSpn = null;
    private String mCurPlmn = null;
    private boolean mCurShowPlmn = false;
    private boolean mCurShowSpn = false;
    private String mCurSpn = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected DataCollectionUtils mDataCollectionUtils;
    private RegistrantList mDataRegStateOrRatChangedRegistrants = new RegistrantList();
    private boolean mDataRoaming = false;
    private RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    private int mDefaultRoamingIndicator;
    private boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants = new RegistrantList();
    private boolean mDeviceShuttingDown = false;
    private boolean mDontPollSignalStrength = false;
    private ArrayList<Pair<Integer, Integer>> mEarfcnPairListForRsrpBoost = null;
    private boolean mEmergencyOnly = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mEvdoBssWorking = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mEvdoDbm = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mEvdoDbmAvg = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mEvdoDownCounter = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mEvdoDownSum = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mEvdoRxPowerThresh = new int[]{-106, -99, -89};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private FtTelephony mFtTel;
    private boolean mGotCountryCode = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mGsmAsuThresh = new int[]{5, 8, 11};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mGsmBssWorking = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mGsmDbm = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mGsmDbmAvg = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mGsmDownCounter = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mGsmDownSum = 0;
    private boolean mGsmRoaming = false;
    private HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    private IccRecords mIccRecords = null;
    private boolean mImsRegistered = false;
    private boolean mImsRegistrationOnOff = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                ServiceStateTracker.this.updateLteEarfcnLists();
            } else if (ServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                    ServiceStateTracker.this.updateSpnDisplay();
                } else if (intent.getAction().equals(ServiceStateTracker.ACTION_RADIO_OFF)) {
                    ServiceStateTracker.this.mAlarmSwitch = false;
                    ServiceStateTracker.this.powerOffRadioSafely(ServiceStateTracker.this.mPhone.mDcTracker);
                }
            } else {
                ServiceStateTracker.this.loge("Ignoring intent " + intent + " received on CDMA phone");
            }
        }
    };
    private boolean mIsEriTextLoaded = false;
    private boolean mIsInPrl;
    private boolean mIsInSignalPoll = false;
    private boolean mIsMinInfoReady = false;
    private boolean mIsModemTriggeredPollingPending = false;
    public boolean mIsOtherPhoneInCall = false;
    public boolean mIsPhoneInCall = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mIsSSInit;
    private boolean mIsScreenOn = true;
    private boolean mIsSubscriptionFromRuim = false;
    private List<CellInfo> mLastCellInfoList = null;
    private long mLastCellInfoListTime;
    private SignalStrength mLastSignalStrength = null;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mLteBssWorking = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mLteDownCounter = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mLteDownSum = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mLteRsrp = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mLteRsrpAvg = 0;
    private int mLteRsrpBoost = 0;
    private final Object mLteRsrpBoostLock = new Object();
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mLteRsrpThresh = new int[]{-115, -110, -97};
    private int mMaxDataCalls = 1;
    private String mMdn;
    private String mMin;
    private boolean mNeedFixZoneAfterNitz = false;
    private RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    private RegistrantList mNetworkDetachedRegistrants = new RegistrantList();
    private CellLocation mNewCellLoc;
    private int mNewMaxDataCalls = 1;
    private int mNewReasonDataDenied = -1;
    private int mNewRejectCode;
    private ServiceState mNewSS;
    private int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", 2000);
    private int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", 600000);
    private boolean mNitzUpdatedTime = false;
    private Notification mNotification;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener(this, null);
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private long mPSOutOfServiceTime = 0;
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private int mPendingRadioPowerOffAfterDataOffTag = 0;
    protected GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog = new LocalLog(10);
    protected int[] mPollingContext;
    private boolean mPowerOffDelayNeed = true;
    private String mPreIsoCountryCode = "";
    private int mPreferredNetworkType;
    private String mPrlVersion;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected CollectionBean mPsOutOfService;
    private RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    private boolean mRadioDisabledByCarrier = false;
    private PendingIntent mRadioOffIntent = null;
    private final LocalLog mRadioPowerLog = new LocalLog(20);
    private final LocalLog mRatLog = new LocalLog(20);
    private final RatRatcheter mRatRatcheter;
    private int mReasonDataDenied = -1;
    private String mRegistrationDeniedReason;
    private int mRegistrationState = -1;
    private int mRejectCode;
    private boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    private int mRoamingIndicator;
    private final LocalLog mRoamingLog = new LocalLog(10);
    public ServiceState mSS;
    private long mSavedAtTime;
    private long mSavedTime;
    private String mSavedTimeZone;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected VivoServiceStateTrackerHelper mServiceStateTrackerHelper;
    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private BroadcastReceiver mSignalIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    ServiceStateTracker.this.updateSignalThreshold();
                }
            }).start();
        }
    };
    private SignalStrength mSignalStrength;
    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private BroadcastReceiver mSpnIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ServiceStateTracker.this.log("action : " + action);
            if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                ServiceStateTracker.this.updateSpnDisplay(true);
            } else if (!"status.bar.update.plmn.spn".equals(action)) {
            } else {
                if (ServiceStateTracker.this.mServiceStateTrackerHelper == null || ServiceStateTracker.this.mServiceStateTrackerHelper.canUpdateSpnToCurrent()) {
                    Intent spnIntent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
                    spnIntent.putExtra("showSpn", ServiceStateTracker.this.mCurShowSpn);
                    spnIntent.putExtra("spn", ServiceStateTracker.this.mCurSpn);
                    spnIntent.putExtra("spnData", ServiceStateTracker.this.mCurDataSpn);
                    spnIntent.putExtra("showPlmn", ServiceStateTracker.this.mCurShowPlmn);
                    spnIntent.putExtra("plmn", ServiceStateTracker.this.mCurPlmn);
                    SubscriptionManager.putPhoneIdAndSubIdExtra(spnIntent, ServiceStateTracker.this.mPhone.getPhoneId());
                    ServiceStateTracker.this.mPhone.getContext().sendStickyBroadcastAsUser(spnIntent, UserHandle.ALL);
                }
            }
        }
    };
    private boolean mSpnUpdatePending = false;
    private boolean mStartedGprsRegCheck;
    private int mSubId = -1;
    private SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected boolean mTdBssWorking = false;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mTdDownCounter = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mTdDownSum = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mTdRscp = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int mTdRscpAvg = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mTdScdmaThresh = new int[]{-105, -98, -91};
    private final LocalLog mTimeLog = new LocalLog(15);
    private final LocalLog mTimeZoneLog = new LocalLog(15);
    private UiccCardApplication mUiccApplcation = null;
    private UiccController mUiccController = null;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private VivoPplmnCollect mVivoPplmnCollect;
    private boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private WakeLock mWakeLock;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int[] mWcdmaAsuThresh = new int[]{4, 7, 11};
    private boolean mZoneDst;
    private int mZoneOffset;
    private long mZoneTime;
    private int psDropScreenon = 0;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    protected int ps_out_of_service_msg_count = 0;
    private VivoNetLowlatency vivoNetLowlatency = null;

    @VivoHook(hookType = VivoHookType.CHANGE_ACCESS)
    protected class CellInfoResult {
        public List<CellInfo> list;
        public Object lockObj = new Object();

        protected CellInfoResult() {
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_CLASS)
    public class SpListener implements OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            int phoneId = ServiceStateTracker.this.mPhone.getPhoneId();
            int phone0CallState = VivoCallServiceStateHelper.VOICE_CALL_END;
            int phone1CallState = VivoCallServiceStateHelper.VOICE_CALL_END;
            if (VivoCallServiceStateHelper.PHONE0_CALL_STATE.equals(key) || VivoCallServiceStateHelper.PHONE1_CALL_STATE.equals(key)) {
                phone0CallState = sharedPreferences.getInt(VivoCallServiceStateHelper.PHONE0_CALL_STATE, -1);
                phone1CallState = sharedPreferences.getInt(VivoCallServiceStateHelper.PHONE1_CALL_STATE, -1);
                if (phoneId == 0) {
                    if (phone0CallState == VivoCallServiceStateHelper.VOICE_CALL_START) {
                        ServiceStateTracker.this.mIsPhoneInCall = true;
                        ServiceStateTracker.this.mIsOtherPhoneInCall = false;
                    } else if (phone1CallState == VivoCallServiceStateHelper.VOICE_CALL_START) {
                        ServiceStateTracker.this.mIsPhoneInCall = false;
                        ServiceStateTracker.this.mIsOtherPhoneInCall = true;
                    } else {
                        ServiceStateTracker.this.mIsPhoneInCall = false;
                        ServiceStateTracker.this.mIsOtherPhoneInCall = false;
                    }
                } else if (phone1CallState == VivoCallServiceStateHelper.VOICE_CALL_START) {
                    ServiceStateTracker.this.mIsPhoneInCall = true;
                    ServiceStateTracker.this.mIsOtherPhoneInCall = false;
                } else if (phone0CallState == VivoCallServiceStateHelper.VOICE_CALL_START) {
                    ServiceStateTracker.this.mIsPhoneInCall = false;
                    ServiceStateTracker.this.mIsOtherPhoneInCall = true;
                } else {
                    ServiceStateTracker.this.mIsPhoneInCall = false;
                    ServiceStateTracker.this.mIsOtherPhoneInCall = false;
                }
                if (ServiceStateTracker.this.mIsPhoneInCall || ServiceStateTracker.this.mIsOtherPhoneInCall) {
                    ServiceStateTracker.this.notifySignalStrengthforCallStart();
                } else {
                    ServiceStateTracker.this.notifySignalStrengthforCallEnd();
                }
            }
        }
    }

    private class SstSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        /* synthetic */ SstSubscriptionsChangedListener(ServiceStateTracker this$0, SstSubscriptionsChangedListener -this1) {
            this();
        }

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (ServiceStateTracker.this.mSubscriptionController.isActiveSubId(subId)) {
                    Context context = ServiceStateTracker.this.mPhone.getContext();
                    ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                    ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                    boolean restoreSelection = context.getResources().getBoolean(17957095) ^ 1;
                    ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                    if (ServiceStateTracker.this.mSpnUpdatePending) {
                        ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                        ServiceStateTracker.this.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, "");
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, "");
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, "");
                    if (!(TextUtils.isEmpty(oldNetworkSelection) && (TextUtils.isEmpty(oldNetworkSelectionName) ^ 1) == 0 && (TextUtils.isEmpty(oldNetworkSelectionShort) ^ 1) == 0)) {
                        Editor editor = sp.edit();
                        editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                        editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                        editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                        editor.remove(Phone.NETWORK_SELECTION_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                        editor.commit();
                    }
                    ServiceStateTracker.this.updateSpnDisplay();
                }
                ServiceStateTracker.this.mPhone.updateVoiceMail();
                if (ServiceStateTracker.this.mSubscriptionController.getSlotIndex(subId) == -1) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(52));
                }
            }
        }
    }

    /* renamed from: -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static /* synthetic */ int[] m3x804b995f() {
        if (f3xba025bb != null) {
            return f3xba025bb;
        }
        int[] iArr = new int[RadioState.values().length];
        try {
            iArr[RadioState.RADIO_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RadioState.RADIO_ON.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RadioState.RADIO_UNAVAILABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        f3xba025bb = iArr;
        return iArr;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mPhone = phone;
        this.mCi = ci;
        this.mServiceStateTrackerHelper = new VivoServiceStateTrackerHelper(this.mPhone);
        this.mRatRatcheter = new RatRatcheter(this.mPhone);
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17957059);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mRestrictedState = new RestrictedState();
        this.mCi.setMccChangedEvent(this, 56, null);
        this.mCi.registerForImsNetworkStateChanged(this, 46, null);
        this.mFtTel = FtTelephonyAdapter.getFtTelephony(this.mPhone.getContext());
        this.mCi.registerForRilConnected(this, 57, null);
        this.mCi.setOnSignalPoll(this, 58, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("status.bar.update.plmn.spn");
        phone.getContext().registerReceiver(this.mSpnIntentReceiver, filter);
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, WAKELOCK_TAG);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.registerForNetworkStateChanged(this, 2, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Global.getInt(this.mCr, "airplane_mode_on", 0);
        int enableCellularOnBoot = Global.getInt(this.mCr, "enable_cellular_on_boot", 1);
        boolean z = enableCellularOnBoot > 0 && airplaneMode <= 0;
        this.mDesiredPowerState = z;
        this.mRadioPowerLog.log("init : airplane mode = " + airplaneMode + " enableCellularOnBoot = " + enableCellularOnBoot);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time"), true, this.mAutoTimeObserver);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time_zone"), true, this.mAutoTimeZoneObserver);
        setSignalStrengthDefaultValues();
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(1, this, 51, null, false);
        Context context = this.mPhone.getContext();
        filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_RADIO_OFF);
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        this.mDataCollectionUtils = new DataCollectionUtils(phone.getContext());
        if (sHandlerThread == null) {
            log(" DataCollectionUtils   tid: " + Process.myTid() + " pid: " + Process.myPid());
            sHandlerThread = new HandlerThread("dataCollectionHandlerThread");
            sHandlerThread.start();
        }
        this.mCollectionThreadHandler = new Handler(sHandlerThread.getLooper());
        this.mPhone.notifyOtaspChanged(0);
        this.mCi.setOnRestrictedStateChanged(this, 23, null);
        updatePhoneType();
        this.mCSST = new CarrierServiceStateTracker(phone, this);
        registerForNetworkAttached(this.mCSST, 101, null);
        registerForNetworkDetached(this.mCSST, 102, null);
        registerForDataConnectionAttached(this.mCSST, 103, null);
        registerForDataConnectionDetached(this.mCSST, 104, null);
        this.mVivoPplmnCollect = new VivoPplmnCollect(context, this.mPhone, this.mPhone.getPhoneId());
        new Thread(new Runnable() {
            public void run() {
                ServiceStateTracker.this.updateSignalThreshold();
            }
        }).start();
        IntentFilter stFilter = new IntentFilter();
        stFilter.addAction("com.vivo.networkstate.feature_update_finish");
        phone.getContext().registerReceiver(this.mSignalIntentReceiver, stFilter);
        this.vivoNetLowlatency = new VivoNetLowlatency(context);
    }

    public void updatePhoneType() {
        if (this.mSS != null && this.mSS.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRegState() == 0) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzUpdatedTime = false;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCellLoc = new GsmCellLocation();
            this.mNewCellLoc = new GsmCellLocation();
            this.mCdmaLteCellLoc = new GsmCellLocation();
        } else {
            this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaLteCellLoc = new GsmCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mPhone.registerForEriFileLoaded(this, 36, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        notifyDataRegStateRilRadioTechnologyChanged();
        this.mPhone.getContext().getSharedPreferences(VivoCallServiceStateHelper.PHONE_CALL_STATE, 0).registerOnSharedPreferenceChangeListener(this.mCallStateSpListener);
    }

    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            setPowerStateToDesired();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:155:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0104 A:{SYNTHETIC, Splitter: B:24:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:157:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0189 A:{SYNTHETIC, Splitter: B:38:0x0189} */
    /* JADX WARNING: Removed duplicated region for block: B:159:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0244 A:{SYNTHETIC, Splitter: B:54:0x0244} */
    /* JADX WARNING: Removed duplicated region for block: B:161:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x02b9 A:{SYNTHETIC, Splitter: B:69:0x02b9} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x031b A:{SYNTHETIC, Splitter: B:85:0x031b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void updateSignalThreshold() {
        Exception e;
        FileNotFoundException e2;
        File file;
        XmlPullParserException e3;
        IOException e4;
        Throwable th;
        log("updateSignalThreshold");
        FileReader fileReader = null;
        try {
            File signalConfigFile = new File("/data/misc/radio/networkstate/signal_config.xml");
            try {
                Reader fileReader2 = new FileReader(signalConfigFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fileReader2);
                    XmlUtils.beginDocument(parser, "thresholds");
                    int configValid = Integer.parseInt(parser.getAttributeValue(null, "valid"));
                    log("configValid = " + configValid);
                    if (configValid == 1) {
                        while (true) {
                            XmlUtils.nextElement(parser);
                            if (!"rat".equals(parser.getName())) {
                                break;
                            }
                            String carrier = parser.getAttributeValue(null, "carrier");
                            int l1 = Integer.parseInt(parser.getAttributeValue(null, "l1"));
                            int l2 = Integer.parseInt(parser.getAttributeValue(null, "l2"));
                            int l3 = Integer.parseInt(parser.getAttributeValue(null, "l3"));
                            if (TextUtils.isEmpty(carrier)) {
                                log("updateSignalThreshold is null carrier = " + carrier);
                            } else if (l1 >= l2 || l1 >= l3 || l2 >= l3) {
                                log("updateSignalThreshold threshold is invalid, l1 = " + l1 + ", l2 = " + l2 + ", l3 = " + l3);
                            } else {
                                log("updateSignalThreshold carrier = " + carrier + ", l1 = " + l1 + ", l2 = " + l2 + ", l3 = " + l3);
                                if (carrier.equals("gsm")) {
                                    if (l1 >= 0) {
                                        this.mGsmAsuThresh[0] = l1;
                                        this.mGsmAsuThresh[1] = l2;
                                        this.mGsmAsuThresh[2] = l3;
                                    }
                                } else if (carrier.equals("wcdma")) {
                                    if (l1 >= 0) {
                                        this.mWcdmaAsuThresh[0] = l1;
                                        this.mWcdmaAsuThresh[1] = l2;
                                        this.mWcdmaAsuThresh[2] = l3;
                                    }
                                } else if (carrier.equals("tdscdma")) {
                                    if (l1 >= -120 && l3 <= -25) {
                                        this.mTdScdmaThresh[0] = l1;
                                        this.mTdScdmaThresh[1] = l2;
                                        this.mTdScdmaThresh[2] = l3;
                                    }
                                } else if (carrier.equals("cdma")) {
                                    if (l1 > -120 && l3 < 0) {
                                        this.mCdmaRxPowerThresh[0] = l1;
                                        this.mCdmaRxPowerThresh[1] = l2;
                                        this.mCdmaRxPowerThresh[2] = l3;
                                    }
                                } else if (carrier.equals("evdo")) {
                                    if (l1 > -120 && l3 < 0) {
                                        this.mEvdoRxPowerThresh[0] = l1;
                                        this.mEvdoRxPowerThresh[1] = l2;
                                        this.mEvdoRxPowerThresh[2] = l3;
                                    }
                                } else if (carrier.equals("lte") && l1 >= -140 && l3 <= -44) {
                                    this.mLteRsrpThresh[0] = l1;
                                    this.mLteRsrpThresh[1] = l2;
                                    this.mLteRsrpThresh[2] = l3;
                                }
                            }
                        }
                    }
                    if (fileReader2 != null) {
                        try {
                            fileReader2.close();
                        } catch (Exception e5) {
                            log("Finally exception e = " + e5);
                        }
                    }
                    Reader reader = fileReader2;
                } catch (FileNotFoundException e6) {
                    e2 = e6;
                    file = signalConfigFile;
                    fileReader = fileReader2;
                } catch (XmlPullParserException e7) {
                    e3 = e7;
                    file = signalConfigFile;
                    fileReader = fileReader2;
                } catch (IOException e8) {
                    e4 = e8;
                    file = signalConfigFile;
                    fileReader = fileReader2;
                } catch (Exception e9) {
                    e5 = e9;
                    file = signalConfigFile;
                    fileReader = fileReader2;
                } catch (Throwable th2) {
                    th = th2;
                    file = signalConfigFile;
                    fileReader = fileReader2;
                }
            } catch (FileNotFoundException e10) {
                e2 = e10;
                try {
                    log("FileNotFoundException e = " + e2);
                    if (fileReader == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (fileReader != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e11) {
                e3 = e11;
                log("XmlPullParserException e = " + e3);
                if (fileReader == null) {
                }
            } catch (IOException e12) {
                e4 = e12;
                log("IOException = " + e4);
                if (fileReader == null) {
                }
            } catch (Exception e13) {
                e5 = e13;
                log("Exception e = " + e5);
                if (fileReader == null) {
                }
            } catch (Throwable th4) {
                th = th4;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (Exception e52) {
                        log("Finally exception e = " + e52);
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e14) {
            e2 = e14;
            log("FileNotFoundException e = " + e2);
            if (fileReader == null) {
                try {
                    fileReader.close();
                } catch (Exception e522) {
                    log("Finally exception e = " + e522);
                }
            }
        } catch (XmlPullParserException e15) {
            e3 = e15;
            log("XmlPullParserException e = " + e3);
            if (fileReader == null) {
                try {
                    fileReader.close();
                } catch (Exception e5222) {
                    log("Finally exception e = " + e5222);
                }
            }
        } catch (IOException e16) {
            e4 = e16;
            log("IOException = " + e4);
            if (fileReader == null) {
                try {
                    fileReader.close();
                } catch (Exception e52222) {
                    log("Finally exception e = " + e52222);
                }
            }
        } catch (Exception e17) {
            e52222 = e17;
            log("Exception e = " + e52222);
            if (fileReader == null) {
                try {
                    fileReader.close();
                } catch (Exception e522222) {
                    log("Finally exception e = " + e522222);
                }
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void dispose() {
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        this.mPhone.getContext().unregisterReceiver(this.mSpnIntentReceiver);
        this.mPhone.getContext().unregisterReceiver(this.mSignalIntentReceiver);
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 1);
        this.mPhone.getContext().getSharedPreferences(VivoCallServiceStateHelper.PHONE_CALL_STATE, 0).unregisterOnSharedPreferenceChangeListener(this.mCallStateSpListener);
        this.mVivoPplmnCollect.dispose();
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return this.mRadioDisabledByCarrier ^ 1;
    }

    protected boolean notifySignalStrength() {
        if (this.mSignalStrength == null || (this.mSignalStrength.equals(this.mLastSignalStrength) ^ 1) == 0) {
            return false;
        }
        this.mLastSignalStrength = this.mSignalStrength;
        try {
            this.mPhone.notifySignalStrength();
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return false;
        }
    }

    protected void notifyDataRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilDataRadioTechnology();
        int drs = this.mSS.getDataRegState();
        log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(rat));
        this.mDataRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
    }

    protected void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
        }
    }

    protected void updatePhoneObject() {
        if (this.mPhone.getContext().getResources().getBoolean(17957040)) {
            boolean isRegistered = this.mSS.getVoiceRegState() != 0 ? this.mSS.getVoiceRegState() == 2 : true;
            if (isRegistered) {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
            } else {
                log("updatePhoneObject: Ignore update");
            }
        }
    }

    public void registerForVoiceRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOnRegistrants.add(r);
        if (this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOn(Handler h) {
        this.mVoiceRoamingOnRegistrants.remove(h);
    }

    public void registerForVoiceRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOffRegistrants.add(r);
        if (!this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOff(Handler h) {
        this.mVoiceRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOnRegistrants.add(r);
        if (this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOn(Handler h) {
        this.mDataRoamingOnRegistrants.remove(h);
    }

    public void registerForDataRoamingOff(Handler h, int what, Object obj, boolean notifyNow) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (notifyNow && (this.mSS.getDataRoaming() ^ 1) != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
    }

    public void setRadioPower(boolean power) {
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = enable ^ 1;
        setPowerStateToDesired();
    }

    public void enableSingleLocationUpdate() {
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    public void enableLocationUpdates() {
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantContinuousLocationUpdates = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    protected void disableSingleLocationUpdate() {
        this.mWantSingleLocationUpdate = false;
        if (!this.mWantSingleLocationUpdate && (this.mWantContinuousLocationUpdates ^ 1) != 0) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = false;
        if (!this.mWantSingleLocationUpdate && (this.mWantContinuousLocationUpdates ^ 1) != 0) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void processCellLocationInfo(CellLocation cellLocation, VoiceRegStateResult voiceRegStateResult) {
        if (this.mPhone.isPhoneTypeGsm()) {
            int psc = -1;
            int cid = -1;
            int lac = -1;
            switch (voiceRegStateResult.cellIdentity.cellInfoType) {
                case 1:
                    if (voiceRegStateResult.cellIdentity.cellIdentityGsm.size() == 1) {
                        CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) voiceRegStateResult.cellIdentity.cellIdentityGsm.get(0);
                        cid = cellIdentityGsm.cid;
                        lac = cellIdentityGsm.lac;
                        break;
                    }
                    break;
                case 3:
                    if (voiceRegStateResult.cellIdentity.cellIdentityLte.size() == 1) {
                        CellIdentityLte cellIdentityLte = (CellIdentityLte) voiceRegStateResult.cellIdentity.cellIdentityLte.get(0);
                        cid = cellIdentityLte.ci;
                        lac = cellIdentityLte.tac;
                        break;
                    }
                    break;
                case 4:
                    if (voiceRegStateResult.cellIdentity.cellIdentityWcdma.size() == 1) {
                        CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) voiceRegStateResult.cellIdentity.cellIdentityWcdma.get(0);
                        cid = cellIdentityWcdma.cid;
                        lac = cellIdentityWcdma.lac;
                        psc = cellIdentityWcdma.psc;
                        break;
                    }
                    break;
                case 5:
                    if (voiceRegStateResult.cellIdentity.cellIdentityTdscdma.size() == 1) {
                        CellIdentityTdscdma cellIdentityTdscdma = (CellIdentityTdscdma) voiceRegStateResult.cellIdentity.cellIdentityTdscdma.get(0);
                        cid = cellIdentityTdscdma.cid;
                        lac = cellIdentityTdscdma.lac;
                        break;
                    }
                    break;
            }
            log("EVENT_POLL_STATE_REGISTRATION cid = " + cid);
            if (lac == 65534 || cid == 268435455 || cid == Integer.MAX_VALUE || cid == -1) {
                log("unknown lac:" + lac + " or cid:" + cid);
                if (regCodeToServiceState(voiceRegStateResult.regState) == 1) {
                    ((GsmCellLocation) cellLocation).setLacAndCid(lac, cid);
                }
            } else {
                if (this.mSS != null) {
                    NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).checkCollectErrorTime(this.mPhone.getPhoneId(), this.mSS.getOperatorNumeric(), this.mNewCellLoc, false);
                }
                if (regCodeToServiceState(voiceRegStateResult.regState) != 1) {
                    ((GsmCellLocation) cellLocation).setLacAndCid(lac, cid);
                }
            }
            ((GsmCellLocation) cellLocation).setPsc(psc);
            return;
        }
        int baseStationId = -1;
        int baseStationLatitude = Integer.MAX_VALUE;
        int baseStationLongitude = Integer.MAX_VALUE;
        int systemId = 0;
        int networkId = 0;
        switch (voiceRegStateResult.cellIdentity.cellInfoType) {
            case 2:
                if (voiceRegStateResult.cellIdentity.cellIdentityCdma.size() == 1) {
                    CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) voiceRegStateResult.cellIdentity.cellIdentityCdma.get(0);
                    baseStationId = cellIdentityCdma.baseStationId;
                    baseStationLatitude = cellIdentityCdma.latitude;
                    baseStationLongitude = cellIdentityCdma.longitude;
                    systemId = cellIdentityCdma.systemId;
                    networkId = cellIdentityCdma.networkId;
                    break;
                }
                break;
        }
        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
            baseStationLatitude = Integer.MAX_VALUE;
            baseStationLongitude = Integer.MAX_VALUE;
        }
        if (baseStationId > 0 && systemId > 0 && networkId >= 0 && this.mSS != null) {
            NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).checkCollectErrorTime(this.mPhone.getPhoneId(), this.mSS.getOperatorNumeric(), this.mNewCellLoc, true);
        }
        ((CdmaCellLocation) cellLocation).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
    }

    private void vivoProcessCdmaLteCellLocationInfo(DataRegStateResult dataRegStateResult) {
        if (dataRegStateResult != null) {
            int psc = -1;
            int cid = -1;
            int lac = -1;
            switch (dataRegStateResult.cellIdentity.cellInfoType) {
                case 1:
                    if (dataRegStateResult.cellIdentity.cellIdentityGsm.size() == 1) {
                        CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) dataRegStateResult.cellIdentity.cellIdentityGsm.get(0);
                        cid = cellIdentityGsm.cid;
                        lac = cellIdentityGsm.lac;
                        break;
                    }
                    break;
                case 3:
                    if (dataRegStateResult.cellIdentity.cellIdentityLte.size() == 1) {
                        CellIdentityLte cellIdentityLte = (CellIdentityLte) dataRegStateResult.cellIdentity.cellIdentityLte.get(0);
                        cid = cellIdentityLte.ci;
                        lac = cellIdentityLte.tac;
                        break;
                    }
                    break;
                case 4:
                    if (dataRegStateResult.cellIdentity.cellIdentityWcdma.size() == 1) {
                        CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) dataRegStateResult.cellIdentity.cellIdentityWcdma.get(0);
                        cid = cellIdentityWcdma.cid;
                        lac = cellIdentityWcdma.lac;
                        psc = cellIdentityWcdma.psc;
                        break;
                    }
                    break;
                case 5:
                    if (dataRegStateResult.cellIdentity.cellIdentityTdscdma.size() == 1) {
                        CellIdentityTdscdma cellIdentityTdscdma = (CellIdentityTdscdma) dataRegStateResult.cellIdentity.cellIdentityTdscdma.get(0);
                        cid = cellIdentityTdscdma.cid;
                        lac = cellIdentityTdscdma.lac;
                        break;
                    }
                    break;
            }
            if (this.mCdmaLteCellLoc != null && (this.mCdmaLteCellLoc instanceof GsmCellLocation)) {
                ((GsmCellLocation) this.mCdmaLteCellLoc).setLacAndCid(lac, cid);
                ((GsmCellLocation) this.mCdmaLteCellLoc).setPsc(psc);
            }
        }
    }

    private int getLteEarfcn(DataRegStateResult dataRegStateResult) {
        switch (dataRegStateResult.cellIdentity.cellInfoType) {
            case 3:
                if (dataRegStateResult.cellIdentity.cellIdentityLte.size() == 1) {
                    return ((CellIdentityLte) dataRegStateResult.cellIdentity.cellIdentityLte.get(0)).earfcn;
                }
                return -1;
            default:
                return -1;
        }
    }

    /* JADX WARNING: Missing block: B:64:0x0274, code:
            return;
     */
    /* JADX WARNING: Missing block: B:120:0x0476, code:
            if (com.android.internal.telephony.VivoNetLowlatency.isMobileNetworkType(r34.mPhone.getContext()) == false) goto L_0x041b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
            case 50:
                if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == RadioState.RADIO_ON) {
                    handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                }
                setPowerStateToDesired();
                modemTriggeredPollState();
                if (this.mCi.getRadioState() != RadioState.RADIO_ON) {
                    this.mDataCollectionUtils.setRadioUnavailableflag(true);
                    break;
                }
                break;
            case 2:
                modemTriggeredPollState();
                break;
            case 3:
                if (this.mPhone != null && this.mPhone.getIccCard() != null && (this.mPhone.getIccCard().hasIccCard() ^ 1) == 0 && this.mCi.getRadioState().isOn()) {
                    onSignalStrengthResult((AsyncResult) msg.obj);
                    break;
                }
                return;
                break;
            case 4:
            case 5:
            case 6:
                handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                break;
            case 10:
                if (!this.mIsScreenOn || this.mUiccApplcation == null) {
                    this.mIsInSignalPoll = false;
                } else {
                    this.mCi.getSignalStrength(obtainMessage(3));
                    queueNextSignalStrengthPoll(true);
                }
                if (!(this.mSubscriptionManager == null || this.mPhone == null || this.vivoNetLowlatency == null)) {
                    int dataPhoneId = this.mPhone.getContext().getSharedPreferences("com.android.phone_preferences", 0).getInt("DEFAULT_DATA_SLOT_ID", 0);
                    this.vivoNetLowlatency.setDataPhoneId(dataPhoneId);
                    if (dataPhoneId == this.mPhone.getPhoneId()) {
                        VivoNetLowlatency vivoNetLowlatency = this.vivoNetLowlatency;
                        if (VivoNetLowlatency.isAutoMode()) {
                            vivoNetLowlatency = this.vivoNetLowlatency;
                            if (1 < VivoNetLowlatency.getUplinkLatencyLevel().getLevel()) {
                                if (this.mIsScreenOn && this.mUiccApplcation != null) {
                                    vivoNetLowlatency = this.vivoNetLowlatency;
                                    if (!VivoNetLowlatency.isAirplaneMode(this.mPhone.getContext()) && this.vivoNetLowlatency.isGamingOnFrontDesk()) {
                                        vivoNetLowlatency = this.vivoNetLowlatency;
                                        break;
                                    }
                                }
                                vivoNetLowlatency = this.vivoNetLowlatency;
                                VivoNetLowlatency.setLatencyLevel(this.mPhone.getContext(), Radio.WWAN, Level.L1, Level.L1);
                                break;
                            }
                        }
                    }
                }
                break;
            case 11:
                ar = (AsyncResult) msg.obj;
                setTimeFromNITZString(((Object[]) ar.result)[0], ((Long) ((Object[]) ar.result)[1]).longValue());
                break;
            case 12:
                log("ignore EVENT_NITZ_TIME_SIGNAL_STRENGTH_UPDATE");
                break;
            case 14:
                log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                ar = (AsyncResult) msg.obj;
                if (!this.mPhone.isPhoneTypeGsm()) {
                    if (ar.exception == null && ar.result != null) {
                        if (ar.result[0] == 1) {
                            this.mPhone.setNetworkSelectionModeAutomatic(null);
                            break;
                        }
                    }
                    log("Unable to getNetworkSelectionMode");
                    break;
                }
                handlePollStateResult(msg.what, ar);
                break;
                break;
            case 15:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    processCellLocationInfo(this.mCellLoc, (VoiceRegStateResult) ar.result);
                    this.mPhone.notifyLocationChanged();
                }
                disableSingleLocationUpdate();
                break;
            case 16:
                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                updatePhoneObject();
                updateOtaspState();
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateSpnDisplay();
                }
                this.mDataCollectionUtils.reportIccId(this.mPhone.getPhoneId());
                pollState();
                break;
            case 17:
                this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                pollState();
                if (!this.mIsInSignalPoll) {
                    queueNextSignalStrengthPoll(false);
                    break;
                }
                break;
            case 18:
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mCi.getVoiceRegistrationState(obtainMessage(15, null));
                    if (this.mCdmaLteCellLoc != null) {
                        this.mCi.getDataRegistrationState(obtainMessage(59, null));
                        break;
                    }
                }
                break;
            case 19:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPreferredNetworkType = ((int[]) ar.result)[0];
                } else {
                    this.mPreferredNetworkType = 7;
                }
                this.mCi.setPreferredNetworkType(22, obtainMessage(20, ar.userObj));
                break;
            case 20:
                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                break;
            case 21:
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                    break;
                }
                break;
            case 22:
                if (this.mPhone.isPhoneTypeGsm() && this.mSS != null) {
                    if ((isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState()) ^ 1) != 0) {
                        GsmCellLocation loc = (GsmCellLocation) this.mPhone.getCellLocation();
                        Object[] objArr = new Object[2];
                        objArr[0] = this.mSS.getOperatorNumeric();
                        objArr[1] = Integer.valueOf(loc != null ? loc.getCid() : -1);
                        EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, objArr);
                        this.mReportedGprsNoReg = true;
                    }
                }
                this.mStartedGprsRegCheck = false;
                break;
            case 23:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RESTRICTED_STATE_CHANGED");
                    onRestrictedStateChanged((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 26:
                if (this.mPhone.getLteOnCdmaMode() == 1) {
                    log("Receive EVENT_RUIM_READY");
                    pollState();
                } else {
                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                if (!this.mIsInSignalPoll) {
                    queueNextSignalStrengthPoll(false);
                    break;
                }
                break;
            case 27:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                    updatePhoneObject();
                    if (this.mPhone.isPhoneTypeCdma()) {
                        updateSpnDisplay();
                    } else {
                        RuimRecords ruim = (RuimRecords) this.mIccRecords;
                        if (ruim != null) {
                            if (ruim.isProvisioned()) {
                                this.mMdn = ruim.getMdn();
                                this.mMin = ruim.getMin();
                                parseSidNid(ruim.getSid(), ruim.getNid());
                                this.mPrlVersion = ruim.getPrlVersion();
                                this.mIsMinInfoReady = true;
                            }
                            updateOtaspState();
                            notifyCdmaSubscriptionInfoReady();
                        }
                        pollState();
                    }
                    this.mDataCollectionUtils.reportIccId(this.mPhone.getPhoneId());
                    break;
                }
                break;
            case 34:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] cdmaSubscription = ar.result;
                        if (cdmaSubscription != null && cdmaSubscription.length >= 5) {
                            this.mMdn = cdmaSubscription[0];
                            parseSidNid(cdmaSubscription[1], cdmaSubscription[2]);
                            this.mMin = cdmaSubscription[3];
                            this.mPrlVersion = cdmaSubscription[4];
                            log("GET_CDMA_SUBSCRIPTION: MDN=" + this.mMdn);
                            this.mIsMinInfoReady = true;
                            updateOtaspState();
                            notifyCdmaSubscriptionInfoReady();
                            if (!this.mIsSubscriptionFromRuim && this.mIccRecords != null) {
                                log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                                this.mIccRecords.setImsi(getImsi());
                                break;
                            }
                            log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                            break;
                        }
                        log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                        break;
                    }
                }
                break;
            case 35:
                updatePhoneObject();
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                getSubscriptionInfoAndStartPollingThreads();
                break;
            case 36:
                log("ERI file has been loaded, repolling.");
                pollState();
                break;
            case 37:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int otaStatus = ((int[]) ar.result)[0];
                    if (otaStatus == 8 || otaStatus == 10) {
                        log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                        this.mCi.getCDMASubscription(obtainMessage(34));
                        break;
                    }
                }
                break;
            case 38:
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff || msg.arg1 != this.mPendingRadioPowerOffAfterDataOffTag) {
                        log("EVENT_SET_RADIO_OFF is stale arg1=" + msg.arg1 + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                        break;
                    }
                    log("EVENT_SET_RADIO_OFF, turn radio off now.");
                    hangupAndPowerOff();
                    this.mPendingRadioPowerOffAfterDataOffTag++;
                    this.mPendingRadioPowerOffAfterDataOff = false;
                    break;
                }
                break;
            case 39:
                handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                break;
            case 40:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPrlVersion = Integer.toString(((int[]) ar.result)[0]);
                    break;
                }
                break;
            case 42:
                onUpdateIccAvailability();
                break;
            case 43:
                ar = msg.obj;
                CellInfoResult result = ar.userObj;
                synchronized (result.lockObj) {
                    if (ar.exception != null) {
                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar.exception);
                        result.list = null;
                    } else {
                        result.list = (List) ar.result;
                    }
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = result.list;
                    result.lockObj.notify();
                    updateByCellInfo();
                }
            case 44:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    List<CellInfo> list = ar.result;
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = list;
                    this.mPhone.notifyCellInfo(list);
                    updateByCellInfo();
                    break;
                }
                log("EVENT_UNSOL_CELL_INFO_LIST: error ignoring, e=" + ar.exception);
                break;
            case 45:
                log("EVENT_CHANGE_IMS_STATE:");
                setPowerStateToDesired();
                break;
            case 46:
                this.mCi.getImsRegistrationState(obtainMessage(47));
                break;
            case 47:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImsRegistered = ((int[]) ar.result)[0] == 1;
                    break;
                }
                break;
            case 48:
                log("EVENT_IMS_CAPABILITY_CHANGED");
                updateSpnDisplay();
                break;
            case 49:
                ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff) {
                        log("EVENT_ALL_DATA_DISCONNECTED is stale");
                        break;
                    }
                    log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                    hangupAndPowerOff();
                    this.mPendingRadioPowerOffAfterDataOff = false;
                    break;
                }
            case 51:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    boolean enable = ((Boolean) ar.result).booleanValue();
                    log("EVENT_RADIO_POWER_FROM_CARRIER: " + enable);
                    setRadioPowerFromCarrier(enable);
                    break;
                }
                break;
            case 52:
                log("EVENT_SIM_NOT_INSERTED");
                cancelAllNotifications();
                this.mMdn = null;
                this.mMin = null;
                this.mIsMinInfoReady = false;
                break;
            case 53:
                log("EVENT_RADIO_POWER_OFF_DONE");
                if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
                    this.mCi.requestShutdown(null);
                    break;
                }
            case 54:
                log("EVENT_NOTIFY_SIGNAL_STRENGTH_CALL_END");
                notifySignalStrength();
                this.mServiceStateTrackerHelper.notifyServiceStateChanged(this.mSS);
                updateSpnDisplay(true);
                break;
            case 55:
                log("EVENT_NOTIFY_IROAMING_DIALOG");
                String IsoCountryCode = SystemProperties.get("gsm.vivo.countrycode", "");
                if (!"cn".equals(IsoCountryCode) && IsoCountryCode.equals(this.mPreIsoCountryCode)) {
                    notifyShowIroamingDialog();
                    break;
                }
            case 56:
                log("EVENT_MCC_CHANGED");
                byte[] responseData = (byte[]) ((AsyncResult) msg.obj).result;
                if (responseData != null && responseData.length >= 3) {
                    String str = new String(new byte[]{responseData[0], responseData[1], responseData[2]});
                    log("mcc changed, mcc=" + str);
                    updateMcc(str);
                    break;
                }
            case 57:
                log("EVENT_RIL_RECONNECTION");
                this.mDataCollectionUtils.setRilReconnectionFlag(true);
                break;
            case 58:
                log("EVENT_SIGNAL_POLL");
                int signalTag = ((Integer) ((AsyncResult) msg.obj).result).intValue();
                if (signalTag != 1) {
                    if (signalTag == 0) {
                        log("phoneId" + getPhoneId() + "--->end signal poll");
                        this.mIsScreenOn = false;
                        break;
                    }
                }
                this.mIsScreenOn = true;
                if (!this.mIsInSignalPoll) {
                    log("phoneId" + getPhoneId() + "--->start signal poll");
                    queueNextSignalStrengthPoll(false);
                    break;
                }
                break;
            case 59:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && this.mCdmaLteCellLoc != null) {
                    vivoProcessCdmaLteCellLocationInfo((DataRegStateResult) ar.result);
                    break;
                }
            default:
                log("Unhandled message with number: " + msg.what);
                break;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void updateByCellInfo() {
        if (isInvalidOperatorNumeric(this.mSS.getOperatorNumeric()) && this.mLastCellInfoList != null && this.mLastCellInfoList.size() > 0) {
            int mcc = Integer.MAX_VALUE;
            CellInfo ci = (CellInfo) this.mLastCellInfoList.get(0);
            if (ci instanceof CellInfoGsm) {
                mcc = ((CellInfoGsm) ci).getCellIdentity().getMcc();
            } else if (ci instanceof CellInfoWcdma) {
                mcc = ((CellInfoWcdma) ci).getCellIdentity().getMcc();
            } else if (ci instanceof CellInfoLte) {
                mcc = ((CellInfoLte) ci).getCellIdentity().getMcc();
            }
            if (mcc != Integer.MAX_VALUE && mcc >= 100 && mcc < 1000) {
                updateMcc(String.valueOf(mcc));
                String isoCountryCode = MccTable.countryCodeForMcc(mcc);
                log("updateByCellInfo isoCountryCode= " + isoCountryCode + ", mSavedTimeZone= " + this.mSavedTimeZone);
                String zone = null;
                if (updateIsoCountryCode(isoCountryCode)) {
                    zone = getTimeZoneByIsoCountryCode(isoCountryCode);
                } else if (this.mSavedTimeZone == null && (TextUtils.isEmpty(isoCountryCode) ^ 1) != 0) {
                    zone = getTimeZoneByIsoCountryCode(isoCountryCode);
                }
                if (zone != null) {
                    if (getAutoTimeZone() && (this.mSavedTimeZone == null || (this.mSavedTimeZone.equals(zone) ^ 1) != 0)) {
                        setAndBroadcastNetworkSetTimeZone(zone);
                    }
                    saveNitzTimeZone(zone);
                }
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void updateMcc(String mcc) {
        if (TextUtils.isEmpty(mcc)) {
            log("updateMcc invalid mcc.");
            return;
        }
        String isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
        if (TextUtils.isEmpty(isoCountryCode)) {
            log("updateMcc invalid isoCountryCode.");
            return;
        }
        String preMcc = SystemProperties.get("persist.radio.vivo.mcc", "");
        if (!TextUtils.equals(preMcc, mcc)) {
            SystemProperties.set("persist.radio.vivo.mcc", mcc);
            String preIsoCountryCode = "";
            if (preMcc != null && preMcc.length() == 3) {
                try {
                    preIsoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(preMcc));
                } catch (NumberFormatException ex) {
                    loge("countryCodeForMcc error" + ex);
                }
            }
            if (!isoCountryCode.equals(preIsoCountryCode)) {
                notifyUserCountryChange();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private boolean updateIsoCountryCode(String isoCountryCode) {
        if (TextUtils.isEmpty(isoCountryCode) || "".equals(isoCountryCode)) {
            log("updateIsoCountryCode invalid iso.");
            return false;
        }
        boolean isUpdated = false;
        if (!TextUtils.equals(SystemProperties.get("gsm.vivo.countrycode", ""), isoCountryCode)) {
            log("updateIsoCountryCode isoCountryCode = " + isoCountryCode);
            SystemProperties.set("gsm.vivo.countrycode", isoCountryCode);
            System.putString(this.mPhone.getContext().getContentResolver(), "device_current_country", isoCountryCode);
            isUpdated = true;
            if (!"cn".equals(isoCountryCode) && this.mServiceStateTrackerHelper.isShowIroamingDialog(isoCountryCode)) {
                this.mPreIsoCountryCode = isoCountryCode;
                Message msg = Message.obtain(this);
                msg.what = 55;
                if (sendMessageDelayed(msg, 120000)) {
                    log("Wait upto 2min for notifyShowIroamingDialog");
                }
            }
        }
        return isUpdated;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private String getTimeZoneByIsoCountryCode(String isoCountryCode) {
        List<String> uniqueZones = TimeUtils.getTimeZoneIdsWithUniqueOffsets(isoCountryCode);
        if (uniqueZones.size() != 1 && (uniqueZones.size() <= 1 || Arrays.binarySearch(SINGLE_TIMEZONE_COUNTRY_CODES, isoCountryCode) < 0)) {
            return null;
        }
        String zone = (String) uniqueZones.get(0);
        log("updateByCellInfo zone = " + zone);
        return zone;
    }

    protected boolean isSidsAllZeros() {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (i != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (sid == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public String getImsi() {
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (TextUtils.isEmpty(operatorNumeric) || (TextUtils.isEmpty(getCdmaMin()) ^ 1) == 0) {
            return null;
        }
        return operatorNumeric + getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        if (!this.mPhone.getIccRecordsLoaded()) {
            log("getOtasp: otasp uninitialized due to sim not loaded");
            return 0;
        } else if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return 3;
        } else if (this.mIsSubscriptionFromRuim && this.mMin == null) {
            return 2;
        } else {
            int provisioningState;
            if (this.mMin == null || this.mMin.length() < 6) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            } else {
                provisioningState = 3;
            }
            log("getOtasp: state=" + provisioningState);
            return provisioningState;
        }
    }

    protected void parseSidNid(String sidStr, String nidStr) {
        int i;
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (i = 0; i < sid.length; i++) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (i = 0; i < nid.length; i++) {
                try {
                    this.mHomeNetworkId[i] = Integer.parseInt(nid[i]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: NID=" + nidStr);
    }

    protected void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    protected Phone getPhone() {
        return this.mPhone;
    }

    protected void handlePollStateResult(int what, AsyncResult ar) {
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            iArr[0] = iArr[0] - 1;
            if (this.mPollingContext[0] == 0) {
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                    this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        boolean isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS);
                        if (isRoamingBetweenOperators != this.mNewSS.getVoiceRoaming()) {
                            log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                            this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
                        }
                    }
                    if (ServiceState.isCdma(this.mNewSS.getRilDataRadioTechnology())) {
                        if (this.mNewSS.getVoiceRegState() == 0) {
                            boolean isVoiceRoaming = this.mNewSS.getVoiceRoaming();
                            if (this.mNewSS.getDataRoaming() != isVoiceRoaming) {
                                log("Data roaming != Voice roaming. Override data roaming to " + isVoiceRoaming);
                                this.mNewSS.setDataRoaming(isVoiceRoaming);
                            }
                        }
                    }
                    this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                    this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                    boolean isPrlLoaded = true;
                    if (TextUtils.isEmpty(this.mPrlVersion)) {
                        isPrlLoaded = false;
                    }
                    if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                        log("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                        this.mNewSS.setCdmaRoamingIndicator(1);
                    } else if (!isSidsAllZeros()) {
                        if (!namMatch && (this.mIsInPrl ^ 1) != 0) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || (this.mIsInPrl ^ 1) == 0) {
                            if (!namMatch && this.mIsInPrl) {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            } else if (this.mRoamingIndicator <= 2) {
                                this.mNewSS.setCdmaRoamingIndicator(1);
                            } else {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            }
                        } else if (ServiceState.isLte(this.mNewSS.getRilVoiceRadioTechnology())) {
                            log("Turn off roaming indicator as voice is LTE");
                            this.mNewSS.setCdmaRoamingIndicator(1);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(2);
                        }
                    }
                    int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                    this.mNewSS.setCdmaEriIconIndex(this.mPhone.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mPhone.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                pollStateDone();
            }
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private int swithToCustomizedType(int nwType) {
        if ((TelephonyPhoneUtils.sIsCMCCEntry || (!TelephonyPhoneUtils.CRACK_SHOW3G && TelephonyPhoneUtils.NEED_LOCKDDS)) && ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).isMultiSimEnabled()) {
            int phone1SubId = this.mSubscriptionController.getSubIdUsingPhoneId(0);
            int phone2SubId = this.mSubscriptionController.getSubIdUsingPhoneId(1);
            log("CMCCEntry phone1SubId: " + phone1SubId + ", phone2SubId: " + phone2SubId);
            if (SubscriptionManager.isValidSubscriptionId(phone1SubId) && SubscriptionManager.isValidSubscriptionId(phone2SubId) && this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId() && nwType == 3 && (TelephonyPhoneUtils.sIsCMCCEntry || (!TelephonyPhoneUtils.isCNYDCracked() && TelephonyPhoneUtils.isInChina()))) {
                return 16;
            }
        }
        return nwType;
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming ? isSameOperatorNameFromSimAndSS(s) ^ 1 : false;
    }

    private int getRegStateFromHalRegState(int regState) {
        switch (regState) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 10:
                return 10;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            default:
                return 0;
        }
    }

    protected void handlePollStateResultMessage(int what, AsyncResult ar) {
        switch (what) {
            case 4:
                VoiceRegStateResult voiceRegStateResult = ar.result;
                int registrationState = getRegStateFromHalRegState(voiceRegStateResult.regState);
                int cssIndicator = voiceRegStateResult.cssSupported ? 1 : 0;
                this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                voiceRegStateResult.rat = swithToCustomizedType(voiceRegStateResult.rat);
                this.mNewSS.setRilVoiceRadioTechnology(voiceRegStateResult.rat);
                int reasonForDenial = voiceRegStateResult.reasonForDenial;
                if (reasonForDenial != 0) {
                    DataCollectionUtils.storeDeniedReason(20, this.mPhone.getPhoneId(), registrationState, reasonForDenial);
                }
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mGsmRoaming = regCodeIsRoaming(registrationState);
                    this.mNewRejectCode = reasonForDenial;
                    this.mNewSS.setCssIndicator(cssIndicator);
                    boolean isVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17957059);
                    if ((registrationState == 13 || registrationState == 10 || registrationState == 12 || registrationState == 14) && isVoiceCapable) {
                        this.mEmergencyOnly = true;
                    } else {
                        this.mEmergencyOnly = false;
                    }
                    if (this.mSS != null) {
                        NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).checkCollectErrorTime(this.mPhone.getPhoneId(), this.mSS.getOperatorNumeric(), this.mNewCellLoc, false);
                    }
                } else {
                    boolean cdmaRoaming;
                    int roamingIndicator = voiceRegStateResult.roamingIndicator;
                    int systemIsInPrl = voiceRegStateResult.systemIsInPrl;
                    int defaultRoamingIndicator = voiceRegStateResult.defaultRoamingIndicator;
                    this.mRegistrationState = registrationState;
                    if (regCodeIsRoaming(registrationState)) {
                        cdmaRoaming = isRoamIndForHomeSystem(Integer.toString(roamingIndicator)) ^ 1;
                    } else {
                        cdmaRoaming = false;
                    }
                    this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators(cdmaRoaming, this.mNewSS));
                    this.mNewSS.setCssIndicator(cssIndicator);
                    this.mRoamingIndicator = roamingIndicator;
                    this.mIsInPrl = systemIsInPrl != 0;
                    this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                    int systemId = 0;
                    int networkId = 0;
                    if (voiceRegStateResult.cellIdentity.cellInfoType == 2 && voiceRegStateResult.cellIdentity.cellIdentityCdma.size() == 1) {
                        CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) voiceRegStateResult.cellIdentity.cellIdentityCdma.get(0);
                        systemId = cellIdentityCdma.systemId;
                        networkId = cellIdentityCdma.networkId;
                    }
                    this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                    if (reasonForDenial == 0) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                    } else if (reasonForDenial == 1) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                    } else {
                        this.mRegistrationDeniedReason = "";
                    }
                    if (this.mRegistrationState == 3) {
                        log("Registration denied, " + this.mRegistrationDeniedReason);
                    }
                }
                processCellLocationInfo(this.mNewCellLoc, voiceRegStateResult);
                log("handlPollVoiceRegResultMessage: regState=" + registrationState + " radioTechnology=" + voiceRegStateResult.rat);
                return;
            case 5:
                DataRegStateResult dataRegStateResult = ar.result;
                int regState = getRegStateFromHalRegState(dataRegStateResult.regState);
                int dataRegState = regCodeToServiceState(regState);
                int newDataRat = dataRegStateResult.rat;
                this.mNewSS.setDataRegState(dataRegState);
                this.mNewSS.setRilDataRadioTechnology(newDataRat);
                if (dataRegStateResult.reasonDataDenied != 0) {
                    DataCollectionUtils.storeDeniedReason(21, this.mPhone.getPhoneId(), regState, dataRegStateResult.reasonDataDenied);
                }
                boolean isDataRoaming;
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mNewReasonDataDenied = dataRegStateResult.reasonDataDenied;
                    this.mNewMaxDataCalls = dataRegStateResult.maxDataCalls;
                    this.mDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoamingFromRegistration(this.mDataRoaming);
                    log("handlPollStateResultMessage: GsmSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                } else if (this.mPhone.isPhoneTypeCdma()) {
                    isDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoaming(isDataRoaming);
                    this.mNewSS.setDataRoamingFromRegistration(isDataRoaming);
                    log("handlPollStateResultMessage: cdma setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                } else {
                    int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                    if ((oldDataRAT == 0 && newDataRat != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat)))) {
                        this.mCi.getSignalStrength(obtainMessage(3));
                    }
                    isDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoaming(isDataRoaming);
                    this.mNewSS.setDataRoamingFromRegistration(isDataRoaming);
                    log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                }
                updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(dataRegStateResult));
                if (this.mCdmaLteCellLoc != null) {
                    vivoProcessCdmaLteCellLocationInfo(dataRegStateResult);
                    return;
                }
                return;
            case 6:
                String[] opNames;
                String brandOverride;
                if (this.mPhone.isPhoneTypeGsm()) {
                    opNames = (String[]) ar.result;
                    if (opNames != null && opNames.length >= 3) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                            return;
                        }
                        opNames = updateOperatorName(opNames);
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                    return;
                }
                opNames = (String[]) ar.result;
                if (opNames == null || opNames.length < 3) {
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                    return;
                }
                if (opNames[2] == null || opNames[2].length() < 5 || "00000".equals(opNames[2])) {
                    opNames[2] = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "00000");
                    log("RIL_REQUEST_OPERATOR.response[2], the numeric,  is bad. Using SystemProperties 'ro.cdma.home.operator.numeric'= " + opNames[2]);
                }
                if (this.mIsSubscriptionFromRuim) {
                    brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                    if (brandOverride != null) {
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        return;
                    }
                    opNames = updateOperatorName(opNames);
                    this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                    return;
                }
                this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                return;
            case 14:
                int[] ints = ar.result;
                this.mNewSS.setIsManualSelection(ints[0] == 1);
                if (ints[0] == 1 && this.mPhone.shouldForceAutoNetworkSelect()) {
                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                    log(" Forcing Automatic Network Selection, manual selection is not allowed");
                    return;
                }
                return;
            default:
                loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
                return;
        }
    }

    private boolean isRoamIndForHomeSystem(String roamInd) {
        String[] homeRoamIndicators = Resources.getSystem().getStringArray(17235990);
        log("isRoamIndForHomeSystem: homeRoamIndicators=" + Arrays.toString(homeRoamIndicators));
        if (homeRoamIndicators != null) {
            for (String homeRoamInd : homeRoamIndicators) {
                if (homeRoamInd.equals(roamInd)) {
                    return true;
                }
            }
            log("isRoamIndForHomeSystem: No match found against list for roamInd=" + roamInd);
            return false;
        }
        log("isRoamIndForHomeSystem: No list found");
        return false;
    }

    protected void updateRoamingState() {
        CarrierConfigManager configLoader;
        PersistableBundle b;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean roaming = !this.mGsmRoaming ? this.mDataRoaming : true;
            if (this.mGsmRoaming && (isOperatorConsideredRoaming(this.mNewSS) ^ 1) != 0 && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                log("updateRoamingState: resource override set non roaming.isSameNamedOperators=" + isSameNamedOperators(this.mNewSS) + ",isOperatorConsideredNonRoaming=" + isOperatorConsideredNonRoaming(this.mNewSS));
                roaming = false;
            }
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        roaming = false;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = false;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = true;
                    }
                } catch (Exception e) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            this.mNewSS.setVoiceRoaming(roaming);
            this.mNewSS.setDataRoaming(roaming);
            return;
        }
        configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                String systemId = Integer.toString(this.mNewSS.getSystemId());
                if (alwaysOnHomeNetwork(b)) {
                    log("updateRoamingState: carrier config override always on home network");
                    setRoamingOff();
                } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOff();
                } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOn();
                }
            } catch (Exception e2) {
                loge("updateRoamingState: unable to access carrier config service");
            }
        } else {
            log("updateRoamingState: no carrier config service available");
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
    }

    private void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(true);
        this.mNewSS.setDataRoaming(true);
        this.mNewSS.setCdmaEriIconIndex(0);
        this.mNewSS.setCdmaEriIconMode(0);
    }

    private void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(false);
        this.mNewSS.setDataRoaming(false);
        this.mNewSS.setCdmaEriIconIndex(1);
    }

    protected void updateSpnDisplay() {
        updateOperatorNameFromEri();
        updateSpnDisplay(false);
    }

    /* JADX WARNING: Missing block: B:137:0x042d, code:
            if (r36 == false) goto L_0x02f9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void updateSpnDisplay(boolean forceUpdate) {
        CharSequence wfcVoiceSpnFormat = null;
        CharSequence wfcDataSpnFormat = null;
        int combinedRegState = getCombinedRegState();
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled() && combinedRegState == 0) {
            String[] wfcSpnFormats = this.mPhone.getContext().getResources().getStringArray(17236066);
            int voiceIdx = 0;
            int dataIdx = 0;
            CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    PersistableBundle b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (b != null) {
                        voiceIdx = b.getInt("wfc_spn_format_idx_int");
                        dataIdx = b.getInt("wfc_data_spn_format_idx_int");
                    }
                } catch (Exception e) {
                    loge("updateSpnDisplay: carrier config error: " + e);
                }
            }
            wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            wfcDataSpnFormat = wfcSpnFormats[dataIdx];
        }
        combinedRegState = getCombinedRegState();
        boolean showPlmn;
        String plmn;
        int subId;
        int[] subIds;
        Intent intent;
        if (this.mPhone.isPhoneTypeGsm()) {
            UiccCardApplication uiccApplication = getUiccCardApplication();
            IccRecords iccRecords = null;
            if (uiccApplication != null) {
                iccRecords = uiccApplication.getIccRecords();
            }
            boolean showOperater = false;
            int rule = iccRecords != null ? iccRecords.getDisplayRule(this.mSS.getOperatorNumeric()) : 0;
            if (combinedRegState == 1 || combinedRegState == 2) {
                showPlmn = true;
                showOperater = true;
                if (this.mEmergencyOnly) {
                    plmn = Resources.getSystem().getText(17039853).toString();
                } else {
                    plmn = Resources.getSystem().getText(51249428).toString();
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
            } else if (combinedRegState == 0) {
                plmn = this.mSS.getOperatorAlpha();
                showPlmn = !TextUtils.isEmpty(plmn) ? (rule & 2) == 2 : false;
                plmn = updatePlmnShow(plmn);
                showOperater = iccRecords == null ? true : showOperator(iccRecords.getOperatorNumeric());
            } else {
                showPlmn = true;
                plmn = Resources.getSystem().getText(51249428).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
            }
            String spn = iccRecords != null ? iccRecords.getServiceProviderName() : "";
            String dataSpn = spn;
            String realSpn = spn;
            boolean showSpn = !TextUtils.isEmpty(spn) ? (rule & 1) == 1 : false;
            if (!TextUtils.isEmpty(spn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0 && (TextUtils.isEmpty(wfcDataSpnFormat) ^ 1) != 0) {
                String originalSpn = spn.trim();
                spn = String.format(wfcVoiceSpnFormat, new Object[]{originalSpn});
                dataSpn = String.format(wfcDataSpnFormat, new Object[]{originalSpn});
                showSpn = true;
                showPlmn = false;
            } else if (!TextUtils.isEmpty(plmn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0) {
                plmn = String.format(wfcVoiceSpnFormat, new Object[]{plmn.trim()});
            } else if (this.mSS.getVoiceRegState() == 3 || (showPlmn && TextUtils.equals(spn, plmn))) {
                spn = null;
                showSpn = false;
            }
            subId = -1;
            subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
            if (subIds != null && subIds.length > 0) {
                subId = subIds[0];
            }
            if ("yes".equalsIgnoreCase(SystemProperties.get("ro.vivo.product.overseas", "no")) && this.mSS != null && this.mSS.getOperatorNumeric() != null && this.mSS.getState() == 0 && TelephonyPhoneUtils.TYPES_OF_THAILAND_TRUE_MOVE.contains(this.mSS.getOperatorAlphaLong()) && TelephonyPhoneUtils.TYPES_OF_THAILAND_TRUE_MOVE.contains(realSpn)) {
                int i = 0;
                while (i < TelephonyPhoneUtils.customPlmnOperator_Th.length) {
                    if (TelephonyPhoneUtils.customPlmnOperator_Th[i][2].equals(this.mSS.getOperatorNumeric())) {
                        showPlmn = true;
                        if (this.mSS.getIsManualSelection()) {
                            showSpn = true;
                            spn = TelephonyPhoneUtils.customPlmnOperator_Th[i][1];
                            plmn = TelephonyPhoneUtils.customPlmnOperator_Th[i][0];
                        } else {
                            showSpn = false;
                            plmn = TelephonyPhoneUtils.customPlmnOperator_Th[i][1];
                        }
                        this.mSS.setOperatorAlphaLong(plmn);
                    } else {
                        i++;
                    }
                }
            }
            if (!(!showSpn || (showPlmn ^ 1) == 0 || TextUtils.isEmpty(spn))) {
                this.mSS.setOperatorAlphaLong(spn);
                spn = updatePlmnShow(spn);
            }
            log("updateSpnDisplay: mIsOtherPhoneInCall: " + this.mIsOtherPhoneInCall);
            if (!this.mIsOtherPhoneInCall || (this.mSS.getVoiceRegState() == 0 && this.mSS.getDataRegState() == 0)) {
                if (this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && (TextUtils.equals(spn, this.mCurSpn) ^ 1) == 0) {
                    if ((TextUtils.equals(dataSpn, this.mCurDataSpn) ^ 1) == 0) {
                        if ((TextUtils.equals(plmn, this.mCurPlmn) ^ 1) == 0) {
                        }
                    }
                }
                if (TelephonyPhoneUtils.isVSimEnable(this.mPhone.getPhoneId()) && (TextUtils.isEmpty(spn) ^ 1) != 0 && showSpn) {
                    showPlmn = false;
                    plmn = "";
                }
                log(String.format("updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' " + "subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn, Integer.valueOf(subId)}));
                intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
                intent.putExtra("showSpn", showSpn);
                intent.putExtra("spn", spn);
                intent.putExtra("spnData", dataSpn);
                intent.putExtra("showPlmn", showPlmn);
                intent.putExtra("plmn", plmn);
                intent.putExtra("showOperater", showOperater);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                this.mServiceStateTrackerHelper.updateSpnDisplay(intent, forceUpdate);
                if (!(forceUpdate || this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn))) {
                    this.mSpnUpdatePending = true;
                }
            }
            this.mSubId = subId;
            this.mCurShowSpn = showSpn;
            this.mCurShowPlmn = showPlmn;
            this.mCurSpn = spn;
            this.mCurDataSpn = dataSpn;
            this.mCurPlmn = plmn;
            return;
        }
        plmn = updatePlmnShow(this.mSS.getOperatorAlpha());
        showPlmn = plmn != null;
        subId = -1;
        subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
        if (subIds != null && subIds.length > 0) {
            subId = subIds[0];
        }
        if (!TextUtils.isEmpty(plmn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0) {
            plmn = String.format(wfcVoiceSpnFormat, new Object[]{plmn.trim()});
        } else if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            log("updateSpnDisplay: overwriting plmn from " + plmn + " to null as radio " + "state is off");
            plmn = null;
        }
        if (combinedRegState == 1) {
            plmn = Resources.getSystem().getText(51249428).toString();
            log("updateSpnDisplay: radio is on but out of svc, set plmn='" + plmn + "'");
        }
        if (!(this.mSubId == subId && (TextUtils.equals(plmn, this.mCurPlmn) ^ 1) == 0 && !forceUpdate)) {
            log(String.format("updateSpnDisplay: changed sending intent showPlmn='%b' plmn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Integer.valueOf(subId)}));
            intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.putExtra("showSpn", false);
            intent.putExtra("spn", "");
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra("plmn", plmn);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mServiceStateTrackerHelper.updateSpnDisplay(intent, forceUpdate);
            if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, false, "")) {
                this.mSpnUpdatePending = true;
            }
        }
        this.mSubId = subId;
        this.mCurShowSpn = false;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = "";
        this.mCurPlmn = plmn;
    }

    protected void setPowerStateToDesired() {
        String tmpLog = "mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier;
        log(tmpLog);
        this.mRadioPowerLog.log(tmpLog);
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && (this.mRadioDisabledByCarrier ^ 1) != 0 && this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            this.mCi.setRadioPower(true, null);
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || (this.mAlarmSwitch ^ 1) == 0) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                AlarmManager am = (AlarmManager) context.getSystemService("alarm");
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 0);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                am.set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
            this.mCi.requestShutdown(null);
        }
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        this.mUiccApplcation.registerForReady(this, 26, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 27, null);
                        }
                    }
                }
            }
        }
    }

    private void logRoamingChange() {
        this.mRoamingLog.log(this.mSS.toString());
    }

    private void logAttachChange() {
        this.mAttachLog.log(this.mSS.toString());
    }

    private void logPhoneTypeChange() {
        this.mPhoneTypeLog.log(Integer.toString(this.mPhone.getPhoneType()));
    }

    private void logRatChange() {
        this.mRatLog.log(this.mSS.toString());
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = true;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mSS.getRilDataRadioTechnology() >= 3) {
                return true;
            }
            if (this.mSS.getCssIndicator() != 1) {
                z = false;
            }
            return z;
        } else if (this.mPhone.isPhoneTypeCdma()) {
            return false;
        } else {
            if (this.mSS.getCssIndicator() != 1) {
                z = false;
            }
            return z;
        }
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (this.mImsRegistrationOnOff && (registered ^ 1) != 0 && this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
            sendMessage(obtainMessage(45));
            return;
        }
        this.mImsRegistrationOnOff = registered;
    }

    public void onImsCapabilityChanged() {
        sendMessage(obtainMessage(48));
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == RadioState.RADIO_ON;
    }

    public void pollState() {
        pollState(false);
    }

    private void modemTriggeredPollState() {
        pollState(true);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        log("pollState: modemTriggered=" + modemTriggered);
        switch (m3x804b995f()[this.mCi.getRadioState().ordinal()]) {
            case 1:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                if (this.mCdmaLteCellLoc != null) {
                    this.mCdmaLteCellLoc.setStateInvalid();
                }
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                if (this.mDeviceShuttingDown || !(modemTriggered || 18 == this.mSS.getRilDataRadioTechnology() || (this.mIsModemTriggeredPollingPending ^ 1) == 0)) {
                    pollStateDone();
                    return;
                }
            case 2:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                if (this.mCdmaLteCellLoc != null) {
                    this.mCdmaLteCellLoc.setStateInvalid();
                }
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                pollStateDone();
                return;
        }
        if (modemTriggered) {
            this.mIsModemTriggeredPollingPending = true;
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getDataRegistrationState(obtainMessage(5, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getVoiceRegistrationState(obtainMessage(4, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            iArr = this.mPollingContext;
            iArr[0] = iArr[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:290:0x0ac8 A:{Splitter: B:258:0x097b, ExcHandler: java.lang.NumberFormatException (r16_0 'ex' java.lang.RuntimeException), PHI: r37 r40 } */
    /* JADX WARNING: Missing block: B:290:0x0ac8, code:
            r16 = move-exception;
     */
    /* JADX WARNING: Missing block: B:291:0x0ac9, code:
            loge("pollStateDone: countryCodeForMcc error: " + r16);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void pollStateDone() {
        if (this.mNewSS.getDataRoaming()) {
            this.mVivoPplmnCollect.setPplmnInfo(this.mNewSS.getOperatorNumeric(), this.mNewSS.getDataNetworkType());
        }
        this.mIsModemTriggeredPollingPending = false;
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateRoamingState();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]" + " oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        boolean hasDeregistered = this.mSS.getVoiceRegState() == 0 ? this.mNewSS.getVoiceRegState() != 0 : false;
        boolean hasDataAttached = this.mSS.getDataRegState() != 0 ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasDataDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasLocationChanged = this.mNewCellLoc.equals(this.mCellLoc) ^ 1;
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() == this.mNewSS.getRilDataRadioTechnology() ? this.mSS.isUsingCarrierAggregation() != this.mNewSS.isUsingCarrierAggregation() : true;
        boolean hasChanged = this.mNewSS.equals(this.mSS) ^ 1;
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() ^ 1 : false;
        boolean hasDataRoamingOn = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() ^ 1 : false;
        boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
        boolean hasCssIndicatorChanged = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean hasDeregitserCA = (this.mSS.getRilDataRadioTechnology() != 19 || this.mNewSS.getRilDataRadioTechnology() == 19) ? false : this.mNewSS.getRilVoiceRadioTechnology() == 14;
        if (hasDeregistered || hasDataDetached || hasRegistered || hasDataAttached) {
            final ServiceState oldSS = new ServiceState(this.mSS);
            final ServiceState newSS = new ServiceState(this.mNewSS);
            final CellLocation cell = this.mCellLoc;
            final boolean registered = hasRegistered;
            final boolean deregistered = hasDeregistered;
            final boolean attached = hasDataAttached;
            final boolean detached = hasDataDetached;
            this.mCollectionThreadHandler.post(new Runnable() {
                public void run() {
                    ServiceStateTracker.this.collectionOutOfService(newSS, oldSS, cell, deregistered, detached, registered, attached);
                }
            });
        }
        boolean has4gHandoff = false;
        boolean hasMultiApnSupport = false;
        boolean hasLostMultiApnSupport = false;
        if (this.mPhone.isPhoneTypeCdmaLte()) {
            if (this.mNewSS.getDataRegState() != 0) {
                has4gHandoff = false;
            } else if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) {
                has4gHandoff = true;
            } else if (this.mSS.getRilDataRadioTechnology() == 13) {
                has4gHandoff = ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology());
            } else {
                has4gHandoff = false;
            }
            hasMultiApnSupport = (ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology()) || this.mNewSS.getRilDataRadioTechnology() == 13) ? !ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) ? this.mSS.getRilDataRadioTechnology() != 13 : false : false;
            hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() >= 4 ? this.mNewSS.getRilDataRadioTechnology() <= 8 : false;
        }
        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeregistered=" + hasDeregistered + " hasDataAttached=" + hasDataAttached + " hasDataDetached=" + hasDataDetached + " hasDataRegStateChanged=" + hasDataRegStateChanged + " hasRilVoiceRadioTechnologyChanged= " + hasRilVoiceRadioTechnologyChanged + " hasRilDataRadioTechnologyChanged=" + hasRilDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + hasVoiceRoamingOn + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + hasDataRoamingOn + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
        if (hasVoiceRegStateChanged || hasDataRegStateChanged) {
            int i;
            if (this.mPhone.isPhoneTypeGsm()) {
                i = EventLogTags.GSM_SERVICE_STATE_CHANGE;
            } else {
                i = EventLogTags.CDMA_SERVICE_STATE_CHANGE;
            }
            EventLog.writeEvent(i, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasRilVoiceRadioTechnologyChanged) {
                int cid = -1;
                GsmCellLocation loc = (GsmCellLocation) this.mNewCellLoc;
                if (loc != null) {
                    cid = loc.getCid();
                }
                EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, new Object[]{Integer.valueOf(cid), Integer.valueOf(this.mSS.getRilVoiceRadioTechnology()), Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology())});
                log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
            }
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            this.mRejectCode = this.mNewRejectCode;
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        boolean lacTacSidChanged = true;
        if (this.mCellLoc instanceof GsmCellLocation) {
            if ((this.mNewCellLoc instanceof GsmCellLocation) && this.mNewCellLoc != null && this.mCellLoc != null && ((GsmCellLocation) this.mNewCellLoc).getLac() == ((GsmCellLocation) this.mCellLoc).getLac()) {
                lacTacSidChanged = false;
            }
        } else if ((this.mCellLoc instanceof CdmaCellLocation) && (this.mNewCellLoc instanceof CdmaCellLocation) && this.mNewCellLoc != null && this.mCellLoc != null && ((CdmaCellLocation) this.mNewCellLoc).getSystemId() == ((CdmaCellLocation) this.mCellLoc).getSystemId()) {
            lacTacSidChanged = false;
        }
        if (lacTacSidChanged) {
            LocationUpdateHelper.getInstance().sendEmptyMessage(1001);
        }
        CellLocation tcl = this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered || hasDeregistered) {
            EmergencyNumberUpdateHelper.getInstance().updateEmergencyPropertyByPhoneId(this.mPhone.getPhoneId());
        }
        if (hasChanged) {
            RoamingStatusCollectHelper.getInstance().updateCurrentStreetStatus(this.mPhone.getPhoneId());
            if (hasRegistered) {
                RoamingStatusCollectHelper.getInstance().setAirplaneOffRegisterTime();
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false");
            this.mNitzUpdatedTime = false;
        }
        if (hasDeregistered) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
            log("hasDeregistered = " + hasDeregistered + ", initialize the SignalStrength bss");
            this.mGsmBssWorking = false;
            this.mTdBssWorking = false;
            this.mLteBssWorking = false;
            this.mCdmaBssWorking = false;
            this.mEvdoBssWorking = false;
        }
        if (hasRejectCauseChanged) {
            setNotification(CS_REJECT_CAUSE_ENABLED);
        }
        if (hasChanged) {
            updateOperatorNameFromEri();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric " + operatorNumeric + " is invalid");
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
            } else if (this.mSS.getRilDataRadioTechnology() != 18) {
                String iso = "";
                String mcc = "";
                try {
                    mcc = operatorNumeric.substring(0, 3);
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                    updateMcc(mcc);
                    updateIsoCountryCode(iso);
                } catch (RuntimeException ex) {
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                this.mGotCountryCode = true;
                if (!this.mNitzUpdatedTime) {
                    if (!((mcc.equals(INVALID_MCC) ^ 1) == 0 || (TextUtils.isEmpty(iso) ^ 1) == 0)) {
                        boolean testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) == 0 : false;
                        List<String> uniqueZones = TimeUtils.getTimeZoneIdsWithUniqueOffsets(iso);
                        if (uniqueZones.size() == 1 || testOneUniqueOffsetPath || Arrays.binarySearch(SINGLE_TIMEZONE_COUNTRY_CODES, iso) >= 0) {
                            String zoneId = (String) uniqueZones.get(0);
                            log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zoneId + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                            this.mTimeZoneLog.log("pollStateDone: set time zone=" + zoneId + " mcc=" + mcc + " iso=" + iso);
                            if (getAutoTimeZone() && (this.mSavedTimeZone == null || (this.mSavedTimeZone.equals(zoneId) ^ 1) != 0)) {
                                setAndBroadcastNetworkSetTimeZone(zoneId);
                            }
                            saveNitzTimeZone(zoneId);
                        } else {
                            log("pollStateDone: there are " + uniqueZones.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                        }
                    }
                }
                if (!this.mPhone.isPhoneTypeGsm()) {
                    setOperatorIdd(operatorNumeric);
                }
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(iso);
                }
            }
            int phoneId = this.mPhone.getPhoneId();
            boolean voiceRoaming = this.mPhone.isPhoneTypeGsm() ? this.mSS.getVoiceRoaming() : !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true;
            tm.setNetworkRoamingForPhone(phoneId, voiceRoaming);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            log("pollStateDone:mIsPhoneInCall = " + this.mIsPhoneInCall + " mIsOtherPhoneInCall=" + this.mIsOtherPhoneInCall);
            if (!this.mIsOtherPhoneInCall) {
                this.mServiceStateTrackerHelper.notifyServiceStateChanged(this.mSS);
                updateSpnDisplay(false);
            } else if (hasDeregitserCA) {
                this.mServiceStateTrackerHelper.notifyServiceStateChanged(this.mSS);
            } else if (this.mSS.getVoiceRegState() == 0 && this.mSS.getDataRegState() == 0) {
                this.mServiceStateTrackerHelper.notifyServiceStateChanged(this.mSS);
                updateSpnDisplay(false);
            }
            this.mPhone.getContext().getContentResolver().insert(ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), ServiceStateTable.getContentValuesForServiceState(this.mSS));
            try {
                TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            } catch (Exception e) {
                log("writeServiceStateChanged e = " + e);
            }
            EmergencyNumberUpdateHelper.getInstance().updateEmergencyProperties();
        }
        if (hasDataAttached || has4gHandoff || hasDataDetached || hasRegistered || hasDeregistered) {
            logAttachChange();
        }
        if (hasDataAttached || has4gHandoff) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasDataDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasRilDataRadioTechnologyChanged || hasRilVoiceRadioTechnologyChanged) {
            logRatChange();
        }
        if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                this.mPhone.notifyDataConnection(null);
            }
        }
        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff) {
            logRoamingChange();
        }
        if (hasVoiceRoamingOn) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOn) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
        if (hasCssIndicatorChanged) {
            this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_CSS_INDICATOR_CHANGED);
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                this.mReportedGprsNoReg = false;
            } else if (!this.mStartedGprsRegCheck && (this.mReportedGprsNoReg ^ 1) != 0) {
                this.mStartedGprsRegCheck = true;
                sendMessageDelayed(obtainMessage(22), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", 60000));
            }
        }
    }

    public void setScreenOnOffState(boolean isScreenOn) {
        this.DIsScreenOn = isScreenOn;
        if (this.cs_out_of_service_msg_count == 1 && isScreenOn) {
            this.csDropScreenon = 1;
        } else if (this.cs_out_of_service_msg_count != 1) {
            this.csDropScreenon = 2;
        }
        if (this.ps_out_of_service_msg_count == 1 && isScreenOn) {
            this.psDropScreenon = 1;
        } else if (this.ps_out_of_service_msg_count != 1) {
            this.psDropScreenon = 2;
        }
        if (this.mPhone != null) {
            if (DataCollectionUtils.DBG) {
                log(" setScreenOnOffState  ps_out_of_service_msg_count: " + this.ps_out_of_service_msg_count + " cs_out_of_service_msg_count: " + this.cs_out_of_service_msg_count);
            }
            if (DataCollectionUtils.DBG) {
                log(" setScreenOnOffState  phone: " + this.mPhone.getPhoneId() + " csDropScreenon: " + this.csDropScreenon + ",,psDropScreenon=" + this.psDropScreenon + ",isScreenOn=" + isScreenOn);
            }
        }
    }

    public void collectionOutOfService(ServiceState mNewSS, ServiceState mSS, CellLocation mCellLoc, boolean hasDeregistered, boolean hasGprsDetached, boolean hasRegistered, boolean hasDataAttached) {
        if (this.mPhone == null) {
            log("collectionOutOfService mPhone is null");
            return;
        }
        String duration;
        int cid1 = 0;
        int lac1 = 0;
        if (mCellLoc != null) {
            if (mCellLoc instanceof GsmCellLocation) {
                GsmCellLocation loc = (GsmCellLocation) mCellLoc;
                if (loc != null) {
                    cid1 = loc.getCid();
                    lac1 = loc.getLac();
                }
            } else if (mCellLoc instanceof CdmaCellLocation) {
                CdmaCellLocation loc2 = (CdmaCellLocation) mCellLoc;
                if (loc2 != null) {
                    cid1 = loc2.getNetworkId();
                    lac1 = loc2.getSystemId();
                }
            }
        }
        long timeNow = System.currentTimeMillis();
        String imei = this.mPhone.getImei();
        int phoneId = this.mPhone.getPhoneId();
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        Context context = this.mPhone.getContext();
        String outOfServiceType = "-1";
        if (hasGprsDetached && hasDeregistered) {
            outOfServiceType = "2";
        } else if (hasDeregistered) {
            outOfServiceType = "0";
        } else {
            outOfServiceType = "1";
        }
        this.mDataCollectionUtils.setPlmn(mSS.getOperatorNumeric());
        if (DataCollectionUtils.DBG) {
            log("DataCollectionUtils  phone: " + phoneId + " cs_out_of_service_msg_count: " + this.cs_out_of_service_msg_count + " ps_out_of_service_msg_count: " + this.ps_out_of_service_msg_count);
        }
        if (!DataCollectionUtils.isAirModeOn() && this.mDataCollectionUtils.isNotMms() && (this.mDataCollectionUtils.getDefaultDataSubChangeFlag() ^ 1) != 0 && this.mDataCollectionUtils.isValidSim(this.mPhone.getSubId()) && this.mDataCollectionUtils.isIdle() && this.mFtTel.isRadioOn(phoneId) && DataCollectionUtils.isValidImeiMeid(this.mPhone.getImei()) && DataCollectionUtils.isValidLoc(lac1, cid1)) {
            String[][] modeminfos = this.mDataCollectionUtils.getModemInfo(phoneId);
            if (hasDeregistered && this.cs_out_of_service_msg_count == 0) {
                if (DataCollectionUtils.DBG) {
                    log("DataCollectionUtils cs is out of service phone: " + phoneId);
                }
                this.mCSOutOfServiceTime = timeNow;
                this.mCsOutOfService = new CollectionBean(phoneId, this.mPhone.getPhoneType(), 2, mSS, mNewSS, mCellLoc, this.mNewCellLoc, outOfServiceType);
                this.mDataCollectionUtils.fillModemInfo(modeminfos, this.mCsOutOfService, mSS.getVoiceNetworkType());
                this.cs_out_of_service_msg_count = 1;
                if (this.DIsScreenOn) {
                    this.csDropScreenon = 1;
                    if (DataCollectionUtils.DBG) {
                        log("DataCollectionUtils  csDropScreenon: " + this.csDropScreenon);
                    }
                }
            }
            if (hasGprsDetached && this.ps_out_of_service_msg_count == 0) {
                if (DataCollectionUtils.DBG) {
                    log("DataCollectionUtils ps is out of service phone: " + phoneId);
                }
                this.mPSOutOfServiceTime = timeNow;
                this.mPsOutOfService = new CollectionBean(phoneId, this.mPhone.getPhoneType(), 2, mSS, mNewSS, mCellLoc, this.mNewCellLoc, outOfServiceType);
                this.mDataCollectionUtils.fillModemInfo(modeminfos, this.mPsOutOfService, mSS.getDataNetworkType());
                this.ps_out_of_service_msg_count = 1;
                if (this.DIsScreenOn) {
                    this.psDropScreenon = 1;
                    if (DataCollectionUtils.DBG) {
                        log("DataCollectionUtils  psDropScreenon: " + this.psDropScreenon);
                    }
                }
            }
        }
        if (hasRegistered && this.cs_out_of_service_msg_count == 1) {
            duration = DataCollectionUtils.getDuration(this.mCSOutOfServiceTime, timeNow);
            if (duration == null) {
                duration = "-1";
            }
            if (DataCollectionUtils.DBG) {
                log("DataCollectionUtils reportOutOfServ cs out of serivce phone: " + this.mPhone.getPhoneId() + " duration: " + duration);
            }
            this.mDataCollectionUtils.reportOutOfServ(this.mCsOutOfService, duration, 20, this.csDropScreenon);
            this.cs_out_of_service_msg_count = 0;
            this.mCsOutOfService = null;
        }
        if (hasDataAttached && this.ps_out_of_service_msg_count == 1) {
            duration = DataCollectionUtils.getDuration(this.mPSOutOfServiceTime, timeNow);
            if (duration == null) {
                duration = "-1";
            }
            if (DataCollectionUtils.DBG) {
                log("DataCollectionUtils reportOutOfServ ps out of serivce phone: " + this.mPhone.getPhoneId() + " duration: " + duration);
            }
            this.mDataCollectionUtils.reportOutOfServ(this.mPsOutOfService, duration, 21, this.psDropScreenon);
            this.ps_out_of_service_msg_count = 0;
            this.mPsOutOfService = null;
        }
        if (this.ps_out_of_service_msg_count == 0 && this.cs_out_of_service_msg_count == 0) {
            this.mDataCollectionUtils.setRadioUnavailableflag(false);
            this.mDataCollectionUtils.setRilReconnectionFlag(false);
        }
    }

    private void updateOperatorNameFromEri() {
        String eriText;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState().isOn() && (this.mIsSubscriptionFromRuim ^ 1) != 0) {
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else {
                    eriText = this.mPhone.getContext().getText(17041231).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
        } else if (this.mPhone.isPhoneTypeCdmaLte()) {
            boolean hasBrandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() != null : false;
            if (!hasBrandOverride && this.mCi.getRadioState().isOn() && this.mPhone.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17956867)) && (this.mIsSubscriptionFromRuim ^ 1) != 0)) {
                eriText = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = this.mIccRecords != null ? this.mIccRecords.getServiceProviderName() : null;
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17041231).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY && this.mIccRecords != null && getCombinedRegState() == 0 && (ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) ^ 1) != 0) {
                boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                int iconIndex = this.mSS.getCdmaEriIconIndex();
                if (showSpn && iconIndex == 1 && isInHomeSidNid(this.mSS.getSystemId(), this.mSS.getNetworkId()) && this.mIccRecords != null) {
                    this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                }
            }
        }
    }

    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        int i = 0;
        while (i < this.mHomeSystemId.length) {
            if (this.mHomeSystemId[i] == sid && (this.mHomeNetworkId[i] == 0 || this.mHomeNetworkId[i] == 65535 || nid == 0 || nid == 65535 || this.mHomeNetworkId[i] == nid)) {
                return true;
            }
            i++;
        }
        return false;
    }

    protected void setOperatorIdd(String operatorNumeric) {
        if (!this.mPhone.getUnitTestMode()) {
            String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
            if (idd == null || (idd.isEmpty() ^ 1) == 0) {
                SystemProperties.set("gsm.operator.idpstring", "+");
            } else {
                SystemProperties.set("gsm.operator.idpstring", idd);
            }
        }
    }

    protected boolean isInvalidOperatorNumeric(String operatorNumeric) {
        if (operatorNumeric == null || operatorNumeric.length() < 5) {
            return true;
        }
        return operatorNumeric.startsWith(INVALID_MCC);
    }

    protected String fixUnknownMcc(String operatorNumeric, int sid) {
        int i = 0;
        if (sid <= 0) {
            return operatorNumeric;
        }
        boolean isNitzTimeZone = false;
        int timeZone = 0;
        if (this.mSavedTimeZone != null) {
            timeZone = TimeZone.getTimeZone(this.mSavedTimeZone).getRawOffset() / MS_PER_HOUR;
            isNitzTimeZone = true;
        } else {
            TimeZone tzone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (tzone != null) {
                timeZone = tzone.getRawOffset() / MS_PER_HOUR;
            }
        }
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (this.mZoneDst) {
            i = 1;
        }
        int mcc = hbpcdUtils.getMcc(sid, timeZone, i, isNitzTimeZone);
        if (mcc > 0) {
            operatorNumeric = Integer.toString(mcc) + DEFAULT_MNC;
        }
        return operatorNumeric;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected String updatePlmnShow(String plmn) {
        if (TextUtils.isEmpty(plmn)) {
            return plmn;
        }
        String localString = TelephonyPhoneUtils.getLocalString(this.mPhone.getContext(), plmn);
        if (!TextUtils.isEmpty(localString)) {
            plmn = localString;
        }
        if (TelephonyPhoneUtils.sIsCMCCEntry || TelephonyPhoneUtils.sInNetEntry) {
            int networkClass;
            int csNetworkClass = 0;
            int psNetworkClass = 0;
            int regState = this.mSS.getVoiceRegState();
            int dataRegState = this.mSS.getDataRegState();
            if (regState == 0) {
                csNetworkClass = TelephonyManager.getNetworkClass(this.mSS.getVoiceNetworkType());
            }
            if (dataRegState == 0) {
                psNetworkClass = TelephonyManager.getNetworkClass(this.mSS.getDataNetworkType());
            }
            if (psNetworkClass > csNetworkClass) {
                networkClass = psNetworkClass;
            } else {
                networkClass = csNetworkClass;
            }
            if (networkClass == 3) {
                plmn = plmn + " 4G";
            } else if (networkClass == 2) {
                plmn = plmn + " 3G";
            }
            log("networkClass:" + networkClass + ",plmn:" + plmn);
        }
        return plmn;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected boolean showOperator(String simOperatorNum) {
        int i = 0;
        if (TextUtils.isEmpty(simOperatorNum)) {
            return true;
        }
        log("simOperatorNum=" + simOperatorNum);
        boolean show = false;
        String[] strArr;
        int length;
        if (TelephonyPhoneUtils.sIsCMCCEntry) {
            strArr = TelephonyPhoneUtils.cmCuPlmn[0];
            length = strArr.length;
            while (i < length) {
                if (strArr[i].equals(simOperatorNum)) {
                    show = true;
                    break;
                }
                i++;
            }
        } else if (TelephonyPhoneUtils.sIsUNICOMEntry) {
            strArr = TelephonyPhoneUtils.cmCuPlmn[1];
            length = strArr.length;
            while (i < length) {
                if (strArr[i].equals(simOperatorNum)) {
                    show = true;
                    break;
                }
                i++;
            }
        } else {
            show = true;
        }
        return show;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    public static String[] updateOperatorName(String[] opNames) {
        String[] operatorNames = opNames;
        if (!(opNames == null || opNames.length < 3 || TextUtils.isEmpty(opNames[2]))) {
            boolean override = false;
            if (TextUtils.isEmpty(opNames[0])) {
                override = true;
            } else if ("46011".equals(opNames[2]) && ("operator".equals(opNames[0]) || "operator".equals(opNames[1]))) {
                override = true;
            } else if ("45400".equals(opNames[2]) && ("NEW WORLD".equals(opNames[0]) || "NEW WORLD".equals(opNames[1]))) {
                override = true;
            } else {
                if (opNames[2].trim().equals(opNames[0].replace(" ", ""))) {
                    override = true;
                }
            }
            if (override) {
                try {
                    OperatorName alpName = CustomPlmnOperatorOverride.getInstance().getOperator(opNames[2]);
                    if (alpName != null) {
                        opNames[0] = alpName.longName;
                        opNames[1] = alpName.shortName;
                    }
                } catch (Exception e) {
                    Rlog.d(LOG_TAG, "CustomPlmnOperatorOverride is not init");
                }
            }
        }
        return opNames;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void notifyUserCountryChange() {
        Intent intent = new Intent("vivo.intent.action.USER_COUNTRY_CHANGE");
        intent.putExtra("iroaming", "yes");
        this.mPhone.getContext().sendBroadcast(intent);
        intent.setPackage("com.vivo.networkimprove");
        this.mPhone.getContext().sendBroadcast(intent);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void notifyShowIroamingDialog() {
        log("notifyShowIroamingDialog!");
        this.mPhone.getContext().sendBroadcast(new Intent("android.intent.action.SHOW_IROAMING_DIALOG"));
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void notifySignalStrengthforCallEnd() {
        Message msg = Message.obtain(this);
        msg.what = 54;
        if (sendMessageDelayed(msg, 4000)) {
            log("Wait upto 4s for notifySignalStrength.");
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected void notifySignalStrengthforCallStart() {
        log("SpListener notifySignalStrengthforCallStart enter!");
        this.mServiceStateTrackerHelper.resetValue();
        if (this.mIsOtherPhoneInCall) {
            removeMessages(54);
        }
    }

    protected void fixTimeZone(String isoCountryCode) {
        TimeZone zone = null;
        String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
        log("fixTimeZone zoneName='" + zoneName + "' mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc='" + isoCountryCode + "' iso-cc-idx=" + Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode));
        if ("".equals(isoCountryCode) && this.mNeedFixZoneAfterNitz) {
            zone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            log("pollStateDone: using NITZ TimeZone");
        } else if (this.mZoneOffset != 0 || this.mZoneDst || zoneName == null || zoneName.length() <= 0 || Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) >= 0) {
            zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime, isoCountryCode);
            log("fixTimeZone: using getTimeZone(off, dst, time, iso)");
        } else {
            if (this.mNeedFixZoneAfterNitz) {
                zone = TimeZone.getDefault();
                long ctm = System.currentTimeMillis();
                long tzOffset = (long) zone.getOffset(ctm);
                log("fixTimeZone: tzOffset=" + tzOffset + " ltod=" + TimeUtils.logTimeOfDay(ctm));
                if (getAutoTime()) {
                    long adj = ctm - tzOffset;
                    log("fixTimeZone: adj ltod=" + TimeUtils.logTimeOfDay(adj));
                    setAndBroadcastNetworkSetTime(adj);
                } else {
                    this.mSavedTime -= tzOffset;
                    log("fixTimeZone: adj mSavedTime=" + this.mSavedTime);
                }
            }
            log("fixTimeZone: using default TimeZone");
        }
        this.mTimeZoneLog.log("fixTimeZone zoneName=" + zoneName + " mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc=" + isoCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz + " zone=" + (zone != null ? zone.getID() : "NULL"));
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else {
                log("fixTimeZone: skip changing zone as getAutoTimeZone was false");
            }
            if (this.mNeedFixZoneAfterNitz) {
                saveNitzTimeZone(zone.getID());
            }
        } else {
            log("fixTimeZone: zone == null, do nothing for zone");
        }
        this.mNeedFixZoneAfterNitz = false;
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, dst ^ 1, when);
        }
        log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        int rawOffset = offset;
        if (dst) {
            rawOffset = offset - MS_PER_HOUR;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                return tz;
            }
        }
        return null;
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case 1:
            case 5:
                return 0;
            default:
                return 1;
        }
    }

    private boolean regCodeIsRoaming(int code) {
        return 5 == code;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = !TextUtils.isEmpty(spn) ? spn.equals(onsl) : false;
        boolean equalsOnss = !TextUtils.isEmpty(spn) ? spn.equals(onss) : false;
        String operatorNumeric = s.getOperatorNumeric();
        if (operatorNumeric != null) {
            String iso = "";
            String mcc = "";
            try {
                iso = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
            } catch (NumberFormatException ex) {
                loge("isSameNamedOperators: countryCodeForMcc error" + ex);
            } catch (StringIndexOutOfBoundsException ex2) {
                loge("isSameNamedOperators: countryCodeForMcc error" + ex2);
            }
            if ("in".equals(iso)) {
                equalsOnsl = false;
                equalsOnss = false;
            }
        }
        if (equalsOnsl) {
            return true;
        }
        return equalsOnss;
    }

    private boolean isSameNamedOperators(ServiceState s) {
        boolean isSameOperator = false;
        String simNumeric = "";
        int subId = 0;
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            subId = this.mPhone.getPhoneId();
            simNumeric = TelephonyManager.getTelephonyProperty(subId, "gsm.sim.operator.numeric", "");
        } else {
            simNumeric = getSystemProperty("gsm.sim.operator.numeric", "");
        }
        String operatorNumeric = s.getOperatorNumeric();
        if (!(operatorNumeric == null || simNumeric == null)) {
            isSameOperator = TelephonyPhoneUtils.isSameOperator(operatorNumeric, simNumeric);
        }
        log("getisRoam isSameOperator=" + isSameOperator);
        if (!(operatorNumeric == null || (operatorNumeric.equals("") ^ 1) == 0 || simNumeric == null || (simNumeric.equals("") ^ 1) == 0 || (isSameOperator ^ 1) == 0)) {
            Intent intent;
            if (subId == 0) {
                if (!(operatorNumericSave1.equals(operatorNumeric) && (simNumericSave1.equals(simNumeric) ^ 1) == 0)) {
                    log("getisRoam sendBroadcast sendBroadcast 1");
                    intent = new Intent("vivo.intent.action.plmnroam");
                    intent.setPackage("com.vivo.networkimprove");
                    intent.putExtra("plmn1", operatorNumeric);
                    intent.putExtra("plmn2", simNumeric);
                    this.mPhone.getContext().sendBroadcast(intent);
                    operatorNumericSave1 = operatorNumeric;
                    simNumericSave1 = simNumeric;
                }
            } else if (!(operatorNumericSave2.equals(operatorNumeric) && (simNumericSave2.equals(simNumeric) ^ 1) == 0)) {
                log("getisRoam sendBroadcast sendBroadcast 2");
                intent = new Intent("vivo.intent.action.plmnroam");
                intent.setPackage("com.vivo.networkimprove");
                intent.putExtra("plmn1", operatorNumeric);
                intent.putExtra("plmn2", simNumeric);
                this.mPhone.getContext().sendBroadcast(intent);
                operatorNumericSave2 = operatorNumeric;
                simNumericSave2 = simNumeric;
            }
        }
        if (!currentMccEqualsSimMcc(s)) {
            return false;
        }
        if (isSameOperator) {
            return true;
        }
        return isSameOperatorNameFromSimAndSS(s);
    }

    private boolean currentMccEqualsSimMcc(ServiceState s) {
        boolean equalsMcc = true;
        try {
            return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
            return equalsMcc;
        }
    }

    private boolean isOperatorConsideredNonRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        Object[] numericArray = null;
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                numericArray = config.getStringArray("non_roaming_operator_string_array");
            }
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (!TextUtils.isEmpty(numeric) && operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        Object[] numericArray = null;
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                numericArray = config.getStringArray("roaming_operator_string_array");
            }
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (!TextUtils.isEmpty(numeric) && operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        boolean z = true;
        RestrictedState newRs = new RestrictedState();
        log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        if (ar.exception == null && ar.result != null) {
            boolean z2;
            int state = ((Integer) ar.result).intValue();
            if ((state & 1) != 0) {
                z2 = true;
            } else if ((state & 4) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            newRs.setCsEmergencyRestricted(z2);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY) {
                if ((state & 2) != 0) {
                    z2 = true;
                } else if ((state & 4) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                newRs.setCsNormalRestricted(z2);
                if ((state & 16) == 0) {
                    z = false;
                }
                newRs.setPsRestricted(z);
            }
            log("onRestrictedStateChanged: new rs " + newRs);
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && (newRs.isPsRestricted() ^ 1) != 0) {
                this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(1002);
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(1006);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(1005);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || (this.mRestrictedState.isCsNormalRestricted() ^ 1) == 0) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(1003);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(1006);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(1005);
                    }
                } else if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (newRs.isCsRestricted()) {
                    setNotification(1003);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(1006);
                }
            } else if (!newRs.isAnyCsRestricted()) {
                setNotification(1004);
            } else if (newRs.isCsRestricted()) {
                setNotification(1003);
            } else if (newRs.isCsNormalRestricted()) {
                setNotification(1005);
            }
            this.mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs " + this.mRestrictedState);
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        if (((GsmCellLocation) this.mCellLoc).getLac() >= 0 && ((GsmCellLocation) this.mCellLoc).getCid() >= 0) {
            return this.mCellLoc;
        }
        List<CellInfo> result = getAllCellInfo(workSource);
        if (result == null) {
            return this.mCellLoc;
        }
        GsmCellLocation cellLocOther = new GsmCellLocation();
        for (CellInfo ci : result) {
            if (ci instanceof CellInfoGsm) {
                android.telephony.CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                cellLocOther.setPsc(cellIdentityGsm.getPsc());
                return cellLocOther;
            } else if (ci instanceof CellInfoWcdma) {
                android.telephony.CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                return cellLocOther;
            } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                android.telephony.CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                    cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                    cellLocOther.setPsc(0);
                }
            }
        }
        return cellLocOther;
    }

    /* JADX WARNING: Missing block: B:63:0x027d, code:
            if (r38.mZoneDst != (r11 != 0)) goto L_0x0173;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setTimeFromNITZString(String nitz, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        log("NITZ: " + nitz + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        long end;
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.clear();
            c.set(16, 0);
            String[] nitzSubs = nitz.split("[/:,+-]");
            int year = Integer.parseInt(nitzSubs[0]) + 2000;
            if (year > MAX_NITZ_YEAR) {
                loge("NITZ year: " + year + " exceeds limit, skip NITZ time update");
                return;
            }
            boolean sign = nitz.indexOf(45) == -1;
            int tzOffset = Integer.parseInt(nitzSubs[6]);
            if (tzOffset != 0) {
                int month = Integer.parseInt(nitzSubs[1]) - 1;
                int date = Integer.parseInt(nitzSubs[2]);
                int hour = Integer.parseInt(nitzSubs[3]);
                int minute = Integer.parseInt(nitzSubs[4]);
                int second = Integer.parseInt(nitzSubs[5]);
                c.set(1, year);
                c.set(2, month);
                c.set(5, date);
                c.set(10, hour);
                c.set(12, minute);
                c.set(13, second);
                int dst = nitzSubs.length >= 8 ? Integer.parseInt(nitzSubs[7]) : 0;
                tzOffset = ((((sign ? 1 : -1) * tzOffset) * 15) * 60) * 1000;
                TimeZone zone = null;
                if (nitzSubs.length >= 9) {
                    zone = TimeZone.getTimeZone(nitzSubs[8].replace('!', '/'));
                }
                String iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
                if (zone == null && this.mGotCountryCode) {
                    if (iso == null || iso.length() <= 0) {
                        zone = getNitzTimeZone(tzOffset, dst != 0, c.getTimeInMillis());
                    } else {
                        zone = TimeUtils.getTimeZone(tzOffset, dst != 0, c.getTimeInMillis(), iso);
                    }
                }
                if (zone != null && this.mZoneOffset == tzOffset) {
                }
                this.mNeedFixZoneAfterNitz = true;
                this.mZoneOffset = tzOffset;
                this.mZoneDst = dst != 0;
                this.mZoneTime = c.getTimeInMillis();
                String tmpLog = "NITZ: nitz=" + nitz + " nitzReceiveTime=" + nitzReceiveTime + " tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone != null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz + " getAutoTimeZone()=" + getAutoTimeZone();
                log(tmpLog);
                this.mTimeZoneLog.log(tmpLog);
                if (zone != null) {
                    if (getAutoTimeZone()) {
                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                    }
                    saveNitzTimeZone(zone.getID());
                }
                String ignore = SystemProperties.get("gsm.ignore-nitz");
                if (ignore != null) {
                    if (ignore.equals("yes")) {
                        log("NITZ: Not setting clock because gsm.ignore-nitz is set");
                        return;
                    }
                }
                this.mWakeLock.acquire();
                if (!this.mPhone.isPhoneTypeGsm() || getAutoTime()) {
                    long millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
                    if (millisSinceNitzReceived < 0) {
                        log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                        end = SystemClock.elapsedRealtime();
                        log("NITZ: end=" + end + " dur=" + (end - start));
                        this.mWakeLock.release();
                        return;
                    } else if (millisSinceNitzReceived > 2147483647L) {
                        log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                        end = SystemClock.elapsedRealtime();
                        log("NITZ: end=" + end + " dur=" + (end - start));
                        this.mWakeLock.release();
                        return;
                    } else {
                        NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).setNitzTimeInfo(this.mPhone.getPhoneId(), c.getTimeInMillis(), nitzReceiveTime, this.mPhone.isPhoneTypeGsm() ^ 1);
                        c.add(14, (int) millisSinceNitzReceived);
                        tmpLog = "NITZ: nitz=" + nitz + " nitzReceiveTime=" + nitzReceiveTime + " Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz;
                        log(tmpLog);
                        this.mTimeLog.log(tmpLog);
                        if (this.mPhone.isPhoneTypeGsm()) {
                            if (NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).shouldUpdateTime(this.mPhone.getPhoneId(), false)) {
                                setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                                NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).setUpdateAtTime(this.mPhone.getPhoneId());
                            }
                            Rlog.i(LOG_TAG, "NITZ: after Setting time of day");
                        } else if (getAutoTime()) {
                            long gained = c.getTimeInMillis() - System.currentTimeMillis();
                            long timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                            int nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                            int nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                            if (this.mSavedAtTime != 0 && timeSinceLastUpdate <= ((long) nitzUpdateSpacing)) {
                                if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                    log("NITZ: ignore, a previous update was " + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                                    end = SystemClock.elapsedRealtime();
                                    log("NITZ: end=" + end + " dur=" + (end - start));
                                    this.mWakeLock.release();
                                    return;
                                }
                            }
                            log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                            if (NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).shouldUpdateTime(this.mPhone.getPhoneId(), true)) {
                                setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                                NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).setUpdateAtTime(this.mPhone.getPhoneId());
                            }
                        }
                    }
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                saveNitzTime(c.getTimeInMillis());
                this.mNitzUpdatedTime = true;
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            loge("NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
        } catch (Throwable th) {
            end = SystemClock.elapsedRealtime();
            log("NITZ: end=" + end + " dur=" + (end - start));
            this.mWakeLock.release();
        }
    }

    private boolean getAutoTime() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    private boolean getAutoTimeZone() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time_zone") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    public void saveNitzTimeZone(String zoneId) {
        this.mSavedTimeZone = zoneId;
    }

    public String getNitzTimeZone() {
        return this.mSavedTimeZone;
    }

    private void saveNitzTime(long time) {
        this.mSavedTime = time;
        this.mSavedAtTime = SystemClock.elapsedRealtime();
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        log("setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).setTimeZone(zoneId);
        if (zoneId != null && zoneId.length() <= 91) {
            SystemProperties.set("persist.radio.vivo.zone", zoneId);
        }
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
        intent.addFlags(536870912);
        intent.putExtra("time-zone", zoneId);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        log("setAndBroadcastNetworkSetTimeZone: call alarm.setTimeZone and broadcast zoneId=" + zoneId);
    }

    private void setAndBroadcastNetworkSetTime(long time) {
        log("setAndBroadcastNetworkSetTime: time=" + time + "ms");
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
        intent.addFlags(536870912);
        intent.putExtra("time", time);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), time);
    }

    private void revertToNitzTime() {
        if (Global.getInt(this.mCr, "auto_time", 0) != 0) {
            log("Reverting to NITZ Time: mSavedTime=" + this.mSavedTime + " mSavedAtTime=" + this.mSavedAtTime);
            if (!(this.mSavedTime == 0 || this.mSavedAtTime == 0 || !NitzTimeUpdatePolicy.getInstance(this.mPhone.getContext()).shouldUpdateTime(this.mPhone.getPhoneId(), true))) {
                setAndBroadcastNetworkSetTime(this.mSavedTime + (SystemClock.elapsedRealtime() - this.mSavedAtTime));
            }
        }
    }

    private void revertToNitzTimeZone() {
        if (Global.getInt(this.mCr, "auto_time_zone", 0) != 0) {
            String tmpLog = "Reverting to NITZ TimeZone: tz=" + this.mSavedTimeZone;
            log(tmpLog);
            this.mTimeZoneLog.log(tmpLog);
            if (this.mSavedTimeZone != null) {
                setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
            }
        }
    }

    private void cancelAllNotifications() {
        log("setNotification: cancelAllNotifications");
        ((NotificationManager) this.mPhone.getContext().getSystemService("notification")).cancelAll();
    }

    public void setNotification(int notifyType) {
    }

    private int selectResourceForRejectCode(int rejCode) {
        switch (rejCode) {
            case 1:
                return 17040361;
            case 2:
                return 17040364;
            case 3:
                return 17040363;
            case 6:
                return 17040362;
            default:
                return 0;
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void queueNextSignalStrengthPoll(boolean needDelay) {
        Message msg = obtainMessage();
        msg.what = 10;
        if (needDelay) {
            sendMessageDelayed(msg, 3000);
        } else {
            sendMessage(msg);
        }
        this.mIsInSignalPoll = true;
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mAttachedRegistrants.add(r);
        if (getCurrentDataConnectionState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionAttached(Handler h) {
        this.mAttachedRegistrants.remove(h);
    }

    public void registerForDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDetachedRegistrants.add(r);
        if (getCurrentDataConnectionState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(Handler h) {
        this.mDetachedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mDataRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForDataRegStateOrRatChanged(Handler h) {
        this.mDataRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForNetworkAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkAttachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkAttached(Handler h) {
        this.mNetworkAttachedRegistrants.remove(h);
    }

    public void registerForNetworkDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkDetachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkDetached(Handler h) {
        this.mNetworkDetachedRegistrants.remove(h);
    }

    public void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictEnabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedEnabled(Handler h) {
        this.mPsRestrictEnabledRegistrants.remove(h);
    }

    public void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictDisabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedDisabled(Handler h) {
        this.mPsRestrictDisabledRegistrants.remove(h);
    }

    /* JADX WARNING: Missing block: B:20:0x0051, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                Message msg;
                int i;
                if (this.mPhone.isPhoneTypeGsm() || this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                    if (!dcTracker.isDisconnected() || (dds != this.mPhone.getSubId() && (dds == this.mPhone.getSubId() || !ProxyController.getInstance().isDataDisconnected(dds)))) {
                        if (this.mPhone.isPhoneTypeGsm() && this.mPhone.isInCall()) {
                            this.mPhone.mCT.mRingingCall.hangupIfAlive();
                            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                        }
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (!(dds == this.mPhone.getSubId() || (ProxyController.getInstance().isDataDisconnected(dds) ^ 1) == 0)) {
                            log("Data is active on DDS.  Wait for all data disconnect");
                            ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49, null);
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        }
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 10000)) {
                            log("Wait upto 10s for data to disconnect, then turn off radio.");
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                            this.mPendingRadioPowerOffAfterDataOff = false;
                        }
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    }
                } else {
                    String[] networkNotClearData = this.mPhone.getContext().getResources().getStringArray(17236052);
                    String currentNetwork = this.mSS.getOperatorNumeric();
                    if (!(networkNotClearData == null || currentNetwork == null)) {
                        for (Object equals : networkNotClearData) {
                            if (currentNetwork.equals(equals)) {
                                log("Not disconnecting data for " + currentNetwork);
                                hangupAndPowerOff();
                                return;
                            }
                        }
                    }
                    if (dcTracker.isDisconnected()) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 30000)) {
                            log("Wait upto 30s for data to disconnect, then turn off radio.");
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                        }
                    }
                }
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (this.mPendingRadioPowerOffAfterDataOff) {
                log("Process pending request to turn radio off.");
                this.mPendingRadioPowerOffAfterDataOffTag++;
                hangupAndPowerOff();
                this.mPendingRadioPowerOffAfterDataOff = false;
                return true;
            }
            return false;
        }
    }

    private boolean containsEarfcnInEarfcnRange(ArrayList<Pair<Integer, Integer>> earfcnPairList, int earfcn) {
        if (earfcnPairList != null) {
            for (Pair<Integer, Integer> earfcnPair : earfcnPairList) {
                if (earfcn >= ((Integer) earfcnPair.first).intValue() && earfcn <= ((Integer) earfcnPair.second).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    ArrayList<Pair<Integer, Integer>> convertEarfcnStringArrayToPairList(String[] earfcnsList) {
        ArrayList<Pair<Integer, Integer>> earfcnPairList = new ArrayList();
        if (earfcnsList != null) {
            int i = 0;
            while (i < earfcnsList.length) {
                try {
                    String[] earfcns = earfcnsList[i].split("-");
                    if (earfcns.length != 2) {
                        return null;
                    }
                    int earfcnStart = Integer.parseInt(earfcns[0]);
                    int earfcnEnd = Integer.parseInt(earfcns[1]);
                    if (earfcnStart > earfcnEnd) {
                        return null;
                    }
                    earfcnPairList.add(new Pair(Integer.valueOf(earfcnStart), Integer.valueOf(earfcnEnd)));
                    i++;
                } catch (PatternSyntaxException e) {
                    return null;
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return earfcnPairList;
    }

    private void updateLteEarfcnLists() {
        PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        synchronized (this.mLteRsrpBoostLock) {
            this.mLteRsrpBoost = b.getInt("lte_earfcns_rsrp_boost_int", 0);
            this.mEarfcnPairListForRsrpBoost = convertEarfcnStringArrayToPairList(b.getStringArray("boosted_lte_earfcns_string_array"));
        }
    }

    private void updateServiceStateLteEarfcnBoost(ServiceState serviceState, int lteEarfcn) {
        synchronized (this.mLteRsrpBoostLock) {
            if (lteEarfcn != -1) {
                if (containsEarfcnInEarfcnRange(this.mEarfcnPairListForRsrpBoost, lteEarfcn)) {
                    serviceState.setLteEarfcnRsrpBoost(this.mLteRsrpBoost);
                }
            }
            serviceState.setLteEarfcnRsrpBoost(0);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int gsmBss(int gsmDbm) {
        if (this.mGsmBssWorking) {
            int up_threshold = gsmDbm - this.mGsmDbmAvg;
            if (up_threshold >= 4) {
                this.mGsmDbm[0] = gsmDbm;
                this.mGsmDbm[1] = gsmDbm;
                this.mGsmDbm[2] = gsmDbm;
                this.mGsmDbm[3] = gsmDbm;
                this.mGsmDbm[4] = gsmDbm;
                this.mGsmDbm[5] = gsmDbm;
                this.mGsmDbm[6] = gsmDbm;
                this.mGsmDbm[7] = gsmDbm;
                this.mGsmDownCounter = 0;
                this.mGsmDownSum = 0;
            } else if (up_threshold < -2) {
                this.mGsmDownCounter++;
                this.mGsmDownSum += gsmDbm;
                if (this.mGsmDownCounter >= 8) {
                    int avgDbm = this.mGsmDownSum / this.mGsmDownCounter;
                    this.mGsmDbm[0] = avgDbm;
                    this.mGsmDbm[1] = avgDbm;
                    this.mGsmDbm[2] = avgDbm;
                    this.mGsmDbm[3] = avgDbm;
                    this.mGsmDbm[4] = avgDbm;
                    this.mGsmDbm[5] = avgDbm;
                    this.mGsmDbm[6] = avgDbm;
                    this.mGsmDbm[7] = avgDbm;
                    this.mGsmDownCounter = 0;
                    this.mGsmDownSum = 0;
                }
            } else {
                this.mGsmDbm[0] = this.mGsmDbm[1];
                this.mGsmDbm[1] = this.mGsmDbm[2];
                this.mGsmDbm[2] = this.mGsmDbm[3];
                this.mGsmDbm[3] = this.mGsmDbm[4];
                this.mGsmDbm[4] = this.mGsmDbm[5];
                this.mGsmDbm[5] = this.mGsmDbm[6];
                this.mGsmDbm[6] = this.mGsmDbm[7];
                this.mGsmDbm[7] = gsmDbm;
                this.mGsmDownCounter = 0;
                this.mGsmDownSum = 0;
            }
        } else {
            log("initialize gsmBss");
            this.mGsmDbm[0] = gsmDbm;
            this.mGsmDbm[1] = gsmDbm;
            this.mGsmDbm[2] = gsmDbm;
            this.mGsmDbm[3] = gsmDbm;
            this.mGsmDbm[4] = gsmDbm;
            this.mGsmDbm[5] = gsmDbm;
            this.mGsmDbm[6] = gsmDbm;
            this.mGsmDbm[7] = gsmDbm;
            this.mGsmBssWorking = true;
        }
        this.mGsmDbmAvg = (((((((this.mGsmDbm[0] + this.mGsmDbm[1]) + this.mGsmDbm[2]) + this.mGsmDbm[3]) + this.mGsmDbm[4]) + this.mGsmDbm[5]) + this.mGsmDbm[6]) + this.mGsmDbm[7]) / 8;
        log("gsmBss mGsmDbmAvg = " + this.mGsmDbmAvg + " mGsmDbm = [" + this.mGsmDbm[0] + "," + this.mGsmDbm[1] + "," + this.mGsmDbm[2] + "," + this.mGsmDbm[3] + this.mGsmDbm[4] + "," + this.mGsmDbm[5] + "," + this.mGsmDbm[6] + "," + this.mGsmDbm[7] + "] mGsmDownCounter = " + this.mGsmDownCounter + " mGsmDownSum = " + this.mGsmDownSum);
        return this.mGsmDbmAvg;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int tdBss(int tdRscp) {
        if (this.mTdBssWorking) {
            int up_threshold = tdRscp - this.mTdRscpAvg;
            if (up_threshold >= 4) {
                this.mTdRscp[0] = tdRscp;
                this.mTdRscp[1] = tdRscp;
                this.mTdRscp[2] = tdRscp;
                this.mTdRscp[3] = tdRscp;
                this.mTdRscp[4] = tdRscp;
                this.mTdRscp[5] = tdRscp;
                this.mTdRscp[6] = tdRscp;
                this.mTdRscp[7] = tdRscp;
                this.mTdDownCounter = 0;
                this.mTdDownSum = 0;
            } else if (up_threshold < -2) {
                this.mTdDownCounter++;
                this.mTdDownSum += tdRscp;
                if (this.mTdDownCounter >= 8) {
                    int avgDbm = this.mTdDownSum / this.mTdDownCounter;
                    this.mTdRscp[0] = avgDbm;
                    this.mTdRscp[1] = avgDbm;
                    this.mTdRscp[2] = avgDbm;
                    this.mTdRscp[3] = avgDbm;
                    this.mTdRscp[4] = avgDbm;
                    this.mTdRscp[5] = avgDbm;
                    this.mTdRscp[6] = avgDbm;
                    this.mTdRscp[7] = avgDbm;
                    this.mTdDownCounter = 0;
                    this.mTdDownSum = 0;
                }
            } else {
                this.mTdRscp[0] = this.mTdRscp[1];
                this.mTdRscp[1] = this.mTdRscp[2];
                this.mTdRscp[2] = this.mTdRscp[3];
                this.mTdRscp[4] = this.mTdRscp[4];
                this.mTdRscp[5] = this.mTdRscp[5];
                this.mTdRscp[6] = this.mTdRscp[6];
                this.mTdRscp[7] = tdRscp;
                this.mTdDownCounter = 0;
                this.mTdDownSum = 0;
            }
        } else {
            log("initialize tdBss");
            this.mTdRscp[0] = tdRscp;
            this.mTdRscp[1] = tdRscp;
            this.mTdRscp[2] = tdRscp;
            this.mTdRscp[3] = tdRscp;
            this.mTdRscp[4] = tdRscp;
            this.mTdRscp[5] = tdRscp;
            this.mTdRscp[6] = tdRscp;
            this.mTdRscp[7] = tdRscp;
            this.mTdBssWorking = true;
        }
        this.mTdRscpAvg = (((((((this.mTdRscp[0] + this.mTdRscp[1]) + this.mTdRscp[2]) + this.mTdRscp[3]) + this.mTdRscp[4]) + this.mTdRscp[5]) + this.mTdRscp[6]) + this.mTdRscp[7]) / 8;
        log("tdBss mTdRscpAvg = " + this.mTdRscpAvg + " mTdRscp = [" + this.mTdRscp[0] + "," + this.mTdRscp[1] + "," + this.mTdRscp[2] + "," + this.mTdRscp[3] + this.mTdRscp[4] + "," + this.mTdRscp[5] + "," + this.mTdRscp[6] + "," + this.mTdRscp[7] + "] mTdDownCounter = " + this.mTdDownCounter + " mTdDownSum = " + this.mTdDownSum);
        return this.mTdRscpAvg;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int lteBss(int lteRsrp) {
        if (this.mLteBssWorking) {
            int up_threshold = lteRsrp - this.mLteRsrpAvg;
            if (up_threshold >= 4) {
                this.mLteRsrp[0] = lteRsrp;
                this.mLteRsrp[1] = lteRsrp;
                this.mLteRsrp[2] = lteRsrp;
                this.mLteRsrp[3] = lteRsrp;
                this.mLteRsrp[4] = lteRsrp;
                this.mLteRsrp[5] = lteRsrp;
                this.mLteRsrp[6] = lteRsrp;
                this.mLteRsrp[7] = lteRsrp;
                this.mLteDownCounter = 0;
                this.mLteDownSum = 0;
            } else if (up_threshold < -2) {
                this.mLteDownCounter++;
                this.mLteDownSum += lteRsrp;
                if (this.mLteDownCounter >= 8) {
                    int avgDbm = this.mLteDownSum / this.mLteDownCounter;
                    this.mLteRsrp[0] = avgDbm;
                    this.mLteRsrp[1] = avgDbm;
                    this.mLteRsrp[2] = avgDbm;
                    this.mLteRsrp[3] = avgDbm;
                    this.mLteRsrp[4] = avgDbm;
                    this.mLteRsrp[5] = avgDbm;
                    this.mLteRsrp[6] = avgDbm;
                    this.mLteRsrp[7] = avgDbm;
                    this.mLteDownCounter = 0;
                    this.mLteDownSum = 0;
                }
            } else {
                this.mLteRsrp[0] = this.mLteRsrp[1];
                this.mLteRsrp[1] = this.mLteRsrp[2];
                this.mLteRsrp[2] = this.mLteRsrp[3];
                this.mLteRsrp[3] = this.mLteRsrp[4];
                this.mLteRsrp[4] = this.mLteRsrp[5];
                this.mLteRsrp[5] = this.mLteRsrp[6];
                this.mLteRsrp[6] = this.mLteRsrp[7];
                this.mLteRsrp[7] = lteRsrp;
                this.mLteDownCounter = 0;
                this.mLteDownSum = 0;
            }
        } else {
            log("initialize lteBss");
            this.mLteRsrp[0] = lteRsrp;
            this.mLteRsrp[1] = lteRsrp;
            this.mLteRsrp[2] = lteRsrp;
            this.mLteRsrp[3] = lteRsrp;
            this.mLteRsrp[4] = lteRsrp;
            this.mLteRsrp[5] = lteRsrp;
            this.mLteRsrp[6] = lteRsrp;
            this.mLteRsrp[7] = lteRsrp;
            this.mLteBssWorking = true;
        }
        this.mLteRsrpAvg = (((((((this.mLteRsrp[0] + this.mLteRsrp[1]) + this.mLteRsrp[2]) + this.mLteRsrp[3]) + this.mLteRsrp[4]) + this.mLteRsrp[5]) + this.mLteRsrp[6]) + this.mLteRsrp[7]) / 8;
        log("lteBss mLteRsrpAvg = " + this.mLteRsrpAvg + " mLteRsrp = [" + this.mLteRsrp[0] + "," + this.mLteRsrp[1] + "," + this.mLteRsrp[2] + "," + this.mLteRsrp[3] + "," + this.mLteRsrp[4] + "," + this.mLteRsrp[5] + "," + this.mLteRsrp[6] + "," + this.mLteRsrp[7] + "] mLteDownCounter = " + this.mLteDownCounter + " mLteDownSum = " + this.mLteDownSum);
        return this.mLteRsrpAvg;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int cdmaBss(int cdmaDbm) {
        if (this.mCdmaBssWorking) {
            int up_threshold = cdmaDbm - this.mCdmaDbmAvg;
            if (up_threshold >= 4) {
                this.mCdmaDbm[0] = cdmaDbm;
                this.mCdmaDbm[1] = cdmaDbm;
                this.mCdmaDbm[2] = cdmaDbm;
                this.mCdmaDbm[3] = cdmaDbm;
                this.mCdmaDbm[4] = cdmaDbm;
                this.mCdmaDbm[5] = cdmaDbm;
                this.mCdmaDbm[6] = cdmaDbm;
                this.mCdmaDbm[7] = cdmaDbm;
                this.mCdmaDownCounter = 0;
                this.mCdmaDownSum = 0;
            } else if (up_threshold < -2) {
                this.mCdmaDownCounter++;
                this.mCdmaDownSum += cdmaDbm;
                if (this.mCdmaDownCounter >= 8) {
                    int avgDbm = this.mCdmaDownSum / this.mCdmaDownCounter;
                    this.mCdmaDbm[0] = avgDbm;
                    this.mCdmaDbm[1] = avgDbm;
                    this.mCdmaDbm[2] = avgDbm;
                    this.mCdmaDbm[3] = avgDbm;
                    this.mCdmaDbm[4] = avgDbm;
                    this.mCdmaDbm[5] = avgDbm;
                    this.mCdmaDbm[6] = avgDbm;
                    this.mCdmaDbm[7] = avgDbm;
                    this.mCdmaDownCounter = 0;
                    this.mCdmaDownSum = 0;
                }
            } else {
                this.mCdmaDbm[0] = this.mCdmaDbm[1];
                this.mCdmaDbm[1] = this.mCdmaDbm[2];
                this.mCdmaDbm[2] = this.mCdmaDbm[3];
                this.mCdmaDbm[3] = this.mCdmaDbm[4];
                this.mCdmaDbm[4] = this.mCdmaDbm[5];
                this.mCdmaDbm[5] = this.mCdmaDbm[6];
                this.mCdmaDbm[6] = this.mCdmaDbm[7];
                this.mCdmaDbm[7] = cdmaDbm;
                this.mCdmaDownCounter = 0;
                this.mCdmaDownSum = 0;
            }
        } else {
            log("initialize cdmaBss");
            this.mCdmaDbm[0] = cdmaDbm;
            this.mCdmaDbm[1] = cdmaDbm;
            this.mCdmaDbm[2] = cdmaDbm;
            this.mCdmaDbm[3] = cdmaDbm;
            this.mCdmaDbm[4] = cdmaDbm;
            this.mCdmaDbm[5] = cdmaDbm;
            this.mCdmaDbm[6] = cdmaDbm;
            this.mCdmaDbm[7] = cdmaDbm;
            this.mCdmaBssWorking = true;
        }
        this.mCdmaDbmAvg = (((((((this.mCdmaDbm[0] + this.mCdmaDbm[1]) + this.mCdmaDbm[2]) + this.mCdmaDbm[3]) + this.mCdmaDbm[4]) + this.mCdmaDbm[5]) + this.mCdmaDbm[6]) + this.mCdmaDbm[7]) / 8;
        log("cdmaBss mCdmaDbmAvg = " + this.mCdmaDbmAvg + " mCdmaDbm = [" + this.mCdmaDbm[0] + "," + this.mCdmaDbm[1] + "," + this.mCdmaDbm[2] + "," + this.mCdmaDbm[3] + "," + this.mCdmaDbm[4] + "," + this.mCdmaDbm[5] + "," + this.mCdmaDbm[6] + "," + this.mCdmaDbm[7] + "] mCdmaDownCounter = " + this.mCdmaDownCounter + " mCdmaDownSum = " + this.mCdmaDownSum);
        return this.mCdmaDbmAvg;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int evdoBss(int evdoDbm) {
        if (this.mEvdoBssWorking) {
            int up_threshold = evdoDbm - this.mEvdoDbmAvg;
            if (up_threshold >= 4) {
                this.mEvdoDbm[0] = evdoDbm;
                this.mEvdoDbm[1] = evdoDbm;
                this.mEvdoDbm[2] = evdoDbm;
                this.mEvdoDbm[3] = evdoDbm;
                this.mEvdoDbm[4] = evdoDbm;
                this.mEvdoDbm[5] = evdoDbm;
                this.mEvdoDbm[6] = evdoDbm;
                this.mEvdoDbm[7] = evdoDbm;
                this.mEvdoDownCounter = 0;
                this.mEvdoDownSum = 0;
            } else if (up_threshold < -2) {
                this.mEvdoDownCounter++;
                this.mEvdoDownSum += evdoDbm;
                if (this.mEvdoDownCounter >= 8) {
                    int avgDbm = this.mEvdoDownSum / this.mEvdoDownCounter;
                    this.mEvdoDbm[0] = avgDbm;
                    this.mEvdoDbm[1] = avgDbm;
                    this.mEvdoDbm[2] = avgDbm;
                    this.mEvdoDbm[3] = avgDbm;
                    this.mEvdoDbm[4] = avgDbm;
                    this.mEvdoDbm[5] = avgDbm;
                    this.mEvdoDbm[6] = avgDbm;
                    this.mEvdoDbm[7] = avgDbm;
                    this.mEvdoDownCounter = 0;
                    this.mEvdoDownSum = 0;
                }
            } else {
                this.mEvdoDbm[0] = this.mEvdoDbm[1];
                this.mEvdoDbm[1] = this.mEvdoDbm[2];
                this.mEvdoDbm[2] = this.mEvdoDbm[3];
                this.mEvdoDbm[3] = this.mEvdoDbm[4];
                this.mEvdoDbm[4] = this.mEvdoDbm[5];
                this.mEvdoDbm[5] = this.mEvdoDbm[6];
                this.mEvdoDbm[6] = this.mEvdoDbm[7];
                this.mEvdoDbm[7] = evdoDbm;
                this.mEvdoDownCounter = 0;
                this.mEvdoDownSum = 0;
            }
        } else {
            log("initialize evdoBss");
            this.mEvdoDbm[0] = evdoDbm;
            this.mEvdoDbm[1] = evdoDbm;
            this.mEvdoDbm[2] = evdoDbm;
            this.mEvdoDbm[3] = evdoDbm;
            this.mEvdoDbm[4] = evdoDbm;
            this.mEvdoDbm[5] = evdoDbm;
            this.mEvdoDbm[6] = evdoDbm;
            this.mEvdoDbm[7] = evdoDbm;
            this.mEvdoBssWorking = true;
        }
        this.mEvdoDbmAvg = (((((((this.mEvdoDbm[0] + this.mEvdoDbm[1]) + this.mEvdoDbm[2]) + this.mEvdoDbm[3]) + this.mEvdoDbm[4]) + this.mEvdoDbm[5]) + this.mEvdoDbm[6]) + this.mEvdoDbm[7]) / 8;
        log("evdoBss mEvdoDbmAvg = " + this.mEvdoDbmAvg + " mEvdoDbm = [" + this.mEvdoDbm[0] + "," + this.mEvdoDbm[1] + "," + this.mEvdoDbm[2] + "," + this.mEvdoDbm[3] + "," + this.mEvdoDbm[4] + "," + this.mEvdoDbm[5] + "," + this.mEvdoDbm[6] + "," + this.mEvdoDbm[7] + "] mEvdoDownCounter = " + this.mEvdoDownCounter + " mEvdoDownSum = " + this.mEvdoDownSum);
        return this.mEvdoDbmAvg;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected int getNeighboringRsrp() {
        int result = -140;
        if (this.mLastCellInfoList != null) {
            for (CellInfo info : this.mLastCellInfoList) {
                if (info != null && (info instanceof CellInfoLte)) {
                    CellSignalStrengthLte lteSignal = ((CellInfoLte) info).getCellSignalStrength();
                    if (lteSignal != null) {
                        int rsrp = lteSignal.getRsrp();
                        if (rsrp > -140 && rsrp <= -44 && rsrp > result) {
                            result = rsrp;
                        }
                    }
                }
            }
        }
        log("phoneId" + getPhoneId() + "--->getNeighboringRsrp = " + result);
        return result;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = this.mPhone.isPhoneTypeGsm();
        int dataRat = this.mSS.getRilDataRadioTechnology();
        int voiceRat = this.mSS.getRilVoiceRadioTechnology();
        if ((dataRat != 18 && ServiceState.isGsm(dataRat)) || (voiceRat != 18 && ServiceState.isGsm(voiceRat))) {
            isGsm = true;
        }
        if (ar.exception != null || ar.result == null) {
            this.mSignalStrength = new SignalStrength(isGsm);
        } else {
            boolean isRatGsm;
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.validateInput();
            this.mSignalStrength.setGsm(isGsm);
            this.mSignalStrength.setLteRsrpBoost(this.mSS.getLteEarfcnRsrpBoost());
            int dataNetworkType = this.mSS.getDataNetworkType();
            if (dataNetworkType == 1 || dataNetworkType == 2 || dataNetworkType == 16) {
                isRatGsm = true;
            } else if (dataNetworkType == 0) {
                int voiceNetworkType = this.mSS.getVoiceNetworkType();
                if (voiceNetworkType == 1 || voiceNetworkType == 2 || voiceNetworkType == 16) {
                    isRatGsm = true;
                } else {
                    isRatGsm = false;
                }
            } else {
                isRatGsm = false;
            }
            if (!(TelephonyPhoneUtils.sInNetEntry || (TelephonyPhoneUtils.sIsCMCCEntry ^ 1) == 0 || (TelephonyPhoneUtils.sIsUNICOMEntry ^ 1) == 0)) {
                if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology())) {
                    boolean isUeIdle = true;
                    if (this.mPhone.getDataConnectionState() == DataState.CONNECTED || this.mPhone.getState() == State.OFFHOOK) {
                        isUeIdle = false;
                    }
                    if (isUeIdle) {
                        int neighboringRsrp = getNeighboringRsrp();
                        if (neighboringRsrp > this.mSignalStrength.getLteRsrp()) {
                            this.mSignalStrength.setLteRsrp(neighboringRsrp);
                        }
                    }
                }
                if (this.mSubscriptionManager != null) {
                    int dataPhoneId = this.mSubscriptionManager.getDefaultDataPhoneId();
                    if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && dataPhoneId != getPhoneId()) {
                        Phone dataPhone = PhoneFactory.getPhone(dataPhoneId);
                        if (dataPhone != null) {
                            ServiceStateTracker dataServiceStateTracker = dataPhone.getServiceStateTracker();
                            if (dataServiceStateTracker != null) {
                                ServiceState dataSS = dataServiceStateTracker.mSS;
                                if (dataSS != null && ServiceState.isLte(dataSS.getRilDataRadioTechnology()) && TelephonyPhoneUtils.isSameOperator(this.mSS.getDataOperatorNumeric(), dataSS.getDataOperatorNumeric())) {
                                    SignalStrength dataSignal = dataPhone.getSignalStrength();
                                    if (dataSignal != null && dataSignal.getLteRsrp() > this.mSignalStrength.getLteRsrp()) {
                                        this.mSignalStrength.setLteRsrp(dataSignal.getLteRsrp());
                                        log("phoneId" + getPhoneId() + "--->double LTE, one SignalStrength");
                                    }
                                }
                            }
                        }
                    }
                }
                if (!(this.mSignalStrength.getGsmSignalStrength() == 99 || this.mSignalStrength.getGsmSignalStrength() == 0)) {
                    this.mSignalStrength.setGsmSignalStrength((gsmBss(this.mSignalStrength.getGsmDbm()) + 113) / 2);
                }
                if (this.mSignalStrength.getTdScdmaDbm() != Integer.MAX_VALUE && this.mSignalStrength.getTdScdmaDbm() <= -25) {
                    this.mSignalStrength.setTdScdmaDbm(tdBss(this.mSignalStrength.getTdScdmaDbm()));
                }
                if (this.mSignalStrength.getLteRsrp() != Integer.MAX_VALUE) {
                    this.mSignalStrength.setLteRsrp(lteBss(this.mSignalStrength.getLteRsrp()));
                }
                if (this.mSignalStrength.getCdmaDbm() != -120) {
                    this.mSignalStrength.setCdmaDbm(cdmaBss(this.mSignalStrength.getCdmaDbm()));
                }
                if (!(this.mSignalStrength.getEvdoDbm() == -120 || (isGsm ^ 1) == 0)) {
                    this.mSignalStrength.setEvdoDbm(evdoBss(this.mSignalStrength.getEvdoDbm()));
                }
            }
            if (isRatGsm) {
                this.mSignalStrength.setGsmLevel(this.mGsmAsuThresh);
            } else {
                this.mSignalStrength.setWcdmaLevel(this.mWcdmaAsuThresh);
            }
            this.mSignalStrength.setTdScdmaLevel(this.mTdScdmaThresh);
            this.mSignalStrength.setCdmaLevel(this.mCdmaRxPowerThresh);
            this.mSignalStrength.setEvdoLevel(this.mEvdoRxPowerThresh);
            this.mSignalStrength.setLteLevel(this.mLteRsrpThresh);
            log("phoneId" + getPhoneId() + "--->after bss mSignalStrength = " + this.mSignalStrength.toString());
        }
        if (!(TelephonyPhoneUtils.sIsCMCCEntry || (TelephonyPhoneUtils.sIsUNICOMEntry ^ 1) == 0 || this.mSignalStrength == null || this.mLastSignalStrength == null || this.mSignalStrength.getLevel() != 0)) {
            log("phoneId" + getPhoneId() + "--->no empty signal");
            this.mSignalStrength = this.mLastSignalStrength;
        }
        return notifySignalStrength();
    }

    protected void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        this.mCi.setRadioPower(false, obtainMessage(53));
    }

    protected void cancelPollState() {
        this.mPollingContext = new int[1];
    }

    protected boolean shouldFixTimeZoneNow(Phone phone, String operatorNumeric, String prevOperatorNumeric, boolean needToFixTimeZone) {
        try {
            int prevMcc;
            int mcc = Integer.parseInt(operatorNumeric.substring(0, 3));
            try {
                prevMcc = Integer.parseInt(prevOperatorNumeric.substring(0, 3));
            } catch (Exception e) {
                prevMcc = mcc + 1;
            }
            boolean iccCardExist = false;
            if (this.mUiccApplcation != null) {
                iccCardExist = this.mUiccApplcation.getState() != AppState.APPSTATE_UNKNOWN;
            }
            boolean retVal = (!iccCardExist || mcc == prevMcc) ? needToFixTimeZone : true;
            log("shouldFixTimeZoneNow: retVal=" + retVal + " iccCardExist=" + iccCardExist + " operatorNumeric=" + operatorNumeric + " mcc=" + mcc + " prevOperatorNumeric=" + prevOperatorNumeric + " prevMcc=" + prevMcc + " needToFixTimeZone=" + needToFixTimeZone + " ltod=" + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
            return retVal;
        } catch (Exception e2) {
            log("shouldFixTimeZoneNow: no mcc, operatorNumeric=" + operatorNumeric + " retVal=false");
            return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo(WorkSource workSource) {
        CellInfoResult result = new CellInfoResult();
        if (this.mCi.getRilVersion() < 8) {
            log("SST.getAllCellInfo(): not implemented");
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            log("SST.getAllCellInfo(): return last, same thread can't block");
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > 10000) {
            Message msg = obtainMessage(43, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mCi.getCellInfoList(msg, workSource);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log("SST.getAllCellInfo(): return last, back to back calls");
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            }
            log("SST.getAllCellInfo(): X size=0 list=null");
            return null;
        }
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mCdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }

    private void saveCdmaSubscriptionSource(int source) {
        log("Storing cdma subscription source: " + source);
        Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    private void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(34));
        pollState();
    }

    private void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        boolean z = false;
        log("Subscription Source : " + newSubscriptionSource);
        if (newSubscriptionSource == 0) {
            z = true;
        }
        this.mIsSubscriptionFromRuim = z;
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (!this.mIsSubscriptionFromRuim) {
            sendMessage(obtainMessage(35));
        }
    }

    private void dumpEarfcnPairList(PrintWriter pw) {
        pw.print(" mEarfcnPairListForRsrpBoost={");
        if (this.mEarfcnPairListForRsrpBoost != null) {
            int i = this.mEarfcnPairListForRsrpBoost.size();
            for (Pair<Integer, Integer> earfcnPair : this.mEarfcnPairListForRsrpBoost) {
                pw.print("(");
                pw.print(earfcnPair.first);
                pw.print(",");
                pw.print(earfcnPair.second);
                pw.print(")");
                i--;
                if (i != 0) {
                    pw.print(",");
                }
            }
        }
        pw.println("}");
    }

    private void dumpCellInfoList(PrintWriter pw) {
        pw.print(" mLastCellInfoList={");
        if (this.mLastCellInfoList != null) {
            boolean first = true;
            for (CellInfo info : this.mLastCellInfoList) {
                if (!first) {
                    pw.print(",");
                }
                first = false;
                pw.print(info.toString());
            }
        }
        pw.println("}");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPollingContext=" + this.mPollingContext + " - " + (this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[0]) : ""));
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + Rlog.pii(false, this.mCellLoc));
        pw.println(" mNewCellLoc=" + Rlog.pii(false, this.mNewCellLoc));
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        dumpCellInfoList(pw);
        pw.flush();
        pw.println(" mPreferredNetworkType=" + this.mPreferredNetworkType);
        pw.println(" mMaxDataCalls=" + this.mMaxDataCalls);
        pw.println(" mNewMaxDataCalls=" + this.mNewMaxDataCalls);
        pw.println(" mReasonDataDenied=" + this.mReasonDataDenied);
        pw.println(" mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        pw.println(" mGsmRoaming=" + this.mGsmRoaming);
        pw.println(" mDataRoaming=" + this.mDataRoaming);
        pw.println(" mEmergencyOnly=" + this.mEmergencyOnly);
        pw.println(" mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
        pw.flush();
        pw.println(" mZoneOffset=" + this.mZoneOffset);
        pw.println(" mZoneDst=" + this.mZoneDst);
        pw.println(" mZoneTime=" + this.mZoneTime);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mNitzUpdatedTime=" + this.mNitzUpdatedTime);
        pw.println(" mSavedTimeZone=" + this.mSavedTimeZone);
        pw.println(" mSavedTime=" + this.mSavedTime);
        pw.println(" mSavedAtTime=" + this.mSavedAtTime);
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.flush();
        pw.println(" mCurrentOtaspMode=" + this.mCurrentOtaspMode);
        pw.println(" mRoamingIndicator=" + this.mRoamingIndicator);
        pw.println(" mIsInPrl=" + this.mIsInPrl);
        pw.println(" mDefaultRoamingIndicator=" + this.mDefaultRoamingIndicator);
        pw.println(" mRegistrationState=" + this.mRegistrationState);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mIsMinInfoReady=" + this.mIsMinInfoReady);
        pw.println(" mIsEriTextLoaded=" + this.mIsEriTextLoaded);
        pw.println(" mIsSubscriptionFromRuim=" + this.mIsSubscriptionFromRuim);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRegistrationDeniedReason=" + this.mRegistrationDeniedReason);
        pw.println(" mCurrentCarrier=" + this.mCurrentCarrier);
        pw.flush();
        pw.println(" mImsRegistered=" + this.mImsRegistered);
        pw.println(" mImsRegistrationOnOff=" + this.mImsRegistrationOnOff);
        pw.println(" mAlarmSwitch=" + this.mAlarmSwitch);
        pw.println(" mRadioDisabledByCarrier" + this.mRadioDisabledByCarrier);
        pw.println(" mPowerOffDelayNeed=" + this.mPowerOffDelayNeed);
        pw.println(" mDeviceShuttingDown=" + this.mDeviceShuttingDown);
        pw.println(" mSpnUpdatePending=" + this.mSpnUpdatePending);
        pw.println(" mLteRsrpBoost=" + this.mLteRsrpBoost);
        dumpEarfcnPairList(pw);
        pw.println(" Roaming Log:");
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        this.mRoamingLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Attach Log:");
        ipw.increaseIndent();
        this.mAttachLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Phone Change Log:");
        ipw.increaseIndent();
        this.mPhoneTypeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Rat Change Log:");
        ipw.increaseIndent();
        this.mRatLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Radio power Log:");
        ipw.increaseIndent();
        this.mRadioPowerLog.dump(fd, ipw, args);
        ipw.println(" Time Logs:");
        ipw.increaseIndent();
        this.mTimeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Time zone Logs:");
        ipw.increaseIndent();
        this.mTimeZoneLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    protected void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    protected boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread();
    }

    protected void updateCarrierMccMncConfiguration(String newOp, String oldOp, Context context) {
        if ((newOp == null && !TextUtils.isEmpty(oldOp)) || (newOp != null && !newOp.equals(oldOp))) {
            log("update mccmnc=" + newOp + " fromServiceState=true");
            MccTable.updateMccMncConfiguration(context, newOp, true);
        }
    }

    protected boolean inSameCountry(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < 5) {
            return false;
        }
        String homeNumeric = getHomeOperatorNumeric();
        if (TextUtils.isEmpty(homeNumeric) || homeNumeric.length() < 5) {
            return false;
        }
        String networkMCC = operatorNumeric.substring(0, 3);
        String homeMCC = homeNumeric.substring(0, 3);
        String networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
        String homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            return false;
        }
        boolean inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = true;
        }
        return inSameCountry;
    }

    protected void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17235991);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    for (int i : intRoamingIndicators) {
                        if (curRoamingIndicator == i) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
            } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                currentServiceState.setVoiceRoamingType(2);
            } else {
                currentServiceState.setVoiceRoamingType(3);
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(0);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(1);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(2);
        } else {
            currentServiceState.setDataRoamingType(3);
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }

    protected String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "");
    }

    protected int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    protected void resetServiceStateInIwlanMode() {
        if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            boolean resetIwlanRatVal = false;
            log("set service state as POWER_OFF");
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                log("pollStateDone: mNewSS = " + this.mNewSS);
                log("pollStateDone: reset iwlan RAT value");
                resetIwlanRatVal = true;
            }
            String operator = this.mNewSS.getOperatorAlphaLong();
            this.mNewSS.setStateOff();
            if (resetIwlanRatVal) {
                this.mNewSS.setRilDataRadioTechnology(18);
                this.mNewSS.setDataRegState(0);
                this.mNewSS.setOperatorAlphaLong(operator);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        }
    }

    protected final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return false;
        }
        return true;
    }

    protected final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    protected final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    protected int getCombinedRegState() {
        int regState = this.mSS.getVoiceRegState();
        int dataRegState = this.mSS.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        log("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }
}
