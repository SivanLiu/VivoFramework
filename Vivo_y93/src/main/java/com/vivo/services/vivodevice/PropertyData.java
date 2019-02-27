package com.vivo.services.vivodevice;

import com.vivo.common.provider.Calendar.Events;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PropertyData {
    protected static String VIVO_DEVICE_NATIVE_CLASS = "com.vivo.services.vivodevice.VivoDeviceNative";
    private List<IPropertyChangedListener> listenerList;
    private boolean reloadEachTime;
    private String value = Events.DEFAULT_SORT_ORDER;
    private Method valueGetMethod;
    private Method valueSetMethod;

    public PropertyData(String value, String set, String get, String dataPoll, boolean reloadEachTime) {
        this.value = value;
        try {
            Class c = Class.forName(VIVO_DEVICE_NATIVE_CLASS);
            if (!(get == null || (get.equals(Events.DEFAULT_SORT_ORDER) ^ 1) == 0)) {
                this.valueGetMethod = c.getMethod(get, new Class[0]);
            }
            this.reloadEachTime = reloadEachTime;
            if (!(set == null || (set.equals(Events.DEFAULT_SORT_ORDER) ^ 1) == 0)) {
                this.valueSetMethod = c.getMethod(set, new Class[]{String.class});
            }
            this.listenerList = new ArrayList();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }
    }

    public String getValue() {
        if (this.value.equals(Events.DEFAULT_SORT_ORDER) || this.reloadEachTime) {
            try {
                if (this.valueGetMethod != null) {
                    this.value = (String) this.valueGetMethod.invoke(VivoDeviceNative.getDeviceNative(), new Object[0]);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
        }
        return this.value;
    }

    public String setValue(String value) {
        try {
            if (this.valueSetMethod != null) {
                VivoDeviceNative v = VivoDeviceNative.getDeviceNative();
                if (((Boolean) this.valueSetMethod.invoke(v, new Object[]{value})).booleanValue()) {
                    this.value = value;
                    return "true";
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return "false";
    }

    public void registerListener(IPropertyChangedListener listener) {
        this.listenerList.add(listener);
    }

    public void unregisterListener(IPropertyChangedListener listener) {
        this.listenerList.remove(listener);
    }
}
