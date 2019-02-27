package com.android.internal.telephony.uicc;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CustomPlmnOperatorOverride;
import com.android.internal.telephony.CustomPlmnOperatorOverride.OperatorName;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyPhoneUtils;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.google.android.mms.pdu.PduHeaders;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class SIMRecords extends IccRecords {
    /* renamed from: -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static final /* synthetic */ int[] f14x102e4fe = null;
    static final int CFF_LINE1_MASK = 15;
    static final int CFF_LINE1_RESET = 240;
    static final int CFF_UNCONDITIONAL_ACTIVE = 10;
    static final int CFF_UNCONDITIONAL_DEACTIVE = 5;
    private static final int CFIS_ADN_CAPABILITY_ID_OFFSET = 14;
    private static final int CFIS_ADN_EXTENSION_ID_OFFSET = 15;
    private static final int CFIS_BCD_NUMBER_LENGTH_OFFSET = 2;
    private static final int CFIS_TON_NPI_OFFSET = 3;
    private static final int CPHS_SST_MBN_ENABLED = 48;
    private static final int CPHS_SST_MBN_MASK = 48;
    private static final boolean CRASH_RIL = false;
    private static final int EVENT_APP_LOCKED = 258;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 257;
    private static final int EVENT_GET_AD_DONE = 9;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CFF_DONE = 24;
    private static final int EVENT_GET_CFIS_DONE = 32;
    private static final int EVENT_GET_CPHS_MAILBOX_DONE = 11;
    private static final int EVENT_GET_CSP_CPHS_DONE = 33;
    private static final int EVENT_GET_EHPLMN_DONE = 40;
    private static final int EVENT_GET_FPLMN_DONE = 41;
    private static final int EVENT_GET_GID1_DONE = 34;
    private static final int EVENT_GET_GID2_DONE = 36;
    private static final int EVENT_GET_HPLMN_W_ACT_DONE = 39;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_INFO_CPHS_DONE = 26;
    private static final int EVENT_GET_MBDN_DONE = 6;
    private static final int EVENT_GET_MBI_DONE = 5;
    private static final int EVENT_GET_MSISDN_DONE = 10;
    private static final int EVENT_GET_MWIS_DONE = 7;
    private static final int EVENT_GET_OPLMN_W_ACT_DONE = 38;
    private static final int EVENT_GET_PLMN_W_ACT_DONE = 37;
    private static final int EVENT_GET_PNN_DONE = 15;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    private static final int EVENT_GET_SPN_DONE = 12;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    private static final int EVENT_SET_MSISDN_DONE = 30;
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    protected static final String LOG_TAG = "SIMRecords";
    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC;
    private static final int SIM_RECORD_EVENT_BASE = 0;
    private static final int SYSTEM_EVENT_BASE = 256;
    static final int TAG_FULL_NETWORK_NAME = 67;
    static final int TAG_SHORT_NETWORK_NAME = 69;
    static final int TAG_SPDI = 163;
    static final int TAG_SPDI_PLMN_LIST = 128;
    private static final boolean VDBG = false;
    private int mCallForwardingStatus;
    private byte[] mCphsInfo;
    boolean mCspPlmnEnabled;
    byte[] mEfCPHS_MWI;
    byte[] mEfCff;
    byte[] mEfCfis;
    byte[] mEfLi;
    byte[] mEfMWIS;
    byte[] mEfPl;
    String mPnnHomeName;
    private final BroadcastReceiver mReceiver;
    ArrayList<String> mSpdiNetworks;
    int mSpnDisplayCondition;
    SpnOverride mSpnOverride;
    private GetSpnFsmState mSpnState;
    UsimServiceTable mUsimServiceTable;
    VoiceMailConstants mVmConfig;

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(SIMRecords this$0, EfPlLoaded -this1) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    private class EfUsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfUsimLiLoaded(SIMRecords this$0, EfUsimLiLoaded -this1) {
            this();
        }

        private EfUsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    private enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues */
    private static /* synthetic */ int[] m14xf89fffa2() {
        if (f14x102e4fe != null) {
            return f14x102e4fe;
        }
        int[] iArr = new int[GetSpnFsmState.values().length];
        try {
            iArr[GetSpnFsmState.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GetSpnFsmState.INIT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        f14x102e4fe = iArr;
        return iArr;
    }

    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " mSpnOverride=" + this.mSpnOverride + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    static {
        String[] strArr = new String[PduHeaders.MESSAGE_COUNT];
        strArr[0] = "302370";
        strArr[1] = "302720";
        strArr[2] = SimulatedCommands.FAKE_MCC_MNC;
        strArr[3] = "405025";
        strArr[4] = "405026";
        strArr[5] = "405027";
        strArr[6] = "405028";
        strArr[7] = "405029";
        strArr[8] = "405030";
        strArr[9] = "405031";
        strArr[10] = "405032";
        strArr[11] = "405033";
        strArr[12] = "405034";
        strArr[13] = "405035";
        strArr[14] = "405036";
        strArr[15] = "405037";
        strArr[16] = "405038";
        strArr[17] = "405039";
        strArr[18] = "405040";
        strArr[19] = "405041";
        strArr[20] = "405042";
        strArr[21] = "405043";
        strArr[22] = "405044";
        strArr[23] = "405045";
        strArr[24] = "405046";
        strArr[25] = "405047";
        strArr[26] = "405750";
        strArr[27] = "405751";
        strArr[28] = "405752";
        strArr[29] = "405753";
        strArr[30] = "405754";
        strArr[31] = "405755";
        strArr[32] = "405756";
        strArr[33] = "405799";
        strArr[34] = "405800";
        strArr[35] = "405801";
        strArr[36] = "405802";
        strArr[37] = "405803";
        strArr[38] = "405804";
        strArr[39] = "405805";
        strArr[40] = "405806";
        strArr[41] = "405807";
        strArr[42] = "405808";
        strArr[43] = "405809";
        strArr[44] = "405810";
        strArr[45] = "405811";
        strArr[46] = "405812";
        strArr[47] = "405813";
        strArr[48] = "405814";
        strArr[49] = "405815";
        strArr[50] = "405816";
        strArr[51] = "405817";
        strArr[52] = "405818";
        strArr[53] = "405819";
        strArr[54] = "405820";
        strArr[55] = "405821";
        strArr[56] = "405822";
        strArr[57] = "405823";
        strArr[58] = "405824";
        strArr[59] = "405825";
        strArr[60] = "405826";
        strArr[61] = "405827";
        strArr[62] = "405828";
        strArr[63] = "405829";
        strArr[64] = "405830";
        strArr[65] = "405831";
        strArr[66] = "405832";
        strArr[TAG_FULL_NETWORK_NAME] = "405833";
        strArr[68] = "405834";
        strArr[69] = "405835";
        strArr[70] = "405836";
        strArr[71] = "405837";
        strArr[72] = "405838";
        strArr[73] = "405839";
        strArr[74] = "405840";
        strArr[75] = "405841";
        strArr[76] = "405842";
        strArr[77] = "405843";
        strArr[78] = "405844";
        strArr[79] = "405845";
        strArr[80] = "405846";
        strArr[81] = "405847";
        strArr[82] = "405848";
        strArr[83] = "405849";
        strArr[84] = "405850";
        strArr[85] = "405851";
        strArr[86] = "405852";
        strArr[87] = "405853";
        strArr[88] = QtiImsExtUtils.CARRIER_ONE_DEFAULT_MCC_MNC;
        strArr[89] = "405855";
        strArr[90] = "405856";
        strArr[91] = "405857";
        strArr[92] = "405858";
        strArr[93] = "405859";
        strArr[94] = "405860";
        strArr[95] = "405861";
        strArr[96] = "405862";
        strArr[97] = "405863";
        strArr[98] = "405864";
        strArr[99] = "405865";
        strArr[100] = "405866";
        strArr[101] = "405867";
        strArr[102] = "405868";
        strArr[103] = "405869";
        strArr[104] = "405870";
        strArr[105] = "405871";
        strArr[106] = "405872";
        strArr[107] = "405873";
        strArr[108] = "405874";
        strArr[109] = "405875";
        strArr[110] = "405876";
        strArr[111] = "405877";
        strArr[112] = "405878";
        strArr[113] = "405879";
        strArr[114] = "405880";
        strArr[115] = "405881";
        strArr[116] = "405882";
        strArr[117] = "405883";
        strArr[118] = "405884";
        strArr[119] = "405885";
        strArr[120] = "405886";
        strArr[121] = "405908";
        strArr[122] = "405909";
        strArr[123] = "405910";
        strArr[124] = "405911";
        strArr[125] = "405912";
        strArr[126] = "405913";
        strArr[127] = "405914";
        strArr[128] = "405915";
        strArr[129] = "405916";
        strArr[130] = "405917";
        strArr[131] = "405918";
        strArr[132] = "405919";
        strArr[133] = "405920";
        strArr[134] = "405921";
        strArr[135] = "405922";
        strArr[136] = "405923";
        strArr[137] = "405924";
        strArr[138] = "405925";
        strArr[139] = "405926";
        strArr[140] = "405927";
        strArr[141] = "405928";
        strArr[142] = "405929";
        strArr[143] = "405930";
        strArr[144] = "405931";
        strArr[145] = "405932";
        strArr[146] = "502142";
        strArr[147] = "502143";
        strArr[148] = "502145";
        strArr[149] = "502146";
        strArr[150] = "502147";
        strArr[151] = "502148";
        strArr[152] = QtiImsExtUtils.CARRIER_ONE_DEFAULT_MCC_MNC;
        strArr[153] = "405855";
        strArr[154] = "405856";
        strArr[155] = "405857";
        strArr[156] = "405858";
        strArr[157] = "405859";
        strArr[PduHeaders.REPLY_CHARGING_ID] = "405860";
        strArr[PduHeaders.REPLY_CHARGING_SIZE] = "405861";
        strArr[160] = "405862";
        strArr[PduHeaders.PREVIOUSLY_SENT_DATE] = "405863";
        strArr[PduHeaders.STORE] = "405864";
        strArr[163] = "405865";
        strArr[PduHeaders.MM_FLAGS] = "405866";
        strArr[PduHeaders.STORE_STATUS] = "405867";
        strArr[PduHeaders.STORE_STATUS_TEXT] = "405868";
        strArr[PduHeaders.STORED] = "405869";
        strArr[PduHeaders.ATTRIBUTES] = "405870";
        strArr[PduHeaders.TOTALS] = "405871";
        strArr[PduHeaders.MBOX_TOTALS] = "405872";
        strArr[PduHeaders.QUOTAS] = "405873";
        strArr[PduHeaders.MBOX_QUOTAS] = "405874";
        MCCMNC_CODES_HAVING_3DIGITS_MNC = strArr;
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mCphsInfo = null;
        this.mCspPlmnEnabled = true;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mEfCff = null;
        this.mEfCfis = null;
        this.mEfLi = null;
        this.mEfPl = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    SIMRecords.this.sendMessage(SIMRecords.this.obtainMessage(257));
                }
            }
        };
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mVmConfig = new VoiceMailConstants();
        this.mSpnOverride = new SpnOverride();
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 258, null);
        log("SIMRecords X ctor this=" + this);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mReceiver, intentfilter);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mContext.unregisterReceiver(this.mReceiver);
        resetRecords();
        super.dispose();
    }

    protected void finalize() {
        log("finalized");
    }

    protected void resetRecords() {
        this.mImsi = null;
        this.mMsisdn = null;
        this.mVoiceMailNum = null;
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mSpnDisplayCondition = -1;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mPlmnActRecords = null;
        this.mOplmnActRecords = null;
        this.mHplmnActRecords = null;
        this.mFplmns = null;
        this.mEhplmns = null;
        this.mAdnCache.reset();
        log("SIMRecords: onRadioOffOrNotAvailable set 'gsm.sim.operator.numeric' to operator=null");
        log("update icc_operator_numeric=" + null);
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), "");
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), "");
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), "");
        this.mRecordsRequested = false;
    }

    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    private int getExtFromEf(int ef) {
        switch (ef) {
            case IccConstants.EF_MSISDN /*28480*/:
                if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                    return IccConstants.EF_EXT5;
                }
                return IccConstants.EF_EXT1;
            default:
                return IccConstants.EF_EXT1;
        }
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        AdnRecord adn = new AdnRecord(this.mNewVoiceMailTag, this.mNewVoiceMailNum);
        if (this.mMailboxIndex != 0 && this.mMailboxIndex != 255) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, onComplete));
        } else if (isCphsMailboxEnabled()) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onComplete));
        } else {
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        int i = 0;
        if (line == 1) {
            try {
                if (this.mEfMWIS != null && this.mEfMWIS.length > 1) {
                    byte[] bArr = this.mEfMWIS;
                    int i2 = this.mEfMWIS[0] & LastCallFailCause.RADIO_LINK_FAILURE;
                    if (countWaiting != 0) {
                        i = 1;
                    }
                    bArr[0] = (byte) (i | i2);
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = (byte) 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mEfCPHS_MWI != null && this.mEfCPHS_MWI.length > 0) {
                    this.mEfCPHS_MWI[0] = (byte) ((countWaiting == 0 ? 5 : 10) | (this.mEfCPHS_MWI[0] & 240));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    private boolean validEfCfis(byte[] data) {
        if (data == null) {
            return false;
        }
        if (data[0] < (byte) 1 || data[0] > (byte) 4) {
            logw("MSP byte: " + data[0] + " is not between 1 and 4", null);
        }
        return true;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        if (this.mEfMWIS != null && this.mEfMWIS.length > 1) {
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (((this.mEfMWIS[0] & 1) != 0) && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else if (this.mEfCPHS_MWI != null && this.mEfCPHS_MWI.length > 0) {
            int indicator = this.mEfCPHS_MWI[0] & 15;
            if (indicator == 10) {
                countVoiceMessages = -1;
            } else if (indicator == 5) {
                countVoiceMessages = 0;
            }
            log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
        }
        return countVoiceMessages;
    }

    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i = 0;
        if (line == 1) {
            if (enable) {
                i = 1;
            }
            this.mCallForwardingStatus = i;
            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
            try {
                if (validEfCfis(this.mEfCfis)) {
                    byte[] bArr;
                    if (enable) {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] & LastCallFailCause.RADIO_LINK_FAILURE);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && (TextUtils.isEmpty(dialNumber) ^ 1) != 0) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(PhoneNumberUtils.convertPreDial(dialNumber));
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = (byte) -1;
                        this.mEfCfis[15] = (byte) -1;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFIS)));
                } else {
                    log("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFF_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    public String getOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            log("getOperatorNumeric: IMSI == null");
            return null;
        } else if (this.mMncLength == -1 || this.mMncLength == 0) {
            log("getSIMOperatorNumeric: bad mncLength");
            return null;
        } else if (imsi.length() >= this.mMncLength + 3) {
            return imsi.substring(0, this.mMncLength + 3);
        } else {
            return null;
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.uicc.SIMRecords.handleMessage(android.os.Message):void, dom blocks: [B:132:0x055e, B:202:0x07fa]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1249)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    @android.annotation.VivoHook(hookType = android.annotation.VivoHook.VivoHookType.CHANGE_CODE)
    public void handleMessage(android.os.Message r25) {
        /*
        r24 = this;
        r16 = 0;
        r0 = r24;
        r2 = r0.mDestroyed;
        r2 = r2.get();
        if (r2 == 0) goto L_0x0045;
    L_0x000c:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r4 = "Received message ";
        r2 = r2.append(r4);
        r0 = r25;
        r2 = r2.append(r0);
        r4 = "[";
        r2 = r2.append(r4);
        r0 = r25;
        r4 = r0.what;
        r2 = r2.append(r4);
        r4 = "] ";
        r2 = r2.append(r4);
        r4 = " while being destroyed. Ignoring.";
        r2 = r2.append(r4);
        r2 = r2.toString();
        r0 = r24;
        r0.loge(r2);
        return;
    L_0x0045:
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        switch(r2) {
            case 1: goto L_0x0055;
            case 3: goto L_0x0073;
            case 4: goto L_0x04ff;
            case 5: goto L_0x024d;
            case 6: goto L_0x02e1;
            case 7: goto L_0x044c;
            case 8: goto L_0x04af;
            case 9: goto L_0x0546;
            case 10: goto L_0x03bf;
            case 11: goto L_0x02e1;
            case 12: goto L_0x0d89;
            case 13: goto L_0x0dd3;
            case 14: goto L_0x0dea;
            case 15: goto L_0x0e00;
            case 17: goto L_0x0f14;
            case 18: goto L_0x0e3f;
            case 19: goto L_0x0e56;
            case 20: goto L_0x0f83;
            case 21: goto L_0x0e76;
            case 22: goto L_0x0ee0;
            case 24: goto L_0x0d99;
            case 25: goto L_0x1061;
            case 26: goto L_0x0f4c;
            case 30: goto L_0x040f;
            case 32: goto L_0x10ba;
            case 33: goto L_0x10f4;
            case 34: goto L_0x1145;
            case 36: goto L_0x119e;
            case 37: goto L_0x11f7;
            case 38: goto L_0x124d;
            case 39: goto L_0x12a3;
            case 40: goto L_0x131a;
            case 41: goto L_0x1358;
            case 257: goto L_0x13d1;
            case 258: goto L_0x0068;
            default: goto L_0x004c;
        };	 Catch:{ RuntimeException -> 0x0059 }
    L_0x004c:
        super.handleMessage(r25);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x004f:
        if (r16 == 0) goto L_0x0054;
    L_0x0051:
        r24.onRecordLoaded();
    L_0x0054:
        return;
    L_0x0055:
        r24.onReady();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x0059:
        r13 = move-exception;
        r2 = "Exception parsing SIM record";	 Catch:{ all -> 0x006c }
        r0 = r24;	 Catch:{ all -> 0x006c }
        r0.logw(r2, r13);	 Catch:{ all -> 0x006c }
        if (r16 == 0) goto L_0x0054;
    L_0x0064:
        r24.onRecordLoaded();
        goto L_0x0054;
    L_0x0068:
        r24.onLocked();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x006c:
        r2 = move-exception;
        if (r16 == 0) goto L_0x0072;
    L_0x006f:
        r24.onRecordLoaded();
    L_0x0072:
        throw r2;
    L_0x0073:
        r16 = 1;
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x009b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x007f:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception querying IMSI, Exception:";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x009b:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.lang.String) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mImsi = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x00e2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00a9:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x00c0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00b4:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 15;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 <= r4) goto L_0x00e2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00c0:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "invalid IMSI ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mImsi = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x00e2:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0147;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0105:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0147;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0110:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.substring(r5, r6);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SIMRecords";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mImsi;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5.substring(r6);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.Rlog.pii(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0147:
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0158;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0151:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0197;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0158:
        if (r14 == 0) goto L_0x0197;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x015a:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0197;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0161:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x016b:
        if (r2 >= r5) goto L_0x0197;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x016d:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0225;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0175:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "IMSI: setting1 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0197:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x01cc;
    L_0x019d:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0229 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0229 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0229 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0229 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0229 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0229 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0229 }
        r4 = "setting2 mMncLength=";	 Catch:{ NumberFormatException -> 0x0229 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0229 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0229 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0229 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0229 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0229 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0229 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0229 }
    L_0x01cc:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x021c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01d2:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x021c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01d9:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x021c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x01e5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x021c:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mImsiReadyRegistrants;	 Catch:{ RuntimeException -> 0x0059 }
        r2.notifyRegistrants();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0225:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x016b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0229:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting3 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x01cc;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x024d:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r17 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x029d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x025f:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_MBI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 & 255;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMailboxIndex = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x029d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x028b:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x029d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0293:
        r2 = "Got valid mailbox number for MBDN";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r17 = 1;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x029d:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r17 == 0) goto L_0x02c6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02a9:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMailboxIndex;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.obtainMessage(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28615; // 0x6fc7 float:4.0098E-41 double:1.41377E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 28616; // 0x6fc8 float:4.01E-41 double:1.4138E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r6, r7, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02c6:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02e1:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x034b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x02f7:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Invalid or missing EF";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r5) goto L_0x0347;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x030b:
        r2 = "[MAILBOX]";	 Catch:{ RuntimeException -> 0x0059 }
    L_0x030e:
        r2 = r4.append(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0322:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0347:
        r2 = "[MBDN]";	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x030e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x034b:
        r3 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r3 = (com.android.internal.telephony.uicc.AdnRecord) r3;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "VM: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r2.append(r3);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r5) goto L_0x03a9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0367:
        r2 = " EF[MAILBOX]";	 Catch:{ RuntimeException -> 0x0059 }
    L_0x036a:
        r2 = r4.append(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.isEmpty();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x03ad;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x037d:
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.what;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x03ad;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0384:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mRecordsToLoad;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mRecordsToLoad = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 11;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.obtainMessage(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadFromEF(r5, r6, r7, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03a9:
        r2 = " EF[MBDN]";	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x036a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03ad:
        r2 = r3.getNumber();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getAlphaTag();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03bf:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x03d5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03cb:
        r2 = "Invalid or missing EF[MSISDN]";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x03d5:
        r3 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r3 = (com.android.internal.telephony.uicc.AdnRecord) r3;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getNumber();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r3.getAlphaTag();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdnTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MSISDN: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SIMRecords";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMsisdn;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.Rlog.pii(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x040f:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0433;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x041b:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewMsisdn;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdn = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewMsisdnTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMsisdnTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "Success to update EF[MSISDN]";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0433:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0437:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x044c:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_MWIS : ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0496;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0479:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_MWIS_DONE exception = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0496:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2 & 255;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x04a9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x049f:
        r2 = "SIMRecords: Uninitialized record MWIS";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04a9:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfMWIS = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04af:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CPHS_MWI: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x04f9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04dc:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04f9:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCPHS_MWI = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x04ff:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x050f:
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.bchToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mIccId = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.bchToString(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mFullIccId = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "iccid: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFullIccId;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = android.telephony.SubscriptionInfo.givePrintableIccid(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x0546:
        r16 = 1;
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r2 = r0.mCarrierTestOverride;	 Catch:{ all -> 0x06ea }
        r2 = r2.isInTestMode();	 Catch:{ all -> 0x06ea }
        if (r2 == 0) goto L_0x07fa;	 Catch:{ all -> 0x06ea }
    L_0x0552:
        r2 = r24.getIMSI();	 Catch:{ all -> 0x06ea }
        if (r2 == 0) goto L_0x07fa;	 Catch:{ all -> 0x06ea }
    L_0x0558:
        r14 = r24.getIMSI();	 Catch:{ all -> 0x06ea }
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x06c5 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x06c5 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x06c5 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x06c5 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x06c5 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x06c5 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x06c5 }
        r4 = "[TestMode] mMncLength=";	 Catch:{ NumberFormatException -> 0x06c5 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x06c5 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x06c5 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x06c5 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x06c5 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x06c5 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x06c5 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x06c5 }
    L_0x058b:
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r4 = 15;	 Catch:{ all -> 0x06ea }
        if (r2 != r4) goto L_0x0cab;	 Catch:{ all -> 0x06ea }
    L_0x0593:
        r2 = 0;	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.mMncLength = r2;	 Catch:{ all -> 0x06ea }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06ea }
        r2.<init>();	 Catch:{ all -> 0x06ea }
        r4 = "setting5 mMncLength=";	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r2 = r2.toString();	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
    L_0x05b5:
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x05c6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05c0:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0cdd;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05c6:
        if (r14 == 0) goto L_0x0620;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05c8:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0620;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05cf:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05f4:
        if (r2 >= r5) goto L_0x0620;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05f6:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0ce6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x05fe:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0620:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x062d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0626:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x065e;
    L_0x062d:
        if (r14 == 0) goto L_0x0d0f;
    L_0x062f:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0cea }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0cea }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0cea }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0cea }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0cea }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0cea }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0cea }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0cea }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0cea }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0cea }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0cea }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0cea }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0cea }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0cea }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0cea }
    L_0x065e:
        if (r14 == 0) goto L_0x004f;
    L_0x0660:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0666:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0672:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.RoamingStatusCollectHelper.getInstance();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.onImsiGetDone(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
    L_0x06c5:
        r12 = move-exception;
        r2 = 0;
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.mMncLength = r2;	 Catch:{ all -> 0x06ea }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06ea }
        r2.<init>();	 Catch:{ all -> 0x06ea }
        r4 = "[TestMode] Corrupt IMSI! mMncLength=";	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r2 = r2.toString();	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.loge(r2);	 Catch:{ all -> 0x06ea }
        goto L_0x058b;
    L_0x06ea:
        r2 = move-exception;
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == r5) goto L_0x06fc;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06f6:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 != 0) goto L_0x0d33;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06fc:
        if (r14 == 0) goto L_0x0756;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x06fe:
        r4 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 < r5) goto L_0x0756;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0705:
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r5.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x072a:
        if (r4 >= r6) goto L_0x0756;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x072c:
        r19 = r5[r4];	 Catch:{ RuntimeException -> 0x0059 }
        r7 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r7 == 0) goto L_0x0d3c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0734:
        r4 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0756:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == 0) goto L_0x0763;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x075c:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = -1;
        if (r4 != r5) goto L_0x0794;
    L_0x0763:
        if (r14 == 0) goto L_0x0d65;
    L_0x0765:
        r4 = 0;
        r5 = 3;
        r4 = r14.substring(r4, r5);	 Catch:{ NumberFormatException -> 0x0d40 }
        r18 = java.lang.Integer.parseInt(r4);	 Catch:{ NumberFormatException -> 0x0d40 }
        r4 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0d40 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0d40 }
        r0.mMncLength = r4;	 Catch:{ NumberFormatException -> 0x0d40 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0d40 }
        r4.<init>();	 Catch:{ NumberFormatException -> 0x0d40 }
        r5 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0d40 }
        r4 = r4.append(r5);	 Catch:{ NumberFormatException -> 0x0d40 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0d40 }
        r5 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0d40 }
        r4 = r4.append(r5);	 Catch:{ NumberFormatException -> 0x0d40 }
        r4 = r4.toString();	 Catch:{ NumberFormatException -> 0x0d40 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0d40 }
        r0.log(r4);	 Catch:{ NumberFormatException -> 0x0d40 }
    L_0x0794:
        if (r14 == 0) goto L_0x07f9;
    L_0x0796:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 == 0) goto L_0x07f9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x079c:
        r4 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 < r5) goto L_0x07f9;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x07a8:
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r4, r5, r6);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.RoamingStatusCollectHelper.getInstance();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r6 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r14.substring(r7, r6);	 Catch:{ RuntimeException -> 0x0059 }
        r4.onImsiGetDone(r5, r6);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x07f9:
        throw r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x07fa:
        r0 = r25;	 Catch:{ all -> 0x06ea }
        r9 = r0.obj;	 Catch:{ all -> 0x06ea }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ all -> 0x06ea }
        r11 = r9.result;	 Catch:{ all -> 0x06ea }
        r11 = (byte[]) r11;	 Catch:{ all -> 0x06ea }
        r2 = r9.exception;	 Catch:{ all -> 0x06ea }
        if (r2 == 0) goto L_0x096e;
    L_0x0808:
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x0819;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0813:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0918;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0819:
        if (r14 == 0) goto L_0x0873;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x081b:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0873;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0822:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0847:
        if (r2 >= r5) goto L_0x0873;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0849:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0921;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0851:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0873:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0880;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0879:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x08b1;
    L_0x0880:
        if (r14 == 0) goto L_0x094a;
    L_0x0882:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0925 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0925 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0925 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0925 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0925 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0925 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0925 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0925 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0925 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0925 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0925 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0925 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0925 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0925 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0925 }
    L_0x08b1:
        if (r14 == 0) goto L_0x004f;
    L_0x08b3:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08b9:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x08c5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.RoamingStatusCollectHelper.getInstance();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.onImsiGetDone(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0918:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0873;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x091f:
        goto L_0x0819;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0921:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0847;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0925:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x08b1;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x094a:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x08b1;
    L_0x096e:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06ea }
        r2.<init>();	 Catch:{ all -> 0x06ea }
        r4 = "EF_AD: ";	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r2 = r2.toString();	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        r2 = r11.length;	 Catch:{ all -> 0x06ea }
        r4 = 3;	 Catch:{ all -> 0x06ea }
        if (r2 >= r4) goto L_0x0afd;	 Catch:{ all -> 0x06ea }
    L_0x098f:
        r2 = "Corrupt AD data on SIM";	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x09a8;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09a2:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0aa7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09a8:
        if (r14 == 0) goto L_0x0a02;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09aa:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0a02;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09b1:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09d6:
        if (r2 >= r5) goto L_0x0a02;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09d8:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0ab0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x09e0:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a02:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0a0f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a08:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x0a40;
    L_0x0a0f:
        if (r14 == 0) goto L_0x0ad9;
    L_0x0a11:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0ab4 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0ab4 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0ab4 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0ab4 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0ab4 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0ab4 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0ab4 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0ab4 }
    L_0x0a40:
        if (r14 == 0) goto L_0x004f;
    L_0x0a42:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a48:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0a54:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.RoamingStatusCollectHelper.getInstance();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.onImsiGetDone(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0aa7:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0a02;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0aae:
        goto L_0x09a8;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ab0:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x09d6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ab4:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0a40;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ad9:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0a40;
    L_0x0afd:
        r2 = r11.length;	 Catch:{ all -> 0x06ea }
        r4 = 3;	 Catch:{ all -> 0x06ea }
        if (r2 != r4) goto L_0x0c6f;	 Catch:{ all -> 0x06ea }
    L_0x0b01:
        r2 = "MNC length not present in EF_AD";	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        r14 = r24.getIMSI();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == r4) goto L_0x0b1a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b14:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0c19;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b1a:
        if (r14 == 0) goto L_0x0b74;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b1c:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x0b74;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b23:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 6;	 Catch:{ RuntimeException -> 0x0059 }
        r20 = r14.substring(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "mccmncCode=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r0);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = MCCMNC_CODES_HAVING_3DIGITS_MNC;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b48:
        if (r2 >= r5) goto L_0x0b74;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b4a:
        r19 = r4[r2];	 Catch:{ RuntimeException -> 0x0059 }
        r6 = r19.equals(r20);	 Catch:{ RuntimeException -> 0x0059 }
        if (r6 == 0) goto L_0x0c22;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b52:
        r2 = 3;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "setting6 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b74:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0b81;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0b7a:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = -1;
        if (r2 != r4) goto L_0x0bb2;
    L_0x0b81:
        if (r14 == 0) goto L_0x0c4b;
    L_0x0b83:
        r2 = 0;
        r4 = 3;
        r2 = r14.substring(r2, r4);	 Catch:{ NumberFormatException -> 0x0c26 }
        r18 = java.lang.Integer.parseInt(r2);	 Catch:{ NumberFormatException -> 0x0c26 }
        r2 = com.android.internal.telephony.MccTable.smallestDigitsMccForMnc(r18);	 Catch:{ NumberFormatException -> 0x0c26 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0c26 }
        r0.mMncLength = r2;	 Catch:{ NumberFormatException -> 0x0c26 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0c26 }
        r2.<init>();	 Catch:{ NumberFormatException -> 0x0c26 }
        r4 = "setting7 mMncLength=";	 Catch:{ NumberFormatException -> 0x0c26 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0c26 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0c26 }
        r4 = r0.mMncLength;	 Catch:{ NumberFormatException -> 0x0c26 }
        r2 = r2.append(r4);	 Catch:{ NumberFormatException -> 0x0c26 }
        r2 = r2.toString();	 Catch:{ NumberFormatException -> 0x0c26 }
        r0 = r24;	 Catch:{ NumberFormatException -> 0x0c26 }
        r0.log(r2);	 Catch:{ NumberFormatException -> 0x0c26 }
    L_0x0bb2:
        if (r14 == 0) goto L_0x004f;
    L_0x0bb4:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0bba:
        r2 = r14.length();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 < r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0bc6:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "update mccmnc=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r14.substring(r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        com.android.internal.telephony.MccTable.updateMccMncConfiguration(r2, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.RoamingStatusCollectHelper.getInstance();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mParentApp;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.getPhoneId();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r5 + 3;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r14.substring(r6, r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.onImsiGetDone(r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c19:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0b74;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c20:
        goto L_0x0b1a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c22:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0b48;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c26:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0bb2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0c4b:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0bb2;
    L_0x0c6f:
        r2 = 3;
        r2 = r11[r2];	 Catch:{ all -> 0x06ea }
        r2 = r2 & 15;	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.mMncLength = r2;	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r4 = 3;	 Catch:{ all -> 0x06ea }
        if (r2 <= r4) goto L_0x0c8c;	 Catch:{ all -> 0x06ea }
    L_0x0c7f:
        r2 = "setting4 mMncLength is too long";	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        r2 = 2;	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.mMncLength = r2;	 Catch:{ all -> 0x06ea }
    L_0x0c8c:
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06ea }
        r2.<init>();	 Catch:{ all -> 0x06ea }
        r4 = "setting4 mMncLength=";	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r2 = r2.toString();	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        goto L_0x058b;	 Catch:{ all -> 0x06ea }
    L_0x0cab:
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r4 = 2;	 Catch:{ all -> 0x06ea }
        if (r2 == r4) goto L_0x05b5;	 Catch:{ all -> 0x06ea }
    L_0x0cb2:
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r2 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r4 = 3;	 Catch:{ all -> 0x06ea }
        if (r2 == r4) goto L_0x05b5;	 Catch:{ all -> 0x06ea }
    L_0x0cb9:
        r2 = -1;	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.mMncLength = r2;	 Catch:{ all -> 0x06ea }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x06ea }
        r2.<init>();	 Catch:{ all -> 0x06ea }
        r4 = "setting5 mMncLength=";	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r4 = r0.mMncLength;	 Catch:{ all -> 0x06ea }
        r2 = r2.append(r4);	 Catch:{ all -> 0x06ea }
        r2 = r2.toString();	 Catch:{ all -> 0x06ea }
        r0 = r24;	 Catch:{ all -> 0x06ea }
        r0.log(r2);	 Catch:{ all -> 0x06ea }
        goto L_0x05b5;
    L_0x0cdd:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0620;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ce4:
        goto L_0x05c6;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ce6:
        r2 = r2 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x05f4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0cea:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x065e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d0f:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x065e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d33:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 2;	 Catch:{ RuntimeException -> 0x0059 }
        if (r4 != r5) goto L_0x0756;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d3a:
        goto L_0x06fc;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d3c:
        r4 = r4 + 1;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x072a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d40:
        r12 = move-exception;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "Corrupt IMSI! setting8 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0794;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d65:
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mMncLength = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "MNC length not present in EF_AD setting9 mMncLength=";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.mMncLength;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0794;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d89:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.getSpnFsm(r2, r9);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0d99:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0db0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0da9:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCff = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0db0:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CFF_CPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCff = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dd3:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0de3:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.parseEfSpdi(r11);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0dea:
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0df4:
        r2 = "update failed. ";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.logw(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e00:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e10:
        r23 = new com.android.internal.telephony.gsm.SimTlv;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r11.length;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r23;	 Catch:{ RuntimeException -> 0x0059 }
        r0.<init>(r11, r4, r2);	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e19:
        r2 = r23.isValidObject();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e1f:
        r2 = r23.getTag();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 67;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x0e3b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e27:
        r2 = r23.getData();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r23.getData();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.IccUtils.networkNameToString(r2, r5, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mPnnHomeName = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e3b:
        r23.nextObject();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x0e19;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e3f:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e4b:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (java.util.ArrayList) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleSmses(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e56:
        r2 = "ENF";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "marked read: sms ";	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0059 }
        android.telephony.Rlog.i(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e76:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r15 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r15 = (java.lang.Integer) r15;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0e88;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e86:
        if (r15 != 0) goto L_0x0eb0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0e88:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Error on SMS_ON_SIM with exp ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = " index ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r15);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0eb0:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "READ EF_SMS RECORD index=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r15);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r15.intValue();	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 22;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = r0.obtainMessage(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 28476; // 0x6f3c float:3.9903E-41 double:1.4069E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r2.loadEFLinearFixed(r6, r4, r5);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ee0:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0ef7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0eec:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (byte[]) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleSms(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ef7:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Error on GET_SMS with exp ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f14:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f24:
        r2 = new com.android.internal.telephony.uicc.UsimServiceTable;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mUsimServiceTable = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "SST: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mUsimServiceTable;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f4c:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f58:
        r2 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (byte[]) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mCphsInfo = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "iCPHS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mCphsInfo;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0f83:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EVENT_SET_MBDN_DONE ex:";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0fba;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0faa:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fba:
        r2 = r24.isCphsMailboxEnabled();	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1015;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fc0:
        r3 = new com.android.internal.telephony.uicc.AdnRecord;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r3.<init>(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = r0;	 Catch:{ RuntimeException -> 0x0059 }
        r21 = (android.os.Message) r21;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x0ff7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fd7:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x0ff7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0fdb:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        r2 = "Callback with MBDN successful.";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r21 = 0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x0ff7:
        r2 = new com.android.internal.telephony.uicc.AdnRecordLoader;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFh;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 25;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r1 = r21;	 Catch:{ RuntimeException -> 0x0059 }
        r8 = r0.obtainMessage(r4, r1);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 28439; // 0x6f17 float:3.9852E-41 double:1.40507E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = 28490; // 0x6f4a float:3.9923E-41 double:1.4076E-319;	 Catch:{ RuntimeException -> 0x0059 }
        r6 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r7 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r2.updateEF(r3, r4, r5, r6, r7, r8);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1015:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1019:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mContext;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "carrier_config";	 Catch:{ RuntimeException -> 0x0059 }
        r10 = r2.getSystemService(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r10 = (android.telephony.CarrierConfigManager) r10;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1054;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x102a:
        if (r10 == 0) goto L_0x1054;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x102c:
        r2 = r10.getConfig();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "editable_voicemail_number_bool";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.getBoolean(r4);	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1054;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1039:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = new com.android.internal.telephony.uicc.IccVmNotSupportedException;	 Catch:{ RuntimeException -> 0x0059 }
        r5 = "Update SIM voice mailbox error";	 Catch:{ RuntimeException -> 0x0059 }
        r4.<init>(r5);	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x104b:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1054:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x104b;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1061:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x109e;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x106d:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailNum;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailNum = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mNewVoiceMailTag;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mVoiceMailTag = r2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x107d:
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1081:
        r2 = "Callback with CPHS MB successful.";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = android.os.AsyncResult.forMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2.exception = r4;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.userObj;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = (android.os.Message) r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x109e:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Set CPHS MailBox with exception: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x107d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10ba:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x10d1;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10ca:
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCfis = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10d1:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CFIS: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEfCfis = r11;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x10f4:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x111d;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1100:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in fetching EF_CSP data ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x111d:
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "EF_CSP: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.handleEfCspData(r11);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1145:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x1177;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1155:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in get GID1 ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid1 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1177:
        r2 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid1 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "GID1: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mGid1;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x119e:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 == 0) goto L_0x11d0;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11ae:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Exception in get GID2 ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid2 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11d0:
        r2 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mGid2 = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "GID2: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mGid2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x11f7:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x1209;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1207:
        if (r11 != 0) goto L_0x1226;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1209:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting User PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1226:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mPlmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x124d:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x125f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x125d:
        if (r11 != 0) goto L_0x127c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x125f:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Operator PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x127c:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mOplmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12a3:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x12b5;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12b3:
        if (r11 != 0) goto L_0x12d2;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12b5:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Home PLMN with Access Tech Records: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x12d2:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Received a PlmnActRecord, raw=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = com.android.internal.telephony.uicc.IccUtils.bytesToHexString(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = com.android.internal.telephony.uicc.PlmnActRecord.getRecords(r11);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mHplmnActRecords = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "HplmnActRecord[]=";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mHplmnActRecords;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = java.util.Arrays.toString(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.log(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x131a:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x132c;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x132a:
        if (r11 != 0) goto L_0x1349;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x132c:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Equivalent Home PLMNs: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1349:
        r2 = "Equivalent Home";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.parseBcdPlmnList(r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mEhplmns = r2;	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1358:
        r16 = 1;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = r0.obj;	 Catch:{ RuntimeException -> 0x0059 }
        r9 = (android.os.AsyncResult) r9;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = r9.result;	 Catch:{ RuntimeException -> 0x0059 }
        r11 = (byte[]) r11;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != 0) goto L_0x136a;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1368:
        if (r11 != 0) goto L_0x1387;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x136a:
        r2 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0059 }
        r2.<init>();	 Catch:{ RuntimeException -> 0x0059 }
        r4 = "Failed getting Forbidden PLMNs: ";	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r9.exception;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.append(r4);	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r2.toString();	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x1387:
        r2 = "Forbidden";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.parseBcdPlmnList(r11, r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.mFplmns = r2;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg1;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 1238273; // 0x12e501 float:1.73519E-39 double:6.11788E-318;	 Catch:{ RuntimeException -> 0x0059 }
        if (r2 != r4) goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x139d:
        r16 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r25;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.arg2;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = java.lang.Integer.valueOf(r2);	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r22 = r0.retrievePendingResponseMessage(r2);	 Catch:{ RuntimeException -> 0x0059 }
        if (r22 == 0) goto L_0x13c7;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13af:
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = r0.mFplmns;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r0.mFplmns;	 Catch:{ RuntimeException -> 0x0059 }
        r4 = r4.length;	 Catch:{ RuntimeException -> 0x0059 }
        r2 = java.util.Arrays.copyOf(r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r22;	 Catch:{ RuntimeException -> 0x0059 }
        android.os.AsyncResult.forMessage(r0, r2, r4);	 Catch:{ RuntimeException -> 0x0059 }
        r22.sendToTarget();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13c7:
        r2 = "Failed to retrieve a response message for FPLMN";	 Catch:{ RuntimeException -> 0x0059 }
        r0 = r24;	 Catch:{ RuntimeException -> 0x0059 }
        r0.loge(r2);	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;	 Catch:{ RuntimeException -> 0x0059 }
    L_0x13d1:
        r24.handleCarrierNameOverride();	 Catch:{ RuntimeException -> 0x0059 }
        goto L_0x004f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SIMRecords.handleMessage(android.os.Message):void");
    }

    protected void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_CFF_CPHS /*28435*/:
            case IccConstants.EF_CFIS /*28619*/:
                log("SIM Refresh called for EF_CFIS or EF_CFF_CPHS");
                loadCallForwardingRecords();
                return;
            case IccConstants.EF_CSP_CPHS /*28437*/:
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            case IccConstants.EF_MAILBOX_CPHS /*28439*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            case IccConstants.EF_FDN /*28475*/:
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                this.mAdnCache.reset();
                return;
            case IccConstants.EF_MSISDN /*28480*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            case IccConstants.EF_MBDN /*28615*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            default:
                this.mAdnCache.reset();
                fetchSimRecords();
                return;
        }
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != (byte) 0) {
            Rlog.d("ENF", "status : " + ba[0]);
        }
        if (ba[0] == (byte) 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = (byte[]) messages.get(i);
            if (ba[0] != (byte) 0) {
                Rlog.i("ENF", "status " + i + ": " + ba[0]);
            }
            if (ba[0] == (byte) 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
                ba[0] = (byte) 1;
            }
        }
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    private void setVoiceCallForwardingFlagFromSimRecords() {
        int i = 1;
        if (validEfCfis(this.mEfCfis)) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else if (this.mEfCff != null) {
            if ((this.mEfCff[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else {
            this.mCallForwardingStatus = -1;
            log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (Resources.getSystem().getBoolean(17957055)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mParentApp.getState() == AppState.APPSTATE_PIN || this.mParentApp.getState() == AppState.APPSTATE_PUK) {
            this.mRecordsRequested = false;
            return;
        }
        String operator = getOperatorNumeric();
        if (TextUtils.isEmpty(operator)) {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        } else {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        }
        String imsi = getIMSI();
        if (TextUtils.isEmpty(imsi) || imsi.length() < 3) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            log("onAllRecordsLoaded set mcc imsi" + "");
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(Integer.parseInt(imsi.substring(0, 3))));
        }
        setVoiceMailByCountry(operator);
        if (TextUtils.isEmpty(getServiceProviderName())) {
            setSpnFromConfig(operator);
        }
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void handleCarrierNameOverride() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader == null || !configLoader.getConfig().getBoolean("carrier_name_override_bool")) {
            setSpnFromConfig(getOperatorNumeric());
            return;
        }
        String carrierName = configLoader.getConfig().getString("carrier_name_string");
        setServiceProviderName(carrierName);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), carrierName);
    }

    private void setDisplayName() {
        SubscriptionManager subManager = SubscriptionManager.from(this.mContext);
        int[] subId = SubscriptionManager.getSubId(this.mParentApp.getPhoneId());
        if (subId == null || subId.length <= 0) {
            log("subId not valid for Phone " + this.mParentApp.getPhoneId());
            return;
        }
        SubscriptionInfo subInfo = subManager.getActiveSubscriptionInfo(subId[0]);
        if (subInfo == null || subInfo.getNameSource() == 2) {
            log("SUB[" + this.mParentApp.getPhoneId() + "] " + subId[0] + " SubInfo not created yet");
        } else {
            CharSequence oldSubName = subInfo.getDisplayName();
            String newCarrierName = this.mTelephonyManager.getSimOperatorName(subId[0]);
            if (!(TextUtils.isEmpty(newCarrierName) || (newCarrierName.equals(oldSubName) ^ 1) == 0)) {
                log("sim name[" + this.mParentApp.getPhoneId() + "] = " + newCarrierName);
                SubscriptionController.getInstance().setDisplayName(newCarrierName, subId[0]);
            }
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void setSpnFromConfig(String carrier) {
        if (TextUtils.isEmpty(getServiceProviderName())) {
            try {
                OperatorName alpName = CustomPlmnOperatorOverride.getInstance().getOperator(carrier);
                if (alpName != null) {
                    setServiceProviderName(alpName.longName);
                    this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), alpName.longName);
                }
            } catch (Exception e) {
                log("CustomPlmnOperatorOverride is not init");
            }
        }
    }

    private void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrier(spn)) {
            this.mIsVoiceMailFixed = true;
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
        }
    }

    public void getForbiddenPlmns(Message response) {
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238273, storePendingResponseMessage(response)));
    }

    public void onReady() {
        fetchSimRecords();
    }

    private void onLocked() {
        log("only fetch EF_LI and EF_PL in lock state");
        loadEfLiAndEfPl();
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
            this.mRecordsRequested = true;
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
        }
    }

    private void loadCallForwardingRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
        this.mRecordsToLoad++;
    }

    protected void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        this.mRecordsToLoad++;
        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MBI, 1, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_AD, obtainMessage(9));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MWIS, 1, obtainMessage(7));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, obtainMessage(8));
        this.mRecordsToLoad++;
        loadCallForwardingRecords();
        getSpnFsm(true, null);
        this.mFh.loadEFTransparent(IccConstants.EF_SPDI, obtainMessage(13));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_PNN, 1, obtainMessage(15));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SST, obtainMessage(17));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_INFO_CPHS, obtainMessage(26));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID1, obtainMessage(34));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID2, obtainMessage(36));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_PLMN_W_ACT, obtainMessage(37));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_OPLMN_W_ACT, obtainMessage(38));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_HPLMN_W_ACT, obtainMessage(39));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_EHPLMN, obtainMessage(40));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238272, -1));
        this.mRecordsToLoad++;
        loadEfLiAndEfPl();
        this.mFh.getEFLinearRecordSize(IccConstants.EF_SMS, obtainMessage(28));
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    public int getDisplayRule(String plmn) {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null && this.mParentApp.getUiccCard().getOperatorBrandOverride() != null) {
            return 2;
        }
        if (TextUtils.isEmpty(getServiceProviderName()) || this.mSpnDisplayCondition == -1) {
            return 2;
        }
        if (isOnMatchingPlmn(plmn)) {
            if ((this.mSpnDisplayCondition & 1) == 1) {
                return 3;
            }
            return 1;
        } else if ((this.mSpnDisplayCondition & 2) == 0) {
            return 3;
        } else {
            return 2;
        }
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private boolean isOnMatchingPlmn(String plmn) {
        if (plmn == null) {
            return false;
        }
        String operator = getOperatorNumeric();
        if (plmn.equals(operator) || TelephonyPhoneUtils.isSameOperator(plmn, operator)) {
            return true;
        }
        if (this.mSpdiNetworks != null) {
            for (String spdiNet : this.mSpdiNetworks) {
                if (plmn.equals(spdiNet)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        byte[] data;
        String spn;
        switch (m14xf89fffa2()[this.mSpnState.ordinal()]) {
            case 1:
                setServiceProviderName(null);
                this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
                this.mRecordsToLoad++;
                this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
                break;
            case 2:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    this.mSpnDisplayCondition = -1;
                } else {
                    data = ar.result;
                    this.mSpnDisplayCondition = data[0] & 255;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        log("Load EF_SPN: no SPN on 3gpp, spnDisplayCondition: " + this.mSpnDisplayCondition);
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        log("Load EF_SPN: " + spn + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case 3:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_CPHS: " + spn);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case 4:
                if (ar == null || ar.exception != null) {
                    setServiceProviderName(null);
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        log("No SPN loaded in either CHPS or 3GPP");
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_SHORT_CPHS: " + spn);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                    }
                }
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
            default:
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
        }
    }

    private void parseEfSpdi(byte[] data) {
        SimTlv tlv = new SimTlv(data, 0, data.length);
        byte[] plmnEntries = null;
        while (tlv.isValidObject()) {
            if (tlv.getTag() == 163) {
                tlv = new SimTlv(tlv.getData(), 0, tlv.getData().length);
            }
            if (tlv.getTag() == 128) {
                plmnEntries = tlv.getData();
                break;
            }
            tlv.nextObject();
        }
        if (plmnEntries != null) {
            this.mSpdiNetworks = new ArrayList(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdToString(plmnEntries, i, 3);
                if (plmnCode.length() >= 5) {
                    log("EF_SPDI network: " + plmnCode);
                    this.mSpdiNetworks.add(plmnCode);
                }
            }
        }
    }

    private String[] parseBcdPlmnList(byte[] data, String description) {
        log("Received " + description + " PLMNs, raw=" + IccUtils.bytesToHexString(data));
        if (data.length == 0 || data.length % 3 != 0) {
            loge("Received invalid " + description + " PLMN list");
            return null;
        }
        int numPlmns = data.length / 3;
        String[] ret = new String[numPlmns];
        for (int i = 0; i < numPlmns; i++) {
            ret[i] = IccUtils.bcdPlmnToString(data, i * 3);
        }
        return ret;
    }

    private boolean isCphsMailboxEnabled() {
        boolean z = true;
        if (this.mCphsInfo == null) {
            return false;
        }
        if ((this.mCphsInfo[1] & 48) != 48) {
            z = false;
        }
        return z;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[SIMRecords] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[SIMRecords] " + s);
    }

    protected void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, "[SIMRecords] " + s, tr);
    }

    protected void logv(String s) {
        Rlog.v(LOG_TAG, "[SIMRecords] " + s);
    }

    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == (byte) -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + data[(i * 2) + 1]);
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                } else {
                    this.mCspPlmnEnabled = false;
                    log("[CSP] Set Automatic Network Selection");
                    this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                }
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SIMRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mVmConfig=" + this.mVmConfig);
        pw.println(" mSpnOverride=" + this.mSpnOverride);
        pw.println(" mCallForwardingStatus=" + this.mCallForwardingStatus);
        pw.println(" mSpnState=" + this.mSpnState);
        pw.println(" mCphsInfo=" + this.mCphsInfo);
        pw.println(" mCspPlmnEnabled=" + this.mCspPlmnEnabled);
        pw.println(" mEfMWIS[]=" + Arrays.toString(this.mEfMWIS));
        pw.println(" mEfCPHS_MWI[]=" + Arrays.toString(this.mEfCPHS_MWI));
        pw.println(" mEfCff[]=" + Arrays.toString(this.mEfCff));
        pw.println(" mEfCfis[]=" + Arrays.toString(this.mEfCfis));
        pw.println(" mSpnDisplayCondition=" + this.mSpnDisplayCondition);
        pw.println(" mSpdiNetworks[]=" + this.mSpdiNetworks);
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid1=" + (this.mFakeGid1 != null ? this.mFakeGid1 : "null"));
        }
        pw.println(" mGid2=" + this.mGid2);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid2=" + (this.mFakeGid2 != null ? this.mFakeGid2 : "null"));
        }
        pw.println(" mPlmnActRecords[]=" + Arrays.toString(this.mPlmnActRecords));
        pw.println(" mOplmnActRecords[]=" + Arrays.toString(this.mOplmnActRecords));
        pw.println(" mHplmnActRecords[]=" + Arrays.toString(this.mHplmnActRecords));
        pw.println(" mFplmns[]=" + Arrays.toString(this.mFplmns));
        pw.println(" mEhplmns[]=" + Arrays.toString(this.mEhplmns));
        pw.flush();
    }
}
