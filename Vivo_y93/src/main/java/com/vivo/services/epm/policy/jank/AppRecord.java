package com.vivo.services.epm.policy.jank;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppRecord {
    public String label;
    public String pkgName;
    public long startTime = System.currentTimeMillis();
    public int uid;
    public int versionCode;
    public String versionName;

    public AppRecord(int uid, String pkgName) {
        this.uid = uid;
        this.pkgName = pkgName;
    }

    public void init(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(this.pkgName, 0);
            this.versionName = packageInfo.versionName;
            this.versionCode = packageInfo.versionCode;
            this.label = packageInfo.applicationInfo.loadLabel(pm).toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "AppRecord{uid=" + this.uid + ", pkgName='" + this.pkgName + '\'' + ", versionName='" + this.versionName + '\'' + ", versionCode=" + this.versionCode + ", label='" + this.label + '\'' + ", startTime=" + this.startTime + '}';
    }
}
