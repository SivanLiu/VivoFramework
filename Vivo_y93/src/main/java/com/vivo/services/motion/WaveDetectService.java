package com.vivo.services.motion;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public final class WaveDetectService implements IMotionRecognitionService {
    private static final int MSG_WAVE_DET_TEST = 1;
    private static WaveDetectService singleWaveDetectService = new WaveDetectService();
    private boolean isWaveDetectServiceWorking = false;
    private Handler mCallBackHandler = null;
    private Handler mServiceHandler = null;

    private class WaveDetectServiceHandler extends Handler {
        public WaveDetectServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(3);
                    WaveDetectService.this.mCallBackHandler.sendMessageDelayed(smsg, 2000);
                    return;
                default:
                    return;
            }
        }
    }

    public static WaveDetectService getInstance() {
        return singleWaveDetectService;
    }

    private WaveDetectService() {
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        if (!this.isWaveDetectServiceWorking) {
            this.isWaveDetectServiceWorking = true;
            this.mCallBackHandler = handler;
            this.mServiceHandler = new WaveDetectServiceHandler(handler.getLooper());
        }
        Message msg = Message.obtain();
        msg.what = 1;
        this.mServiceHandler.sendMessage(msg);
        return true;
    }

    public boolean stopMotionRecognitionService() {
        if (this.isWaveDetectServiceWorking) {
            this.isWaveDetectServiceWorking = false;
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
        }
        return true;
    }
}
