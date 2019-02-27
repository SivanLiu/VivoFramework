package com.vivo.services.popupcamera;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;

final class FrontCameraErrorDialog extends BaseErrorDialog {
    private static final int FORCE_CLOSE = 1;
    private static final String TAG = "FrontCameraErrorDialog";
    private static final int WAIT = 2;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = msg.what;
            FrontCameraErrorDialog.this.dismiss();
        }
    };

    public FrontCameraErrorDialog(Context context, boolean aboveSystem, String message) {
        super(context);
        Resources res = context.getResources();
        setCancelable(true);
        setMessage(message);
        setButton(-1, res.getText(17039961), this.mHandler.obtainMessage(1));
        if (aboveSystem) {
            getWindow().setType(2010);
        }
        LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 272;
        getWindow().setAttributes(attrs);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
