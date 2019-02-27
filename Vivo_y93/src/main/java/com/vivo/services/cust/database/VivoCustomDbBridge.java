package com.vivo.services.cust.database;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import com.vivo.services.cust.data.BluetoothAttr;
import com.vivo.services.cust.data.BrowserAttr;
import com.vivo.services.cust.data.ContactAttr;
import com.vivo.services.cust.data.NetworkAttr;
import com.vivo.services.cust.data.PackageAttr;
import com.vivo.services.cust.data.WlanAttr;
import com.vivo.services.cust.server.Utils;
import java.util.ArrayList;
import java.util.List;

public class VivoCustomDbBridge {
    private static final int ADD_PACKAGE_TO_PRELOAD = 1;
    private static final String TAG = "VivoCustomDbBridge";
    private BluetoothAttrDbHelper bluetoothDbHelper;
    private BrowserAttrDbHelper browserDbHelper;
    private ContactAttrDbHelper contactDbHelper;
    private Context context;
    private ActivityManager mActivityManager;
    private ArrayMap<String, BluetoothAttr> mBluetooths;
    private ArrayMap<String, BrowserAttr> mBrowsers;
    private ArrayMap<String, ContactAttr> mContacts;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    List<String> packageNames = VivoCustomDbBridge.this.getPersistApps();
                    for (int i = 0; i < packageNames.size(); i++) {
                        Log.d(VivoCustomDbBridge.TAG, "add persistent app: " + ((String) packageNames.get(i)) + " " + (VivoCustomDbBridge.this.mActivityManager.addPreloadProcess((String) packageNames.get(i), 5000) ? "successed" : "failed"));
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ArrayMap<String, NetworkAttr> mNetworks;
    private ArrayMap<String, PackageAttr> mPackages;
    private ArrayMap<String, WlanAttr> mWlans;
    private NetworkAttrDbHelper networkDbHelper;
    private PackageAttrDbHelper packageDbHelper;
    private WlanAttrDbHelper wlanDbHelper;

    public VivoCustomDbBridge(Context context) {
        this.context = context;
        this.packageDbHelper = new PackageAttrDbHelper(context);
        this.contactDbHelper = new ContactAttrDbHelper(context);
        this.browserDbHelper = new BrowserAttrDbHelper(context);
        this.networkDbHelper = new NetworkAttrDbHelper(context);
        this.wlanDbHelper = new WlanAttrDbHelper(context);
        this.bluetoothDbHelper = new BluetoothAttrDbHelper(context);
        this.mPackages = new ArrayMap();
        this.mContacts = new ArrayMap();
        this.mBrowsers = new ArrayMap();
        this.mNetworks = new ArrayMap();
        this.mWlans = new ArrayMap();
        this.mBluetooths = new ArrayMap();
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        load();
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageInstallBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageInstallBlackList(List<String> packageNames, boolean isBlackList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.installBlackList = isBlackList;
                    isUpdated = true;
                } else if (pa.installBlackList == isBlackList) {
                    return;
                } else {
                    pa.installBlackList = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageInstallBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.installBlackList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageInstallBlackList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:25:0x003e, code:
            if (r0 == false) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:26:0x0040, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:27:0x0043, code:
            android.util.Log.d(TAG, "setPackageInstallWhiteList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:28:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageInstallWhiteList(List<String> packageNames, boolean isWhiteList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null || pa.installWhiteList == isWhiteList) {
                    pa = new PackageAttr(packageName);
                    pa.installWhiteList = isWhiteList;
                    isUpdated = true;
                } else if (pa.installWhiteList == isWhiteList) {
                    return;
                } else {
                    pa.installWhiteList = isWhiteList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageInstallWhiteList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.installWhiteList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageInstallWhiteList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageUnInstallBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageUnInstallBlackList(List<String> packageNames, boolean isBlackList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.uninstallBlackList = isBlackList;
                    isUpdated = true;
                } else if (pa.uninstallBlackList == isBlackList) {
                    return;
                } else {
                    pa.uninstallBlackList = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageUnInstallBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.uninstallBlackList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageUnInstallBlackList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageDataNetworkBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageDataNetworkBlackList(List<String> packageNames, boolean isBlackList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.dataNetworkBlackList = isBlackList;
                    isUpdated = true;
                } else if (pa.dataNetworkBlackList == isBlackList) {
                    return;
                } else {
                    pa.dataNetworkBlackList = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageDataNetworkBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.dataNetworkBlackList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageDataNetworkBlackList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageDataNetworkWhiteList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageDataNetworkWhiteList(List<String> packageNames, boolean isWhiteList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.dataNetworkWhiteList = isWhiteList;
                    isUpdated = true;
                } else if (pa.dataNetworkWhiteList == isWhiteList) {
                    return;
                } else {
                    pa.dataNetworkWhiteList = isWhiteList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageDataNetworkWhiteList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.dataNetworkWhiteList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageDataNetworkWhiteList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageWifiNetworkBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageWifiNetworkBlackList(List<String> packageNames, boolean isBlackList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.wifiNetworkBlackList = isBlackList;
                    isUpdated = true;
                } else if (pa.wifiNetworkBlackList == isBlackList) {
                    return;
                } else {
                    pa.wifiNetworkBlackList = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageWifiNetworkBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.wifiNetworkBlackList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageWifiNetworkBlackList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPackageWifiNetworkWhiteList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageWifiNetworkWhiteList(List<String> packageNames, boolean isWhiteList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.wifiNetworkWhiteList = isWhiteList;
                    isUpdated = true;
                } else if (pa.wifiNetworkWhiteList == isWhiteList) {
                    return;
                } else {
                    pa.wifiNetworkWhiteList = isWhiteList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageWifiNetworkWhiteList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.wifiNetworkWhiteList) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageWifiNetworkWhiteList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setPersistentApp mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPersistApps(List<String> packageNames, boolean isPersist) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.persistent = isPersist;
                    isUpdated = true;
                } else if (pa.persistent == isPersist) {
                    return;
                } else {
                    pa.persistent = isPersist;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPersistApps() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.persistent) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPersistApps size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x007b, code:
            if (r0 == false) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:24:0x007d, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x0080, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDisallowedRunningApp(List<String> packageNames, boolean disAllow) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                Log.d(TAG, "setDisallowedRunningApp packageName:" + packageName + " disAllow:" + disAllow);
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa != null) {
                    Log.d(TAG, "setDisallowedRunningApp forbidRun:" + pa.forbidRun);
                    if (pa.forbidRun == disAllow) {
                        return;
                    } else {
                        pa.forbidRun = disAllow;
                        isUpdated = true;
                    }
                } else {
                    pa = new PackageAttr(packageName);
                    pa.forbidRun = disAllow;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getDisallowedRunningApp() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.forbidRun) {
                list.add(pa.packageName);
            }
        }
        return list;
    }

    public void syncPackages() {
        updatePackages();
    }

    /* JADX WARNING: Missing block: B:18:0x007c, code:
            if (r0 == false) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:19:0x007e, code:
            if (r13 == false) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:20:0x0080, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:21:0x0083, code:
            android.util.Log.d(TAG, "backupPackageNotificationState mPackages:" + r11 + ", enabled = " + r12);
     */
    /* JADX WARNING: Missing block: B:22:0x00a8, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean backupPackageNotificationState(String packageName, boolean enabled, boolean allowUpdate) {
        if (packageName == null) {
            return false;
        }
        synchronized (this.mPackages) {
            PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
            boolean isUpdated;
            if (pa != null) {
                int oldType = Integer.parseInt(pa.custType);
                if ((oldType & 1) != 0) {
                    Log.d(TAG, "backupPackageNotificationState failed, mPackages:" + packageName + ", enabled = " + enabled);
                    return false;
                }
                pa.custType = Integer.toString(oldType | 1);
                pa.retain3 = enabled;
                isUpdated = true;
                Log.d(TAG, "backupPackageNotificationState mPackages:" + packageName + ", newType = " + pa.custType);
            } else {
                pa = new PackageAttr(packageName);
                pa.custType = Integer.toString(1);
                pa.retain3 = enabled;
                isUpdated = true;
            }
            this.mPackages.put(packageName, pa);
        }
    }

    public List<String> getPackageNotificationBackupList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if ((Integer.parseInt(pa.custType) & 1) != 0) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageNotificationBackupList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:18:0x005a, code:
            if (r0 == false) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:19:0x005c, code:
            if (r11 == false) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:20:0x005e, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:21:0x0061, code:
            android.util.Log.d(TAG, "getPackageNotificationBackup mPackages:" + r10 + ", state = " + r4);
     */
    /* JADX WARNING: Missing block: B:22:0x0086, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPackageNotificationBackup(String packageName, boolean allowUpdate) {
        int state = -1;
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
            if (pa != null) {
                int oldType = Integer.parseInt(pa.custType);
                if ((oldType & 1) == 0) {
                    return -1;
                }
                pa.custType = Integer.toString(oldType & -2);
                state = pa.retain3 ? 1 : 0;
                pa.retain3 = false;
                isUpdated = true;
                Log.d(TAG, "getPackageNotificationBackup mPackages:" + packageName + ", newType = " + pa.custType);
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public int getPackageNotificationState(String packageName) {
        int state = 0;
        PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
        if (pa != null) {
            if (pa.retain1) {
                state = 1;
            }
            if (pa.retain2) {
                state |= 2;
            }
        }
        Log.d(TAG, "getPackageNotificationState packageName:" + packageName + ", state=" + state);
        return state;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            if (r10 == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:25:0x003e, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:26:0x0041, code:
            android.util.Log.d(TAG, "setPackageNotificationBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:27:0x0061, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageNotificationBlackList(List<String> packageNames, boolean isBlackList, boolean allowUpdate) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.retain1 = isBlackList;
                    isUpdated = true;
                } else if (pa.retain1 == isBlackList) {
                    return;
                } else {
                    pa.retain1 = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageNotificationBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.retain1) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageNotificationBlackList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            if (r10 == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:25:0x003e, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:26:0x0041, code:
            android.util.Log.d(TAG, "setPackageNotificationWhiteList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:27:0x0061, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPackageNotificationWhiteList(List<String> packageNames, boolean isWhiteList, boolean allowUpdate) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.retain2 = isWhiteList;
                    isUpdated = true;
                } else if (pa.retain2 == isWhiteList) {
                    return;
                } else {
                    pa.retain2 = isWhiteList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getPackageNotificationWhiteList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.retain2) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getPackageNotificationWhiteList size:" + list.size());
        return list;
    }

    /* JADX WARNING: Missing block: B:23:0x003a, code:
            if (r0 == false) goto L_0x003f;
     */
    /* JADX WARNING: Missing block: B:24:0x003c, code:
            updatePackages();
     */
    /* JADX WARNING: Missing block: B:25:0x003f, code:
            android.util.Log.d(TAG, "setForbitRunBlackList mPackages size:" + r7.mPackages.size());
     */
    /* JADX WARNING: Missing block: B:26:0x005f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setForbitRunBlackList(List<String> packageNames, boolean isBlackList) {
        boolean isUpdated = false;
        synchronized (this.mPackages) {
            for (String packageName : packageNames) {
                PackageAttr pa = (PackageAttr) this.mPackages.get(packageName);
                if (pa == null) {
                    pa = new PackageAttr(packageName);
                    pa.retain4 = isBlackList;
                    isUpdated = true;
                } else if (pa.retain4 == isBlackList) {
                    return;
                } else {
                    pa.retain4 = isBlackList;
                    isUpdated = true;
                }
                this.mPackages.put(packageName, pa);
            }
        }
    }

    public List<String> getForbitRunBlackList() {
        List<String> list = new ArrayList();
        for (PackageAttr pa : this.mPackages.values()) {
            if (pa.retain4) {
                list.add(pa.packageName);
            }
        }
        Log.d(TAG, "getForbitRunBlackList size:" + list.size());
        return list;
    }

    public void clearPackageState(int state) {
        boolean isUpdated = false;
        for (PackageAttr pa : this.mPackages.values()) {
            switch (state) {
                case 0:
                case 1:
                    break;
                case 2:
                    if (!pa.uninstallBlackList) {
                        break;
                    }
                    Log.d("VCS", "clear uninstall blackList: " + pa.packageName);
                    pa.uninstallBlackList = false;
                    isUpdated = true;
                    break;
                case 3:
                    if (!pa.uninstallWhiteList) {
                        break;
                    }
                    pa.uninstallWhiteList = false;
                    isUpdated = true;
                    break;
                case 4:
                    if (!pa.installBlackList) {
                        break;
                    }
                    pa.installBlackList = false;
                    isUpdated = true;
                    break;
                case 5:
                    if (!pa.installWhiteList) {
                        break;
                    }
                    pa.installWhiteList = false;
                    isUpdated = true;
                    break;
                case 6:
                    if (!pa.dataNetworkBlackList) {
                        break;
                    }
                    pa.dataNetworkBlackList = false;
                    isUpdated = true;
                    break;
                case 7:
                    if (!pa.dataNetworkWhiteList) {
                        break;
                    }
                    pa.dataNetworkWhiteList = false;
                    isUpdated = true;
                    break;
                case Utils.WIFIBL /*8*/:
                    if (!pa.wifiNetworkBlackList) {
                        break;
                    }
                    pa.wifiNetworkBlackList = false;
                    isUpdated = true;
                    break;
                case Utils.WIFIWL /*9*/:
                    if (!pa.wifiNetworkWhiteList) {
                        break;
                    }
                    pa.wifiNetworkWhiteList = false;
                    isUpdated = true;
                    break;
                default:
                    break;
            }
        }
        if (isUpdated) {
            updatePackages();
        }
    }

    private String modeToBehavior(int inOutMode) {
        String behavior = Utils.STR_BOTH;
        switch (inOutMode) {
            case 0:
                return Utils.STR_BOTH;
            case 1:
                return Utils.STR_CALLIN;
            case 2:
                return Utils.STR_CALLOUT;
            default:
                return behavior;
        }
    }

    private int behaviorToMode(String behavior) {
        if (behavior.equals(Utils.STR_BOTH)) {
            return 0;
        }
        if (behavior.equals(Utils.STR_CALLIN)) {
            return 1;
        }
        if (behavior.equals(Utils.STR_CALLOUT)) {
            return 2;
        }
        return 0;
    }

    private String simIDToSimslot(int simID) {
        String simslot = Utils.STR_BOTH;
        switch (simID) {
            case 0:
                return Utils.STR_BOTH;
            case 1:
                return Utils.STR_SIM1;
            case 2:
                return Utils.STR_SIM2;
            default:
                return simslot;
        }
    }

    private int simslotToSimID(String simslot) {
        if (simslot.equals(Utils.STR_BOTH)) {
            return 0;
        }
        if (simslot.equals(Utils.STR_SIM1)) {
            return 1;
        }
        if (simslot.equals(Utils.STR_SIM2)) {
            return 2;
        }
        return 0;
    }

    public void setPhoneBlackList(List<String> numbers, boolean isBlackList, int inOutMode, int simID) {
        String behavior = modeToBehavior(inOutMode);
        String simslot = simIDToSimslot(simID);
        boolean isUpdated = false;
        for (String num : numbers) {
            ContactAttr ct = (ContactAttr) this.mContacts.get(num);
            if (ct == null) {
                ct = new ContactAttr(num);
                ct.phone_blackList = isBlackList;
                ct.updatePhoneBalckBehavior(behavior);
                ct.updatePhoneBalckSimslot(simslot);
                isUpdated = true;
            } else if (ct.phone_blackList != isBlackList || (ct.getPhoneBalckBehavior().equals(behavior) ^ 1) != 0 || (ct.getPhoneBalckSimslot().equals(simslot) ^ 1) != 0) {
                ct.phone_blackList = isBlackList;
                ct.updatePhoneBalckBehavior(behavior);
                ct.updatePhoneBalckSimslot(simslot);
                isUpdated = true;
            }
            this.mContacts.put(num, ct);
        }
        if (isUpdated) {
            updateContacts();
        }
        Log.d(TAG, "setPhoneBlackList mContacts size:" + this.mContacts.size());
    }

    public void clearPhoneBlackList() {
        boolean isUpdated = false;
        for (ContactAttr ct : this.mContacts.values()) {
            if (ct.phone_blackList) {
                ct.phone_blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateContacts();
        }
    }

    public List<String> getPhoneBlackList() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.phone_blackList) {
                list.add(ca.number);
            }
        }
        Log.d(TAG, "getPhoneBlackList size:" + list.size());
        return list;
    }

    public List<String> getPhoneBlackListInfo() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.phone_blackList) {
                String modeStr = Integer.toString(behaviorToMode(ca.getPhoneBalckBehavior()));
                list.add(ca.number + "," + modeStr + "," + Integer.toString(simslotToSimID(ca.getPhoneBalckSimslot())));
            }
        }
        Log.d(TAG, "getPhoneBlackListInfo size:" + list.size());
        return list;
    }

    public void setPhoneWhiteList(List<String> numbers, boolean isWhiteList, int inOutMode, int simID) {
        String behavior = modeToBehavior(inOutMode);
        String simslot = simIDToSimslot(simID);
        boolean isUpdated = false;
        for (String num : numbers) {
            ContactAttr ct = (ContactAttr) this.mContacts.get(num);
            if (ct == null) {
                ct = new ContactAttr(num);
                ct.phone_whiteList = isWhiteList;
                ct.updatePhoneWhiteBehavior(behavior);
                ct.updatePhoneWhiteSimslot(simslot);
                isUpdated = true;
            } else if (ct.phone_whiteList != isWhiteList || (ct.getPhoneWhiteBehavior().equals(behavior) ^ 1) != 0 || (ct.getPhoneWhiteSimslot().equals(simslot) ^ 1) != 0) {
                ct.phone_whiteList = isWhiteList;
                ct.updatePhoneWhiteBehavior(behavior);
                ct.updatePhoneWhiteSimslot(simslot);
                isUpdated = true;
            }
            this.mContacts.put(num, ct);
        }
        if (isUpdated) {
            updateContacts();
        }
        Log.d(TAG, "setPhoneWhiteList mContacts size:" + this.mContacts.size());
    }

    public void clearPhoneWhiteList() {
        boolean isUpdated = false;
        for (ContactAttr ct : this.mContacts.values()) {
            if (ct.phone_whiteList) {
                ct.phone_whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateContacts();
        }
    }

    public List<String> getPhoneWhiteList() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.phone_whiteList) {
                list.add(ca.number);
            }
        }
        Log.d(TAG, "getPhoneWhiteList size:" + list.size());
        return list;
    }

    public List<String> getPhoneWhiteListInfo() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.phone_whiteList) {
                String modeStr = Integer.toString(behaviorToMode(ca.getPhoneWhiteBehavior()));
                list.add(ca.number + "," + modeStr + "," + Integer.toString(simslotToSimID(ca.getPhoneWhiteSimslot())));
            }
        }
        Log.d(TAG, "getPhoneWhiteListInfo size:" + list.size());
        return list;
    }

    public void setSmsBlackList(List<String> numbers, boolean isBlackList) {
        boolean isUpdated = false;
        for (String num : numbers) {
            ContactAttr ct = (ContactAttr) this.mContacts.get(num);
            if (ct == null || ct.sms_blackList == isBlackList) {
                ct = new ContactAttr(num);
                ct.sms_blackList = isBlackList;
            } else {
                ct.sms_blackList = isBlackList;
            }
            isUpdated = true;
            this.mContacts.put(num, ct);
        }
        if (isUpdated) {
            updateContacts();
        }
        Log.d(TAG, "setSmsBlackList mContacts size:" + this.mContacts.size());
    }

    public void clearSmsBlackList() {
        boolean isUpdated = false;
        for (ContactAttr ct : this.mContacts.values()) {
            if (ct.sms_blackList) {
                ct.sms_blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateContacts();
        }
    }

    public List<String> getSmsBlackList() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.sms_blackList) {
                list.add(ca.number);
            }
        }
        Log.d(TAG, "getSmsBlackList size:" + list.size());
        return list;
    }

    public void setSmsWhiteList(List<String> numbers, boolean isWhiteList) {
        boolean isUpdated = false;
        for (String num : numbers) {
            ContactAttr ct = (ContactAttr) this.mContacts.get(num);
            if (ct == null || ct.sms_blackList == isWhiteList) {
                ct = new ContactAttr(num);
                ct.sms_whiteList = isWhiteList;
            } else {
                ct.sms_whiteList = isWhiteList;
            }
            isUpdated = true;
            this.mContacts.put(num, ct);
        }
        if (isUpdated) {
            updateContacts();
        }
        Log.d(TAG, "setSmsBlackList mContacts size:" + this.mContacts.size());
    }

    public void clearSmsWhiteList() {
        boolean isUpdated = false;
        for (ContactAttr ct : this.mContacts.values()) {
            if (ct.sms_whiteList) {
                ct.sms_whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateContacts();
        }
    }

    public List<String> getSmsWhiteList() {
        List<String> list = new ArrayList();
        for (ContactAttr ca : this.mContacts.values()) {
            if (ca.sms_whiteList) {
                list.add(ca.number);
            }
        }
        Log.d(TAG, "getSmsBlackList size:" + list.size());
        return list;
    }

    public void setDomainNameBlackList(List<String> urls, boolean isBlackList) {
        boolean isUpdated = false;
        for (String url : urls) {
            BrowserAttr ba = (BrowserAttr) this.mBrowsers.get(url);
            if (ba == null || ba.blackList == isBlackList) {
                ba = new BrowserAttr(url);
                ba.blackList = isBlackList;
            } else {
                ba.blackList = isBlackList;
            }
            isUpdated = true;
            this.mBrowsers.put(url, ba);
        }
        if (isUpdated) {
            updateBrowsers();
        }
        Log.d(TAG, "setBrowserBlackList mBrowsers size:" + this.mBrowsers.size());
    }

    public void clearDomainNameBlackList() {
        boolean isUpdated = false;
        for (BrowserAttr ba : this.mBrowsers.values()) {
            if (ba.blackList) {
                ba.blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateBrowsers();
        }
    }

    public List<String> getDomainNameBlackList() {
        List<String> list = new ArrayList();
        for (BrowserAttr ba : this.mBrowsers.values()) {
            if (ba.blackList) {
                list.add(ba.url);
            }
        }
        Log.d(TAG, "getBrowsersBlackList size:" + list.size());
        return list;
    }

    public void setDomainNameWhiteList(List<String> urls, boolean isWhiteList) {
        boolean isUpdated = false;
        for (String url : urls) {
            BrowserAttr ba = (BrowserAttr) this.mBrowsers.get(url);
            if (ba == null || ba.whiteList == isWhiteList) {
                ba = new BrowserAttr(url);
                ba.whiteList = isWhiteList;
            } else {
                ba.whiteList = isWhiteList;
            }
            isUpdated = true;
            this.mBrowsers.put(url, ba);
        }
        if (isUpdated) {
            updateBrowsers();
        }
        Log.d(TAG, "setBrowsersWhiteList mBrowsers size:" + this.mBrowsers.size());
    }

    public void clearDomainNameWhiteList() {
        boolean isUpdated = false;
        for (BrowserAttr ba : this.mBrowsers.values()) {
            if (ba.whiteList) {
                ba.whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateBrowsers();
        }
    }

    public List<String> getDomainNameWhiteList() {
        List<String> list = new ArrayList();
        for (BrowserAttr ba : this.mBrowsers.values()) {
            if (ba.whiteList) {
                list.add(ba.url);
            }
        }
        Log.d(TAG, "getBrowsersWhiteList size:" + list.size());
        return list;
    }

    public void setIpAddrBlackList(List<String> ips, boolean isBlackList) {
        boolean isUpdated = false;
        for (String ip : ips) {
            NetworkAttr na = (NetworkAttr) this.mNetworks.get(ip);
            if (na == null || na.blackList == isBlackList) {
                na = new NetworkAttr(ip);
                na.blackList = isBlackList;
            } else {
                na.blackList = isBlackList;
            }
            isUpdated = true;
            this.mNetworks.put(ip, na);
        }
        if (isUpdated) {
            updateNetworks();
        }
        Log.d(TAG, "setNetworksBlackList mNetworks size:" + this.mNetworks.size());
    }

    public void clearIpAddrBlackList() {
        boolean isUpdated = false;
        for (NetworkAttr na : this.mNetworks.values()) {
            if (na.blackList) {
                na.blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateNetworks();
        }
    }

    public List<String> getIpAddrBlackList() {
        List<String> list = new ArrayList();
        for (NetworkAttr na : this.mNetworks.values()) {
            if (na.blackList) {
                list.add(na.ip);
            }
        }
        Log.d(TAG, "getNetworksBlackList size:" + list.size());
        return list;
    }

    public void setIpAddrWhiteList(List<String> ips, boolean isWhiteList) {
        boolean isUpdated = false;
        for (String ip : ips) {
            NetworkAttr na = (NetworkAttr) this.mNetworks.get(ip);
            if (na == null || na.whiteList == isWhiteList) {
                na = new NetworkAttr(ip);
                na.whiteList = isWhiteList;
            } else {
                na.whiteList = isWhiteList;
            }
            isUpdated = true;
            this.mNetworks.put(ip, na);
        }
        if (isUpdated) {
            updateNetworks();
        }
        Log.d(TAG, "setNetworksWhiteList mNetworks size:" + this.mNetworks.size());
    }

    public void clearIpAddrWhiteList() {
        boolean isUpdated = false;
        for (NetworkAttr na : this.mNetworks.values()) {
            if (na.whiteList) {
                na.whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateNetworks();
        }
    }

    public List<String> getIpAddrWhiteList() {
        List<String> list = new ArrayList();
        for (NetworkAttr na : this.mNetworks.values()) {
            if (na.whiteList) {
                list.add(na.ip);
            }
        }
        Log.d(TAG, "getNetworksWhiteList size:" + list.size());
        return list;
    }

    public void setWlanBlackList(List<String> ssids, boolean isBlackList) {
        boolean isUpdated = false;
        for (String ssid : ssids) {
            WlanAttr wt = (WlanAttr) this.mWlans.get(ssid);
            if (wt == null || wt.blackList == isBlackList) {
                wt = new WlanAttr(ssid);
                wt.blackList = isBlackList;
            } else {
                wt.blackList = isBlackList;
            }
            isUpdated = true;
            this.mWlans.put(ssid, wt);
        }
        if (isUpdated) {
            updateWlans();
        }
        Log.d(TAG, "setWlanBlackList Wlans size:" + this.mWlans.size());
    }

    public void clearWlanBlackList() {
        boolean isUpdated = false;
        for (WlanAttr wt : this.mWlans.values()) {
            if (wt.blackList) {
                wt.blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateWlans();
        }
    }

    public List<String> getWlanBlackList() {
        List<String> list = new ArrayList();
        for (WlanAttr wt : this.mWlans.values()) {
            if (wt.blackList) {
                list.add(wt.iccid);
            }
        }
        Log.d(TAG, "getWlanBlackList size:" + list.size());
        return list;
    }

    public void setWlanWhiteList(List<String> iccids, boolean isWhiteList) {
        boolean isUpdated = false;
        for (String iccid : iccids) {
            WlanAttr wt = (WlanAttr) this.mWlans.get(iccid);
            if (wt == null || wt.whiteList == isWhiteList) {
                wt = new WlanAttr(iccid);
                wt.whiteList = isWhiteList;
            } else {
                wt.whiteList = isWhiteList;
            }
            isUpdated = true;
            this.mWlans.put(iccid, wt);
        }
        if (isUpdated) {
            updateWlans();
        }
        Log.d(TAG, "setWlanWhiteList mWlans size:" + this.mWlans.size());
    }

    public void clearWlanWhiteList() {
        boolean isUpdated = false;
        for (WlanAttr wt : this.mWlans.values()) {
            if (wt.whiteList) {
                wt.whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateWlans();
        }
    }

    public List<String> getWlanWhiteList() {
        List<String> list = new ArrayList();
        for (WlanAttr wt : this.mWlans.values()) {
            if (wt.whiteList) {
                list.add(wt.iccid);
            }
        }
        Log.d(TAG, "getWlanWhiteList size:" + list.size());
        return list;
    }

    public void setBluetoothBlackList(List<String> macs, boolean isBlackList) {
        boolean isUpdated = false;
        for (String mac : macs) {
            BluetoothAttr bt = (BluetoothAttr) this.mBluetooths.get(mac);
            if (bt == null || bt.blackList == isBlackList) {
                bt = new BluetoothAttr(mac);
                bt.blackList = isBlackList;
            } else {
                bt.blackList = isBlackList;
            }
            isUpdated = true;
            this.mBluetooths.put(mac, bt);
        }
        if (isUpdated) {
            updateBluetooths();
        }
        Log.d(TAG, "setBluetoothBlackList mBluetooths size:" + this.mBluetooths.size());
    }

    public void clearBluetoothBlackList() {
        boolean isUpdated = false;
        for (BluetoothAttr bt : this.mBluetooths.values()) {
            if (bt.blackList) {
                bt.blackList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateBluetooths();
        }
    }

    public List<String> getBluetoothBlackList() {
        List<String> list = new ArrayList();
        for (BluetoothAttr bt : this.mBluetooths.values()) {
            if (bt.blackList) {
                list.add(bt.mac);
            }
        }
        Log.d(TAG, "getBluetoothBlackList size:" + list.size());
        return list;
    }

    public void setBluetoothWhiteList(List<String> macs, boolean isWhiteList) {
        boolean isUpdated = false;
        for (String mac : macs) {
            BluetoothAttr bt = (BluetoothAttr) this.mBluetooths.get(mac);
            if (bt == null || bt.whiteList == isWhiteList) {
                bt = new BluetoothAttr(mac);
                bt.whiteList = isWhiteList;
            } else {
                bt.whiteList = isWhiteList;
            }
            isUpdated = true;
            this.mBluetooths.put(mac, bt);
        }
        if (isUpdated) {
            updateBluetooths();
        }
        Log.d(TAG, "setBluetoothWhiteList mBluetooths size:" + this.mBluetooths.size());
    }

    public void clearBluetoothWhiteList() {
        boolean isUpdated = false;
        for (BluetoothAttr bt : this.mBluetooths.values()) {
            if (bt.whiteList) {
                bt.whiteList = false;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            updateBluetooths();
        }
    }

    public List<String> getBluetoothWhiteList() {
        List<String> list = new ArrayList();
        for (BluetoothAttr bt : this.mBluetooths.values()) {
            if (bt.whiteList) {
                list.add(bt.mac);
            }
        }
        Log.d(TAG, "getBluetoothWhiteList size:" + list.size());
        return list;
    }

    private void load() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mPackages) {
                    List<PackageAttr> allPakcages = VivoCustomDbBridge.this.packageDbHelper.getAllPackages();
                    if (allPakcages != null && allPakcages.size() > 0) {
                        for (PackageAttr p : allPakcages) {
                            Log.d(VivoCustomDbBridge.TAG, "init package:" + p.packageName);
                            VivoCustomDbBridge.this.mPackages.put(p.packageName, p);
                        }
                    }
                }
                synchronized (VivoCustomDbBridge.this.mContacts) {
                    List<ContactAttr> allContacts = VivoCustomDbBridge.this.contactDbHelper.getAllContacts();
                    if (allContacts != null && allContacts.size() > 0) {
                        for (ContactAttr c : allContacts) {
                            Log.d(VivoCustomDbBridge.TAG, "init contact:" + c.number);
                            VivoCustomDbBridge.this.mContacts.put(c.number, c);
                        }
                    }
                }
                synchronized (VivoCustomDbBridge.this.mBrowsers) {
                    List<BrowserAttr> allUrls = VivoCustomDbBridge.this.browserDbHelper.getAllBrowsers();
                    if (allUrls != null && allUrls.size() > 0) {
                        for (BrowserAttr b : allUrls) {
                            Log.d(VivoCustomDbBridge.TAG, "init browser:" + b.url);
                            VivoCustomDbBridge.this.mBrowsers.put(b.url, b);
                        }
                    }
                }
                synchronized (VivoCustomDbBridge.this.mNetworks) {
                    List<NetworkAttr> allIps = VivoCustomDbBridge.this.networkDbHelper.getAllNetworks();
                    if (allIps != null && allIps.size() > 0) {
                        for (NetworkAttr n : allIps) {
                            Log.d(VivoCustomDbBridge.TAG, "init network:" + n.ip);
                            VivoCustomDbBridge.this.mNetworks.put(n.ip, n);
                        }
                    }
                }
                synchronized (VivoCustomDbBridge.this.mWlans) {
                    List<WlanAttr> allwlans = VivoCustomDbBridge.this.wlanDbHelper.getAllWlans();
                    if (allwlans != null && allwlans.size() > 0) {
                        for (WlanAttr w : allwlans) {
                            Log.d(VivoCustomDbBridge.TAG, "init wlans:" + w.iccid);
                            VivoCustomDbBridge.this.mWlans.put(w.iccid, w);
                        }
                    }
                }
                synchronized (VivoCustomDbBridge.this.mBluetooths) {
                    List<BluetoothAttr> allBluetooths = VivoCustomDbBridge.this.bluetoothDbHelper.getAllBluetooths();
                    if (allBluetooths != null && allBluetooths.size() > 0) {
                        for (BluetoothAttr bt : allBluetooths) {
                            Log.d(VivoCustomDbBridge.TAG, "init mBluetooths:" + bt.mac);
                            VivoCustomDbBridge.this.mBluetooths.put(bt.mac, bt);
                        }
                    }
                }
                Message message = new Message();
                message.what = 1;
                VivoCustomDbBridge.this.mHandler.sendMessage(message);
            }
        }).start();
    }

    private void updatePackages() {
        new Thread(new Runnable() {
            public void run() {
                List<String> removePackageList = new ArrayList();
                synchronized (VivoCustomDbBridge.this.mPackages) {
                    for (PackageAttr pa : VivoCustomDbBridge.this.mPackages.values()) {
                        int type = Integer.parseInt(pa.custType);
                        if (Utils.DEBUG_EXTRA) {
                            Log.d(VivoCustomDbBridge.TAG, "updatePackages " + pa.packageName + ":" + pa.custType + "," + pa.persistent + "," + pa.forbidRun + "," + pa.uninstallBlackList + "," + pa.uninstallWhiteList + "," + pa.installBlackList + "," + pa.installWhiteList + "," + pa.dataNetworkBlackList + "," + pa.dataNetworkWhiteList + "," + pa.wifiNetworkBlackList + "," + pa.wifiNetworkWhiteList + "," + pa.retain1 + "," + pa.retain2);
                        }
                        if (type != 0 || ((((((((((((((((pa.persistent | pa.forbidRun) | pa.uninstallBlackList) | pa.uninstallWhiteList) | pa.installBlackList) | pa.installWhiteList) | pa.dataNetworkBlackList) | pa.dataNetworkWhiteList) | pa.wifiNetworkBlackList) | pa.wifiNetworkWhiteList) | pa.retain1) | pa.retain2) | pa.retain3) | pa.retain4) | pa.retain5) | pa.retain6) | pa.retain7)) {
                            VivoCustomDbBridge.this.packageDbHelper.save(pa);
                        } else {
                            if (Utils.DEBUG_EXTRA) {
                                Log.i(VivoCustomDbBridge.TAG, "updatePackages add removePackageList :packageName = " + pa.packageName);
                            }
                            removePackageList.add(pa.packageName);
                        }
                    }
                    if (!removePackageList.isEmpty()) {
                        for (String packName : removePackageList) {
                            Log.i(VivoCustomDbBridge.TAG, "updatePackages remove :packName = " + packName);
                            VivoCustomDbBridge.this.mPackages.remove(packName);
                            VivoCustomDbBridge.this.packageDbHelper.delete(packName);
                        }
                    }
                    if (Utils.DEBUG_EXTRA) {
                        if (VivoCustomDbBridge.this.mPackages != null) {
                            Log.i(VivoCustomDbBridge.TAG, "updatePackages :mPackages size = " + VivoCustomDbBridge.this.mPackages.size());
                        }
                        List<PackageAttr> allPakcages = VivoCustomDbBridge.this.packageDbHelper.getAllPackages();
                        if (allPakcages != null) {
                            Log.i(VivoCustomDbBridge.TAG, "updatePackages :allPakcages size = " + allPakcages.size());
                            for (PackageAttr pkg : allPakcages) {
                                Log.i(VivoCustomDbBridge.TAG, "updatePackages :pkg = " + pkg.packageName);
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void updateContacts() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mContacts) {
                    for (ContactAttr ct : VivoCustomDbBridge.this.mContacts.values()) {
                        VivoCustomDbBridge.this.contactDbHelper.save(ct);
                    }
                }
            }
        }).start();
    }

    private void updateBrowsers() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mBrowsers) {
                    for (BrowserAttr ba : VivoCustomDbBridge.this.mBrowsers.values()) {
                        VivoCustomDbBridge.this.browserDbHelper.save(ba);
                    }
                }
            }
        }).start();
    }

    private void updateNetworks() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mNetworks) {
                    for (NetworkAttr na : VivoCustomDbBridge.this.mNetworks.values()) {
                        VivoCustomDbBridge.this.networkDbHelper.save(na);
                    }
                }
            }
        }).start();
    }

    private void updateWlans() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mWlans) {
                    for (WlanAttr wt : VivoCustomDbBridge.this.mWlans.values()) {
                        VivoCustomDbBridge.this.wlanDbHelper.save(wt);
                    }
                }
            }
        }).start();
    }

    private void updateBluetooths() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoCustomDbBridge.this.mBluetooths) {
                    for (BluetoothAttr bt : VivoCustomDbBridge.this.mBluetooths.values()) {
                        VivoCustomDbBridge.this.bluetoothDbHelper.save(bt);
                    }
                }
            }
        }).start();
    }
}
