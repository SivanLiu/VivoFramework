package com.vivo.services.epm.config;

import android.content.ContentValues;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfigFileRecord {
    public static final int MASK_CONTENTVALUESLIST = 3;
    public static final int MASK_STRINGLIST = 2;
    public static final int MASK_SWITCH = 1;
    private static final String TAG = "EPM";
    List<Object> configs;
    List<Object> lastConfigs;
    final RemoteCallbackList<IConfigChangeCallback> mConfigChangeObservers;
    private ConfigFileObserver mConfigFileObserver;
    private final Object mDispatchChangeLock;
    private String mFilePath;
    private final Object mLock;
    private int type;

    public boolean isSwitchConfigFile() {
        return this.type == 1;
    }

    public boolean isStringListConfigFile() {
        return this.type == 2;
    }

    public boolean isContentValuesListConfigFile() {
        return this.type == 3;
    }

    public ConfigFileRecord(String path, int type) {
        this(path, type, null);
    }

    public ConfigFileRecord(String path, int type, List<Object> configs) {
        this.mLock = new Object();
        this.mDispatchChangeLock = new Object();
        this.mConfigChangeObservers = new RemoteCallbackList();
        this.mFilePath = path;
        this.type = type;
        this.configs = configs;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public void setConfigList(List<Object> configs) {
        synchronized (this.mLock) {
            this.lastConfigs = this.configs;
            this.configs = configs;
        }
    }

    public List<String> computeChangedConfigNames() {
        ArrayList<String> changedNames = new ArrayList();
        if (this.lastConfigs == null) {
            return changedNames;
        }
        boolean found;
        boolean isChanged;
        String name;
        if (this.type == 1) {
            synchronized (this.mLock) {
                List<Switch> last = ListConvertHelper.convertObjectList2SwitchList(this.lastConfigs);
                List<Switch> current = ListConvertHelper.convertObjectList2SwitchList(this.configs);
            }
            for (Switch w : last) {
                found = false;
                isChanged = false;
                name = w.getName();
                boolean isOn = w.isOn();
                for (Switch k : current) {
                    if (name.equals(k.getName())) {
                        found = true;
                        isChanged = isOn != k.isOn();
                        if (!isChanged || (found ^ 1) != 0) {
                            changedNames.add(name);
                        }
                    }
                }
                if (!isChanged) {
                }
                changedNames.add(name);
            }
            for (Switch k2 : current) {
                found = false;
                name = k2.getName();
                for (Switch w2 : last) {
                    if (name.equals(w2.getName())) {
                        found = true;
                    }
                }
                if (!found) {
                    changedNames.add(name);
                }
            }
        } else if (this.type == 2) {
            synchronized (this.mLock) {
                List<StringList> last2 = ListConvertHelper.convertObjectList2StringList(this.lastConfigs);
                List<StringList> current2 = ListConvertHelper.convertObjectList2StringList(this.configs);
            }
            for (StringList w3 : last2) {
                found = false;
                isChanged = false;
                name = w3.getName();
                List<String> values = w3.getValues();
                for (StringList k3 : current2) {
                    if (name.equals(k3.getName())) {
                        found = true;
                        isChanged = ListConvertHelper.compareStringList(values, k3.getValues());
                        break;
                    }
                }
                if (isChanged || (found ^ 1) != 0) {
                    changedNames.add(name);
                }
            }
            for (StringList k32 : current2) {
                found = false;
                name = k32.getName();
                for (StringList w32 : last2) {
                    if (name.equals(w32.getName())) {
                        found = true;
                    }
                    if (!found) {
                        changedNames.add(name);
                    }
                }
            }
        } else if (this.type == 3) {
            synchronized (this.mLock) {
                List<ContentValuesList> last3 = ListConvertHelper.convertObjectList2ContentValuesList(this.lastConfigs);
                List<ContentValuesList> current3 = ListConvertHelper.convertObjectList2ContentValuesList(this.configs);
            }
            for (ContentValuesList w4 : last3) {
                found = false;
                isChanged = false;
                name = w4.getName();
                List<ContentValues> values2 = w4.getValues();
                for (ContentValuesList k4 : current3) {
                    if (name.equals(k4.getName())) {
                        found = true;
                        isChanged = ListConvertHelper.compareContentValuesList(values2, k4.getValues());
                        break;
                    }
                }
                if (isChanged || (found ^ 1) != 0) {
                    changedNames.add(name);
                }
            }
            for (ContentValuesList k42 : current3) {
                found = false;
                name = k42.getName();
                for (ContentValuesList w42 : last3) {
                    if (name.equals(w42.getName())) {
                        found = true;
                    }
                    if (!found) {
                        changedNames.add(name);
                    }
                }
            }
        }
        return changedNames;
    }

    public void startObserverConfigFile(Handler handler) {
        synchronized (this) {
            Log.d(TAG, "startObserverConfigFile " + this.mFilePath);
            if (this.mConfigFileObserver != null) {
                this.mConfigFileObserver.stopWatching();
            }
            File file = new File(this.mFilePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.mConfigFileObserver = new ConfigFileObserver(this.mFilePath, this.type, 1544, handler);
            this.mConfigFileObserver.startWatching();
        }
        return;
    }

    public boolean isCallbackRegistered(IConfigChangeCallback callback) {
        synchronized (this) {
            int i = this.mConfigChangeObservers.beginBroadcast();
            while (i > 0) {
                i--;
                if (callback.asBinder() == ((IConfigChangeCallback) this.mConfigChangeObservers.getBroadcastItem(i)).asBinder()) {
                    Log.d(TAG, "callback " + callback.asBinder() + " is registered");
                    this.mConfigChangeObservers.finishBroadcast();
                    return true;
                }
            }
            this.mConfigChangeObservers.finishBroadcast();
            return false;
        }
    }

    public boolean addConfigChangeCallback(IConfigChangeCallback callback, String name) {
        if (isCallbackRegistered(callback)) {
            Log.d(TAG, "callback " + callback + " has registered, one ConfigurationObserver only observe one config...");
            return false;
        }
        boolean register;
        synchronized (this) {
            register = this.mConfigChangeObservers.register(callback, name);
        }
        return register;
    }

    public boolean removeConfigChangeCallback(IConfigChangeCallback callback, String name) {
        boolean unregister;
        synchronized (this) {
            unregister = this.mConfigChangeObservers.unregister(callback);
        }
        return unregister;
    }

    public void dispatchConfigChanged() {
        List<String> names = computeChangedConfigNames();
        Log.d(TAG, "dispatchConfigChanged " + this.mFilePath + " changed names={" + names + "}");
        if (names != null && names.size() != 0) {
            synchronized (this.mDispatchChangeLock) {
                int i = this.mConfigChangeObservers.beginBroadcast();
                while (i > 0) {
                    i--;
                    IConfigChangeCallback observer = (IConfigChangeCallback) this.mConfigChangeObservers.getBroadcastItem(i);
                    String name = (String) this.mConfigChangeObservers.getBroadcastCookie(i);
                    if (names.contains(name) && observer != null) {
                        try {
                            observer.onConfigChange(this.mFilePath, name);
                        } catch (RemoteException e) {
                        }
                    }
                }
                this.mConfigChangeObservers.finishBroadcast();
            }
        }
    }

    public void dump(PrintWriter pw) {
        pw.println(this.mFilePath + "{ ");
        if (this.configs == null) {
            pw.println("nothing");
        } else if (isSwitchConfigFile()) {
            for (Switch o : this.configs) {
                pw.println(o);
            }
        } else if (isStringListConfigFile()) {
            for (StringList o2 : this.configs) {
                pw.println(o2);
            }
        } else if (isContentValuesListConfigFile()) {
            for (ContentValuesList o3 : this.configs) {
                pw.println(o3);
            }
        }
        pw.println(" }");
        pw.println("{");
        int i = this.mConfigChangeObservers.beginBroadcast();
        while (i > 0) {
            i--;
            pw.println(((String) this.mConfigChangeObservers.getBroadcastCookie(i)) + " callback-->" + ((IConfigChangeCallback) this.mConfigChangeObservers.getBroadcastItem(i)).asBinder());
        }
        this.mConfigChangeObservers.finishBroadcast();
        pw.println("}");
    }
}
