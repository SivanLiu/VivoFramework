package com.vivo.common.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.vivo.common.provider.Calendar.Events;

public class Utils {
    public static final String ADD_BLACKLIST_FROM_INTERNAL = "add_blacklist_from_internal";
    public static final String BLACKLIST_ADDED_DIALOG_FLAG = "blacklist_added_dialog_flag";
    public static final String BLACKLIST_EDIT_ID = "blacklist_edit_id";
    public static final String BLACKLIST_EDIT_NAME = "blacklist_edit_name";
    public static final String BLACKLIST_EDIT_NUMBER = "blacklist_edit_number";
    public static final String BLACKLIST_EDIT_NUMBERS = "blacklist_edit_numbers";
    public static final String BLACKLIST_EDIT_TITLE = "blacklist_edit_title";
    public static final String BLACKLIST_FROM_EXTERNAL = "blacklist_from_external";
    public static final String BLACK_LIST_ADDED_NUMBERS = "black_list_added_numbers";
    public static final String DEFAULT_TIMING_INTERCEPT_END_TIME = "07:00";
    public static final String DEFAULT_TIMING_INTERCEPT_START_TIME = "23:00";
    public static final String EXPORT_INTERCEPT_LOG = "export_intercept_log";
    public static final String FROM_BLACKWHITELIST = "from_blackwhitelist";
    public static final String IMPORT_INTERCEPT_LOG = "import_intercept_log";
    public static final int INTERCEPT_MODE_NORMAL = 0;
    public static final int INTERCEPT_MODE_TIMING = 1;
    public static final String KEY_SHOW_TITLE_BUTTON = "show_title_button";
    public static final int MIN_LENGTH_WITHOUT12520 = 6;
    private static final String[] PREFIX = new String[]{"+86", "125862", "17951", "17911", "12593", "0086"};
    public static final String SETTINGS_COMPLETELY_REJECT = "settings_completely_reject";
    public static final String SETTINGS_HARASS_INTERCEPT = "settings_harass_intercept";
    public static final String SETTINGS_INTERCEPT_MODE = "settings_intercept_mode";
    public static final String SETTINGS_INTERCEPT_REMIND = "settings_intercept_remind";
    public static final String SETTINGS_INTERCEPT_RINGING_ONCE = "settings_intercept_ringing_once";
    public static final String SETTINGS_RECEIVE_WHITELIST = "settings_receive_whitelist";
    public static final String SETTINGS_RECEIVE_WHITELIST_IN_TIMING = "settings_receive_whitelist_in_timing";
    public static final String SETTINGS_REJECT_BLACKLIST = "settings_reject_blacklist";
    public static final String SETTINGS_REJECT_BLACKLIST_IN_TIMING = "settings_reject_blacklist_in_timing";
    public static final String SETTINGS_REJECT_KEYWORD = "settings_reject_keyword";
    public static final String SETTINGS_REJECT_KEYWORD_IN_TIMING = "settings_reject_keyword_in_timing";
    public static final String SETTINGS_REJECT_NO_NUMBER = "settings_reject_no_number";
    public static final String SETTINGS_REJECT_NO_NUMBER_IN_TIMING = "settings_reject_no_number_in_timing";
    public static final String SETTINGS_REPEAT_CALL = "settings_repeat_call";
    public static final String SETTINGS_TIMING_INTERCEPT = "settings_timing_intercept";
    public static final String SETTINGS_TIMING_INTERCEPT_MODE = "settings_timing_intercept_mode";
    public static final String TIMING_INTERCEPT_END_TIME = "timing_intercept_end_time";
    public static final String TIMING_INTERCEPT_START_TIME = "timing_intercept_start_time";

    public static final class Blacklist {
        public static final int MAX_NUMBER_LENGTH = 30;
        public static final int MIN_NUMBER_LENGTH = 3;
    }

    public static final class InterceptMode {
        public static final int accept_contacts_only = 2;
        public static final int accept_whitelist_only = 3;
        public static final int blacklist_only = 1;
        public static final int reject_all = 4;
        public static final int smart_intercept = 0;
    }

    public static String formatNumber(String num) {
        if (TextUtils.isEmpty(num)) {
            return num;
        }
        for (int i = 0; i < PREFIX.length; i++) {
            int len = PREFIX[i].length();
            if (num.equals(PREFIX[i])) {
                return num;
            }
            if (num.startsWith(PREFIX[i])) {
                num = num.substring(len);
                break;
            }
        }
        return num;
    }

