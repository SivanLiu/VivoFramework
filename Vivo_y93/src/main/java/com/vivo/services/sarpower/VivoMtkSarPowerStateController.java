package com.vivo.services.sarpower;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Slog;
import com.iqoo.engineermode.PhoneInterface;
import com.iqoo.engineermode.PhoneInterface.Stub;
import com.vivo.services.rms.ProcessList;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class VivoMtkSarPowerStateController extends VivoSarPowerStateController {
    private static final byte ACTION_SAR_POWER_FAR = (byte) 0;
    private static final byte ACTION_SAR_POWER_NEAR_BODY = (byte) 2;
    private static final byte ACTION_SAR_POWER_NEAR_HEAD = (byte) 1;
    private static final String KEY_FACTORY_MODE = "persist_factory_mode";
    private static final int MSG_SAR_POWER_CHANGE = 0;
    private static final int MSG_SAR_UPDATE_PARAM = 2;
    private static final int PHONE_ID_CDMA = 1;
    private static final int PHONE_ID_GSM = 0;
    private static final String ReductionCommand_2G = "AT+ERFTX=10,1\r\n";
    private static final String ReductionCommand_3G = "AT+ERFTX=10,2\r\n";
    private static final String ReductionCommand_4G = "AT+ERFTX=10,3,1,32,16,0\r\n";
    private static final String ReductionCommand_All = "AT+ERFTX=9,8,8,8,16,16,16\r\n";
    private static final String ReductionCommand_All_For_White_Card = "AT+ERFTX=9,16,16,16,32,32,32\r\n";
    private static final String ReductionCommand_All_For_White_Card_On_C2K = "AT+ERFTX=4,8,0,16,32\r\n";
    private static final String ReductionCommand_All_On_C2K = "AT+ERFTX=4,8,0,8,16\r\n";
    private static final String ResetCommand_2G = "AT+ERFTX=10,1\r\n";
    private static final String ResetCommand_3G = "AT+ERFTX=10,2\r\n";
    private static final String ResetCommand_4G = "AT+ERFTX=10,3,1,0,0,0\r\n";
    private static final String ResetCommand_All = "AT+ERFTX=9,0,0,0,0,0,0";
    private static final String ResetCommand_All_On_C2K = "AT+ERFTX=4,8,0,0,0";
    private static final String TAG = "SarPowerStateService";
    private static Context mContext;
    private static HandlerThread mThread;
    private String[] ReductionCommandsBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    private String[] ReductionCommandsHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    private String[] ReductionCommandsOnC2K = new String[]{"AT+ERFTX=4,8,0,8,16"};
    private String[] ReductionCommandsOnC2KWhite = new String[]{"AT+ERFTX=4,8,0,8,16"};
    private String[] ReductionCommandsWhiteBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    private String[] ReductionCommandsWhiteHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    private boolean isBootCompleted = false;
    private boolean isUnderFactoryMode = SystemProperties.get("persist.sys.factory.mode", "no").equals("yes");
    private CommandConfig mCommandConfig = new CommandConfig();
    private ContentResolver mContentResolver;
    private Looper mMainLooper;
    private PhoneInterface mPhoneInterface = null;
    private PhoneServiceConnection mPhoneServiceConnection = null;
    private PowerChangeHandler mPowerChangeHandler;
    private Runnable mRegisterRunnable = new Runnable() {
        public void run() {
            VivoMtkSarPowerStateController.this.registerObserver();
        }
    };
    private SarSettingsObserver mSarSettingsObserver;

    class PhoneServiceConnection implements ServiceConnection {
        PhoneServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Slog.d(VivoMtkSarPowerStateController.TAG, "onServiceConnected:" + name);
            Slog.d(VivoMtkSarPowerStateController.TAG, "onServiceConnected:" + service);
            try {
                VivoMtkSarPowerStateController.this.mPhoneInterface = Stub.asInterface(service);
            } catch (Exception e) {
                Slog.e(VivoMtkSarPowerStateController.TAG, "Exception", e);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Slog.d(VivoMtkSarPowerStateController.TAG, "onServiceDisconnected:" + name);
        }
    }

    private final class PowerChangeHandler extends Handler {
        public PowerChangeHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    byte powerState;
                    if (VivoMtkSarPowerStateController.this.mSarPowerRfDetectState == 1) {
                        powerState = VivoMtkSarPowerStateController.ACTION_SAR_POWER_FAR;
                    } else if (VivoMtkSarPowerStateController.this.mCardOneState == 1 || VivoMtkSarPowerStateController.this.mCardTwoState == 1) {
                        if (VivoMtkSarPowerStateController.this.mProximityState == 0) {
                            powerState = VivoMtkSarPowerStateController.ACTION_SAR_POWER_NEAR_HEAD;
                        } else {
                            powerState = VivoMtkSarPowerStateController.ACTION_SAR_POWER_NEAR_BODY;
                        }
                    } else if (VivoMtkSarPowerStateController.this.mProximityState == 0 && VivoMtkSarPowerStateController.this.mScreenState == 0) {
                        powerState = VivoMtkSarPowerStateController.ACTION_SAR_POWER_NEAR_HEAD;
                    } else {
                        powerState = VivoMtkSarPowerStateController.ACTION_SAR_POWER_NEAR_BODY;
                    }
                    Slog.d(VivoMtkSarPowerStateController.TAG, "PowerChangeHandler power change, mProximityState = " + VivoMtkSarPowerStateController.this.mProximityState + ", mScreenState = " + VivoMtkSarPowerStateController.this.mScreenState + ", mSarPowerRfDetectState = " + VivoMtkSarPowerStateController.this.mSarPowerRfDetectState + ", mCardOneState = " + VivoMtkSarPowerStateController.this.mCardOneState + ", mCardTwoState = " + VivoMtkSarPowerStateController.this.mCardTwoState + ", mLastCardState = " + VivoMtkSarPowerStateController.this.mLastCardState + ", powerState = " + powerState + ", mLastSarPowerState = " + VivoMtkSarPowerStateController.this.mLastSarPowerState + ", mForceUpdateState = " + VivoMtkSarPowerStateController.this.mForceUpdateState);
                    if (VivoMtkSarPowerStateController.this.mLastSarPowerState != powerState || VivoMtkSarPowerStateController.this.mForceUpdateState) {
                        VivoMtkSarPowerStateController.this.mLastSarPowerState = powerState;
                        if (VivoMtkSarPowerStateController.this.isBootCompleted) {
                            if (VivoMtkSarPowerStateController.this.mForceUpdateState) {
                                VivoMtkSarPowerStateController.this.mForceUpdateState = false;
                            }
                            VivoMtkSarPowerStateController.this.handleSarPowerReduction(powerState);
                            return;
                        }
                        Slog.d(VivoMtkSarPowerStateController.TAG, "not boot completed, so do not react");
                        return;
                    }
                    return;
                case 2:
                    VivoMtkSarPowerStateController.this.updateSarCommandConfig();
                    VivoMtkSarPowerStateController.this.notifyForceUpdateState();
                    VivoMtkSarPowerStateController.this.handleSarMessage(0, ProcessList.SERVICE_ADJ);
                    Slog.i(VivoMtkSarPowerStateController.TAG, "MSG_SAR_UPDATE_PARAM update params");
                    return;
                default:
                    Slog.d(VivoMtkSarPowerStateController.TAG, "PowerChangeHandler default, the mProximityState is" + VivoMtkSarPowerStateController.this.mProximityState);
                    return;
            }
        }
    }

    private final class SarSettingsObserver extends ContentObserver {
        public SarSettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            VivoMtkSarPowerStateController.this.handleSettingsChanged();
        }
    }

    public VivoMtkSarPowerStateController(Context contxt) {
        mContext = contxt;
        this.mContentResolver = mContext.getContentResolver();
        mThread = new HandlerThread("SarPowerStateService_Mtk");
        mThread.start();
        this.mMainLooper = mThread.getLooper();
        this.mPowerChangeHandler = new PowerChangeHandler(this.mMainLooper);
        this.mSarSettingsObserver = new SarSettingsObserver(this.mPowerChangeHandler);
        updateSarCommandConfig();
        this.mPowerChangeHandler.post(this.mRegisterRunnable);
    }

    private void updateSarCommandConfig() {
        this.mCommandConfig.updateSarCommands();
        this.ReductionCommandsHead = this.mCommandConfig.mSarCommandsHead;
        this.ReductionCommandsBody = this.mCommandConfig.mSarCommandsBody;
        this.ReductionCommandsWhiteHead = this.mCommandConfig.mSarCommandsWhiteHead;
        this.ReductionCommandsWhiteBody = this.mCommandConfig.mSarCommandsWhiteBody;
        this.ReductionCommandsOnC2K = this.mCommandConfig.mSarCommandsOnC2K;
        this.ReductionCommandsOnC2KWhite = this.mCommandConfig.mSarCommandsOnC2KWhite;
        Slog.d(TAG, "updateSarCommandConfig");
    }

    public boolean initialPowerState() {
        this.isBootCompleted = true;
        startPhoneService();
        return true;
    }

    public void handleSarMessage(int sarMsg, int deleyTimes) {
        switch (sarMsg) {
            case 0:
                this.mPowerChangeHandler.removeMessages(0);
                this.mPowerChangeHandler.sendMessageDelayed(this.mPowerChangeHandler.obtainMessage(0), (long) deleyTimes);
                return;
            default:
                return;
        }
    }

    private void startPhoneService() {
        Intent intent = new Intent();
        intent.setClassName("com.iqoo.engineermode", "com.iqoo.engineermode.PhoneService");
        this.mPhoneServiceConnection = new PhoneServiceConnection();
        Slog.d(TAG, "startPhoneService");
        mContext.bindService(intent, this.mPhoneServiceConnection, 1);
    }

    private void handleSarPowerReduction(byte enable) {
        if (enable != (byte) 0) {
            String[] ReductionCommands;
            if (enable == ACTION_SAR_POWER_NEAR_HEAD) {
                try {
                    ReductionCommands = (this.mCardOneState == 1 || this.mCardTwoState == 1) ? this.ReductionCommandsWhiteHead : this.ReductionCommandsHead;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            ReductionCommands = (this.mCardOneState == 1 || this.mCardTwoState == 1) ? this.ReductionCommandsWhiteBody : this.ReductionCommandsBody;
            if (this.mCardOneState == 1 || this.mCardTwoState == 1) {
                Slog.e(TAG, "reduce power for white card");
                sendCommand(ResetCommand_All, 0);
                sendCommand("AT+ERFTX=13,7,0,0", 0);
                Thread.sleep(100);
                if (ReductionCommands.length == 0) {
                    Slog.e(TAG, "common command not config, please check it.");
                } else if (ReductionCommands[0].equals("")) {
                    Slog.e(TAG, "common command is empty, no need to reduce power.");
                } else {
                    for (String sendCommand : ReductionCommands) {
                        sendCommand(sendCommand, 0);
                        Thread.sleep(100);
                    }
                }
                if (this.ReductionCommandsOnC2KWhite.length == 0) {
                    Slog.e(TAG, "c2k command not config, please check it.");
                    return;
                } else if (this.ReductionCommandsOnC2KWhite[0].equals("")) {
                    Slog.e(TAG, "c2k command is empty, no need to reduce power.");
                    return;
                } else {
                    for (String sendCommand2 : this.ReductionCommandsOnC2KWhite) {
                        sendCommand(sendCommand2, 1);
                        Thread.sleep(100);
                    }
                    return;
                }
            }
            Slog.e(TAG, "reduce power");
            sendCommand(ResetCommand_All, 0);
            sendCommand("AT+ERFTX=13,7,0,0", 0);
            Thread.sleep(100);
            if (ReductionCommands.length == 0) {
                Slog.e(TAG, "common command not config, please check it.");
            } else if (ReductionCommands[0].equals("")) {
                Slog.e(TAG, "common command is empty, no need to reduce power.");
            } else {
                for (String sendCommand22 : ReductionCommands) {
                    sendCommand(sendCommand22, 0);
                    Thread.sleep(100);
                }
            }
            if (this.ReductionCommandsOnC2K.length == 0) {
                Slog.e(TAG, "c2k command not config, please check it.");
                return;
            } else if (this.ReductionCommandsOnC2K[0].equals("")) {
                Slog.e(TAG, "c2k command is empty, no need to reduce power.");
                return;
            } else {
                for (String sendCommand222 : this.ReductionCommandsOnC2K) {
                    sendCommand(sendCommand222, 1);
                    Thread.sleep(100);
                }
                return;
            }
        }
        Slog.e(TAG, "reset power");
        sendCommand(ResetCommand_All, 0);
        sendCommand("AT+ERFTX=13,7,0,0", 0);
        if (this.ReductionCommandsOnC2K.length == 0 && this.ReductionCommandsOnC2KWhite.length == 0) {
            Slog.e(TAG, "c2k command not config, please check it.");
        } else if (this.ReductionCommandsOnC2K[0].equals("") && (this.ReductionCommandsOnC2KWhite[0].equals("") ^ 1) == 0) {
            Slog.e(TAG, "c2k command is empty, no need to reset power.");
        } else {
            sendCommand(ResetCommand_All_On_C2K, 1);
        }
    }

    private void stopPhoneService() {
        try {
            if (this.mPhoneInterface != null) {
                Slog.d(TAG, "stopPhoneService");
                mContext.unbindService(this.mPhoneServiceConnection);
                this.mPhoneServiceConnection = null;
                this.mPhoneInterface = null;
            }
        } catch (Exception e) {
            Slog.e(TAG, "Exception", e);
        }
    }

    private void sendCommand(String cmd, int id) {
        Slog.d(TAG, "command:" + cmd + ", id:" + id);
        try {
            if (this.mPhoneInterface == null) {
                Slog.e(TAG, "mPhoneInterface is null, cannot send command");
            } else {
                String rsp = this.mPhoneInterface.sendATCommand(cmd, id);
            }
        } catch (Exception ex) {
            Slog.e(TAG, "sendCommand exception:" + ex);
        }
    }

    public void notifySarPowerTest(int powerStateValue) {
    }

    private int getCurrentFactoryMode(int oldMode) {
        int mode = oldMode;
        try {
            mode = Global.getInt(this.mContentResolver, KEY_FACTORY_MODE, 0);
            Slog.i(TAG, "getCurrentFactoryMode:" + mode);
            return mode;
        } catch (Exception e) {
            Slog.e(TAG, "getCurrentFactoryMode failed.");
            return mode;
        }
    }

    private void handleSettingsChanged() {
        int oldMode = this.isUnderFactoryMode ? 1 : 0;
        if (oldMode != getCurrentFactoryMode(oldMode)) {
            this.mPowerChangeHandler.sendMessage(this.mPowerChangeHandler.obtainMessage(2));
            Slog.i(TAG, "mode change. update the params");
            this.isUnderFactoryMode = SystemProperties.get("persist.sys.factory.mode", "no").equals("yes");
        }
    }

    private void registerObserver() {
        mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_FACTORY_MODE), false, this.mSarSettingsObserver, -1);
        Slog.d(TAG, "registerObserver");
    }
}
