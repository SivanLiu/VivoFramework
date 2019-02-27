package com.vivo.services.security.server;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.qualcomm.qcrilhook.EmbmsOemHook;
import com.vivo.common.VivoCollectData;
import com.vivo.framework.security.VivoPermissionManager;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.security.client.IVivoPermissionCallback;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.client.VivoPermissionType.VivoPermissionGroup;
import com.vivo.services.security.server.db.VivoPermissionDataBase;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public final class VivoPermissionDialog {
    /* renamed from: -com-vivo-services-security-client-VivoPermissionTypeSwitchesValues */
    private static final /* synthetic */ int[] f3xc01a3235 = null;
    public static final long CONFIRM_PERIOD = 1000;
    public static final long CONFIRM_TIMEOUT = 20000;
    public static final String PROP_SUPER_SAVER = "sys.super_power_save";
    private boolean isDialogChecked = false;
    private boolean isVivoImeiPkg = false;
    private AlertDialog mAlertDialog = null;
    private RemoteCallbackList<IVivoPermissionCallback> mCallbacks = null;
    private int mCallingPid = -1;
    private Timer mConfirmTimer = null;
    private Context mContext = null;
    private String mPackageName = null;
    private String mPermissionName = null;
    private int mPermissionResult = 0;
    private boolean mRememberChoice = false;
    private Handler mUiHandler = null;
    private String mVPDKey = null;
    private VivoPermissionService mVPS = null;
    private VivoCollectData mVivoCollectData = null;
    private VivoPermissionInfo mVpi = null;
    private VivoPermissionType mVpt = null;
    private boolean timeCountDownRight = true;

    private final class ConfirmDialogListener implements OnClickListener, OnCheckedChangeListener {
        ConfirmDialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            VivoPermissionDialog.this.cancelConfirmTimer();
            VivoPermissionDialog.this.isDialogChecked = true;
            if (which == -1) {
                VivoPermissionDialog.this.mRememberChoice = true;
                VivoPermissionDialog.this.setPermissionResultSync(1);
            } else if (which == -2) {
                VivoPermissionDialog.this.setPermissionResultSync(2);
            }
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            VivoPermissionDialog.this.mRememberChoice = isChecked;
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
            return SystemProperties.getBoolean("sys.super_power_save", false) || VivoPermissionService.isKeyguardLocked(VivoPermissionDialog.this.mContext);
        }

        public void run() {
            this.mTimeLeft -= this.mPeriod;
            if (this.mTimeLeft <= 0 || needDismiss()) {
                VivoPermissionDialog.this.dismiss();
            } else {
                updateDialog();
            }
        }

        private void updateDialog() {
            VivoPermissionDialog.this.mUiHandler.post(new Runnable() {
                public void run() {
                    VivoPermissionService.printfInfo("mTimeLeft=" + ConfirmTimerTask.this.mTimeLeft);
                    if (VivoPermissionDialog.this.mAlertDialog != null) {
                        synchronized (VivoPermissionDialog.this.mAlertDialog) {
                            if (VivoPermissionDialog.this.mAlertDialog != null) {
                                VivoPermissionDialog.this.mAlertDialog.getButton(-2).setText(VivoPermissionDialog.this.getNegativeButtonText(ConfirmTimerTask.this.mTimeLeft / ConfirmTimerTask.this.mPeriod));
                            }
                        }
                    }
                }
            });
        }
    }

    /* renamed from: -getcom-vivo-services-security-client-VivoPermissionTypeSwitchesValues */
    private static /* synthetic */ int[] m3x8169cf11() {
        if (f3xc01a3235 != null) {
            return f3xc01a3235;
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
            iArr[VivoPermissionType.INTERNET.ordinal()] = 24;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[VivoPermissionType.LAST.ordinal()] = 25;
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
            iArr[VivoPermissionType.READ_INTERNET_RECORDS.ordinal()] = 26;
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
            iArr[VivoPermissionType.RW_FILE.ordinal()] = 27;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[VivoPermissionType.SCREENSHOT.ordinal()] = 28;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[VivoPermissionType.SEND_EMAIL.ordinal()] = 29;
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
            iArr[VivoPermissionType.THIRD_PHONE.ordinal()] = 30;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[VivoPermissionType.WHITE_INTERNET_RECORDS.ordinal()] = 31;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CALENDAR.ordinal()] = 19;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CALL_LOG.ordinal()] = 20;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[VivoPermissionType.WRITE_CONTACTS.ordinal()] = 21;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[VivoPermissionType.WRITE_MMS.ordinal()] = 22;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[VivoPermissionType.WRITE_SMS.ordinal()] = 23;
        } catch (NoSuchFieldError e31) {
        }
        f3xc01a3235 = iArr;
        return iArr;
    }

    public VivoPermissionDialog(VivoPermissionService vps, Context context, Handler uiHandler, String packageName, String permName, int pid, String key) {
        this.mVPS = vps;
        this.mContext = new ContextThemeWrapper(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT));
        this.mUiHandler = uiHandler;
        this.mPackageName = packageName;
        this.mPermissionName = permName;
        this.mCallingPid = pid;
        this.mVPDKey = key;
        this.mCallbacks = new RemoteCallbackList();
        this.mVivoCollectData = new VivoCollectData(context);
        this.isVivoImeiPkg = this.mVPS.isVivoImeiPkg(packageName);
        if (VivoPermissionManager.getInstance().isOverSeas()) {
            this.timeCountDownRight = false;
        }
    }

    public void show() {
        String packageName = this.mPackageName;
        String permName = this.mPermissionName;
        ConfirmDialogListener listener = new ConfirmDialogListener();
        this.mVpt = VivoPermissionType.getVPType(permName);
        VivoPermissionGroup vpg = this.mVpt.getVPGroup();
        this.mVpi = this.mVPS.getAppPermission(packageName);
        if (this.mVpi == null) {
            this.mPermissionResult = 1;
            return;
        }
        View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(50528333, null);
        TextView content = (TextView) layout.findViewById(51183686);
        String string = this.mContext.getString(51249206);
        Object[] objArr = new Object[2];
        objArr[0] = getAppName(packageName);
        objArr[1] = getPermissionString(this.mVpt);
        String contentStr = String.format(string, objArr);
        content.setText(contentStr);
        TextView contentHint = (TextView) layout.findViewById(51183687);
        if (VivoPermissionManager.getInstance().getOSVersion() < 3.0f || (VivoPermissionConfig.IS_25T30_LITE ^ 1) == 0) {
            contentHint.setText(51249208);
        } else {
            contentHint.setText(51249253);
        }
        CheckBox rememberCB = (CheckBox) layout.findViewById(51183738);
        rememberCB.setOnCheckedChangeListener(listener);
        if (needRemeber(this.mVpt)) {
            rememberCB.setChecked(true);
            this.mRememberChoice = true;
            if (this.mVpt == VivoPermissionType.READ_PHONE_STATE && this.isVivoImeiPkg) {
                String imeiText = String.format(this.mContext.getString(51249692), new Object[]{getAppName(packageName)});
                if (this.mVPS.needShowImeiTipsDialogOne(this.mVpi, this.mVpt.getVPTypeId())) {
                    rememberCB.setChecked(false);
                    this.mRememberChoice = false;
                    content.setText(imeiText);
                    rememberCB.setVisibility(8);
                } else if (this.mVPS.needShowImeiTipsDialogTwo(this.mVpi, this.mVpt.getVPTypeId())) {
                    rememberCB.setChecked(false);
                    this.mRememberChoice = false;
                    content.setText(imeiText);
                }
            }
            this.mAlertDialog = new Builder(this.mContext, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT)).setTitle(51249205).setView(layout).setNegativeButton(getNegativeButtonText(20), listener).setPositiveButton(51249592, listener).create();
        } else {
            rememberCB.setChecked(false);
            this.mRememberChoice = false;
            contentHint.setVisibility(8);
            rememberCB.setVisibility(8);
            if (VivoPermissionType.getVPType(permName) == VivoPermissionType.BLUETOOTH) {
                contentStr = getAppName(packageName) + this.mContext.getString(51249595);
            } else {
                if (VivoPermissionType.getVPType(permName) == VivoPermissionType.CHANGE_WIFI_STATE) {
                    contentStr = getAppName(packageName) + this.mContext.getString(51249596);
                }
            }
            this.mAlertDialog = new Builder(this.mContext, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT)).setMessage(contentStr).setNegativeButton(getNegativeButtonText(20), listener).setPositiveButton(51249202, listener).create();
        }
        this.mAlertDialog.getWindow().setType(2003);
        LayoutParams attributes = this.mAlertDialog.getWindow().getAttributes();
        attributes.privateFlags |= 536870912;
        this.mAlertDialog.setCancelable(false);
        this.mAlertDialog.show();
        startConfirmTimer(20000, 1000);
    }

    private boolean needRemeber(VivoPermissionType vpt) {
        switch (m3x8169cf11()[vpt.ordinal()]) {
            case 2:
            case 7:
                return false;
            default:
                return true;
        }
    }

    private String getNegativeButtonText(long timeLeft) {
        StringBuffer sb = new StringBuffer();
        if (this.timeCountDownRight) {
            sb.append(this.mContext.getString(51249203));
            sb.append("(").append(timeLeft).append("s").append(")");
        } else {
            sb.append("(").append(timeLeft).append("s").append(")");
            sb.append(this.mContext.getString(51249203));
        }
        return sb.toString();
    }

    private String getAppName(String packageName) {
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 64).applicationInfo.loadLabel(this.mContext.getPackageManager()).toString();
        } catch (NameNotFoundException e) {
            VivoPermissionService.printfInfo("Can't get calling app package info");
            e.printStackTrace();
            return packageName;
        }
    }

    private String getPermissionString(VivoPermissionType vpt) {
        String result = vpt.toString();
        int stringId = -1;
        switch (m3x8169cf11()[vpt.ordinal()]) {
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
                stringId = 51249591;
                break;
            case 20:
                stringId = 51249194;
                break;
            case 21:
                stringId = 51249193;
                break;
            case 22:
                stringId = 51249246;
                break;
            case 23:
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
        VivoPermissionService.printfInfo("dismissing VivoPermissionDialog...");
        cancelConfirmTimer();
        setPermissionResultSync(2);
        if (this.mAlertDialog != null) {
            synchronized (this.mAlertDialog) {
                this.mAlertDialog.dismiss();
            }
            this.mAlertDialog = null;
        }
    }

    public boolean isPermissionConfirmed() {
        return this.mPermissionResult != 0;
    }

    public boolean isRememberChoice() {
        return this.mRememberChoice;
    }

    public int getPermissionResult(String permName) {
        return this.mPermissionResult;
    }

    public int getCallingPid() {
        return this.mCallingPid;
    }

    public void handleWaitTimeOut() {
        this.mPermissionResult = 2;
    }

    public void registerCallback(IVivoPermissionCallback cb) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.register(cb);
        }
    }

    public void notifyCallbacks(boolean result) {
        synchronized (this.mCallbacks) {
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((IVivoPermissionCallback) this.mCallbacks.getBroadcastItem(i)).onPermissionConfirmed(result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallbacks.finishBroadcast();
        }
    }

    private void setPermissionResultSync(int result) {
        boolean z = true;
        synchronized (this) {
            this.mPermissionResult = result;
            int vpTypeId = this.mVpt.getVPTypeId();
            if (this.mVpt == VivoPermissionType.READ_PHONE_STATE && this.isDialogChecked && this.isVivoImeiPkg) {
                int count;
                if (result == 1) {
                    if (this.mVPS.needShowImeiTipsDialogOne(this.mVpi, vpTypeId)) {
                        collectData(this.mPackageName, "0", (this.mVpi.getTipsDialogOneMode(vpTypeId) + 1) + "");
                    } else if (this.mVPS.needShowImeiTipsDialogTwo(this.mVpi, vpTypeId)) {
                        collectData(this.mPackageName, "0", ((this.mVpi.getTipsDialogOneMode(vpTypeId) + this.mVpi.getTipsDialogTwoMode(vpTypeId)) + 1) + "");
                    }
                } else if (this.mVPS.needShowImeiTipsDialogOne(this.mVpi, vpTypeId)) {
                    count = this.mVpi.getTipsDialogOneMode(vpTypeId) + 1;
                    this.mVpi.setTipsDialogOneMode(vpTypeId, count);
                    collectData(this.mPackageName, "1", count + "");
                    this.mVPS.setAppPermissionExt(this.mVpi);
                } else if (this.mVPS.needShowImeiTipsDialogTwo(this.mVpi, vpTypeId)) {
                    count = (this.mVpi.getTipsDialogOneMode(vpTypeId) + this.mVpi.getTipsDialogTwoMode(vpTypeId)) + 1;
                    if (this.mRememberChoice) {
                        this.mVpi.setTipsDialogTwoMode(vpTypeId, this.mVpi.getTipsDialogTwoMode(vpTypeId) + 1);
                        collectData(this.mPackageName, "2", count + "");
                    } else {
                        collectData(this.mPackageName, "1", count + "");
                    }
                }
            }
            if (this.mRememberChoice && this.isDialogChecked) {
                this.mVPS.setAppPermission(this.mPackageName, vpTypeId, result);
            }
            if (result != 1) {
                z = false;
            }
            notifyCallbacks(z);
            this.mVPS.removeVPD(this.mVPDKey);
            notifyAll();
        }
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

    private void collectData(String pkg, String mode, String type) {
        if (this.mVivoCollectData.getControlInfo("243")) {
            try {
                long curTime = System.currentTimeMillis();
                HashMap<String, String> params = new HashMap();
                params.put("pkg", pkg);
                params.put("mode", mode);
                params.put("type", type);
                this.mVivoCollectData.writeData("243", "2433", curTime, curTime, 0, 1, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
