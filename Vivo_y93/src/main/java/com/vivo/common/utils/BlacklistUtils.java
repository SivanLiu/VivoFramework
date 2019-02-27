package com.vivo.common.utils;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.FtBuild;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class BlacklistUtils {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_TYPE = "type";
    public static final String INTENT_ACTION_ADD_BLACKLIST = "bbk.intent.action.ADD_BLACKLIST";
    public static final String INTENT_ACTION_DEL_BLACKLIST = "bbk.intent.action.DEL_BLACKLIST";
    private static final int MINI_MATCH = (FtBuild.isOverSeas() ? 7 : 11);
    private static final int SDK_VERSION = SystemProperties.getInt("ro.build.version.sdk", 23);
    private static final String TAG = "BlackListUtils";

    public static boolean isInBlacklist(ContentResolver cr, String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Uri uri;
        String numberColumn;
        if (SDK_VERSION > 23) {
            uri = Uri.parse("content://com.android.blockednumber/blocked");
            numberColumn = "original_number";
        } else {
            uri = Uri.parse("content://black_list/blacklist");
            numberColumn = "number";
        }
        boolean ans = false;
        Cursor cursor = null;
        String numberquery = number;
        int numberLenth = number.length();
        try {
            if (number.contains("'")) {
                numberquery = number.replace("'", "''");
            }
            if (numberLenth >= MINI_MATCH) {
                cursor = cr.query(uri, null, " PHONE_NUMBERS_EQUAL(" + numberColumn + ", '" + numberquery + "') ", null, null);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(numberColumn);
                sb.append("='");
                sb.append(numberquery);
                sb.append("'");
                cursor = cr.query(uri, null, sb.toString(), null, null);
            }
            if (cursor != null && cursor.getCount() > 0) {
                ans = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "isInBlackList number:" + number + ",ans=" + ans);
        return ans;
    }

    public static void addToBlackList(Context context, String name, String number) {
        Intent intent = new Intent("bbk.intent.action.ADD_BLACKLIST");
        intent.putExtra("number", new String[]{number});
        intent.putExtra("name", name);
        intent.putExtra("type", "add");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "activity not founded");
        }
    }

    public static void delFromBlackList(Context context, String name, String number) {
        Intent intent = new Intent("bbk.intent.action.DEL_BLACKLIST");
        intent.putExtra("number", new String[]{number});
        intent.putExtra("name", name);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "activity not founded");
        }
    }
}
