package android.app.ai.beeservice.capture.util;

import android.app.Activity;
import android.app.mobileagent.util.LogUtil;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.view.View;
import java.util.ArrayList;
import java.util.Map;
import org.json.JSONObject;

public abstract class CaptureBase {
    public String TAG;
    public Activity mActivity;
    public ArrayList<String> mCurrentPageCaptureType;
    public String mCurrentPageInfoType;
    public String mCurrentPageName;
    public String mCurrentPagePkg;
    public Map<String, String> mCurrentPageProperties;
    public Resources mCurrentPageResources;
    public String mPkgName;
    public UserProfilingHelper mUserProfilingHelper;

    public abstract JSONObject captureView();

    public CaptureBase(Activity activity) {
        this.mUserProfilingHelper = null;
        this.TAG = "CaptureBase";
        this.mUserProfilingHelper = null;
        this.mPkgName = "包名";
        this.mCurrentPagePkg = "";
        this.mCurrentPageName = "";
        this.mCurrentPageInfoType = "";
        this.mCurrentPageResources = null;
        this.mCurrentPageCaptureType = null;
        this.mCurrentPageProperties = null;
        this.mActivity = null;
        initCaptureBase(activity);
    }

    private void initCaptureBase(Activity activity) {
        this.mActivity = activity;
        this.mUserProfilingHelper = UserProfilingHelper.getInstance();
        LogUtil.d(this.TAG, "Helper: " + this.mUserProfilingHelper);
        this.mCurrentPagePkg = this.mActivity.getPackageName();
        this.mCurrentPageResources = this.mActivity.getResources();
        this.mCurrentPageName = this.mActivity.getClass().getSimpleName();
        if (this.mUserProfilingHelper != null) {
            this.mCurrentPageCaptureType = this.mUserProfilingHelper.getPageTypeList(this.mCurrentPagePkg, this.mCurrentPageName, this.mActivity);
            LogUtil.d(this.TAG, "type:" + this.mCurrentPageCaptureType);
            this.mCurrentPageInfoType = this.mUserProfilingHelper.getInfoType(this.mCurrentPagePkg, this.mCurrentPageName, this.mActivity);
            LogUtil.d(this.TAG, "infotype:" + this.mCurrentPageInfoType);
        }
    }

    public String getViewId(View view) {
        if (view == null) {
            return "";
        }
        try {
            return this.mCurrentPageResources.getResourceEntryName(view.getId());
        } catch (NotFoundException ex) {
            LogUtil.e(this.TAG, "NotFoundException:" + ex);
            return "";
        }
    }
}
