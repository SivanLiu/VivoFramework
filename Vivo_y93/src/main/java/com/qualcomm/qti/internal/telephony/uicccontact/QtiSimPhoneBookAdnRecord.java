package com.qualcomm.qti.internal.telephony.uicccontact;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.text.TextUtils;
import java.util.Arrays;

public class QtiSimPhoneBookAdnRecord {
    static final String LOG_TAG = "QtiSimPhoneBookAdnRecord";
    public int mAdNumCount = 0;
    public String[] mAdNumbers = null;
    public String mAlphaTag = null;
    public int mEmailCount = 0;
    public String[] mEmails = null;
    public int mInstanceId = -1;
    public String mNumber = null;
    public int mRecordIndex = 0;

    public int getRecordIndex() {
        return this.mRecordIndex;
    }

    public String getAlphaTag() {
        return this.mAlphaTag;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public int getNumEmails() {
        return this.mEmailCount;
    }

    public String[] getEmails() {
        return this.mEmails;
    }

    public int getNumAdNumbers() {
        return this.mAdNumCount;
    }

    public String[] getAdNumbers() {
        return this.mAdNumbers;
    }

    public int getInstanceId() {
        return this.mInstanceId;
    }

    public static String ConvertToPhoneNumber(String input) {
        return input == null ? null : input.replace('e', ';').replace('T', ',').replace('?', 'N');
    }

    @VivoHook(hookType = VivoHookType.CHANGE_CODE)
    public static String ConvertToRecordNumber(String input) {
        return input == null ? null : input.replace(';', 'e').replace(',', 'T').replace('w', 'e').replace('p', 'T').replace('N', '?');
    }

    public boolean isEmpty() {
        if (TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && this.mEmails == null && this.mAdNumbers == null) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimPhoneBookAdnRecord{").append("index =").append(this.mRecordIndex);
        sb.append(", name = ").append(this.mAlphaTag == null ? "null" : this.mAlphaTag);
        sb.append(", number = ").append(this.mNumber == null ? "null" : this.mNumber);
        sb.append(", email count = ").append(this.mEmailCount);
        sb.append(", email = ").append(Arrays.toString(this.mEmails));
        sb.append(", ad number count = ").append(this.mAdNumCount);
        sb.append(", ad number = ").append(Arrays.toString(this.mAdNumbers));
        sb.append(", instanceId = ").append(this.mInstanceId);
        sb.append("}");
        return sb.toString();
    }
}
