package com.vivo.services.motion;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.vivo.common.VivoCollectData;
import java.util.ArrayList;
import java.util.List;
import vivo.app.VivoFrameworkFactory;
import vivo.app.motion.IMotionManager;
import vivo.app.motion.IMotionManager.Stub;

@VivoHook(hookType = VivoHookType.PUBLIC_API_CLASS)
public class MotionRecognitionManager {
    public static final int ACTION_TYPE_DIRECT_CALL = 1;
    public static final int ACTION_TYPE_MESSAGE_REMIND = 2;
    public static final int ACTION_TYPE_MOVE = 19;
    public static final int ACTION_TYPE_PHONE_ACROSS = 9;
    public static final int ACTION_TYPE_PHONE_AWAY = 18;
    public static final int ACTION_TYPE_PHONE_CLOSE = 17;
    public static final int ACTION_TYPE_PHONE_PICK_UP = 20;
    public static final int ACTION_TYPE_PICK_UP = 5;
    public static final int ACTION_TYPE_POCKET_MODE = 6;
    public static final int ACTION_TYPE_PROXACROSS_DETECT = 4;
    public static final int ACTION_TYPE_RIASE_UP_WAKE = 25;
    public static final int ACTION_TYPE_SCREEN_DOWN = 21;
    public static final int ACTION_TYPE_SCREEN_DOWN_NO = 23;
    public static final int ACTION_TYPE_SCREEN_DOWN_PICK_UP = 24;
    public static final int ACTION_TYPE_SCREEN_DOWN_YES = 22;
    public static final int ACTION_TYPE_SCREEN_OFF_WAKEUP = 8;
    public static final int ACTION_TYPE_SHAKE = 7;
    public static final int ACTION_TYPE_WAVE_DETECT = 3;
    public static final int GESTURE_TYPE = 80;
    public static final int GESTURE_TYPE_ERROR = 84;
    public static final int GESTURE_TYPE_M = 81;
    public static final int GESTURE_TYPE_S = 82;
    public static final int GESTURE_TYPE_V = 83;
    private static final String HALL_STATE_PATH = "/sys/class/switch/hall/state";
    public static final int MSG_ACTION_CAL_FINISH = 16;
    private static final String TAG = "MotionRecognitionManager";
    private static MotionRecognitionThead mMotionRecognitionThead = null;
    private static int newState = -1;
    static final ArrayList<MotionRecognitionListenerClass> sListenersClassList = new ArrayList();
    private AllConfig mAllConfig = null;
    private DirectCallingService mDirectCallingService = null;
    private GestureService mGestureService = null;
    private final IBinder mICallBack = new Binder();
    private Context mMRMcontex;
    private MessageRemindService mMessageRemindService = null;
    private Handler mMotionRecognitionHandler = null;
    private Looper mMotionRecognitionLooper;
    private Looper mMotionRecognitionMainLooper;
    private MoveService mMoveService = null;
    private boolean mNeedVibrator = true;
    private PhoneAcrossService mPhoneAcrossService = null;
    private PhonePickUpService mPhonePickUpService = null;
    private PhoneScreenDownPickUpService mPhoneScreenDownPickUpService = null;
    private PhoneScreenDownService mPhoneScreenDownService = null;
    private PocketModeService mPocketModeService = null;
    private ProximityAcrossService mProximityAcrossService = null;
    private RaiseUpWakeService mRaiseUpWakeService = null;
    private ScreenOffWakeupService mScreenOffWakeupService = null;
    private ShakeDetectService mShakeDetectService = null;
    private ShakeDetectServiceTwo mShakeDetectServiceTwo = null;
    public VivoCollectData mVivoCollectData = null;
    private VivoFrameworkFactory mVivoFrameworkFactory = null;
    private WaveDetectService mWaveDetectService = null;

