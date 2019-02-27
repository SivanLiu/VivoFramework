package com.vivo.services.security.server.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.server.VivoPermissionService;
import java.util.List;

public class VivoPermissionDataBase extends ASecurityDataBase<VivoPermissionInfo> {
    public static final int GET_DENIED = 2;
    public static final int GET_GRANTED = 4;
    public static final int GET_MASK = 15;
    public static final int GET_UNKNOWN = 1;
    public static final int GET_WARNING = 8;

    public VivoPermissionDataBase(Context context) {
        super(context);
    }

    public VivoPermissionInfo extractData(SQLiteDatabase sqlDatabase, Cursor cursor) {
        boolean z;
        boolean z2 = true;
        VivoPermissionInfo vpi = new VivoPermissionInfo(cursor.getString(cursor.getColumnIndex(VivoSecurityDBHelper.ATTR_PACKAGE)));
        if (cursor.getInt(cursor.getColumnIndex(VivoSecurityDBHelper.ATTR_IS_WHITELIST)) == 1) {
            z = true;
        } else {
            z = false;
        }
        vpi.setWhiteListApp(z);
        if (cursor.getInt(cursor.getColumnIndex(VivoSecurityDBHelper.ATTR_IS_BLACKLIST)) == 1) {
            z = true;
        } else {
            z = false;
        }
        vpi.setBlackListApp(z);
        for (int index = 0; index < 30; index++) {
            vpi.setAllPermission(index, cursor.getInt(cursor.getColumnIndex(VivoPermissionType.getVPType(index).toString())));
        }
        for (int ftimeIndex = 0; ftimeIndex < 30; ftimeIndex++) {
            vpi.setAllPermissionBackup(ftimeIndex, cursor.getInt(cursor.getColumnIndex("FTime" + VivoPermissionType.getVPType(ftimeIndex).toString())));
        }
        if (cursor.getInt(cursor.getColumnIndex(VivoSecurityDBHelper.ATTR_IS_CONFIGURED)) != 1) {
            z2 = false;
        }
        vpi.setConfigured(z2);
        return vpi;
    }

    public void batchSave(List<VivoPermissionInfo> paramList) {
        if (!ASecurityDataBase.isNull(paramList)) {
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            localSQLiteDatabase.beginTransaction();
            try {
                for (VivoPermissionInfo localUser : paramList) {
                    save(localSQLiteDatabase, localUser);
                }
                localSQLiteDatabase.setTransactionSuccessful();
            } finally {
                localSQLiteDatabase.endTransaction();
            }
        }
    }

    public int delete(String pkg) {
        if (pkg == null || pkg.length() == 0) {
            return -1;
        }
        StringBuffer deleteSql = new StringBuffer();
        deleteSql.append(VivoSecurityDBHelper.ATTR_PACKAGE).append(" = '").append(pkg).append("'");
        return this.mDBHelper.getWritableDatabase().delete(VivoSecurityDBHelper.TABLE_PERMISSION, deleteSql.toString(), null);
    }

    public int delete(VivoPermissionInfo vpi) {
        if (vpi == null) {
            return -1;
        }
        return delete(vpi.getPackageName());
    }

    protected VivoPermissionInfo findById(SQLiteDatabase sd, String pkg) {
        StringBuffer findSql = new StringBuffer();
        findSql.append("select * from ");
        findSql.append(VivoSecurityDBHelper.TABLE_PERMISSION);
        findSql.append(" where ");
        findSql.append(VivoSecurityDBHelper.ATTR_PACKAGE).append(" = '").append(pkg).append("'");
        return (VivoPermissionInfo) query(sd, findSql.toString());
    }

    public VivoPermissionInfo findById(String pkg) {
        if (pkg == null || pkg.length() == 0) {
            return null;
        }
        return findById(this.mDBHelper.getWritableDatabase(), pkg);
    }

