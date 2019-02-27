package com.vivo.common.autobrightness;

import android.util.Log;
import com.vivo.common.provider.Calendar.Events;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class AppBriRatioJson {
    private static final String TAG = "AppBriRatioJson";
    private JSONObject mJsonObj = null;
    public ArrayList<String> mKeyArrayList = null;

    public boolean parseJsonString(String json) {
        if (json == null || Events.DEFAULT_SORT_ORDER.equals(json)) {
            this.mKeyArrayList = null;
            this.mJsonObj = null;
            return false;
        }
        try {
            this.mJsonObj = new JSONObject(json);
            if (this.mJsonObj != null) {
                this.mKeyArrayList = new ArrayList(this.mJsonObj.keySet());
            } else {
                this.mKeyArrayList = null;
            }
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "parseJsonString got exception", e);
            return false;
        }
    }

    public boolean hasType(String type) {
        if (this.mKeyArrayList == null || type == null || Events.DEFAULT_SORT_ORDER.equals(type)) {
            return false;
        }
        for (String key : this.mKeyArrayList) {
            if (type.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean addType(String type, String pkg) {
        if (type == null || Events.DEFAULT_SORT_ORDER.equals(type) || pkg == null || Events.DEFAULT_SORT_ORDER.equals(pkg)) {
            return false;
        }
        if (this.mJsonObj == null) {
            this.mJsonObj = new JSONObject();
        }
        try {
            this.mJsonObj.put(type, pkg);
            this.mKeyArrayList = new ArrayList(this.mJsonObj.keySet());
        } catch (JSONException e) {
            Log.e(TAG, "addType got exception", e);
        }
        return true;
    }

    public String toJsonString() {
        if (this.mJsonObj == null) {
            return Events.DEFAULT_SORT_ORDER;
        }
        return this.mJsonObj.toString();
    }
}
