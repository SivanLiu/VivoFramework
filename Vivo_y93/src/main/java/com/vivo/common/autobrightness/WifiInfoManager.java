package com.vivo.common.autobrightness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;

public class WifiInfoManager {
    private static final long HIGHEST_UPDATE_RATE = 3600000;
    private static final long LISTENER_DELAY_TIME = 60000;
    private static final int MSG_INIT = 1;
    private static final int MSG_REQUEST_WIFI_INFO = 2;
    private static final int RETRY_TIME = 2;
    private static final String TAG = TextTool.makeTag("WifiInfoManager");
    private static WifiInfoManager sInstance;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private BackgroundHandler mHandler;
    private long mLastUpdateTime;
    private String mLocation = "unknown";
    private Looper mLooper;
    private int mRetryTime;
    private long mUpdateRate = HIGHEST_UPDATE_RATE;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver = new WifiReceiver(this, null);

    private class BackgroundHandler extends Handler {
        /* synthetic */ BackgroundHandler(WifiInfoManager this$0, Looper looper, BackgroundHandler -this2) {
            this(looper);
        }

        private BackgroundHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WifiInfoManager.this.initialize();
                    return;
                case 2:
                    WifiInfoManager.this.onWifiInfoChanged();
                    return;
                default:
                    return;
            }
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        /* synthetic */ WifiReceiver(WifiInfoManager this$0, WifiReceiver -this1) {
            this();
        }

        private WifiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                WifiInfoManager.this.log("WifiReceiver action = " + action);
                NetworkInfo networkInfo;
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo == null || networkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        WifiInfoManager.this.log("Wifi is not connect");
                    } else {
                        WifiInfoManager.this.log("Wifi is connected");
                    }
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    networkInfo = WifiInfoManager.this.mConnectivityManager.getActiveNetworkInfo();
                    if (networkInfo == null || networkInfo.getType() != 1) {
                        WifiInfoManager.this.log("WifiReceiver Don't have Wifi Connection");
                        WifiInfoManager.this.mHandler.removeMessages(2);
                    } else {
                        WifiInfoManager.this.log("WifiReceiver Have Wifi Connection");
                        WifiInfoManager.this.mHandler.removeMessages(2);
                        WifiInfoManager.this.mHandler.sendEmptyMessage(2);
                    }
                } else {
                    Slog.e(WifiInfoManager.TAG, "unkown action:" + action);
                }
            }
        }
    }

    private WifiInfoManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        log("Call WifiInfoManager()");
        HandlerThread ht = new HandlerThread("AutoWIThread");
        ht.start();
        this.mLooper = ht.getLooper();
        this.mHandler = new BackgroundHandler(this, this.mLooper, null);
        this.mHandler.sendEmptyMessage(1);
    }

    public static WifiInfoManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (WifiInfoManager.class) {
                if (sInstance == null) {
                    sInstance = new WifiInfoManager(context);
                }
            }
        }
        return sInstance;
    }

    private void onWifiInfoChanged() {
        log("onWifiInfoChanged has data back.");
        if (this.mWifiManager != null) {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            String split = "&&";
            StringBuilder sb = new StringBuilder();
            String ssid = info.getSSID();
            String macAddr = info.getMacAddress();
            String ipAddr = intToStrIp(info.getIpAddress());
            int networkID = info.getNetworkId();
            int speed = info.getLinkSpeed();
            if (TextTool.isNull(ssid)) {
                sb.append("-1").append("&&");
            } else {
                sb.append(ssid).append("&&");
            }
            if (TextTool.isNull(macAddr)) {
                sb.append("-1").append("&&");
            } else {
                sb.append(macAddr).append("&&");
            }
            if (TextTool.isNull(ipAddr)) {
                sb.append("-1").append("&&");
            } else {
                sb.append(ipAddr).append("&&");
            }
            if (TextTool.isNull(String.valueOf(networkID))) {
                sb.append("-1").append("&&");
            } else {
                sb.append(networkID).append("&&");
            }
            if (TextTool.isNull(String.valueOf(speed))) {
                sb.append("-1");
            } else {
                sb.append(speed);
            }
            log("Update w info:" + sb.toString());
            this.mLocation = sb.toString();
        }
    }

    public void createJobber() {
        log("create job, mHandler is null: " + (this.mHandler == null));
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
            long delay = CommonUtil.getTime(4, 0, 0, 0) - System.currentTimeMillis();
            log("delay : " + delay);
            if (delay <= LISTENER_DELAY_TIME) {
                this.mHandler.sendEmptyMessageDelayed(2, LISTENER_DELAY_TIME);
            } else {
                this.mHandler.sendEmptyMessageDelayed(2, delay);
            }
        }
    }

    public void cancelJobber() {
        if (this.mLooper != null) {
            this.mLooper.quit();
        }
    }

    private void requestListener() {
        try {
            this.mHandler.removeMessages(2);
            long leadTime = (this.mLastUpdateTime + this.mUpdateRate) - System.currentTimeMillis();
            if (leadTime > 0) {
                Slog.e(TAG, "Lc has been updated 1 hours ago, waiting for the next update.");
                this.mHandler.sendEmptyMessageDelayed(2, leadTime);
                return;
            }
            this.mHandler.sendEmptyMessageDelayed(2, this.mUpdateRate);
            Slog.e(TAG, "Request listener start.");
        } catch (Exception e) {
            Slog.e(TAG, "exp:" + e);
            if (this.mRetryTime < 2) {
                this.mRetryTime++;
                Slog.e(TAG, "When exp retry.");
                this.mHandler.removeMessages(2);
                this.mHandler.sendEmptyMessageDelayed(2, ((long) (this.mRetryTime * (this.mRetryTime + 1))) * LISTENER_DELAY_TIME);
            }
        }
    }

    private void initialize() {
        this.mContext.registerReceiver(this.mWifiReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
        this.mContext.registerReceiver(this.mWifiReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private boolean isOnline() {
        NetworkInfo netInfo = this.mConnectivityManager.getActiveNetworkInfo();
        return netInfo != null ? netInfo.isConnectedOrConnecting() : false;
    }

    private String intToStrIp(int ip) {
        return (ip & 255) + "." + ((ip >> 8) & 255) + "." + ((ip >> 16) & 255) + "." + ((ip >> 24) & 255);
    }

    private void log(String msg) {
        if (AblConfig.isDebug()) {
            Slog.d(TAG, msg);
        }
    }

    public String getInfo() {
        return this.mLocation;
    }
}
