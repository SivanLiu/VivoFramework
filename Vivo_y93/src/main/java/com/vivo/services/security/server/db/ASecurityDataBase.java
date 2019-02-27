package com.vivo.services.security.server.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public abstract class ASecurityDataBase<T> implements ICursorDataExtractor<T> {
    protected Context mContext;
    protected VivoSecurityDBHelper mDBHelper;

    public ASecurityDataBase(Context context) {
        if (context == null) {
            throw new NullPointerException("context should not be null");
        }
        this.mContext = context;
        this.mDBHelper = VivoSecurityDBHelper.getInstance(context);
    }

    protected static boolean isNull(Object object) {
        if (object == null) {
            return true;
        }
        return (object instanceof List) && ((List) object).size() == 0;
    }

    protected List<T> find(SQLiteDatabase sd, String sql) {
        List<T> localArrayList = null;
        Cursor localCursor = sd.rawQuery(sql, null);
        if (localCursor != null) {
            if (localCursor.moveToFirst()) {
                localArrayList = new ArrayList();
                do {
                    localArrayList.add(extractData(sd, localCursor));
                } while (localCursor.moveToNext());
            }
            localCursor.deactivate();
            localCursor.close();
        }
        return localArrayList;
    }

    public List<T> find(String sql) {
        return find(this.mDBHelper.getWritableDatabase(), sql);
    }

    public List<T> find(String paramString, int paramInt1, int paramInt2) {
        return find("select * from (" + paramString + ")" + " limit " + paramInt2 + " offset " + ((paramInt1 - 1) * paramInt2));
    }

    protected T query(SQLiteDatabase paramSQLiteDatabase, String paramString) {
        T result = null;
        Cursor cursor = paramSQLiteDatabase.rawQuery(paramString, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = extractData(paramSQLiteDatabase, cursor);
            }
            cursor.deactivate();
            cursor.close();
        }
        return result;
    }

    public T query(String paramString) {
        return query(this.mDBHelper.getWritableDatabase(), paramString);
    }
}