    private class MotionRecognitionHandler extends Handler {
        public MotionRecognitionHandler(Looper mLooper) {
            super(mLooper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 16:
                    int action = msg.obj.intValue();
                    Log.d(MotionRecognitionManager.TAG, "handleMessage " + action);
                    if (action == 1 || action == 2 || action == 3 || action == 4 || action == 5 || action == 6 || action == 7 || action == 8 || action == 9 || action == 17 || action == 18 || action == 19 || action == 20 || action == 21 || action == 22 || action == 23 || action == 24 || action == 80 || action == 81 || action == 82 || action == 83 || action == 84 || action == 25) {
                        if (8 == action) {
                            MotionRecognitionManager.this.mVivoCollectData.writeData("1006", "10061", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, null);
                            Log.d(MotionRecognitionManager.TAG, "vivo collect data: SCREEN_OFF_WAKEUP");
                        } else if (25 == action) {
                            MotionRecognitionManager.this.mVivoCollectData.writeData("1008", "10081", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, null);
                            Log.d(MotionRecognitionManager.TAG, "vivo collect data: POCKET_MODE or RAISE_UP_WAKE");
                        } else if (7 == action) {
                            MotionRecognitionManager.this.mVivoCollectData.writeData("1010", "10101", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, null);
                            Log.d(MotionRecognitionManager.TAG, "vivo collect data: SHAKE_PHONE");
                        } else if (4 == action) {
                            MotionRecognitionManager.this.mVivoCollectData.writeData("1007", "10071", System.currentTimeMillis(), System.currentTimeMillis(), 0, 1, null);
                            Log.d(MotionRecognitionManager.TAG, "vivo collect data: PROXACROSS_DETECT");
                        }
                        synchronized (MotionRecognitionManager.sListenersClassList) {
                            for (int i = 0; i < MotionRecognitionManager.sListenersClassList.size(); i++) {
                                MotionRecognitionListenerClass mListenerClass = (MotionRecognitionListenerClass) MotionRecognitionManager.sListenersClassList.get(i);
                                if (mListenerClass.hasActionType(action)) {
                                    Log.d(MotionRecognitionManager.TAG, "hasActionType:" + action);
                                    Context mContext = mListenerClass.mMotionRecognitionListener.onMotionActionTriger(action);
                                    if (mContext != null && action == 1 && MotionRecognitionManager.this.mNeedVibrator) {
                                        ((Vibrator) mContext.getSystemService("vibrator")).vibrate(new long[]{1, 50}, -1);
                                    }
                                    if (mListenerClass.needDelete > 0) {
                                        MotionRecognitionManager.sListenersClassList.remove(mListenerClass);
                                    }
                                }
                            }
                        }
                    } else {
                        return;
                    }
                    break;
            }
        }
    }

    private class MotionRecognitionListenerClass {
        public Context contex;
        public SparseBooleanArray mActionType = new SparseBooleanArray();
        private final ArrayList<Integer> mActionTypeList = new ArrayList();
        private MotionRecognitionListener mMotionRecognitionListener;
        public int needDelete;
        public int needWait;

        public MotionRecognitionListenerClass(MotionRecognitionListener listener, Context cntx, int actionType, int wait) {
            this.mMotionRecognitionListener = listener;
            addActionType(actionType);
            this.contex = cntx;
            this.needWait = wait;
            this.needDelete = 0;
            MotionRecognitionManager.this.mVivoCollectData = new VivoCollectData(this.contex);
        }

