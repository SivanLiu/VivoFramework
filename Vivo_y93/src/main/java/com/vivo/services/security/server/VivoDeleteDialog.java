package com.vivo.services.security.server;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import vivo.util.VivoThemeUtil;
import vivo.util.VivoThemeUtil.ThemeType;

public final class VivoDeleteDialog {
    public static final long CONFIRM_PERIOD = 1000;
    public static final long CONFIRM_TIMEOUT = 20000;
    public static final String PROP_SUPER_SAVER = "sys.super_power_save";
    private boolean isUserClicked = false;
    private AlertDialog mAlertDialog = null;
    private int mCallingUid = -1;
    private Timer mConfirmTimer = null;
    private Context mContext = null;
    private String mPackageName = null;
    private String mPathName = null;
    private String mPathPkg = "";
    private int mPermissionResult = 0;
    private String mType = "";
    private Handler mUiHandler = null;
    private String mVPDKey = null;
    private VivoPermissionService mVPS = null;

    private final class ConfirmDialogListener implements OnClickListener, OnCheckedChangeListener {
        ConfirmDialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            VivoDeleteDialog.this.cancelConfirmTimer();
            VivoDeleteDialog.this.isUserClicked = true;
            if (which == -1) {
                VivoDeleteDialog.this.setPermissionResultSync(1);
            } else if (which == -2) {
                VivoDeleteDialog.this.setPermissionResultSync(2);
            }
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
            return SystemProperties.getBoolean("sys.super_power_save", false) || VivoPermissionService.isKeyguardLocked(VivoDeleteDialog.this.mContext);
        }

        public void run() {
            this.mTimeLeft -= this.mPeriod;
            if (this.mTimeLeft <= 0 || needDismiss()) {
                VivoDeleteDialog.this.dismiss();
            } else {
                updateDialog();
            }
        }

