package com.vivo.services.cust.server;

import android.util.Log;
import android.util.Xml;
import com.vivo.services.cust.server.Utils.DynamicRecord;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CustomConfigInfo {
    private static final String TAG = "VCS";
    private static CustomConfigInfo sInstance = new CustomConfigInfo();
    private List<String> mAccessibilityServcieList = new ArrayList();
    private List<String> mActivedAdminList = new ArrayList();
    private List<String> mAllEmmPackageList = new ArrayList();
    private List<String> mAppOpPermList = new ArrayList(Arrays.asList(new String[]{"23", "24", "43"}));
    private List<String> mDataEmmPackageList = new ArrayList();
    private String mDeviceOwner = null;
    private List<String> mDisabledAppList = new ArrayList();
    private List<DynamicRecord> mDynamicRecordList = new ArrayList();
    private List<String> mNotificationListenerList = new ArrayList();
    private List<String> mPackagePermList = new ArrayList(Arrays.asList(new String[]{"android.permission.WRITE_APN_SETTINGS", "android.Manifest.permission.MODIFY_PHONE_STATE", "android.permission.CAPTURE_AUDIO_OUTPUT", "android.permission.PACKAGE_USAGE_STATS"}));
    private List<DynamicRecord> mSetOpRecordList = new ArrayList();
    private List<String> mSystemEmmPackageList = new ArrayList();
    private List<String> mTrustedAppList = new ArrayList();

    public static CustomConfigInfo getDefault() {
        return sInstance;
    }

    private CustomConfigInfo() {
        loadConfigInfo();
    }

    public void loadConfigInfo() {
        loadConfigFromCode();
        parseConfigFromXml();
    }

    private boolean loadConfigFromCode() {
        boolean result;
        boolean settingResult = initSecureSetting();
        boolean accessibilityResult = initAccessibilityServcies();
        boolean notificationResult = initNotificationListeners();
        boolean trustedResult = initTrustedApps();
        boolean disabledResult = initDisabledApps();
        boolean adminResult = initActiveAdmin();
        boolean ownerResult = initDeviceOwner();
        boolean opResult = initAppOpPerm();
        boolean permResult = initPackagePerm();
        boolean packResult = initEmmPackages();
        if (settingResult && accessibilityResult && notificationResult && notificationResult && trustedResult && disabledResult && adminResult && ownerResult && opResult && permResult) {
            result = packResult;
        } else {
            result = false;
        }
        Log.i(TAG, "loadConfigFromCode result : " + result);
        return result;
    }

    private boolean initSecureSetting() {
        return true;
    }

    private boolean initAccessibilityServcies() {
        return true;
    }

    private boolean initNotificationListeners() {
        return true;
    }

    private boolean initTrustedApps() {
        this.mTrustedAppList.add("com.android.VideoPlayer");
        this.mTrustedAppList.add("com.bbk.theme");
        return true;
    }

    private boolean initDisabledApps() {
        return true;
    }

    private boolean initActiveAdmin() {
        return true;
    }

    private boolean initDeviceOwner() {
        return true;
    }

    private boolean initAppOpPerm() {
        return true;
    }

    private boolean initPackagePerm() {
        return true;
    }

    private boolean initEmmPackages() {
        this.mAllEmmPackageList.add("com.sct.emmDemo");
        this.mDataEmmPackageList.add("com.sct.emmDemo");
        this.mAllEmmPackageList.add("com.vivo.emmTools");
        this.mDataEmmPackageList.add("com.vivo.emmTools");
        return true;
    }

    private boolean parseAccessibilityServcie(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String componentName = parser.nextText();
        Log.i(TAG, "parseAccessibilityServcie : componentName = " + componentName + ", action = " + action);
        if (componentName == null || "".equals(componentName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mAccessibilityServcieList.remove(componentName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mAccessibilityServcieList.clear();
            }
            this.mAccessibilityServcieList.add(componentName);
        }
        return true;
    }

    private boolean parseNotificationListener(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String componentName = parser.nextText();
        Log.i(TAG, "parseNotificationListener : componentName = " + componentName + ", action = " + action);
        if (componentName == null || "".equals(componentName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mNotificationListenerList.remove(componentName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mNotificationListenerList.clear();
            }
            this.mNotificationListenerList.add(componentName);
        }
        return true;
    }

    private boolean parseTrustedApp(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String packageName = parser.nextText();
        Log.i(TAG, "parseTrustedApp : packageName = " + packageName + ", action = " + action);
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mTrustedAppList.remove(packageName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mTrustedAppList.clear();
            }
            if (!this.mTrustedAppList.contains(packageName)) {
                this.mTrustedAppList.add(packageName);
            }
        }
        return true;
    }

    private boolean parseDisabledApp(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String packageName = parser.nextText();
        Log.i(TAG, "parseDisabledApp : packageName = " + packageName + ", action = " + action);
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mDisabledAppList.remove(packageName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mDisabledAppList.clear();
            }
            this.mDisabledAppList.add(packageName);
        }
        return true;
    }

    private boolean parseDeviceOwner(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String componentName = parser.nextText();
        Log.i(TAG, "parseDeviceOwner : componentName = " + componentName + ", action = " + action);
        if (componentName == null || "".equals(componentName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_UPDATE;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            if (this.mDeviceOwner != null && this.mDeviceOwner.equals(componentName)) {
                this.mDeviceOwner = null;
            }
        } else if (!Utils.ACTION_ADD.equals(action)) {
            this.mDeviceOwner = componentName;
        } else if (this.mDeviceOwner != null) {
            return false;
        } else {
            this.mDeviceOwner = componentName;
        }
        return true;
    }

    private boolean parseActivedAdmin(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String componentName = parser.nextText();
        Log.i(TAG, "parseActivedAdmin : componentName = " + componentName + ", action = " + action);
        if (componentName == null || "".equals(componentName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mActivedAdminList.remove(componentName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mActivedAdminList.clear();
            }
            this.mActivedAdminList.add(componentName);
        }
        return true;
    }

    private boolean parseOpPermission(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String opCodeStr = parser.nextText();
        Log.i(TAG, "parseOpPermission : opCodeStr = " + opCodeStr + ", action = " + action);
        if (opCodeStr == null || "".equals(opCodeStr)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mAppOpPermList.remove(opCodeStr);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mAppOpPermList.clear();
            }
            this.mAppOpPermList.add(opCodeStr);
        }
        return true;
    }

    private boolean parsePackagePermission(XmlPullParser parser) throws XmlPullParserException, IOException {
        String action = parser.getAttributeValue(null, "action");
        String permName = parser.nextText();
        Log.i(TAG, "parsePackagePermission : permName = " + permName + ", action = " + action);
        if (permName == null || "".equals(permName)) {
            return false;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            this.mPackagePermList.remove(permName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mPackagePermList.clear();
            }
            this.mPackagePermList.add(permName);
        }
        return true;
    }

    private boolean parseSetupOpRecord(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packName = parser.getAttributeValue(null, "packName");
        String codeStr = parser.getAttributeValue(null, "opCode");
        String action = parser.getAttributeValue(null, "action");
        String mode = parser.nextText();
        Log.i(TAG, "parseSetupOpRecord : packName = " + packName + ", action = " + action + ", codeStr = " + codeStr + ", mode = " + mode);
        if (mode == null || "".equals(mode) || packName == null || "".equals(packName)) {
            return false;
        }
        try {
            int code = Integer.parseInt(codeStr);
            if (action == null || "".equals(action)) {
                action = Utils.ACTION_UPDATE;
            }
            this.mSetOpRecordList.add(new DynamicRecord(packName, "setOp", "default", action, mode, code));
            return true;
        } catch (Exception e) {
            Log.w(TAG, "parseSetupOpRecord : parseInt fail", e);
            return false;
        }
    }

    private boolean parseDynamicRecord(XmlPullParser parser) throws XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, "name");
        String type = parser.getAttributeValue(null, "type");
        String namespace = parser.getAttributeValue(null, "namespace");
        String action = parser.getAttributeValue(null, "action");
        String valueType = parser.getAttributeValue(null, "valuetype");
        String valueStr = parser.nextText();
        int value = 0;
        Log.i(TAG, "parseDynamicRecord : name = " + name + ", action = " + action + ", type = " + type + ", namespace = " + namespace + ", valueStr = " + valueStr);
        if (valueStr == null || "".equals(valueStr) || name == null || "".equals(name)) {
            return false;
        }
        DynamicRecord record;
        if (type == null || "".equals(type)) {
            type = Utils.TYPE_SETTING_DATABASE;
        }
        if (namespace == null || "".equals(namespace)) {
            namespace = Utils.DATABASE_NAMESPACE_SECURE;
        }
        if (action == null || "".equals(action)) {
            action = Utils.ACTION_ADD;
        }
        if (valueType == null || "".equals(valueType) || Utils.VALUE_MODE_INT.equals(valueType)) {
            valueType = Utils.VALUE_MODE_INT;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                Log.w(TAG, "parseDynamicRecord : parseInt failed", e);
                valueType = Utils.VALUE_MODE_STRING;
            }
        }
        if (Utils.VALUE_MODE_INT.equals(valueType)) {
            record = new DynamicRecord(name, type, namespace, action, value);
        } else {
            DynamicRecord dynamicRecord = new DynamicRecord(name, type, namespace, action, valueStr);
        }
        this.mDynamicRecordList.add(record);
        return true;
    }

    private boolean parseEmmPackage(XmlPullParser parser) throws XmlPullParserException, IOException {
        String type = parser.getAttributeValue(null, "type");
        String action = parser.getAttributeValue(null, "action");
        String packageName = parser.nextText();
        Log.i(TAG, "parseEmmPackage : packageName = " + packageName + ", type = " + type + ", action = " + action);
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        if (Utils.ACTION_DELETE.equals(action)) {
            if (Utils.DATABASE_NAMESPACE_SYSTEM.equals(type)) {
                this.mSystemEmmPackageList.remove(packageName);
            } else {
                this.mDataEmmPackageList.remove(packageName);
            }
            this.mAllEmmPackageList.remove(packageName);
        } else {
            if (Utils.ACTION_UPDATE.equals(action)) {
                this.mSystemEmmPackageList.clear();
                this.mDataEmmPackageList.clear();
                this.mAllEmmPackageList.clear();
            }
            if (Utils.DATABASE_NAMESPACE_SYSTEM.equals(type) && (this.mSystemEmmPackageList.contains(packageName) ^ 1) != 0) {
                this.mSystemEmmPackageList.add(packageName);
            } else if (!this.mDataEmmPackageList.contains(packageName)) {
                this.mDataEmmPackageList.add(packageName);
            }
            if (!this.mAllEmmPackageList.contains(packageName)) {
                this.mAllEmmPackageList.add(packageName);
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x007b A:{Splitter: B:9:0x0025, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c9 A:{SYNTHETIC, Splitter: B:47:0x00c9} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0088 A:{SYNTHETIC, Splitter: B:22:0x0088} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0164 A:{Splitter: B:7:0x0020, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00af A:{SYNTHETIC, Splitter: B:36:0x00af} */
    /* JADX WARNING: Missing block: B:17:0x007b, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:18:0x007c, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            android.util.Log.w(TAG, "parseInfoFromXml failed :", r2);
     */
    /* JADX WARNING: Missing block: B:21:0x0086, code:
            if (r6 != null) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:80:0x014a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:81:0x014b, code:
            r1.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:82:0x0150, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:83:0x0151, code:
            r1.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:85:0x0161, code:
            r8 = th;
     */
    /* JADX WARNING: Missing block: B:86:0x0164, code:
            r2 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseConfigFromXml() {
        Exception e;
        File configFile = new File(Utils.GN_CONFIG_FILE);
        if (configFile == null || !configFile.exists() || 0 == configFile.length()) {
            Log.w(TAG, "parseInfoFromXml file not exists or is empty");
        } else {
            XmlPullParser parser = Xml.newPullParser();
            FileReader fileReader = null;
            try {
                FileReader reader = new FileReader(configFile);
                try {
                    parser.setInput(reader);
                    int eventType = parser.getEventType();
                    Log.i(TAG, "parseXml : eventType = " + eventType);
                    while (eventType != 1) {
                        switch (eventType) {
                            case 2:
                                String name = parser.getName();
                                Log.i(TAG, "parseXml START_TAG : name = " + name);
                                if (!"package".equalsIgnoreCase(name)) {
                                    if (!"database".equalsIgnoreCase(name) && !"property".equalsIgnoreCase(name)) {
                                        if (!"setupOp".equalsIgnoreCase(name)) {
                                            if (!"packagePerm".equalsIgnoreCase(name)) {
                                                if (!"appOp".equalsIgnoreCase(name)) {
                                                    if (!"deviceOwner".equalsIgnoreCase(name)) {
                                                        if (!"activedAdmin".equalsIgnoreCase(name)) {
                                                            if (!"disabledApp".equalsIgnoreCase(name)) {
                                                                if (!"trustedApp".equalsIgnoreCase(name)) {
                                                                    if (!"notificationListener".equalsIgnoreCase(name)) {
                                                                        if (!"accessibilityServcie".equalsIgnoreCase(name)) {
                                                                            break;
                                                                        }
                                                                        parseAccessibilityServcie(parser);
                                                                        break;
                                                                    }
                                                                    parseNotificationListener(parser);
                                                                    break;
                                                                }
                                                                parseTrustedApp(parser);
                                                                break;
                                                            }
                                                            parseDisabledApp(parser);
                                                            break;
                                                        }
                                                        parseActivedAdmin(parser);
                                                        break;
                                                    }
                                                    parseDeviceOwner(parser);
                                                    break;
                                                }
                                                parseOpPermission(parser);
                                                break;
                                            }
                                            parsePackagePermission(parser);
                                            break;
                                        }
                                        parseSetupOpRecord(parser);
                                        break;
                                    }
                                    parseDynamicRecord(parser);
                                    break;
                                }
                                parseEmmPackage(parser);
                                break;
                            default:
                                break;
                        }
                        eventType = parser.next();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e3) {
                } catch (Exception e4) {
                    e = e4;
                    fileReader = reader;
                    Log.w(TAG, "parseInfoFromXml failed :", e);
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return true;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    fileReader = reader;
                    if (fileReader != null) {
                    }
                    throw th2;
                }
            } catch (XmlPullParserException e5) {
            } catch (Exception e6) {
                e = e6;
                Log.w(TAG, "parseInfoFromXml failed :", e);
                if (fileReader != null) {
                }
                return true;
            }
        }
        return true;
    }

    public List<DynamicRecord> getDefaultSetting() {
        return this.mDynamicRecordList;
    }

    public List<String> getCustomizedApps(int type) {
        if (type == 1) {
            return this.mSystemEmmPackageList;
        }
        if (type == 2) {
            return this.mDataEmmPackageList;
        }
        return this.mAllEmmPackageList;
    }

    public List<String> getByPassPermissions() {
        return this.mPackagePermList;
    }

    public List<String> getByPassOps() {
        return this.mAppOpPermList;
    }

    public String getDefaultDeviceOwner() {
        return this.mDeviceOwner;
    }

    public List<String> getDefaultDisabledApps() {
        return this.mDisabledAppList;
    }

    public List<String> getDefaultTrustedAppStores() {
        return this.mTrustedAppList;
    }

    public List<String> getDefaultNotificationListeners() {
        return this.mNotificationListenerList;
    }

    public List<String> getDefaultActivedAdmins() {
        return this.mActivedAdminList;
    }

    public List<String> getDefaultActivedAccessibilityServcies() {
        return this.mAccessibilityServcieList;
    }

    public List<DynamicRecord> getDefaultAllowOps() {
        return this.mSetOpRecordList;
    }
}
