package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.WlanAttr;
import java.util.List;

public class WlanAttrDbHelper extends CustomDataBase<WlanAttr> {
    private final String TAG = "WlanAttrDbHelper";

    public WlanAttrDbHelper(Context context) {
        super(context);
    }

    public WlanAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        WlanAttr wlan = new WlanAttr(cursor.getString(cursor.getColumnIndex("ICCID")));
        if (cursor.getInt(cursor.getColumnIndex("BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        wlan.blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("WHITELIST")) != 1) {
            z2 = false;
        }
        wlan.whiteList = z2;
        wlan.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return wlan;
    }

    public void save(WlanAttr wlan) {
        if (wlan != null && (TextUtils.isEmpty(wlan.iccid) ^ 1) != 0) {
            Log.d("WlanAttrDbHelper", "save WlanAttr iccid:" + wlan.iccid);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("ICCID", wlan.iccid);
            localContentValues.put("BLACKLIST", Boolean.valueOf(wlan.blackList));
            localContentValues.put("WHITELIST", Boolean.valueOf(wlan.whiteList));
            localContentValues.put("CUSTTYPE", wlan.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_WLAN, null, localContentValues);
        }
    }

    public void save(List<WlanAttr> list) {
        for (WlanAttr wlan : list) {
            save(wlan);
        }
    }

    public int delete(WlanAttr wlan) {
        return -1;
    }

    public List<WlanAttr> getAllWlans() {
        return find("select * from WLAN_RESTRICTION");
    }
}
