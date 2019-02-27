package com.vivo.services.epm.policy.crash;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Slog;
import com.vivo.services.epm.BaseExceptionPolicyHandler;
import com.vivo.services.epm.EventData;
import com.vivo.services.rms.ProcessList;
import java.util.ArrayList;

public class CrashExceptionPolicyHandler extends BaseExceptionPolicyHandler {
    private final int CRASH_ANR_TYPE = 1;
    private final int CRASH_DIED_TYPE = 3;
    private final int CRASH_EXP_TYPE = 2;
    private final int MSG_UPDATE_CRASHLIST = ProcessList.UNKNOWN_ADJ;
    private final String TAG = "CEP";
    private ActivityManager mActivityManager;
    private Context mContext;
    private ArrayList<DiedAppInfo> mDiedAppList = new ArrayList();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ProcessList.UNKNOWN_ADJ /*1001*/:
                    DiedAppInfo appInfo = msg.obj;
                    if (appInfo != null) {
                        CrashExceptionPolicyHandler.this.mThirdPartyExceptionHelper.updateAppList(appInfo.packageName, 2, appInfo.versionCode, null, null, null);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ThirdPartyExceptionHelper mThirdPartyExceptionHelper;

    static class DiedAppInfo {
        int diedCount;
        long firstDiedTime;
        String packageName;
        int versionCode;

        DiedAppInfo() {
        }
    }

    public CrashExceptionPolicyHandler(Context context) {
        super(context);
        this.mContext = context;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mThirdPartyExceptionHelper = ThirdPartyExceptionHelper.getInstance(this.mContext);
    }

    public void handleExceptionEvent(EventData data) {
        if (this.mThirdPartyExceptionHelper == null || (this.mThirdPartyExceptionHelper.isEpmThirdCrashEnable() ^ 1) == 0) {
            ContentValues cv = data.getContent();
            int crashType = cv.getAsInteger("crash_type").intValue();
            String packageName = cv.getAsString("packageName");
            int versionCode = cv.getAsInteger("versionCode").intValue();
            Slog.d("CEP", "packageName:" + packageName + ",versionCode:" + versionCode + ",crashType:" + crashType);
            switch (crashType) {
                case 1:
                    boolean isSlientANR = cv.getAsBoolean("isSilentANR").booleanValue();
                    String anrReason = cv.getAsString("anrReason");
                    if (!isSlientANR) {
                        this.mThirdPartyExceptionHelper.updateAppList(packageName, 1, versionCode, null, null, anrReason);
                        break;
                    }
                    break;
                case 2:
                    this.mThirdPartyExceptionHelper.updateAppList(packageName, 2, versionCode, cv.getAsString("longMsg"), cv.getAsString("stackTrace"), null);
                    break;
                case 3:
                    updateDiedAppList(packageName, versionCode);
                    break;
            }
        }
    }

    private void updateDiedAppList(String packageName, int versionCode) {
        DiedAppInfo appInfo;
        if (this.mDiedAppList == null || this.mDiedAppList.size() <= 0 || !containDiedAppInfo(packageName)) {
            appInfo = new DiedAppInfo();
            appInfo.packageName = packageName;
            appInfo.diedCount++;
            appInfo.firstDiedTime = System.currentTimeMillis();
            appInfo.versionCode = versionCode;
            Slog.d("CEP", "diedCount:" + appInfo.diedCount);
            this.mDiedAppList.add(appInfo);
            return;
        }
        for (DiedAppInfo appInfo2 : this.mDiedAppList) {
            if (appInfo2 != null && packageName.equals(appInfo2.packageName)) {
                int index = this.mDiedAppList.indexOf(appInfo2);
                if (appInfo2.diedCount < this.mThirdPartyExceptionHelper.getLimitDiedCount()) {
                    appInfo2.diedCount++;
                    if (appInfo2.diedCount == 1) {
                        appInfo2.firstDiedTime = System.currentTimeMillis();
                    }
                    Slog.d("CEP", "diedCount:" + appInfo2.diedCount);
                } else if (this.mThirdPartyExceptionHelper.isInLimitTime(appInfo2.firstDiedTime, appInfo2.firstDiedTime + this.mThirdPartyExceptionHelper.getDiedLimitTime())) {
                    Slog.d("CEP", "am forceStopPackage :" + packageName);
                    appInfo2.diedCount = 0;
                    this.mActivityManager.forceStopPackage(packageName);
                    Message.obtain(this.mHandler, ProcessList.UNKNOWN_ADJ, appInfo2).sendToTarget();
                }
                this.mDiedAppList.set(index, appInfo2);
            }
        }
    }

    private boolean containDiedAppInfo(String packageName) {
        if (this.mDiedAppList.size() > 0) {
            for (DiedAppInfo appInfo : this.mDiedAppList) {
                if (appInfo != null && packageName.equals(appInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String onExtraDump() {
        return this.mThirdPartyExceptionHelper.dump();
    }
}
