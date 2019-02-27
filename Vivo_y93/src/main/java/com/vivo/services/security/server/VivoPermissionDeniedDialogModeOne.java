package com.vivo.services.security.server;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.qualcomm.qcrilhook.EmbmsOemHook;
import com.vivo.framework.security.VivoPermissionManager;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.server.db.VivoPermissionDataBase;
import java.util.Timer;
import java.util.TimerTask;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public final class VivoPermissionDeniedDialogModeOne {
    /* renamed from: -com-vivo-services-security-client-VivoPermissionTypeSwitchesValues */
    private static final /* synthetic */ int[] f0xc01a3235 = null;
    public static final long CONFIRM_PERIOD = 1000;
    public static final long CONFIRM_TIMEOUT = 2000000;
    public static final String PROP_SUPER_SAVER = "sys.super_power_save";
    private boolean isDialogChecked = false;
    private AlertDialog mAlertDialog = null;
    private int mCallingPid = -1;
    private Timer mConfirmTimer = null;
    private Context mContext = null;
    private String mPackageName = null;
    private String mPermissionName = null;
    private Handler mUiHandler = null;
    private String mVPDKey = null;
    private VivoPermissionService mVPS = null;

    private final class ConfirmDialogListener implements OnClickListener {
        ConfirmDialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            VivoPermissionDeniedDialogModeOne.this.cancelConfirmTimer();
            VivoPermissionDeniedDialogModeOne.this.isDialogChecked = true;
            VivoPermissionDeniedDialogModeOne.this.mVPS.removeVPD1(VivoPermissionDeniedDialogModeOne.this.mVPDKey);
            int vpTypeId = VivoPermissionType.getVPType(VivoPermissionDeniedDialogModeOne.this.mPermissionName).getVPTypeId();
            if (VivoPermissionDeniedDialogModeOne.this.mVPS.checkConfigDeniedMode(VivoPermissionDeniedDialogModeOne.this.mPackageName, VivoPermissionDeniedDialogModeOne.this.mPermissionName) == 48) {
                VivoPermissionDeniedDialogModeOne.this.mVPS.setConfigDeniedMode(VivoPermissionDeniedDialogModeOne.this.mPackageName, VivoPermissionDeniedDialogModeOne.this.mPermissionName, 80);
            }
            if (which == -1) {
                VivoPermissionDeniedDialogModeOne.this.startSettingPermissionActivity();
                VivoPermissionService.printfInfo("1 click POSITIVE to SettingPermissionActivity");
                return;
            }
            VivoPermissionService.printfInfo("1 click NEGATIVE to cancel");
        }
    }

    private final class ConfirmTimerTask extends TimerTask {
        private long mPeriod = 0;
        private long mTimeLeft = 0;

        ConfirmTimerTask(long timeout, long period) {
            this.mTimeLeft = timeout;
            this.mPeriod = period;
        }

        private boolean needDismiss() {
            return SystemProperties.getBoolean("sys.super_power_save", false) || VivoPermissionService.isKeyguardLocked(VivoPermissionDeniedDialogModeOne.this.mContext);
        }

        public void run() {
            if (needDismiss()) {
                VivoPermissionDeniedDialogModeOne.this.dismiss();
            }
        }
    }

    /* renamed from: -getcom-vivo-services-security-client-VivoPermissionTypeSwitchesValues */
    private static /* synthetic */ int[] m0x8169cf11() {
        if (f0xc01a3235 != null) {
            return f0xc01a3235;
        }
        int[] iArr = new int[VivoPermissionType.values().length];
        try {
            iArr[VivoPermissionType.ACCESS_LOCATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[VivoPermissionType.BLUETOOTH.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[VivoPermissionType.CALL_PHONE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[VivoPermissionType.CAMERA_IMAGE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[VivoPermissionType.CAMERA_VIDEO.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[VivoPermissionType.CHANGE_NETWORK_STATE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[VivoPermissionType.CHANGE_WIFI_STATE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[VivoPermissionType.INTERNET.ordinal()] = 23;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[VivoPermissionType.LAST.ordinal()] = 24;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[VivoPermissionType.MONITOR_CALL.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[VivoPermissionType.NFC.ordinal()] = 9;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[VivoPermissionType.READ_CALENDAR.ordinal()] = 10;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[VivoPermissionType.READ_CALL_LOG.ordinal()] = 11;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[VivoPermissionType.READ_CONTACTS.ordinal()] = 12;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[VivoPermissionType.READ_INTERNET_RECORDS.ordinal()] = 25;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[VivoPermissionType.READ_MMS.ordinal()] = 13;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[VivoPermissionType.READ_PHONE_STATE.ordinal()] = 14;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[VivoPermissionType.READ_SMS.ordinal()] = 15;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[VivoPermissionType.RECORD_AUDIO.ordinal()] = 16;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[VivoPermissionType.RW_FILE.ordinal()] = 26;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[VivoPermissionType.SCREENSHOT.ordinal()] = 27;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[VivoPermissionType.SEND_EMAIL.ordinal()] = 28;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[VivoPermissionType.SEND_MMS.ordinal()] = 17;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[VivoPermissionType.SEND_SMS.ordinal()] = 18;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[VivoPermissionType.THIRD_PHONE.ordinal()] = 29;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[VivoPermissionType.WHITE_INTERNET_RECORDS.ordinal()] = 30;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CALENDAR.ordinal()] = 31;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CALL_LOG.ordinal()] = 19;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CONTACTS.ordinal()] = 20;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[VivoPermissionType.WRITE_MMS.ordinal()] = 21;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[VivoPermissionType.WRITE_SMS.ordinal()] = 22;
        } catch (NoSuchFieldError e31) {
        }
        f0xc01a3235 = iArr;
        return iArr;
    }

    public VivoPermissionDeniedDialogModeOne(VivoPermissionService vps, Context context, Handler uiHandler, String packageName, String permName, int pid, String key) {
        this.mVPS = vps;
        this.mContext = new ContextThemeWrapper(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT));
        this.mUiHandler = uiHandler;
        this.mPackageName = packageName;
        this.mPermissionName = permName;
        this.mCallingPid = pid;
        this.mVPDKey = key;
    }

    private String getContentStr() {
        String packageName = this.mPackageName;
        String PermStr = getPermissionString(this.mPermissionName);
        if (VivoPermissionManager.getInstance().getOSVersion() < 3.0f || (VivoPermissionConfig.IS_25T30_LITE ^ 1) == 0) {
            return String.format(this.mContext.getString(51249251), new Object[]{getAppName(packageName), PermStr});
        }
        return String.format(this.mContext.getString(51249255), new Object[]{getAppName(packageName), PermStr});
    }

    private String getContentTitle() {
        String PermStr = getPermissionString(this.mPermissionName);
        return String.format(this.mContext.getString(51249216), new Object[]{PermStr});
    }

    public void show() {
        ConfirmDialogListener listener = new ConfirmDialogListener();
        View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(50528335, null);
        ((TextView) layout.findViewById(51183687)).setText(getContentStr());
        this.mAlertDialog = new Builder(this.mContext, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT)).setTitle(getContentTitle()).setView(layout).setPositiveButton(51249214, listener).setNegativeButton(51249247, listener).create();
        this.mAlertDialog.getWindow().setType(2003);
        LayoutParams attributes = this.mAlertDialog.getWindow().getAttributes();
        attributes.privateFlags |= 536870912;
        this.mAlertDialog.setCancelable(false);
        this.mAlertDialog.show();
        startConfirmTimer(2000000, 1000);
    }

    private String getAppName(String packageName) {
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 64).applicationInfo.loadLabel(this.mContext.getPackageManager()).toString();
        } catch (NameNotFoundException e) {
            VivoPermissionService.printfInfo("vpftd Can't get calling app package info");
            e.printStackTrace();
            return packageName;
        }
    }

    private String getPermissionString(String permName) {
        VivoPermissionType vpt = VivoPermissionType.getVPType(permName);
        String result = vpt.toString();
        int stringId = -1;
        switch (m0x8169cf11()[vpt.ordinal()]) {
            case 1:
                stringId = 51249195;
                break;
            case 2:
                stringId = 51249211;
                break;
            case 3:
                stringId = 51249185;
                break;
            case 4:
                stringId = 51249244;
                break;
            case 5:
                stringId = 51249244;
                break;
            case 6:
                stringId = 51249209;
                break;
            case 7:
                stringId = 51249210;
                break;
            case 8:
                stringId = 51249186;
                break;
            case 9:
                stringId = 51249212;
                break;
            case 10:
                stringId = 51249252;
                break;
            case EmbmsOemHook.UNSOL_TYPE_CONTENT_DESC_PER_OBJ_CONTROL /*11*/:
                stringId = 51249190;
                break;
            case EmbmsOemHook.UNSOL_TYPE_EMBMS_STATUS /*12*/:
                stringId = 51249189;
                break;
            case EmbmsOemHook.UNSOL_TYPE_GET_INTERESTED_TMGI_LIST /*13*/:
                stringId = 51249245;
                break;
            case 14:
                stringId = 51249196;
                break;
            case VivoPermissionDataBase.GET_MASK /*15*/:
                stringId = 51249245;
                break;
            case ProcessStates.WORKING /*16*/:
                stringId = 51249199;
                break;
            case 17:
                stringId = 51249184;
                break;
            case 18:
                stringId = 51249183;
                break;
            case 19:
                stringId = 51249194;
                break;
            case 20:
                stringId = 51249193;
                break;
            case 21:
                stringId = 51249246;
                break;
            case 22:
                stringId = 51249246;
                break;
        }
        if (stringId != -1) {
            result = this.mContext.getString(stringId).toLowerCase();
        }
        if (stringId == 51249183 || stringId == 51249191 || stringId == 51249187) {
            return result.replace("sms", "SMS");
        }
        if (stringId == 51249184 || stringId == 51249192 || stringId == 51249188) {
            return result.replace("mms", "MMS");
        }
        if (stringId == 51249245 || stringId == 51249246) {
            return result.replace("sms", "SMS").replace("mms", "MMS");
        }
        if (stringId == 51249196) {
            return result.replace("id", "ID");
        }
        if (stringId == 51249210) {
            if (VivoPermissionManager.getInstance().isOverSeas()) {
                return result.replace("wlan", "Wi-Fi").replace("wi-fi", "Wi-Fi");
            }
            return result.replace("wlan", "WLAN");
        } else if (stringId == 51249212) {
            return result.replace("nfc", "NFC");
        } else {
            if (stringId == 51249209) {
                return result.replace("mobile", "Mobile").replace("network", "Network");
            }
            if (stringId == 51249211) {
                return result.replace("bluetooth", "Bluetooth");
            }
            return result;
        }
    }

    public void dismiss() {
        VivoPermissionService.printfInfo("1 dismissing VivoPermissionDeniedDialogModeOne...");
        cancelConfirmTimer();
        this.mVPS.removeVPD1(this.mVPDKey);
        if (this.mAlertDialog != null) {
            synchronized (this.mAlertDialog) {
                this.mAlertDialog.dismiss();
            }
            this.mAlertDialog = null;
        }
    }

    public int getCallingPid() {
        return this.mCallingPid;
    }

    boolean isDialogClicked() {
        return this.isDialogChecked;
    }

    private void startConfirmTimer(long timeout, long period) {
        if (this.mConfirmTimer == null) {
            this.mConfirmTimer = new Timer();
        }
        this.mConfirmTimer.schedule(new ConfirmTimerTask(timeout, period), period, period);
    }

    private void cancelConfirmTimer() {
        if (this.mConfirmTimer != null) {
            this.mConfirmTimer.cancel();
            this.mConfirmTimer = null;
        }
    }

    private void startSettingPermissionActivity() {
        ComponentName componetName;
        if (isPermissionManagerAppExisted()) {
            componetName = new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity");
        } else {
            componetName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity");
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.putExtra("packagename", this.mPackageName);
            intent.setFlags(268468224);
            this.mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            VivoPermissionService.printfInfo("1 start com.iqoo.secure fail");
        }
    }

    private boolean isPermissionManagerAppExisted() {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = this.mContext.getPackageManager().getApplicationInfo("com.vivo.permissionmanager", 0);
        } catch (Exception e) {
            e.printStackTrace();
            VivoPermissionService.printfInfo("check isPermissionManagerAppExisted fail");
        }
        if (applicationInfo == null) {
            return false;
        }
        return true;
    }
}
