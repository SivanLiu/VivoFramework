package com.vivo.services.cust.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VivoCustomDbHelper extends SQLiteOpenHelper {
    public static final String ATTR_CUSTOM_TYPE = "C_TYPE";
    private static final int DATABASE_VERSION = 3;
    private static final String DB_NAME = "vivo_custom.db";
    public static final String TABLE_BLUETOOTH = "BLUETOOTH_RESTRICTION";
    public static final String TABLE_BROWSER = "BROWSER_RESTRICTION";
    public static final String TABLE_CONTACT = "CONTACT_RESTRICTION";
    public static final String TABLE_NETWORK = "NETWORK_RESTRICTION";
    public static final String TABLE_PACKAGE = "PACKAGE_RESTRICTION";
    public static final String TABLE_WLAN = "WLAN_RESTRICTION";
    private static final String TAG = "VCDB";
    private static byte[] mLock = new byte[0];
    private static VivoCustomDbHelper vcDbHelper = null;

    public static VivoCustomDbHelper getInstance(Context paramContext) {
        if (vcDbHelper == null) {
            synchronized (mLock) {
                if (vcDbHelper == null) {
                    vcDbHelper = new VivoCustomDbHelper(paramContext);
                }
            }
        }
        Log.d(TAG, "--->VivoCustomDbHelper");
        return vcDbHelper;
    }

    public VivoCustomDbHelper(Context context) {
        super(context, DB_NAME, null, 3);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--->createTables");
        createTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StringBuffer stringBuffer;
        Log.d(TAG, "--->upgrading vcs database from version " + oldVersion + " to " + newVersion);
        int upgradeVersion = oldVersion;
        if (oldVersion == 1) {
            stringBuffer = new StringBuffer();
            stringBuffer = new StringBuffer();
            stringBuffer.append("CREATE TABLE IF NOT EXISTS ");
            stringBuffer.append(TABLE_WLAN).append(" (");
            stringBuffer.append("ICCID").append(" TEXT NOT NULL PRIMARY KEY,");
            stringBuffer.append("BLACKLIST").append(" BOOLEAN,");
            stringBuffer.append("WHITELIST").append(" BOOLEAN,");
            stringBuffer.append("RETAIN1").append(" TEXT,");
            stringBuffer.append("RETAIN2").append(" TEXT,");
            stringBuffer.append("RETAIN3").append(" TEXT,");
            stringBuffer.append("RETAIN4").append(" TEXT,");
            stringBuffer.append("RETAIN5").append(" BOOLEAN,");
            stringBuffer.append("RETAIN6").append(" BOOLEAN,");
            stringBuffer.append("RETAIN7").append(" BOOLEAN,");
            stringBuffer.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
            db.execSQL(stringBuffer.toString());
            Log.d(TAG, "--->upgrading vcs database from version " + oldVersion + " to " + newVersion + " create TABLE WLAN!");
            upgradeVersion = 2;
        }
        if (upgradeVersion == 2) {
            stringBuffer = new StringBuffer();
            stringBuffer = new StringBuffer();
            stringBuffer.append("CREATE TABLE IF NOT EXISTS ");
            stringBuffer.append(TABLE_BLUETOOTH).append(" (");
            stringBuffer.append("MAC").append(" TEXT NOT NULL PRIMARY KEY,");
            stringBuffer.append("BLACKLIST").append(" BOOLEAN,");
            stringBuffer.append("WHITELIST").append(" BOOLEAN,");
            stringBuffer.append("RETAIN1").append(" TEXT,");
            stringBuffer.append("RETAIN2").append(" TEXT,");
            stringBuffer.append("RETAIN3").append(" TEXT,");
            stringBuffer.append("RETAIN4").append(" TEXT,");
            stringBuffer.append("RETAIN5").append(" BOOLEAN,");
            stringBuffer.append("RETAIN6").append(" BOOLEAN,");
            stringBuffer.append("RETAIN7").append(" BOOLEAN,");
            stringBuffer.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
            db.execSQL(stringBuffer.toString());
            Log.d(TAG, "--->upgrading vcs database from version " + oldVersion + " to " + newVersion + " create TABLE WLAN!");
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTables(SQLiteDatabase db) {
        StringBuffer packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_PACKAGE).append(" (");
        packageSql.append("PACKAGE_NAME").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("PERSISTENT").append(" BOOLEAN,");
        packageSql.append("FORBIDRUN").append(" BOOLEAN,");
        packageSql.append("UNINSTALLBL").append(" BOOLEAN,");
        packageSql.append("UNINSTALLWL").append(" BOOLEAN,");
        packageSql.append("INSTALLBL").append(" BOOLEAN,");
        packageSql.append("INSTALLWL").append(" BOOLEAN,");
        packageSql.append("DATA_NETWORK_BL").append(" BOOLEAN,");
        packageSql.append("DATA_NETWORK_WL").append(" BOOLEAN,");
        packageSql.append("WIFI_NETWORK_BL").append(" BOOLEAN,");
        packageSql.append("WIFI_NETWORK_WL").append(" BOOLEAN,");
        packageSql.append("RETAIN1").append(" BOOLEAN,");
        packageSql.append("RETAIN2").append(" BOOLEAN,");
        packageSql.append("RETAIN3").append(" BOOLEAN,");
        packageSql.append("RETAIN4").append(" BOOLEAN,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_CONTACT).append(" (");
        packageSql.append("NUMBER").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("AUTORECORD").append(" BOOLEAN,");
        packageSql.append("PHONE_BLACKLIST").append(" BOOLEAN,");
        packageSql.append("PHONE_WHITELIST").append(" BOOLEAN,");
        packageSql.append("PHONE_BEHAVIOR").append(" TEXT,");
        packageSql.append("PHONE_SIMSLOT").append(" TEXT,");
        packageSql.append("SMS_BLACKLIST").append(" BOOLEAN,");
        packageSql.append("SMS_WHITELIST").append(" BOOLEAN,");
        packageSql.append("SMS_BEHAVIOR").append(" TEXT,");
        packageSql.append("SMS_SIMSLOT").append(" TEXT,");
        packageSql.append("RETAIN1").append(" TEXT,");
        packageSql.append("RETAIN2").append(" TEXT,");
        packageSql.append("RETAIN3").append(" TEXT,");
        packageSql.append("RETAIN4").append(" TEXT,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_BROWSER).append(" (");
        packageSql.append("URL").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("BLACKLIST").append(" BOOLEAN,");
        packageSql.append("WHITELIST").append(" BOOLEAN,");
        packageSql.append("RETAIN1").append(" TEXT,");
        packageSql.append("RETAIN2").append(" TEXT,");
        packageSql.append("RETAIN3").append(" TEXT,");
        packageSql.append("RETAIN4").append(" TEXT,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_NETWORK).append(" (");
        packageSql.append("IP").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("BLACKLIST").append(" BOOLEAN,");
        packageSql.append("WHITELIST").append(" BOOLEAN,");
        packageSql.append("RETAIN1").append(" TEXT,");
        packageSql.append("RETAIN2").append(" TEXT,");
        packageSql.append("RETAIN3").append(" TEXT,");
        packageSql.append("RETAIN4").append(" TEXT,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_WLAN).append(" (");
        packageSql.append("ICCID").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("BLACKLIST").append(" BOOLEAN,");
        packageSql.append("WHITELIST").append(" BOOLEAN,");
        packageSql.append("RETAIN1").append(" TEXT,");
        packageSql.append("RETAIN2").append(" TEXT,");
        packageSql.append("RETAIN3").append(" TEXT,");
        packageSql.append("RETAIN4").append(" TEXT,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        packageSql = new StringBuffer();
        packageSql.append("CREATE TABLE ");
        packageSql.append(TABLE_BLUETOOTH).append(" (");
        packageSql.append("MAC").append(" TEXT NOT NULL PRIMARY KEY,");
        packageSql.append("BLACKLIST").append(" BOOLEAN,");
        packageSql.append("WHITELIST").append(" BOOLEAN,");
        packageSql.append("RETAIN1").append(" TEXT,");
        packageSql.append("RETAIN2").append(" TEXT,");
        packageSql.append("RETAIN3").append(" TEXT,");
        packageSql.append("RETAIN4").append(" TEXT,");
        packageSql.append("RETAIN5").append(" BOOLEAN,");
        packageSql.append("RETAIN6").append(" BOOLEAN,");
        packageSql.append("RETAIN7").append(" BOOLEAN,");
        packageSql.append("CUSTTYPE").append(" TEXT NOT NULL").append(");");
        db.execSQL(packageSql.toString());
        Log.d(TAG, "<----createTables");
    }

    public static int getDBVersion() {
        return 3;
    }
}
