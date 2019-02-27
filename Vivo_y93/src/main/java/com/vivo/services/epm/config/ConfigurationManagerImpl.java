package com.vivo.services.epm.config;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.vivo.services.rms.ProcessList;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class ConfigurationManagerImpl implements IConfigurationManager {
    private static final String DEFAULT_SYSTEM_CONFIG_DIR = "/data/bbkcore/";
    private static final int MSG_RE_OBSERVER_CONFIG_FILE = 1;
    static final String TAG = "EPM";
    private static ConfigurationManagerImpl sInstance;
    private HashMap<String, ConfigFileRecord> mContentValuesListConfigFileMap = new HashMap();
    private Context mContext;
    private Handler mMainHandler;
    private HandlerThread mMainHandlerThread;
    private HashMap<String, ConfigFileRecord> mStringListConfigFileMap = new HashMap();
    private HashMap<String, ConfigFileRecord> mSwitchConfigFileMap = new HashMap();

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(ConfigurationManagerImpl.TAG, "MSG_RE_OBSERVER_CONFIG_FILE");
                    String path = msg.obj;
                    HashMap tmp = null;
                    switch (msg.arg1) {
                        case 1:
                            tmp = ConfigurationManagerImpl.this.mSwitchConfigFileMap;
                            break;
                        case 2:
                            tmp = ConfigurationManagerImpl.this.mStringListConfigFileMap;
                            break;
                        case 3:
                            tmp = ConfigurationManagerImpl.this.mContentValuesListConfigFileMap;
                            break;
                    }
                    if (tmp != null) {
                        ConfigFileRecord record;
                        synchronized (tmp) {
                            record = (ConfigFileRecord) tmp.get(path);
                        }
                        if (record != null) {
                            record.startObserverConfigFile(this);
                            return;
                        }
                        return;
                    }
                    return;
                case 1000:
                    Log.d(ConfigurationManagerImpl.TAG, "MSG_CONFIG_FILE_DELETE");
                    Message reObserveMsg = Message.obtain(this, 1, msg.obj);
                    reObserveMsg.arg1 = msg.arg1;
                    sendMessageDelayed(reObserveMsg, 4000);
                    return;
                case ProcessList.UNKNOWN_ADJ /*1001*/:
                    Log.d(ConfigurationManagerImpl.TAG, "MSG_CONFIG_FILE_CLOSE_WRITE");
                    ConfigurationManagerImpl.this.reparseSystemConfigFile(msg.obj, msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    private List<Object> parseSystemConfigFile(String filePath, int type) {
        ConfigFileRecord record;
        File file = new File(filePath);
        if (!file.exists()) {
            Log.d(TAG, "system config file is not existed, we create the default system config file");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HashMap tmp = null;
        switch (type) {
            case 1:
                tmp = this.mSwitchConfigFileMap;
                break;
            case 2:
                tmp = this.mStringListConfigFileMap;
                break;
            case 3:
                tmp = this.mContentValuesListConfigFileMap;
                break;
        }
        synchronized (tmp) {
            record = (ConfigFileRecord) tmp.get(filePath);
        }
        List<Object> list = null;
        long beginTime = System.currentTimeMillis();
        switch (type) {
            case 1:
                list = ListConvertHelper.convertSwitchList2ObjectList(XmlPullParserHelper.getSwitchListFromFile(filePath));
                break;
            case 2:
                list = ListConvertHelper.convertStringList2ObjectList(XmlPullParserHelper.getStringListFromFile(filePath));
                break;
            case 3:
                list = ListConvertHelper.convertContentValuesList2ObjectList(XmlPullParserHelper.getContentValuesListFromFile(filePath));
                break;
        }
        Log.d(TAG, "parse config file " + filePath + " costs " + (System.currentTimeMillis() - beginTime) + "ms!!!");
        if (record == null) {
            record = new ConfigFileRecord(filePath, type, list);
            record.startObserverConfigFile(this.mMainHandler);
        } else {
            record.setConfigList(list);
        }
        synchronized (tmp) {
            tmp.put(filePath, record);
        }
        return list;
    }

    private void reparseSystemConfigFile(String filePath, int type) {
        ConfigFileRecord record;
        Log.d(TAG, "reparseSystemConfigFile filePath=" + filePath + " type=" + type);
        parseSystemConfigFile(filePath, type);
        HashMap tmp = null;
        switch (type) {
            case 1:
                tmp = this.mSwitchConfigFileMap;
                break;
            case 2:
                tmp = this.mStringListConfigFileMap;
                break;
            case 3:
                tmp = this.mContentValuesListConfigFileMap;
                break;
        }
        synchronized (tmp) {
            record = (ConfigFileRecord) tmp.get(filePath);
        }
        if (record != null) {
            record.dispatchConfigChanged();
        }
    }

    private ConfigurationManagerImpl(Context context) {
        Log.d(TAG, "ConfigurationManagerImpl");
        this.mContext = context;
        this.mMainHandlerThread = new HandlerThread(TAG);
        this.mMainHandlerThread.start();
        this.mMainHandler = new MainHandler(this.mMainHandlerThread.getLooper());
    }

    public static synchronized ConfigurationManagerImpl initConfigurationManagerImpl(Context context) {
        ConfigurationManagerImpl configurationManagerImpl;
        synchronized (ConfigurationManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new ConfigurationManagerImpl(context);
            }
            configurationManagerImpl = sInstance;
        }
        return configurationManagerImpl;
    }

    public static synchronized ConfigurationManagerImpl getInstance() {
        ConfigurationManagerImpl configurationManagerImpl;
        synchronized (ConfigurationManagerImpl.class) {
            configurationManagerImpl = sInstance;
        }
        return configurationManagerImpl;
    }

    private Switch getSwitchByName(String file, String name) {
        ConfigFileRecord record;
        synchronized (this.mSwitchConfigFileMap) {
            record = (ConfigFileRecord) this.mSwitchConfigFileMap.get(file);
        }
        if (record == null) {
            parseSystemConfigFile(file, 1);
        }
        synchronized (this.mSwitchConfigFileMap) {
            record = (ConfigFileRecord) this.mSwitchConfigFileMap.get(file);
        }
        if (record == null) {
            return null;
        }
        List<Switch> list = ListConvertHelper.convertObjectList2SwitchList(record.configs);
        synchronized (record) {
            for (Switch w : list) {
                if (name.equals(w.getName())) {
                    return w;
                }
            }
            return new Switch(name, file, true);
        }
    }

    private StringList getStringListByName(String file, String name) {
        ConfigFileRecord record;
        synchronized (this.mStringListConfigFileMap) {
            record = (ConfigFileRecord) this.mStringListConfigFileMap.get(file);
        }
        if (record == null) {
            parseSystemConfigFile(file, 2);
        }
        synchronized (this.mStringListConfigFileMap) {
            record = (ConfigFileRecord) this.mStringListConfigFileMap.get(file);
        }
        if (record == null) {
            return null;
        }
        List<StringList> list = ListConvertHelper.convertObjectList2StringList(record.configs);
        synchronized (record) {
            for (StringList w : list) {
                if (name.equals(w.getName())) {
                    return w;
                }
            }
            return new StringList(name, file, true);
        }
    }

    private ContentValuesList getContentValuesListByName(String file, String name) {
        ConfigFileRecord record;
        synchronized (this.mContentValuesListConfigFileMap) {
            record = (ConfigFileRecord) this.mContentValuesListConfigFileMap.get(file);
        }
        if (record == null) {
            parseSystemConfigFile(file, 3);
        }
        synchronized (this.mContentValuesListConfigFileMap) {
            record = (ConfigFileRecord) this.mContentValuesListConfigFileMap.get(file);
        }
        if (record == null) {
            return null;
        }
        List<ContentValuesList> list = ListConvertHelper.convertObjectList2ContentValuesList(record.configs);
        synchronized (record) {
            for (ContentValuesList w : list) {
                if (name.equals(w.getName())) {
                    return w;
                }
            }
            return new ContentValuesList(name, file, true);
        }
    }

    public Switch getSwitch(String switchName, String fileName) {
        Log.d(TAG, "getSwitch switchName=" + switchName + " fileName=" + fileName);
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(switchName)) {
            return null;
        }
        return getSwitchByName(fileName, switchName);
    }

    public boolean registerSwitchObserver(Switch w, IConfigChangeCallback callback) {
        Log.d(TAG, "registerSwitchObserver switch=" + w + " callback=" + callback);
        if (callback == null || (w.isInvalidSwitch() ^ 1) == 0) {
            return false;
        }
        ConfigFileRecord record;
        synchronized (this.mSwitchConfigFileMap) {
            record = (ConfigFileRecord) this.mSwitchConfigFileMap.get(w.getConfigFilePath());
        }
        if (record != null) {
            return record.addConfigChangeCallback(callback, w.getName());
        }
        return false;
    }

    public void unregisterSwitchObserver(Switch w, IConfigChangeCallback callback) {
        Log.d(TAG, "unregisterSwitchObserver switch=" + w + " callback=" + callback);
        if (callback != null && (w.isInvalidSwitch() ^ 1) != 0) {
            ConfigFileRecord record;
            synchronized (this.mSwitchConfigFileMap) {
                record = (ConfigFileRecord) this.mSwitchConfigFileMap.get(w.getConfigFilePath());
            }
            if (record != null) {
                record.removeConfigChangeCallback(callback, w.getName());
            }
        }
    }

    public StringList getStringList(String name, String fileName) {
        Log.d(TAG, "getStringList name=" + name + " fileName=" + fileName);
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(name)) {
            return null;
        }
        return getStringListByName(fileName, name);
    }

    public boolean registerStringListObserver(StringList list, IConfigChangeCallback callback) {
        Log.d(TAG, "registerStringListObserver list=" + list + " callback=" + callback);
        if (callback == null || (list.isInvalidList() ^ 1) == 0) {
            return false;
        }
        ConfigFileRecord record;
        synchronized (this.mStringListConfigFileMap) {
            record = (ConfigFileRecord) this.mStringListConfigFileMap.get(list.getConfigFilePath());
        }
        if (record != null) {
            return record.addConfigChangeCallback(callback, list.getName());
        }
        return false;
    }

    public void unregisterStringListObserver(StringList list, IConfigChangeCallback callback) {
        Log.d(TAG, "unregisterStringListObserver list=" + list + " callback=" + callback);
        if (callback != null && (list.isInvalidList() ^ 1) != 0) {
            ConfigFileRecord record;
            synchronized (this.mStringListConfigFileMap) {
                record = (ConfigFileRecord) this.mStringListConfigFileMap.get(list.getConfigFilePath());
            }
            if (record != null) {
                record.removeConfigChangeCallback(callback, list.getName());
            }
        }
    }

    public ContentValuesList getContentValuesList(String name, String fileName) {
        Log.d(TAG, "getContentValuesList name=" + name + " fileName=" + fileName);
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(name)) {
            return null;
        }
        return getContentValuesListByName(fileName, name);
    }

    public boolean registerContentValuesListObserver(ContentValuesList list, IConfigChangeCallback callback) {
        Log.d(TAG, "registerContentValuesListObserver list=" + list + " callback=" + callback);
        if (callback == null || (list.isInvalidList() ^ 1) == 0) {
            return false;
        }
        ConfigFileRecord record;
        synchronized (this.mContentValuesListConfigFileMap) {
            record = (ConfigFileRecord) this.mContentValuesListConfigFileMap.get(list.getConfigFilePath());
        }
        if (record != null) {
            return record.addConfigChangeCallback(callback, list.getName());
        }
        return false;
    }

    public void unregisterContentValuesListObserver(ContentValuesList list, IConfigChangeCallback callback) {
        Log.d(TAG, "unregisterContentValuesListObserver list=" + list + " callback=" + callback);
        if (callback != null && (list.isInvalidList() ^ 1) != 0) {
            ConfigFileRecord record;
            synchronized (this.mContentValuesListConfigFileMap) {
                record = (ConfigFileRecord) this.mContentValuesListConfigFileMap.get(list.getConfigFilePath());
            }
            if (record != null) {
                record.removeConfigChangeCallback(callback, list.getName());
            }
        }
    }

    public void dump(PrintWriter pw) {
        pw.println("Configuration Status:");
        pw.println("switch configs:");
        synchronized (this.mSwitchConfigFileMap) {
            for (String key : this.mSwitchConfigFileMap.keySet()) {
                ((ConfigFileRecord) this.mSwitchConfigFileMap.get(key)).dump(pw);
            }
        }
        pw.println("********************************************************");
        pw.println("stringlist configs:");
        synchronized (this.mStringListConfigFileMap) {
            for (String key2 : this.mStringListConfigFileMap.keySet()) {
                ((ConfigFileRecord) this.mStringListConfigFileMap.get(key2)).dump(pw);
            }
        }
        pw.println("********************************************************");
        pw.println("contentvalues configs:");
        synchronized (this.mContentValuesListConfigFileMap) {
            for (String key22 : this.mContentValuesListConfigFileMap.keySet()) {
                ((ConfigFileRecord) this.mContentValuesListConfigFileMap.get(key22)).dump(pw);
            }
        }
        pw.println("********************************************************");
    }
}