        public void addActionType(int actionType) {
            this.mActionType.put(actionType, true);
            this.mActionTypeList.add(new Integer(actionType));
            if (actionType == 1) {
                this.mActionType.put(5, true);
                this.mActionTypeList.add(new Integer(5));
            }
            if (actionType == 9) {
                this.mActionType.put(5, true);
                this.mActionTypeList.add(new Integer(5));
                this.mActionType.put(17, true);
                this.mActionTypeList.add(new Integer(17));
                this.mActionType.put(18, true);
                this.mActionTypeList.add(new Integer(18));
            }
            if (actionType == 21) {
                this.mActionType.put(22, true);
                this.mActionTypeList.add(new Integer(22));
                this.mActionType.put(23, true);
                this.mActionTypeList.add(new Integer(23));
            }
            if (actionType == 80) {
                this.mActionType.put(81, true);
                this.mActionTypeList.add(new Integer(81));
                this.mActionType.put(82, true);
                this.mActionTypeList.add(new Integer(82));
                this.mActionType.put(83, true);
                this.mActionTypeList.add(new Integer(83));
                this.mActionType.put(84, true);
                this.mActionTypeList.add(new Integer(84));
            }
        }

        public int removeActionType(int actionType) {
            this.mActionType.delete(actionType);
            this.mActionTypeList.remove(new Integer(actionType));
            if (actionType == 1) {
                this.mActionType.delete(5);
                this.mActionTypeList.remove(new Integer(5));
            }
            if (actionType == 9) {
                this.mActionType.delete(5);
                this.mActionTypeList.remove(new Integer(5));
                this.mActionType.delete(17);
                this.mActionTypeList.remove(new Integer(17));
                this.mActionType.delete(18);
                this.mActionTypeList.remove(new Integer(18));
            }
            if (actionType == 21) {
                this.mActionType.delete(22);
                this.mActionTypeList.remove(new Integer(22));
                this.mActionType.delete(23);
                this.mActionTypeList.remove(new Integer(23));
            }
            if (actionType == 80) {
                this.mActionType.delete(81);
                this.mActionTypeList.remove(new Integer(81));
                this.mActionType.delete(82);
                this.mActionTypeList.remove(new Integer(82));
                this.mActionType.delete(83);
                this.mActionTypeList.remove(new Integer(83));
                this.mActionType.delete(84);
                this.mActionTypeList.remove(new Integer(84));
            }
            return this.mActionType.size();
        }

        public boolean hasActionType(int actionType) {
            return this.mActionType.get(actionType);
        }

        public MotionRecognitionListener getListener() {
            return this.mMotionRecognitionListener;
        }

        public ArrayList<Integer> getActionTypes() {
            return this.mActionTypeList;
        }
    }

    private class MotionRecognitionThead extends Thread {
        Looper mLooper;

        /* synthetic */ MotionRecognitionThead(MotionRecognitionManager this$0, MotionRecognitionThead -this1) {
            this();
        }

        private MotionRecognitionThead() {
            this.mLooper = null;
        }

        public void run() {
            Looper.prepare();
            synchronized (this) {
                this.mLooper = Looper.myLooper();
                notifyAll();
            }
            Looper.loop();
        }

