package com.vivo.services.popupcamera;

import android.os.Handler;
import android.os.IHwBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.vivo.hardware.vibrator_hall.V1_0.IVibHallStatusCallback;
import vendor.vivo.hardware.vibrator_hall.V1_0.IVibHallStatusCallback.Stub;
import vendor.vivo.hardware.vibrator_hall.V1_0.IVib_Hall;

public class VibHallWrapper {
    private static final String TAG = "PopupCameraManagerService";
    private static volatile boolean isVibHallDied = false;
    private static final Object mVibHallDeathLock = new Object();
    private static VibHallDeathRecipient sVibHallDeathRecipient = null;
    private static final long sVibHallDeathRecipientCookie = 100000;
    private static IVib_Hall sVibHallInstance = null;
    private static VibHallStatusCallback sVibHallStatusCallback;

    private static final class VibHallDeathRecipient implements DeathRecipient {
        private Handler mHandler;

        public VibHallDeathRecipient(Handler handler) {
            this.mHandler = handler;
        }

        public void serviceDied(long cookie) {
            Log.d(VibHallWrapper.TAG, "VibHall Died");
            if (cookie == VibHallWrapper.sVibHallDeathRecipientCookie && VibHallWrapper.sVibHallInstance != null) {
                synchronized (VibHallWrapper.mVibHallDeathLock) {
                    try {
                        VibHallWrapper.sVibHallInstance.unlinkToDeath(VibHallWrapper.sVibHallDeathRecipient);
                    } catch (RemoteException e) {
                        Log.e(VibHallWrapper.TAG, "RemoteException : unable to unlink vibrator_hall DeathRecipient");
                    }
                    VibHallWrapper.sVibHallInstance = null;
                    VibHallWrapper.isVibHallDied = true;
                }
                return;
            }
            return;
        }
    }

    private static final class VibHallStatusCallback extends Stub {
        private Handler mHandler;

        public VibHallStatusCallback(Handler handler) {
            this.mHandler = handler;
        }

        private boolean isValidVibHallStatus(int status) {
            return status == 1 || status == 2 || status == 3 || status == 4 || status == 5 || status == 0;
        }

        private String getStatusTypeString(int status) {
            switch (status) {
                case 0:
                    return "canceled";
                case 1:
                    return "push-ok";
                case 2:
                    return "popup-ok";
                case 3:
                    return "push-jammed";
                case 4:
                    return "popup-jammed";
                case 5:
                    return "pressed";
                default:
                    return "invalid";
            }
        }

        public int onVibHallStatusChanged(int statusType, int extra, int cookie) {
            Log.d(VibHallWrapper.TAG, "onVibHallStatusChanged statusType=" + getStatusTypeString(statusType) + " extra=" + extra + " cookie=" + cookie);
            if (this.mHandler != null && isValidVibHallStatus(statusType)) {
                Message msg = this.mHandler.obtainMessage(statusType);
                msg.arg1 = extra;
                msg.arg2 = cookie;
                this.mHandler.sendMessage(msg);
            }
            return 1;
        }
    }

    public static void initVibHallWrapper(Handler handler) {
        sVibHallDeathRecipient = new VibHallDeathRecipient(handler);
        sVibHallStatusCallback = new VibHallStatusCallback(handler);
    }

    private static IVib_Hall getVibHallService() {
        synchronized (mVibHallDeathLock) {
            if (sVibHallInstance == null) {
                try {
                    sVibHallInstance = IVib_Hall.getService();
                } catch (RemoteException e) {
                    sVibHallInstance = null;
                    e.printStackTrace();
                } catch (NoSuchElementException e1) {
                    sVibHallInstance = null;
                    e1.printStackTrace();
                }
                if (sVibHallInstance != null) {
                    if (isVibHallDied) {
                        Log.d(TAG, "vib_hall service is restarted, get IVib_Hall again");
                    }
                    isVibHallDied = false;
                    try {
                        sVibHallInstance.registVibHallStatusCallback(sVibHallStatusCallback);
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        sVibHallInstance.linkToDeath(sVibHallDeathRecipient, sVibHallDeathRecipientCookie);
                    } catch (RemoteException e22) {
                        e22.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "IVib_Hall.getService() get error !!!");
                }
            }
        }
        return sVibHallInstance;
    }

    public static int openStepVibrator(int cookie) {
        Log.d(TAG, "openStepVibrator cookie=" + cookie);
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return -1;
        }
        try {
            return tmp.open_step_vib(cookie);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int closeStepVibrator(int cookie) {
        Log.d(TAG, "closeStepVibrator cookie=" + cookie);
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return -1;
        }
        try {
            return tmp.close_step_vib(cookie);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int closeStepVibratorAfterFalling() {
        Log.d(TAG, "closeStepVibratorAfterFalling");
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return -1;
        }
        try {
            return tmp.close_step_vib_after_falling();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int openHall() {
        int i = -1;
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return i;
        }
        try {
            return tmp.open_hall();
        } catch (RemoteException e) {
            e.printStackTrace();
            return i;
        }
    }

    public static int closeHall() {
        int i = -1;
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return i;
        }
        try {
            return tmp.close_hall();
        } catch (RemoteException e) {
            e.printStackTrace();
            return i;
        }
    }

    public static int getHallValue() {
        int i = -1;
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return i;
        }
        try {
            return tmp.get_hall_value();
        } catch (RemoteException e) {
            e.printStackTrace();
            return i;
        }
    }

    public static int registVibHallStatusCallback(IVibHallStatusCallback callback) {
        IVib_Hall tmp = getVibHallService();
        if (tmp == null) {
            return -1;
        }
        try {
            tmp.registVibHallStatusCallback(callback);
            return 1;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
