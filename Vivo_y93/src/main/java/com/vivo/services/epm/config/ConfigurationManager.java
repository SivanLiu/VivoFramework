package com.vivo.services.epm.config;

import android.util.Log;

public class ConfigurationManager {
    private static final String TAG = "ConfigurationManager";
    private static ConfigurationManager sInstance = null;
    private static IConfigurationManager sService = null;

    private ConfigurationManager() {
        sService = getServiceImpl();
    }

    public static synchronized ConfigurationManager getInstance() {
        ConfigurationManager configurationManager;
        synchronized (ConfigurationManager.class) {
            if (sInstance == null) {
                sInstance = new ConfigurationManager();
            }
            configurationManager = sInstance;
        }
        return configurationManager;
    }

    private static IConfigurationManager getServiceImpl() {
        if (sService != null) {
            return sService;
        }
        sService = ConfigurationManagerImpl.getInstance();
        return sService;
    }

    public Switch getSwitch(String fileName, String switchName) {
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return null;
        }
        try {
            return service.getSwitch(switchName, fileName);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getSwitch", e);
            return null;
        }
    }

    public boolean registerSwitchObserver(Switch w, ConfigurationObserver observer) {
        if (w == null || w.isInvalidSwitch() || observer == null) {
            Log.d(TAG, "invalid args");
            return false;
        }
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return false;
        }
        try {
            return service.registerSwitchObserver(w, observer);
        } catch (Exception e) {
            Log.e(TAG, "Exception in registerSwitchObserver", e);
            return false;
        }
    }

    public void unregisterSwitchObserver(Switch w, ConfigurationObserver observer) {
        if (w == null || w.isInvalidSwitch() || observer == null) {
            Log.d(TAG, "invalid args");
            return;
        }
        IConfigurationManager service = getServiceImpl();
        if (service != null) {
            try {
                service.unregisterSwitchObserver(w, observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterSwitchObserver", e);
            }
        }
    }

    public StringList getStringList(String fileName, String name) {
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return null;
        }
        try {
            return service.getStringList(name, fileName);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getStringList", e);
            return null;
        }
    }

    public boolean registerStringListObserver(StringList list, ConfigurationObserver observer) {
        if (list == null || list.isInvalidList() || observer == null) {
            Log.d(TAG, "invalid args");
            return false;
        }
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return false;
        }
        try {
            return service.registerStringListObserver(list.makeMiniCopy(), observer);
        } catch (Exception e) {
            Log.e(TAG, "Exception in registerStringListObserver", e);
            return false;
        }
    }

    public void unregisterStringListObserver(StringList list, ConfigurationObserver observer) {
        if (list == null || list.isInvalidList() || observer == null) {
            Log.d(TAG, "invalid args");
            return;
        }
        IConfigurationManager service = getServiceImpl();
        if (service != null) {
            try {
                service.unregisterStringListObserver(list.makeMiniCopy(), observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterStringListObserver", e);
            }
        }
    }

    public ContentValuesList getContentValuesList(String fileName, String name) {
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return null;
        }
        try {
            return service.getContentValuesList(name, fileName);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getContentValuesList", e);
            return null;
        }
    }

    public boolean registerContentValuesListObserver(ContentValuesList list, ConfigurationObserver observer) {
        if (list == null || list.isInvalidList() || observer == null) {
            Log.d(TAG, "invalid args");
            return false;
        }
        IConfigurationManager service = getServiceImpl();
        if (service == null) {
            return false;
        }
        try {
            return service.registerContentValuesListObserver(list.makeMiniCopy(), observer);
        } catch (Exception e) {
            Log.e(TAG, "Exception in registerContentValuesListObserver", e);
            return false;
        }
    }

    public void unregisterContentValuesListObserver(ContentValuesList list, ConfigurationObserver observer) {
        if (list == null || list.isInvalidList() || observer == null) {
            Log.d(TAG, "invalid args");
            return;
        }
        IConfigurationManager service = getServiceImpl();
        if (service != null) {
            try {
                service.unregisterContentValuesListObserver(list.makeMiniCopy(), observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterContentValuesListObserver", e);
            }
        }
    }
}
