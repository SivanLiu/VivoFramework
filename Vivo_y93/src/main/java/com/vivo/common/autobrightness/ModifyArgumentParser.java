package com.vivo.common.autobrightness;

import android.util.Log;
import android.util.Slog;
import com.vivo.common.provider.Calendar.Events;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ModifyArgumentParser {
    public static final String KEY_CAMERA_OPEN_COUNT = "camopcount";
    public static final String KEY_CURVE_VERSION = "c_ver";
    public static final String KEY_LCM_COLOR = "cl";
    public static final String KEY_PHONESTATUS = "phone_status";
    public static final String KEY_PHONESTATUS_COUNT = "pscount";
    public static final String KEY_RECORD_NEED = "rneed";
    public static final String KEY_USER_SET_BRI = "set_bri";
    public static final String TAG = "ModifyArgumentParser";

    public static JSONObject argumentToJsonObject(ModifyArgument argument) {
        JSONObject jsonObj = new JSONObject();
        try {
            int i;
            if (argument.mRecordNeed.length > 0) {
                JSONArray paramArray = new JSONArray();
                for (i = 0; i < argument.mRecordNeed.length; i++) {
                    if (argument.mRecordNeed[i].length > 0) {
                        JSONArray param = new JSONArray();
                        for (int put : argument.mRecordNeed[i]) {
                            param.put(put);
                        }
                        paramArray.put(param);
                    } else {
                        paramArray.put(null);
                    }
                }
                jsonObj.put(KEY_RECORD_NEED, paramArray);
            }
            if (argument.mPhoneStatus.length > 0) {
                JSONArray phonestatus = new JSONArray();
                for (int put2 : argument.mPhoneStatus) {
                    phonestatus.put(put2);
                }
                jsonObj.put(KEY_PHONESTATUS, phonestatus);
            }
            jsonObj.put(KEY_PHONESTATUS_COUNT, argument.mPhoneStatusCount);
            jsonObj.put(KEY_CAMERA_OPEN_COUNT, argument.mCameraOpenCount);
            jsonObj.put(KEY_USER_SET_BRI, argument.bUserSettingBrightness);
            jsonObj.put(KEY_CURVE_VERSION, 2);
            return jsonObj;
        } catch (JSONException ex) {
            Log.e(TAG, "argumentToJsonString e: ", ex);
            return null;
        }
    }

    public String argumentToJsonString(ModifyArgument argument) {
        if (argument == null) {
            Log.d(TAG, "argumentToJsonString argument is null. return \"\"");
            return Events.DEFAULT_SORT_ORDER;
        }
        String ret = Events.DEFAULT_SORT_ORDER;
        JSONObject obj = argumentToJsonObject(argument);
        if (obj != null) {
            ret = obj.toString();
        }
        return ret;
    }

    public boolean stringToArgument(String argStr, ModifyArgument argument) {
        if (argStr == null || Events.DEFAULT_SORT_ORDER.equals(argStr) || argument == null) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("stringToArgument invalid arg: jsonStr=\"");
            if (argStr == null) {
                argStr = "NULL";
            }
            Log.d(str, append.append(argStr).append("\" ").append(argument == null ? "NULL" : "NOT_NUL").toString());
            return false;
        }
        try {
            boolean tmpUserSet = Boolean.FALSE.booleanValue();
            JSONObject obj = (JSONObject) new JSONTokener(argStr).nextValue();
            try {
                int curveVersion = obj.getInt(KEY_CURVE_VERSION);
                if (curveVersion != 2) {
                    Slog.e(TAG, "stringToArgument saved: ver" + curveVersion + " not equals " + 2 + " Don't use this moidify argument.");
                    return false;
                }
                argument.bUserSettingBrightness = obj.getBoolean(KEY_USER_SET_BRI);
                return true;
            } catch (JSONException e) {
                Slog.e(TAG, "stringToArgument Can't find curve_version. Don't use this moidify argument.");
                return false;
            }
        } catch (JSONException ex) {
            Log.e(TAG, "stringToArgument ex:", ex);
            return false;
        }
    }
}