    public List<VivoPermissionInfo> findVPIsByWhiteList(boolean inWhiteList) {
        StringBuffer findSql = new StringBuffer();
        findSql.append("select * from ");
        findSql.append(VivoSecurityDBHelper.TABLE_PERMISSION);
        findSql.append(" where ");
        findSql.append(VivoSecurityDBHelper.ATTR_IS_WHITELIST).append(" = ").append(inWhiteList ? 1 : 0);
        return find(findSql.toString());
    }

    public List<VivoPermissionInfo> findVPIsByBlackList(boolean inBlackList) {
        StringBuffer findSql = new StringBuffer();
        findSql.append("select * from ");
        findSql.append(VivoSecurityDBHelper.TABLE_PERMISSION);
        findSql.append(" where ");
        findSql.append(VivoSecurityDBHelper.ATTR_IS_BLACKLIST).append(" = ").append(inBlackList ? 1 : 0);
        return find(findSql.toString());
    }

    public List<VivoPermissionInfo> findAllVPIs() {
        StringBuffer findSql = new StringBuffer();
        findSql.append("select * from ");
        findSql.append(VivoSecurityDBHelper.TABLE_PERMISSION);
        String sql = findSql.toString();
        VivoPermissionService.printfInfo("findAllVPIs:sql=" + sql);
        return find(sql);
    }

    protected void save(SQLiteDatabase paramSQLiteDatabase, VivoPermissionInfo vpi) {
        if (vpi != null) {
            ContentValues localContentValues = new ContentValues();
            localContentValues.put(VivoSecurityDBHelper.ATTR_PACKAGE, vpi.getPackageName());
            localContentValues.put(VivoSecurityDBHelper.ATTR_IS_WHITELIST, Boolean.valueOf(vpi.isWhiteListApp()));
            localContentValues.put(VivoSecurityDBHelper.ATTR_IS_BLACKLIST, Boolean.valueOf(vpi.isBlackListApp()));
            for (int index = 0; index < 30; index++) {
                localContentValues.put(VivoPermissionType.getVPType(index).toString(), Integer.valueOf(vpi.getAllPermission(index)));
            }
            for (int ftimeIndex = 0; ftimeIndex < 30; ftimeIndex++) {
                localContentValues.put("FTime" + VivoPermissionType.getVPType(ftimeIndex).toString(), Integer.valueOf(vpi.getAllPermissionBackup(ftimeIndex)));
            }
            localContentValues.put(VivoSecurityDBHelper.ATTR_IS_CONFIGURED, Boolean.valueOf(vpi.isConfigured()));
            paramSQLiteDatabase.replace(VivoSecurityDBHelper.TABLE_PERMISSION, null, localContentValues);
        }
    }

    public void save(VivoPermissionInfo vpi) {
        if (vpi != null) {
            save(this.mDBHelper.getWritableDatabase(), vpi);
        }
    }

    public void save(String packageName, int permTypeId, int result) {
        if (packageName != null && packageName.length() != 0) {
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put(VivoSecurityDBHelper.ATTR_PACKAGE, packageName);
            localContentValues.put(VivoPermissionType.getVPType(permTypeId).toString(), Integer.valueOf(result));
            localSQLiteDatabase.replace(VivoSecurityDBHelper.TABLE_PERMISSION, null, localContentValues);
        }
    }

    public void save(String packageName, boolean isWhiteList, boolean isBlackList) {
        if (packageName != null && packageName.length() != 0) {
            SQLiteDatabase localSQLiteDatabase = this.mDBHelper.getWritableDatabase();
            ContentValues localContentValues = new ContentValues();
            localContentValues.put(VivoSecurityDBHelper.ATTR_PACKAGE, packageName);
            localContentValues.put(VivoSecurityDBHelper.ATTR_IS_WHITELIST, Boolean.valueOf(isWhiteList));
            localContentValues.put(VivoSecurityDBHelper.ATTR_IS_BLACKLIST, Boolean.valueOf(isBlackList));
            localSQLiteDatabase.replace(VivoSecurityDBHelper.TABLE_PERMISSION, null, localContentValues);
        }
    }
}
