package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import libcore.util.EmptyArray;

public class SQLiteQueryBuilder {
    private static final String TAG = "SQLiteQueryBuilder";
    private static final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
    private boolean mDistinct = false;
    private CursorFactory mFactory = null;
    private Map<String, String> mProjectionMap = null;
    private boolean mStrict;
    private String mTables = "";
    private StringBuilder mWhereClause = null;

    public void setDistinct(boolean distinct) {
        this.mDistinct = distinct;
    }

    public String getTables() {
        return this.mTables;
    }

    public void setTables(String inTables) {
        this.mTables = inTables;
    }

    public void appendWhere(CharSequence inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        this.mWhereClause.append(inWhere);
    }

    public void appendWhereEscapeString(String inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        DatabaseUtils.appendEscapedSQLString(this.mWhereClause, inWhere);
    }

    public void setProjectionMap(Map<String, String> columnMap) {
        this.mProjectionMap = columnMap;
    }

    public void setCursorFactory(CursorFactory factory) {
        this.mFactory = factory;
    }

    public void setStrict(boolean flag) {
        this.mStrict = flag;
    }

    public static String buildQueryString(boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy, String limit) {
        if (TextUtils.isEmpty(groupBy) && (TextUtils.isEmpty(having) ^ 1) != 0) {
            throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
        } else if (TextUtils.isEmpty(limit) || (sLimitPattern.matcher(limit).matches() ^ 1) == 0) {
            StringBuilder query = new StringBuilder(120);
            query.append("SELECT ");
            if (distinct) {
                query.append("DISTINCT ");
            }
            if (columns == null || columns.length == 0) {
                query.append("* ");
            } else {
                appendColumns(query, columns);
            }
            query.append("FROM ");
            query.append(tables);
            appendClause(query, " WHERE ", where);
            appendClause(query, " GROUP BY ", groupBy);
            appendClause(query, " HAVING ", having);
            appendClause(query, " ORDER BY ", orderBy);
            appendClause(query, " LIMIT ", limit);
            return query.toString();
        } else {
            throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
        }
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    public static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];
            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, null, null);
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, limit, null);
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit, CancellationSignal cancellationSignal) {
        if (this.mTables == null) {
            return null;
        }
        String sql;
        String unwrappedSql = buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
        if (!this.mStrict || selection == null || selection.length() <= 0) {
            sql = unwrappedSql;
        } else {
            db.validateSql(unwrappedSql, cancellationSignal);
            sql = buildQuery(projectionIn, wrap(selection), groupBy, having, sortOrder, limit);
        }
        String[] sqlArgs = selectionArgs;
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(selectionArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.rawQueryWithFactory(this.mFactory, sql, selectionArgs, SQLiteDatabase.findEditTable(this.mTables), cancellationSignal);
    }

    public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
        String sql;
        Objects.requireNonNull(this.mTables, "No tables defined");
        Objects.requireNonNull(db, "No database defined");
        Objects.requireNonNull(values, "No values defined");
        String unwrappedSql = buildUpdate(values, selection);
        if (this.mStrict) {
            db.validateSql(unwrappedSql, null);
            sql = buildUpdate(values, wrap(selection));
        } else {
            sql = unwrappedSql;
        }
        if (selectionArgs == null) {
            selectionArgs = EmptyArray.STRING;
        }
        String[] rawKeys = (String[]) values.keySet().toArray(EmptyArray.STRING);
        int valuesLength = rawKeys.length;
        Object[] sqlArgs = new Object[(selectionArgs.length + valuesLength)];
        for (int i = 0; i < sqlArgs.length; i++) {
            if (i < valuesLength) {
                sqlArgs[i] = values.get(rawKeys[i]);
            } else {
                sqlArgs[i] = selectionArgs[i - valuesLength];
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(sqlArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.executeSql(sql, sqlArgs);
    }

    public int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
        String sql;
        Objects.requireNonNull(this.mTables, "No tables defined");
        Objects.requireNonNull(db, "No database defined");
        String unwrappedSql = buildDelete(selection);
        if (this.mStrict) {
            db.validateSql(unwrappedSql, null);
            sql = buildDelete(wrap(selection));
        } else {
            sql = unwrappedSql;
        }
        String[] sqlArgs = selectionArgs;
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(selectionArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.executeSql(sql, selectionArgs);
    }

    public String buildQuery(String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit) {
        return buildQueryString(this.mDistinct, this.mTables, computeProjection(projectionIn), computeWhere(selection), groupBy, having, sortOrder, limit);
    }

    @Deprecated
    public String buildQuery(String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        return buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    public String buildUpdate(ContentValues values, String selection) {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(this.mTables);
        sql.append(" SET ");
        String[] rawKeys = (String[]) values.keySet().toArray(EmptyArray.STRING);
        for (int i = 0; i < rawKeys.length; i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append(rawKeys[i]);
            sql.append("=?");
        }
        appendClause(sql, " WHERE ", computeWhere(selection));
        return sql.toString();
    }

    public String buildDelete(String selection) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("DELETE FROM ");
        sql.append(this.mTables);
        appendClause(sql, " WHERE ", computeWhere(selection));
        return sql.toString();
    }

    public String buildUnionSubQuery(String typeDiscriminatorColumn, String[] unionColumns, Set<String> columnsPresentInTable, int computedColumnsOffset, String typeDiscriminatorValue, String selection, String groupBy, String having) {
        int unionColumnsCount = unionColumns.length;
        String[] projectionIn = new String[unionColumnsCount];
        for (int i = 0; i < unionColumnsCount; i++) {
            String unionColumn = unionColumns[i];
            if (unionColumn.equals(typeDiscriminatorColumn)) {
                projectionIn[i] = "'" + typeDiscriminatorValue + "' AS " + typeDiscriminatorColumn;
            } else if (i <= computedColumnsOffset || columnsPresentInTable.contains(unionColumn)) {
                projectionIn[i] = unionColumn;
            } else {
                projectionIn[i] = "NULL AS " + unionColumn;
            }
        }
        return buildQuery(projectionIn, selection, groupBy, having, null, null);
    }

    @Deprecated
    public String buildUnionSubQuery(String typeDiscriminatorColumn, String[] unionColumns, Set<String> columnsPresentInTable, int computedColumnsOffset, String typeDiscriminatorValue, String selection, String[] selectionArgs, String groupBy, String having) {
        return buildUnionSubQuery(typeDiscriminatorColumn, unionColumns, columnsPresentInTable, computedColumnsOffset, typeDiscriminatorValue, selection, groupBy, having);
    }

    public String buildUnionQuery(String[] subQueries, String sortOrder, String limit) {
        StringBuilder query = new StringBuilder(128);
        int subQueryCount = subQueries.length;
        String unionOperator = this.mDistinct ? " UNION " : " UNION ALL ";
        for (int i = 0; i < subQueryCount; i++) {
            if (i > 0) {
                query.append(unionOperator);
            }
            query.append(subQueries[i]);
        }
        appendClause(query, " ORDER BY ", sortOrder);
        appendClause(query, " LIMIT ", limit);
        return query.toString();
    }

    private String[] computeProjection(String[] projectionIn) {
        String[] projection;
        int i;
        if (projectionIn == null || projectionIn.length <= 0) {
            if (this.mProjectionMap == null) {
                return null;
            }
            Set<Entry<String, String>> entrySet = this.mProjectionMap.entrySet();
            projection = new String[entrySet.size()];
            i = 0;
            for (Entry<String, String> entry : entrySet) {
                if (!((String) entry.getKey()).equals("_count")) {
                    int i2 = i + 1;
                    projection[i] = (String) entry.getValue();
                    i = i2;
                }
            }
            return projection;
        } else if (this.mProjectionMap == null) {
            return projectionIn;
        } else {
            projection = new String[projectionIn.length];
            int length = projectionIn.length;
            for (i = 0; i < length; i++) {
                String userColumn = projectionIn[i];
                String column = (String) this.mProjectionMap.get(userColumn);
                if (column != null) {
                    projection[i] = column;
                } else if (this.mStrict || !(userColumn.contains(" AS ") || userColumn.contains(" as "))) {
                    throw new IllegalArgumentException("Invalid column " + projectionIn[i]);
                } else {
                    projection[i] = userColumn;
                }
            }
            return projection;
        }
    }

    private String computeWhere(String selection) {
        boolean hasInternal = TextUtils.isEmpty(this.mWhereClause) ^ 1;
        boolean hasExternal = TextUtils.isEmpty(selection) ^ 1;
        if (!hasInternal && !hasExternal) {
            return null;
        }
        StringBuilder where = new StringBuilder();
        if (hasInternal) {
            where.append('(').append(this.mWhereClause).append(')');
        }
        if (hasInternal && hasExternal) {
            where.append(" AND ");
        }
        if (hasExternal) {
            where.append('(').append(selection).append(')');
        }
        return where.toString();
    }

    private String wrap(String arg) {
        if (TextUtils.isEmpty(arg)) {
            return arg;
        }
        return "(" + arg + ")";
    }
}
