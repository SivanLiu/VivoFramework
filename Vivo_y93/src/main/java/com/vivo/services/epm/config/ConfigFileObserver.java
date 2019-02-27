package com.vivo.services.epm.config;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.vivo.services.rms.sdk.Consts.ProcessStates;

public class ConfigFileObserver extends FileObserver {
    protected static final int MSG_CONFIG_FILE_CLOSE_WRITE = 1001;
    protected static final int MSG_CONFIG_FILE_DELETE = 1000;
    private static final String TAG = "EPM";
    private Handler handler;
    private String mFilePath;
    private int type;

    public ConfigFileObserver(String path, int type, int flags, Handler handler) {
        super(path, flags);
        this.handler = handler;
        this.type = type;
        this.mFilePath = path;
    }

    public void onEvent(int event, String path) {
        switch (event) {
            case 8:
                Log.d(TAG, this.mFilePath + "write finished");
                Message m = Message.obtain(this.handler, 1001, this.mFilePath);
                m.arg1 = this.type;
                m.sendToTarget();
                return;
            case ProcessStates.PAUSING /*512*/:
            case 1024:
                Log.d(TAG, this.mFilePath + " delete event");
                Message msg = Message.obtain(this.handler, MSG_CONFIG_FILE_DELETE, this.mFilePath);
                msg.arg1 = this.type;
                msg.sendToTarget();
                return;
            default:
                return;
        }
    }
}
