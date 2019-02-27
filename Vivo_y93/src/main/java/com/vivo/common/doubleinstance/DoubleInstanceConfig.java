package com.vivo.common.doubleinstance;

import android.util.Log;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public final class DoubleInstanceConfig {
    private static final String ACTIVITY_WITHOUT_CHOOSER = "ActivityWithoutChooser";
    private static final String ACTIVITY_WITH_CHOOSER = "ActivityWithChooser";
    private static final String BROADCAST_TO_DOUBLE_USER = "BroadcastToDoubleUser";
    private static final String BROADCAST_TO_OWNER_USER = "BroadcastToOwnerUser";
    private static final String DATABASE_AUTH = "DatabaseAuth";
    private static final String INSTANCE_SWITCHER = "InstanceSwitcher";
    private static final String PACKAGE_WITHOUT_CHOOSER = "PackageWithoutChooser";
    private static final String QUERY_MATCHED_INTENT_ACTION = "QueryMatchedIntentAction";
    private static final String QUERY_MATCHED_INTENT_SCHEME = "QueryMatchedIntentScheme";
    private static final String SETTINGS_USE_OWNER_USER = "SettingsUseOwnerUser";
    private static final String SHARE_ACTION_WITHOUT_CHOOSER = "ActionWithoutChooser";
    private static final String SUPPOERTED_APP_PACKAGE_NAME = "SupportedAppPackageName";
    private static final String SYSTEM_APP_IN_DOUBLE_USER = "SystemAppInDoubleUser";
    private static final String TAG = "DoubleInstanceConfig";
    public static final int TYPE_ACTION_WITHOUT_CHOOSER = 13;
    public static final int TYPE_ACTIVITY_WITHOUT_CHOOSER = 2;
    public static final int TYPE_ACTIVITY_WITH_CHOOSER = 4;
    public static final int TYPE_BROADCAST_TO_DOUBLE_USER = 9;
    public static final int TYPE_BROADCAST_TO_OWNER_USER = 8;
    public static final int TYPE_DATABASE_AUTH = 6;
    public static final int TYPE_INSTANCE_SWITCHER = 5;
    public static final int TYPE_PACKAGE_WITHOUT_CHOOSER = 3;
    public static final int TYPE_QUERY_MATCHED_INTENT_ACTION = 11;
    public static final int TYPE_QUERY_MATCHED_INTENT_SCHEME = 12;
    public static final int TYPE_SETTINGS_USE_OWNER_USER = 10;
    public static final int TYPE_SUPPOERTED_APP_PACKAGE_NAME = 1;
    public static final int TYPE_SYSTEM_APP_IN_DOUBLE_USER = 7;
    private static DoubleInstanceConfig sDoubleInstanceConfig;
    private ArrayList<String> mActionWihoutChooser = new ArrayList();
    private ArrayList<String> mActivityWithChooser = new ArrayList();
    private ArrayList<String> mActivityWithoutChooser = new ArrayList();
    private ArrayList<String> mBroadcastToDoubleUser = new ArrayList();
    private ArrayList<String> mBroadcastToOwnerUser = new ArrayList();
    private ArrayList<String> mDatabaseAuth = new ArrayList();
    private boolean mDoubleInstanceEnabled = false;
    private ArrayList<String> mInstanceSwitcher = new ArrayList();
    private ArrayList<String> mMatchedIntentActions = new ArrayList();
    private ArrayList<String> mMatchedIntentSchemes = new ArrayList();
    private ArrayList<String> mPackageWithoutChooser = new ArrayList();
    private ArrayList<String> mSettingsUseOwnerUser = new ArrayList();
    private ArrayList<String> mSupportedAppPackageName = new ArrayList();
    private ArrayList<String> mSystemAppInDoubleUser = new ArrayList();

    public static DoubleInstanceConfig getInstance() {
        if (sDoubleInstanceConfig == null) {
            synchronized (DoubleInstanceConfig.class) {
                if (sDoubleInstanceConfig == null) {
                    sDoubleInstanceConfig = new DoubleInstanceConfig();
                }
            }
        }
        return sDoubleInstanceConfig;
    }

    private DoubleInstanceConfig() {
    }

    public void setDoubleInstanceConfig(boolean enabled, Map<String, ArrayList<String>> map) {
        Log.d(TAG, "setDoubleInstanceConfig enabled:" + enabled);
        this.mDoubleInstanceEnabled = enabled;
        for (Entry<String, ArrayList<String>> entry : map.entrySet()) {
            String str = (String) entry.getKey();
            if (str.equals(SUPPOERTED_APP_PACKAGE_NAME)) {
                this.mSupportedAppPackageName.clear();
                this.mSupportedAppPackageName.addAll((ArrayList) entry.getValue());
            } else if (str.equals(ACTIVITY_WITHOUT_CHOOSER)) {
                this.mActivityWithoutChooser.clear();
                this.mActivityWithoutChooser.addAll((ArrayList) entry.getValue());
            } else if (str.equals(PACKAGE_WITHOUT_CHOOSER)) {
                this.mPackageWithoutChooser.clear();
                this.mPackageWithoutChooser.addAll((ArrayList) entry.getValue());
            } else if (str.equals(ACTIVITY_WITH_CHOOSER)) {
                this.mActivityWithChooser.clear();
                this.mActivityWithChooser.addAll((ArrayList) entry.getValue());
            } else if (str.equals(INSTANCE_SWITCHER)) {
                this.mInstanceSwitcher.clear();
                this.mInstanceSwitcher.addAll((ArrayList) entry.getValue());
            } else if (str.equals(DATABASE_AUTH)) {
                this.mDatabaseAuth.clear();
                this.mDatabaseAuth.addAll((ArrayList) entry.getValue());
            } else if (str.equals(SYSTEM_APP_IN_DOUBLE_USER)) {
                this.mSystemAppInDoubleUser.clear();
                this.mSystemAppInDoubleUser.addAll((ArrayList) entry.getValue());
            } else if (str.equals(BROADCAST_TO_OWNER_USER)) {
                this.mBroadcastToOwnerUser.clear();
                this.mBroadcastToOwnerUser.addAll((ArrayList) entry.getValue());
            } else {
                if (str.equals(BROADCAST_TO_DOUBLE_USER)) {
                    this.mBroadcastToDoubleUser.clear();
                    this.mBroadcastToDoubleUser.addAll((ArrayList) entry.getValue());
                } else if (!str.equals(SETTINGS_USE_OWNER_USER)) {
                    if (!str.equals(QUERY_MATCHED_INTENT_ACTION)) {
                        if (str.equals(QUERY_MATCHED_INTENT_SCHEME)) {
                            this.mMatchedIntentSchemes.clear();
                            this.mMatchedIntentSchemes.addAll((ArrayList) entry.getValue());
                        } else if (str.equals(SHARE_ACTION_WITHOUT_CHOOSER)) {
                            this.mActionWihoutChooser.clear();
                            this.mActionWihoutChooser.addAll((ArrayList) entry.getValue());
                        }
                    }
                    this.mMatchedIntentActions.clear();
                    this.mMatchedIntentActions.addAll((ArrayList) entry.getValue());
                }
                this.mSettingsUseOwnerUser.clear();
                this.mSettingsUseOwnerUser.addAll((ArrayList) entry.getValue());
                this.mMatchedIntentActions.clear();
                this.mMatchedIntentActions.addAll((ArrayList) entry.getValue());
            }
        }
    }

    public ArrayList<String> getDoubleInstanceConfig(int type) {
        switch (type) {
            case 1:
                return this.mSupportedAppPackageName;
            case 2:
                return this.mActivityWithoutChooser;
            case 3:
                return this.mPackageWithoutChooser;
            case 4:
                return this.mActivityWithChooser;
            case 5:
                return this.mInstanceSwitcher;
            case 6:
                return this.mDatabaseAuth;
            case 7:
                return this.mSystemAppInDoubleUser;
            case 8:
                return this.mBroadcastToOwnerUser;
            case 9:
                return this.mBroadcastToDoubleUser;
            case 10:
                return this.mSettingsUseOwnerUser;
            case 11:
                return this.mMatchedIntentActions;
            case 12:
                return this.mMatchedIntentSchemes;
            case 13:
                return this.mActionWihoutChooser;
            default:
                return null;
        }
    }

    public ArrayList<String> getSupportedAppPackageName() {
        return this.mSupportedAppPackageName;
    }

    public ArrayList<String> getActivityWithoutChooser() {
        return this.mActivityWithoutChooser;
    }

    public ArrayList<String> getPackageWithoutChooser() {
        return this.mPackageWithoutChooser;
    }

    public ArrayList<String> getActivityWithChooser() {
        return this.mActivityWithChooser;
    }

    public ArrayList<String> getInstanceSwitcher() {
        return this.mInstanceSwitcher;
    }

    public ArrayList<String> getDatabaseAuth() {
        return this.mDatabaseAuth;
    }

    public ArrayList<String> getSystemAppInDoubleUser() {
        return this.mSystemAppInDoubleUser;
    }

    public ArrayList<String> getBroadcastToOwnerUser() {
        return this.mBroadcastToOwnerUser;
    }

    public ArrayList<String> getBroadcastToDoubleUser() {
        return this.mBroadcastToDoubleUser;
    }
}
