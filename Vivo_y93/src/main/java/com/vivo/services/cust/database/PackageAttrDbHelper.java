package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.PackageAttr;
import java.util.List;

public class PackageAttrDbHelper extends CustomDataBase<PackageAttr> {
    private final String TAG = "PackageAttrDbHelper";

    public PackageAttrDbHelper(Context context) {
        super(context);
    }

    public PackageAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        PackageAttr pa = new PackageAttr(cursor.getString(cursor.getColumnIndex("PACKAGE_NAME")));
        if (cursor.getInt(cursor.getColumnIndex("PERSISTENT")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.persistent = z;
        if (cursor.getInt(cursor.getColumnIndex("FORBIDRUN")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.forbidRun = z;
        if (cursor.getInt(cursor.getColumnIndex("UNINSTALLBL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.uninstallBlackList = z;
        if (cursor.getInt(cursor.getColumnIndex("UNINSTALLWL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.uninstallWhiteList = z;
        if (cursor.getInt(cursor.getColumnIndex("INSTALLBL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.installBlackList = z;
        if (cursor.getInt(cursor.getColumnIndex("INSTALLWL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.installWhiteList = z;
        if (cursor.getInt(cursor.getColumnIndex("DATA_NETWORK_BL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.dataNetworkBlackList = z;
        if (cursor.getInt(cursor.getColumnIndex("DATA_NETWORK_WL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.dataNetworkWhiteList = z;
        if (cursor.getInt(cursor.getColumnIndex("WIFI_NETWORK_BL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.wifiNetworkBlackList = z;
        if (cursor.getInt(cursor.getColumnIndex("WIFI_NETWORK_WL")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.wifiNetworkWhiteList = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN1")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain1 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN2")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain2 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN3")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain3 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN4")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain4 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN5")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain5 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN6")) == 1) {
            z = true;
        } else {
            z = false;
        }
        pa.retain6 = z;
        if (cursor.getInt(cursor.getColumnIndex("RETAIN7")) != 1) {
            z2 = false;
        }
        pa.retain7 = z2;
        pa.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return pa;
    }

    public void save(String pkgName) {
        Log.d("PackageAttrDbHelper", "save packageName:" + pkgName);
    }

    public void save(String pkgName, String custType) {
    }

    public void save(PackageAttr pkg) {
        if (pkg != null && (TextUtils.isEmpty(pkg.packageName) ^ 1) != 0) {
            Log.d("PackageAttrDbHelper", "save PackageAttr packageName:" + pkg.packageName);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("PACKAGE_NAME", pkg.packageName);
            localContentValues.put("PERSISTENT", Boolean.valueOf(pkg.persistent));
            localContentValues.put("FORBIDRUN", Boolean.valueOf(pkg.forbidRun));
            localContentValues.put("UNINSTALLBL", Boolean.valueOf(pkg.uninstallBlackList));
            localContentValues.put("UNINSTALLWL", Boolean.valueOf(pkg.uninstallWhiteList));
            localContentValues.put("INSTALLBL", Boolean.valueOf(pkg.installBlackList));
            localContentValues.put("INSTALLWL", Boolean.valueOf(pkg.installWhiteList));
            localContentValues.put("DATA_NETWORK_BL", Boolean.valueOf(pkg.dataNetworkBlackList));
            localContentValues.put("DATA_NETWORK_WL", Boolean.valueOf(pkg.dataNetworkWhiteList));
            localContentValues.put("WIFI_NETWORK_BL", Boolean.valueOf(pkg.wifiNetworkBlackList));
            localContentValues.put("WIFI_NETWORK_WL", Boolean.valueOf(pkg.wifiNetworkWhiteList));
            localContentValues.put("RETAIN1", Boolean.valueOf(pkg.retain1));
            localContentValues.put("RETAIN2", Boolean.valueOf(pkg.retain2));
            localContentValues.put("RETAIN3", Boolean.valueOf(pkg.retain3));
            localContentValues.put("RETAIN4", Boolean.valueOf(pkg.retain4));
            localContentValues.put("RETAIN5", Boolean.valueOf(pkg.retain5));
            localContentValues.put("RETAIN6", Boolean.valueOf(pkg.retain6));
            localContentValues.put("RETAIN7", Boolean.valueOf(pkg.retain7));
            localContentValues.put("CUSTTYPE", pkg.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_PACKAGE, null, localContentValues);
        }
    }

    public void save(List<PackageAttr> list) {
        for (PackageAttr pa : list) {
            save(pa);
        }
    }

    public int delete(String pkgName) {
        return this.mDBHelper.getWritableDatabase().delete(VivoCustomDbHelper.TABLE_PACKAGE, "PACKAGE_NAME=?", new String[]{pkgName});
    }

    public int delete(PackageAttr pkg) {
        return delete(pkg.packageName);
    }

    public List<PackageAttr> getAllPackages() {
        return find("select * from PACKAGE_RESTRICTION");
    }
}
