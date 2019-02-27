package com.vivo.common.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import java.util.ArrayList;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class SmartDialer {
    public static final String AUTHORITY = "com.android.contacts.smartdialer";
    public static final int CONTACT_ID_INDEX = 13;
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.contacts.smartdialer");
    public static final int DATE_INDEX = 5;
    public static final int EMAIL_ADDRESS_INDEX = 12;
    public static final int END_MASK = 8191;
    private static final int HIGHLIGHT_COLOR = -15762206;
    public static final int HISTORY_TYPE_CALL_ANSWER = 1;
    public static final int HISTORY_TYPE_CALL_ANSWER_VT = 9;
    public static final int HISTORY_TYPE_CALL_DIAL = 2;
    public static final int HISTORY_TYPE_CALL_DIAL_VT = 10;
    public static final int HISTORY_TYPE_CALL_LOST = 3;
    public static final int HISTORY_TYPE_CALL_LOST_VT = 11;
    public static final int HISTORY_TYPE_EXCHANGE_ACCOUNT = 13;
    public static final int HISTORY_TYPE_GOOGLE_ACCOUNT = 12;
    public static final int HISTORY_TYPE_INDEX = 4;
    public static final int HISTORY_TYPE_LAST_CALLED_ACCOUNT = 15;
    public static final int HISTORY_TYPE_MSG = 4;
    public static final int HISTORY_TYPE_NO_USED = 5;
    public static final int HISTORY_TYPE_OTHER_ACCOUNT = 14;
    public static final int HZ_FLAG_POS = 26;
    public static final int INDICATE_PHONE_SIM_INDEX = 11;
    public static final int MATCH_END_INDEX = 7;
    public static final int MATCH_START_INDEX = 6;
    public static final int MATCH_TYPE_EMAIL = 5;
    public static final int MATCH_TYPE_FULL = 0;
    public static final int MATCH_TYPE_INDEX = 3;
    public static final int MATCH_TYPE_JP = 1;
    public static final int MATCH_TYPE_JP_QP = 2;
    public static final int MATCH_TYPE_NUMBER = 4;
    public static final int MATCH_TYPE_QP = 3;
    public static final int NAME_INDEX = 1;
    public static final int NUM_OF_BINARY_DIGITS = 13;
    public static final int PHONE_NUMBER_INDEX = 2;
    public static final int PHOTO_ID_INDEX = 0;
    public static final int SECTION_MATCHED_INDEX = 9;
    public static final int SECTION_NAME_INDEX = 8;
    public static final int SECTION_NUMBER_INDEX = 10;
    public static final String SMART_SEARCH_TYPE_NOMAL = "SmartSearchExcludingEmail";
    public static final String SMART_SEARCH_TYPE_PLUS_EMAIL = "SmartSearchIncludingEmail";
    public static final int START_POS = 13;
    private static final String TAG = "SmartDialer";

    private SmartDialer() {
    }

    public static void startServiceIfNecessary(Context context) {
        context.getContentResolver().insert(CONTENT_URI, new ContentValues());
    }

    public static Cursor doSearch(Context context, String pattern) {
        return doSearch(context, pattern, SMART_SEARCH_TYPE_NOMAL);
    }

    public static Cursor doSearch(Context context, String pattern, String sortOrder) {
        return context.getContentResolver().query(CONTENT_URI, null, pattern, null, sortOrder);
    }

    public static void prepare(Context context) {
        context.getContentResolver().update(CONTENT_URI, new ContentValues(), null, null);
    }

    public static void release(Context context) {
        context.getContentResolver().delete(CONTENT_URI, null, null);
    }

    public static CharSequence formatHightLight_NUM(Context context, String phoneNumber, Cursor cursor) {
        SpannableStringBuilder buf = new SpannableStringBuilder(phoneNumber);
        buf.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLOR), cursor.getInt(6), cursor.getInt(7) + 1, 33);
        return buf;
    }

    public static CharSequence formatHightLight_name_NUM(Context context, String phoneNumber, Cursor cursor) {
        SpannableStringBuilder buf = new SpannableStringBuilder(phoneNumber);
        buf.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLOR), cursor.getInt(6), cursor.getInt(7) + 1, 33);
        return buf;
    }

    public static CharSequence formatHightLight_JP(Context context, String name, Cursor cursor) {
        SpannableStringBuilder buf = new SpannableStringBuilder(Events.DEFAULT_SORT_ORDER);
        int matchStart = cursor.getInt(6);
        int matchEnd = cursor.getInt(7);
        int[] sectionName = getIntArray(cursor, 8);
        buf.append(name);
        if (matchEnd >= sectionName.length) {
            Log.e(TAG, "wwww matchEnd >= size of sectionName! name = " + name);
            return buf;
        }
        for (int i = matchStart; i <= matchEnd; i++) {
            int start = (-67108865 & sectionName[i]) >> 13;
            int end = start;
            buf.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLOR), start, start + 1, 17);
        }
        return buf;
    }

    public static CharSequence formatHightLight_JP_QP(Context context, String name, Cursor cursor) {
        int i;
        SpannableStringBuilder buf = new SpannableStringBuilder(Events.DEFAULT_SORT_ORDER);
        int start_sec = -1;
        int[] sectionMatched = getIntArray(cursor, 9);
        int[] sectionName = getIntArray(cursor, 8);
        int[] sectionNumber = getIntArray(cursor, 10);
        buf.append(name);
        int start_matched = sectionMatched[sectionMatched.length - 1] >> 13;
        for (i = 0; i < sectionNumber.length; i++) {
            if ((sectionNumber[i] >> 13) == start_matched) {
                start_sec = i;
                break;
            }
        }
        if (start_sec == -1) {
            return buf;
        }
        for (i = 0; i < sectionMatched.length; i++) {
            int start;
            int end;
            int tmp = sectionName[((sectionMatched.length - 1) - i) + start_sec];
            if (((tmp >> 26) & 1) == 1) {
                start = (-67108865 & tmp) >> 13;
                end = start;
            } else {
                start = (-67108865 & tmp) >> 13;
                tmp = sectionMatched[i];
                end = start + ((tmp & END_MASK) - (tmp >> 13));
            }
            buf.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLOR), start, end + 1, 17);
        }
        return buf;
    }

    public static CharSequence formatHightLight_QP(Context context, String name, Cursor cursor) {
        SpannableStringBuilder buf = new SpannableStringBuilder(Events.DEFAULT_SORT_ORDER);
        buf.append(name);
        for (int tmp : getIntArray(cursor, 8)) {
            buf.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLOR), (-67108865 & tmp) >> 13, (tmp & END_MASK) + 1, 17);
        }
        return buf;
    }

    private static int[] getIntArray(Cursor cursor, int sectionColumnIndex) {
        byte[] bytes = cursor.getBlob(sectionColumnIndex);
        int[] intArray = new int[(bytes.length >> 2)];
        for (int i = 0; i < intArray.length; i++) {
            int j = i << 2;
            intArray[i] = ((((bytes[j] << 24) & -16777216) | ((bytes[j + 1] << 16) & 16711680)) | ((bytes[j + 2] << 8) & 65280)) | (bytes[j + 3] & 255);
        }
        return intArray;
    }

    public static ArrayList<Integer> getHightLightInterval(ArrayList<Integer> name, ArrayList<Integer> quan_pin, int begin, int end) {
        ArrayList<Integer> ans = new ArrayList();
        int size = quan_pin.size();
        int len = (end - begin) + 1;
        for (int index = 0; index < size; index++) {
            int item = ((Integer) quan_pin.get(index)).intValue();
            int st = item >> 13;
            int ed = item & END_MASK;
            if (ed >= begin) {
                if (st > end) {
                    break;
                }
                int name_item = ((Integer) name.get(index)).intValue();
                if (((name_item >> 26) & 1) == 1) {
                    ans.add(Integer.valueOf(-67108865 & name_item));
                } else {
                    name_item &= -67108865;
                    int name_st = name_item >> 13;
                    int name_ed = name_item & END_MASK;
                    if (st <= begin && ed <= end) {
                        ans.add(Integer.valueOf(((name_st + (begin - st)) << 13) | name_ed));
                    } else if (st <= begin && ed > end) {
                        int delta = begin - st;
                        ans.add(Integer.valueOf(((name_st + delta) << 13) | (((name_st + delta) + len) - 1)));
                    } else if (st <= begin || ed > end) {
                        ans.add(Integer.valueOf((name_st << 13) | (name_st + (end - st))));
                    } else {
                        ans.add(Integer.valueOf(name_item));
                    }
                }
            }
        }
        return ans;
    }
}
