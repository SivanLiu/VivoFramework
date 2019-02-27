package com.vivo.services.sarpower;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoQcomSarPowerStateController extends VivoSarPowerStateController {
    private static final String ACTION_CLOSE_CAMERA = "com.android.camera.ACTION_CLOSE_CAMERA";
    private static final String ACTION_OPEN_CAMERA = "com.android.camera.ACTION_OPEN_CAMERA";
    private static final byte ACTION_SAR_POWER_FALL_1 = (byte) 1;
    private static final byte ACTION_SAR_POWER_FALL_1_PD1728 = (byte) 1;
    private static final byte ACTION_SAR_POWER_FALL_2 = (byte) 2;
    private static final byte ACTION_SAR_POWER_FALL_4 = (byte) 4;
    private static final byte ACTION_SAR_POWER_FAR = (byte) 0;
    private static final int ACTION_SAR_POWER_MAX = 8;
    private static final int ACTION_SAR_POWER_MIN = 0;
    private static final byte ACTION_SAR_POWER_SIM_BODY_PD1728F_EX = (byte) 1;
    private static final byte ACTION_SAR_POWER_SIM_HEAD_PD1728F_EX = (byte) 4;
    private static final byte ACTION_SAR_POWER_WHITE_CARD = (byte) 3;
    private static final byte ACTION_SAR_POWER_WHITE_CARD_BODY_PD1728F_EX = (byte) 3;
    private static final byte ACTION_SAR_POWER_WHITE_CARD_HEAD_PD1728F_EX = (byte) 2;
    private static final byte ACTION_SAR_POWER_WHITE_CARD_PD1728 = (byte) 2;
    private static final String CAMERA_ID = "mCameraId";
    private static final int MSG_SAR_POWER_CHANGE = 0;
    private static final String QcRilHookClassName = "com.qualcomm.qcrilhook.QcRilHook";
    private static final String SetSarPowerMethodName = "qcRilSetSarPower";
    private static final String TAG = "SarPowerStateService";
    private static Context mContext;
    private static HandlerThread mThread;
    private static final String model = SystemProperties.get("ro.vivo.product.model", "unkown").toLowerCase();
    private Class<?> QcRilHook = null;
    private Constructor<?>[] cons = null;
    private boolean isOpenFrontCamera = false;
    private final BroadcastReceiver mCameraEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(VivoQcomSarPowerStateController.TAG, "mCameraEventReceiver action:" + action);
            String cameraIDStr;
            if (VivoQcomSarPowerStateController.ACTION_OPEN_CAMERA.equals(action)) {
                try {
                    cameraIDStr = intent.getStringExtra(VivoQcomSarPowerStateController.CAMERA_ID);
                    Slog.d(VivoQcomSarPowerStateController.TAG, "mCameraEventReceiver:  cameraIDStr " + cameraIDStr);
                    if (Integer.parseInt(cameraIDStr) == 1) {
                        VivoQcomSarPowerStateController.this.isOpenFrontCamera = true;
                        VivoQcomSarPowerStateController.this.mPowerChangeHandler.removeMessages(0);
                        VivoQcomSarPowerStateController.this.mPowerChangeHandler.sendMessageDelayed(VivoQcomSarPowerStateController.this.mPowerChangeHandler.obtainMessage(0), 0);
                    }
                } catch (Exception e) {
                    Slog.d(VivoQcomSarPowerStateController.TAG, "ACTION_OPEN_CAMERA exp");
                }
            } else if (VivoQcomSarPowerStateController.ACTION_CLOSE_CAMERA.equals(action)) {
                try {
                    cameraIDStr = intent.getStringExtra(VivoQcomSarPowerStateController.CAMERA_ID);
                    Slog.d(VivoQcomSarPowerStateController.TAG, "mCameraEventReceiver:  cameraIDStr " + cameraIDStr);
                    if (Integer.parseInt(cameraIDStr) == 1) {
                        VivoQcomSarPowerStateController.this.isOpenFrontCamera = false;
                        VivoQcomSarPowerStateController.this.mPowerChangeHandler.removeMessages(0);
                        VivoQcomSarPowerStateController.this.mPowerChangeHandler.sendMessageDelayed(VivoQcomSarPowerStateController.this.mPowerChangeHandler.obtainMessage(0), 0);
                    }
                } catch (Exception e2) {
                    Slog.d(VivoQcomSarPowerStateController.TAG, "ACTION_OPEN_CAMERA exp");
                }
            }
        }
    };
    private Looper mMainLooper;
    private PowerChangeHandler mPowerChangeHandler;
    private Object mSarQcRilHook = null;
    private Method qcRilSetSarPower = null;

    private final class PowerChangeHandler extends Handler {
        public PowerChangeHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    byte powerState;
                    if (VivoQcomSarPowerStateController.model.equals("pd1728f_ex")) {
                        if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                powerState = (byte) 2;
                            } else {
                                powerState = (byte) 3;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                            powerState = (byte) 4;
                        } else {
                            powerState = (byte) 1;
                        }
                    } else if (VivoQcomSarPowerStateController.model.startsWith("pd1728")) {
                        if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                powerState = (byte) 2;
                            } else {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                            powerState = (byte) 1;
                        } else {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        }
                    } else if (VivoQcomSarPowerStateController.model.equals("pd1730c") || VivoQcomSarPowerStateController.model.equals("pd1730cf_ex")) {
                        if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                if (VivoQcomSarPowerStateController.this.isOpenFrontCamera) {
                                    powerState = (byte) 7;
                                } else {
                                    powerState = (byte) 3;
                                }
                            } else if (VivoQcomSarPowerStateController.this.isOpenFrontCamera) {
                                powerState = (byte) 8;
                            } else {
                                powerState = (byte) 4;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                            if (VivoQcomSarPowerStateController.this.isOpenFrontCamera) {
                                powerState = (byte) 5;
                            } else {
                                powerState = (byte) 1;
                            }
                        } else if (VivoQcomSarPowerStateController.this.isOpenFrontCamera) {
                            powerState = (byte) 6;
                        } else {
                            powerState = (byte) 2;
                        }
                    } else if (VivoQcomSarPowerStateController.model.equals("pd1730d") || VivoQcomSarPowerStateController.model.equals("pd1730df_ex")) {
                        if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                powerState = (byte) 3;
                            } else {
                                powerState = (byte) 4;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                            powerState = (byte) 1;
                        } else {
                            powerState = (byte) 2;
                        }
                    } else if (!VivoQcomSarPowerStateController.model.startsWith("pd1731") || (VivoQcomSarPowerStateController.model.startsWith("pd1731f_ex") ^ 1) == 0 || (VivoQcomSarPowerStateController.model.startsWith("pd1731c") ^ 1) == 0) {
                        if (VivoQcomSarPowerStateController.model.startsWith("pd1806")) {
                            if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                                if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                    powerState = (byte) 2;
                                } else {
                                    powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                                }
                            } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                                powerState = (byte) 1;
                            } else {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            }
                        } else if (VivoQcomSarPowerStateController.model.startsWith("pd1635") || VivoQcomSarPowerStateController.model.startsWith("pd1619") || VivoQcomSarPowerStateController.model.startsWith("pd1616") || VivoQcomSarPowerStateController.model.startsWith("pd1624")) {
                            if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                                powerState = (byte) 1;
                            } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1)) {
                                powerState = (byte) 1;
                            } else {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            }
                        } else if (VivoQcomSarPowerStateController.model.startsWith("pd1610")) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                                powerState = (byte) 1;
                            } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1)) {
                                powerState = (byte) 1;
                            } else if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                                powerState = (byte) 1;
                            } else {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            }
                        } else if (VivoQcomSarPowerStateController.model.startsWith("pd1708") || VivoQcomSarPowerStateController.model.startsWith("pd1718")) {
                            if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                                powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                            } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                                if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                    powerState = (byte) 3;
                                } else {
                                    powerState = (byte) 4;
                                }
                            } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                                powerState = (byte) 1;
                            } else {
                                powerState = (byte) 2;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                            if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                                powerState = (byte) 3;
                            } else {
                                powerState = (byte) 4;
                            }
                        } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                            powerState = (byte) 1;
                        } else {
                            powerState = (byte) 2;
                        }
                    } else if (VivoQcomSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                        powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                    } else if (VivoQcomSarPowerStateController.this.mCardOneState == 1 || VivoQcomSarPowerStateController.this.mCardTwoState == 1) {
                        if (VivoQcomSarPowerStateController.this.mProximityState == 0) {
                            powerState = (byte) 2;
                        } else {
                            powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                        }
                    } else if (VivoQcomSarPowerStateController.this.mProximityState == 0 && VivoQcomSarPowerStateController.this.mScreenState == 0) {
                        powerState = (byte) 1;
                    } else {
                        powerState = VivoQcomSarPowerStateController.ACTION_SAR_POWER_FAR;
                    }
                    Slog.d(VivoQcomSarPowerStateController.TAG, "PowerChangeHandler power change, mProximityState = " + VivoQcomSarPowerStateController.this.mProximityState + ", mScreenState = " + VivoQcomSarPowerStateController.this.mScreenState + ", mSarPowerRfDetectState = " + VivoQcomSarPowerStateController.this.mSarPowerRfDetectState + ", mCardOneState = " + VivoQcomSarPowerStateController.this.mCardOneState + ", mCardTwoState = " + VivoQcomSarPowerStateController.this.mCardTwoState + ", mLastCardState = " + VivoQcomSarPowerStateController.this.mLastCardState + ", isOpenFrontCamera = " + VivoQcomSarPowerStateController.this.isOpenFrontCamera + ", powerState = " + powerState + ", mLastSarPowerState = " + VivoQcomSarPowerStateController.this.mLastSarPowerState);
                    if (VivoQcomSarPowerStateController.this.mLastSarPowerState != powerState) {
                        VivoQcomSarPowerStateController.this.mLastSarPowerState = powerState;
                        VivoQcomSarPowerStateController.this.setQcRiSarPowerState(powerState);
                        return;
                    }
                    return;
                default:
                    Slog.d(VivoQcomSarPowerStateController.TAG, "PowerChangeHandler default, the mProximityState is" + VivoQcomSarPowerStateController.this.mProximityState);
                    return;
            }
        }
    }

    public VivoQcomSarPowerStateController(Context contxt) {
        mContext = contxt;
        mThread = new HandlerThread("SarPowerStateService_Qcom");
        mThread.start();
        this.mMainLooper = mThread.getLooper();
        this.mPowerChangeHandler = new PowerChangeHandler(this.mMainLooper);
        if (model.equals("pd1730c") || model.equals("pd1730cf_ex")) {
            IntentFilter filterOpenCamera = new IntentFilter();
            filterOpenCamera.addAction(ACTION_OPEN_CAMERA);
            mContext.registerReceiver(this.mCameraEventReceiver, filterOpenCamera);
            IntentFilter filterCloseCamera = new IntentFilter();
            filterCloseCamera.addAction(ACTION_CLOSE_CAMERA);
            mContext.registerReceiver(this.mCameraEventReceiver, filterCloseCamera);
        }
    }

    public boolean initialPowerState() {
        if (sarQcRilHookReflect()) {
            return true;
        }
        Slog.e(TAG, "sarQcRilHookReflect init fail");
        return false;
    }

    public void handleSarMessage(int sarMsg, int delayTimes) {
        switch (sarMsg) {
            case 0:
                this.mPowerChangeHandler.removeMessages(0);
                this.mPowerChangeHandler.sendMessageDelayed(this.mPowerChangeHandler.obtainMessage(0), (long) delayTimes);
                return;
            default:
                return;
        }
    }

    private boolean sarQcRilHookReflect() {
        try {
            this.QcRilHook = Class.forName(QcRilHookClassName);
            if (this.QcRilHook == null) {
                Slog.e(TAG, "QcRilHook class get fail");
                return false;
            }
            this.cons = this.QcRilHook.getConstructors();
            if (this.cons == null) {
                Slog.e(TAG, "Constructors get fail");
                return false;
            }
            this.mSarQcRilHook = this.cons[0].newInstance(new Object[]{mContext});
            if (this.mSarQcRilHook == null) {
                Slog.e(TAG, "mSarQcRilHook Object get fail");
                return false;
            }
            this.qcRilSetSarPower = this.QcRilHook.getMethod(SetSarPowerMethodName, new Class[]{Byte.TYPE});
            if (this.qcRilSetSarPower != null) {
                return true;
            }
            Slog.e(TAG, "mSarQcRilHook Method get fail");
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "sarQcRilHookReflect throws exception ");
            e.printStackTrace();
            return false;
        }
    }

    private void setQcRiSarPowerState(byte powerState) {
        if (this.qcRilSetSarPower == null) {
            Slog.d(TAG, "qcRilSetSarPower is null return");
            return;
        }
        try {
            if (!((Boolean) this.qcRilSetSarPower.invoke(this.mSarQcRilHook, new Object[]{new Byte(powerState)})).booleanValue()) {
                Slog.e(TAG, "writeSarTestMode return fail");
            }
        } catch (Exception e) {
            Slog.e(TAG, "setQcRiSarPowerState throws exception ");
            e.printStackTrace();
        }
        Slog.d(TAG, "setQcRiSarPowerState powerState = " + powerState);
    }

    public void notifySarPowerTest(int powerStateValue) {
        if (powerStateValue <= 8 && powerStateValue >= 0) {
            setQcRiSarPowerState((byte) powerStateValue);
        }
    }
}
