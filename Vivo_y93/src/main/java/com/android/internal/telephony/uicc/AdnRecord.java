package com.android.internal.telephony.uicc;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import java.util.Arrays;

public class AdnRecord implements Parcelable {
    static final int ADN_BCD_NUMBER_LENGTH = 0;
    static final int ADN_CAPABILITY_ID = 12;
    static final int ADN_DIALING_NUMBER_END = 11;
    static final int ADN_DIALING_NUMBER_START = 2;
    static final int ADN_EXTENSION_ID = 13;
    static final int ADN_TON_AND_NPI = 1;
    public static final Creator<AdnRecord> CREATOR = new Creator<AdnRecord>() {
        public AdnRecord createFromParcel(Parcel source) {
            return new AdnRecord(source.readInt(), source.readInt(), source.readString(), source.readString(), source.readStringArray(), source.readStringArray());
        }

        public AdnRecord[] newArray(int size) {
            return new AdnRecord[size];
        }
    };
    static final int EXT_RECORD_LENGTH_BYTES = 13;
    static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    static final int EXT_RECORD_TYPE_MASK = 3;
    static final int FOOTER_SIZE_BYTES = 14;
    static final String LOG_TAG = "AdnRecord";
    static final int MAX_EXT_CALLED_PARTY_LENGTH = 10;
    static final int MAX_NUMBER_SIZE_BYTES = 11;
    String[] mAdditionalNumbers;
    String mAlphaTag;
    int mEfid;
    String[] mEmails;
    int mExtRecord;
    String mNumber;
    int mRecordNumber;

    public AdnRecord(byte[] record) {
        this(0, 0, record);
    }

