package com.vivo.services.engineerutile;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.vivo.services.rms.ProcessList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import vivo.app.engineerutile.IBBKEngineerUtileService.Stub;

public class BBKEngineerUtileService extends Stub {
    private static final int CHECK_CAMERA_DEVICE_CONNECT_TRANSACTION = 6;
    private static final int IS_SERVICE_WORK_TRANSACTION = 8;
    private static final int IS_SETUPWIZARD_DISABLED_TRANSACTION = 7;
    private static final int SEND_BROADCAST_FROM_ATCID_TRANSACTION = 1;
    private static final int START_ACTIVITY_FROM_ATCID_TRANSACTION = 2;
    private static final int START_SERVICE_FROM_ATCID_TRANSACTION = 4;
    private static final int STOP_ACTIVITY_FROM_ATCID_TRANSACTION = 3;
    private static final int STOP_SERVICE_FROM_ATCID_TRANSACTION = 5;
    private static final String TAG = "BBKEngineerUtileService";
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    Runnable poolRoot;

    public BBKEngineerUtileService() {
        this.mContext = null;
        this.poolRoot = new Runnable() {
            /* JADX WARNING: Removed duplicated region for block: B:24:0x0061 A:{SYNTHETIC, Splitter: B:24:0x0061} */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Throwable th;
                BufferedReader br = null;
                try {
                    BufferedReader br2 = new BufferedReader(new FileReader(new File("proc/isroot/isroot")));
                    try {
                        if (br2.readLine().contains("1")) {
                            SystemProperties.set("sys.emsvr.root", "1");
                            Log.d(BBKEngineerUtileService.TAG, "set is_root flag");
                        } else {
                            BBKEngineerUtileService.this.mHandler.postDelayed(BBKEngineerUtileService.this.poolRoot, 3000);
                        }
                        if (br2 != null) {
                            try {
                                br2.close();
                            } catch (Exception e) {
                            }
                        }
                        br = br2;
                    } catch (Exception e2) {
                        br = br2;
                    } catch (Throwable th2) {
                        th = th2;
                        br = br2;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e3) {
                            }
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    try {
                        Log.e(BBKEngineerUtileService.TAG, "Open proc/isroot/isroot fail!!!");
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e5) {
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (br != null) {
                        }
                        throw th;
                    }
                }
            }
        };
        this.mHandlerThread = new HandlerThread("BBKEngineerUtileServiceHT");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
    }

    public BBKEngineerUtileService(Context context) {
        this.mContext = null;
        this.poolRoot = /* anonymous class already generated */;
        Log.d(TAG, "BBKEngineerUtileService service start");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("BBKEngineerUtileServiceHT");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mHandler.postDelayed(this.poolRoot, 10000);
        BBKEngineerPowerSave bBKEngineerPowerSave = new BBKEngineerPowerSave(this.mContext);
        startPcbaService();
    }

    public void sendBroadcastFromAtcid(final String name, final String action, final String extra) {
        String _name = name;
        String _action = action;
        String _extra = extra;
        this.mHandler.post(new Runnable() {
            public void run() {
                String[] strings;
                Intent intent = new Intent(action);
                intent.addFlags(16777216);
                if (name != null) {
                    strings = name.split("/");
                    if (!(strings[0] == null || strings[1] == null)) {
                        if (!strings[1].contains(strings[0])) {
                            strings[1] = strings[0] + strings[1];
                        }
                        Log.d(BBKEngineerUtileService.TAG, "packageName: " + strings[0] + "/" + strings[1]);
                        intent.setClassName(strings[0], strings[1]);
                    }
                }
                if (extra != null) {
                    int i = 0;
                    strings = extra.split(" ");
                    while (i < strings.length) {
                        if (strings[i].equals("-e") || strings[i].equals("--es")) {
                            intent.putExtra(strings[i + 1], strings[i + 2]);
                        } else if (strings[i].equals("--ez")) {
                            intent.putExtra(strings[i + 1], Boolean.parseBoolean(strings[i + 2]));
                        } else if (strings[i].equals("--ei")) {
                            intent.putExtra(strings[i + 1], Integer.parseInt(strings[i + 2]));
                        } else if (strings[i].equals("--el")) {
                            intent.putExtra(strings[i + 1], Long.parseLong(strings[i + 2]));
                        }
                        i += 3;
                    }
                }
                Log.d(BBKEngineerUtileService.TAG, "sendBroadcastFromAtcid intent: " + intent.toString());
                BBKEngineerUtileService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        });
    }

    public void startActivityFromAtcid(final String name, final String action, final String extra) {
        String _name = name;
        String _action = action;
        String _extra = extra;
        this.mHandler.post(new Runnable() {
            public void run() {
                String[] strings;
                Intent intent = new Intent(action);
                if (name != null) {
                    strings = name.split("/");
                    if (!(strings[0] == null || strings[1] == null)) {
                        if (!strings[1].contains(strings[0])) {
                            strings[1] = strings[0] + strings[1];
                        }
                        Log.d(BBKEngineerUtileService.TAG, "packageName: " + strings[0] + "/" + strings[1]);
                        intent.setClassName(strings[0], strings[1]);
                    }
                }
                if (extra != null) {
                    int i = 0;
                    strings = extra.split(" ");
                    while (i < strings.length) {
                        if (strings[i].equals("-e") || strings[i].equals("--es")) {
                            intent.putExtra(strings[i + 1], strings[i + 2]);
                        } else if (strings[i].equals("--ez")) {
                            intent.putExtra(strings[i + 1], Boolean.parseBoolean(strings[i + 2]));
                        } else if (strings[i].equals("--ei")) {
                            intent.putExtra(strings[i + 1], Integer.parseInt(strings[i + 2]));
                        } else if (strings[i].equals("--el")) {
                            intent.putExtra(strings[i + 1], Long.parseLong(strings[i + 2]));
                        }
                        i += 3;
                    }
                }
                Log.d(BBKEngineerUtileService.TAG, "startActivityFromAtcid intent: " + intent.toString());
                intent.setFlags(268435456);
                BBKEngineerUtileService.this.mContext.startActivity(intent);
            }
        });
    }

    public void stopActivityFromAtcid(final String name, String action, String extra) {
        String _name = name;
        this.mHandler.post(new Runnable() {
            public void run() {
                ((ActivityManager) BBKEngineerUtileService.this.mContext.getSystemService("activity")).forceStopPackage(name);
            }
        });
    }

    public void startServiceFromAtcid(final String name, final String action, final String extra) {
        String _name = name;
        String _action = action;
        String _extra = extra;
        this.mHandler.post(new Runnable() {
            public void run() {
                String[] strings;
                Intent intent = new Intent(action);
                String packageName = null;
                if (name != null) {
                    strings = name.split("/");
                    if (!(strings[0] == null || strings[1] == null)) {
                        if (!strings[1].contains(strings[0])) {
                            strings[1] = strings[0] + strings[1];
                        }
                        Log.d(BBKEngineerUtileService.TAG, "packageName: " + strings[0] + "/" + strings[1]);
                        packageName = strings[0];
                        intent.setClassName(strings[0], strings[1]);
                    }
                }
                if (extra != null) {
                    int i = 0;
                    strings = extra.split(" ");
                    while (i < strings.length) {
                        if (strings[i].equals("-e") || strings[i].equals("--es")) {
                            intent.putExtra(strings[i + 1], strings[i + 2]);
                        } else if (strings[i].equals("--ez")) {
                            intent.putExtra(strings[i + 1], Boolean.parseBoolean(strings[i + 2]));
                        } else if (strings[i].equals("--ei")) {
                            intent.putExtra(strings[i + 1], Integer.parseInt(strings[i + 2]));
                        } else if (strings[i].equals("--el")) {
                            intent.putExtra(strings[i + 1], Long.parseLong(strings[i + 2]));
                        }
                        i += 3;
                    }
                }
                if (packageName != null) {
                    try {
                        ApplicationInfo appInfo = BBKEngineerUtileService.this.mContext.getPackageManager().getApplicationInfo(packageName, 1);
                        Log.d(BBKEngineerUtileService.TAG, "startServiceFromAtcid, uid: " + appInfo.uid + ", intent: " + intent.toString());
                        if (1000 == appInfo.uid) {
                            BBKEngineerUtileService.this.mContext.startService(intent);
                        } else {
                            BBKEngineerUtileService.this.mContext.startForegroundService(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void stopServiceFromAtcid(final String name, final String action, final String extra) {
        String _name = name;
        String _action = action;
        String _extra = extra;
        this.mHandler.post(new Runnable() {
            public void run() {
                String[] strings;
                Intent intent = new Intent(action);
                if (name != null) {
                    strings = name.split("/");
                    if (!(strings[0] == null || strings[1] == null)) {
                        if (!strings[1].contains(strings[0])) {
                            strings[1] = strings[0] + strings[1];
                        }
                        Log.d(BBKEngineerUtileService.TAG, "packageName: " + strings[0] + "/" + strings[1]);
                        intent.setClassName(strings[0], strings[1]);
                    }
                }
                if (extra != null) {
                    int i = 0;
                    strings = extra.split(" ");
                    while (i < strings.length) {
                        if (strings[i].equals("-e") || strings[i].equals("--es")) {
                            intent.putExtra(strings[i + 1], strings[i + 2]);
                        } else if (strings[i].equals("--ez")) {
                            intent.putExtra(strings[i + 1], Boolean.parseBoolean(strings[i + 2]));
                        } else if (strings[i].equals("--ei")) {
                            intent.putExtra(strings[i + 1], Integer.parseInt(strings[i + 2]));
                        } else if (strings[i].equals("--el")) {
                            intent.putExtra(strings[i + 1], Long.parseLong(strings[i + 2]));
                        }
                        i += 3;
                    }
                }
                Log.d(BBKEngineerUtileService.TAG, "stopServiceFromAtcid intent: " + intent.toString());
                BBKEngineerUtileService.this.mContext.stopService(intent);
            }
        });
    }

    public int checkCameraDeviceConnect() {
        int status = 0;
        CameraInfo mCameraInfo = new CameraInfo();
        Log.v(TAG, "checkCameraDeviceConnect");
        int mCameraNumber = Camera.getNumberOfCameras();
        Log.e(TAG, "yqm test cameranumbers=" + mCameraNumber);
        for (int i = 0; i < mCameraNumber; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            Log.e(TAG, "yqm test cameinfo.facing=" + mCameraInfo.facing);
            if (mCameraInfo.facing == 0) {
                status |= 1;
            } else if (mCameraInfo.facing == 1) {
                status |= 2;
            }
        }
        Log.v(TAG, "status: " + status);
        return status & 3;
    }

    public int isSetupwizardDisabled() {
        int state = this.mContext.getPackageManager().getComponentEnabledSetting(new ComponentName("com.vivo.setupwizard", "com.vivo.setupwizard.LaunchActivity"));
        Log.v(TAG, "isSetupwizardDisabled state: " + state);
        return state;
    }

    public int isServiceWork(String serviceName) {
        List<RunningServiceInfo> mList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningServices(ProcessList.PERCEPTIBLE_APP_ADJ);
        if (mList == null) {
            Log.v(TAG, "service list null");
            return 0;
        }
        Log.v(TAG, "List.size():" + mList.size());
        int i = mList.size() - 1;
        while (i >= 0) {
            if (serviceName.equals(((RunningServiceInfo) mList.get(i)).service.getClassName())) {
                Log.v(TAG, "position:" + i + "mList.get(i).started: " + ((RunningServiceInfo) mList.get(i)).started + ", mList.get(i).restarting: " + ((RunningServiceInfo) mList.get(i)).restarting);
                if (((RunningServiceInfo) mList.get(i)).started && ((RunningServiceInfo) mList.get(i)).restarting == 0) {
                    return 1;
                }
                return 0;
            }
            i--;
        }
        return 0;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 6:
                data.enforceInterface("vivo.app.engineerutile.IBBKEngineerUtileService");
                int val = checkCameraDeviceConnect();
                reply.writeNoException();
                reply.writeInt(val);
                return true;
            case 7:
                data.enforceInterface("vivo.app.engineerutile.IBBKEngineerUtileService");
                int state = isSetupwizardDisabled();
                reply.writeNoException();
                reply.writeInt(state);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private void startPcbaService() {
        if (!"1".equals(SystemProperties.get("ro.pcba.control", "1"))) {
            Log.v(TAG, "start PCBAFloatView");
            PCBAFloatView pCBAFloatView = new PCBAFloatView(this.mContext);
        }
    }
}
