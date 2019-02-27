package com.android.internal.telephony.uicc;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.TelephonyPhoneUtils;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class RuimRecords extends IccRecords {
    private static final int CSIM_IMSI_MNC_LENGTH = 2;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    static final String LOG_TAG = "RuimRecords";
    boolean mCsimSpnDisplayCondition;
    private byte[] mEFli;
    private byte[] mEFpl;
    private String mHomeNetworkId;
    private String mHomeSystemId;
    private String mMdn;
    private String mMin;
    private String mMin2Min1;
    private String mMyMobileNumber;
    private String mNai;
    private boolean mOtaCommited;
    private String mPrlVersion;

    private class EfCsimCdmaHomeLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimCdmaHomeLoaded(RuimRecords this$0, EfCsimCdmaHomeLoaded -this1) {
            this();
        }

        private EfCsimCdmaHomeLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> dataList = ar.result;
            RuimRecords.this.log("CSIM_CDMAHOME data size=" + dataList.size());
            if (!dataList.isEmpty()) {
                StringBuilder sidBuf = new StringBuilder();
                StringBuilder nidBuf = new StringBuilder();
                for (byte[] data : dataList) {
                    if (data.length == 5) {
                        int nid = ((data[3] & 255) << 8) | (data[2] & 255);
                        sidBuf.append(((data[1] & 255) << 8) | (data[0] & 255)).append(',');
                        nidBuf.append(nid).append(',');
                    }
                }
                sidBuf.setLength(sidBuf.length() - 1);
                nidBuf.setLength(nidBuf.length() - 1);
                RuimRecords.this.mHomeSystemId = sidBuf.toString();
                RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimEprlLoaded(RuimRecords this$0, EfCsimEprlLoaded -this1) {
            this();
        }

        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    private class EfCsimImsimLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimImsimLoaded(RuimRecords this$0, EfCsimImsimLoaded -this1) {
            this();
        }

        private EfCsimImsimLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            if (data == null || data.length < 10) {
                RuimRecords.this.log("Invalid IMSI from EF_CSIM_IMSIM " + IccUtils.bytesToHexString(data));
                RuimRecords.this.mImsi = null;
                RuimRecords.this.mMin = null;
                return;
            }
            RuimRecords.this.log("CSIM_IMSIM=" + IccUtils.bytesToHexString(data));
            if ((data[7] & 128) == 128) {
                RuimRecords.this.mImsi = RuimRecords.this.decodeImsi(data);
                if (!(TextUtils.isEmpty(RuimRecords.this.mImsi) || TelephonyPhoneUtils.isNumeric(RuimRecords.this.mImsi.substring(0, 3)))) {
                    RuimRecords.this.log("imsi not all numbers IMSI " + RuimRecords.this.mImsi);
                    RuimRecords.this.mImsi = null;
                }
                if (RuimRecords.this.mImsi != null) {
                    RuimRecords.this.mMin = RuimRecords.this.mImsi.substring(5, 15);
                    RuimRecords.this.log("IMSI: " + RuimRecords.this.mImsi.substring(0, 5) + "xxxxxxxxx");
                } else {
                    RuimRecords.this.log("IMSI is null");
                }
            } else {
                RuimRecords.this.log("IMSI not provisioned in card");
            }
            String operatorNumeric = RuimRecords.this.getOperatorNumeric();
            if (operatorNumeric != null && operatorNumeric.length() <= 6) {
                MccTable.updateMccMncConfiguration(RuimRecords.this.mContext, operatorNumeric, false);
            }
            RuimRecords.this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    private class EfCsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimLiLoaded(RuimRecords this$0, EfCsimLiLoaded -this1) {
            this();
        }

        private EfCsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case (byte) 1:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 110;
                        break;
                    case (byte) 2:
                        RuimRecords.this.mEFli[i] = (byte) 102;
                        RuimRecords.this.mEFli[i + 1] = (byte) 114;
                        break;
                    case (byte) 3:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 115;
                        break;
                    case (byte) 4:
                        RuimRecords.this.mEFli[i] = (byte) 106;
                        RuimRecords.this.mEFli[i + 1] = (byte) 97;
                        break;
                    case (byte) 5:
                        RuimRecords.this.mEFli[i] = (byte) 107;
                        RuimRecords.this.mEFli[i + 1] = (byte) 111;
                        break;
                    case (byte) 6:
                        RuimRecords.this.mEFli[i] = (byte) 122;
                        RuimRecords.this.mEFli[i + 1] = (byte) 104;
                        break;
                    case (byte) 7:
                        RuimRecords.this.mEFli[i] = (byte) 104;
                        RuimRecords.this.mEFli[i + 1] = (byte) 101;
                        break;
                    default:
                        RuimRecords.this.mEFli[i] = (byte) 32;
                        RuimRecords.this.mEFli[i + 1] = (byte) 32;
                        break;
                }
            }
            RuimRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(RuimRecords.this.mEFli));
        }
    }

    private class EfCsimMdnLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimMdnLoaded(RuimRecords this$0, EfCsimMdnLoaded -this1) {
            this();
        }

        private EfCsimMdnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_MDN=" + IccUtils.bytesToHexString(data));
            RuimRecords.this.mMdn = IccUtils.cdmaBcdToString(data, 1, data[0] & 15);
            RuimRecords.this.log("CSIM MDN=" + RuimRecords.this.mMdn);
        }
    }

    private class EfCsimMipUppLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimMipUppLoaded(RuimRecords this$0, EfCsimMipUppLoaded -this1) {
            this();
        }

        private EfCsimMipUppLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MIPUPP";
        }

        boolean checkLengthLegal(int length, int expectLength) {
            if (length >= expectLength) {
                return true;
            }
            Log.e(RuimRecords.LOG_TAG, "CSIM MIPUPP format error, length = " + length + "expected length at least =" + expectLength);
            return false;
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            try {
                int mipUppLength = bitStream.read(8) << 3;
                if (checkLengthLegal(mipUppLength, 1)) {
                    mipUppLength--;
                    if (bitStream.read(1) == 1) {
                        if (checkLengthLegal(mipUppLength, 11)) {
                            bitStream.skip(11);
                            mipUppLength -= 11;
                        } else {
                            return;
                        }
                    }
                    if (checkLengthLegal(mipUppLength, 4)) {
                        int numNai = bitStream.read(4);
                        mipUppLength -= 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength, 4)) {
                            int naiEntryIndex = bitStream.read(4);
                            mipUppLength -= 4;
                            if (checkLengthLegal(mipUppLength, 8)) {
                                int naiLength = bitStream.read(8);
                                mipUppLength -= 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength, naiLength << 3)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(8) & 255);
                                        }
                                        RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                        }
                                        return;
                                    }
                                    return;
                                }
                                if (checkLengthLegal(mipUppLength, (naiLength << 3) + 102)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    mipUppLength -= (naiLength << 3) + 102;
                                    if (bitStream.read(1) == 1) {
                                        if (checkLengthLegal(mipUppLength, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength, 5)) {
                                        bitStream.skip(4);
                                        mipUppLength = (mipUppLength - 4) - 1;
                                        if (bitStream.read(1) == 1) {
                                            if (checkLengthLegal(mipUppLength, 32)) {
                                                bitStream.skip(32);
                                                mipUppLength -= 32;
                                            } else {
                                                return;
                                            }
                                        }
                                        index++;
                                    } else {
                                        return;
                                    }
                                }
                                return;
                            }
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read Exception error!");
            }
        }
    }

    private class EfCsimSpnLoaded implements IccRecordLoaded {
        /* synthetic */ EfCsimSpnLoaded(RuimRecords this$0, EfCsimSpnLoaded -this1) {
            this();
        }

        private EfCsimSpnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRecordLoaded(AsyncResult ar) {
            boolean z;
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords ruimRecords = RuimRecords.this;
            if ((data[0] & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            ruimRecords.mCsimSpnDisplayCondition = z;
            int encoding = data[1];
            int language = data[2];
            byte[] spnData = new byte[32];
            System.arraycopy(data, 3, spnData, 0, data.length + -3 < 32 ? data.length - 3 : 32);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & 255) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName("");
                return;
            }
            switch (encoding) {
                case 0:
                case 8:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
                    break;
                case 2:
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (!TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.log("Some corruption in SPN decoding = " + spn);
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                        break;
                    }
                    RuimRecords.this.setServiceProviderName(spn);
                    break;
                case 3:
                case 9:
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    break;
                case 4:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                    break;
                default:
                    try {
                        RuimRecords.this.log("SPN encoding not supported");
                        break;
                    } catch (Exception e) {
                        RuimRecords.this.log("spn decode error: " + e);
                        break;
                    }
            }
            RuimRecords.this.log("spn=" + RuimRecords.this.getServiceProviderName());
            RuimRecords.this.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
        }
    }

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(RuimRecords this$0, EfPlLoaded -this1) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=" + "xxxx" + " mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=" + this.mMdn + " mMin=" + this.mMin + " mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mOtaCommited = false;
        this.mEFpl = null;
        this.mEFli = null;
        this.mCsimSpnDisplayCondition = false;
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        log("RuimRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing RuimRecords " + this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    protected void finalize() {
        log("RuimRecords finalized");
    }

    protected void resetRecords() {
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mAdnCache.reset();
        this.mRecordsRequested = false;
    }

    public String getMdnNumber() {
        return this.mMyMobileNumber;
    }

    public String getCdmaMin() {
        return this.mMin2Min1;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public String getNAI() {
        return this.mNai;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        loge("method setVoiceMailNumber is not implemented");
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }

    private int decodeImsiDigits(int digits, int length) {
        int i;
        int constant = 0;
        for (i = 0; i < length; i++) {
            constant = (constant * 10) + 1;
        }
        digits += constant;
        int denominator = 1;
        for (i = 0; i < length; i++) {
            if ((digits / denominator) % 10 == 0) {
                digits -= denominator * 10;
            }
            denominator *= 10;
        }
        return digits;
    }

    private String decodeImsi(byte[] data) {
        int mcc = decodeImsiDigits(((data[9] & 3) << 8) | (data[8] & 255), 3);
        int digits_11_12 = decodeImsiDigits(data[6] & 127, 2);
        int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
        int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
        int digit7 = (data[4] >> 2) & 15;
        if (digit7 > 9) {
            digit7 = 0;
        }
        int last3digits = ((data[4] & 3) << 8) | (data[3] & 255);
        first3digits = decodeImsiDigits(first3digits, 3);
        second3digits = decodeImsiDigits(second3digits, 3);
        last3digits = decodeImsiDigits(last3digits, 3);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(mcc)}));
        builder.append(String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(digits_11_12)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits)}));
        builder.append(String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits)}));
        return builder.toString();
    }

    public String getOperatorNumeric() {
        return getRUIMOperatorNumeric();
    }

    public String getRUIMOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            return null;
        }
        if (this.mMncLength == -1 || this.mMncLength == 0) {
            return this.mImsi.substring(0, 5);
        }
        return imsi.substring(0, this.mMncLength + 3);
    }

    private void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            AsyncResult ar;
            switch (msg.what) {
                case 1:
                    onReady();
                    break;
                case 4:
                    log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
                    break;
                case 5:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    byte[] data = ar.result;
                    if (ar.exception == null) {
                        this.mIccId = IccUtils.bchToString(data, 0, data.length);
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        break;
                    }
                    break;
                case 10:
                    ar = msg.obj;
                    String[] localTemp = ar.result;
                    if (ar.exception == null) {
                        this.mMyMobileNumber = localTemp[0];
                        this.mMin2Min1 = localTemp[3];
                        this.mPrlVersion = localTemp[4];
                        log("MDN: " + this.mMyMobileNumber + " MIN: " + this.mMin2Min1);
                        break;
                    }
                    break;
                case 14:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                        break;
                    }
                    break;
                case 17:
                    log("Event EVENT_GET_SST_DONE Received");
                    break;
                case 18:
                case 19:
                case 21:
                case 22:
                    Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
            if (null != null) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (null != null) {
                onRecordLoaded();
            }
        }
    }

    private static String[] getAssetLanguages(Context ctx) {
        String[] locales = ctx.getAssets().getLocales();
        String[] localeLangs = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            String localeStr = locales[i];
            int separator = localeStr.indexOf(45);
            if (separator < 0) {
                localeLangs[i] = localeStr;
            } else {
                localeLangs[i] = localeStr.substring(0, separator);
            }
        }
        return localeLangs;
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

    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (Resources.getSystem().getBoolean(17957055)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(10));
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    private void fetchRuimRecords() {
        if (this.mContext != null) {
            this.mRecordsRequested = true;
            log("fetchRuimRecords " + this.mRecordsToLoad);
            this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(28474, obtainMessage(100, new EfCsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(28481, obtainMessage(100, new EfCsimSpnLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.getEFLinearRecordSize(IccConstants.EF_SMS, obtainMessage(28));
            log("fetchRuimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        }
    }

    public int getDisplayRule(String plmn) {
        return 0;
    }

    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    protected void handleFileUpdate(int efid) {
        this.mAdnCache.reset();
        fetchRuimRecords();
    }

    public String getMdn() {
        return this.mMdn;
    }

    public String getMin() {
        return this.mMin;
    }

    public String getSid() {
        return this.mHomeSystemId;
    }

    public String getNid() {
        return this.mHomeNetworkId;
    }

    public boolean getCsimSpnDisplayCondition() {
        return this.mCsimSpnDisplayCondition;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[RuimRecords] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RuimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mOtaCommited=" + this.mOtaCommited);
        pw.println(" mMyMobileNumber=" + this.mMyMobileNumber);
        pw.println(" mMin2Min1=" + this.mMin2Min1);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mEFpl[]=" + Arrays.toString(this.mEFpl));
        pw.println(" mEFli[]=" + Arrays.toString(this.mEFli));
        pw.println(" mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.flush();
    }
}
