package com.vivo.services.rms.appmng.namelist;

import android.os.Bundle;
import com.vivo.services.rms.EventDispatcher;
import java.util.ArrayList;

public class WidgetList {
    public static final String LIST_NAME = "widget";
    public static final Object LOCK = new Object();
    private static final ArrayList<String> WIDGET_LIST = new ArrayList();

    public static void fillBundle(Bundle dest) {
        synchronized (WIDGET_LIST) {
            if (!WIDGET_LIST.isEmpty()) {
                Bundle data = new Bundle();
                data.putStringArrayList(LIST_NAME, new ArrayList(WIDGET_LIST));
                dest.putBundle("_apptypes", data);
            }
        }
    }

    public static ArrayList<String> getList() {
        return WIDGET_LIST;
    }

    public static void addWidget(String pkg) {
        synchronized (WIDGET_LIST) {
            if (!WIDGET_LIST.contains(pkg)) {
                WIDGET_LIST.add(pkg);
                EventDispatcher.getInstance().setAppList(LIST_NAME, new ArrayList(WIDGET_LIST));
            }
        }
    }

    public static void removeWidget(String pkg) {
        synchronized (WIDGET_LIST) {
            WIDGET_LIST.remove(pkg);
            EventDispatcher.getInstance().setAppList(LIST_NAME, new ArrayList(WIDGET_LIST));
        }
    }
}
