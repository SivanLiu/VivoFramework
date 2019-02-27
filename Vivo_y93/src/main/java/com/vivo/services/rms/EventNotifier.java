package com.vivo.services.rms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import com.vivo.services.rms.appmng.AppManager;
import com.vivo.services.rms.appmng.namelist.WidgetList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.rms.sdk.IEventCallback;
import com.vivo.services.rms.sdk.IEventCallbackNative;
import com.vivo.services.rms.sdk.RMNative;
import com.vivo.services.rms.sdk.args.Args;
import com.vivo.services.rms.sdk.args.ArgsFactory;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class EventNotifier implements ServiceConnection {
    private static final int MAX_ACC_EVENT_COUNT = 512;
    private static final int MAX_TRY_REBIND_COUNT = 40;
    private static final int MSG_BINDSERVICE = 1;
    private static final int MSG_EVENT = 0;
    private static final String PACKAGE_NAME = "com.vivo.abe";
    private static final int PROCESS_EVENT = 0;
    public static final String PROC_NAME = "com.vivo.rms";
    private static final String SERVICE_NAME = "com.vivo.rms.dispatcher.EventReceiverService";
    private static final int SYSTEM_EVENT = 1;
    public static final String TAG = "rms";
    private AtomicInteger mAccEventCount = new AtomicInteger(0);
    private int mConnectedTimes;
    private Context mContext;
    private String mDeathReason;
    private Handler mEventHanler;
    private IEventCallback mNotifier;
    private volatile int mPid = -1;
    private boolean mSuicide;
    private int mTid;
    private int mTryRebindTimes;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EventNotifier.this.handleEvent(msg.arg1, msg.arg2, (Args) msg.obj);
                    return;
                case 1:
                    EventNotifier.this.realBindService();
                    EventNotifier.this.bindServiceIfNeeded();
                    return;
                default:
                    return;
            }
        }
    }

    public EventNotifier(Context context, Looper looper) {
        this.mContext = context;
        this.mEventHanler = new EventHandler(looper);
    }

    public void systemReady() {
        bindServiceIfNeeded();
    }

    public void onServiceConnected(ComponentName name, IBinder listener) {
        if (!isServiceConnected()) {
            IEventCallback l = IEventCallbackNative.asInterface(listener);
            try {
                synchronized (AppManager.getInstance()) {
                    Bundle data = new Bundle();
                    AppManager.getInstance().doInitLocked(data);
                    WidgetList.fillBundle(data);
                    fillSystemEvent(data);
                    fillDeathReason(data);
                    l.doInit(data);
                    this.mEventHanler.removeMessages(0);
                    this.mEventHanler.removeMessages(1);
                    this.mNotifier = l;
                    this.mPid = l.myPid();
                    this.mConnectedTimes++;
                    this.mSuicide = false;
                }
                this.mTryRebindTimes = 0;
                Log.i("rms", String.format("EventReceiverService is connected pid=%d, times=%d", new Object[]{Integer.valueOf(this.mPid), Integer.valueOf(this.mConnectedTimes)}));
            } catch (Exception e) {
                Log.e("rms", "onServiceConnected exeption : " + e.getMessage());
            }
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        if (isServiceConnected()) {
            this.mPid = -1;
            this.mNotifier = null;
            this.mAccEventCount.getAndSet(0);
            this.mEventHanler.removeMessages(0);
            this.mEventHanler.removeMessages(1);
            Config.setRmsEnable(false);
            bindServiceIfNeeded();
            Log.d("rms", "EventReceiverService is disconnected");
        }
    }

    private void fillSystemEvent(Bundle data) {
        if (RMInjectorImpl.self() != null && RMInjectorImpl.self().isMonkey()) {
            data.putInt(EventDispatcher.NAME_MONKEY_STATE, 1);
        }
    }

    private void fillDeathReason(Bundle data) {
        boolean z = false;
        String str = "fromReboot";
        if (this.mConnectedTimes == 0) {
            z = true;
        }
        data.putBoolean(str, z);
        data.putString("deathReason", this.mSuicide ? "suicide" : this.mDeathReason);
    }

    public void setDeathReason(String reason) {
        this.mDeathReason = reason;
    }

    public void bindServiceIfNeeded() {
        if (!isServiceConnected() && !this.mEventHanler.hasMessages(1)) {
            int i = this.mTryRebindTimes + 1;
            this.mTryRebindTimes = i;
            if (i < MAX_TRY_REBIND_COUNT) {
                this.mEventHanler.sendEmptyMessageDelayed(1, 3000);
            }
        }
    }

    private void realBindService() {
        synchronized (this) {
            if (!isServiceConnected()) {
                Intent intent = new Intent();
                intent.putExtra("version", RMNative.VERSION);
                intent.putExtra("caller", this.mContext.getPackageName());
                intent.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
                try {
                    this.mContext.bindServiceAsUser(intent, this, 1, Process.myUserHandle());
                } catch (Exception e) {
                    Log.e("rms", e.getMessage());
                }
            }
        }
        return;
    }

    public boolean isServiceConnected() {
        return this.mPid > 0;
    }

    public void postProcessEvent(int event, Args args) {
        if (isServiceConnected()) {
            this.mAccEventCount.addAndGet(1);
            this.mEventHanler.obtainMessage(0, 0, event, args).sendToTarget();
        }
    }

    public void postSystemEvent(int event, Args args) {
        if (isServiceConnected()) {
            this.mAccEventCount.addAndGet(1);
            this.mEventHanler.obtainMessage(0, 1, event, args).sendToTarget();
        }
    }

    public void postDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        try {
            if (this.mNotifier != null) {
                this.mNotifier.dumpData(fd, args);
            }
        } catch (Exception e) {
            pw.println("dumpData exeption : " + e.getMessage());
        }
    }

    private void handleEvent(int type, int event, Args args) {
        int accNum;
        if (args != null && this.mNotifier != null) {
            if (this.mTid == 0) {
                this.mTid = Process.myTid();
                Process.setThreadPriority(this.mTid, -8);
            }
            if (type == 0) {
                try {
                    this.mNotifier.onProcessEvent(event, args);
                } catch (Exception e) {
                    Log.e("rms", "handle event exception : " + e.getMessage());
                    accNum = this.mAccEventCount.addAndGet(-1);
                    if (accNum > 512) {
                        this.mSuicide = true;
                        Process.killProcess(this.mPid);
                        Log.e("rms", String.format("Maybe hang! accumulative %d kill %d.", new Object[]{Integer.valueOf(accNum), Integer.valueOf(this.mPid)}));
                    } else if (accNum > ProcessStates.HASNOTIFICATION) {
                        Log.e("rms", "System is slowly, accNum=" + accNum);
                    }
                } catch (Throwable th) {
                    accNum = this.mAccEventCount.addAndGet(-1);
                    if (accNum > 512) {
                        this.mSuicide = true;
                        Process.killProcess(this.mPid);
                        Log.e("rms", String.format("Maybe hang! accumulative %d kill %d.", new Object[]{Integer.valueOf(accNum), Integer.valueOf(this.mPid)}));
                    } else if (accNum > ProcessStates.HASNOTIFICATION) {
                        Log.e("rms", "System is slowly, accNum=" + accNum);
                    }
                }
            } else if (type == 1) {
                this.mNotifier.onSystemEvent(event, args);
            }
            ArgsFactory.recycle(args);
            accNum = this.mAccEventCount.addAndGet(-1);
            if (accNum > 512) {
                this.mSuicide = true;
                Process.killProcess(this.mPid);
                Log.e("rms", String.format("Maybe hang! accumulative %d kill %d.", new Object[]{Integer.valueOf(accNum), Integer.valueOf(this.mPid)}));
            } else if (accNum > ProcessStates.HASNOTIFICATION) {
                Log.e("rms", "System is slowly, accNum=" + accNum);
            }
        }
    }
}
