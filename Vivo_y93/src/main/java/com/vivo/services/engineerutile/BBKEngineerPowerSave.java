package com.vivo.services.engineerutile;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BBKEngineerPowerSave {
    private static final String ACTION_SAVE_POWER = "android.intent.action.ALARM_SAVE_POWER";
    private static final long DELAY_TIME_MILLIS = 28800000;
    private static final String TAG = "BBKEngineerUtileService";
    private static final String USER_ACTION_CONTACTS_CHANGED = "contacts_changed";
    private static final String USER_ACTION_NETWORK_CONNECTED = "network_connected";
    private static final String USER_ACTION_NO_CHANGEDE = "no";
    private static final String USER_ACTION_PROPER = "persist.sys.user.action";
    private static final String USER_ACTION_SIM_STATE_CHANGED = "sim_ready";
    private static final String USER_ACTION_TIME_CHANGED = "time_changed";
    private static final String USER_ACTION_USB_CONNECT = "usb_connect";
    private BroadcastReceiver mConnecteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (act.equals("android.intent.action.SIM_STATE_CHANGED")) {
                try {
                    if (((TelephonyManager) context.getSystemService("phone")).getSimState() == 5) {
                        Log.d(BBKEngineerPowerSave.TAG, "SIM_STATE_READY");
                        SystemProperties.set(BBKEngineerPowerSave.USER_ACTION_PROPER, BBKEngineerPowerSave.USER_ACTION_SIM_STATE_CHANGED);
                        BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                        BBKEngineerPowerSave.this.cancelAlarmManager();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (act.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (info != null && State.CONNECTED == info.getState()) {
                    Log.d(BBKEngineerPowerSave.TAG, "info.getState():" + info.getState());
                    SystemProperties.set(BBKEngineerPowerSave.USER_ACTION_PROPER, BBKEngineerPowerSave.USER_ACTION_NETWORK_CONNECTED);
                    BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                    BBKEngineerPowerSave.this.cancelAlarmManager();
                }
            } else if (act.equals("android.intent.action.SCREEN_ON") || act.equals("android.intent.action.SCREEN_OFF")) {
                BBKEngineerPowerSave.this.cancelAlarmManager();
                if (BBKEngineerPowerSave.USER_ACTION_NO_CHANGEDE.equals(SystemProperties.get(BBKEngineerPowerSave.USER_ACTION_PROPER, BBKEngineerPowerSave.USER_ACTION_NO_CHANGEDE))) {
                    BBKEngineerPowerSave.this.setAlarmManager();
                } else {
                    BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                }
            } else if (act.equals(BBKEngineerPowerSave.ACTION_SAVE_POWER)) {
                Log.d(BBKEngineerPowerSave.TAG, "AlarmReceiver");
                if (BBKEngineerPowerSave.this.needStart()) {
                    BBKEngineerPowerSave.this.showShutdownDailog();
                    return;
                }
                BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                BBKEngineerPowerSave.this.cancelAlarmManager();
            } else if (act.equals("android.intent.action.TIME_SET")) {
                Log.d(BBKEngineerPowerSave.TAG, "Intent.ACTION_TIME_CHANGED");
                if (BBKEngineerPowerSave.this.isSystemReady() && BBKEngineerPowerSave.this.isUserChageTime()) {
                    SystemProperties.set(BBKEngineerPowerSave.USER_ACTION_PROPER, BBKEngineerPowerSave.USER_ACTION_TIME_CHANGED);
                    BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                    BBKEngineerPowerSave.this.cancelAlarmManager();
                    return;
                }
                Log.d(BBKEngineerPowerSave.TAG, "system selfChange!");
            } else if (act.equals("android.intent.action.BATTERY_CHANGED")) {
                int status = intent.getIntExtra("status", 1);
                Log.d(BBKEngineerPowerSave.TAG, "battery_status:" + status);
                if (status == 2) {
                    SystemProperties.set(BBKEngineerPowerSave.USER_ACTION_PROPER, BBKEngineerPowerSave.USER_ACTION_USB_CONNECT);
                    BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                    BBKEngineerPowerSave.this.cancelAlarmManager();
                } else if (status == 5) {
                    BBKEngineerPowerSave.this.cancelAlarmManager();
                    BBKEngineerPowerSave.this.setAlarmManager();
                }
            }
        }
    };
    private Context mContext = null;
    Runnable mCountdownRunnable = new Runnable() {
        public void run() {
            if (BBKEngineerPowerSave.this.mShutdownDialog == null || !BBKEngineerPowerSave.this.mShutdownDialog.isShowing()) {
                Log.d(BBKEngineerPowerSave.TAG, "user cancel");
                return;
            }
            BBKEngineerPowerSave bBKEngineerPowerSave = BBKEngineerPowerSave.this;
            bBKEngineerPowerSave.mShutdownSecond = bBKEngineerPowerSave.mShutdownSecond - 1;
            BBKEngineerPowerSave.this.mShutdownDialog.setMessage(String.format(BBKEngineerPowerSave.this.mContext.getString(51249532), new Object[]{Integer.valueOf(BBKEngineerPowerSave.this.mShutdownSecond)}));
            if (BBKEngineerPowerSave.this.mShutdownSecond <= 0) {
                BBKEngineerPowerSave.this.shutdown();
            } else {
                BBKEngineerPowerSave.this.mHandler.postDelayed(BBKEngineerPowerSave.this.mCountdownRunnable, 1000);
            }
        }
    };
    public Handler mHandler = null;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.d(BBKEngineerPowerSave.TAG, "selfChange:" + selfChange);
            if (BBKEngineerPowerSave.this.simCardReady()) {
                BBKEngineerPowerSave.this.unRegisterNetworkEvent();
                BBKEngineerPowerSave.this.cancelAlarmManager();
            }
        }
    };
    private AlertDialog mShutdownDialog = null;
    private int mShutdownSecond = 30;

    public BBKEngineerPowerSave(Context context) {
        Log.d(TAG, "BBKEngineerPowerSave start");
        this.mContext = context;
        if (needStart()) {
            registerNetworkEvent();
        }
    }

    private void showShutdownDailog() {
        unRegisterNetworkEvent();
        cancelAlarmManager();
        Log.d(TAG, "showAlertDialog");
        this.mShutdownSecond = 30;
        this.mShutdownDialog = new Builder(this.mContext).setIconAttribute(16843605).setTitle(this.mContext.getString(51249531)).setMessage(String.format(this.mContext.getString(51249532), new Object[]{Integer.valueOf(this.mShutdownSecond)})).setPositiveButton(this.mContext.getString(51249533), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BBKEngineerPowerSave.this.shutdown();
            }
        }).setNegativeButton(this.mContext.getString(51249534), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(BBKEngineerPowerSave.TAG, "user cancel");
                dialog.dismiss();
                if (BBKEngineerPowerSave.this.needStart()) {
                    BBKEngineerPowerSave.this.registerNetworkEvent();
                    BBKEngineerPowerSave.this.setAlarmManager();
                }
            }
        }).create();
        this.mShutdownDialog.setCancelable(false);
        this.mShutdownDialog.getWindow().setType(2010);
        this.mShutdownDialog.getWindow().addFlags(2621568);
        this.mShutdownDialog.show();
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        this.mHandler.postDelayed(this.mCountdownRunnable, 1000);
    }

    private boolean needStart() {
        String factory = SystemProperties.get("persist.sys.factory.mode", USER_ACTION_NO_CHANGEDE);
        if (factory == null || !factory.equals("yes")) {
            String bsptestRunning = SystemProperties.get("sys.bsptest.finish", "-1");
            if (bsptestRunning == null || !(bsptestRunning.equals("0") || bsptestRunning.equals("1"))) {
                String act = SystemProperties.get(USER_ACTION_PROPER, USER_ACTION_NO_CHANGEDE);
                if (USER_ACTION_NO_CHANGEDE.equals(act)) {
                    if (!isSystemReady()) {
                        Log.i(TAG, "system not ready.");
                    } else if (simCardReady()) {
                        return false;
                    } else {
                        if (wifiConnected()) {
                            SystemProperties.set(USER_ACTION_PROPER, USER_ACTION_NETWORK_CONNECTED);
                            return false;
                        }
                    }
                    return true;
                }
                Log.d(TAG, "user mode:" + act);
                return false;
            }
            Log.d(TAG, "bsptest is running");
            return false;
        }
        Log.d(TAG, "factory mode");
        return false;
    }

    private void registerNetworkEvent() {
        Log.d(TAG, "registerNetworkEvent()");
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction(ACTION_SAVE_POWER);
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mConnecteReceiver, filter);
        this.mContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mObserver);
    }

    private void unRegisterNetworkEvent() {
        Log.d(TAG, "unRegisterNetworkEvent()");
        try {
            this.mContext.unregisterReceiver(this.mConnecteReceiver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        } catch (Exception ex) {
            Log.d(TAG, "unRegisterNetworkEvent():" + ex.getMessage());
        }
    }

    private void setAlarmManager() {
        Log.d(TAG, "setAlarmManager()");
        try {
            ((AlarmManager) this.mContext.getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + DELAY_TIME_MILLIS, getPendingIntent());
        } catch (Exception ex) {
            Log.d(TAG, "setAlarmManager():" + ex.getMessage());
        }
    }

    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager()");
        ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_SAVE_POWER), 0);
    }

    private boolean wifiConnectedBefore() {
        boolean network = false;
        try {
            String configPath = "/data/misc/wifi/wpa_supplicant.conf";
            FileReader reader = new FileReader(new File("/data/misc/wifi/wpa_supplicant.conf"));
            BufferedReader br = new BufferedReader(reader);
            String line;
            do {
                line = br.readLine();
                if (line == null) {
                    break;
                }
            } while (!line.startsWith("network={"));
            Log.d(TAG, "network={" + br.readLine() + "}");
            network = true;
            br.close();
            reader.close();
        } catch (Exception ex) {
            Log.e(TAG, "wifiConnectedBefore():" + ex.getMessage());
        }
        return network;
    }

    private boolean wifiConnected() {
        try {
            if (((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1).isConnected()) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "wifiConnected():" + ex.getMessage());
            return false;
        }
    }

    private boolean isSystemReady() {
        String bootanim = SystemProperties.get("init.svc.bootanim", "running");
        String boot_completed = SystemProperties.get("sys.boot_completed", "0");
        if (bootanim.equals("stopped") && boot_completed.equals("1")) {
            return true;
        }
        return false;
    }

    private boolean isUserChageTime() {
        try {
            if (Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) == 0) {
                return true;
            }
        } catch (Exception ex) {
            Log.e(TAG, "isUserChageTime():" + ex.getMessage());
        }
        return false;
    }

    private boolean simCardReady() {
        try {
            int contacts = this.mContext.getContentResolver().query(Contacts.CONTENT_URI, null, null, null, null).getCount();
            if (contacts > 0) {
                SystemProperties.set(USER_ACTION_PROPER, USER_ACTION_CONTACTS_CHANGED);
                Log.d(TAG, "Contacts count:" + contacts);
                return true;
            }
            if (((TelephonyManager) this.mContext.getSystemService("phone")).getSimState() == 5) {
                SystemProperties.set(USER_ACTION_PROPER, USER_ACTION_SIM_STATE_CHANGED);
                Log.d(TAG, "SIM_STATE_READY!");
                return true;
            }
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "simCardReady():" + ex.getMessage());
        }
    }

    private void shutdown() {
        try {
            Log.d(TAG, "shutdown by BBKEngineerPowerSave!");
            SystemProperties.set(USER_ACTION_PROPER, "shutdown");
            IPowerManager pms = Stub.asInterface(ServiceManager.getService("power"));
            Class<?> cls = Class.forName("android.os.IPowerManager");
            if (VERSION.SDK_INT > 23) {
                cls.getDeclaredMethod("shutdown", new Class[]{Boolean.TYPE, String.class, Boolean.TYPE}).invoke(pms, new Object[]{Boolean.valueOf(false), "BBKEngineerPowerSave", Boolean.valueOf(false)});
                return;
            }
            cls.getDeclaredMethod("shutdown", new Class[]{Boolean.TYPE, Boolean.TYPE}).invoke(pms, new Object[]{Boolean.valueOf(false), Boolean.valueOf(false)});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
