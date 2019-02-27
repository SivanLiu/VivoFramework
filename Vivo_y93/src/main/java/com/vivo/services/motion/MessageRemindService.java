package com.vivo.services.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.Vibrator;
import android.util.Log;
import com.sensoroperate.VivoSensorTest;
import com.vivo.common.autobrightness.AblConfig;
import com.vivo.common.provider.Calendar.Events;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public final class MessageRemindService implements IMotionRecognitionService {
    private static int AMD_last_value = 0;
    private static final int DATA_COUNT = 15;
    private static final int MSG_ACCELEROMETER_REGISTER = 5;
    private static final int MSG_ACCELEROMETER_UNREGISTER = 6;
    private static final int MSG_REMIND_ACTION_DET_START = 1;
    private static final int MSG_REMIND_ACTION_DET_START_SURE = 2;
    private static final int MSG_REMIND_ACTION_DET_STOP = 3;
    private static final int MSG_REMIND_ACTION_TRIGER = 4;
    private static String MTK_PLATFORM = "MTK";
    private static String PLATFORM_TAG = "ro.vivo.product.solution";
    private static String QCOM_PLATFORM = "QCOM";
    private static final int SENSOR_TYPE_AMD = 33171006;
    private static final int STATE_COUNT = 5;
    private static final String TAG = "MessageRemindService";
    private static int datacount = 0;
    private static long last_acc_x = 65535;
    private static long last_acc_y = 65535;
    private static long last_acc_z = 65535;
    private static int makeSureStaticCnt = 10;
    private static int one_time_switch = 0;
    private static int registerstate = 0;
    private static MessageRemindService singleMessageRemindService = new MessageRemindService();
    private static int statecount = 0;
    private static int temMakeSureStaticCnt = 0;
    private boolean isMessageRemindServiceAMDWorking = false;
    private boolean isMessageRemindServiceEintWorking = false;
    private boolean isMessageRemindServiceWorking = false;
    private MotionSensorEventListener mAMDListener = new MotionSensorEventListener(this, null);
    private WakeLock mAWakeLock = null;
    private float[] mAccSensorVal = new float[3];
    private MotionSensorEventListener mAcceleromererListener = new MotionSensorEventListener(this, null);
    private Handler mCallBackHandler = null;
    private Context mContext = null;
    private FlatPositionInfo mFlatPositionInfo = new FlatPositionInfo();
    private SensorManager mSensorManager;
    private SensorTrigerObserver mSensorTrigerObserver = new SensorTrigerObserver();
    private Handler mServiceHandler = null;
    private VivoSensorTest mVivoSensorTest = null;
    private PowerManager pm = null;

    private class MessageRemindServiceHandler extends Handler {
        PowerManager pm = ((PowerManager) MessageRemindService.this.mContext.getSystemService("power"));
        WakeLock wl = this.pm.newWakeLock(1, "SmartRemind");

        public MessageRemindServiceHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            String platform = SystemProperties.get("ro.vivo.product.platform", null);
            Log.d(MessageRemindService.TAG, "prop:" + prop + " platform:" + platform);
            Log.d(MessageRemindService.TAG, "MessageRemindServiceHandler handleMessage " + msg.what);
            switch (msg.what) {
                case 1:
                    if (MessageRemindService.this.mFlatPositionInfo.IsDevInFlatState()) {
                        this.wl.acquire(200);
                        if (MessageRemindService.this.mServiceHandler != null) {
                            MessageRemindService.this.mServiceHandler.sendEmptyMessageDelayed(2, 100);
                            break;
                        }
                    }
                    break;
                case 2:
                    if (!MessageRemindService.this.mFlatPositionInfo.IsDevInFlatState()) {
                        MessageRemindService.temMakeSureStaticCnt = 0;
                        break;
                    }
                    MessageRemindService.temMakeSureStaticCnt = MessageRemindService.temMakeSureStaticCnt + 1;
                    if (MessageRemindService.temMakeSureStaticCnt < MessageRemindService.makeSureStaticCnt) {
                        this.wl.acquire(200);
                        if (MessageRemindService.this.mServiceHandler != null) {
                            MessageRemindService.this.mServiceHandler.sendEmptyMessageDelayed(2, 100);
                            break;
                        }
                    }
                    MessageRemindService.this.mSensorTrigerObserver.StartSensorTrigerObserver();
                    MessageRemindService.temMakeSureStaticCnt = 0;
                    break;
                    break;
                case 3:
                    if (MessageRemindService.this.isMessageRemindServiceEintWorking || MessageRemindService.this.isMessageRemindServiceAMDWorking) {
                        if (MessageRemindService.this.isMessageRemindServiceAMDWorking) {
                            MessageRemindService.this.mSensorManager.unregisterListener(MessageRemindService.this.mAMDListener);
                            MessageRemindService.this.isMessageRemindServiceAMDWorking = false;
                        }
                        MessageRemindService.this.mSensorTrigerObserver.StopSensorTrigerObserver();
                        break;
                    }
                    Log.w(MessageRemindService.TAG, "MessageRemindService Eint is not working,just return");
                    return;
                    break;
                case 4:
                    Log.d(MessageRemindService.TAG, "MSG_REMIND_ACTION_TRIGER+++");
                    Message smsg = Message.obtain();
                    smsg.what = 16;
                    smsg.obj = new Integer(2);
                    if (MessageRemindService.this.mCallBackHandler != null) {
                        MessageRemindService.this.mCallBackHandler.sendMessage(smsg);
                        Vibrator mVibrator = (Vibrator) MessageRemindService.this.mContext.getSystemService("vibrator");
                        if (platform.equals("MTK6765")) {
                            mVibrator.vibrate(new long[]{10, 150, 100, 150}, -1);
                        } else {
                            mVibrator.vibrate(new long[]{1000, 150, 100, 150}, -1);
                        }
                    }
                    Log.d(MessageRemindService.TAG, "MSG_REMIND_ACTION_TRIGER---");
                    if (MessageRemindService.this.mSensorManager != null && MessageRemindService.this.isMessageRemindServiceAMDWorking) {
                        MessageRemindService.this.mSensorManager.unregisterListener(MessageRemindService.this.mAMDListener);
                        MessageRemindService.this.isMessageRemindServiceAMDWorking = false;
                        Log.d(MessageRemindService.TAG, "unregisterListener AMD after triger");
                        break;
                    }
                case 5:
                    if (MessageRemindService.this.mSensorManager != null && MessageRemindService.registerstate == 0) {
                        MessageRemindService.this.mSensorManager.registerListener(MessageRemindService.this.mAcceleromererListener, MessageRemindService.this.mSensorManager.getDefaultSensor(1), 20000, MessageRemindService.this.mServiceHandler);
                        MessageRemindService.registerstate = 1;
                        break;
                    }
                case 6:
                    if (MessageRemindService.this.mSensorManager != null && MessageRemindService.registerstate == 1) {
                        MessageRemindService.this.mSensorManager.unregisterListener(MessageRemindService.this.mAcceleromererListener);
                        MessageRemindService.registerstate = 0;
                        break;
                    }
            }
        }
    }

    private class MotionSensorEventListener implements SensorEventListener {
        /* synthetic */ MotionSensorEventListener(MessageRemindService this$0, MotionSensorEventListener -this1) {
            this();
        }

        private MotionSensorEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            Log.d(MessageRemindService.TAG, " onSensorChanged: type=" + event.sensor.getType() + ", value=" + event.values[0] + "," + event.values[1] + "," + event.values[2]);
            switch (event.sensor.getType()) {
                case 1:
                    if (prop == null) {
                        return;
                    }
                    if (AllConfig.mIsArchADSP || AllConfig.mIsNewMTKArch) {
                        MessageRemindService.datacount = MessageRemindService.datacount + 1;
                        if (MessageRemindService.datacount >= 5) {
                            if (MessageRemindService.this.IsDevInFlatState_startstate(event)) {
                                MessageRemindService.statecount = MessageRemindService.statecount + 1;
                            } else {
                                MessageRemindService.statecount = 0;
                            }
                        }
                        if (MessageRemindService.statecount >= 5) {
                            MessageRemindService.statecount = 0;
                            if (MessageRemindService.this.mServiceHandler != null) {
                                MessageRemindService.this.mServiceHandler.sendEmptyMessageDelayed(6, 0);
                            }
                            if (MessageRemindService.this.mSensorManager != null && AllConfig.mIsNewMTKArch) {
                                Log.d(MessageRemindService.TAG, " new mtkarch registerListener AMD  ");
                                MessageRemindService.this.mSensorManager.registerListener(MessageRemindService.this.mAMDListener, MessageRemindService.this.mSensorManager.getDefaultSensor(MessageRemindService.SENSOR_TYPE_AMD), 20000, MessageRemindService.this.mServiceHandler);
                                MessageRemindService.this.isMessageRemindServiceAMDWorking = true;
                            }
                        }
                        if (MessageRemindService.statecount < 5 && MessageRemindService.datacount >= 15) {
                            MessageRemindService.statecount = 0;
                            if (MessageRemindService.this.mServiceHandler != null) {
                                MessageRemindService.this.mServiceHandler.sendEmptyMessageDelayed(6, 0);
                            } else if (MessageRemindService.datacount >= 30 && MessageRemindService.this.mSensorManager != null && MessageRemindService.registerstate == 1) {
                                MessageRemindService.this.mSensorManager.unregisterListener(MessageRemindService.this.mAcceleromererListener);
                                MessageRemindService.registerstate = 0;
                                Log.d(MessageRemindService.TAG, " service has been stop, need to unregister acc ");
                            }
                        }
                        Log.d(MessageRemindService.TAG, " datacount: " + MessageRemindService.datacount + " statecount: " + MessageRemindService.statecount);
                        return;
                    }
                    return;
                case MessageRemindService.SENSOR_TYPE_AMD /*33171006*/:
                    Message msg;
                    if (prop != null && AllConfig.mIsArchADSP) {
                        if (event.values[0] == 2.0f && MessageRemindService.AMD_last_value == 1 && MessageRemindService.this.isMessageRemindServiceWorking && MessageRemindService.one_time_switch == 0) {
                            MessageRemindService.one_time_switch = 1;
                            msg = Message.obtain();
                            msg.what = 4;
                            if (MessageRemindService.this.mServiceHandler != null) {
                                MessageRemindService.this.mServiceHandler.sendMessage(msg);
                            }
                        }
                        MessageRemindService.AMD_last_value = (int) event.values[0];
                    }
                    if (prop != null && AllConfig.mIsNewMTKArch && MessageRemindService.this.isMessageRemindServiceWorking && event.values[0] == 2.0f) {
                        msg = Message.obtain();
                        msg.what = 4;
                        if (MessageRemindService.this.mServiceHandler != null) {
                            MessageRemindService.this.mServiceHandler.sendMessage(msg);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class SensorTrigerObserver extends UEventObserver {
        private static final String DEV_NAME = "/sys/class/switch/gsensor/name";
        private static final String DEV_PATH = "DEVPATH=/devices/virtual/switch/gsensor";
        private static final String DEV_STATE = "/sys/class/switch/gsensor/state";

        SensorTrigerObserver() {
            char[] buffer = new char[1024];
            String newName = null;
            int newState = 0;
            try {
                FileReader file = new FileReader(DEV_STATE);
                int len = file.read(buffer, 0, 1024);
                file.close();
                newState = Integer.valueOf(new String(buffer, 0, len).trim()).intValue();
                file = new FileReader(DEV_NAME);
                len = file.read(buffer, 0, 1024);
                file.close();
                newName = new String(buffer, 0, len).trim();
            } catch (FileNotFoundException e) {
                Log.w(MessageRemindService.TAG, "gsensor eint not support");
            } catch (Exception e2) {
                Log.e(MessageRemindService.TAG, Events.DEFAULT_SORT_ORDER, e2);
            }
            update(newName, newState);
            if (SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null) != null && AllConfig.mIsArchADSP) {
                MessageRemindService.this.mVivoSensorTest = VivoSensorTest.getInstance();
            }
        }

        public void StartSensorTrigerObserver() {
            startObserving(DEV_PATH);
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            String PLATFORM_INFO = SystemProperties.get(MessageRemindService.PLATFORM_TAG);
            Log.d(MessageRemindService.TAG, "StartSensorTrigerObserver prop:" + prop);
            if (AllConfig.mIsArchADSP) {
                int[] AccSensorOperate = new int[3];
                AccSensorOperate[0] = 19;
                if (!(MessageRemindService.this.mVivoSensorTest == null || MessageRemindService.this.mVivoSensorTest.VivoSensorOprate(57, MessageRemindService.this.mAccSensorVal, AccSensorOperate, AccSensorOperate.length) == 0)) {
                    Log.d(MessageRemindService.TAG, "set acc sensor int fail 1");
                }
            } else {
                MessageRemindService.this.writeFile("/sys/bus/platform/drivers/gsensor/set_eint", "1");
            }
            MessageRemindService.this.isMessageRemindServiceEintWorking = true;
        }

        public void StopSensorTrigerObserver() {
            stopObserving();
            String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
            String PLATFORM_INFO = SystemProperties.get(MessageRemindService.PLATFORM_TAG);
            Log.d(MessageRemindService.TAG, "StopSensorTrigerObserver prop:" + prop);
            if (AllConfig.mIsNewMTKArch) {
                Log.d(MessageRemindService.TAG, "StopSensorTrigerObserver new mtk arch");
            } else if (AllConfig.mIsArchADSP) {
                int[] AccSensorOperate = new int[3];
                AccSensorOperate[0] = 20;
                if (!(MessageRemindService.this.mVivoSensorTest == null || MessageRemindService.this.mVivoSensorTest.VivoSensorOprate(57, MessageRemindService.this.mAccSensorVal, AccSensorOperate, AccSensorOperate.length) == 0)) {
                    Log.d(MessageRemindService.TAG, "set acc sensor int fail 2");
                }
            } else {
                MessageRemindService.this.writeFile("/sys/bus/platform/drivers/gsensor/set_eint", "0");
            }
            MessageRemindService.this.isMessageRemindServiceEintWorking = false;
        }

        public void onUEvent(UEvent event) {
            Log.d(MessageRemindService.TAG, "SensorTrigerObserver" + event.toString());
            try {
                update(event.get("SWITCH_NAME"), Integer.parseInt(event.get("SWITCH_STATE")));
            } catch (NumberFormatException e) {
                Log.d(MessageRemindService.TAG, "Could not parse switch state from event " + event);
            }
        }

        private final synchronized void update(String newName, int newState) {
            if (newState == 1) {
                if (MessageRemindService.this.isMessageRemindServiceWorking) {
                    Message msg;
                    if (AllConfig.mLimitSwitch) {
                        Log.d(MessageRemindService.TAG, "update one_time_switch " + MessageRemindService.one_time_switch);
                        if (MessageRemindService.one_time_switch == 0) {
                            MessageRemindService.one_time_switch = 1;
                            msg = Message.obtain();
                            msg.what = 4;
                            if (MessageRemindService.this.mServiceHandler != null) {
                                MessageRemindService.this.mServiceHandler.sendMessage(msg);
                            }
                        }
                    } else {
                        msg = Message.obtain();
                        msg.what = 4;
                        if (MessageRemindService.this.mServiceHandler != null) {
                            MessageRemindService.this.mServiceHandler.sendMessage(msg);
                        }
                    }
                }
            }
            Log.d(MessageRemindService.TAG, "SensorTrigerObserver update( " + newName + ", " + newState + ");");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0033 A:{SYNTHETIC, Splitter: B:14:0x0033} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A:{SYNTHETIC, Splitter: B:19:0x003c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile(String path, String data) {
        Throwable th;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data.getBytes());
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (IOException e) {
                    }
                }
                fos = fos2;
            } catch (IOException e2) {
                fos = fos2;
                try {
                    Log.d(TAG, "Unable to write " + path);
                    if (fos == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                if (fos != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            Log.d(TAG, "Unable to write " + path);
            if (fos == null) {
                try {
                    fos.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a9 A:{SYNTHETIC, Splitter: B:26:0x00a9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readFile(String fileName) {
        FileNotFoundException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(fileName)));
            try {
                tempString = reader2.readLine();
                reader2.close();
            } catch (Exception e2) {
                try {
                    Log.d("TAG", "reader.readLine():" + e2.getMessage());
                } catch (FileNotFoundException e3) {
                    e = e3;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Log.d("TAG", "the readFile is:" + e1.getMessage());
                }
            }
            reader = reader2;
        } catch (FileNotFoundException e4) {
            e = e4;
            try {
                Log.d("TAG", "the readFile is:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Log.d("TAG", "the readFile is:" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Log.d("TAG", "the readFile is:" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    public boolean IsDevInFlatState_startstate(SensorEvent event) {
        boolean result;
        long acc_x = (long) (event.values[0] * 1000.0f);
        long acc_y = (long) (event.values[1] * 1000.0f);
        long acc_z = (long) (event.values[2] * 1000.0f);
        long comp_sum = ((acc_x * acc_x) + (acc_y * acc_y)) + (acc_z * acc_z);
        if (comp_sum >= 132250000 || comp_sum <= 72250000) {
            last_acc_x = 65535;
            last_acc_y = 65535;
            last_acc_z = 65535;
            result = false;
        } else {
            result = true;
        }
        if (!(!result || last_acc_x == 65535 || last_acc_y == 65535 || last_acc_z == 65535 || (Math.abs(acc_x - last_acc_x) <= 2000 && Math.abs(acc_y - last_acc_y) <= 2000 && Math.abs(acc_z - last_acc_z) <= 2000))) {
            result = false;
        }
        Log.d(TAG, "IsDevInFlatState--" + acc_x + "," + acc_y + "," + acc_z + "," + comp_sum + "," + last_acc_x + "," + last_acc_y + "," + last_acc_z + "," + result);
        last_acc_x = acc_x;
        last_acc_y = acc_y;
        last_acc_z = acc_z;
        return result;
    }

    public static MessageRemindService getInstance() {
        return singleMessageRemindService;
    }

    private MessageRemindService() {
        Log.d(TAG, "MessageRemindService Creat");
    }

    public boolean startMotionRecognitionService(Context context, Handler handler) {
        if (!this.isMessageRemindServiceWorking) {
            this.mContext = context;
            this.isMessageRemindServiceWorking = true;
            this.isMessageRemindServiceAMDWorking = false;
            this.mCallBackHandler = handler;
            this.mServiceHandler = new MessageRemindServiceHandler(handler.getLooper());
            temMakeSureStaticCnt = 0;
            registerstate = 0;
            datacount = 0;
            statecount = 0;
            one_time_switch = 0;
            AMD_last_value = 0;
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.pm = (PowerManager) this.mContext.getSystemService("power");
            this.mAWakeLock = this.pm.newWakeLock(1, TAG);
        }
        Log.d(TAG, "startMotionRecognitionService");
        String prop = SystemProperties.get(AblConfig.PROP_PRODUCT_MODEL, null);
        if (!(prop == null || !AllConfig.mIsArchADSP || this.mServiceHandler == null || this.mAWakeLock == null)) {
            this.mAWakeLock.acquire(5000);
            Log.d(TAG, "mAWakeLock success");
            this.mServiceHandler.sendEmptyMessageDelayed(5, 1500);
        }
        if (!(prop == null || !AllConfig.mIsNewMTKArch || this.mServiceHandler == null || this.mAWakeLock == null)) {
            this.mAWakeLock.acquire(5000);
            Log.d(TAG, "mAWakeLock success for new mtk arch");
            this.mServiceHandler.sendEmptyMessageDelayed(5, 1500);
        }
        Message msg = Message.obtain();
        if (prop == null || !AllConfig.mIsArchADSP) {
            if (prop == null || !AllConfig.mIsNewMTKArch) {
                msg.what = 1;
                if (this.mServiceHandler != null) {
                    this.mAWakeLock.acquire(2000);
                    this.mServiceHandler.sendEmptyMessageDelayed(1, 1500);
                }
            } else {
                Log.d(TAG, "new mtk arch not register Sensor Triger Observer");
            }
        }
        if (this.mSensorManager != null && AllConfig.mIsArchADSP) {
            Log.d(TAG, " registerListener AMD  ");
            this.mSensorManager.registerListener(this.mAMDListener, this.mSensorManager.getDefaultSensor(SENSOR_TYPE_AMD), 20000, this.mServiceHandler);
            this.isMessageRemindServiceAMDWorking = true;
        }
        return true;
    }

    public boolean stopMotionRecognitionService() {
        Log.d(TAG, "stopMotionRecognitionService");
        Message msg = Message.obtain();
        msg.what = 3;
        AMD_last_value = 0;
        if (this.mServiceHandler != null) {
            this.mServiceHandler.removeMessages(2);
            this.mServiceHandler.sendMessage(msg);
            this.mServiceHandler.sendEmptyMessageDelayed(6, 0);
        }
        if (this.isMessageRemindServiceWorking) {
            this.isMessageRemindServiceWorking = false;
            this.mCallBackHandler = null;
            this.mServiceHandler = null;
        }
        return true;
    }
}
