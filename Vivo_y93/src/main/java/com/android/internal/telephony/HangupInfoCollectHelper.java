package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.text.format.Time;
import com.android.internal.telephony.DriverCall.State;
import com.vivo.common.VivoCollectData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HangupInfoCollectHelper extends Handler {
    private static final String CONFIG_STRING = "persist.sys.force.hangup";
    private static final int EVENT_CALL_STATE_CHANGE = 3;
    private static final int EVENT_DIAL_ERROR = 14;
    private static final int EVENT_DIAL_TIMEOUT = 13;
    private static final int EVENT_FORCE_HANGUP_RECEIVE_TIMEOUT = 12;
    private static final int EVENT_FORCE_HANGUP_WAITFOR_RECOVER_TIMEOUT = 15;
    private static final int EVENT_GET_MODEM_FORCE_HANGUP_TIMEOUT = 16;
    private static final int EVENT_HANGUP_RECEIVE = 2;
    private static final int EVENT_HANGUP_SEND = 1;
    private static final int EVENT_HANGUP_TIMEOUT = 11;
    private static final int EVENT_SET_MODEM_FORCE_HANGUP_TIMEOUT = 17;
    private static final int EVENT_SET_MODEM_RESTART = 18;
    private static final String TAG = "HangupInfoCollectHelper";
    private static int WAITFOR_DIAL_TIMEOUT = 15000;
    private static int WAITFOR_HANGUP_AFTER_FORCE_HANGUP_TIMEOUT = DataCollectionUtils.OUT_OF_SERV_REPORT_DELAY;
    private static int WAITFOR_RECEOVER_AFTER_FORCE_HANGUP_TIMEOUT = 43200000;
    private BroadcastReceiver mBroadcastReceiver;
    private CommandsInterface mCi;
    private Context mContext;
    private String mDialErrorInfo;
    private String mDialTimeOutInfo;
    private long mElapsedRealBeginTime;
    private String mForceHangupTime;
    private String mForceHangupTimeout;
    private long mHangupBeginTime;
    private long mHangupBeginTimeTmp;
    private int mHangupCount;
    private String mHangupEnable;
    private long mHangupEndTime;
    private boolean mHangupTimeOutAgain;
    private Integer mInstanceId;
    private boolean mIsRecovery;
    private boolean mIsReset;
    private boolean mNeedCollect;
    private String mRecoveryTimeOutInfo;
    private String mResetTime;
    private VivoCollectData mVivoCollectData;
    private int mWaitforHangupTimeOut;

    public HangupInfoCollectHelper(Context context, Looper looper, CommandsInterface ci, Integer instanceId) {
        this(context, looper, ci, instanceId, null);
    }

    private HangupInfoCollectHelper(Context context, Looper looper, CommandsInterface ci, Integer instanceId, VivoCollectData vivoCollectData) {
        super(looper);
        this.mVivoCollectData = null;
        this.mWaitforHangupTimeOut = SystemProperties.getInt("persist.sys.hangup.timeout", 3000);
        this.mHangupEnable = SystemProperties.get(CONFIG_STRING);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                    HangupInfoCollectHelper.this.actionShutDown();
                } else if ("com.volte.config.forceHangupTimeout".equals(intent.getAction())) {
                    HangupInfoCollectHelper.this.judgeModemTimeout();
                } else if ("com.volte.config.forceHangupEnable".equals(intent.getAction())) {
                    HangupInfoCollectHelper.this.mHangupEnable = SystemProperties.get(HangupInfoCollectHelper.CONFIG_STRING);
                }
            }
        };
        this.mVivoCollectData = vivoCollectData;
        this.mContext = context;
        this.mCi = ci;
        this.mInstanceId = instanceId;
        this.mNeedCollect = false;
        this.mHangupBeginTime = 0;
        this.mHangupEndTime = 0;
        this.mForceHangupTime = "";
        this.mHangupCount = 0;
        this.mIsReset = false;
        this.mIsRecovery = false;
        this.mDialErrorInfo = "";
        this.mDialTimeOutInfo = "";
        this.mResetTime = "";
        this.mRecoveryTimeOutInfo = "";
        collectData();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("com.volte.config.forceHangupTimeout");
        filter.addAction("com.volte.config.forceHangupEnable");
        context.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void handleMessage(Message msg) {
        log("handleMessage msg.what=" + msg.what);
        switch (msg.what) {
            case 1:
                int gsmIndex = msg.arg1;
                this.mHangupBeginTimeTmp = System.currentTimeMillis();
                this.mElapsedRealBeginTime = SystemClock.elapsedRealtime();
                if (this.mHangupBeginTime == 0) {
                    this.mHangupBeginTime = System.currentTimeMillis();
                }
                sendMessageDelayed(obtainMessage(11, gsmIndex, 0), (long) this.mWaitforHangupTimeOut);
                this.mHangupCount++;
                return;
            case 2:
                long hangupEndTime = System.currentTimeMillis();
                long elapsedRealEndTime = SystemClock.elapsedRealtime();
                if (this.mElapsedRealBeginTime != 0 && elapsedRealEndTime - this.mElapsedRealBeginTime >= ((long) this.mWaitforHangupTimeOut)) {
                    this.mHangupTimeOutAgain = true;
                    collectData();
                    this.mHangupTimeOutAgain = false;
                    this.mNeedCollect = true;
                    this.mHangupBeginTime = this.mHangupBeginTimeTmp;
                    this.mHangupEndTime = hangupEndTime;
                    saveHangupInfoToSharepreference();
                    sendMessageDelayed(obtainMessage(15), (long) WAITFOR_RECEOVER_AFTER_FORCE_HANGUP_TIMEOUT);
                }
                this.mElapsedRealBeginTime = 0;
                return;
            case 3:
                if (hasMessages(15)) {
                    removeMessages(15);
                }
                if (hasMessages(13)) {
                    removeMessages(13);
                }
                this.mIsRecovery = true;
                saveHangupInfoToSharepreference();
                collectData();
                return;
            case 11:
                forceHangup(msg.arg1);
                this.mForceHangupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
                sendMessageDelayed(obtainMessage(12), (long) WAITFOR_HANGUP_AFTER_FORCE_HANGUP_TIMEOUT);
                return;
            case 12:
                this.mForceHangupTimeout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
                this.mNeedCollect = true;
                saveHangupInfoToSharepreference();
                return;
            case 13:
                addDialTimeOutInfo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())));
                return;
            case 14:
                addDialErrorInfo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())));
                return;
            case 15:
                this.mRecoveryTimeOutInfo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
                saveHangupInfoToSharepreference();
                collectData();
                return;
            case 16:
                AsyncResult ar = msg.obj;
                if (ar.exception == null && ar.result != null) {
                    try {
                        int apTimeOut;
                        String time = ar.result;
                        int modemTime = Integer.parseInt(time);
                        if (this.mWaitforHangupTimeOut > 90000 || this.mWaitforHangupTimeOut < 3000) {
                            apTimeOut = 30;
                        } else {
                            apTimeOut = this.mWaitforHangupTimeOut / 3000;
                        }
                        log("EVENT_HANGUP_INFO_GET time = " + time + " modemTime = " + modemTime + " apTimeOut = " + apTimeOut);
                        String ENTRY_PROP = SystemProperties.get("ro.vivo.op.entry", "").toUpperCase();
                        boolean bCmccEntry = false;
                        if (!TextUtils.isEmpty(ENTRY_PROP) && ENTRY_PROP.contains("CMCC")) {
                            bCmccEntry = true;
                        }
                        if (!bCmccEntry && apTimeOut != modemTime) {
                            this.mCi.sendMiscInfo(17, "" + apTimeOut, obtainMessage(17));
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        log("EVENT_HANGUP_INFO_GET exception e = " + e);
                        return;
                    }
                }
                return;
            case 17:
                if (((AsyncResult) msg.obj).exception == null) {
                    if (hasMessages(18)) {
                        removeMessages(18);
                    }
                    sendMessageDelayed(obtainMessage(18), VivoNetLowlatency.LEVEL_SET_MINIMUM_TIME_INTERVAL);
                    return;
                }
                return;
            case 18:
                log("EVENT_SET_MODEM_RESTART vivoresetmodem!");
                Intent intent = new Intent("vivo.intent.action.vivoresetmodem");
                intent.putExtra("flag", 1641);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                return;
            default:
                return;
        }
    }

    public void hangupSend(int gsmId) {
        if (!"0".equals(this.mHangupEnable)) {
            sendMessage(obtainMessage(1, gsmId, 0));
        }
    }

    public void hangupReceive() {
        if (hasMessages(13)) {
            removeMessages(13);
        }
        if (hasMessages(11)) {
            removeMessages(11);
        }
        if (hasMessages(12)) {
            removeMessages(12);
        }
        if (!"0".equals(this.mHangupEnable)) {
            sendMessage(obtainMessage(2));
        }
    }

    public void callStateChange(State state) {
        if (!this.mNeedCollect) {
            return;
        }
        if (state == State.INCOMING || state == State.ALERTING || state == State.ACTIVE) {
            sendMessage(obtainMessage(3));
        }
    }

    public void dialSend() {
        if (this.mNeedCollect) {
            sendMessageDelayed(obtainMessage(13), (long) WAITFOR_DIAL_TIMEOUT);
        }
    }

    public void addDialError() {
        if (this.mNeedCollect) {
            sendMessage(obtainMessage(14));
        }
        if (hasMessages(13)) {
            removeMessages(13);
        }
    }

    public void actionShutDown() {
        if (this.mNeedCollect) {
            this.mResetTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
            this.mIsReset = true;
            saveHangupInfoToSharepreference();
        }
    }

    public void judgeModemTimeout() {
    }

    private void forceHangup(int gsmIndex) {
    }

    private void addDialErrorInfo(String info) {
        if (TextUtils.isEmpty(this.mDialErrorInfo)) {
            this.mDialErrorInfo = info;
        } else {
            this.mDialErrorInfo += " " + info;
        }
        saveHangupInfoToSharepreference();
    }

    private void addDialTimeOutInfo(String info) {
        if (TextUtils.isEmpty(this.mDialTimeOutInfo)) {
            this.mDialTimeOutInfo = info;
        } else {
            this.mDialTimeOutInfo += " " + info;
        }
        saveHangupInfoToSharepreference();
    }

    private void saveHangupInfoToSharepreference() {
        String hangupCost = "";
        if (this.mHangupEndTime != 0) {
            hangupCost = String.valueOf(this.mHangupEndTime - this.mHangupBeginTime);
        } else {
            hangupCost = "no Response!";
        }
        log("saveHangupInfoToSharepreference mNeedCollect = " + this.mNeedCollect);
        SharedPreferences prefs = this.mContext.getSharedPreferences("com.android.hangupData" + this.mInstanceId, 0);
        if (this.mNeedCollect && prefs != null) {
            Editor editor = prefs.edit();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String beginTime = formatter.format(new Date(this.mHangupBeginTime));
            String endTime = formatter.format(new Date(this.mHangupEndTime));
            editor.putBoolean("isNeedCollect", this.mNeedCollect);
            editor.putString("hangupBeginTime", beginTime);
            editor.putString("hangupEndTime", endTime);
            editor.putString("forceHangupTime", this.mForceHangupTime);
            editor.putString("hangupCost", hangupCost);
            if (this.mHangupCount != 0) {
                editor.putString("hangupSuccessCount", String.valueOf(this.mHangupCount));
            }
            editor.putString("isRecovery", String.valueOf(this.mIsRecovery));
            editor.putString("isReset", String.valueOf(this.mIsReset));
            editor.putString("dialErrorInfo", this.mDialErrorInfo);
            editor.putString("dialTimeOutInfo", this.mDialTimeOutInfo);
            editor.putString("resetTime", this.mResetTime);
            editor.putString("recoverTimeOutInfo", this.mRecoveryTimeOutInfo);
            editor.commit();
        }
    }

    private void collectData() {
        if (!"0".equals(this.mHangupEnable) || !"2".equals(this.mHangupEnable)) {
            if (this.mNeedCollect) {
                dispose();
            }
            SharedPreferences prefs = this.mContext.getSharedPreferences("com.android.hangupData" + this.mInstanceId, 0);
            if (prefs != null && prefs.getBoolean("isNeedCollect", false)) {
                String hangupBeginTime = prefs.getString("hangupBeginTime", "");
                String hangupEndTime = prefs.getString("hangupEndTime", "");
                String forceHangupTime = prefs.getString("forceHangupTime", "");
                String hangupCost = prefs.getString("hangupCost", "0");
                String isRecovery = prefs.getString("isRecovery", "false");
                String isReset = prefs.getString("isReset", "false");
                String dialerrorInfo = prefs.getString("dialErrorInfo", "");
                if (this.mHangupTimeOutAgain) {
                    dialerrorInfo = "hangupTimeOutAgain " + dialerrorInfo;
                }
                collectData(hangupBeginTime, hangupEndTime, forceHangupTime, hangupCost, isRecovery, isReset, dialerrorInfo, prefs.getString("dialTimeOutInfo", ""), prefs.getString("resetTime", ""), prefs.getString("recoverTimeOutInfo", ""), prefs.getString("hangupSuccessCount", "0"));
                disposeSharepreference();
                this.mHangupCount = 0;
            }
        }
    }

    private void dispose() {
        this.mNeedCollect = false;
        this.mHangupBeginTime = 0;
        this.mHangupEndTime = 0;
        this.mForceHangupTime = "";
        this.mIsReset = false;
        this.mIsRecovery = false;
        this.mDialErrorInfo = "";
        this.mDialTimeOutInfo = "";
        this.mResetTime = "";
        this.mRecoveryTimeOutInfo = "";
    }

    private void disposeSharepreference() {
        SharedPreferences prefs = this.mContext.getSharedPreferences("com.android.hangupData" + this.mInstanceId, 0);
        if (prefs != null) {
            Editor editor = prefs.edit();
            editor.putBoolean("isNeedCollect", false);
            editor.putString("hangupBeginTime", "");
            editor.putString("hangupEndTime", "");
            editor.putString("forceHangupTime", "");
            editor.putString("hangupCost", "0");
            editor.putString("isRecovery", "false");
            editor.putString("isReset", "false");
            editor.putString("dialErrorInfo", "false");
            editor.putString("dialtimeoutInfo", "");
            editor.putString("resetTime", "");
            editor.putString("recoverTimeOutInfo", "");
            editor.putString("hangupSuccessCount", "0");
            editor.commit();
        }
    }

    private void collectData(String hangupBeginTime, String hangupEndTime, String sendAtTime, String hangupCost, String isRecovery, String isReset, String dialerrorInfo, String dialtimeoutInfo, String resetTime, String recoverTimeOutInfo, String hangupCount) {
        if (!"0".equals(this.mHangupEnable)) {
            if (this.mVivoCollectData == null) {
                this.mVivoCollectData = new VivoCollectData(this.mContext);
            }
            HashMap<String, String> params = new HashMap();
            params.put("hangupBeginTime", hangupBeginTime);
            params.put("hangupEndTime", hangupEndTime);
            params.put("sendAtTime", sendAtTime);
            params.put("hangupCost", hangupCost);
            params.put("isRecovery", isRecovery);
            params.put("isReset", isReset);
            params.put("dialerrorInfo", dialerrorInfo);
            params.put("dialtimeoutInfo", dialtimeoutInfo);
            params.put("resetTime", resetTime);
            params.put("recoverTimeOutInfo", recoverTimeOutInfo);
            params.put("hangupCount", hangupCount);
            params.put("simIndex", String.valueOf(this.mInstanceId));
            log("collectData hangupBeginTime = " + hangupBeginTime + " hangupEndTime = " + hangupEndTime);
            log("collectData sendAtTime = " + sendAtTime + " hangupCost = " + hangupCost);
            log("collectData isRecovery = " + isRecovery + " isReset = " + isReset);
            log("collectData dialerrorInfo = " + dialerrorInfo + " dialtimeoutInfo = " + dialtimeoutInfo);
            log("collectData resetTime = " + resetTime + " recoverTimeOutInfo = " + recoverTimeOutInfo + " hangupCount = " + hangupCount + " mInstanceId = " + this.mInstanceId);
            this.mVivoCollectData.writeData("804", "8045", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, params);
        }
    }

    private void sendBrocastToCollect(String timenow, String type, String ah, String sim_id, String fail_cause, String in_out, String lac1, String cid1, String cs1, String ps1, String plmn, String lac2, String cid2, String cs2, String ps2, String isim, String mbn_info, String volte_config, String volte, String dest_num, String temp1, String temp2, String info) {
        String infoupdate = "";
        String newline = "\n";
        Time time = new Time();
        String proVersion = SystemProperties.get("ro.product.model.bbk", "-1");
        String SysVersion = SystemProperties.get("ro.vivo.product.version", "-1");
        infoupdate = "EXCEPTION TIME:" + timenow + newline + "EXCEPTION SYSVER:" + SysVersion + newline + "EXCEPTION MODULE:com.vivo.network" + newline + "EXCEPTION VERSIONNAME:" + proVersion + newline + "EXCEPTION VERSIONCODE:" + SystemProperties.get("ro.build.version.release", "-1") + newline + "EXCEPTION TYPE:" + type + newline + "EXCEPTION AH:" + ah + newline + "EXCEPTION SIM_ID:" + sim_id + newline + "EXCEPTION FAIL_CAUSE:" + fail_cause + newline + "EXCEPTION IN_OUT:" + in_out + newline + "EXCEPTION LAC1:" + lac1 + newline + "EXCEPTION LAC2:" + lac2 + newline + "EXCEPTION CID1:" + cid1 + newline + "EXCEPTION CID2:" + cid2 + newline + "EXCEPTION PLMN:" + plmn + newline + "EXCEPTION CS1:" + cs1 + newline + "EXCEPTION CS2:" + cs2 + newline + "EXCEPTION PS1:" + ps1 + newline + "EXCEPTION PS2:" + ps2 + newline + "EXCEPTION ISIM:" + isim + newline + "EXCEPTION MBN INFO:" + mbn_info + newline + "EXCEPTION VOLTE Config:" + volte_config + newline + "EXCEPTION VOLTE:" + volte + newline + "EXCEPTION DEST_NUM:" + dest_num + newline + "EXCEPTION KEEPWORD1:" + temp1 + newline + "EXCEPTION KEEPWORD2:" + temp2 + newline + "EXCEPTION INFO:" + info + newline + newline;
        Intent intent = new Intent();
        intent.setAction("com.bbk.iqoo.appanalysis.services.LogUploadService");
        intent.setPackage("com.bbk.iqoo.logsystem");
        intent.putExtra("info", infoupdate);
        intent.putExtra("module", "network");
        intent.putExtra("attr", 1);
        this.mContext.startService(intent);
        log("sendBrocastToCollect " + infoupdate);
    }

    private void log(String str) {
        Rlog.e(TAG + this.mInstanceId, str);
    }
}