    public static String removeLocationPrefix(String num) {
        if (TextUtils.isEmpty(num)) {
            return num;
        }
        String regex1 = "^010";
        String regex2 = "^02[0-9]{8,9}";
        String regex3 = "^0[3-9][0-9]{9,10}";
        String regex1f = "010";
        String regex2f = "02[0-9]";
        String regex3f = "0[3-9][0-9]{2}";
        String number = null;
        if (10 <= num.length() && num.length() <= 12) {
            if (num.matches(regex1)) {
                number = num.replaceFirst(regex1f, Events.DEFAULT_SORT_ORDER);
            } else if (num.matches(regex2)) {
                number = num.replaceFirst(regex2f, Events.DEFAULT_SORT_ORDER);
            } else if (num.matches(regex3)) {
                number = num.replaceFirst(regex3f, Events.DEFAULT_SORT_ORDER);
            }
        }
        if (TextUtils.isEmpty(number)) {
            number = num;
        }
        return number;
    }

    public static String removePrefix12520(String num) {
        if (TextUtils.isEmpty(num)) {
            return num;
        }
        String prefix = "12520";
        int len = prefix.length();
        if (!num.equals(prefix) && num.length() >= 11 && num.startsWith(prefix)) {
            num = num.substring(len);
        }
        return num;
    }

    public static boolean isHarassInterceptEnable(Context context) {
        return System.getInt(context.getContentResolver(), SETTINGS_HARASS_INTERCEPT, 1) == 1;
    }

    public static int getInterceptMode(Context context) {
        int index = System.getInt(context.getContentResolver(), SETTINGS_INTERCEPT_MODE, 0);
        if (index < 0 || index > 4) {
            return 0;
        }
        return index;
    }

    public static boolean isInterceptRingingOnceEnable(Context context) {
        return System.getInt(context.getContentResolver(), SETTINGS_INTERCEPT_RINGING_ONCE, 0) == 1;
    }

    public static boolean isTimingInterceptEnable(Context context) {
        return System.getInt(context.getContentResolver(), SETTINGS_TIMING_INTERCEPT, 0) == 1;
    }

    public static boolean isInterceptRemindEnable(Context context) {
        return System.getInt(context.getContentResolver(), SETTINGS_INTERCEPT_REMIND, 1) == 1;
    }

    public static boolean isCompletelyRejectEnable(Context context) {
        return System.getInt(context.getContentResolver(), SETTINGS_COMPLETELY_REJECT, 0) == 1;
    }

    public static int getTimingInterceptMode(Context context) {
        int index = System.getInt(context.getContentResolver(), SETTINGS_TIMING_INTERCEPT_MODE, 2);
        if (index < 0 || index > 4) {
            return 2;
        }
        return index;
    }

    public static void setHarassIntercept(Context context, boolean on) {
        System.putInt(context.getContentResolver(), SETTINGS_HARASS_INTERCEPT, on ? 1 : 0);
    }

    public static void setInterceptMode(Context context, int index) {
        System.putInt(context.getContentResolver(), SETTINGS_INTERCEPT_MODE, index);
    }

    public static void setInterceptRingingOnce(Context context, boolean on) {
        System.putInt(context.getContentResolver(), SETTINGS_INTERCEPT_RINGING_ONCE, on ? 1 : 0);
    }

    public static void setTimingIntercept(Context context, boolean on) {
        System.putInt(context.getContentResolver(), SETTINGS_TIMING_INTERCEPT, on ? 1 : 0);
    }

    public static void setInterceptRemind(Context context, boolean on) {
        System.putInt(context.getContentResolver(), SETTINGS_INTERCEPT_REMIND, on ? 1 : 0);
    }

    public static void setCompletelyReject(Context context, boolean on) {
        System.putInt(context.getContentResolver(), SETTINGS_COMPLETELY_REJECT, on ? 1 : 0);
    }

    public static void setTimingInterceptMode(Context context, int index) {
        System.putInt(context.getContentResolver(), SETTINGS_TIMING_INTERCEPT_MODE, index);
    }

    public static boolean isRejectNoNumberEnable(ContentResolver cr) {
        return System.getInt(cr, SETTINGS_REJECT_NO_NUMBER, 0) == 1;
    }

    public static boolean isRejectBlacklistEnable(ContentResolver cr) {
        return System.getInt(cr, SETTINGS_REJECT_BLACKLIST, 1) == 1;
    }
}
