package com.vivo.services.security.server;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PermissionInfo;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.VivoCustomUtils;
import com.android.internal.util.ArrayUtils;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.vivo.common.utils.MD5Util;
import com.vivo.framework.security.VivoPermissionManager;
import com.vivo.services.rms.ProcessList;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import com.vivo.services.security.client.VivoPermissionInfo;
import com.vivo.services.security.client.VivoPermissionType;
import com.vivo.services.security.client.VivoPermissionType.VivoPermissionCategory;
import com.vivo.services.security.client.VivoPermissionType.VivoPermissionGroup;
import com.vivo.services.security.server.db.VivoPermissionDataBase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VivoPermissionConfig {
    public static final String ACCESS_WIFI_STATE_PERMISSION = "ACCESS_WIFI_STATE";
    private static final int DEBUG_CTS_TEST = SystemProperties.getInt("persist.debug.ctstest", 0);
    private static final boolean DEBUG_CTS_TEST_23;
    private static final String GN_SUPPORT = SystemProperties.get("ro.build.gn.support", "0");
    public static final boolean IS_25T30_LITE = "Funtouch OS_3.0 Lite".equals(SystemProperties.get("ro.vivo.os.build.display.id", "0"));
    private static final String PLATFORM_PACKAGE_NAME = "android";
    private static final String TAG = "VPS_VPC";
    private static final File mBuiltInThirdPartDataDir = new File("/data/vivo-apps");
    private static final File mBuiltInThirdPartDir = new File("/apps");
    private static final File mBuiltInThirdPartVivoDir = new File("/system/vivo-apps");
    private static int mDataBaseState = 1;
    private static byte[] mVPILock = new byte[0];
    private final int REMOVED_APP_LIST_MAX_NUM = 10;
    private ArrayList<String> mAppWhiteList = null;
    private AppOpsManager mAppopsManager = null;
    private HashMap<String, String> mBuiltInThirdPartMap = null;
    private Context mContext = null;
    private boolean mIsConfigFinished = false;
    private List<VivoPermissionInfo> mMonitorAppList = null;
    private PackageManager mPackageManager = null;
    private SparseArray<ArrayList<VivoPermissionInfo>> mPermissionList = null;
    private HashMap<String, VivoPermissionInfo> mPermissionMap = null;
    private ArrayList<VivoPermissionInfo> mRemovedAppList = null;
    private List<VivoPermissionInfo> mTrustedAppList = null;
    private VivoPermissionDataBase mVPDB = null;
    private VivoPermissionService mVPS = null;

    static {
        boolean z;
        if ("1".equals(SystemProperties.get("ro.build.g_test", "0"))) {
            z = true;
        } else {
            z = "1".equals(SystemProperties.get("ro.build.aia", "0"));
        }
        DEBUG_CTS_TEST_23 = z;
    }

    public VivoPermissionConfig(VivoPermissionService vps, Context context) {
        this.mVPS = vps;
        this.mContext = context;
        this.mPermissionList = new SparseArray(30);
        for (int index = 0; index < 30; index++) {
            this.mPermissionList.put(index, new ArrayList());
        }
        this.mTrustedAppList = new ArrayList();
        this.mMonitorAppList = new ArrayList();
        this.mPermissionMap = new HashMap();
        this.mRemovedAppList = new ArrayList();
        this.mBuiltInThirdPartMap = new HashMap();
        this.mAppWhiteList = new ArrayList();
        this.mAppWhiteList.add("com.vivo.PCTools");
        if (!"0".equals(GN_SUPPORT)) {
            List<String> emmApps = VivoCustomUtils.getCustomizedApps(2);
            if (emmApps != null && emmApps.size() > 0) {
                for (String app : emmApps) {
                    this.mAppWhiteList.add(app);
                }
            }
        }
        this.mPackageManager = this.mContext.getPackageManager();
        this.mAppopsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mVPDB = new VivoPermissionDataBase(context);
        startDefaultPermissionConfigAsync();
    }

    private void startDefaultPermissionConfigAsync() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (VivoPermissionConfig.mVPILock) {
                    VivoPermissionService.printfInfo("Start:startDefaultPermissionConfig");
                    VivoPermissionConfig.this.startDefaultPermissionConfig();
                    VivoPermissionService.printfInfo("Finish:startDefaultPermissionConfig");
                    VivoPermissionService.printfInfo("mDataBaseState=" + VivoPermissionConfig.mDataBaseState);
                    VivoPermissionConfig.this.mIsConfigFinished = true;
                }
            }
        }).start();
    }

    private void startDefaultPermissionConfig() {
        List allVPIs = null;
        try {
            allVPIs = this.mVPDB.findAllVPIs();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        configBuiltInThirdPartMD5();
        if (allVPIs == null || allVPIs.size() == 0) {
            buildDefaultPermissionsDB();
            SystemProperties.set("persist.vivo.perm.init", "true");
            List<VivoPermissionInfo> allNewVPIs = null;
            try {
                allNewVPIs = this.mVPDB.findAllVPIs();
            } catch (SQLiteException e2) {
                e2.printStackTrace();
            }
            configAllPermissions(allNewVPIs);
            return;
        }
        String projectName = VivoPermissionManager.getInstance().getProjectName();
        if (projectName == null || !projectName.equals("PD1421")) {
            VivoPermissionService.printfInfo("not need checkIfMonitorAPKRemoved");
            configAllPermissions(allVPIs);
        } else if (checkIfAPKNoLongerNeedMonitor(allVPIs)) {
            List<VivoPermissionInfo> allVPIs_temp = null;
            try {
                allVPIs_temp = this.mVPDB.findAllVPIs();
            } catch (SQLiteException e22) {
                e22.printStackTrace();
            }
            configAllPermissions(allVPIs_temp);
        } else {
            configAllPermissions(allVPIs);
        }
    }

    private boolean checkIfAPKNoLongerNeedMonitor(List<VivoPermissionInfo> allVPIs) {
        VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor begin");
        boolean hasApkRemove = false;
        if (allVPIs == null || allVPIs.size() == 0) {
            return false;
        }
        int allVPIsSize = allVPIs.size();
        for (int index = 0; index < allVPIsSize; index++) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) allVPIs.get(index);
            if (vpi == null) {
                VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor vpi is null index=" + index + " allVPIsSize=" + allVPIsSize);
            } else if (checkIfAPKNoLongerNeedMonitor(vpi.getPackageName())) {
                VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor true app:" + vpi.getPackageName());
                hasApkRemove = true;
                removeVPIFromDB(vpi.getPackageName(), true);
            }
        }
        VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor end hasApkRemove=" + hasApkRemove);
        return hasApkRemove;
    }

    private boolean checkIfAPKNoLongerNeedMonitor(String packageName) {
        if (packageName == null) {
            return false;
        }
        PackageInfo pi = null;
        long identity = Binder.clearCallingIdentity();
        try {
            pi = this.mPackageManager.getPackageInfo(packageName, IQcRilHook.SERVICE_PROGRAMMING_BASE);
            boolean isMonitorSystemApp = VivoPermissionManager.getInstance().needMonitorSystemApp();
            if ((this.mVPS.needCheckPkg(pi) ^ 1) && !isMonitorSystemApp) {
                VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor apk is move to system app");
                return true;
            } else if (!this.mAppWhiteList.contains(pi.packageName)) {
                return false;
            } else {
                VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor apk is in WhiteList");
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            VivoPermissionService.printfInfo("checkIfAPKNoLongerNeedMonitor cannot find this apk:" + packageName);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void buildDefaultPermissionsDB() {
        List<PackageInfo> piList = this.mPackageManager.getInstalledPackages(IQcRilHook.SERVICE_PROGRAMMING_BASE);
        int size = piList.size();
        for (int index = 0; index < size; index++) {
            VivoPermissionInfo parseVPI = parseDefaultPackagePermission((PackageInfo) piList.get(index), false);
            if (parseVPI != null) {
                saveVPIToDB(parseVPI, false);
            }
        }
    }

    private VivoPermissionInfo parseDefaultPackagePermission(String packageName, boolean grantPermissions) {
        long identity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = this.mPackageManager.getPackageInfo(packageName, IQcRilHook.SERVICE_PROGRAMMING_BASE);
            Binder.restoreCallingIdentity(identity);
            return parseDefaultPackagePermission(pi, grantPermissions);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private VivoPermissionInfo parseDefaultPackagePermission(PackageInfo pi, boolean grantPermissions) {
        if (pi == null) {
            return null;
        }
        VivoPermissionInfo vpi = null;
        boolean isMonitorSystemApp = VivoPermissionManager.getInstance().needMonitorSystemApp();
        boolean isSystemApp = this.mVPS.needCheckPkg(pi) ^ 1;
        if (this.mVPS.isTestApp(pi.packageName)) {
            return null;
        }
        if (this.mAppWhiteList.contains(pi.packageName)) {
            return null;
        }
        try {
            int packageUid = this.mPackageManager.getPackageUid(pi.packageName, 0);
            VivoPermissionService.printfInfo("parseDefaultPackagePermission packageName:" + pi.packageName + " packageUid:" + packageUid);
            if (pi.requestedPermissions == null) {
                VivoPermissionService.printfInfo("isSystemApp:" + isSystemApp + " " + pi.packageName + " requestedPermissions is null uid=" + packageUid);
                return null;
            }
            int index;
            if (isSystemApp || 1000 == packageUid || ProcessList.UNKNOWN_ADJ == packageUid) {
                VivoPermissionService.printfInfo("parseDefaultPackagePermission -------->SystemApp " + pi.packageName + " uid:" + packageUid + " requestedPermissionsNull=" + false);
                int result;
                if (isMonitorSystemApp) {
                    if (1000 == packageUid || ProcessList.UNKNOWN_ADJ == packageUid) {
                        vpi = new VivoPermissionInfo(pi.packageName);
                        for (index = 0; index < 30; index++) {
                            if (VivoPermissionType.getVPType(index).getVPCategory() == VivoPermissionCategory.OTHERS) {
                                vpi.setPermissionResult(index, 1);
                            } else {
                                vpi.setPermissionResult(index, 3);
                                vpi.setDeniedMode(index, 48);
                                vpi.setDeniedDialogMode(index, ProcessStates.PAUSING);
                            }
                        }
                    } else {
                        vpi = new VivoPermissionInfo(pi.packageName);
                        for (String configDefaultPermissionResults : pi.requestedPermissions) {
                            configDefaultPermissionResults(vpi, configDefaultPermissionResults);
                        }
                        for (index = 0; index < 30; index++) {
                            result = vpi.getPermissionResult(index);
                            if (result == 4) {
                                vpi.setPermissionResult(index, 3);
                            }
                            if (result != 0) {
                                vpi.setDeniedMode(index, 48);
                                vpi.setDeniedDialogMode(index, ProcessStates.PAUSING);
                            }
                        }
                    }
                } else if (TextUtils.equals("com.vivo.game", pi.packageName) || TextUtils.equals("com.chaozh.iReader", pi.packageName)) {
                    vpi = new VivoPermissionInfo(pi.packageName);
                    for (String configDefaultPermissionResults2 : pi.requestedPermissions) {
                        configDefaultPermissionResults(vpi, configDefaultPermissionResults2);
                    }
                    for (index = 0; index < 30; index++) {
                        result = vpi.getPermissionResult(index);
                        if (result == 4) {
                            vpi.setPermissionResult(index, 1);
                        }
                        if (result != 0) {
                            vpi.setDeniedMode(index, 32);
                            vpi.setDeniedDialogMode(index, 768);
                        }
                    }
                }
            } else {
                VivoPermissionService.printfInfo("parseDefaultPackagePermission -------->ThirdParty " + pi.packageName + " uid:" + packageUid + " requestedPermissionsNull=" + false);
                vpi = new VivoPermissionInfo(pi.packageName);
                for (String configDefaultPermissionResults22 : pi.requestedPermissions) {
                    configDefaultPermissionResults(vpi, configDefaultPermissionResults22);
                }
                if (needDefaultTrustThirdPartApp(pi)) {
                    vpi.grantAllPermissions();
                    grantAllRuntimePermissions(pi.packageName, true);
                } else {
                    boolean isSpecialPkg = "jp.co.hit_point.tabikaeru.st".equals(pi.packageName);
                    index = 0;
                    while (index < 30) {
                        if (vpi.getPermissionResult(index) == 4) {
                            if (grantPermissions) {
                                vpi.setPermissionResult(index, 1);
                                vpi.setDeniedMode(index, 32);
                                vpi.setDeniedDialogMode(index, 768);
                            } else {
                                vpi.setPermissionResult(index, 3);
                            }
                        }
                        if (isSpecialPkg && index == 22) {
                            doForRuntimePermission(pi.packageName, index, 1);
                        }
                        index++;
                    }
                    if (1 == DEBUG_CTS_TEST) {
                        vpi.grantAllPermissions();
                        VivoPermissionService.printfInfo("DEBUG_TCS_TEST so set all permission GRANTED");
                    }
                    if (VivoPermissionManager.getInstance().isOverSeas()) {
                        vpi.grantAllPermissions();
                    }
                }
            }
            return vpi;
        } catch (Exception e) {
            VivoPermissionService.printfError("fatal error:getPackageUid(" + pi.packageName + ") fail uid = -1");
            e.printStackTrace();
            return null;
        }
    }

    private boolean needDefaultTrustThirdPartApp(PackageInfo pi) {
        if (!VivoPermissionManager.getInstance().needMonitorBuildInApps() && checkBuildInThirdPartApp(pi)) {
            return true;
        }
        return false;
    }

    private boolean checkBuildInThirdPartApp(PackageInfo pi) {
        if (pi == null || pi.applicationInfo == null) {
            return false;
        }
        boolean result = false;
        try {
            String pkgPath = pi.applicationInfo.sourceDir;
            String backupPkgMD5 = (String) this.mBuiltInThirdPartMap.get(pi.packageName);
            if (backupPkgMD5 != null && backupPkgMD5.length() > 0) {
                result = MD5Util.check(new File(pkgPath), backupPkgMD5);
            }
            VivoPermissionService.printfInfo("checkBuildInThirdPartApp-->pkgPath=" + pkgPath + ";result=" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void configDefaultCustomPermissionResults(VivoPermissionInfo vpi, int typeId) {
        int customTypeId = -1;
        if (typeId == VivoPermissionType.CHANGE_NETWORK_STATE.getVPTypeId()) {
            customTypeId = VivoPermissionType.SEND_MMS.getVPTypeId();
        }
        if (customTypeId != -1) {
            vpi.setPermissionResult(customTypeId, 4);
        }
    }

    private void configDefaultPermissionResults(VivoPermissionInfo vpi, String permission) {
        VivoPermissionType type = VivoPermissionType.getVPType(permission);
        int typeId = type.getVPTypeId();
        if (VivoPermissionType.isValidTypeId(typeId)) {
            VivoPermissionService.printfInfo(vpi.getPackageName() + " has permission: " + permission);
            if (type.getVPCategory() == VivoPermissionCategory.OTHERS) {
                vpi.setPermissionResult(typeId, 1);
            } else if (permission.contains(ACCESS_WIFI_STATE_PERMISSION)) {
                VivoPermissionService.printfInfo("configDefaultPermissionResults permission=ACCESS_WIFI_STATE just return");
                return;
            } else {
                vpi.setPermissionResult(typeId, 4);
            }
            configDefaultCustomPermissionResults(vpi, typeId);
        }
    }

    private void saveVPIToDB(final VivoPermissionInfo vpi, boolean isAsync) {
        if (vpi != null) {
            if (isAsync) {
                new Thread(new Runnable() {
                    public void run() {
                        VivoPermissionService.printfDebug("mVPDB is saving vpi :" + vpi.getPackageName());
                        try {
                            VivoPermissionConfig.this.mVPDB.save(vpi);
                        } catch (SQLiteException e) {
                            e.printStackTrace();
                        }
                        VivoPermissionService.printfDebug("mVPDB saved vpi.");
                    }
                }).start();
            } else {
                try {
                    this.mVPDB.save(vpi);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeVPIFromDB(final String packageName, boolean isAsync) {
        if (isAsync) {
            new Thread(new Runnable() {
                public void run() {
                    VivoPermissionService.printfDebug("mVPDB is deleting " + packageName);
                    try {
                        VivoPermissionService.printfDebug("mVPDB deleted result=" + VivoPermissionConfig.this.mVPDB.delete(packageName));
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return;
        }
        try {
            this.mVPDB.delete(packageName);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private void addVPIToSpecPermissionList(VivoPermissionInfo vpi, int type) {
        if (VivoPermissionType.isValidTypeId(type)) {
            List<VivoPermissionInfo> vpiList = (List) this.mPermissionList.get(type);
            boolean hasAdded = false;
            for (int index = vpiList.size() - 1; index >= 0; index--) {
                if (((VivoPermissionInfo) vpiList.get(index)).getPackageName().equals(vpi.getPackageName())) {
                    hasAdded = true;
                    break;
                }
            }
            if (!hasAdded) {
                vpiList.add(vpi);
            }
            return;
        }
        VivoPermissionService.printfError("addAPIToSpecifiedPermPkgList!!! Invalid VivoPermissionType ID!!!");
    }

    private void addVPIToPermissionList(VivoPermissionInfo vpi) {
        if (vpi != null) {
            for (int index = 0; index < 30; index++) {
                if (vpi.getPermissionResult(index) != 0) {
                    addVPIToSpecPermissionList(vpi, index);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addVPIToPermissionMap(VivoPermissionInfo vpi) {
        if (!(vpi == null || vpi.getPackageName() == null || this.mPermissionMap.containsKey(vpi.getPackageName()))) {
            this.mPermissionMap.put(vpi.getPackageName(), vpi);
        }
    }

    private void configAllPermissions(List<VivoPermissionInfo> allVPIs) {
        if (allVPIs != null && allVPIs.size() != 0) {
            int allVPIsSize = allVPIs.size();
            for (int index = 0; index < allVPIsSize; index++) {
                VivoPermissionInfo vpi = (VivoPermissionInfo) allVPIs.get(index);
                if (isNeedDisplay(vpi)) {
                    addVPIToPermissionList(vpi);
                    addVPIToPermissionMap(vpi);
                    if (vpi.isWhiteListApp()) {
                        this.mTrustedAppList.add(vpi);
                    } else {
                        this.mMonitorAppList.add(vpi);
                    }
                }
            }
        }
    }

    protected void configBuiltInThirdPartMD5() {
        File apkFile;
        File[] childFiles;
        PackageLite pkg;
        String[] builtInThirdPartFiles = mBuiltInThirdPartDir.list();
        if (builtInThirdPartFiles != null) {
            for (String file : builtInThirdPartFiles) {
                apkFile = new File(mBuiltInThirdPartDir, file);
                if (apkFile.isDirectory()) {
                    childFiles = apkFile.listFiles();
                    if (ArrayUtils.isEmpty(childFiles)) {
                    } else {
                        apkFile = childFiles[0];
                    }
                }
                if (apkFile.getPath().endsWith(".apk")) {
                    try {
                        this.mBuiltInThirdPartMap.put(PackageParser.parsePackageLite(apkFile, 0).packageName, MD5Util.getFileMD5String(apkFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String[] builtInThirdPartVivoFile = mBuiltInThirdPartVivoDir.list();
        if (builtInThirdPartVivoFile != null) {
            Slog.d(TAG, "builtInThirdPartVivoFile is not null");
            for (String file2 : builtInThirdPartVivoFile) {
                apkFile = new File(mBuiltInThirdPartVivoDir, file2);
                if (apkFile.isDirectory()) {
                    childFiles = apkFile.listFiles();
                    if (ArrayUtils.isEmpty(childFiles)) {
                    } else {
                        apkFile = childFiles[0];
                    }
                }
                if (apkFile.getPath().endsWith(".apk")) {
                    try {
                        pkg = PackageParser.parsePackageLite(apkFile, 0);
                        if (((String) this.mBuiltInThirdPartMap.get(pkg.packageName)) != null) {
                            Slog.d(TAG, "pkg " + pkg.packageName + " already exists");
                        } else {
                            Slog.d(TAG, "add pkg " + pkg.packageName);
                            this.mBuiltInThirdPartMap.put(pkg.packageName, MD5Util.getFileMD5String(apkFile));
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } else {
            Slog.d(TAG, "builtInThirdPartVivoFile is null skip it");
        }
        String[] builtInThirdPartDataFile = mBuiltInThirdPartDataDir.list();
        if (builtInThirdPartDataFile != null) {
            Slog.d(TAG, "builtInThirdPartDataFile is not null");
            for (String file22 : builtInThirdPartDataFile) {
                apkFile = new File(mBuiltInThirdPartDataDir, file22);
                if (apkFile.isDirectory()) {
                    childFiles = apkFile.listFiles();
                    if (ArrayUtils.isEmpty(childFiles)) {
                    } else {
                        for (File childFile : childFiles) {
                            if (isApkPath(childFile)) {
                                apkFile = childFile;
                            }
                        }
                    }
                }
                if (isApkPath(apkFile)) {
                    try {
                        pkg = PackageParser.parsePackageLite(apkFile, 0);
                        if (((String) this.mBuiltInThirdPartMap.get(pkg.packageName)) != null) {
                            Slog.d(TAG, "pkg " + pkg.packageName + " already exists");
                        } else {
                            Slog.d(TAG, "add pkg " + pkg.packageName);
                            this.mBuiltInThirdPartMap.put(pkg.packageName, MD5Util.getFileMD5String(apkFile));
                        }
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
            }
            return;
        }
        Slog.d(TAG, "builtInThirdPartDataFile is null skip it");
    }

    private boolean isApkPath(File file) {
        if (file == null || file.getPath() == null || !file.getPath().endsWith(".apk")) {
            return false;
        }
        return true;
    }

    protected boolean isNeedDisplay(VivoPermissionInfo vpi) {
        if (vpi == null) {
            return false;
        }
        for (int index = 0; index < 30; index++) {
            VivoPermissionCategory vpc = VivoPermissionType.getVPType(index).getVPCategory();
            if (vpi.getPermissionResult(index) != 0 && vpc != VivoPermissionCategory.OTHERS) {
                return true;
            }
        }
        return false;
    }

    public List<VivoPermissionInfo> getSpecifiedPermAppList(int vpTypeId) {
        if (VivoPermissionType.isValidTypeId(vpTypeId)) {
            List<VivoPermissionInfo> list;
            synchronized (mVPILock) {
                handleVpiList((List) this.mPermissionList.get(vpTypeId), true);
                list = (List) this.mPermissionList.get(vpTypeId);
            }
            return list;
        }
        VivoPermissionService.printfError("getSpecifiedPermPkgList:failed!!! Invalid VivoPermissionType ID!!!");
        return null;
    }

    public List<VivoPermissionInfo> getTrustedAppList() {
        List<VivoPermissionInfo> list;
        synchronized (mVPILock) {
            list = this.mTrustedAppList;
        }
        return list;
    }

    public List<VivoPermissionInfo> getMonitorAppList() {
        List<VivoPermissionInfo> list;
        synchronized (mVPILock) {
            handleVpiList(this.mMonitorAppList, false);
            list = this.mMonitorAppList;
        }
        return list;
    }

    public Map<String, VivoPermissionInfo> getPermissionMap() {
        if (this.mIsConfigFinished) {
            return this.mPermissionMap;
        }
        VivoPermissionService.printfInfo("getPermissionMap mIsConfigFinished=false, return null!");
        return null;
    }

    public void handleRuntimePermission(String packageName, boolean nedfixed) {
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo != null && packageInfo.applicationInfo != null && packageInfo.requestedPermissions != null) {
            boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
            VivoPermissionService.printfDebug("handleRuntimePermission supportsRuntimePermissions=" + supportsRuntimePermissions);
            if (supportsRuntimePermissions) {
                int permissionCount = packageInfo.requestedPermissions.length;
                for (int i = 0; i < permissionCount; i++) {
                    String requestedPermission = packageInfo.requestedPermissions[i];
                    VivoPermissionService.printfDebug("handleRuntimePermission requestedPermission=" + requestedPermission);
                    if (isRuntimePermission(requestedPermission)) {
                        int typeId = VivoPermissionType.getVPType(requestedPermission).getVPTypeId();
                        VivoPermissionService.printfDebug("handleRuntimePermission  typeId=" + typeId);
                        if (VivoPermissionType.isValidTypeId(typeId)) {
                            boolean granted = (packageInfo.requestedPermissionsFlags[i] & 2) != 0;
                            boolean isFixed = false;
                            if (nedfixed) {
                                isFixed = (this.mPackageManager.getPermissionFlags(requestedPermission, packageName, new UserHandle(0)) & 2) != 0;
                            }
                            int result = granted ? 1 : isFixed ? 2 : 3;
                            setVivoPermissionInfo(packageName, typeId, result);
                        }
                    } else {
                        VivoPermissionService.printfDebug("handleRuntimePermission  isRuntimePermission = nonono");
                    }
                }
            }
        }
    }

    public VivoPermissionInfo getAppPermission(String packageName) {
        VivoPermissionInfo vpi = null;
        synchronized (mVPILock) {
            if (this.mPermissionMap.containsKey(packageName)) {
                vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            } else {
                updateForPackageAdded_l(packageName, false);
                if (this.mPermissionMap.containsKey(packageName)) {
                    vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
                } else {
                    VivoPermissionService.printfInfo("getAppPermission(" + packageName + ") is null !");
                }
            }
        }
        return vpi;
    }

    private void updateTrustedAndMonitorAppList_l(VivoPermissionInfo vpi) {
        if (vpi != null) {
            if (vpi.isWhiteListApp()) {
                if (this.mMonitorAppList.contains(vpi)) {
                    this.mMonitorAppList.remove(vpi);
                }
                if (!this.mTrustedAppList.contains(vpi)) {
                    this.mTrustedAppList.add(0, vpi);
                }
            } else {
                if (this.mTrustedAppList.contains(vpi)) {
                    this.mTrustedAppList.remove(vpi);
                }
                if (!this.mMonitorAppList.contains(vpi)) {
                    this.mMonitorAppList.add(0, vpi);
                }
            }
        }
    }

    public void setWhiteListApp(String packageName, boolean enable) {
        synchronized (mVPILock) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi == null) {
                return;
            }
            vpi.setWhiteListApp(enable);
            updateTrustedAndMonitorAppList_l(vpi);
            VivoPermissionService.printfDebug("setWhiteListApp-->start saveVPIToDB");
            saveVPIToDB(vpi, true);
        }
    }

    public void setBlackListApp(String packageName, boolean enable) {
        synchronized (mVPILock) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi == null) {
                return;
            }
            vpi.setBlackListApp(enable);
            updateTrustedAndMonitorAppList_l(vpi);
            VivoPermissionService.printfDebug("setBlackListApp-->start saveVPIToDB");
            saveVPIToDB(vpi, true);
        }
    }

    public boolean isBuildInThirdPartApp(String packageName) {
        if (packageName == null) {
            return false;
        }
        PackageInfo pi = null;
        long identity = Binder.clearCallingIdentity();
        try {
            pi = this.mPackageManager.getPackageInfo(packageName, IQcRilHook.SERVICE_PROGRAMMING_BASE);
            return checkBuildInThirdPartApp(pi);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            VivoPermissionService.printfInfo("isBuildInThirdPartApp cannot find this apk:" + packageName);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getDataBaseState() {
        return mDataBaseState;
    }

    public static void setDataBaseState(int state) {
        mDataBaseState = state;
    }

    private VivoPermissionInfo setVivoPermissionInfo(String packageName, int vpTypeId, int result) {
        synchronized (mVPILock) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi == null) {
                return vpi;
            }
            vpi.setPermissionResult(vpTypeId, result);
            if (VivoPermissionManager.getInstance().getOSVersion() >= 3.0f && result != 1 && vpi.isWhiteListApp()) {
                vpi.setWhiteListApp(false);
            }
            updateTrustedAndMonitorAppList_l(vpi);
            return vpi;
        }
    }

    public void saveAppPermission(String packageName, int vpTypeId, int result) {
        VivoPermissionInfo vpi = setVivoPermissionInfo(packageName, vpTypeId, result);
        if (vpi != null) {
            saveVPIToDB(vpi, true);
        }
    }

    public void saveAppPermission(VivoPermissionInfo paramVPI) {
        if (paramVPI != null && paramVPI.getPackageName() != null) {
            synchronized (mVPILock) {
                VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(paramVPI.getPackageName());
                if (vpi == null) {
                    return;
                }
                vpi.copyFrom(paramVPI);
                updateTrustedAndMonitorAppList_l(vpi);
                VivoPermissionService.printfInfo("saveAppPermission all(" + vpi.getPackageName() + ")-->start saveVPIToDB");
                saveVPIToDB(vpi, true);
            }
        }
    }

    private void updatePackagePermission(VivoPermissionInfo oldVpi, VivoPermissionInfo newVpi) {
        if (oldVpi != null && newVpi != null) {
            String OldPkg = oldVpi.getPackageName();
            if (OldPkg != null && (OldPkg.equals(newVpi.getPackageName()) ^ 1) == 0) {
                newVpi.updateFrom(oldVpi);
            }
        }
    }

    private void updateForPackageReplaced_l(String packageName) {
        if (packageName != null) {
            int totalRemovedAppNum = this.mRemovedAppList.size();
            int replacedIndex = -1;
            for (int index = 0; index < totalRemovedAppNum; index++) {
                if (packageName.equals(((VivoPermissionInfo) this.mRemovedAppList.get(index)).getPackageName())) {
                    replacedIndex = index;
                    break;
                }
            }
            if (-1 == replacedIndex) {
                VivoPermissionService.printfInfo("updateForPackageReplaced_l but mRemovedAppList not have this package:" + packageName);
                return;
            }
            VivoPermissionInfo vpiReplaced = (VivoPermissionInfo) this.mRemovedAppList.get(replacedIndex);
            this.mRemovedAppList.remove(replacedIndex);
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi != null) {
                removePackageFromList(packageName);
                updatePackagePermission(vpiReplaced, vpi);
                addPackageToList(vpi);
                VivoPermissionService.printfInfo("updateForPackageReplaced_l end (" + packageName + ")-->start saveVPIToDB");
                saveVPIToDB(vpi, true);
            }
        }
    }

    public void updateForPackageReplaced(String packageName) {
        synchronized (mVPILock) {
            updateForPackageReplaced_l(packageName);
        }
    }

    private void addPackageToList(VivoPermissionInfo vpi) {
        if (vpi != null) {
            addVPIToPermissionList(vpi);
            addVPIToPermissionMap(vpi);
            if (vpi.isWhiteListApp()) {
                this.mTrustedAppList.add(0, vpi);
            } else {
                this.mMonitorAppList.add(0, vpi);
            }
        }
    }

    private void updateForPackageAdded_l(String packageName, boolean grantPermissions) {
        if (this.mAppWhiteList.contains(packageName)) {
            VivoPermissionService.printfDebug("updateForPackageAdded(" + packageName + ")" + "WhiteList");
        } else if (this.mPermissionMap.containsKey(packageName)) {
            VivoPermissionService.printfDebug("updateForPackageAdded(" + packageName + ") is already in mPermissionMap,skip it");
        } else {
            VivoPermissionInfo vpi = parseDefaultPackagePermission(packageName, grantPermissions);
            if (vpi == null) {
                VivoPermissionService.printfInfo("updateForPackageAdded_ls failed! " + packageName);
            } else if (isNeedDisplay(vpi)) {
                addPackageToList(vpi);
                VivoPermissionService.printfInfo("updateForPackageAdded(" + packageName + ")-->start saveVPIToDB");
                saveVPIToDB(vpi, true);
            }
        }
    }

    public void updateForPackageAdded(String packageName, boolean grantPermissions) {
        synchronized (mVPILock) {
            updateForPackageAdded_l(packageName, grantPermissions);
        }
    }

    private void removePackageFromList(String packageName) {
        if (packageName != null) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi != null) {
                this.mPermissionMap.remove(packageName);
                for (int index = 0; index < 30; index++) {
                    List<VivoPermissionInfo> vpiList = (List) this.mPermissionList.get(index);
                    if (vpiList.contains(vpi)) {
                        vpiList.remove(vpi);
                    }
                }
                if (vpi.isWhiteListApp()) {
                    this.mTrustedAppList.remove(vpi);
                } else {
                    this.mMonitorAppList.remove(vpi);
                }
            }
        }
    }

    public void updateForPackageRemoved(String packageName) {
        synchronized (mVPILock) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi == null) {
                return;
            }
            if (this.mRemovedAppList.size() >= 10) {
                for (int index = 9; index >= 5; index--) {
                    this.mRemovedAppList.remove(index);
                }
            }
            this.mRemovedAppList.add(0, vpi);
            removePackageFromList(packageName);
            VivoPermissionService.printfInfo("updateForPackageRemoved(" + packageName + ")-->start removeVPIFromDB");
            removeVPIFromDB(packageName, true);
        }
    }

    public int checkConfigPermission(String packageName, String permName) {
        if (!this.mIsConfigFinished) {
            VivoPermissionService.printfInfo("mIsConfigFinished=false, just GRANTED!");
            return 1;
        } else if (this.mAppWhiteList.contains(packageName)) {
            return 1;
        } else {
            int typeId = VivoPermissionType.getVPType(permName).getVPTypeId();
            int result = 0;
            VivoPermissionInfo vpi = getAppPermission(packageName);
            if (vpi != null) {
                result = vpi.getPermissionResult(typeId);
                if (result == 2 && permName.contains(ACCESS_WIFI_STATE_PERMISSION) && packageName.toLowerCase().contains("wifi")) {
                    result = 1;
                }
                if (typeId == 19 || typeId == 18) {
                    if (DEBUG_CTS_TEST_23) {
                        result = 1;
                    } else {
                        result = 3;
                    }
                }
            } else {
                int packageUid = -1;
                try {
                    packageUid = this.mPackageManager.getPackageUid(packageName, 0);
                } catch (Exception e) {
                    VivoPermissionService.printfInfo("getPackageUid(" + packageName + ") fail uid = -1");
                    e.printStackTrace();
                }
                VivoPermissionService.printfInfo("checkConfigPermission(" + packageName + "," + permName + ") is VivoPermissionInfo.UNKNOWN" + " uid =" + packageUid);
            }
            return result;
        }
    }

    public int checkConfigDeniedMode(String packageName, String permName) {
        int typeId = VivoPermissionType.getVPType(permName).getVPTypeId();
        VivoPermissionInfo vpi = getAppPermission(packageName);
        if (vpi != null) {
            return vpi.getDeniedMode(typeId);
        }
        int packageUid = -1;
        try {
            packageUid = this.mPackageManager.getPackageUid(packageName, 0);
        } catch (Exception e) {
            VivoPermissionService.printfInfo("getPackageUid(" + packageName + ") fail uid = -1");
            e.printStackTrace();
        }
        VivoPermissionService.printfInfo("checkConfigDeniedMode(" + packageName + "," + permName + ") is VivoPermissionInfo.ZERO_TIMES" + " uid =" + packageUid);
        return 32;
    }

    public void setConfigDeniedMode(String packageName, String permName, int deniedMode) {
        int typeId = VivoPermissionType.getVPType(permName).getVPTypeId();
        synchronized (mVPILock) {
            VivoPermissionInfo vpi = (VivoPermissionInfo) this.mPermissionMap.get(packageName);
            if (vpi == null) {
                return;
            }
            vpi.setDeniedMode(typeId, deniedMode);
            saveVPIToDB(vpi, true);
        }
    }

    public int checkConfigDeniedDialogMode(String packageName, String permName) {
        int typeId = VivoPermissionType.getVPType(permName).getVPTypeId();
        VivoPermissionInfo vpi = getAppPermission(packageName);
        if (vpi != null) {
            return vpi.getDeniedDialogMode(typeId);
        }
        int packageUid = -1;
        try {
            packageUid = this.mPackageManager.getPackageUid(packageName, 0);
        } catch (Exception e) {
            VivoPermissionService.printfInfo("getPackageUid(" + packageName + ") fail uid = -1");
            e.printStackTrace();
        }
        VivoPermissionService.printfInfo("checkConfigDeniedDialogMode(" + packageName + "," + permName + ") is DENIED_DIALOG_MODE_NO_COUNTDOWN_SETTING" + " uid =" + packageUid);
        return ProcessStates.HASNOTIFICATION;
    }

    public void doForUpdate() {
        boolean needDo = "false".equals(SystemProperties.get("persist.vivo.perm.init", "false"));
        VivoPermissionService.printfDebug("do for update + " + needDo);
        if (needDo) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        List<VivoPermissionInfo> allVPIs = VivoPermissionConfig.this.mVPDB.findAllVPIs();
                        if (allVPIs != null) {
                            for (VivoPermissionInfo vpi : allVPIs) {
                                VivoPermissionConfig.this.setNewAppPermission(vpi);
                            }
                            SystemProperties.set("persist.vivo.perm.init", "true");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void setNewAppPermission(VivoPermissionInfo vpi) {
        String pkgName = vpi.getPackageName();
        if (pkgName != null) {
            PackageInfo packageInfo = getPackageInfo(pkgName);
            if (packageInfo != null && packageInfo.applicationInfo != null && packageInfo.requestedPermissions != null) {
                boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
                if (this.mVPS.needCheckPkg(packageInfo)) {
                    for (int index = 0; index < 30; index++) {
                        VivoPermissionType type = VivoPermissionType.getVPType(index);
                        if (!(type.getVPCategory() == VivoPermissionCategory.OTHERS || type.getVPGroup() == VivoPermissionGroup.CUSTOM)) {
                            setNewAppPermission(packageInfo, index, vpi.getAllPermission(index), supportsRuntimePermissions);
                        }
                    }
                }
            }
        }
    }

    public boolean isRuntimePermission(String permission) {
        if (permission != null) {
            PermissionInfo info = getPermissionInfo(permission);
            if (info != null && info.packageName != null && PLATFORM_PACKAGE_NAME.equals(info.packageName) && (info.protectionLevel & 15) == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isRuntimePermission(PermissionInfo info) {
        if (info == null || info.packageName == null || !PLATFORM_PACKAGE_NAME.equals(info.packageName) || (info.protectionLevel & 15) != 1) {
            return false;
        }
        return true;
    }

    public PermissionInfo getPermissionInfo(String permission) {
        if (permission != null) {
            try {
                return this.mPackageManager.getPermissionInfo(permission, 0);
            } catch (NameNotFoundException e) {
            }
        }
        return null;
    }

    public PackageInfo getPackageInfo(String packageName) {
        try {
            return this.mPackageManager.getPackageInfo(packageName, IQcRilHook.SERVICE_PROGRAMMING_BASE);
        } catch (NameNotFoundException e) {
            VivoPermissionService.printfError("No package: " + packageName + "  error = " + e);
            return null;
        }
    }

    public boolean setOnePermission(String packageName, String perm, int uid, boolean granted) {
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo == null || packageInfo.applicationInfo == null || (isRuntimePermission(perm) ^ 1) != 0) {
            return false;
        }
        if (!(packageInfo.applicationInfo.targetSdkVersion > 22)) {
            int typeId = VivoPermissionType.getVPType(perm).getVPTypeId();
            String appOp = AppOpsManager.permissionToOp(perm);
            int uidMode = granted ? 0 : 1;
            if (VivoPermissionType.isValidTypeId(typeId) && VivoPermissionType.getVPType(perm).getVPCategory() != VivoPermissionCategory.OTHERS) {
                saveAppPermission(packageName, typeId, granted ? 1 : 3);
                uidMode = 0;
            }
            if (this.mAppopsManager == null || appOp == null) {
                return false;
            }
            this.mAppopsManager.setUidMode(appOp, uid, uidMode);
        }
        return true;
    }

    public int checkOnePermission(String packageName, String perm, int uid) {
        VivoPermissionService.printfDebug("checkOnePermission  packageName=" + packageName + " perm=" + perm + " uid=" + uid);
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo == null || packageInfo.applicationInfo == null || packageInfo.requestedPermissions == null || (isRuntimePermission(perm) ^ 1) != 0) {
            return 1;
        }
        boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
        VivoPermissionService.printfDebug("checkOnePermission  supportsRuntimePermissions=" + supportsRuntimePermissions);
        if (supportsRuntimePermissions) {
            int permissionCount = packageInfo.requestedPermissions.length;
            for (int i = 0; i < permissionCount; i++) {
                String requestedPermission = packageInfo.requestedPermissions[i];
                if (requestedPermission.equals(perm)) {
                    boolean granted = (packageInfo.requestedPermissionsFlags[i] & 2) != 0;
                    int permissionFlags = this.mPackageManager.getPermissionFlags(requestedPermission, packageName, UserHandle.getUserHandleForUid(uid));
                    boolean isFixed = (permissionFlags & 2) != 0;
                    VivoPermissionService.printfDebug("checkOnePermission  granted=" + granted + " permissionFlags=" + permissionFlags + " isFixed=" + isFixed);
                    int i2 = granted ? 1 : isFixed ? 2 : 3;
                    return i2;
                }
            }
            return 1;
        }
        int typeId = VivoPermissionType.getVPType(perm).getVPTypeId();
        VivoPermissionService.printfDebug("checkOnePermission typeId=" + typeId);
        if (!this.mVPS.isTestApp(packageName) && VivoPermissionType.isValidTypeId(typeId) && VivoPermissionType.getVPType(perm).getVPCategory() != VivoPermissionCategory.OTHERS) {
            return checkConfigPermission(packageName, perm);
        }
        String appOp = AppOpsManager.permissionToOp(perm);
        VivoPermissionService.printfDebug("checkOnePermission appOp=" + appOp);
        boolean appOpAllowed = false;
        if (!(this.mAppopsManager == null || appOp == null)) {
            appOpAllowed = this.mAppopsManager.checkOpNoThrow(appOp, packageInfo.applicationInfo.uid, packageInfo.packageName) == 0;
        }
        return appOpAllowed ? 1 : 3;
    }

    public boolean doForRuntimePermission(String packageName, int vpTypeId, int result) {
        if (packageName != null) {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (!(packageInfo == null || packageInfo.applicationInfo == null || packageInfo.requestedPermissions == null)) {
                boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
                VivoPermissionService.printfDebug("doForRuntimePermission packageName=" + packageInfo.packageName + " supportsRuntimePermissions=" + supportsRuntimePermissions);
                return setNewAppPermission(packageInfo, vpTypeId, result, supportsRuntimePermissions);
            }
        }
        return false;
    }

    public void handleGroupPermission(String packageName, VivoPermissionGroup vpg, int result) {
        if (packageName != null && vpg != VivoPermissionGroup.CUSTOM) {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (packageInfo != null && packageInfo.applicationInfo != null && packageInfo.requestedPermissions != null) {
                boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
                VivoPermissionService.printfDebug("handleGroupPermission packageName=" + packageInfo.packageName + " supportsRuntimePermissions=" + supportsRuntimePermissions);
                int permissionCount = packageInfo.requestedPermissions.length;
                for (int i = 0; i < permissionCount; i++) {
                    String requestedPermission = packageInfo.requestedPermissions[i];
                    if (VivoPermissionService.needHandleGroup(requestedPermission)) {
                        VivoPermissionService.printfDebug("handleGroupPermission  requestedPermission =" + requestedPermission + " vpg=" + vpg);
                        PermissionInfo info = getPermissionInfo(requestedPermission);
                        if (!(info == null || info.group == null || !vpg.getValue().equals(info.group))) {
                            VivoPermissionService.printfDebug("handleGroupPermission group = " + info.group + "  requestedPermission =" + requestedPermission);
                            if (supportsRuntimePermissions) {
                                setRuntimePermission(packageInfo, packageInfo.requestedPermissionsFlags[i], requestedPermission, result);
                            } else {
                                boolean granted = result == 1;
                                String appOp = AppOpsManager.permissionToOp(requestedPermission);
                                if (!(this.mAppopsManager == null || appOp == null)) {
                                    int i2;
                                    AppOpsManager appOpsManager = this.mAppopsManager;
                                    int i3 = packageInfo.applicationInfo.uid;
                                    if (granted) {
                                        i2 = 0;
                                    } else {
                                        i2 = 1;
                                    }
                                    appOpsManager.setUidMode(appOp, i3, i2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean setNewAppPermission(PackageInfo packageInfo, int vpTypeId, int result, boolean supportRT) {
        boolean isDone = false;
        VivoPermissionGroup vpg = VivoPermissionType.getVPType(vpTypeId).getVPGroup();
        VivoPermissionService.printfDebug("setNewAppPermission packageName=" + packageInfo.packageName + " vpTypeId=" + vpTypeId + " vpg=" + vpg + " result=" + result);
        int permissionCount = packageInfo.requestedPermissions.length;
        for (int i = 0; i < permissionCount; i++) {
            String requestedPermission = packageInfo.requestedPermissions[i];
            PermissionInfo permInfo = getPermissionInfo(requestedPermission);
            VivoPermissionService.printfDebug("setNewAppPermission requestedPermission=" + requestedPermission + " permInfo=" + permInfo);
            if (isRuntimePermission(permInfo)) {
                int typeId = VivoPermissionType.getVPType(requestedPermission).getVPTypeId();
                VivoPermissionService.printfDebug("setNewAppPermission typeId = " + typeId + " permInfo.group=" + permInfo.group + " supportRT=" + supportRT);
                if (VivoPermissionService.needHandleGroup(requestedPermission) && vpg.getValue().equals(permInfo.group)) {
                    if (supportRT) {
                        setRuntimePermission(packageInfo, packageInfo.requestedPermissionsFlags[i], requestedPermission, result);
                    } else {
                        setOldAppPermission(packageInfo, requestedPermission, (result & 15) == 1, true);
                    }
                } else if (vpTypeId == typeId) {
                    if (supportRT) {
                        setRuntimePermission(packageInfo, packageInfo.requestedPermissionsFlags[i], requestedPermission, result);
                        isDone = true;
                    } else {
                        setOldAppPermission(packageInfo, requestedPermission, (result & 15) == 1, false);
                    }
                }
            }
        }
        return isDone;
    }

    private void setRuntimePermission(PackageInfo packageInfo, int requestedPermissionsFlag, String requestedPermission, int result) {
        int uid = packageInfo.applicationInfo.uid;
        int permissionFlag = this.mPackageManager.getPermissionFlags(requestedPermission, packageInfo.packageName, UserHandle.getUserHandleForUid(uid));
        boolean granted = (requestedPermissionsFlag & 2) != 0;
        if ((permissionFlag & 16) == 0 && (permissionFlag & 4) == 0) {
            boolean isFixed = (permissionFlag & 2) != 0;
            boolean isSet = (permissionFlag & 1) != 0;
            VivoPermissionService.printfDebug("setRuntimePermission isFixed=" + isFixed + " isSet=" + isSet);
            if ((result & 15) == 1) {
                String appOp = AppOpsManager.permissionToOp(requestedPermission);
                if (!(this.mAppopsManager == null || appOp == null)) {
                    this.mAppopsManager.setUidMode(appOp, uid, 0);
                }
                if (!granted) {
                    this.mPackageManager.grantRuntimePermission(packageInfo.packageName, requestedPermission, UserHandle.getUserHandleForUid(uid));
                }
                if (isFixed || isSet) {
                    this.mPackageManager.updatePermissionFlags(requestedPermission, packageInfo.packageName, 3, 0, UserHandle.getUserHandleForUid(uid));
                }
            } else if ((result & 15) == 3) {
                if (granted) {
                    this.mPackageManager.revokeRuntimePermission(packageInfo.packageName, requestedPermission, UserHandle.getUserHandleForUid(uid));
                }
                if (isSet || isFixed) {
                    this.mPackageManager.updatePermissionFlags(requestedPermission, packageInfo.packageName, 3, 0, UserHandle.getUserHandleForUid(uid));
                }
            } else if ((result & 15) == 2) {
                if (granted) {
                    this.mPackageManager.revokeRuntimePermission(packageInfo.packageName, requestedPermission, UserHandle.getUserHandleForUid(uid));
                }
                if (isSet || (isFixed ^ 1) != 0) {
                    this.mPackageManager.updatePermissionFlags(requestedPermission, packageInfo.packageName, 3, 2, UserHandle.getUserHandleForUid(uid));
                }
            }
            return;
        }
        VivoPermissionService.printfDebug("setRuntimePermission permissionFlags fix do nothing");
    }

    public void grantAllRuntimePermissions(String packageName, boolean enable) {
        if (packageName != null && enable) {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if (packageInfo != null && packageInfo.applicationInfo != null && packageInfo.requestedPermissions != null) {
                boolean supportsRuntimePermissions = packageInfo.applicationInfo.targetSdkVersion > 22;
                VivoPermissionService.printfDebug("grantAllRuntimePermissions packageName=" + packageInfo.packageName + " supportsRuntimePermissions=" + supportsRuntimePermissions);
                if (supportsRuntimePermissions) {
                    int permissionCount = packageInfo.requestedPermissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        String requestedPermission = packageInfo.requestedPermissions[i];
                        if (isRuntimePermission(requestedPermission)) {
                            setRuntimePermission(packageInfo, packageInfo.requestedPermissionsFlags[i], requestedPermission, 1);
                        }
                    }
                }
            }
        }
    }

    public void setOldAppPermission(PackageInfo packageInfo, String perm, boolean grant, boolean nedGrant) {
        String appOp = AppOpsManager.permissionToOp(perm);
        VivoPermissionService.printfDebug("setOldAppPermission appOp=" + appOp + " grant = " + grant);
        if (this.mAppopsManager != null && appOp != null) {
            int uid = packageInfo.applicationInfo.uid;
            boolean shouldRevokeOnUpgrade = (this.mPackageManager.getPermissionFlags(perm, packageInfo.packageName, UserHandle.getUserHandleForUid(uid)) & 8) != 0;
            boolean appOpAllowed = this.mAppopsManager.checkOpNoThrow(appOp, uid, packageInfo.packageName) == 0;
            if (grant) {
                if (!appOpAllowed) {
                    this.mAppopsManager.setUidMode(appOp, uid, 0);
                }
                if (shouldRevokeOnUpgrade) {
                    this.mPackageManager.updatePermissionFlags(perm, packageInfo.packageName, 8, 0, UserHandle.getUserHandleForUid(uid));
                    return;
                }
                return;
            }
            if (appOpAllowed && nedGrant) {
                this.mAppopsManager.setUidMode(appOp, uid, 1);
            }
            if (!shouldRevokeOnUpgrade) {
                this.mPackageManager.updatePermissionFlags(perm, packageInfo.packageName, 8, 8, UserHandle.getUserHandleForUid(uid));
            }
        }
    }

    private void handleVpiList(List<VivoPermissionInfo> vpis, boolean nedfixed) {
        if (vpis != null) {
            for (VivoPermissionInfo vpi : vpis) {
                long identity = Binder.clearCallingIdentity();
                try {
                    handleRuntimePermission(vpi.getPackageName(), nedfixed);
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        }
    }
}
