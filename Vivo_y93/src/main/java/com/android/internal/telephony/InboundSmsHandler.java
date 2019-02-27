package com.android.internal.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.ActivityManager;
import android.app.BroadcastOptions;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.telephony.CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codeaurora.ims.QtiCallConstants;

public abstract class InboundSmsHandler extends StateMachine {
    private static String ACTION_OPEN_SMS_APP = "com.android.internal.telephony.OPEN_DEFAULT_SMS_APP";
    public static final int ADDRESS_COLUMN = 6;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    static final long BROADCASST_SMS_WAIT_TIME = 43200000;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DESTINATION_PORT_COLUMN = 2;
    public static final int DISPLAY_ADDRESS_COLUMN = 9;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    private static String ERROR_TAG = NOTIFICATION_TAG;
    private static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_STATE_TIMEOUT = 10;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    private static final int EVENT_UPDATE_TRACKER = 9;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    static final int HANDLE_PROCESS_PART_MESSAGES = 1;
    public static final int ID_COLUMN = 7;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    static final long LONG_SMS_RECEIVE_INTERVAL = 20000;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = new String[]{"pdu"};
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = new String[]{"pdu", "sequence", "destination_port", "recv_time", "display_originating_addr"};
    private static final Map<Integer, Integer> PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(0), Integer.valueOf(0));
            put(Integer.valueOf(1), Integer.valueOf(1));
            put(Integer.valueOf(2), Integer.valueOf(2));
            put(Integer.valueOf(3), Integer.valueOf(3));
            put(Integer.valueOf(9), Integer.valueOf(4));
        }
    };
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    public static final int STATE_TIMEOUT = 300000;
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    @VivoHook(hookType = VivoHookType.NEW_FIELD)
    static boolean sIsEvdo = false;
    protected static final Uri sRawUri = Uri.withAppendedPath(Sms.CONTENT_URI, "raw");
    protected static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Sms.CONTENT_URI, "raw/permanentDelete");
    private final int DELETE_PERMANENTLY = 1;
    private final int MARK_DELETED = 2;
    protected CellBroadcastHandler mCellBroadcastHandler;
    protected final Context mContext;
    private final DefaultState mDefaultState = new DefaultState(this, null);
    private final DeliveringState mDeliveringState = new DeliveringState(this, null);
    IDeviceIdleController mDeviceIdleController;
    private final IdleState mIdleState = new IdleState(this, null);
    protected Phone mPhone;
    private final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState = new StartupState(this, null);
    protected SmsStorageMonitor mStorageMonitor;
    private UserManager mUserManager;
    private final WaitingState mWaitingState = new WaitingState(this, null);
    private final WakeLock mWakeLock;
    private int mWakeLockTimeout;
    private final WapPushOverSms mWapPush;

    private final class CarrierServicesSmsFilterCallback implements CarrierServicesSmsFilterCallbackInterface {
        private final int mDestPort;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        private boolean mNeedAddFlag = false;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private final String mSmsFormat;
        private final boolean mUserUnlocked;

        CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
        }

        @VivoHook(hookType = VivoHookType.NEW_METHOD)
        CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked, boolean isNeedAddFlag) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
            this.mNeedAddFlag = isNeedAddFlag;
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public void onFilterComplete(int result) {
            InboundSmsHandler.this.logv("onFilterComplete: result is " + result);
            if ((result & 1) != 0) {
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (VisualVoicemailSmsFilter.filter(InboundSmsHandler.this.mContext, this.mPdus, this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId())) {
                InboundSmsHandler.this.log("Visual voicemail SMS dropped");
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mPdus, this.mSmsFormat, this.mDestPort, this.mSmsBroadcastReceiver, this.mNeedAddFlag);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }
    }

    private class DefaultState extends State {
        /* synthetic */ DefaultState(InboundSmsHandler this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    InboundSmsHandler.this.onUpdatePhoneObject((Phone) msg.obj);
                    break;
                default:
                    String errorText = "processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName();
                    if (!Build.IS_DEBUGGABLE) {
                        InboundSmsHandler.this.loge(errorText);
                        break;
                    }
                    InboundSmsHandler.this.loge("---- Dumping InboundSmsHandler ----");
                    InboundSmsHandler.this.loge("Total records=" + InboundSmsHandler.this.getLogRecCount());
                    for (int i = Math.max(InboundSmsHandler.this.getLogRecSize() - 20, 0); i < InboundSmsHandler.this.getLogRecSize(); i++) {
                        InboundSmsHandler.this.loge("Rec[%d]: %s\n" + i + InboundSmsHandler.this.getLogRec(i).toString());
                    }
                    InboundSmsHandler.this.loge("---- Dumped InboundSmsHandler ----");
                    throw new RuntimeException(errorText);
            }
            return true;
        }
    }

    private class DeliveringState extends State {
        /* synthetic */ DeliveringState(InboundSmsHandler this$0, DeliveringState -this1) {
            this();
        }

        private DeliveringState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Delivering state");
        }

        public void exit() {
            InboundSmsHandler.this.log("leaving Delivering state");
        }

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    try {
                        InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                    } catch (Exception ex) {
                        InboundSmsHandler.this.processCrashLog();
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
                    }
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                case 2:
                    boolean result = true;
                    try {
                        result = InboundSmsHandler.this.processMessagePart(msg.obj);
                    } catch (Exception ex2) {
                        InboundSmsHandler.this.processCrashLog();
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex2);
                    }
                    if (result) {
                        InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mWaitingState);
                    } else {
                        InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        InboundSmsHandler.this.sendMessage(4);
                    }
                    return true;
                case 4:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return true;
                case 8:
                    InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class IdleState extends State {
        /* synthetic */ IdleState(InboundSmsHandler this$0, IdleState -this1) {
            this();
        }

        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler.this.sendMessageDelayed(5, (long) InboundSmsHandler.this.getWakeLockTimeout());
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler.this.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        /* synthetic */ NewMessageNotificationActionReceiver(NewMessageNotificationActionReceiver -this0) {
            this();
        }

        private NewMessageNotificationActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (InboundSmsHandler.ACTION_OPEN_SMS_APP.equals(intent.getAction())) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(Sms.getDefaultSmsPackage(context)));
            }
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano;
        @VivoHook(hookType = VivoHookType.NEW_FIELD)
        private boolean mCanDelete = true;
        private final String mDeleteWhere;
        private final String[] mDeleteWhereArgs;

        @VivoHook(hookType = VivoHookType.CHANGE_CODE)
        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
            this.mBroadcastTimeNano = System.nanoTime();
            this.mCanDelete = tracker.getCanDeleteFlag();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.provider.Telephony.SMS_DELIVER")) {
                intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
                intent.setComponent(null);
                Intent intent2 = intent;
                InboundSmsHandler.this.dispatchIntent(intent2, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null), this, UserHandle.ALL);
            } else if (action.equals("android.provider.Telephony.WAP_PUSH_DELIVER")) {
                intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                intent.setComponent(null);
                intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
                Bundle options = null;
                try {
                    long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                    BroadcastOptions bopts = BroadcastOptions.makeBasic();
                    bopts.setTemporaryAppWhitelistDuration(duration);
                    options = bopts.toBundle();
                } catch (RemoteException e) {
                }
                String mimeType = intent.getType();
                InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
            } else {
                if (!("android.intent.action.DATA_SMS_RECEIVED".equals(action) || ("android.provider.Telephony.SMS_RECEIVED".equals(action) ^ 1) == 0 || ("android.intent.action.DATA_SMS_RECEIVED".equals(action) ^ 1) == 0 || ("android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action) ^ 1) == 0)) {
                    InboundSmsHandler.this.loge("unexpected BroadcastReceiver action: " + action);
                }
                int rc = getResultCode();
                if (rc == -1 || rc == 1) {
                    InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                } else {
                    InboundSmsHandler.this.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                }
                if (!InboundSmsHandler.sIsEvdo) {
                    InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                    InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                } else if (this.mCanDelete) {
                    InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                    InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                }
                InboundSmsHandler.this.sendMessage(3);
                int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                if (durationMillis >= 5000) {
                    InboundSmsHandler.this.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                } else {
                    InboundSmsHandler.this.log("ordered broadcast completed in: " + durationMillis + " ms");
                }
            }
        }
    }

    private class StartupState extends State {
        /* synthetic */ StartupState(InboundSmsHandler this$0, StartupState -this1) {
            this();
        }

        private StartupState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Startup state");
            InboundSmsHandler.this.setWakeLockTimeout(0);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("StartupState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 6:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class WaitingState extends State {
        private InboundSmsTracker mTracker;

        /* synthetic */ WaitingState(InboundSmsHandler this$0, WaitingState -this1) {
            this();
        }

        private WaitingState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Waiting state");
            this.mTracker = null;
            InboundSmsHandler.this.sendMessageDelayed(10, 300000);
        }

        public void exit() {
            InboundSmsHandler.this.log("exiting Waiting state");
            InboundSmsHandler.this.setWakeLockTimeout(InboundSmsHandler.WAKELOCK_TIMEOUT);
            InboundSmsHandler.this.removeMessages(10);
            InboundSmsHandler.this.removeMessages(9);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case 2:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 3:
                    InboundSmsHandler.this.sendMessage(4);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 9:
                    this.mTracker = (InboundSmsTracker) msg.obj;
                    return true;
                case 10:
                    if (this.mTracker != null) {
                        InboundSmsHandler.this.log("WaitingState.processMessage: EVENT_STATE_TIMEOUT; dropping message");
                        InboundSmsHandler.this.dropSms(new SmsBroadcastReceiver(this.mTracker));
                    } else {
                        InboundSmsHandler.this.log("WaitingState.processMessage: EVENT_STATE_TIMEOUT; mTracker is null - sending EVENT_BROADCAST_COMPLETE");
                        InboundSmsHandler.this.sendMessage(3);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    protected abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    protected abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    protected abstract boolean is3gpp2();

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name);
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = new WapPushOverSms(context);
        this.mSmsReceiveDisabled = TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17957018)) ^ 1;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        if (TextUtils.isEmpty(TelephonyPhoneUtils.sVivoOpEntry)) {
            sIsEvdo = false;
        } else {
            sIsEvdo = !"CTCC_RWA".equals(TelephonyPhoneUtils.sVivoOpEntry) ? "CTCC_RWB".equals(TelephonyPhoneUtils.sVivoOpEntry) : true;
        }
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(7, phone);
    }

    protected void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    private void handleNewSms(AsyncResult ar) {
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        int result;
        try {
            result = dispatchMessage(ar.result.mWrappedSmsMessage);
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            notifyAndAcknowledgeLastIncomingSms(result == 1, result, null);
        }
    }

    private void handleInjectSms(AsyncResult ar) {
        int result;
        PendingIntent pendingIntent = null;
        try {
            pendingIntent = (PendingIntent) ar.userObj;
            SmsMessage sms = ar.result;
            if (sms == null) {
                result = 2;
            } else {
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(result);
            } catch (CanceledException e) {
            }
        }
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return 2;
        }
        String gn = SystemProperties.get("ro.build.gn.support", "0");
        if ("1".equals(gn)) {
            int ctSmsReceive = Secure.getInt(this.mContext.getContentResolver(), "ct_sms_receive", 1);
            int nctSmsBlock = Secure.getInt(this.mContext.getContentResolver(), "nct_sms_block", 1);
            String imsi = this.mPhone.getSubscriberId();
            int opType = TelephonyPhoneUtils.getOperatorTypeByImsi(imsi);
            log("dispatchMessage:ctSmsReceive = " + ctSmsReceive + ",nctSmsBlock = " + nctSmsBlock + ",imsi = " + imsi + ",opType = " + opType);
            if ((opType == 2 && ctSmsReceive == 0) || (opType != 2 && nctSmsBlock == 0)) {
                log("Special version not support receive sms.");
                return 1;
            }
        } else if (!"0".equals(gn)) {
            int smsBlockSim = Secure.getInt(this.mContext.getContentResolver(), "ct_network_sms_blocksim", 0);
            int smsBlockReceive = Secure.getInt(this.mContext.getContentResolver(), "ct_network_sms_blockreceive", 1);
            if ((smsBlockSim == 0 || smsBlockSim - 1 == this.mPhone.getPhoneId()) && smsBlockReceive == 0) {
                log("Special version not support receive sms.");
                return 1;
            }
        }
        if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return 1;
        }
        boolean onlyCore = false;
        try {
            onlyCore = Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException e) {
        }
        if (!onlyCore) {
            return dispatchMessageRadioSpecific(smsb);
        }
        log("Received a short message in encrypted state. Rejecting.");
        return 2;
    }

    protected void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    protected int dispatchNormalMessage(SmsMessageBase sms) {
        SmsHeader smsHeader = sms.getUserDataHeader();
        if (SystemProperties.get("ro.build.gn.support", "0").equals("0") || !TelephonyPhoneUtils.isMDMForbidden(this.mContext, sms.getOriginatingAddress())) {
            InboundSmsTracker tracker;
            if (smsHeader == null || smsHeader.concatRef == null) {
                int destPort = -1;
                if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                    destPort = smsHeader.portAddrs.destPort;
                    log("destination port: " + destPort);
                }
                tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getMessageBody());
            } else {
                ConcatRef concatRef = smsHeader.concatRef;
                PortAddrs portAddrs = smsHeader.portAddrs;
                tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : -1, is3gpp2(), sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody());
            }
            return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == -1);
        }
        log("isMDMForbidden not support receive sms.");
        return 1;
    }

    protected int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        switch (addTrackerToRawTable(tracker, deDup)) {
            case 1:
                sendMessage(2, tracker);
                return 1;
            case 5:
                return 1;
            default:
                return 2;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private boolean processMessagePart(InboundSmsTracker tracker) {
        int messageCount;
        Object[] objArr;
        int destPort;
        String address;
        byte[][] pdus;
        Cursor cursor;
        String refNumber;
        String count;
        int index;
        int port;
        List<byte[]> pduList;
        SmsBroadcastReceiver resultReceiver;
        ByteArrayOutputStream output;
        Bundle bundle;
        SmsMessage msg;
        byte[] pdu;
        String sca;
        String str;
        int result;
        if (sIsEvdo) {
            messageCount = tracker.getMessageCount();
            if (messageCount <= 0) {
                objArr = new Object[3];
                objArr[0] = "72298611";
                objArr[1] = Integer.valueOf(-1);
                objArr[2] = String.format("processMessagePart: invalid messageCount = %d", new Object[]{Integer.valueOf(messageCount)});
                EventLog.writeEvent(1397638484, objArr);
                return false;
            }
            destPort = tracker.getDestPort();
            address = "";
            if (messageCount == 1) {
                pdus = new byte[][]{tracker.getPdu()};
                tracker.setCanDeleteFlag(true);
            } else {
                cursor = null;
                try {
                    address = tracker.getAddress();
                    refNumber = Integer.toString(tracker.getReferenceNumber());
                    count = Integer.toString(tracker.getMessageCount());
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegments(), new String[]{address, refNumber, count}, null);
                    int cursorCount = cursor.getCount();
                    log("destPort ===========" + destPort);
                    if (destPort == 2948 || destPort == 9200) {
                        if (cursorCount < messageCount) {
                            tracker.setCanDeleteFlag(false);
                            if (cursor != null) {
                                cursor.close();
                            }
                            return false;
                        }
                    } else if (cursorCount <= 0) {
                        sendMessage(4);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } else if (cursorCount < messageCount) {
                        long currentTime = System.currentTimeMillis();
                        cursor.moveToFirst();
                        long intervalTime = currentTime - cursor.getLong(3);
                        cursor.moveToPosition(-1);
                        log("intervalTime ==========" + intervalTime);
                        if (intervalTime > BROADCASST_SMS_WAIT_TIME) {
                            messageCount = cursorCount;
                            tracker.setCanDeleteFlag(true);
                        } else if (intervalTime <= ((long) (messageCount - 1)) * LONG_SMS_RECEIVE_INTERVAL || intervalTime >= BROADCASST_SMS_WAIT_TIME) {
                            if (!(intervalTime >= ((long) (messageCount - 1)) * LONG_SMS_RECEIVE_INTERVAL || getHandler() == null || (getHandler().hasMessages(2) ^ 1) == 0)) {
                                sendMessageDelayed(2, tracker, ((long) (messageCount - 1)) * LONG_SMS_RECEIVE_INTERVAL);
                            }
                            tracker.setCanDeleteFlag(false);
                            if (cursor != null) {
                                cursor.close();
                            }
                            return false;
                        } else {
                            messageCount = cursorCount;
                            tracker.setCanDeleteFlag(false);
                        }
                    } else {
                        if (getHandler() != null && getHandler().hasMessages(2)) {
                            getHandler().removeMessages(2, tracker);
                            getHandler().removeMessages(2);
                        }
                        tracker.setCanDeleteFlag(true);
                    }
                    pdus = new byte[messageCount][];
                    while (cursor.moveToNext()) {
                        index = cursor.getInt(1) - tracker.getIndexOffset();
                        if (index >= pdus.length || index < 0) {
                            objArr = new Object[3];
                            objArr[0] = "72298611";
                            objArr[1] = Integer.valueOf(-1);
                            objArr[2] = String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", new Object[]{Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount)});
                            EventLog.writeEvent(1397638484, objArr);
                        } else {
                            pdus[index] = HexDump.hexStringToByteArray(cursor.getString(0));
                            if (index == 0 && (cursor.isNull(2) ^ 1) != 0) {
                                port = InboundSmsTracker.getRealDestPort(cursor.getInt(2));
                                if (port != -1) {
                                    destPort = port;
                                }
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable e) {
                    loge("Can't access multipart SMS database", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            pduList = Arrays.asList(pdus);
            if (pduList.size() == 0 || pduList.contains(null)) {
                loge("processMessagePart: returning false due to " + (pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)"));
                return false;
            }
            resultReceiver = new SmsBroadcastReceiver(tracker);
            if (!this.mUserManager.isUserUnlocked()) {
                return processMessagePartWithUserLocked(tracker, pdus, destPort, resultReceiver);
            }
            if (destPort == 2948) {
                output = new ByteArrayOutputStream();
                bundle = new Bundle();
                for (byte[] pdu2 : pdus) {
                    if (!tracker.is3gpp2()) {
                        msg = SmsMessage.createFromPdu(pdu2, "3gpp");
                        if (msg != null) {
                            pdu2 = msg.getUserData();
                            if (address == "") {
                                address = msg.getOriginatingAddress();
                                if (address == "") {
                                    address = tracker.getAddress();
                                }
                            }
                            sca = msg.getServiceCenterAddress();
                            bundle.putString("address", address);
                            str = "service_center";
                            if (sca == null) {
                                sca = "";
                            }
                            bundle.putString(str, sca);
                        } else {
                            loge("processMessagePart: SmsMessage.createFromPdu returned null");
                            return false;
                        }
                    }
                    output.write(pdu2, 0, pdu2.length);
                }
                result = this.mWapPush.dispatchWapPdu(output.toByteArray(), bundle, resultReceiver, this);
                log("dispatchWapPdu() returned " + result);
                if (result == -1) {
                    return true;
                }
                deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                return false;
            } else if (BlockChecker.isBlocked(this.mContext, tracker.getAddress())) {
                dispatchBlockMessage(pdus, tracker.getFormat(), tracker.getDestPort());
                deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                return false;
            } else if (!filterSms(pdus, destPort, tracker, resultReceiver, true)) {
                dispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver);
            }
        } else {
            messageCount = tracker.getMessageCount();
            destPort = tracker.getDestPort();
            boolean block = false;
            if (messageCount <= 0) {
                objArr = new Object[3];
                objArr[0] = "72298611";
                objArr[1] = Integer.valueOf(-1);
                objArr[2] = String.format("processMessagePart: invalid messageCount = %d", new Object[]{Integer.valueOf(messageCount)});
                EventLog.writeEvent(1397638484, objArr);
                return false;
            }
            address = "";
            if (messageCount == 1) {
                pdus = new byte[][]{tracker.getPdu()};
                block = BlockChecker.isBlocked(this.mContext, tracker.getDisplayAddress());
            } else {
                cursor = null;
                try {
                    address = tracker.getAddress();
                    refNumber = Integer.toString(tracker.getReferenceNumber());
                    count = Integer.toString(tracker.getMessageCount());
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegments(), new String[]{address, refNumber, count}, null);
                    if (cursor.getCount() < messageCount) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    pdus = new byte[messageCount][];
                    while (cursor.moveToNext()) {
                        index = cursor.getInt(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(1))).intValue()) - tracker.getIndexOffset();
                        if (index >= pdus.length || index < 0) {
                            objArr = new Object[3];
                            objArr[0] = "72298611";
                            objArr[1] = Integer.valueOf(-1);
                            objArr[2] = String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", new Object[]{Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount)});
                            EventLog.writeEvent(1397638484, objArr);
                        } else {
                            pdus[index] = HexDump.hexStringToByteArray(cursor.getString(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(0))).intValue()));
                            if (index == 0) {
                                if ((cursor.isNull(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(2))).intValue()) ^ 1) != 0) {
                                    port = InboundSmsTracker.getRealDestPort(cursor.getInt(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(2))).intValue()));
                                    if (port != -1) {
                                        destPort = port;
                                    }
                                }
                            }
                            if (!block) {
                                block = BlockChecker.isBlocked(this.mContext, cursor.getString(((Integer) PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(9))).intValue()));
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable e2) {
                    loge("Can't access multipart SMS database", e2);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th2;
                }
            }
            pduList = Arrays.asList(pdus);
            if (pduList.size() == 0 || pduList.contains(null)) {
                loge("processMessagePart: returning false due to " + (pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)"));
                return false;
            }
            resultReceiver = new SmsBroadcastReceiver(tracker);
            if (!this.mUserManager.isUserUnlocked()) {
                return processMessagePartWithUserLocked(tracker, pdus, destPort, resultReceiver);
            }
            if (destPort == 2948) {
                output = new ByteArrayOutputStream();
                bundle = new Bundle();
                for (byte[] pdu22 : pdus) {
                    if (!tracker.is3gpp2()) {
                        msg = SmsMessage.createFromPdu(pdu22, "3gpp");
                        if (msg != null) {
                            pdu22 = msg.getUserData();
                            if (address == "") {
                                address = msg.getOriginatingAddress();
                                if (address == "") {
                                    address = tracker.getAddress();
                                }
                            }
                            sca = msg.getServiceCenterAddress();
                            bundle.putString("address", address);
                            str = "service_center";
                            if (sca == null) {
                                sca = "";
                            }
                            bundle.putString(str, sca);
                        } else {
                            loge("processMessagePart: SmsMessage.createFromPdu returned null");
                            return false;
                        }
                    }
                    output.write(pdu22, 0, pdu22.length);
                }
                result = this.mWapPush.dispatchWapPdu(output.toByteArray(), resultReceiver, this);
                log("dispatchWapPdu() returned " + result);
                if (result == -1) {
                    return true;
                }
                deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                return false;
            } else if (block) {
                dispatchBlockMessage(pdus, tracker.getFormat(), tracker.getDestPort());
                deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 1);
                return false;
            } else if (!filterSms(pdus, destPort, tracker, resultReceiver, true)) {
                dispatchSmsDeliveryIntent(pdus, tracker.getFormat(), destPort, resultReceiver);
            }
        }
        return true;
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void dispatchBlockMessage(byte[][] pdus, String format, int destPort) {
        Intent intent = new Intent("com.vivo.BlockNumber.ACTION_BLOCK_MESSAGE");
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", format);
        intent.putExtra("destport", destPort);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        intent.setPackage("com.android.server.telecom");
        this.mContext.sendBroadcast(intent);
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort, SmsBroadcastReceiver resultReceiver) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == 2948 && this.mWapPush.isWapPushForMms(pdus[0], this)) {
            showNewMessageNotification();
            return false;
        } else if (destPort != -1) {
            return false;
        } else {
            if (filterSms(pdus, destPort, tracker, resultReceiver, false)) {
                return true;
            }
            showNewMessageNotification();
            return false;
        }
    }

    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040391)).setContentText(this.mContext.getString(17040390)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1073741824)).setChannelId("others").build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        if (new CarrierServicesSmsFilter(this.mContext, this.mPhone, pdus, destPort, tracker.getFormat(), new CarrierServicesSmsFilterCallback(pdus, destPort, tracker.getFormat(), resultReceiver, userUnlocked), getName()).filter()) {
            return true;
        }
        if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
            return false;
        }
        log("Visual voicemail SMS dropped");
        dropSms(resultReceiver);
        return true;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        intent.addFlags(134217728);
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users = null;
            try {
                users = ActivityManager.getService().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users == null) {
                users = new int[]{user.getIdentifier()};
            }
            for (int i = users.length - 1; i >= 0; i--) {
                UserHandle targetUser = new UserHandle(users[i]);
                if (users[i] != 0) {
                    if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        UserInfo info = this.mUserManager.getUserInfo(users[i]);
                        if (info != null) {
                            if (info.isManagedProfile()) {
                            }
                        }
                    }
                }
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, null, getHandler(), -1, null, null);
            }
            resultReceiver.onReceive(this.mContext, intent);
            return;
        }
        log("action =====" + intent.getAction());
        if (isVivoMessage(intent)) {
            intent.setComponent(null);
            intent.setAction("android.provider.Telephony.FINDPHONE_SMS_RECEIVED");
            this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
        } else if ("android.provider.Telephony.SMS_DELIVER".equals(intent.getAction()) && isRequireSystemMmsHandle()) {
            boolean needSendBroadcast = true;
            try {
                IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
                if (iMms != null) {
                    VivoTelephonyApiParams vivoTelephonyApiParams = new VivoTelephonyApiParams("API_TAG_receivedMessage");
                    vivoTelephonyApiParams.put("sms_intent", intent);
                    VivoTelephonyApiParams ret = null;
                    try {
                        ret = iMms.vivoTelephonyApi(vivoTelephonyApiParams);
                    } catch (SecurityException se) {
                        log("SecurityException:" + se.toString());
                    }
                    if (ret != null) {
                        if (ret.containsKey("vivo_remove_pdu")) {
                            if (ret.getAsBoolean("vivo_remove_pdu").booleanValue()) {
                                intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                            }
                        }
                    }
                    resultReceiver.onReceive(this.mContext, intent);
                    needSendBroadcast = false;
                }
            } catch (RemoteException e2) {
            }
            if (needSendBroadcast) {
                this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
            }
        } else if ("android.provider.Telephony.WAP_PUSH_DELIVER".equals(intent.getAction()) && isRequireSystemMmsHandle()) {
            ComponentName componentName = SmsApplication.getDefaultMmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering WAP PUSH to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
        } else {
            this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected boolean isVivoMessage(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        if (messages == null || messages.length < 1) {
            return false;
        }
        try {
            SmsMessage smsMessage = SmsMessage.createFromPdu(messages[0], intent.getStringExtra("format"));
            if (smsMessage != null) {
                String body = smsMessage.getMessageBody();
                return (TextUtils.isEmpty(body) || (body.indexOf("#vivo#location#") == -1 && body.indexOf("#vivo#ring#") == -1 && body.indexOf("#vivo#lock#") == -1 && body.indexOf("#vivo#backup#") == -1 && body.indexOf("#vivo#clean#") == -1 && body.indexOf("#vivo#sim#") == -1)) ? false : true;
            }
        } catch (NullPointerException e) {
            log(Log.getStackTraceString(e));
        } catch (Exception e2) {
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    protected boolean isRequireSystemMmsHandle() {
        boolean isRequire = false;
        int userId = this.mContext.getUserId();
        int callingUid = Binder.getCallingUid();
        if (UserHandle.getAppId(callingUid) >= 10000) {
            userId = UserHandle.getUserId(callingUid);
        }
        String pkg = Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId);
        log("pkg name ==" + pkg);
        if ("com.android.mms".equals(pkg) || "com.vivo.easyshare".equals(pkg)) {
            isRequire = true;
        } else {
            try {
                IMms iMms = IMms.Stub.asInterface(ServiceManager.getService("imms"));
                if (iMms != null) {
                    VivoTelephonyApiParams param = new VivoTelephonyApiParams("API_TAG_isRequireSystemMmsHandle");
                    param.put("package_name", pkg);
                    param.put("require_alert", Boolean.valueOf(true));
                    VivoTelephonyApiParams ret = null;
                    try {
                        ret = iMms.vivoTelephonyApi(param);
                    } catch (SecurityException se) {
                        log("SecurityException:" + se.toString());
                    }
                    if (ret != null) {
                        isRequire = ret.getAsBoolean("is_require_system_mms_handle").booleanValue();
                    }
                    if (isRequire) {
                        SmsApplication.setDefaultApplication("com.android.mms", this.mContext);
                    }
                }
            } catch (RemoteException e) {
            }
        }
        log("isRequireSystemMms ==" + isRequire);
        return isRequire;
    }

    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == 1 ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
        } else {
            log("Deleted " + rows + " rows from raw table.");
        }
    }

    private Bundle handleSmsWhitelisting(ComponentName target) {
        String pkgName;
        String reason;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", format);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        }
        intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
        intent.setData(Uri.parse("sms://localhost:" + destPort));
        intent.setComponent(null);
        intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver, boolean isNeedAddFlag) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra("format", format);
        intent.putExtra("need_add_flag", isNeedAddFlag);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        }
        intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
        intent.setData(Uri.parse("sms://localhost:" + destPort));
        intent.setComponent(null);
        intent.addFlags(QtiCallConstants.CAPABILITY_SUPPORTS_DOWNGRADE_TO_VOICE_REMOTE);
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean duplicateExists(InboundSmsTracker tracker) throws SQLException {
        String where;
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        String seqNumber = Integer.toString(tracker.getSequenceNumber());
        String date = Long.toString(tracker.getTimestamp());
        String messageBody = tracker.getMessageBody();
        if (tracker.getMessageCount() == 1) {
            where = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=?";
        } else {
            where = tracker.getQueryForMultiPartDuplicates();
        }
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(sRawUri, PDU_PROJECTION, where, new String[]{address, refNumber, count, seqNumber, date, messageBody}, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            loge("Discarding duplicate message segment, refNumber=" + refNumber + " seqNumber=" + seqNumber + " count=" + count);
            String oldPduString = cursor.getString(0);
            byte[] pdu = tracker.getPdu();
            byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
            if (!Arrays.equals(oldPdu, tracker.getPdu())) {
                loge("Warning: dup message segment PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        if (deDup) {
            try {
                if (duplicateExists(tracker)) {
                    return 5;
                }
            } catch (SQLException e) {
                loge("Can't access SMS database", e);
                return 2;
            }
        }
        logd("Skipped message de-duping logic");
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        Uri newUri = this.mResolver.insert(sRawUri, tracker.getContentValues());
        log("URI of new row -> " + newUri);
        try {
            long rowId = ContentUris.parseId(newUri);
            if (tracker.getMessageCount() == 1) {
                tracker.setDeleteWhere(SELECT_BY_ID, new String[]{Long.toString(rowId)});
            } else {
                tracker.setDeleteWhere(tracker.getQueryForSegments(), new String[]{address, refNumber, count});
            }
            return 1;
        } catch (Exception e2) {
            loge("error parsing URI for new row: " + newUri, e2);
            return 2;
        }
    }

    static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    private void dropSms(SmsBroadcastReceiver receiver) {
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < 1) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        int i = 0;
        int length = messages.length;
        while (i < length) {
            try {
                messages[i].getDisplayMessageBody();
                i++;
            } catch (NullPointerException e) {
                loge("NPE inside SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        Uri insert;
        try {
            insert = this.mContext.getContentResolver().insert(Inbox.CONTENT_URI, values);
            return insert;
        } catch (Exception e2) {
            insert = "Failed to persist inbox message";
            loge(insert, e2);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        int i = 0;
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", Integer.valueOf(0));
        values.put("read", Integer.valueOf(0));
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put("subject", subject);
        }
        String str = "reply_path_present";
        if (sms.isReplyPathPresent()) {
            i = 1;
        }
        values.put(str, Integer.valueOf(i));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        int i = 0;
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        int length = msgs.length;
        while (i < length) {
            body.append(msgs[i].getDisplayMessageBody());
            i++;
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? "" : s.replace(12, 10);
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getWakeLockTimeout() {
        return this.mWakeLockTimeout;
    }

    private void setWakeLockTimeout(int timeOut) {
        this.mWakeLockTimeout = timeOut;
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private void processCrashLog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        SharedPreferences sharedPreferences = sp;
        Set<String> crashTimes = sharedPreferences.getStringSet("crash_time_sets", new HashSet());
        long time = System.currentTimeMillis();
        Set<String> newCrashTimes = new HashSet();
        for (String ct : crashTimes) {
            long t = Long.parseLong(ct);
            if (time - t > 0 && time - t <= 3600000) {
                newCrashTimes.add(ct);
            }
        }
        Log.e(ERROR_TAG, " -> crash time set: " + newCrashTimes + ";current: " + time);
        if (newCrashTimes.size() >= 9) {
            newCrashTimes.clear();
            try {
                deleteFromRawTable("_id >0", null, 1);
                Log.e(ERROR_TAG, "delete all raw record");
            } catch (Exception ex) {
                Log.e(ERROR_TAG, " -> delete exception", ex);
            }
        } else {
            newCrashTimes.add(String.valueOf(time));
            Cursor cursor = null;
            Log.e(ERROR_TAG, " ->=========== dump raw===============");
            try {
                cursor = this.mContext.getContentResolver().query(sRawUri, null, null, null, null);
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    int columnSize = cursor.getColumnCount();
                    StringBuffer sb = new StringBuffer();
                    do {
                        for (int i = 0; i < columnSize; i++) {
                            sb.append(cursor.getString(i)).append("#");
                        }
                        sb.append(System.lineSeparator());
                    } while (cursor.moveToNext());
                    Log.e(ERROR_TAG, " ->raw record: " + sb.toString());
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ex2) {
                Log.e(ERROR_TAG, " -> dump exception", ex2);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Editor editor = sp.edit();
        editor.putStringSet("crash_time_sets", newCrashTimes);
        editor.commit();
    }
}
