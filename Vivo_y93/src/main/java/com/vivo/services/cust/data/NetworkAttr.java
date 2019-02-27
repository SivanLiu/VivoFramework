package com.vivo.services.cust.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkAttr implements Parcelable {
    public static final String ATTR_BLACKLIST = "BLACKLIST";
    public static final String ATTR_CUSTTYPE = "CUSTTYPE";
    public static final String ATTR_IP = "IP";
    public static final String ATTR_RETAIN1 = "RETAIN1";
    public static final String ATTR_RETAIN2 = "RETAIN2";
    public static final String ATTR_RETAIN3 = "RETAIN3";
    public static final String ATTR_RETAIN4 = "RETAIN4";
    public static final String ATTR_RETAIN5 = "RETAIN5";
    public static final String ATTR_RETAIN6 = "RETAIN6";
    public static final String ATTR_RETAIN7 = "RETAIN7";
    public static final String ATTR_WHITELIST = "WHITELIST";
    public static final Creator<NetworkAttr> CREATOR = new Creator<NetworkAttr>() {
        public NetworkAttr createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            NetworkAttr na = new NetworkAttr(in.readString());
            na.blackList = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            na.whiteList = z;
            na.retain1 = in.readString();
            na.retain2 = in.readString();
            na.retain3 = in.readString();
            na.retain4 = in.readString();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            na.retain5 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            na.retain6 = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            na.retain7 = z2;
            na.custType = in.readString();
            return na;
        }

        public NetworkAttr[] newArray(int size) {
            return new NetworkAttr[size];
        }
    };
    public boolean blackList;
    public String custType;
    public String ip;
    public String retain1;
    public String retain2;
    public String retain3;
    public String retain4;
    public boolean retain5;
    public boolean retain6;
    public boolean retain7;
    public boolean whiteList;

    public NetworkAttr(String ip) {
        this.ip = ip;
        this.blackList = false;
        this.whiteList = false;
        this.custType = "0";
    }

    public NetworkAttr(String ip, boolean bList, boolean wList, String r1, String r2, String r3, String r4, boolean r5, boolean r6, boolean r7, String ctype) {
        this.ip = ip;
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
        dest.writeString(this.ip);
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
