package com.vivo.common;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class ToolUtils {
    public static String makeTag(String clz) {
        return "BD_" + clz;
    }

    public static boolean isEmpty(String txt) {
        return txt == null || txt.trim().length() <= 0;
    }

    public static boolean isEmpty(JSONObject obj) {
        return obj == null || obj.length() <= 0;
    }

    public static boolean isEmpty(List<?> lt) {
        return lt == null || lt.size() <= 0;
    }

    public static boolean isEmpty(Set<?> st) {
        return st == null || st.size() <= 0;
    }

    public static boolean isEmpty(Map<?, ?> mp) {
        return mp == null || mp.size() <= 0;
    }

    public static Object getStaticObjectField(Class<?> clazz, String field) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField(field);
        declaredField.setAccessible(true);
        return declaredField.get(null);
    }
}
