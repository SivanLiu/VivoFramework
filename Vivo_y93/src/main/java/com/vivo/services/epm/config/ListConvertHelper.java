package com.vivo.services.epm.config;

import android.content.ContentValues;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListConvertHelper {
    public static List<Object> convertSwitchList2ObjectList(List<Switch> src) {
        List<Object> to = new ArrayList();
        if (src == null) {
            return to;
        }
        for (Switch s : src) {
            to.add(s);
        }
        return to;
    }

    public static List<Object> convertStringList2ObjectList(List<StringList> src) {
        List<Object> to = new ArrayList();
        if (src == null) {
            return to;
        }
        for (StringList s : src) {
            to.add(s);
        }
        return to;
    }

    public static List<Object> convertContentValuesList2ObjectList(List<ContentValuesList> src) {
        List<Object> to = new ArrayList();
        if (src == null) {
            return to;
        }
        for (ContentValuesList s : src) {
            to.add(s);
        }
        return to;
    }

    public static List<Switch> convertObjectList2SwitchList(List<Object> src) {
        List<Switch> to = new ArrayList();
        if (src == null) {
            return to;
        }
        Iterator o$iterator = src.iterator();
        while (o$iterator.hasNext()) {
            to.add((Switch) o$iterator.next());
        }
        return to;
    }

    public static List<StringList> convertObjectList2StringList(List<Object> src) {
        List<StringList> to = new ArrayList();
        if (src == null) {
            return to;
        }
        Iterator o$iterator = src.iterator();
        while (o$iterator.hasNext()) {
            to.add((StringList) o$iterator.next());
        }
        return to;
    }

    public static List<ContentValuesList> convertObjectList2ContentValuesList(List<Object> src) {
        List<ContentValuesList> to = new ArrayList();
        if (src == null) {
            return to;
        }
        Iterator o$iterator = src.iterator();
        while (o$iterator.hasNext()) {
            to.add((ContentValuesList) o$iterator.next());
        }
        return to;
    }

    public static boolean compareStringList(List<String> old, List<String> current) {
        if (old == null && current != null) {
            return false;
        }
        if (old != null && current == null) {
            return true;
        }
        if (old == null && current == null) {
            return false;
        }
        if (old.size() != current.size()) {
            return true;
        }
        for (int i = 0; i < old.size(); i++) {
            if (!((String) old.get(i)).equals(current.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean compareContentValuesList(List<ContentValues> old, List<ContentValues> current) {
        if (old == null && current != null) {
            return false;
        }
        if (old != null && current == null) {
            return true;
        }
        if (old == null && current == null) {
            return false;
        }
        if (old.size() != current.size()) {
            return true;
        }
        for (int i = 0; i < old.size(); i++) {
            if (!compareContentValues((ContentValues) old.get(i), (ContentValues) current.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean compareContentValues(ContentValues old, ContentValues current) {
        if (old == null && current != null) {
            return false;
        }
        if (old != null && current == null) {
            return false;
        }
        if (old == null && current == null) {
            return true;
        }
        if (old.keySet().size() != current.keySet().size()) {
            return false;
        }
        for (String key : old.keySet()) {
            if (!old.getAsString(key).equals(current.getAsString(key))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSwitchRepeated(List<Switch> list, Switch w) {
        if (list == null || w == null) {
            return false;
        }
        String name = w.getName();
        if (name == null) {
            return false;
        }
        for (Switch tmp : list) {
            if (name.equals(tmp.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStringListRepeated(List<StringList> list, StringList sl) {
        if (list == null || sl == null) {
            return false;
        }
        String name = sl.getName();
        if (name == null) {
            return false;
        }
        for (StringList tmp : list) {
            if (name.equals(tmp.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isContentValuesListRepeated(List<ContentValuesList> list, ContentValuesList sl) {
        if (list == null || sl == null) {
            return false;
        }
        String name = sl.getName();
        if (name == null) {
            return false;
        }
        for (ContentValuesList tmp : list) {
            if (name.equals(tmp.getName())) {
                return true;
            }
        }
        return false;
    }
}
