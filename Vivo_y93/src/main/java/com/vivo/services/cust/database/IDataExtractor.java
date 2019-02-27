package com.vivo.services.cust.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface IDataExtractor<T> {
    T extractData(SQLiteDatabase sQLiteDatabase, Cursor cursor);
}
