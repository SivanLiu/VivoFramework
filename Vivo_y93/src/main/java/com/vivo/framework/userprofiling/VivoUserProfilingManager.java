package com.vivo.framework.userprofiling;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import vivo.app.userprofiling.AbsVivoUserProfilingManager;
import vivo.app.userprofiling.IVivoUserProfiling;
import vivo.app.userprofiling.IVivoUserProfiling.Stub;

public class VivoUserProfilingManager extends AbsVivoUserProfilingManager {
    public static final String SERVICE_NAME = "vivo_user_profile_service";
    private static final String SPLIT_COLON = ":";
    private static final String SPLIT_STR = "#";
    private static final String TAG = "VivoUserProfilingManager";
    private static Map<String, String> mAppConfigMap = new HashMap();
    private static boolean mCaptureOn = false;
    private static Context mContext = null;
    private static VivoUserProfilingManager sInstance = null;
    private static IVivoUserProfiling sService = null;

    private VivoUserProfilingManager() {
        sService = getService();
        if (sService != null) {
            getApplicationConfig();
            getCaptureSwitch();
            return;
        }
        Log.d(TAG, "service is null! ");
    }

    public static VivoUserProfilingManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoUserProfilingManager();
        }
        return sInstance;
    }

    private static IVivoUserProfiling getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(SERVICE_NAME);
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public void ping(String msg) {
        IVivoUserProfiling service = getService();
        if (service != null) {
            try {
                service.ping(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in ping", e);
            }
        }
    }

    public int reportDataToBee(String tableName, String values, String pkgName, String infoType) {
        IVivoUserProfiling service = getService();
        if (service == null) {
            Log.d(TAG, "service is null! ");
            return -1;
        }
        try {
            return service.reportDataToBee(tableName, values, pkgName, infoType);
        } catch (RemoteException e) {
            Log.e(TAG, "report exception! ");
            return -1;
        }
    }

    public void updateConfig() {
        if (getService() != null) {
            try {
                sService.updateConfig();
            } catch (RemoteException e) {
                Log.e(TAG, "updateConfig exception " + e);
            }
        }
    }

    private void getApplicationConfig() {
        if (getService() != null) {
            try {
                String applicationConfig = sService.getApplicationConfig();
                if (applicationConfig != null) {
                    JSONObject json = new JSONObject(applicationConfig);
                    if (json != null) {
                        Iterator<String> keys = json.keys();
                        if (mAppConfigMap != null) {
                            mAppConfigMap.clear();
                        } else {
                            mAppConfigMap = new HashMap();
                        }
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            mAppConfigMap.put(key, json.get(key).toString());
                        }
                    }
                }
                Log.d(TAG, "Config is " + applicationConfig);
            } catch (RemoteException e) {
                Log.e(TAG, "Config RemoteException " + e);
            } catch (JSONException e2) {
                Log.e(TAG, "Config JSONException " + e2);
            }
        }
    }

    public Map<String, String> getPropertiesMapByPagetype(String pkgName, String pageName, String pageType) {
        if (mAppConfigMap == null || mAppConfigMap.size() < 1) {
            return null;
        }
        Map<String, String> propertiesMap = new HashMap();
        for (Entry<String, String> entry : mAppConfigMap.entrySet()) {
            String value = (String) entry.getValue();
            if (((String) entry.getKey()).contains(pkgName + SPLIT_STR + pageName + SPLIT_STR + pageType)) {
                String[] pageProperties = splitString(value, SPLIT_STR);
                if (pageProperties == null) {
                    return null;
                }
                for (String prop : pageProperties) {
                    if (prop != null) {
                        String[] propValue = splitString(prop, SPLIT_COLON);
                        if (propValue != null && propValue.length == 2) {
                            propertiesMap.put(propValue[0], propValue[1]);
                        }
                    }
                }
                return propertiesMap;
            }
        }
        return propertiesMap;
    }

    private String[] splitString(String srcStr, String splitStr) {
        if (srcStr == null || splitStr == null) {
            return null;
        }
        String[] desStr = null;
        try {
            desStr = srcStr.split(splitStr);
        } catch (Exception e) {
            Log.e(TAG, "splitString:" + e.fillInStackTrace());
        }
        return desStr;
    }

    public ArrayList<String> getPageTypeList(String pkgName, String pageName) {
        if (mAppConfigMap == null || mAppConfigMap.size() < 1) {
            return null;
        }
        ArrayList<String> pageTypeList = new ArrayList();
        for (Entry<String, String> entry : mAppConfigMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.contains(pkgName + SPLIT_STR + pageName)) {
                String[] pageInfo = splitString(key, SPLIT_STR);
                if (pageInfo != null && pageInfo.length == 4) {
                    pageTypeList.add(pageInfo[2]);
                }
            }
        }
        return pageTypeList;
    }

    public String getInfoType(String pkgName, String pageName) {
        if (mAppConfigMap == null || mAppConfigMap.size() < 1) {
            return null;
        }
        String infoType = null;
        ArrayList<String> pageTypeList = new ArrayList();
        for (Entry<String, String> entry : mAppConfigMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.contains(pkgName + SPLIT_STR + pageName)) {
                String[] pageInfo = splitString(key, SPLIT_STR);
                if (pageInfo != null && pageInfo.length == 4) {
                    infoType = pageInfo[3];
                    break;
                }
            }
        }
        return infoType;
    }

    private void getCaptureSwitch() {
        IVivoUserProfiling service = getService();
        if (service == null) {
            Log.d(TAG, "service is null! ");
            mCaptureOn = false;
        }
        try {
            mCaptureOn = service.isCaptureOn();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException! ");
            mCaptureOn = false;
        }
    }

    public boolean isCaptureOn() {
        return mCaptureOn;
    }

    public boolean isPropertiesContainPage(String pkgName, String pageName) {
        if (mAppConfigMap == null || mAppConfigMap.size() < 1) {
            return false;
        }
        for (Entry<String, String> entry : mAppConfigMap.entrySet()) {
            if (((String) entry.getKey()).contains(pkgName + SPLIT_STR + pageName)) {
                Log.d(TAG, "page contain");
                return true;
            }
        }
        return false;
    }

    public boolean isPropertiesContainPkg(String pkgName) {
        if (mAppConfigMap == null || mAppConfigMap.size() < 1) {
            return false;
        }
        for (Entry<String, String> entry : mAppConfigMap.entrySet()) {
            if (((String) entry.getKey()).contains(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public void setWeChatWebViewValue(String webViewValue) {
        IVivoUserProfiling service = getService();
        if (service != null) {
            try {
                service.setWeChatWebViewValue(webViewValue);
            } catch (RemoteException e) {
            }
        }
    }

    public String getWeChatWebViewValue() {
        String str = null;
        IVivoUserProfiling service = getService();
        if (service != null) {
            try {
                return service.getWeChatWebViewValue();
            } catch (RemoteException e) {
            }
        }
        return str;
    }

    public void updateWeChatViewId(String textViewId) {
        IVivoUserProfiling service = getService();
        if (service != null) {
            try {
                service.updateWeChatViewId(textViewId);
            } catch (RemoteException e) {
            }
        }
    }
}
