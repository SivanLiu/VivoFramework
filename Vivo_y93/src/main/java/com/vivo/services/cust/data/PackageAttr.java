package com.vivo.services.cust.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PackageAttr implements Parcelable {
    public static final String ATTR_CUSTTYPE = "CUSTTYPE";
    public static final String ATTR_DATA_NETWORK_BL = "DATA_NETWORK_BL";
    public static final String ATTR_DATA_NETWORK_WL = "DATA_NETWORK_WL";
    public static final String ATTR_FORBIDRUN = "FORBIDRUN";
    public static final String ATTR_INSTALLBL = "INSTALLBL";
    public static final String ATTR_INSTALLWL = "INSTALLWL";
    public static final String ATTR_PACKAGE = "PACKAGE_NAME";
    public static final String ATTR_PERSIST = "PERSISTENT";
    public static final String ATTR_RETAIN1 = "RETAIN1";
    public static final String ATTR_RETAIN2 = "RETAIN2";
    public static final String ATTR_RETAIN3 = "RETAIN3";
    public static final String ATTR_RETAIN4 = "RETAIN4";
    public static final String ATTR_RETAIN5 = "RETAIN5";
    public static final String ATTR_RETAIN6 = "RETAIN6";
    public static final String ATTR_RETAIN7 = "RETAIN7";
    public static final String ATTR_UNINSTALLBL = "UNINSTALLBL";
    public static final String ATTR_UNINSTALLWL = "UNINSTALLWL";
    public static final String ATTR_WIFI_NETWORK_BL = "WIFI_NETWORK_BL";
    public static final String ATTR_WIFI_NETWORK_WL = "WIFI_NETWORK_WL";
    public static final Creator<PackageAttr> CREATOR = new Creator<PackageAttr>() {
        public PackageAttr createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            PackageAttr pka = new PackageAttr(in.readString());
            pka.persistent = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.forbidRun = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.uninstallBlackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.uninstallWhiteList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.installBlackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.installWhiteList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.dataNetworkBlackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.dataNetworkWhiteList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.wifiNetworkBlackList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.wifiNetworkWhiteList = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain1 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain2 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain3 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain4 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain5 = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            pka.retain6 = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            pka.retain7 = z2;
            pka.custType = in.readString();
            return pka;
        }

        public PackageAttr[] newArray(int size) {
            return new PackageAttr[size];
        }
    };
    public static final int CUSTTYPE_BACKUP_NOTIFICATION = 1;
    public static final int CUSTTYPE_DEFAULT = 0;
    public String custType;
    public boolean dataNetworkBlackList;
    public boolean dataNetworkWhiteList;
    public boolean forbidRun;
    public boolean installBlackList;
    public boolean installWhiteList;
    public String packageName;
    public boolean persistent;
    public boolean retain1;
    public boolean retain2;
    public boolean retain3;
    public boolean retain4;
    public boolean retain5;
    public boolean retain6;
    public boolean retain7;
    public boolean uninstallBlackList;
    public boolean uninstallWhiteList;
    public boolean wifiNetworkBlackList;
    public boolean wifiNetworkWhiteList;

    public PackageAttr(String pkgName) {
        this.custType = Integer.toString(0);
        this.packageName = pkgName;
        this.persistent = false;
        this.forbidRun = false;
        this.uninstallBlackList = false;
        this.uninstallWhiteList = false;
        this.installBlackList = false;
        this.installWhiteList = false;
        this.dataNetworkBlackList = false;
        this.dataNetworkWhiteList = false;
        this.wifiNetworkBlackList = false;
        this.wifiNetworkWhiteList = false;
        this.retain1 = false;
        this.retain2 = false;
        this.retain3 = false;
        this.retain4 = false;
        this.retain5 = false;
        this.retain6 = false;
        this.retain7 = false;
    }

    public PackageAttr(String pkgName, boolean persist, boolean forbit, boolean uninstallBl, boolean uninstallWl, boolean installBl, boolean installWl, boolean data_networkBl, boolean data_networkWl, boolean wifi_networkBl, boolean wifi_networkWl, boolean r1, boolean r2, boolean r3, boolean r4, boolean r5, boolean r6, boolean r7, String cust_type) {
        this.custType = cust_type;
        this.packageName = pkgName;
        this.persistent = persist;
        this.forbidRun = forbit;
        this.uninstallBlackList = uninstallBl;
        this.uninstallWhiteList = uninstallWl;
        this.installBlackList = installBl;
        this.installWhiteList = installWl;
        this.dataNetworkBlackList = data_networkBl;
        this.dataNetworkWhiteList = data_networkWl;
        this.wifiNetworkBlackList = wifi_networkBl;
        this.wifiNetworkWhiteList = wifi_networkWl;
        this.retain1 = r1;
        this.retain2 = r2;
        this.retain3 = r3;
        this.retain4 = r4;
        this.retain5 = r5;
        this.retain6 = r6;
        this.retain7 = r7;
        this.custType = cust_type;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.packageName);
        dest.writeInt(this.persistent ? 1 : 0);
        if (this.forbidRun) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.uninstallBlackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.uninstallWhiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.installBlackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.installWhiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.dataNetworkBlackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.dataNetworkWhiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.wifiNetworkBlackList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.wifiNetworkWhiteList) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.retain1) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.retain2) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.retain3) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.retain4) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
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
