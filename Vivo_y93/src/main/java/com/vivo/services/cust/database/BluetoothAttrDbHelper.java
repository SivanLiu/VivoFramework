package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.BluetoothAttr;
import java.util.List;

public class BluetoothAttrDbHelper extends CustomDataBase<BluetoothAttr> {
    private final String TAG = "BluetoothAttrDbHelper";

    public BluetoothAttrDbHelper(Context context) {
        super(context);
    }

    public BluetoothAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        BluetoothAttr bluetooth = new BluetoothAttr(cursor.getString(cursor.getColumnIndex("MAC")));
        if (cursor.getInt(cursor.getColumnIndex("BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        bluetooth.blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("WHITELIST")) != 1) {
            z2 = false;
        }
        bluetooth.whiteList = z2;
        bluetooth.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return bluetooth;
    }

    public void save(BluetoothAttr bluetooth) {
        if (bluetooth != null && (TextUtils.isEmpty(bluetooth.mac) ^ 1) != 0) {
            Log.d("BluetoothAttrDbHelper", "save BluetoothAttr mac:" + bluetooth.mac);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("MAC", bluetooth.mac);
            localContentValues.put("BLACKLIST", Boolean.valueOf(bluetooth.blackList));
            localContentValues.put("WHITELIST", Boolean.valueOf(bluetooth.whiteList));
            localContentValues.put("CUSTTYPE", bluetooth.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_BLUETOOTH, null, localContentValues);
        }
    }

    public void save(List<BluetoothAttr> list) {
        for (BluetoothAttr bluetooth : list) {
            save(bluetooth);
        }
    }

    public int delete(BluetoothAttr bluetooth) {
        return -1;
    }

    public List<BluetoothAttr> getAllBluetooths() {
        return find("select * from BLUETOOTH_RESTRICTION");
    }
}
