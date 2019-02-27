package com.vivo.services.popupcamera;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.FtBuild;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.vivo.services.popupcamera.PopupFrontCameraPermissionHelper.PopupFrontCameraPermissionState;

final class CameraPopupPermissionCheckDialog extends BaseErrorDialog {
    public static final int MAX_COUNTDOWN_TIMES = 20;
    private static final int MSG_DENY_PERMISSION = 1;
    private static final int MSG_GRANT_PERMISSION = 0;
    private static final int MSG_UPDATE_COUNTDOWN_TIMES = 2;
    private static final String TAG = "PopupCameraManagerService";
    private TextView confirmMessage;
    private Context context;
    private String denyPermTips;
    private String grantPermTips;
    private TextView hintMessage;
    private volatile boolean isCanceledByFrontCameraCloseTask = false;
    private volatile boolean isGrantedToUser = false;
    private volatile boolean isPermissionConfirmedByUser = false;
    private volatile boolean isRememberCheckBoxChecked = false;
    private volatile boolean isUserConfirmTimeout = false;
    private int mCurrentCountDown = 20;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = 1;
            PopupFrontCameraPermissionState -get5;
            switch (msg.what) {
                case 0:
                    CameraPopupPermissionCheckDialog.this.isPermissionConfirmedByUser = true;
                    CameraPopupPermissionCheckDialog.this.isGrantedToUser = true;
                    CameraPopupPermissionCheckDialog.this.permissionState.currentState = 0;
                    -get5 = CameraPopupPermissionCheckDialog.this.permissionState;
                    if (!CameraPopupPermissionCheckDialog.this.isRememberCheckBoxChecked) {
                        i = 0;
                    }
                    -get5.alwaysDeny = i;
                    PopupFrontCameraPermissionHelper.setFrontCameraPermissionStateToSettings(CameraPopupPermissionCheckDialog.this.context, CameraPopupPermissionCheckDialog.this.permissionState);
                    Log.d(CameraPopupPermissionCheckDialog.TAG, "the user grant popupcamera permission, notify the caller to run continue");
                    CameraPopupPermissionCheckDialog.this.cancelCountDownTimesMessages();
                    CameraPopupPermissionCheckDialog.this.dismiss();
                    return;
                case 1:
                    CameraPopupPermissionCheckDialog.this.isPermissionConfirmedByUser = true;
                    CameraPopupPermissionCheckDialog.this.isGrantedToUser = false;
                    CameraPopupPermissionCheckDialog.this.permissionState.currentState = 1;
                    -get5 = CameraPopupPermissionCheckDialog.this.permissionState;
                    if (!CameraPopupPermissionCheckDialog.this.isRememberCheckBoxChecked) {
                        i = 0;
                    }
                    -get5.alwaysDeny = i;
                    PopupFrontCameraPermissionHelper.setFrontCameraPermissionStateToSettings(CameraPopupPermissionCheckDialog.this.context, CameraPopupPermissionCheckDialog.this.permissionState);
                    Log.d(CameraPopupPermissionCheckDialog.TAG, "the user deny popupcamera permission, notify the caller to run continue");
                    CameraPopupPermissionCheckDialog.this.cancelCountDownTimesMessages();
                    CameraPopupPermissionCheckDialog.this.dismiss();
                    return;
                case 2:
                    if (CameraPopupPermissionCheckDialog.this.mCurrentCountDown > 0) {
                        CameraPopupPermissionCheckDialog.this.getButton(-2).setText(CameraPopupPermissionCheckDialog.this.denyPermTips + " (" + CameraPopupPermissionCheckDialog.this.mCurrentCountDown + CameraPopupPermissionCheckDialog.this.context.getString(51249629) + ")");
                    }
                    CameraPopupPermissionCheckDialog cameraPopupPermissionCheckDialog = CameraPopupPermissionCheckDialog.this;
                    cameraPopupPermissionCheckDialog.mCurrentCountDown = cameraPopupPermissionCheckDialog.mCurrentCountDown - 1;
                    if (CameraPopupPermissionCheckDialog.this.mCurrentCountDown >= 0) {
                        sendEmptyMessageDelayed(2, 1000);
                        return;
                    }
                    CameraPopupPermissionCheckDialog.this.dismiss();
                    CameraPopupPermissionCheckDialog.this.cancelCountDownTimesMessages();
                    CameraPopupPermissionCheckDialog.this.isUserConfirmTimeout = true;
                    CameraPopupPermissionCheckDialog.this.isPermissionConfirmedByUser = true;
                    CameraPopupPermissionCheckDialog.this.isGrantedToUser = false;
                    Log.d(CameraPopupPermissionCheckDialog.TAG, "the user doesn't confirm with 10 seconds, notify the caller to run continue");
                    return;
                default:
                    return;
            }
        }
    };
    private String mMessage;
    private PopupFrontCameraPermissionState permissionState;
    private CheckBox rememberCheckBox;

    public CameraPopupPermissionCheckDialog(Context context, boolean aboveSystem, String message, String grantPermTips, String denyPermTips, boolean isShowRememberCheckbox, PopupFrontCameraPermissionState state) {
        super(context);
        this.context = context;
        this.permissionState = state;
        Resources res = context.getResources();
        setCancelable(false);
        View layout = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(50528333, null);
        this.confirmMessage = (TextView) layout.findViewById(51183686);
        this.hintMessage = (TextView) layout.findViewById(51183687);
        if (FtBuild.isOverSeas()) {
            this.hintMessage.setText(51249631);
        } else {
            this.hintMessage.setText(51249253);
        }
        this.rememberCheckBox = (CheckBox) layout.findViewById(51183738);
        this.rememberCheckBox.setChecked(false);
        this.confirmMessage.setText(message);
        this.rememberCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(CameraPopupPermissionCheckDialog.TAG, "onCheckedChanged checked=" + b);
                CameraPopupPermissionCheckDialog.this.isRememberCheckBoxChecked = b;
            }
        });
        if (!isShowRememberCheckbox) {
            this.rememberCheckBox.setVisibility(8);
            this.hintMessage.setVisibility(8);
        }
        setTitle(51249205);
        setView(layout);
        this.mMessage = message;
        this.grantPermTips = grantPermTips;
        this.denyPermTips = denyPermTips;
        setButton(-2, denyPermTips, this.mHandler.obtainMessage(1));
        setButton(-1, grantPermTips, this.mHandler.obtainMessage(0));
        if (aboveSystem) {
            getWindow().setType(2010);
        }
        LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 272;
        getWindow().setAttributes(attrs);
        setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                CameraPopupPermissionCheckDialog.this.mHandler.sendEmptyMessage(2);
            }
        });
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                CameraPopupPermissionCheckDialog.this.mCurrentCountDown = 20;
                CameraPopupPermissionCheckDialog.this.cancelCountDownTimesMessages();
                CameraPopupPermissionCheckDialog.this.nofityCallersToRun();
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean isPermissionConfirmed() {
        return this.isPermissionConfirmedByUser;
    }

    public boolean isPermissionGranted() {
        return this.isGrantedToUser;
    }

    public boolean isConfirmTimeout() {
        return this.isUserConfirmTimeout;
    }

    public void cancelPermissionCheck() {
        this.isCanceledByFrontCameraCloseTask = true;
    }

    public boolean isPermissionCheckCanceled() {
        return this.isCanceledByFrontCameraCloseTask;
    }

    private void cancelCountDownTimesMessages() {
        if (this.mHandler.hasMessages(2)) {
            this.mHandler.removeMessages(2);
        }
    }

    private void nofityCallersToRun() {
        synchronized (this) {
            notifyAll();
        }
    }
}