        public Looper getLooper() {
            if (!isAlive()) {
                return null;
            }
            synchronized (this) {
                while (isAlive() && this.mLooper == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return this.mLooper;
        }
    }

    public MotionRecognitionManager(Looper MainLooper) {
        synchronized (this) {
            this.mAllConfig = new AllConfig();
            this.mMotionRecognitionMainLooper = MainLooper;
            this.mDirectCallingService = DirectCallingService.getInstance();
            this.mMessageRemindService = MessageRemindService.getInstance();
            this.mWaveDetectService = WaveDetectService.getInstance();
            this.mProximityAcrossService = ProximityAcrossService.getInstance();
            this.mPocketModeService = PocketModeService.getInstance();
            if (AllConfig.mIsShakeTwo) {
                this.mShakeDetectServiceTwo = ShakeDetectServiceTwo.getInstance();
            } else {
                this.mShakeDetectService = ShakeDetectService.getInstance();
            }
            this.mScreenOffWakeupService = ScreenOffWakeupService.getInstance();
            this.mPhoneAcrossService = PhoneAcrossService.getInstance();
            this.mMoveService = MoveService.getInstance();
            this.mPhonePickUpService = PhonePickUpService.getInstance();
            this.mPhoneScreenDownService = PhoneScreenDownService.getInstance();
            this.mPhoneScreenDownPickUpService = PhoneScreenDownPickUpService.getInstance();
            this.mGestureService = GestureService.getInstance();
            this.mRaiseUpWakeService = RaiseUpWakeService.getInstance();
            if (this.mMotionRecognitionLooper == null) {
                mMotionRecognitionThead = new MotionRecognitionThead(this, null);
                mMotionRecognitionThead.start();
            }
            this.mMotionRecognitionLooper = mMotionRecognitionThead.getLooper();
            this.mMotionRecognitionHandler = new MotionRecognitionHandler(this.mMotionRecognitionLooper);
        }
    }

    private boolean enableMotionRecognitionLocked(Context context, int actionType) {
        for (MotionRecognitionListenerClass i : sListenersClassList) {
            if (i.hasActionType(actionType)) {
                boolean isSuperPower = SystemProperties.getBoolean("sys.super_power_save", false);
                if (!isSuperPower || (actionType != 1 && actionType != 4 && actionType != 25)) {
                    switch (actionType) {
                        case 1:
                            this.mDirectCallingService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 2:
                            this.mMessageRemindService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 3:
                            this.mWaveDetectService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 4:
                            this.mProximityAcrossService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 6:
                            this.mPocketModeService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 7:
                            if (!AllConfig.mIsShakeTwo) {
                                this.mShakeDetectService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                                break;
                            }
                            this.mShakeDetectServiceTwo.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 8:
                            this.mScreenOffWakeupService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 9:
                            this.mPhoneAcrossService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 19:
                            this.mMoveService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 20:
                            this.mPhonePickUpService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 21:
                            this.mPhoneScreenDownService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 24:
                            this.mPhoneScreenDownPickUpService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case 25:
                            this.mRaiseUpWakeService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        case GESTURE_TYPE /*80*/:
                            this.mGestureService.startMotionRecognitionService(context, this.mMotionRecognitionHandler);
                            break;
                        default:
                            break;
                    }
                }
                Log.d(TAG, "isSuperPower:" + isSuperPower + ",actionType:" + actionType);
                return true;
            }
        }
        return true;
    }

    private boolean disableMotionRecognitionLocked(Context context, int actionType) {
        for (MotionRecognitionListenerClass i : sListenersClassList) {
            if (i.hasActionType(actionType)) {
                Log.d(TAG, "Still has action: " + actionType + "not disable");
                return true;
            }
        }
        switch (actionType) {
            case 1:
                this.mDirectCallingService.stopMotionRecognitionService();
                break;
            case 2:
                this.mMessageRemindService.stopMotionRecognitionService();
                break;
            case 3:
                this.mWaveDetectService.stopMotionRecognitionService();
                break;
            case 4:
                this.mProximityAcrossService.stopMotionRecognitionService();
                break;
            case 6:
                this.mPocketModeService.stopMotionRecognitionService();
                break;
            case 7:
                if (!AllConfig.mIsShakeTwo) {
                    this.mShakeDetectService.stopMotionRecognitionService();
                    break;
                }
                this.mShakeDetectServiceTwo.stopMotionRecognitionService();
                break;
            case 8:
                this.mScreenOffWakeupService.stopMotionRecognitionService();
                break;
            case 9:
                this.mPhoneAcrossService.stopMotionRecognitionService();
                break;
            case 19:
                this.mMoveService.stopMotionRecognitionService();
                break;
            case 20:
                this.mPhonePickUpService.stopMotionRecognitionService();
                break;
            case 21:
                this.mPhoneScreenDownService.stopMotionRecognitionService();
                break;
            case 24:
                this.mPhoneScreenDownPickUpService.stopMotionRecognitionService();
                break;
            case 25:
                this.mRaiseUpWakeService.stopMotionRecognitionService();
                break;
            case GESTURE_TYPE /*80*/:
                this.mGestureService.stopMotionRecognitionService();
                break;
        }
        return true;
    }

    private boolean forceDisableMotionRecognition(Context context, int actionType) {
        switch (actionType) {
            case 1:
                this.mDirectCallingService.stopMotionRecognitionService();
                break;
            case 2:
                this.mMessageRemindService.stopMotionRecognitionService();
                break;
            case 3:
                this.mWaveDetectService.stopMotionRecognitionService();
                break;
            case 4:
                this.mProximityAcrossService.stopMotionRecognitionService();
                break;
            case 6:
                this.mPocketModeService.stopMotionRecognitionService();
                break;
            case 7:
                if (!AllConfig.mIsShakeTwo) {
                    this.mShakeDetectService.stopMotionRecognitionService();
                    break;
                }
                this.mShakeDetectServiceTwo.stopMotionRecognitionService();
                break;
            case 8:
                this.mScreenOffWakeupService.stopMotionRecognitionService();
                break;
            case 9:
                this.mPhoneAcrossService.stopMotionRecognitionService();
                break;
            case 19:
                this.mMoveService.stopMotionRecognitionService();
                break;
            case 20:
                this.mPhonePickUpService.stopMotionRecognitionService();
                break;
            case 21:
                this.mPhoneScreenDownService.stopMotionRecognitionService();
                break;
            case 24:
                this.mPhoneScreenDownPickUpService.stopMotionRecognitionService();
                break;
            case 25:
                this.mRaiseUpWakeService.stopMotionRecognitionService();
                break;
            case GESTURE_TYPE /*80*/:
                this.mGestureService.stopMotionRecognitionService();
                break;
        }
        return true;
    }

    private static IMotionManager checkService() {
        return Stub.asInterface(ServiceManager.checkService("motion_manager"));
    }

    public boolean isTopListener(MotionRecognitionListener listener) {
        int mDialerDirectCallRegistered = 0;
        List clients = null;
        IMotionManager service = checkService();
        if (service != null) {
            try {
                clients = service.getClients();
            } catch (Exception e) {
                return false;
            }
        }
        try {
            mDialerDirectCallRegistered = System.getInt(this.mMRMcontex.getContentResolver(), "bbk_dialerdirectcallregister_setting", 0);
            Log.d(TAG, "dialer direct call has registered ornot: " + mDialerDirectCallRegistered);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (clients == null) {
            return false;
        }
        if (clients.size() <= 0) {
            return false;
        }
        if (!listener.toString().equals((String) clients.get(0))) {
            return false;
        }
        if (mDialerDirectCallRegistered != 1) {
            return true;
        }
        Log.d(TAG, "dialer direct call has registered");
        return false;
    }

    public boolean registerMotionRecognitionListener(Context context, int actionType, MotionRecognitionListener listener, boolean needVibrator) {
        this.mNeedVibrator = needVibrator;
        return registerMotionRecognitionListenerInternal(context, actionType, 0, listener);
    }

    public boolean registerMotionRecognitionListener(Context context, int actionType, int wait, MotionRecognitionListener listener) {
        return registerMotionRecognitionListenerInternal(context, actionType, wait, listener);
    }

    public boolean registerMotionRecognitionListener(Context context, int actionType, MotionRecognitionListener listener) {
        return registerMotionRecognitionListenerInternal(context, actionType, 0, listener);
    }

    public boolean registerMotionRecognitionListenerInternal(Context context, int actionType, int wait, MotionRecognitionListener listener) {
        synchronized (sListenersClassList) {
            try {
                this.mVivoFrameworkFactory = VivoFrameworkFactory.getFrameworkFactoryImpl();
                if (this.mVivoFrameworkFactory != null) {
                    this.mVivoFrameworkFactory.getMotionManager().register(listener.toString(), context.getPackageName(), String.valueOf(actionType), this.mICallBack);
                }
            } catch (Exception e) {
            }
            MotionRecognitionListenerClass m = null;
            for (MotionRecognitionListenerClass i : sListenersClassList) {
                if (i.getListener() == listener) {
                    m = i;
                    break;
                }
            }
            if (m == null) {
                sListenersClassList.add(new MotionRecognitionListenerClass(listener, context, actionType, wait));
                if (!sListenersClassList.isEmpty()) {
                    enableMotionRecognitionLocked(context, actionType);
                }
            } else if (!m.hasActionType(actionType) || actionType == 80) {
                m.addActionType(actionType);
                enableMotionRecognitionLocked(context, actionType);
            }
        }
        this.mMRMcontex = context;
        if (actionType == 1 && listener != null && (listener.toString().contains("dialer") || listener.toString().contains("contacts"))) {
            System.putInt(context.getContentResolver(), "bbk_dialerdirectcallregister_setting", 1);
            Log.d(TAG, "register dialer direct call");
        }
        Log.d(TAG, "registerMotionRecognitionListener " + actionType);
        return true;
    }

    public boolean unregisterMotionRecognitionListener(Context context, MotionRecognitionListener listener) {
        if (listener == null) {
            return false;
        }
        synchronized (sListenersClassList) {
            this.mVivoFrameworkFactory = VivoFrameworkFactory.getFrameworkFactoryImpl();
            try {
                if (this.mVivoFrameworkFactory != null) {
                    this.mVivoFrameworkFactory.getMotionManager().unregister(listener.toString());
                }
            } catch (Exception e) {
            }
            int size = sListenersClassList.size();
            int i = 0;
            while (i < size) {
                MotionRecognitionListenerClass m = (MotionRecognitionListenerClass) sListenersClassList.get(i);
                if (m.getListener() == listener) {
                    if (m.needWait == 0) {
                        sListenersClassList.remove(m);
                        for (Integer actionType : m.getActionTypes()) {
                            disableMotionRecognitionLocked(context, actionType.intValue());
                        }
                    } else {
                        for (Integer actionType2 : m.getActionTypes()) {
                            forceDisableMotionRecognition(context, actionType2.intValue());
                        }
                        m.needDelete = 1;
                    }
                } else {
                    i++;
                }
            }
        }
        Log.d(TAG, "unregisterMotionRecognitionListener");
        return true;
    }

    public boolean unregisterMotionRecognitionListener(Context context, int actionType, MotionRecognitionListener listener) {
        if (listener == null) {
            return false;
        }
        synchronized (sListenersClassList) {
            IMotionManager service = checkService();
            if (service != null) {
                try {
                    service.unregister(listener.toString());
                } catch (Exception e) {
                }
            }
            int size = sListenersClassList.size();
            int i = 0;
            while (i < size) {
                MotionRecognitionListenerClass m = (MotionRecognitionListenerClass) sListenersClassList.get(i);
                if (m.getListener() == listener) {
                    for (Integer mactionType : m.getActionTypes()) {
                        if (mactionType.intValue() == actionType) {
                            if (m.removeActionType(actionType) == 0) {
                                sListenersClassList.remove(m);
                                Log.d(TAG, "unregisterMotionRecognitionListener type last");
                            }
                            disableMotionRecognitionLocked(context, mactionType.intValue());
                        }
                    }
                } else {
                    i++;
                }
            }
        }
        if (actionType == 1 && (listener.toString().contains("dialer") || listener.toString().contains("contacts"))) {
            System.putInt(context.getContentResolver(), "bbk_dialerdirectcallregister_setting", 0);
            Log.d(TAG, "unregister dialer direct call");
        }
        Log.d(TAG, "unregisterMotionRecognitionListener type: " + actionType);
        return true;
    }

    public static void loadGestureLib() {
        GestureService.loadGestureLib();
    }
}
