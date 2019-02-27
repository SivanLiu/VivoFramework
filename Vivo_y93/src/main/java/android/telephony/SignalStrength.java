package android.telephony;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import com.android.internal.os.PowerProfile;

public class SignalStrength implements Parcelable {
    public static final Creator<SignalStrength> CREATOR = new Creator() {
        public SignalStrength createFromParcel(Parcel in) {
            return new SignalStrength(in);
        }

        public SignalStrength[] newArray(int size) {
            return new SignalStrength[size];
        }
    };
    private static final boolean DBG = false;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final String LOG_TAG = "SignalStrength";
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final String[] SIGNAL_STRENGTH_NAMES = new String[]{PowerProfile.POWER_NONE, "poor", "moderate", "good", "great"};
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    private static final int[] VIVO_LTE_RSRP_THRESH_OP_ENTRY = new int[]{-120, -118, -114, -105};
    private boolean isGsm;
    private int mCdmaDbm;
    private int mCdmaEcio;
    private int mCdmaLevel;
    private int mEvdoDbm;
    private int mEvdoEcio;
    private int mEvdoLevel;
    private int mEvdoSnr;
    private int mGsmBitErrorRate;
    private int mGsmLevel;
    private int mGsmSignalStrength;
    private int mLteCqi;
    private int mLteLevel;
    private int mLteRsrp;
    private int mLteRsrpBoost;
    private int mLteRsrq;
    private int mLteRssnr;
    private int mLteSignalStrength;
    private int mTdScdmaLevel;
    private int mTdScdmaRscp;
    private int[] mThreshRsrp;
    private int mWcdmaLevel;
    private boolean sIsCMCCEntry;
    private boolean sIsUNICOMEntry;
    private String vivo_op_entry = SystemProperties.get("ro.vivo.op.entry", "no");

    public static SignalStrength newFromBundle(Bundle m) {
        SignalStrength ret = new SignalStrength();
        ret.setFromNotifierBundle(m);
        return ret;
    }

    public SignalStrength() {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = true;
        this.mGsmLevel = 0;
        this.mWcdmaLevel = 0;
        this.mTdScdmaLevel = 0;
        this.mCdmaLevel = 0;
        this.mEvdoLevel = 0;
        this.mLteLevel = 0;
    }

