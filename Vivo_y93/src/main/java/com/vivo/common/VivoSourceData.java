package com.vivo.common;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Directory;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VivoSourceData {
    private static final String TAG = ToolUtils.makeTag("SourceData");

    public static boolean isSystemApp(Context context, String pkgName) {
        for (PackageInfo pkgInfo : context.getPackageManager().getInstalledPackages(0)) {
            if ((pkgInfo.applicationInfo.flags & 1) != 0 && pkgInfo.packageName != null && pkgInfo.packageName.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public static String getSource(Context context, String pkgName) {
        if (ToolUtils.isEmpty(pkgName)) {
            return null;
        }
        String[] projection = new String[]{Directory.PACKAGE_NAME, "source"};
        String selection = "packageName=?";
        String[] selectionArgs = new String[]{pkgName};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://com.vivo.abe.configlist.provider/sourceinfo"), projection, selection, selectionArgs, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(1);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Throwable e) {
            Log.w(TAG, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Map<String, String> getSource(Context context, Set<String> pkgNames) {
        if (ToolUtils.isEmpty((Set) pkgNames)) {
            return null;
        }
        Map<String, String> result = new HashMap();
        String[] projection = new String[]{Directory.PACKAGE_NAME, "source"};
        String selection = "packageName=?";
        Uri uri = Uri.parse("content://com.vivo.abe.configlist.provider/sourceinfo");
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        for (String pkgName : pkgNames) {
            try {
                cursor = cr.query(uri, projection, selection, new String[]{pkgName}, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    result.put(pkgName, cursor.getString(1));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable e) {
                Log.w(TAG, e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return result;
    }
}
