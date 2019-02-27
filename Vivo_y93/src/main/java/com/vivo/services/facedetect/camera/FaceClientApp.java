package com.vivo.services.facedetect.camera;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.util.Slog;

public class FaceClientApp {
    private static final String FACE_ACTION = "com.vivo.action.FACEWINDOWSERVIER";
    private static final String SERVICE_CLASS = "com.vivo.facewindow.FaceWindowService";
    private static final String SERVICE_PACKAGE = "com.vivo.facewindow";
    private static final String TAG = "FaceClientApp";
    private Context mContext;
    private ServiceConnection mFaceWindowServiceConnect = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.i(FaceClientApp.TAG, "conntect service success");
            FaceClientApp.this.serviceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            Slog.i(FaceClientApp.TAG, "onServiceDisconnected");
            FaceClientApp.this.serviceMessenger = null;
        }

        public void onBindingDied(ComponentName name) {
            Slog.i(FaceClientApp.TAG, "onBindingDied");
            FaceClientApp.this.serviceMessenger = null;
        }
    };
    private Messenger serviceMessenger = null;

    public FaceClientApp(Context context) {
        this.mContext = context;
    }

    public void bindFaceWindowService() {
        Slog.i(TAG, "bind FaceWindowService");
        if (this.serviceMessenger == null) {
            Intent intent = new Intent(FACE_ACTION);
            intent.setComponent(new ComponentName(SERVICE_PACKAGE, SERVICE_CLASS));
            if (!this.mContext.bindServiceAsUser(intent, this.mFaceWindowServiceConnect, 1, UserHandle.SYSTEM)) {
                Slog.e(TAG, "bindService fail");
            }
        }
    }

    public void unBindFaceWindowService() {
        Slog.i(TAG, "unBind FaceWindowService");
        if (this.serviceMessenger != null) {
            this.mContext.unbindService(this.mFaceWindowServiceConnect);
        }
    }

    public void sendMessageToFaceWindowService(int what, int arg1, int arg2) {
        try {
            if (this.serviceMessenger != null) {
                Message msg = Message.obtain();
                msg.what = what;
                msg.arg1 = arg1;
                msg.arg2 = arg2;
                this.serviceMessenger.send(msg);
            }
        } catch (Exception e) {
            Slog.e(TAG, "exception fail", e);
        }
    }
}