    public SignalStrength(boolean gsmFlag) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        this.mGsmSignalStrength = 99;
        this.mGsmBitErrorRate = -1;
        this.mCdmaDbm = -1;
        this.mCdmaEcio = -1;
        this.mEvdoDbm = -1;
        this.mEvdoEcio = -1;
        this.mEvdoSnr = -1;
        this.mLteSignalStrength = 99;
        this.mLteRsrp = Integer.MAX_VALUE;
        this.mLteRsrq = Integer.MAX_VALUE;
        this.mLteRssnr = Integer.MAX_VALUE;
        this.mLteCqi = Integer.MAX_VALUE;
        this.mLteRsrpBoost = 0;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsmFlag;
        this.mGsmLevel = 0;
        this.mWcdmaLevel = 0;
        this.mTdScdmaLevel = 0;
        this.mCdmaLevel = 0;
        this.mEvdoLevel = 0;
        this.mLteLevel = 0;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, int tdScdmaRscp, boolean gsmFlag) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, lteRsrpBoost, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int tdScdmaRscp, boolean gsmFlag) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
        this.mTdScdmaRscp = tdScdmaRscp;
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, boolean gsmFlag) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, lteSignalStrength, lteRsrp, lteRsrq, lteRssnr, lteCqi, 0, gsmFlag);
    }

    public SignalStrength(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsmFlag) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsmFlag);
    }

    public SignalStrength(SignalStrength s) {
        boolean z;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        copyFrom(s);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, boolean gsm) {
        initialize(gsmSignalStrength, gsmBitErrorRate, cdmaDbm, cdmaEcio, evdoDbm, evdoEcio, evdoSnr, 99, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, gsm);
    }

    public void initialize(int gsmSignalStrength, int gsmBitErrorRate, int cdmaDbm, int cdmaEcio, int evdoDbm, int evdoEcio, int evdoSnr, int lteSignalStrength, int lteRsrp, int lteRsrq, int lteRssnr, int lteCqi, int lteRsrpBoost, boolean gsm) {
        this.mGsmSignalStrength = gsmSignalStrength;
        this.mGsmBitErrorRate = gsmBitErrorRate;
        this.mCdmaDbm = cdmaDbm;
        this.mCdmaEcio = cdmaEcio;
        this.mEvdoDbm = evdoDbm;
        this.mEvdoEcio = evdoEcio;
        this.mEvdoSnr = evdoSnr;
        this.mLteSignalStrength = lteSignalStrength;
        this.mLteRsrp = lteRsrp;
        this.mLteRsrq = lteRsrq;
        this.mLteRssnr = lteRssnr;
        this.mLteCqi = lteCqi;
        this.mLteRsrpBoost = lteRsrpBoost;
        this.mTdScdmaRscp = Integer.MAX_VALUE;
        this.isGsm = gsm;
    }

    protected void copyFrom(SignalStrength s) {
        this.mGsmSignalStrength = s.mGsmSignalStrength;
        this.mGsmBitErrorRate = s.mGsmBitErrorRate;
        this.mCdmaDbm = s.mCdmaDbm;
        this.mCdmaEcio = s.mCdmaEcio;
        this.mEvdoDbm = s.mEvdoDbm;
        this.mEvdoEcio = s.mEvdoEcio;
        this.mEvdoSnr = s.mEvdoSnr;
        this.mLteSignalStrength = s.mLteSignalStrength;
        this.mLteRsrp = s.mLteRsrp;
        this.mLteRsrq = s.mLteRsrq;
        this.mLteRssnr = s.mLteRssnr;
        this.mLteCqi = s.mLteCqi;
        this.mLteRsrpBoost = s.mLteRsrpBoost;
        this.mTdScdmaRscp = s.mTdScdmaRscp;
        this.isGsm = s.isGsm;
        this.mGsmLevel = s.mGsmLevel;
        this.mWcdmaLevel = s.mWcdmaLevel;
        this.mTdScdmaLevel = s.mTdScdmaLevel;
        this.mCdmaLevel = s.mCdmaLevel;
        this.mEvdoLevel = s.mEvdoLevel;
        this.mLteLevel = s.mLteLevel;
    }

    public SignalStrength(Parcel in) {
        boolean z;
        boolean z2 = true;
        if ("CMCC_RWA".equals(this.vivo_op_entry) || "CMCC".equals(this.vivo_op_entry) || "CMCC_RWB".equals(this.vivo_op_entry) || "FULL_CMCC_RWA".equals(this.vivo_op_entry)) {
            z = true;
        } else {
            z = "FULL_CMCC_RWB".equals(this.vivo_op_entry);
        }
        this.sIsCMCCEntry = z;
        if (this.vivo_op_entry != null) {
            z = this.vivo_op_entry.contains("UNICOM_RW");
        } else {
            z = false;
        }
        this.sIsUNICOMEntry = z;
        this.mGsmSignalStrength = in.readInt();
        this.mGsmBitErrorRate = in.readInt();
        this.mCdmaDbm = in.readInt();
        this.mCdmaEcio = in.readInt();
        this.mEvdoDbm = in.readInt();
        this.mEvdoEcio = in.readInt();
        this.mEvdoSnr = in.readInt();
        this.mLteSignalStrength = in.readInt();
        this.mLteRsrp = in.readInt();
        this.mLteRsrq = in.readInt();
        this.mLteRssnr = in.readInt();
        this.mLteCqi = in.readInt();
        this.mLteRsrpBoost = in.readInt();
        this.mTdScdmaRscp = in.readInt();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.isGsm = z2;
        this.mGsmLevel = in.readInt();
        this.mWcdmaLevel = in.readInt();
        this.mTdScdmaLevel = in.readInt();
        this.mCdmaLevel = in.readInt();
        this.mEvdoLevel = in.readInt();
        this.mLteLevel = in.readInt();
    }

    public static SignalStrength makeSignalStrengthFromRilParcel(Parcel in) {
        boolean z = false;
        SignalStrength ss = new SignalStrength();
        ss.mGsmSignalStrength = in.readInt();
        ss.mGsmBitErrorRate = in.readInt();
        ss.mCdmaDbm = in.readInt();
        ss.mCdmaEcio = in.readInt();
        ss.mEvdoDbm = in.readInt();
        ss.mEvdoEcio = in.readInt();
        ss.mEvdoSnr = in.readInt();
        ss.mLteSignalStrength = in.readInt();
        ss.mLteRsrp = in.readInt();
        ss.mLteRsrq = in.readInt();
        ss.mLteRssnr = in.readInt();
        ss.mLteCqi = in.readInt();
        ss.mTdScdmaRscp = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        ss.isGsm = z;
        ss.mGsmLevel = in.readInt();
        ss.mWcdmaLevel = in.readInt();
        ss.mTdScdmaLevel = in.readInt();
        ss.mCdmaLevel = in.readInt();
        ss.mEvdoLevel = in.readInt();
        ss.mLteLevel = in.readInt();
        return ss;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mGsmSignalStrength);
        out.writeInt(this.mGsmBitErrorRate);
        out.writeInt(this.mCdmaDbm);
        out.writeInt(this.mCdmaEcio);
        out.writeInt(this.mEvdoDbm);
        out.writeInt(this.mEvdoEcio);
        out.writeInt(this.mEvdoSnr);
        out.writeInt(this.mLteSignalStrength);
        out.writeInt(this.mLteRsrp);
        out.writeInt(this.mLteRsrq);
        out.writeInt(this.mLteRssnr);
        out.writeInt(this.mLteCqi);
        out.writeInt(this.mLteRsrpBoost);
        out.writeInt(this.mTdScdmaRscp);
        out.writeInt(this.isGsm ? 1 : 0);
        out.writeInt(this.mGsmLevel);
        out.writeInt(this.mWcdmaLevel);
        out.writeInt(this.mTdScdmaLevel);
        out.writeInt(this.mCdmaLevel);
        out.writeInt(this.mEvdoLevel);
        out.writeInt(this.mLteLevel);
    }

    public int describeContents() {
        return 0;
    }

    public void validateInput() {
        int i;
        int i2 = 99;
        int i3 = -1;
        int i4 = -120;
        int i5 = Integer.MAX_VALUE;
        this.mGsmSignalStrength = this.mGsmSignalStrength > 0 ? this.mGsmSignalStrength : 99;
        if (this.mCdmaDbm > 0) {
            i = -this.mCdmaDbm;
        } else {
            i = -120;
        }
        this.mCdmaDbm = i;
        this.mCdmaEcio = this.mCdmaEcio > 0 ? -this.mCdmaEcio : -160;
        if (this.mEvdoDbm > 0) {
            i4 = -this.mEvdoDbm;
        }
        this.mEvdoDbm = i4;
        if (this.mEvdoEcio >= 0) {
            i = -this.mEvdoEcio;
        } else {
            i = -1;
        }
        this.mEvdoEcio = i;
        if (this.mEvdoSnr > 0 && this.mEvdoSnr <= 8) {
            i3 = this.mEvdoSnr;
        }
        this.mEvdoSnr = i3;
        if (this.mLteSignalStrength >= 0) {
            i2 = this.mLteSignalStrength;
        }
        this.mLteSignalStrength = i2;
        if (this.mLteRsrp < 44 || this.mLteRsrp >= 140) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrp;
        }
        this.mLteRsrp = i;
        if (this.mLteRsrq < 3 || this.mLteRsrq > 20) {
            i = Integer.MAX_VALUE;
        } else {
            i = -this.mLteRsrq;
        }
        this.mLteRsrq = i;
        if (this.mLteRssnr < -200 || this.mLteRssnr > 300) {
            i = Integer.MAX_VALUE;
        } else {
            i = this.mLteRssnr;
        }
        this.mLteRssnr = i;
        if (this.mTdScdmaRscp >= 25 && this.mTdScdmaRscp <= 120) {
            i5 = -this.mTdScdmaRscp;
        }
        this.mTdScdmaRscp = i5;
    }

    public void setGsm(boolean gsmFlag) {
        this.isGsm = gsmFlag;
    }

    public void setThreshRsrp(int[] threshRsrp) {
        this.mThreshRsrp = threshRsrp;
    }

    public void setLteRsrpBoost(int lteRsrpBoost) {
        this.mLteRsrpBoost = lteRsrpBoost;
    }

    public int getGsmSignalStrength() {
        return this.mGsmSignalStrength;
    }

    public int getGsmBitErrorRate() {
        return this.mGsmBitErrorRate;
    }

    public int getCdmaDbm() {
        return this.mCdmaDbm;
    }

    public int getCdmaEcio() {
        return this.mCdmaEcio;
    }

    public int getEvdoDbm() {
        return this.mEvdoDbm;
    }

    public int getEvdoEcio() {
        return this.mEvdoEcio;
    }

    public int getEvdoSnr() {
        return this.mEvdoSnr;
    }

    public int getLteSignalStrength() {
        return this.mLteSignalStrength;
    }

    public int getLteRsrp() {
        return this.mLteRsrp;
    }

    public int getLteRsrq() {
        return this.mLteRsrq;
    }

    public int getLteRssnr() {
        return this.mLteRssnr;
    }

    public int getLteCqi() {
        return this.mLteCqi;
    }

    public int getLteRsrpBoost() {
        return this.mLteRsrpBoost;
    }

    public int getLevel() {
        if (this.isGsm) {
            int level = this.mLteLevel;
            if (level != 0) {
                return level;
            }
            level = this.mTdScdmaLevel;
            if (level != 0) {
                return level;
            }
            level = this.mWcdmaLevel;
            if (level == 0) {
                return this.mGsmLevel;
            }
            return level;
        } else if (this.mEvdoLevel == 0) {
            return this.mCdmaLevel;
        } else {
            if (this.mCdmaLevel == 0) {
                return this.mEvdoLevel;
            }
            return this.mCdmaLevel < this.mEvdoLevel ? this.mCdmaLevel : this.mEvdoLevel;
        }
    }

    public int getAsuLevel() {
        int asuLevel;
        if (this.isGsm) {
            asuLevel = getLteLevel() == 0 ? getTdScdmaLevel() == 0 ? getGsmAsuLevel() : getTdScdmaAsuLevel() : getLteAsuLevel();
        } else {
            int cdmaAsuLevel = getCdmaAsuLevel();
            int evdoAsuLevel = getEvdoAsuLevel();
            asuLevel = evdoAsuLevel == 0 ? cdmaAsuLevel : cdmaAsuLevel == 0 ? evdoAsuLevel : cdmaAsuLevel < evdoAsuLevel ? cdmaAsuLevel : evdoAsuLevel;
        }
        if (asuLevel == 255) {
            return 99;
        }
        return asuLevel;
    }

    public int getDbm() {
        if (isGsm()) {
            int dBm = getLteDbm();
            if (dBm == Integer.MAX_VALUE) {
                if (getTdScdmaLevel() == 0) {
                    dBm = getGsmDbm();
                } else {
                    dBm = getTdScdmaDbm();
                }
            }
            if (dBm == -1 || dBm == Integer.MAX_VALUE) {
                dBm = 0;
            }
            return dBm;
        }
        int cdmaDbm = getCdmaDbm();
        int evdoDbm = getEvdoDbm();
        if (evdoDbm != -120) {
            if (cdmaDbm == -120) {
                cdmaDbm = evdoDbm;
            } else if (cdmaDbm >= evdoDbm) {
                cdmaDbm = evdoDbm;
            }
        }
        return cdmaDbm;
    }

    public int getGsmDbm() {
        int gsmSignalStrength = getGsmSignalStrength();
        int asu = gsmSignalStrength == 99 ? -1 : gsmSignalStrength;
        if (asu != -1) {
            return (asu * 2) - 113;
        }
        return -1;
    }

    public void setGsmLevel(int[] gsmAsuThresh) {
        if (gsmAsuThresh != null && gsmAsuThresh.length >= 3) {
            if (this.mGsmSignalStrength == 99) {
                this.mGsmLevel = 0;
            } else if (this.mGsmSignalStrength >= gsmAsuThresh[2]) {
                this.mGsmLevel = 4;
            } else if (this.mGsmSignalStrength >= gsmAsuThresh[1]) {
                this.mGsmLevel = 3;
            } else if (this.mGsmSignalStrength >= gsmAsuThresh[0]) {
                this.mGsmLevel = 2;
            } else {
                this.mGsmLevel = 1;
            }
        }
    }

    public void setWcdmaLevel(int[] wcdmaAsuThresh) {
        if (wcdmaAsuThresh != null && wcdmaAsuThresh.length >= 3) {
            if (this.mGsmSignalStrength == 99) {
                this.mWcdmaLevel = 0;
            } else if (this.mGsmSignalStrength >= wcdmaAsuThresh[2]) {
                this.mWcdmaLevel = 4;
            } else if (this.mGsmSignalStrength >= wcdmaAsuThresh[1]) {
                this.mWcdmaLevel = 3;
            } else if (this.mGsmSignalStrength >= wcdmaAsuThresh[0]) {
                this.mWcdmaLevel = 2;
            } else {
                this.mWcdmaLevel = 1;
            }
        }
    }

    public int getGsmLevel() {
        return this.mGsmLevel;
    }

    public int getGsmAsuLevel() {
        return getGsmSignalStrength();
    }

    public void setCdmaLevel(int[] cdmaRxPowerThresh) {
        if (cdmaRxPowerThresh != null && cdmaRxPowerThresh.length >= 3) {
            if (this.mCdmaDbm == -120) {
                this.mCdmaLevel = 0;
            } else if (this.mCdmaDbm >= cdmaRxPowerThresh[2]) {
                this.mCdmaLevel = 4;
            } else if (this.mCdmaDbm >= cdmaRxPowerThresh[1]) {
                this.mCdmaLevel = 3;
            } else if (this.mCdmaDbm >= cdmaRxPowerThresh[0]) {
                this.mCdmaLevel = 2;
            } else {
                this.mCdmaLevel = 1;
            }
        }
    }

    public int getCdmaLevel() {
        return this.mCdmaLevel;
    }

    public int getCdmaAsuLevel() {
        int cdmaAsuLevel;
        int ecioAsuLevel;
        int cdmaDbm = getCdmaDbm();
        int cdmaEcio = getCdmaEcio();
        if (cdmaDbm >= -75) {
            cdmaAsuLevel = 16;
        } else if (cdmaDbm >= -82) {
            cdmaAsuLevel = 8;
        } else if (cdmaDbm >= -90) {
            cdmaAsuLevel = 4;
        } else if (cdmaDbm >= -95) {
            cdmaAsuLevel = 2;
        } else if (cdmaDbm >= -100) {
            cdmaAsuLevel = 1;
        } else {
            cdmaAsuLevel = 99;
        }
        if (cdmaEcio >= -90) {
            ecioAsuLevel = 16;
        } else if (cdmaEcio >= -100) {
            ecioAsuLevel = 8;
        } else if (cdmaEcio >= -115) {
            ecioAsuLevel = 4;
        } else if (cdmaEcio >= -130) {
            ecioAsuLevel = 2;
        } else if (cdmaEcio >= -150) {
            ecioAsuLevel = 1;
        } else {
            ecioAsuLevel = 99;
        }
        return cdmaAsuLevel < ecioAsuLevel ? cdmaAsuLevel : ecioAsuLevel;
    }

    public void setEvdoLevel(int[] evdoRxPowerThresh) {
        if (evdoRxPowerThresh != null && evdoRxPowerThresh.length >= 3) {
            if (this.mEvdoDbm == -120) {
                this.mEvdoLevel = 0;
            } else if (this.mEvdoDbm >= evdoRxPowerThresh[2]) {
                this.mEvdoLevel = 4;
            } else if (this.mEvdoDbm >= evdoRxPowerThresh[1]) {
                this.mEvdoLevel = 3;
            } else if (this.mEvdoDbm >= evdoRxPowerThresh[0]) {
                this.mEvdoLevel = 2;
            } else {
                this.mEvdoLevel = 1;
            }
        }
    }

    public int getEvdoLevel() {
        return this.mEvdoLevel;
    }

    public int getEvdoAsuLevel() {
        int levelEvdoDbm;
        int levelEvdoSnr;
        int evdoDbm = getEvdoDbm();
        int evdoSnr = getEvdoSnr();
        if (evdoDbm >= -65) {
            levelEvdoDbm = 16;
        } else if (evdoDbm >= -75) {
            levelEvdoDbm = 8;
        } else if (evdoDbm >= -85) {
            levelEvdoDbm = 4;
        } else if (evdoDbm >= -95) {
            levelEvdoDbm = 2;
        } else if (evdoDbm >= -105) {
            levelEvdoDbm = 1;
        } else {
            levelEvdoDbm = 99;
        }
        if (evdoSnr >= 7) {
            levelEvdoSnr = 16;
        } else if (evdoSnr >= 6) {
            levelEvdoSnr = 8;
        } else if (evdoSnr >= 5) {
            levelEvdoSnr = 4;
        } else if (evdoSnr >= 3) {
            levelEvdoSnr = 2;
        } else if (evdoSnr >= 1) {
            levelEvdoSnr = 1;
        } else {
            levelEvdoSnr = 99;
        }
        return levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
    }

    public int getLteDbm() {
        return this.mLteRsrp;
    }

    public void setLteLevel(int[] lteRsrpThresh) {
        if (this.sIsCMCCEntry || this.sIsUNICOMEntry) {
            if (this.mLteRsrp >= VIVO_LTE_RSRP_THRESH_OP_ENTRY[3]) {
                this.mLteLevel = 4;
            } else if (this.mLteRsrp >= VIVO_LTE_RSRP_THRESH_OP_ENTRY[2]) {
                this.mLteLevel = 3;
            } else if (this.mLteRsrp >= VIVO_LTE_RSRP_THRESH_OP_ENTRY[1]) {
                this.mLteLevel = 2;
            } else if (this.mLteRsrp >= VIVO_LTE_RSRP_THRESH_OP_ENTRY[0]) {
                this.mLteLevel = 1;
            } else {
                this.mLteLevel = 0;
            }
            if (this.mLteRsrp == Integer.MAX_VALUE) {
                this.mLteLevel = 0;
            }
        } else if (lteRsrpThresh != null && lteRsrpThresh.length >= 3) {
            if (this.mLteRsrp == Integer.MAX_VALUE) {
                this.mLteLevel = 0;
            } else if (this.mLteRsrp >= lteRsrpThresh[2]) {
                this.mLteLevel = 4;
            } else if (this.mLteRsrp >= lteRsrpThresh[1]) {
                this.mLteLevel = 3;
            } else if (this.mLteRsrp >= lteRsrpThresh[0]) {
                this.mLteLevel = 2;
            } else {
                this.mLteLevel = 1;
            }
        }
    }

    public int getLteLevel() {
        return this.mLteLevel;
    }

    public int getLteAsuLevel() {
        int lteDbm = getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return lteDbm + 140;
    }

    public boolean isGsm() {
        return this.isGsm;
    }

    public int getTdScdmaDbm() {
        return this.mTdScdmaRscp;
    }

    public void setTdScdmaLevel(int[] tdRscpThresh) {
        if (tdRscpThresh != null && tdRscpThresh.length >= 3) {
            if (this.mTdScdmaRscp > -25 || this.mTdScdmaRscp == Integer.MAX_VALUE) {
                this.mTdScdmaLevel = 0;
            } else if (this.mTdScdmaRscp >= tdRscpThresh[2]) {
                this.mTdScdmaLevel = 4;
            } else if (this.mTdScdmaRscp >= tdRscpThresh[1]) {
                this.mTdScdmaLevel = 3;
            } else if (this.mTdScdmaRscp >= tdRscpThresh[0]) {
                this.mTdScdmaLevel = 2;
            } else {
                this.mTdScdmaLevel = 1;
            }
        }
    }

    public int getTdScdmaLevel() {
        return this.mTdScdmaLevel;
    }

    public int getTdScdmaAsuLevel() {
        int tdScdmaDbm = getTdScdmaDbm();
        if (tdScdmaDbm == Integer.MAX_VALUE) {
            return 255;
        }
        return tdScdmaDbm + 120;
    }

    public int hashCode() {
        int i;
        int i2 = (this.mTdScdmaRscp * 31) + (((((((((((((this.mGsmSignalStrength * 31) + (this.mGsmBitErrorRate * 31)) + (this.mCdmaDbm * 31)) + (this.mCdmaEcio * 31)) + (this.mEvdoDbm * 31)) + (this.mEvdoEcio * 31)) + (this.mEvdoSnr * 31)) + (this.mLteSignalStrength * 31)) + (this.mLteRsrp * 31)) + (this.mLteRsrq * 31)) + (this.mLteRssnr * 31)) + (this.mLteCqi * 31)) + (this.mLteRsrpBoost * 31));
        if (this.isGsm) {
            i = 1;
        } else {
            i = 0;
        }
        return i + i2;
    }

    public void setGsmSignalStrength(int gsmSignalStrength) {
        this.mGsmSignalStrength = gsmSignalStrength;
    }

    public void setTdScdmaDbm(int tdScdmaDbm) {
        this.mTdScdmaRscp = tdScdmaDbm;
    }

    public void setLteRsrp(int lteRsrp) {
        this.mLteRsrp = lteRsrp;
    }

    public void setCdmaDbm(int cdmaDbm) {
        this.mCdmaDbm = cdmaDbm;
    }

    public void setEvdoDbm(int evdoDbm) {
        this.mEvdoDbm = evdoDbm;
    }

    public void setGsmBitErrorRate(int gsmBitErrorRate) {
        this.mGsmBitErrorRate = gsmBitErrorRate;
    }

    public boolean equals(Object o) {
        boolean z = false;
        try {
            SignalStrength s = (SignalStrength) o;
            if (o == null) {
                return false;
            }
            if (this.mGsmSignalStrength == s.mGsmSignalStrength && this.mGsmBitErrorRate == s.mGsmBitErrorRate && this.mCdmaDbm == s.mCdmaDbm && this.mCdmaEcio == s.mCdmaEcio && this.mEvdoDbm == s.mEvdoDbm && this.mEvdoEcio == s.mEvdoEcio && this.mEvdoSnr == s.mEvdoSnr && this.mLteSignalStrength == s.mLteSignalStrength && this.mLteRsrp == s.mLteRsrp && this.mLteRsrq == s.mLteRsrq && this.mLteRssnr == s.mLteRssnr && this.mLteCqi == s.mLteCqi && this.mLteRsrpBoost == s.mLteRsrpBoost && this.mTdScdmaRscp == s.mTdScdmaRscp && this.isGsm == s.isGsm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("SignalStrength: ").append(this.mGsmSignalStrength).append(" ").append(this.mGsmBitErrorRate).append(" ").append(this.mCdmaDbm).append(" ").append(this.mCdmaEcio).append(" ").append(this.mEvdoDbm).append(" ").append(this.mEvdoEcio).append(" ").append(this.mEvdoSnr).append(" ").append(this.mLteSignalStrength).append(" ").append(this.mLteRsrp).append(" ").append(this.mLteRsrq).append(" ").append(this.mLteRssnr).append(" ").append(this.mLteCqi).append(" ").append(this.mLteRsrpBoost).append(" ").append(this.mTdScdmaRscp).append(" ");
        if (this.isGsm) {
            str = "gsm|lte";
        } else {
            str = "cdma";
        }
        return append.append(str).append(" ").append(this.mGsmLevel).append(" ").append(this.mWcdmaLevel).append(" ").append(this.mTdScdmaLevel).append(" ").append(this.mCdmaLevel).append(" ").append(this.mEvdoLevel).append(" ").append(this.mLteLevel).toString();
    }

    private void setFromNotifierBundle(Bundle m) {
        this.mGsmSignalStrength = m.getInt("GsmSignalStrength");
        this.mGsmBitErrorRate = m.getInt("GsmBitErrorRate");
        this.mCdmaDbm = m.getInt("CdmaDbm");
        this.mCdmaEcio = m.getInt("CdmaEcio");
        this.mEvdoDbm = m.getInt("EvdoDbm");
        this.mEvdoEcio = m.getInt("EvdoEcio");
        this.mEvdoSnr = m.getInt("EvdoSnr");
        this.mLteSignalStrength = m.getInt("LteSignalStrength");
        this.mLteRsrp = m.getInt("LteRsrp");
        this.mLteRsrq = m.getInt("LteRsrq");
        this.mLteRssnr = m.getInt("LteRssnr");
        this.mLteCqi = m.getInt("LteCqi");
        this.mLteRsrpBoost = m.getInt("lteRsrpBoost");
        this.mTdScdmaRscp = m.getInt("TdScdma");
        this.isGsm = m.getBoolean("isGsm");
        this.mGsmLevel = m.getInt("GsmLevel");
        this.mWcdmaLevel = m.getInt("WcdmaLevel");
        this.mTdScdmaLevel = m.getInt("TdScdmaLevel");
        this.mCdmaLevel = m.getInt("CdmaLevel");
        this.mEvdoLevel = m.getInt("EvdoLevel");
        this.mLteLevel = m.getInt("LteLevel");
    }

    public void fillInNotifierBundle(Bundle m) {
        m.putInt("GsmSignalStrength", this.mGsmSignalStrength);
        m.putInt("GsmBitErrorRate", this.mGsmBitErrorRate);
        m.putInt("CdmaDbm", this.mCdmaDbm);
        m.putInt("CdmaEcio", this.mCdmaEcio);
        m.putInt("EvdoDbm", this.mEvdoDbm);
        m.putInt("EvdoEcio", this.mEvdoEcio);
        m.putInt("EvdoSnr", this.mEvdoSnr);
        m.putInt("LteSignalStrength", this.mLteSignalStrength);
        m.putInt("LteRsrp", this.mLteRsrp);
        m.putInt("LteRsrq", this.mLteRsrq);
        m.putInt("LteRssnr", this.mLteRssnr);
        m.putInt("LteCqi", this.mLteCqi);
        m.putInt("lteRsrpBoost", this.mLteRsrpBoost);
        m.putInt("TdScdma", this.mTdScdmaRscp);
        m.putBoolean("isGsm", this.isGsm);
        m.putInt("GsmLevel", this.mGsmLevel);
        m.putInt("WcdmaLevel", this.mWcdmaLevel);
        m.putInt("TdScdmaLevel", this.mTdScdmaLevel);
        m.putInt("CdmaLevel", this.mCdmaLevel);
        m.putInt("EvdoLevel", this.mEvdoLevel);
        m.putInt("LteLevel", this.mLteLevel);
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}
