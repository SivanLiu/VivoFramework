package com.vivo.services.cust.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ContactAttr implements Parcelable {
    public static final String ATTR_AUTORECORD = "AUTORECORD";
    public static final String ATTR_CUSTTYPE = "CUSTTYPE";
    public static final String ATTR_NUM = "NUMBER";
    public static final String ATTR_PHONE_BEHAVIOR = "PHONE_BEHAVIOR";
    public static final String ATTR_PHONE_BLACKLIST = "PHONE_BLACKLIST";
    public static final String ATTR_PHONE_SIMSLOT = "PHONE_SIMSLOT";
    public static final String ATTR_PHONE_WHITELIST = "PHONE_WHITELIST";
    public static final String ATTR_RETAIN1 = "RETAIN1";
    public static final String ATTR_RETAIN2 = "RETAIN2";
    public static final String ATTR_RETAIN3 = "RETAIN3";
    public static final String ATTR_RETAIN4 = "RETAIN4";
    public static final String ATTR_RETAIN5 = "RETAIN5";
    public static final String ATTR_RETAIN6 = "RETAIN6";
    public static final String ATTR_RETAIN7 = "RETAIN7";
    public static final String ATTR_SMS_BEHAVIOR = "SMS_BEHAVIOR";
    public static final String ATTR_SMS_BLACKlIST = "SMS_BLACKLIST";
    public static final String ATTR_SMS_SIMSLOT = "SMS_SIMSLOT";
    public static final String ATTR_SMS_WHITELIST = "SMS_WHITELIST";
    public static final Creator<ContactAttr> CREATOR = new Creator<ContactAttr>() {
        public ContactAttr createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            ContactAttr cta = new ContactAttr(in.readString());
            cta.autoRecord = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.phone_blackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.phone_whiteList = z;
            cta.phone_behavior = in.readString();
            cta.phone_simslot = in.readString();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.sms_blackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.sms_whiteList = z;
            cta.sms_behavior = in.readString();
            cta.sms_simslot = in.readString();
            cta.retain1 = in.readString();
            cta.retain2 = in.readString();
            cta.retain3 = in.readString();
            cta.retain4 = in.readString();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.retain5 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            cta.retain6 = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            cta.retain7 = z2;
            cta.custType = in.readString();
            return cta;
        }

        public ContactAttr[] newArray(int size) {
            return new ContactAttr[size];
        }
    };
    public boolean autoRecord;
    public String custType;
    public String number;
    public String phone_behavior;
    public boolean phone_blackList;
    public String phone_simslot;
    public boolean phone_whiteList;
    public String retain1;
    public String retain2;
    public String retain3;
    public String retain4;
    public boolean retain5;
    public boolean retain6;
    public boolean retain7;
    public String sms_behavior;
    public boolean sms_blackList;
    public String sms_simslot;
    public boolean sms_whiteList;

    public ContactAttr(String num) {
        this.number = num;
        this.autoRecord = false;
        this.phone_blackList = false;
        this.phone_whiteList = false;
        this.phone_behavior = "all";
        this.phone_simslot = "all";
        this.sms_blackList = false;
        this.sms_whiteList = false;
        this.sms_behavior = "all";
        this.sms_simslot = "all";
        this.custType = "0";
    }

    public ContactAttr(String num, boolean record, boolean pbList, boolean pwList, String pbehavior, String psim, boolean sbList, boolean swList, String sbehavior, String ssim, String r1, String r2, String r3, String r4, boolean r5, boolean r6, boolean r7, String ctype) {
        this.number = num;
        this.autoRecord = record;
        this.phone_blackList = pbList;
        this.phone_whiteList = pwList;
        this.phone_behavior = pbehavior;
        this.phone_simslot = psim;
        this.sms_blackList = sbList;
        this.sms_whiteList = swList;
        this.sms_behavior = sbehavior;
        this.sms_simslot = ssim;
        this.retain1 = r1;
        this.retain2 = r2;
        this.retain3 = r3;
        this.retain4 = r4;
        this.retain5 = r5;
        this.retain6 = r6;
        this.retain7 = r7;
        this.custType = ctype;
    }

    public int describeContents() {
        return 0;
    }

    public void updatePhoneBalckBehavior(String blackBehavior) {
        if (this.phone_behavior == null || "all".equals(this.phone_behavior)) {
            this.phone_behavior = blackBehavior + ",both";
            return;
        }
        String[] strArray = this.phone_behavior.split(",");
        if (strArray == null || strArray.length < 2) {
            this.phone_behavior = blackBehavior + ",both";
            return;
        }
        strArray[0] = blackBehavior;
        this.phone_behavior = strArray[0] + "," + strArray[1];
    }

    public String getPhoneBalckBehavior() {
        String result = "both";
        if (this.phone_behavior == null) {
            this.phone_behavior = "all";
            return result;
        } else if ("all".equals(this.phone_behavior)) {
            return result;
        } else {
            String[] strArray = this.phone_behavior.split(",");
            if (strArray == null || strArray.length < 2) {
                return result;
            }
            return strArray[0];
        }
    }

    public void updatePhoneWhiteBehavior(String whiteBehavior) {
        if (this.phone_behavior == null || "all".equals(this.phone_behavior)) {
            this.phone_behavior = "both," + whiteBehavior;
            return;
        }
        String[] strArray = this.phone_behavior.split(",");
        if (strArray == null || strArray.length < 2) {
            this.phone_behavior = "both," + whiteBehavior;
            return;
        }
        strArray[1] = whiteBehavior;
        this.phone_behavior = strArray[0] + "," + strArray[1];
    }

    public String getPhoneWhiteBehavior() {
        String result = "both";
        if (this.phone_behavior == null) {
            this.phone_behavior = "all";
            return result;
        } else if ("all".equals(this.phone_behavior)) {
            return result;
        } else {
            String[] strArray = this.phone_behavior.split(",");
            if (strArray == null || strArray.length < 2) {
                return result;
            }
            return strArray[1];
        }
    }

    public void updatePhoneBalckSimslot(String blackSimslot) {
        if (this.phone_simslot == null || "all".equals(this.phone_simslot)) {
            this.phone_simslot = blackSimslot + ",both";
            return;
        }
        String[] strArray = this.phone_simslot.split(",");
        if (strArray == null || strArray.length < 2) {
            this.phone_simslot = blackSimslot + ",both";
            return;
        }
        strArray[0] = blackSimslot;
        this.phone_simslot = strArray[0] + "," + strArray[1];
    }

    public String getPhoneBalckSimslot() {
        String result = "both";
        if (this.phone_simslot == null) {
            this.phone_simslot = "all";
            return result;
        } else if ("all".equals(this.phone_simslot)) {
            return result;
        } else {
            String[] strArray = this.phone_simslot.split(",");
            if (strArray == null || strArray.length < 2) {
                return result;
            }
            return strArray[0];
        }
    }

    public void updatePhoneWhiteSimslot(String whiteSimslot) {
        if (this.phone_simslot == null || "all".equals(this.phone_simslot)) {
            this.phone_simslot = "both," + whiteSimslot;
            return;
        }
        String[] strArray = this.phone_simslot.split(",");
        if (strArray == null || strArray.length < 2) {
            this.phone_simslot = "both," + whiteSimslot;
            return;
        }
        strArray[1] = whiteSimslot;
        this.phone_simslot = strArray[0] + "," + strArray[1];
    }

    public String getPhoneWhiteSimslot() {
        String result = "both";
        if (this.phone_simslot == null) {
            this.phone_simslot = "all";
            return result;
        } else if ("all".equals(this.phone_simslot)) {
            return result;
        } else {
            String[] strArray = this.phone_simslot.split(",");
            if (strArray == null || strArray.length < 2) {
                return result;
            }
            return strArray[1];
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.number);
        dest.writeInt(this.autoRecord ? 1 : 0);
        if (this.phone_blackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.phone_whiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.phone_behavior);
        dest.writeString(this.phone_simslot);
        if (this.sms_blackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.sms_whiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.sms_behavior);
        dest.writeString(this.sms_simslot);
        dest.writeString(this.retain1);
        dest.writeString(this.retain2);
        dest.writeString(this.retain3);
        dest.writeString(this.retain4);
        if (this.retain5) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.retain6) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.retain7) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeString(this.custType);
    }
}
