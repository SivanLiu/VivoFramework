package com.vivo.services.cust.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class BrowserAttr implements Parcelable {
    public static final String ATTR_BLACKLIST = "BLACKLIST";
    public static final String ATTR_CUSTTYPE = "CUSTTYPE";
    public static final String ATTR_RETAIN1 = "RETAIN1";
    public static final String ATTR_RETAIN2 = "RETAIN2";
    public static final String ATTR_RETAIN3 = "RETAIN3";
    public static final String ATTR_RETAIN4 = "RETAIN4";
    public static final String ATTR_RETAIN5 = "RETAIN5";
    public static final String ATTR_RETAIN6 = "RETAIN6";
    public static final String ATTR_RETAIN7 = "RETAIN7";
    public static final String ATTR_URL = "URL";
    public static final String ATTR_WHITELIST = "WHITELIST";
    public static final Creator<BrowserAttr> CREATOR = new Creator<BrowserAttr>() {
        public BrowserAttr createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            BrowserAttr ba = new BrowserAttr(in.readString());
            ba.blackList = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            ba.whiteList = z;
            ba.retain1 = in.readString();
            ba.retain2 = in.readString();
            ba.retain3 = in.readString();
            ba.retain4 = in.readString();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            ba.retain5 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            ba.retain6 = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            ba.retain7 = z2;
            ba.custType = in.readString();
            return ba;
        }

        public BrowserAttr[] newArray(int size) {
            return new BrowserAttr[size];
        }
    };
    public boolean blackList;
    public String custType;
    public String retain1;
    public String retain2;
    public String retain3;
    public String retain4;
    public boolean retain5;
    public boolean retain6;
    public boolean retain7;
    public String url;
    public boolean whiteList;

    public BrowserAttr(String url) {
        this.url = url;
        this.blackList = false;
        this.whiteList = false;
        this.custType = "0";
    }

    public BrowserAttr(String url, boolean bList, boolean wList, String r1, String r2, String r3, String r4, boolean r5, boolean r6, boolean r7, String ctype) {
        this.url = url;
        this.blackList = bList;
        this.whiteList = wList;
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

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.url);
        dest.writeInt(this.blackList ? 1 : 0);
        if (this.whiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
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
