package android.app.ai.beeservice.capture.util;

import android.app.mobileagent.util.LogUtil;
import android.content.Context;
import java.util.ArrayList;
import java.util.Map;
import vivo.app.VivoFrameworkFactory;
import vivo.app.userprofiling.AbsVivoUserProfilingManager;

public class UserProfilingHelper {
    public static final int REPORT_ERROR_CODE = -1;
    private static final String TAG = "UserProfilingHelper";
    private static UserProfilingHelper mInstance = null;
    private AbsVivoUserProfilingManager mUserProfilingManager;

    private UserProfilingHelper() {
        this.mUserProfilingManager = null;
        this.mUserProfilingManager = getUserProfilingManager();
    }

    public static synchronized UserProfilingHelper getInstance() {
        synchronized (UserProfilingHelper.class) {
            UserProfilingHelper userProfilingHelper;
            if (mInstance != null) {
                userProfilingHelper = mInstance;
                return userProfilingHelper;
            }
            mInstance = new UserProfilingHelper();
            userProfilingHelper = mInstance;
            return userProfilingHelper;
        }
    }

    private AbsVivoUserProfilingManager getUserProfilingManager() {
        if (this.mUserProfilingManager != null) {
            return this.mUserProfilingManager;
        }
        if (VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
            this.mUserProfilingManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoUserProfilingManager();
        }
        return this.mUserProfilingManager;
    }

    public ArrayList<String> getPageTypeList(String pkgName, String pageName, Context context) {
        if (this.mUserProfilingManager == null) {
            return null;
        }
        LogUtil.d(TAG, "Type:" + this.mUserProfilingManager.getPageTypeList(pkgName, pageName));
        return this.mUserProfilingManager.getPageTypeList(pkgName, pageName);
    }

    public String getInfoType(String pkgName, String pageName, Context context) {
        if (this.mUserProfilingManager == null) {
            return null;
        }
        LogUtil.d(TAG, "InfoType:" + this.mUserProfilingManager.getInfoType(pkgName, pageName));
        return this.mUserProfilingManager.getInfoType(pkgName, pageName);
    }

    public int reportDataToBee(String suffix, String values, Context context, String pkgName, String infoType) {
        if (this.mUserProfilingManager == null) {
            return -1;
        }
        LogUtil.d(TAG, "reportDataToBee");
        return this.mUserProfilingManager.reportDataToBee(suffix, values, pkgName, infoType);
    }

    public Map<String, String> getPropertiesMapByPagetype(String pkgName, String pageName, String pageType, Context context) {
        if (this.mUserProfilingManager == null) {
            return null;
        }
        LogUtil.d(TAG, "get Prop");
        return this.mUserProfilingManager.getPropertiesMapByPagetype(pkgName, pageName, pageType);
    }

    public void setWeChatWebViewValue(String webViewValue) {
        if (this.mUserProfilingManager != null) {
            LogUtil.d(TAG, "setWeChatWebViewValue");
            this.mUserProfilingManager.setWeChatWebViewValue(webViewValue);
        }
    }
}
