package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.NetworkAttr;
import java.util.List;

public class NetworkAttrDbHelper extends CustomDataBase<NetworkAttr> {
    private final String TAG = "NetworkAttrDbHelper";

    public NetworkAttrDbHelper(Context context) {
        super(context);
    }

    public NetworkAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        NetworkAttr network = new NetworkAttr(cursor.getString(cursor.getColumnIndex("IP")));
        if (cursor.getInt(cursor.getColumnIndex("BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        network.blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("WHITELIST")) != 1) {
            z2 = false;
        }
        network.whiteList = z2;
        network.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return network;
    }

    public void save(NetworkAttr network) {
        if (network != null && (TextUtils.isEmpty(network.ip) ^ 1) != 0) {
            Log.d("NetworkAttrDbHelper", "save NetworkAttr ip:" + network.ip);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("IP", network.ip);
            localContentValues.put("BLACKLIST", Boolean.valueOf(network.blackList));
            localContentValues.put("WHITELIST", Boolean.valueOf(network.whiteList));
            localContentValues.put("CUSTTYPE", network.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_NETWORK, null, localContentValues);
        }
    }

    public void save(List<NetworkAttr> list) {
        for (NetworkAttr network : list) {
            save(network);
        }
    }

    public int delete(NetworkAttr network) {
        return -1;
    }

    public List<NetworkAttr> getAllNetworks() {
        return find("select * from NETWORK_RESTRICTION");
    }
}
