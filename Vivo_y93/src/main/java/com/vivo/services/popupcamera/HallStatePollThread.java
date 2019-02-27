package com.vivo.services.popupcamera;

import android.os.Handler;

public class HallStatePollThread extends Thread {
    private static final boolean DEBUG = true;
    private static final String TAG = "HallStatePollThread";
    private Handler mHandler;

    public HallStatePollThread(String name, Handler handler) {
        super(name);
        this.mHandler = handler;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPollHallState() {
    }

    public void stopPollHallState() {
    }
}
