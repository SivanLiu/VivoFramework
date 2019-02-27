package android.telephony;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.graphics.Bitmap;
import android.text.TextUtils;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class FtSubInfo {
    public String mCarrierName = "";
    public int mColor = 0;
    public String mCountryIso = "";
    public int mDataRoaming = 0;
    public int mDispalyNumberFormat = 0;
    public String mDisplayName = "";
    public String mIccId = "";
    public Bitmap mIconBitmap = null;
    public int mMcc = 0;
    public int mMnc = 0;
    public int mNameSource = 0;
    public String mNumber = "";
    public int mNwMode = -1;
    public int mSimIconRes = 0;
    public int mSlotId = -1;
    public int mStatus = 0;
    public int mSubId = -1;
    String tostr;

    public String toString() {
        if (this.tostr == null) {
            StringBuilder s = new StringBuilder();
            s.append("vvvvvvvvvvvvvvvv FtSubInfo is:");
            s.append("\nFtSubInfo.mSubId:                ").append(this.mSubId);
            s.append("\nFtSubInfo.mIccId:                ");
            if (TextUtils.isEmpty(this.mIccId)) {
                s.append("null");
            } else {
                s.append(this.mIccId);
            }
            s.append("\nFtSubInfo.mSlotId:                 ").append(this.mSlotId);
            s.append("\nFtSubInfo.mDisplayName:          ");
            if (TextUtils.isEmpty(this.mDisplayName)) {
                s.append("null");
            } else {
                s.append(this.mDisplayName);
            }
            s.append("\nFtSubInfo.mCarrierName:          ");
            if (TextUtils.isEmpty(this.mCarrierName)) {
                s.append("null");
            } else {
                s.append(this.mCarrierName);
            }
            s.append("\nFtSubInfo.mNameSource:           ").append(this.mNameSource);
            s.append("\nFtSubInfo.mColor:                ").append(this.mColor);
            s.append("\nFtSubInfo.mNumber:               ");
            if (TextUtils.isEmpty(this.mNumber)) {
                s.append("null");
            } else {
                s.append(this.mNumber);
            }
            s.append("\nFtSubInfo.mDispalyNumberFormat:  ").append(this.mDispalyNumberFormat);
            s.append("\nFtSubInfo.mDataRoaming:     ").append(this.mDataRoaming);
            s.append("\nFtSubInfo.mSimIconRes:     ").append(this.mSimIconRes);
            s.append("\nFtSubInfo.mMcc:     ").append(this.mMcc);
            s.append("\nFtSubInfo.mMnc:     ").append(this.mMnc);
            s.append("\nFtSubInfo.mCountryIso:               ");
            if (TextUtils.isEmpty(this.mCountryIso)) {
                s.append("null");
            } else {
                s.append(this.mCountryIso);
            }
            s.append("\nFtSubInfo.mStatus:     ").append(this.mStatus);
            s.append("\nFtSubInfo.mNwMode:     ").append(this.mNwMode);
            this.tostr = s.toString();
        }
        return this.tostr;
    }
}