        private void updateDialog() {
            VivoDeleteDialog.this.mUiHandler.post(new Runnable() {
                public void run() {
                    VivoPermissionService.printfInfo("mTimeLeft=" + ConfirmTimerTask.this.mTimeLeft);
                    if (VivoDeleteDialog.this.mAlertDialog != null) {
                        synchronized (VivoDeleteDialog.this.mAlertDialog) {
                            if (VivoDeleteDialog.this.mAlertDialog != null) {
                                VivoDeleteDialog.this.mAlertDialog.getButton(-2).setText(VivoDeleteDialog.this.getNegativeButtonText(ConfirmTimerTask.this.mTimeLeft / ConfirmTimerTask.this.mPeriod));
                            }
                        }
                    }
                }
            });
        }
    }

    public VivoDeleteDialog(VivoPermissionService vps, Context context, Handler uiHandler, String packageName, String pathName, String pathPkg, String type, int uid, String key) {
        this.mVPS = vps;
        this.mContext = new ContextThemeWrapper(context, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT));
        this.mUiHandler = uiHandler;
        this.mPackageName = packageName;
        this.mPathName = pathName;
        this.mCallingUid = uid;
        this.mVPDKey = key;
        this.mPathPkg = pathPkg;
        this.mType = type;
    }

    public void show() {
        String packageName = this.mPackageName;
        String pathName = this.mPathName;
        File file = new File(this.mPathName);
        ConfirmDialogListener listener = new ConfirmDialogListener();
        View layout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(50528277, null);
        TextView contentHint = (TextView) layout.findViewById(51183687);
        ((TextView) layout.findViewById(51183686)).setText(String.format(this.mContext.getString(51249646), new Object[]{getAppName(packageName), pathName}));
        contentHint.setText(getHintText(this.mPathPkg, this.mType));
        this.mAlertDialog = new Builder(this.mContext, VivoThemeUtil.getSystemThemeStyle(ThemeType.DIALOG_ALERT)).setTitle(51249345).setView(layout).setNegativeButton(getNegativeButtonText(20), listener).setPositiveButton(51249202, listener).create();
        this.mAlertDialog.getWindow().setType(2003);
        LayoutParams attributes = this.mAlertDialog.getWindow().getAttributes();
        attributes.privateFlags |= 536870912;
        this.mAlertDialog.setCancelable(false);
        this.mAlertDialog.show();
        startConfirmTimer(20000, 1000);
    }

    private String getNegativeButtonText(long timeLeft) {
        StringBuffer sb = new StringBuffer();
        sb.append("(").append(timeLeft).append("s").append(")");
        sb.append(this.mContext.getString(51249203));
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

    private String getHintText(String pkg, String types) {
        StringBuffer result = new StringBuffer("");
        String[] splitTypes;
        int i;
        if (!TextUtils.isEmpty(pkg)) {
            splitTypes = types.split(",");
            for (i = 0; i < splitTypes.length; i++) {
                result.append(getTypeText(splitTypes[i]));
                if (i < splitTypes.length - 1) {
                    result.append(12289);
                }
            }
            return String.format(this.mContext.getString(51249680), new Object[]{getAppName(pkg), result.toString()});
        } else if (TextUtils.isEmpty(types)) {
            return result.toString();
        } else {
            splitTypes = types.split(",");
            for (i = 0; i < splitTypes.length; i++) {
                result.append(getTypeText(splitTypes[i]));
                if (i < splitTypes.length - 1) {
                    result.append(12289);
                }
            }
            return String.format(this.mContext.getString(51249679), new Object[]{result.toString()});
        }
    }

    private String getTypeText(String type) {
        if (type.equals("001")) {
            return this.mContext.getString(51249647);
        }
        if (type.equals("002")) {
            return this.mContext.getString(51249648);
        }
        if (type.equals("003")) {
            return this.mContext.getString(51249649);
        }
        if (type.equals("004")) {
            return this.mContext.getString(51249650);
        }
        if (type.equals("005")) {
            return this.mContext.getString(51249651);
        }
        if (type.equals("006")) {
            return this.mContext.getString(51249652);
        }
        if (type.equals("007")) {
            return this.mContext.getString(51249653);
        }
        if (type.equals("008")) {
            return this.mContext.getString(51249654);
        }
        if (type.equals("009")) {
            return this.mContext.getString(51249655);
        }
        if (type.equals("010")) {
            return this.mContext.getString(51249656);
        }
        if (type.equals("011")) {
            return this.mContext.getString(51249657);
        }
        if (type.equals("012")) {
            return this.mContext.getString(51249658);
        }
        if (type.equals("013")) {
            return this.mContext.getString(51249659);
        }
        if (type.equals("014")) {
            return this.mContext.getString(51249660);
        }
        if (type.equals("015")) {
            return this.mContext.getString(51249661);
        }
        if (type.equals("016")) {
            return this.mContext.getString(51249662);
        }
        if (type.equals("017")) {
            return this.mContext.getString(51249663);
        }
        if (type.equals("018")) {
            return this.mContext.getString(51249664);
        }
        if (type.equals("019")) {
            return this.mContext.getString(51249665);
        }
        if (type.equals("020")) {
            return this.mContext.getString(51249666);
        }
        if (type.equals("021")) {
            return this.mContext.getString(51249667);
        }
        if (type.equals("022")) {
            return this.mContext.getString(51249668);
        }
        if (type.equals("023")) {
            return this.mContext.getString(51249669);
        }
        if (type.equals("024")) {
            return this.mContext.getString(51249670);
        }
        if (type.equals("025")) {
            return this.mContext.getString(51249671);
        }
        if (type.equals("026")) {
            return this.mContext.getString(51249672);
        }
        if (type.equals("027")) {
            return this.mContext.getString(51249673);
        }
        if (type.equals("028")) {
            return this.mContext.getString(51249674);
        }
        if (type.equals("029")) {
            return this.mContext.getString(51249675);
        }
        if (type.equals("030")) {
            return this.mContext.getString(51249676);
        }
        if (type.equals("031")) {
            return this.mContext.getString(51249677);
        }
        if (type.equals("032")) {
            return this.mContext.getString(51249678);
        }
        return "";
    }

    public void dismiss() {
        VivoPermissionService.printfInfo("dismissing VivoDeleteDialog...");
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

    public boolean isUserClicked() {
        return this.isUserClicked;
    }

    public int getPermissionResult(String pathName) {
        return this.mPermissionResult;
    }

    public int getCallingUid() {
        return this.mCallingUid;
    }

    public void handleWaitTimeOut() {
        this.mPermissionResult = 2;
    }

    private void setPermissionResultSync(int result) {
        synchronized (this) {
            this.mPermissionResult = result;
            this.mVPS.removeVDD(this.mVPDKey);
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
}
