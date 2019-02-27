package com.vivo.common.autobrightness;

import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import java.util.HashMap;

public class ParamParser {
    private HashMap<String, String> mMap = new HashMap(64);

    public ParamParser(String str) {
        unflatten(str);
    }

    public String get(String key) {
        return (String) this.mMap.get(key);
    }

    public String flatten() {
        StringBuilder flattened = new StringBuilder(StateInfo.STATE_BIT_BATTERY);
        for (String k : this.mMap.keySet()) {
            flattened.append(k);
            flattened.append("=");
            flattened.append((String) this.mMap.get(k));
            flattened.append(";");
        }
        flattened.deleteCharAt(flattened.length() - 1);
        return flattened.toString();
    }

    public void unflatten(String flattened) {
        this.mMap.clear();
        StringSplitter<String> splitter = new SimpleStringSplitter(';');
        splitter.setString(flattened);
        for (String kv : splitter) {
            int pos = kv.indexOf(61);
            if (pos != -1) {
                this.mMap.put(kv.substring(0, pos), kv.substring(pos + 1));
            }
        }
    }

    public void clear() {
        this.mMap.clear();
    }
}
