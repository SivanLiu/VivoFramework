package com.vivo.services.security.server.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.server.VivoPermissionConfig;
import com.vivo.services.security.server.VivoPermissionService;

public class VivoSecurityDBHelper extends SQLiteOpenHelper {
    public static final String ATTR_IS_BLACKLIST = "IS_BLACKLIST";
    public static final String ATTR_IS_CONFIGURED = "IS_CONFIGURED";
    public static final String ATTR_IS_WHITELIST = "IS_WHITELIST";
    public static final String ATTR_PACKAGE = "PACKAGE";
    private static final String DATABASE_NAME = "vivo_security.db";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_PERMISSION = "PERMISSION";
    private static final byte[] mLock = new byte[0];
    private static VivoSecurityDBHelper sDBHelper = null;

    public static int getDatabaseVersion() {
        return 4;
    }

    public VivoSecurityDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        VivoPermissionService.printfInfo("VSDB onCreate START");
        createTables(db);
        VivoPermissionService.printfInfo("VSDB onCreate END");
        VivoPermissionConfig.setDataBaseState(2);
    }

    private void createTables(SQLiteDatabase db) {
        VivoPermissionService.printfInfo("VSDB createTables 1");
        StringBuffer permissionSql = new StringBuffer();
        permissionSql.append("CREATE TABLE ");
        permissionSql.append(TABLE_PERMISSION).append(" (");
        permissionSql.append(ATTR_PACKAGE).append(" TEXT NOT NULL PRIMARY KEY,");
        permissionSql.append(ATTR_IS_WHITELIST).append(" BOOLEAN,");
        permissionSql.append(ATTR_IS_BLACKLIST).append(" BOOLEAN,");
        for (int index = 0; index < 30; index++) {
            permissionSql.append(VivoPermissionType.getVPType(index)).append(" INTEGER NOT NULL,");
        }
        for (int ftimeIndex = 0; ftimeIndex < 30; ftimeIndex++) {
            permissionSql.append("FTime").append(VivoPermissionType.getVPType(ftimeIndex)).append(" INTEGER NOT NULL,");
        }
        permissionSql.append(ATTR_IS_CONFIGURED).append(" BOOLEAN").append(");");
        VivoPermissionService.printfInfo("VSDB" + permissionSql.toString());
        VivoPermissionService.printfInfo("VSDB createTables 2");
        db.execSQL(permissionSql.toString());
        VivoPermissionService.printfInfo("VSDB createTables 3");
    }

    private void clearTables(SQLiteDatabase db) {
        VivoPermissionService.printfInfo("VSDB clearTables START");
        db.execSQL("DROP TABLE PERMISSION");
        VivoPermissionService.printfInfo("VSDB clearTables END");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int j;
        VivoPermissionService.printfInfo("VSDB onUpgrade: start currentVersion : " + db.getVersion() + " oldVersion=" + oldVersion + ";newVersion=" + newVersion);
        long startWhen = System.nanoTime();
        if (1 == oldVersion && 2 == newVersion) {
            clearTables(db);
            createTables(db);
            oldVersion = 2;
        }
        if (2 == oldVersion && 3 == newVersion) {
            VivoPermissionService.printfInfo("VSDB onUpgrade23:1");
            for (int i = 24; i < 30; i++) {
                String columns_add1 = "ALTER TABLE PERMISSION ADD " + VivoPermissionType.getVPType(i) + " INTEGER NOT NULL" + " DEFAULT -2";
                VivoPermissionService.printfInfo("VSDB onUpgrade columns_add1 =" + columns_add1);
                db.execSQL(columns_add1);
            }
            VivoPermissionService.printfInfo("VSDB onUpgrade23:2");
            for (j = 0; j < 30; j++) {
                String columns_add2 = "ALTER TABLE PERMISSION ADD FTime" + VivoPermissionType.getVPType(j) + " INTEGER NOT NULL" + " DEFAULT 3";
                VivoPermissionService.printfInfo("VSDB onUpgrade23 columns_add2 =" + columns_add2);
                db.execSQL(columns_add2);
            }
            oldVersion = 3;
            VivoPermissionService.printfInfo("VSDB onUpgrade23:3");
        }
        if (2 == oldVersion && 4 == newVersion) {
            VivoPermissionService.printfInfo("VSDB onUpgrade24:1");
            clearTables(db);
            VivoPermissionService.printfInfo("VSDB onUpgrade24:2");
            createTables(db);
            VivoPermissionService.printfInfo("VSDB onUpgrade24:3");
            oldVersion = 4;
        }
        if (3 == oldVersion && 4 == newVersion) {
            String columns_rename;
            for (j = 0; j < 30; j++) {
                columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(j) + "=3" + " WHERE " + VivoPermissionType.getVPType(j) + "=1";
                VivoPermissionService.printfInfo("VSDB onUpgrade341-1 j = " + j + " columns_rename =" + columns_rename);
                db.execSQL(columns_rename);
                columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(j) + "=2" + " WHERE " + VivoPermissionType.getVPType(j) + "=-1";
                VivoPermissionService.printfInfo("VSDB onUpgrade341-2 j = " + j + " columns_rename =" + columns_rename);
                db.execSQL(columns_rename);
                columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(j) + "=1" + " WHERE " + VivoPermissionType.getVPType(j) + "=0";
                VivoPermissionService.printfInfo("VSDB onUpgrade341-3 j = " + j + " columns_rename =" + columns_rename);
                db.execSQL(columns_rename);
                columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(j) + "=0" + " WHERE " + VivoPermissionType.getVPType(j) + "=-2";
                VivoPermissionService.printfInfo("VSDB onUpgrade341-4 j = " + j + " columns_rename =" + columns_rename);
                db.execSQL(columns_rename);
            }
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(4) + "=2" + " WHERE " + VivoPermissionType.getVPType(6) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade342-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(6) + "=2" + " WHERE " + VivoPermissionType.getVPType(4) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade342-2  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(4) + "=3" + " WHERE " + VivoPermissionType.getVPType(6) + "=3";
            VivoPermissionService.printfInfo("VSDB onUpgrade342-3  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(5) + "=2" + " WHERE " + VivoPermissionType.getVPType(7) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade343-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(7) + "=2" + " WHERE " + VivoPermissionType.getVPType(5) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade343-2  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(5) + "=3" + " WHERE " + VivoPermissionType.getVPType(7) + "=3";
            VivoPermissionService.printfInfo("VSDB onUpgrade343-3  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(14) + "=2" + " WHERE " + VivoPermissionType.getVPType(15) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade344-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(15) + "=2" + " WHERE " + VivoPermissionType.getVPType(14) + "=2";
            VivoPermissionService.printfInfo("VSDB onUpgrade344-2  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(14) + "=3" + " WHERE " + VivoPermissionType.getVPType(15) + "=3";
            VivoPermissionService.printfInfo("VSDB onUpgrade344-3  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(6) + "=0";
            VivoPermissionService.printfInfo("VSDB onUpgrade345-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(7) + "=0";
            VivoPermissionService.printfInfo("VSDB onUpgrade345-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(15) + "=0";
            VivoPermissionService.printfInfo("VSDB onUpgrade345-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
            for (int k = 0; k < 30; k++) {
                columns_rename = "UPDATE PERMISSION SET FTime" + VivoPermissionType.getVPType(k) + "=" + VivoPermissionType.getVPType(k);
                VivoPermissionService.printfInfo("VSDB onUpgrade346-1 k = " + k + " columns_rename =" + columns_rename);
                db.execSQL(columns_rename);
            }
            columns_rename = "UPDATE PERMISSION SET " + VivoPermissionType.getVPType(15) + "=0";
            VivoPermissionService.printfInfo("VSDB onUpgrade347-1  columns_rename =" + columns_rename);
            db.execSQL(columns_rename);
        }
        VivoPermissionService.printfInfo("VSDB onUpgrade: end " + ((System.nanoTime() - startWhen) / 1000000) + "ms");
        VivoPermissionConfig.setDataBaseState(3);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        VivoPermissionService.printfInfo("VSDB onDowngrade:oldVersion=" + oldVersion + ";newVersion=" + newVersion);
        if (oldVersion > newVersion) {
            clearTables(db);
            createTables(db);
        }
    }

    public static VivoSecurityDBHelper getInstance(Context paramContext) {
        if (sDBHelper == null) {
            synchronized (mLock) {
                if (sDBHelper == null) {
                    sDBHelper = new VivoSecurityDBHelper(paramContext);
                }
            }
        }
        return sDBHelper;
    }
}