    public AdnRecord(int efid, int recordNumber, byte[] record) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        parseRecord(record);
    }

    public AdnRecord(String alphaTag, String number) {
        this(0, 0, alphaTag, number);
    }

    public AdnRecord(String alphaTag, String number, String[] emails) {
        this(0, 0, alphaTag, number, emails);
    }

    public AdnRecord(String alphaTag, String number, String[] emails, String[] additionalNumbers) {
        this(0, 0, alphaTag, number, emails, additionalNumbers);
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.mAdditionalNumbers = null;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails, String[] additionalNumbers) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.mAdditionalNumbers = covertPauseAndWaitForSim(additionalNumbers);
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = 255;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = null;
        this.mAdditionalNumbers = null;
    }

    public String getAlphaTag() {
        return this.mAlphaTag;
    }

    public int getEfid() {
        return this.mEfid;
    }

    public int getRecId() {
        return this.mRecordNumber;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public String[] getEmails() {
        return this.mEmails;
    }

    public void setEmails(String[] emails) {
        this.mEmails = emails;
    }

    public String[] getAdditionalNumbers() {
        return this.mAdditionalNumbers;
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public void setAdditionalNumbers(String[] additionalNumbers) {
        this.mAdditionalNumbers = covertPauseAndWaitForSim(additionalNumbers);
    }

    public int getRecordNumber() {
        return this.mRecordNumber;
    }

    public void setRecordNumber(int recNumber) {
        this.mRecordNumber = recNumber;
    }

    public String toString() {
        return "ADN Record '" + this.mAlphaTag + "' '" + Rlog.pii(LOG_TAG, this.mNumber) + " " + Rlog.pii(LOG_TAG, this.mEmails) + " " + Rlog.pii(LOG_TAG, this.mAdditionalNumbers) + "'";
    }

    public boolean isEmpty() {
        if (TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && this.mEmails == null && this.mAdditionalNumbers == null) {
            return true;
        }
        return false;
    }

    public boolean hasExtendedRecord() {
        return (this.mExtRecord == 0 || this.mExtRecord == 255) ? false : true;
    }

    private static boolean stringCompareNullEqualsEmpty(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        Object s22;
        if (s1 == null) {
            s1 = "";
        }
        if (s22 == null) {
            s22 = "";
        }
        return s1.equals(s22);
    }

    private static boolean arrayCompareNullEqualsEmpty(String[] s1, String[] s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null) {
            s1 = new String[]{""};
        }
        if (s2 == null) {
            s2 = new String[]{""};
        }
        for (String str : s1) {
            if (!TextUtils.isEmpty(str) && !Arrays.asList(s2).contains(str)) {
                return false;
            }
        }
        for (String str2 : s2) {
            if (!TextUtils.isEmpty(str2) && !Arrays.asList(s1).contains(str2)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEqual(AdnRecord adn) {
        if (stringCompareNullEqualsEmpty(this.mAlphaTag, adn.mAlphaTag) && stringCompareNullEqualsEmpty(this.mNumber, adn.mNumber) && arrayCompareNullEqualsEmpty(this.mEmails, adn.mEmails)) {
            return arrayCompareNullEqualsEmpty(this.mAdditionalNumbers, adn.mAdditionalNumbers);
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEfid);
        dest.writeInt(this.mRecordNumber);
        dest.writeString(this.mAlphaTag);
        dest.writeString(this.mNumber);
        dest.writeStringArray(this.mEmails);
        dest.writeStringArray(this.mAdditionalNumbers);
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public byte[] buildAdnString(int recordSize) {
        int footerOffset = recordSize - 14;
        byte[] adnString = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            adnString[i] = (byte) -1;
        }
        if (TextUtils.isEmpty(this.mNumber) && TextUtils.isEmpty(this.mAlphaTag)) {
            Rlog.w(LOG_TAG, "[buildAdnString] Empty dialing number");
            return adnString;
        } else if (this.mNumber == null || this.mNumber.length() <= 20) {
            byte[] byteTag;
            if (TextUtils.isEmpty(this.mAlphaTag)) {
                byteTag = new byte[0];
            } else {
                byteTag = IccUtils.stringToAdnStringField(this.mAlphaTag);
            }
            if (byteTag.length > footerOffset) {
                Rlog.w(LOG_TAG, "[buildAdnString] Max length of tag is " + footerOffset);
                return null;
            }
            if (!TextUtils.isEmpty(this.mNumber)) {
                byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(PhoneNumberUtils.convertPreDial(this.mNumber));
                System.arraycopy(bcdNumber, 0, adnString, footerOffset + 1, bcdNumber.length);
                adnString[footerOffset + 0] = (byte) bcdNumber.length;
            }
            adnString[footerOffset + 12] = (byte) -1;
            adnString[footerOffset + 13] = (byte) -1;
            if (byteTag.length > 0) {
                System.arraycopy(byteTag, 0, adnString, 0, byteTag.length);
            }
            return adnString;
        } else {
            Rlog.w(LOG_TAG, "[buildAdnString] Max length of dialing number is 20");
            return null;
        }
    }

    public void appendExtRecord(byte[] extRecord) {
        try {
            if (extRecord.length == 13 && (extRecord[0] & 3) == 2 && (extRecord[1] & 255) <= 10) {
                this.mNumber += PhoneNumberUtils.calledPartyBCDFragmentToString(extRecord, 2, extRecord[1] & 255);
            }
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord ext record", ex);
        }
    }

    private void parseRecord(byte[] record) {
        try {
            this.mAlphaTag = IccUtils.adnStringFieldToString(record, 0, record.length - 14);
            int footerOffset = record.length - 14;
            int numberLength = record[footerOffset] & 255;
            if (numberLength > 11) {
                this.mNumber = "";
                return;
            }
            this.mNumber = PhoneNumberUtils.calledPartyBCDToString(record, footerOffset + 1, numberLength);
            this.mExtRecord = record[record.length - 1] & 255;
            this.mEmails = null;
            this.mAdditionalNumbers = null;
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord", ex);
            this.mNumber = "";
            this.mAlphaTag = "";
            this.mEmails = null;
            this.mAdditionalNumbers = null;
        }
    }

    @VivoHook(hookType = VivoHookType.NEW_METHOD)
    private static String[] covertPauseAndWaitForSim(String[] numbers) {
        if (numbers != null && numbers.length > 0) {
            for (int i = 0; i < numbers.length; i++) {
                if (!TextUtils.isEmpty(numbers[i])) {
                    numbers[i] = numbers[i].replace(",", "p");
                    numbers[i] = numbers[i].replace(";", "w");
                }
            }
        }
        return numbers;
    }
}
