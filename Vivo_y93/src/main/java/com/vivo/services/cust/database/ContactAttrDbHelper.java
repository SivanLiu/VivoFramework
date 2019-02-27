package com.vivo.services.cust.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.cust.data.ContactAttr;
import java.util.List;

public class ContactAttrDbHelper extends CustomDataBase<ContactAttr> {
    private final String TAG = "ContactAttrDbHelper";

    public ContactAttrDbHelper(Context context) {
        super(context);
    }

    public ContactAttr extractData(SQLiteDatabase sqlDB, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        ContactAttr contact = new ContactAttr(cursor.getString(cursor.getColumnIndex("NUMBER")));
        if (cursor.getInt(cursor.getColumnIndex("AUTORECORD")) == 1) {
            z = true;
        } else {
            z = false;
        }
        contact.autoRecord = z;
        if (cursor.getInt(cursor.getColumnIndex("PHONE_BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        contact.phone_blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("PHONE_WHITELIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        contact.phone_whiteList = z;
        contact.phone_behavior = cursor.getString(cursor.getColumnIndex("PHONE_BEHAVIOR"));
        contact.phone_simslot = cursor.getString(cursor.getColumnIndex("PHONE_SIMSLOT"));
        if (cursor.getInt(cursor.getColumnIndex("SMS_BLACKLIST")) == 1) {
            z = true;
        } else {
            z = false;
        }
        contact.sms_blackList = z;
        if (cursor.getInt(cursor.getColumnIndex("SMS_WHITELIST")) != 1) {
            z2 = false;
        }
        contact.sms_whiteList = z2;
        contact.sms_behavior = cursor.getString(cursor.getColumnIndex("SMS_BEHAVIOR"));
        contact.sms_simslot = cursor.getString(cursor.getColumnIndex("SMS_SIMSLOT"));
        contact.custType = cursor.getString(cursor.getColumnIndex("CUSTTYPE"));
        return contact;
    }

    public void save(ContactAttr contact) {
        if (contact != null && (TextUtils.isEmpty(contact.number) ^ 1) != 0) {
            Log.d("ContactAttrDbHelper", "save ContactAttr number:" + contact.number);
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("NUMBER", contact.number);
            localContentValues.put("AUTORECORD", Boolean.valueOf(contact.autoRecord));
            localContentValues.put("PHONE_BLACKLIST", Boolean.valueOf(contact.phone_blackList));
            localContentValues.put("PHONE_WHITELIST", Boolean.valueOf(contact.phone_whiteList));
            localContentValues.put("PHONE_BEHAVIOR", contact.phone_behavior);
            localContentValues.put("PHONE_SIMSLOT", contact.phone_simslot);
            localContentValues.put("SMS_BLACKLIST", Boolean.valueOf(contact.sms_blackList));
            localContentValues.put("SMS_WHITELIST", Boolean.valueOf(contact.sms_whiteList));
            localContentValues.put("SMS_BEHAVIOR", contact.sms_behavior);
            localContentValues.put("SMS_SIMSLOT", contact.sms_simslot);
            localContentValues.put("CUSTTYPE", contact.custType);
            localSQLiteDatabase.replace(VivoCustomDbHelper.TABLE_CONTACT, null, localContentValues);
        }
    }

    public void save(List<ContactAttr> list) {
        for (ContactAttr contact : list) {
            save(contact);
        }
    }

    public int delete(ContactAttr contact) {
        return -1;
    }

    public List<ContactAttr> getAllContacts() {
        return find("select * from CONTACT_RESTRICTION");
    }
}
