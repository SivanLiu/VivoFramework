package com.vivo.services.popupcamera;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;

final class FrontCameraTemperatureProtectDialog extends BaseErrorDialog {
    private static final int MAX_COUNTDOWN_TIMES = 5;
    private static final int MSG_FORCE_CLOSE = 1;
    private static final int MSG_UPDATE_COUNTDOWN_TIMES = 2;
    private static final String TAG = "FrontCameraTemperatureProtectDialog";
    private Context context;
    private int mCurrentCountDown = 5;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    FrontCameraTemperatureProtectDialog.this.dismiss();
                    return;
                case 2:
                    if (FrontCameraTemperatureProtectDialog.this.mCurrentCountDown > 0) {
                        FrontCameraTemperatureProtectDialog.this.getButton(-1).setText(FrontCameraTemperatureProtectDialog.this.positiveTips + " (" + FrontCameraTemperatureProtectDialog.this.mCurrentCountDown + FrontCameraTemperatureProtectDialog.this.context.getString(51249629) + ")");
                    } else {
                        FrontCameraTemperatureProtectDialog.this.getButton(-1).setEnabled(true);
                        FrontCameraTemperatureProtectDialog.this.getButton(-1).setText(FrontCameraTemperatureProtectDialog.this.positiveTips);
                    }
                    FrontCameraTemperatureProtectDialog frontCameraTemperatureProtectDialog = FrontCameraTemperatureProtectDialog.this;
                    frontCameraTemperatureProtectDialog.mCurrentCountDown = frontCameraTemperatureProtectDialog.mCurrentCountDown - 1;
                    if (FrontCameraTemperatureProtectDialog.this.mCurrentCountDown >= 0) {
                        sendEmptyMessageDelayed(2, 1000);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private String mMessage;
    private String positiveTips;

    public FrontCameraTemperatureProtectDialog(Context context, boolean aboveSystem, String message, String positiveTips) {
        super(context);
        this.context = context;
        Resources res = context.getResources();
        setCancelable(false);
        setMessage(message);
        this.mMessage = message;
        this.positiveTips = positiveTips;
        setButton(-1, positiveTips, this.mHandler.obtainMessage(1));
        if (aboveSystem) {
            getWindow().setType(2010);
        }
        LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 272;
        getWindow().setAttributes(attrs);
        setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                FrontCameraTemperatureProtectDialog.this.getButton(-1).setEnabled(false);
                FrontCameraTemperatureProtectDialog.this.mHandler.sendEmptyMessage(2);
            }
        });
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                FrontCameraTemperatureProtectDialog.this.mCurrentCountDown = 5;
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
