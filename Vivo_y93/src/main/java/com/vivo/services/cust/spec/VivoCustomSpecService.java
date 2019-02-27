package com.vivo.services.cust.spec;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FtBuild;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.app.LocalePicker;
import com.vivo.services.cust.VivoCustomManager;
import com.vivo.services.cust.spec.IVivoCustomSpecService.Stub;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VivoCustomSpecService extends Stub {
    private static Uri APN_LIST_URI = Uri.parse("content://telephony/carriers");
    private static final int BUSI_CALL_RECORD = 11;
    private static final int BUSI_CT_DATA = 2;
    private static final int BUSI_CT_SMS = 1;
    private static final int BUSI_CT_VOICE = 0;
    private static final int BUSI_NCT_DATA = 5;
    private static final int BUSI_NCT_SMS = 4;
    private static final int BUSI_NCT_VOICE = 3;
    private static final boolean CHECK_UP = "yes".equals(SystemProperties.get("persist.gn.p_check", "no"));
    private static final boolean DEBUG = true;
    public static final String EMM_SECURITY_PERMISSION = "com.chinatelecom.permission.security.EMM";
    private static final String FLAG_LSJY = "1";
    private static final String FLAG_SDJNSS = "111";
    private static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final String TAG = "VCSS";
    private static final String VIVO_CUSTOM_SECURE_PERMISSION = "com.vivo.custom.permission.PLUG_IN";
    private static final String VIVO_CUSTOM_SUPPORT = SystemProperties.get("ro.build.gn.support", "0");
    private ActivityManager mActivityManager;
    private AppOpsManager mAppOps;
    private ContentResolver mContentResolver;
    private Context mContext;
    private DevicePolicyManager mDPM;
    private IDevicePolicyManager mDPMS;
    private PackageManager mPackageManager;
    private IPackageManager mPms;
    private TelephonyManager mTeleManager;
    private Handler mUiHandler;
    private VivoCustomManager mVivoCustomManager;

    public VivoCustomSpecService(Context context, Handler handler) {
        this.mContext = context;
        this.mUiHandler = handler;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mVivoCustomManager = new VivoCustomManager();
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPms = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        this.mDPM = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mDPMS = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mTeleManager = (TelephonyManager) this.mContext.getSystemService("phone");
        preload();
    }

    private void preload() {
        addUninstallBlackList();
    }

    private void checkUp() {
        int callingUid = Binder.getCallingUid();
        if (!CHECK_UP) {
            return;
        }
        if (callingUid != 0 && callingUid == 1000) {
            return;
        }
        if (VIVO_CUSTOM_SUPPORT.equals(FLAG_LSJY)) {
            this.mContext.enforceCallingOrSelfPermission(EMM_SECURITY_PERMISSION, null);
            if (SystemProperties.getBoolean("persist.ctemmsign.enable", DEBUG)) {
                long valid_time = Long.parseLong(SystemProperties.get("persist.security.cvtm", "0"));
                boolean isvalid = System.currentTimeMillis() <= valid_time ? DEBUG : CHECK_UP;
                Log.d(TAG, "checkCert valid_time " + valid_time + " is valid = " + isvalid);
                if (!isvalid) {
                    throw new SecurityException("SecurityException: signature is unavailable, should be available!");
                }
            }
        } else if (VIVO_CUSTOM_SUPPORT.equals("11")) {
            boolean xiaolvGrant = this.mContext.checkCallingOrSelfPermission("komect.aqb.permission.MDM_PLUGIN") == 0 ? DEBUG : CHECK_UP;
            boolean heGuanjiaGrant = this.mContext.checkCallingOrSelfPermission(VIVO_CUSTOM_SECURE_PERMISSION) == 0 ? DEBUG : CHECK_UP;
            if (!xiaolvGrant && !heGuanjiaGrant) {
                throw new SecurityException("SecurityException: signature is unavailable, APP doesn't have permission.");
            }
        } else {
            this.mContext.enforceCallingOrSelfPermission(VIVO_CUSTOM_SECURE_PERMISSION, null);
        }
    }

    public boolean setDeviceOwner(ComponentName who) {
        checkUp();
        boolean success = CHECK_UP;
        if (who != null) {
            long callingId = Binder.clearCallingIdentity();
            try {
                Log.d(TAG, "current deviceOwner" + this.mDPM.getDeviceOwner());
                success = this.mDPM.setDeviceOwner(who);
                Log.d(TAG, "current deviceOwner" + this.mDPM.getDeviceOwner());
            } catch (Exception e) {
                Log.d(TAG, "setDeviceOwner exception occur! " + e);
                e.printStackTrace();
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        return success;
    }

    public String getAPIVersion() {
        checkUp();
        String apiVersion = "CTV3.1_" + FtBuild.getProductVersion();
        Log.d(TAG, "getAPIVersion api verison = " + apiVersion);
        return apiVersion;
    }

    public String getRomVersion() {
        checkUp();
        String romVersion = "" + FtBuild.getRomVersion();
        Log.d(TAG, "getRomVersion rom verison = " + romVersion);
        return romVersion;
    }

    public void setDevicePolicyManagerUIState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_settings_device_manager", state);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getDevicePolicyManagerUIState() {
        return Secure.getInt(this.mContentResolver, "ct_settings_device_manager", BUSI_CT_VOICE);
    }

    public void setAccessibilityServcieUIState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_settings_accessibility", state);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getAccessibilityServcieUIState() {
        return Secure.getInt(this.mContentResolver, "ct_settings_accessibility", BUSI_CT_VOICE);
    }

    public void clearDeviceOwner(String packageName) {
        checkUp();
        if (packageName != null) {
            try {
                Log.d(TAG, "current deviceOwner" + this.mDPM.getDeviceOwner());
                this.mDPMS.clearDeviceOwner(packageName);
                Log.d(TAG, "current deviceOwner" + this.mDPM.getDeviceOwner());
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to talk to package manager", e);
            }
        }
    }

    public boolean isTrustedAppStoreEnabled() {
        checkUp();
        boolean isTrustedAppStoreEnable = SystemProperties.getInt("persist.sys.gn.trust_enabled", BUSI_CT_VOICE) == BUSI_CT_SMS ? DEBUG : CHECK_UP;
        Log.d(TAG, "is TrustedAppStore Enabled = " + isTrustedAppStoreEnable);
        return isTrustedAppStoreEnable;
    }

    public void setFlightModeStateNormal(int value) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", value);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", value);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getFlightModeStateNormal() {
        checkUp();
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", BUSI_CT_VOICE);
    }

    public void setFaceWakeState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_security_facewake", state);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getFaceWakeState() {
        return Secure.getInt(this.mContentResolver, "ct_security_facewake", BUSI_CT_SMS);
    }

    public void setFingerprintState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_security_fingerprint", state);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getFingerprintState() {
        return Secure.getInt(this.mContentResolver, "ct_security_fingerprint", BUSI_CT_SMS);
    }

    public void setSmartlockState(int state) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "ct_security_smartlock", state);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public int getSmartlockState() {
        return Secure.getInt(this.mContentResolver, "ct_security_smartlock", BUSI_CT_SMS);
    }

    public void setDataEnabled(boolean value) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mTeleManager.setDataEnabled(value);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean getDataEnabled() {
        return this.mTeleManager.getDataEnabled();
    }

    public void setMobileSettings(ComponentName admin, String busi, Bundle settings) {
        if (admin == null || busi == null || settings == null) {
            throw new IllegalArgumentException("IllegalArgumentException:setMobileSettings admin/settings/busi is null!");
        }
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            switch (translate(busi)) {
                case BUSI_CT_VOICE /*0*/:
                    if (!settings.containsKey("CALLIN")) {
                        if (settings.containsKey("CALLOUT")) {
                            Secure.putInt(this.mContentResolver, "ct_voice_callout", settings.getInt("CALLOUT", BUSI_CT_SMS));
                            break;
                        }
                    }
                    Secure.putInt(this.mContentResolver, "ct_voice_callin", settings.getInt("CALLIN", BUSI_CT_SMS));
                    break;
                    break;
                case BUSI_CT_SMS /*1*/:
                    if (!settings.containsKey("RECEIVE")) {
                        if (settings.containsKey("SEND")) {
                            Secure.putInt(this.mContentResolver, "ct_sms_send", settings.getInt("SEND", BUSI_CT_SMS));
                            break;
                        }
                    }
                    Secure.putInt(this.mContentResolver, "ct_sms_receive", settings.getInt("RECEIVE", BUSI_CT_SMS));
                    break;
                    break;
                case BUSI_CT_DATA /*2*/:
                    if (!settings.containsKey("BLOCK")) {
                        if (settings.containsKey("CONNECT")) {
                            Secure.putInt(this.mContentResolver, "ct_data_connect", settings.getInt("CONNECT", BUSI_CT_SMS));
                            break;
                        }
                    }
                    Secure.putInt(this.mContentResolver, "ct_data_block", settings.getInt("BLOCK", BUSI_CT_SMS));
                    break;
                    break;
                case BUSI_NCT_VOICE /*3*/:
                    if (settings.containsKey("BLOCK")) {
                        Secure.putInt(this.mContentResolver, "nct_voice_block", settings.getInt("BLOCK", BUSI_CT_SMS));
                        break;
                    }
                    break;
                case BUSI_NCT_SMS /*4*/:
                    if (settings.containsKey("BLOCK")) {
                        Secure.putInt(this.mContentResolver, "nct_sms_block", settings.getInt("BLOCK", BUSI_CT_SMS));
                        break;
                    }
                    break;
                case BUSI_NCT_DATA /*5*/:
                    if (settings.containsKey("BLOCK")) {
                        Secure.putInt(this.mContentResolver, "nct_data_block", settings.getInt("BLOCK", BUSI_CT_SMS));
                        break;
                    }
                    break;
            }
            Log.d(TAG, "setMobileSettings package = " + admin.getPackageName() + " busi = " + busi + " setting = " + settings.toString());
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public Bundle getMobileSettings(ComponentName admin, String busi, String setting) {
        if (admin == null || busi == null || setting == null) {
            throw new IllegalArgumentException("IllegalArgumentException:getMobileSettings admin/settings/busi is null!");
        }
        checkUp();
        Bundle mBundle = new Bundle();
        switch (translate(busi)) {
            case BUSI_CT_VOICE /*0*/:
                if (!setting.equals("CALLIN")) {
                    if (setting.equals("CALLOUT")) {
                        mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_voice_callout", BUSI_CT_SMS));
                        break;
                    }
                }
                mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_voice_callin", BUSI_CT_SMS));
                break;
                break;
            case BUSI_CT_SMS /*1*/:
                if (!setting.equals("RECEIVE")) {
                    if (setting.equals("SEND")) {
                        mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_sms_send", BUSI_CT_SMS));
                        break;
                    }
                }
                mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_sms_receive", BUSI_CT_SMS));
                break;
                break;
            case BUSI_CT_DATA /*2*/:
                if (!setting.equals("BLOCK")) {
                    if (setting.equals("CONNECT")) {
                        mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_data_connect", BUSI_CT_SMS));
                        break;
                    }
                }
                mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "ct_data_block", BUSI_CT_SMS));
                break;
                break;
            case BUSI_NCT_VOICE /*3*/:
                if (setting.equals("BLOCK")) {
                    mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "nct_voice_block", BUSI_CT_SMS));
                    break;
                }
                break;
            case BUSI_NCT_SMS /*4*/:
                if (setting.equals("BLOCK")) {
                    mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "nct_sms_block", BUSI_CT_SMS));
                    break;
                }
                break;
            case BUSI_NCT_DATA /*5*/:
                if (setting.equals("BLOCK")) {
                    mBundle.putInt(setting, Secure.getInt(this.mContentResolver, "nct_data_block", BUSI_CT_SMS));
                    break;
                }
                break;
        }
        Log.d(TAG, "getMobileSettings package = " + admin.getPackageName() + " busi = " + busi + " setting = " + mBundle.toString());
        return mBundle;
    }

    private int translate(String busi) {
        if (busi.equals("CT-VOICE")) {
            return BUSI_CT_VOICE;
        }
        if (busi.equals("CT-SMS")) {
            return BUSI_CT_SMS;
        }
        if (busi.equals("CT-DATA")) {
            return BUSI_CT_DATA;
        }
        if (busi.equals("NCT-VOICE")) {
            return BUSI_NCT_VOICE;
        }
        if (busi.equals("NCT-SMS")) {
            return BUSI_NCT_SMS;
        }
        if (busi.equals("NCT-DATA")) {
            return BUSI_NCT_DATA;
        }
        return -1;
    }

    public void disablePackage(ComponentName admin, String packageName) {
        checkUp();
        long callingId = Binder.clearCallingIdentity();
        try {
            if (packageName.equals("com.vivo.animationtool")) {
                this.mPms.setApplicationEnabledSetting(packageName, BUSI_CT_DATA, BUSI_CT_VOICE, BUSI_CT_VOICE, null);
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to talk to package manager", e);
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    private void addUninstallBlackList() {
        List<String> pkgs = new ArrayList();
        if (FLAG_SDJNSS.equals(VIVO_CUSTOM_SUPPORT)) {
            pkgs.add("cn.ishansong");
        }
        if (pkgs != null && pkgs.size() > 0) {
            Log.d(TAG, "addUninstallBlackList apps size = " + pkgs.size());
            this.mVivoCustomManager.addUninstallBlackList(pkgs);
            Secure.putInt(this.mContentResolver, "ct_app_uninstall_restrict_pattern", BUSI_CT_SMS);
        }
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        checkUp();
        Log.d(TAG, "set LanguageChange Disabled: " + disabled);
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "language_chinese_and_disabled", disabled ? BUSI_CT_SMS : BUSI_CT_VOICE);
            if (disabled) {
                IActivityManager am = ActivityManagerNative.getDefault();
                Configuration config = am.getConfiguration();
                LocalePicker.updateLocale(Locale.SIMPLIFIED_CHINESE);
                Log.d(TAG, "new local lable: " + am.getConfiguration().locale.toString());
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Exception e) {
            e.printStackTrace();
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        checkUp();
        Log.d(TAG, "set DeveloperOptions Disabled: " + disabled);
        long callingId = Binder.clearCallingIdentity();
        try {
            Secure.putInt(this.mContentResolver, "developer_disabled", disabled ? BUSI_CT_SMS : BUSI_CT_VOICE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean isDeveloperOptionsDisabled() {
        checkUp();
        return Secure.getInt(this.mContentResolver, "developer_disabled", BUSI_CT_VOICE) == BUSI_CT_SMS ? DEBUG : CHECK_UP;
    }
}
