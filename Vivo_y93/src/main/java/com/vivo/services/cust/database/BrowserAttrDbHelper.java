package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.BrowserAttr;
import java.util.List;

public class BrowserAttrDbHelper extends CustomDataBase<BrowserAttr> {
    private final String TAG = "BrowserAttrDbHelper";

    public BrowserAttrDbHelper(Context context) {
        super(context);
    }

    public BrowserAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        BrowserAttr browser = new BrowserAttr(cursor.getString(cursor.getColumnIndex("URL")));
        if (cursor.getInt(cursor.getColumnIndex("BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        browser.blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("WHITELIST")) != 1) {
            z2 = false;
        }
        browser.whiteList = z2;
        browser.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return browser;
    }

    public void save(BrowserAttr browser) {
        if (browser != null && (TextUtils.isEmpty(browser.url) ^ 1) != 0) {
            Log.d("BrowserAttrDbHelper", "save BrowserAttr url:" + browser.url);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("URL", browser.url);
            localContentValues.put("BLACKLIST", Boolean.valueOf(browser.blackList));
            localContentValues.put("WHITELIST", Boolean.valueOf(browser.whiteList));
            localContentValues.put("CUSTTYPE", browser.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_BROWSER, null, localContentValues);
        }
    }

    public void save(List<BrowserAttr> list) {
        for (BrowserAttr browser : list) {
            save(browser);
        }
    }

    public int delete(BrowserAttr browser) {
        return -1;
    }

    public List<BrowserAttr> getAllBrowsers() {
        return find("select * from BROWSER_RESTRICTION");
    }
}
