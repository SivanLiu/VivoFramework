package com.vivo.provider;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.provider.CallLog;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class VivoCallLog extends CallLog {

    public static class Calls extends android.provider.CallLog.Calls {
        public static final String CACHED_PHOTO_ID = "photo_id";
        public static final String CONF_CALLID = "conf_callid";
        public static final String ENCRYPT = "encrypt";
        public static final String IS_CHILD = "is_child";
        public static final String IS_MULTI = "is_multi";
        public static final String IS_SECRET = "is_secret";
        public static final String NUMBE_RMARK_LABLE = "number_mark_lable";
        public static final String RECENT = "recent";
        public static final String RECORD_DURATION = "record_duration";
        public static final String RECORD_PATH = "record_path";
        public static final String SIM_ID = "simid";
        public static final String SUBSCRIPTION = "subscription";
        public static final String TYPE_MARKED = "type_marked";
        public static final String VTCALL = "vtcall";
    }
}
