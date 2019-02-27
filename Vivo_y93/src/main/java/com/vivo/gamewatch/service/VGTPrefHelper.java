package com.vivo.gamewatch.service;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class VGTPrefHelper {
    public static void put(String name, Bundle data) {
        if (data != null && data.keySet() != null) {
            Set<String> keys = data.keySet();
            Editor editor = VGTServer.getInstance().getAppContext().getSharedPreferences(name, 0).edit();
            for (String key : keys) {
                Object value = data.get(key);
                if (value instanceof Integer) {
                    editor.putInt(key, ((Integer) value).intValue());
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, ((Float) value).floatValue());
                } else if (value instanceof Long) {
                    editor.putLong(key, ((Long) value).longValue());
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, ((Boolean) value).booleanValue());
                } else {
                    Log.e("VGT", String.format("VGTPrefHelper put error %s:%s", new Object[]{key, value}));
                }
            }
            editor.commit();
        }
    }

    public static void remove(String name, ArrayList<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            Editor editor = VGTServer.getInstance().getAppContext().getSharedPreferences(name, 0).edit();
            for (String key : keys) {
                editor.remove(key);
            }
            editor.commit();
        }
    }

    public static Bundle get(String name, ArrayList<String> keys) {
        Bundle result = new Bundle();
        if (keys == null || keys.isEmpty()) {
            return result;
        }
        Map<String, ?> maps = VGTServer.getInstance().getAppContext().getSharedPreferences(name, 0).getAll();
        for (String key : keys) {
            Object value = maps.get(key);
            if (value instanceof Integer) {
                result.putInt(key, ((Integer) value).intValue());
            } else if (value instanceof String) {
                result.putString(key, (String) value);
            } else if (value instanceof Float) {
                result.putFloat(key, ((Float) value).floatValue());
            } else if (value instanceof Long) {
                result.putLong(key, ((Long) value).longValue());
            } else if (value instanceof Boolean) {
                result.putBoolean(key, ((Boolean) value).booleanValue());
            } else {
                Log.e("VGT", String.format("VGTPrefHelper get error %s:%s", new Object[]{key, value}));
            }
        }
        return result;
    }
}
