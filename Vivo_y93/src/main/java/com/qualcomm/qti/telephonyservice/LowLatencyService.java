package com.qualcomm.qti.telephonyservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.qti.hardware.data.latency.V1_0.IClientToken;
import vendor.qti.hardware.data.latency.V1_0.IClientToken.Stub;
import vendor.qti.hardware.data.latency.V1_0.ILinkLatency;
import vendor.qti.hardware.data.latency.V1_0.ILinkLatency.createLatencyServiceCallback;
import vendor.qti.hardware.data.latency.V1_0.ILinkLatencyService;
import vendor.qti.hardware.data.latency.V1_0.ILinkLatencyService.setLevelCallback;

public class LowLatencyService extends Service {
    public static final long LEVEL_SET_MINIMUM_TIME_INTERVAL = 10000;
    public static final String SYSTEMPROPERTIES_LATENCY_DL_LEVEL = "persist.sys.vivo.qtilowlatencyleveldl";
    public static final String SYSTEMPROPERTIES_LATENCY_PRIO = "persist.sys.vivo.qtilowlatencyprio";
    public static final String SYSTEMPROPERTIES_LATENCY_UL_LEVEL = "persist.sys.vivo.qtilowlatencylevelul";
    private static final String TAG = "LowLatencyService";
    public static Context context;
    public static long lastSetLevelTime = 0;
    public static ILinkLatency mHidlService = null;
    public static ILinkLatencyService mILinkLatencyService = null;
    private final IClientToken clientToken = new Stub() {
    };
    private boolean isHIDLDead = true;
    private boolean isServiceStarted = false;
    private final long mDeathBinderCookie = 1011011;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void serviceDied(long cookie) {
            Log.d(LowLatencyService.TAG, "HIDL SErvice Died");
            if (cookie == 1011011) {
                try {
                    LowLatencyService.mHidlService.unlinkToDeath(LowLatencyService.this.mDeathRecipient);
                } catch (RemoteException e) {
                    Log.d(LowLatencyService.TAG, "RemoteException : unable to unlink DeathRecipient");
                }
                LowLatencyService.this.isHIDLDead = true;
                LowLatencyService.this.isServiceStarted = false;
                LowLatencyService.this.mIServiceManager = null;
                try {
                    if (LowLatencyService.this.mIServiceManager != null) {
                        Log.d(LowLatencyService.TAG, "mDeathRecipient Already have a Service Manager running");
                        return;
                    }
                    LowLatencyService.this.mIServiceManager = IServiceManager.getService();
                    if (LowLatencyService.this.mIServiceManager == null) {
                        Log.e(LowLatencyService.TAG, "mDeathRecipient Failed to obtain Service Manager");
                    } else if (!LowLatencyService.this.mIServiceManager.registerForNotifications(ILinkLatency.kInterfaceName, "", LowLatencyService.this.mServiceNotificationCallback)) {
                        Log.e(LowLatencyService.TAG, "mDeathRecipient Failed to register for notifications to ILinkLatencyService");
                    }
                } catch (RemoteException e2) {
                    Log.d(LowLatencyService.TAG, "mDeathRecipient serviceDied Caught Exception:", e2);
                }
            }
        }
    };
    private HandlerThread mEventThread;
    private Handler mHandler;
    private IServiceManager mIServiceManager = null;
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.d(LowLatencyService.TAG, "received null intent");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                Log.d(LowLatencyService.TAG, "received null intent action");
                return;
            }
            if (action.equals("com.qualcomm.qti.telephonyservice.intent.action.CHANGE_LEVEL")) {
                Log.d(LowLatencyService.TAG, "Low Latency Level Changed Event Received");
                long rat = intent.getLongExtra("Rat_type", 0);
                long level_ul = intent.getLongExtra("Level_UL", 1);
                long level_dl = intent.getLongExtra("Level_DL", 1);
                Log.d(LowLatencyService.TAG, "setLevel: rat " + rat + " level_ul " + level_ul + " level_dl " + level_dl);
                try {
                    if (LowLatencyService.isOutOfMinimumTimeOfSetlevel()) {
                        LowLatencyService.mILinkLatencyService.setLevel(rat, level_ul, level_dl, LowLatencyService.this.mLLCb);
                        LowLatencyService.lastSetLevelTime = System.currentTimeMillis();
                        Log.d(LowLatencyService.TAG, "Low Latency level Changed");
                    }
                } catch (Exception e) {
                    Log.d(LowLatencyService.TAG, "setLevel Caught Exception:", e);
                }
            } else if (action.equals("com.qualcomm.qti.telephonyservice.intent.action.CHANGE_PRIO")) {
                Log.d(LowLatencyService.TAG, "Low Latency prioritize Changed Event Received");
                Boolean prio_default = Boolean.valueOf(intent.getBooleanExtra("prio_default", true));
                Log.d(LowLatencyService.TAG, "prio_default: " + prio_default);
                try {
                    if (LowLatencyService.isOutOfMinimumTimeOfSetlevel()) {
                        long statusCode = LowLatencyService.mILinkLatencyService.prioritizeDefaultDataSubscription(prio_default.booleanValue());
                        Log.d(LowLatencyService.TAG, "Low Latency prioritize Changed: " + statusCode);
                        if (statusCode == 0) {
                            SystemProperties.set(LowLatencyService.SYSTEMPROPERTIES_LATENCY_PRIO, prio_default.toString());
                        }
                        LowLatencyService.lastSetLevelTime = System.currentTimeMillis();
                    }
                } catch (Exception e2) {
                    Log.d(LowLatencyService.TAG, "prioritizeDefaultDataSubscription Caught Exception:", e2);
                }
            }
        }
    };
    private final LCallback mLCb = new LCallback();
    private final LLCallback mLLCb = new LLCallback();
    private final Object mLock = new Object();
    private final IServiceNotification mServiceNotificationCallback = new IServiceNotification.Stub() {
        public void onRegistration(String fqName, String name, boolean preexisting) {
            synchronized (LowLatencyService.this.mLock) {
                Log.d(LowLatencyService.TAG, "IServiceNotification.onRegistration");
                LowLatencyService.this.startService();
            }
        }
    };

    public class LCallback implements createLatencyServiceCallback {
        public long res = 0;
        public ILinkLatencyService service;

        public void onValues(long tmpResult, ILinkLatencyService tmpservice) {
            this.res = tmpResult;
            this.service = tmpservice;
            if (this.service != null) {
                LowLatencyService.mILinkLatencyService = this.service;
                LowLatencyService.this.isServiceStarted = true;
                Log.d(LowLatencyService.TAG, "Received HIDL-LCallback");
            }
        }
    }

    public class LLCallback implements setLevelCallback {
        public long level_dl;
        public long level_ul;
        public long res = 0;

        public void onValues(long tmpResult, long tmplevel_ul, long tmplevel_dl) {
            this.res = tmpResult;
            this.level_ul = tmplevel_ul;
            this.level_dl = tmplevel_dl;
            SystemProperties.set(LowLatencyService.SYSTEMPROPERTIES_LATENCY_UL_LEVEL, new Long(this.level_ul).toString());
            SystemProperties.set(LowLatencyService.SYSTEMPROPERTIES_LATENCY_DL_LEVEL, new Long(this.level_dl).toString());
            Log.d(LowLatencyService.TAG, "Received HIDL-LLCallback res: " + this.res);
            Log.d(LowLatencyService.TAG, "Received HIDL-LLCallback ul: " + this.level_ul + " dl: " + this.level_dl);
        }
    }

    private synchronized void startService() {
        if (!this.isServiceStarted) {
            Log.d(TAG, "Trying to get HIDL-ILinkLatency");
            try {
                mHidlService = ILinkLatency.getService();
            } catch (RemoteException e) {
                Log.d(TAG, "startService Caught Exception:", e);
            } catch (NoSuchElementException nSE) {
                Log.d(TAG, "startService Caught Exception: Service not Registered yet", nSE);
            } catch (Exception e2) {
                Log.d(TAG, "startService Caught Exception:", e2);
            }
            if (mHidlService != null) {
                this.isServiceStarted = true;
                Log.d(TAG, "Received HIDL-ILinkLatency");
                try {
                    mHidlService.createLatencyService(this.clientToken, this.mLCb);
                    this.isHIDLDead = mHidlService.linkToDeath(this.mDeathRecipient, 1011011) ^ 1;
                    Log.d(TAG, "isHIDLDead " + this.isHIDLDead);
                } catch (Exception e22) {
                    Log.d(TAG, "startService Caught Exception:", e22);
                }
            }
        }
        return;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate method");
        context = this;
        this.mEventThread = new HandlerThread("MainEventThread");
        this.mEventThread.start();
        this.mHandler = new Handler(this.mEventThread.getLooper()) {
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.qualcomm.qti.telephonyservice.intent.action.CHANGE_LEVEL");
        filter.addAction("com.qualcomm.qti.telephonyservice.intent.action.CHANGE_PRIO");
        context.registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
    }

    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart: Attempt to start Service");
        super.onStart(intent, startId);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand- Call");
        new Thread() {
            public void run() {
                try {
                    if (LowLatencyService.this.isServiceStarted) {
                        Log.d(LowLatencyService.TAG, "Already HIDL-ILinkLatencyService is Started ");
                    } else if (LowLatencyService.this.mIServiceManager != null) {
                        Log.d(LowLatencyService.TAG, "Already have a Service Manager running");
                    } else {
                        LowLatencyService.this.mIServiceManager = IServiceManager.getService();
                        if (LowLatencyService.this.mIServiceManager == null) {
                            Log.e(LowLatencyService.TAG, "Failed to obtain Service Manager");
                            return;
                        }
                        if (!LowLatencyService.this.mIServiceManager.registerForNotifications(ILinkLatency.kInterfaceName, "", LowLatencyService.this.mServiceNotificationCallback)) {
                            Log.e(LowLatencyService.TAG, "Failed to register for notifications to ILinkLatencyService");
                        }
                        Log.e(LowLatencyService.TAG, "register for notifications to ILinkLatencyService");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return 1;
    }

    public void onDestroy() {
        Log.d(TAG, "LowLatencyService Destroyed Successfully...");
        super.onDestroy();
        if (mHidlService != null) {
            try {
                mHidlService.unlinkToDeath(this.mDeathRecipient);
            } catch (RemoteException e) {
                Log.d(TAG, "onDestroy Caught Exception:", e);
            }
            mHidlService = null;
        }
        context.unregisterReceiver(this.mIntentReceiver);
        this.mEventThread.quit();
    }

    public static boolean isOutOfMinimumTimeOfSetlevel() {
        if (System.currentTimeMillis() - lastSetLevelTime > LEVEL_SET_MINIMUM_TIME_INTERVAL) {
            return true;
        }
        return false;
    }
}
