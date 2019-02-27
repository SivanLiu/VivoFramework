package com.vivo.services.epm.config;

import android.util.Log;

public class DefaultConfigurationManager {
    private static final String DEFAULT_EPM_POLICY_CONTENTVALUESLIST = "/data/bbkcore/default_epm_policy_cvlist.xml";
    private static final String DEFAULT_EPM_POLICY_STRINGLIST = "/data/bbkcore/default_epm_policy_stringlist.xml";
    private static final String DEFAULT_EPM_POLICY_SWITCH = "/data/bbkcore/default_epm_policy_switch.xml";
    private static final String TAG = "DefaultConfigurationManager";
    private static DefaultConfigurationManager sInstance = null;
    private static ConfigurationManager sService = null;

    private DefaultConfigurationManager() {
        sService = getConfigurationManager();
    }

    public static synchronized DefaultConfigurationManager getInstance() {
        DefaultConfigurationManager defaultConfigurationManager;
        synchronized (DefaultConfigurationManager.class) {
            if (sInstance == null) {
                sInstance = new DefaultConfigurationManager();
            }
            defaultConfigurationManager = sInstance;
        }
        return defaultConfigurationManager;
    }

    private static ConfigurationManager getConfigurationManager() {
        if (sService != null) {
            return sService;
        }
        sService = ConfigurationManager.getInstance();
        return sService;
    }

    public Switch getSwitch(String switchName) {
        ConfigurationManager service = getConfigurationManager();
        if (service == null) {
            return null;
        }
        try {
            return service.getSwitch(DEFAULT_EPM_POLICY_SWITCH, switchName);
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
        ConfigurationManager service = getConfigurationManager();
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
        ConfigurationManager service = getConfigurationManager();
        if (service != null) {
            try {
                service.unregisterSwitchObserver(w, observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterSwitchObserver", e);
            }
        }
    }

    public StringList getStringList(String name) {
        ConfigurationManager service = getConfigurationManager();
        if (service == null) {
            return null;
        }
        try {
            return service.getStringList(DEFAULT_EPM_POLICY_STRINGLIST, name);
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
        ConfigurationManager service = getConfigurationManager();
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
        ConfigurationManager service = getConfigurationManager();
        if (service != null) {
            try {
                service.unregisterStringListObserver(list.makeMiniCopy(), observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterStringListObserver", e);
            }
        }
    }

    public ContentValuesList getContentValuesList(String name) {
        ConfigurationManager service = getConfigurationManager();
        if (service == null) {
            return null;
        }
        try {
            return service.getContentValuesList(DEFAULT_EPM_POLICY_CONTENTVALUESLIST, name);
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
        ConfigurationManager service = getConfigurationManager();
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
        ConfigurationManager service = getConfigurationManager();
        if (service != null) {
            try {
                service.unregisterContentValuesListObserver(list.makeMiniCopy(), observer);
            } catch (Exception e) {
                Log.e(TAG, "Exception in unregisterContentValuesListObserver", e);
            }
        }
    }
}
