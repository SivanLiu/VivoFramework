package com.vivo.services.epm.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.vivo.core.interfaces.messagecore.IDismissCallback;
import com.vivo.core.interfaces.messagecore.IDisplayCallback;
import com.vivo.core.interfaces.messagecore.IMessageDisplayer;
import com.vivo.core.interfaces.messagecore.IMessageDisplayer.Stub;

public class MessageCenterHelper {
    public static final int MSG_BIND_SERVICE = 0;
    public static final int REBIND_SERVICE_TIME_INTERVAL = 10000;
    private static final String TAG = "EPM";
    private static MessageCenterHelper sInstance;
    private Context mContext;
    private Handler mHandler = new MainHandler(this, null);
    private IMessageDisplayer mMessageDisplyer;
    private MCDisplayConnection mServiceConnection = new MCDisplayConnection(this, null);

    private final class BootReceiver extends BroadcastReceiver {
        /* synthetic */ BootReceiver(MessageCenterHelper this$0, BootReceiver -this1) {
            this();
        }

        private BootReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Log.d(MessageCenterHelper.TAG, "receive bootcomplected broadcast, init MessageCenterHelper");
                MessageCenterHelper.this.bindMessageCenterService();
            }
        }
    }

    private class MCDisplayConnection implements ServiceConnection {
        /* synthetic */ MCDisplayConnection(MessageCenterHelper this$0, MCDisplayConnection -this1) {
            this();
        }

        private MCDisplayConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(MessageCenterHelper.TAG, "onServiceConnected");
            MessageCenterHelper.this.mMessageDisplyer = Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(MessageCenterHelper.TAG, "onServiceDisconnected " + name);
            MessageCenterHelper.this.mMessageDisplyer = null;
            if (MessageCenterHelper.this.mHandler != null) {
                MessageCenterHelper.this.mHandler.sendEmptyMessageDelayed(0, 10000);
            }
        }
    }

    private class MainHandler extends Handler {
        /* synthetic */ MainHandler(MessageCenterHelper this$0, MainHandler -this1) {
            this();
        }

        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    MessageCenterHelper.this.bindMessageCenterService();
                    return;
                default:
                    return;
            }
        }
    }

    private MessageCenterHelper(Context context) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(new BootReceiver(this, null), filter);
    }

    public static synchronized void initMessageCenterHelper(Context context) {
        synchronized (MessageCenterHelper.class) {
            if (sInstance == null) {
                sInstance = new MessageCenterHelper(context);
            }
        }
    }

    public static synchronized MessageCenterHelper getInstance() {
        MessageCenterHelper messageCenterHelper;
        synchronized (MessageCenterHelper.class) {
            messageCenterHelper = sInstance;
        }
        return messageCenterHelper;
    }

    private void bindMessageCenterService() {
        Intent intent = new Intent();
        intent.setAction("com.vivo.abe.action.MESSAGE_DISPLAY_SERVICE");
        intent.setPackage("com.vivo.abe");
        this.mContext.bindService(intent, this.mServiceConnection, 1);
    }

    public void send(int displayId, NotificationMessage message, IDisplayCallback.Stub showCallback) {
        Log.d(TAG, "send displayId=" + displayId);
        if (this.mMessageDisplyer != null) {
            try {
                this.mMessageDisplyer.notify(displayId, message.pack(), showCallback);
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "mMessageDisplyer is null, ignore the message");
    }

    public void cancel(int displayId, int messageId, IDismissCallback.Stub dismissCallback) {
        Log.d(TAG, "cancel displayId=" + displayId + " messageId=" + messageId);
        if (this.mMessageDisplyer != null) {
            try {
                this.mMessageDisplyer.cancel(displayId, messageId, null, dismissCallback);
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "mMessageDisplyer is null, ignore the message");
    }
}
