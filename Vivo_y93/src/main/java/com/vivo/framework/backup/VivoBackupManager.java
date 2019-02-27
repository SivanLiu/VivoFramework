package com.vivo.framework.backup;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.vivo.common.provider.Weather;
import java.util.Random;
import vivo.app.backup.AbsVivoBackupManager;
import vivo.app.backup.IPackageBackupRestoreObserver;
import vivo.app.backup.IVivoBackupManager;
import vivo.app.backup.IVivoBackupManager.Stub;

public class VivoBackupManager extends AbsVivoBackupManager {
    private static final String TAG = "VivoBackupManager";
    private static VivoBackupManager sInstance = null;
    private static IVivoBackupManager sService = null;

    private VivoBackupManager() {
        sService = getService();
    }

    public static VivoBackupManager getInstance() {
        if (sInstance == null) {
            sInstance = new VivoBackupManager();
        }
        return sInstance;
    }

    private static IVivoBackupManager getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("vivo_backup_service");
        if (b == null) {
            return null;
        }
        sService = Stub.asInterface(b);
        return sService;
    }

    public boolean backupPackage(String pkgName, ParcelFileDescriptor fd, IPackageBackupRestoreObserver observer) {
        return backupPackage(pkgName, fd, observer, false, false, false, false, false, false);
    }

    public boolean backupPackage(String pkgName, ParcelFileDescriptor fd, IPackageBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean compress, boolean doKeyValue) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "backupPackages, Service == null");
            return false;
        }
        try {
            return service.backupPackage(pkgName, fd, observer, includeApks, includeObbs, includeShared, doWidgets, false, false, compress, doKeyValue);
        } catch (RemoteException e) {
            Log.e(TAG, "backupPackages couldn't connect!");
            return false;
        }
    }

    public boolean restoreBackupFile(ParcelFileDescriptor fd, String[] cmdList, IPackageBackupRestoreObserver observer) {
        return restoreBackupFile("RandomPkg" + new Random().nextInt(Weather.WEATHERVERSION_ROM_2_0), fd, cmdList, observer);
    }

    public boolean restoreBackupFile(String pkgName, ParcelFileDescriptor fd, String[] cmdList, IPackageBackupRestoreObserver observer) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "restoreBackupFile, Service == null");
            return false;
        }
        try {
            return service.restoreBackupFile(pkgName, fd, cmdList, observer);
        } catch (RemoteException e) {
            Log.e(TAG, "restoreBackupFile couldn't connect!");
            return false;
        }
    }

    public boolean backupPackageByZip(String pkgName, ParcelFileDescriptor writeFile, boolean enableDual, String[] dirs, IPackageBackupRestoreObserver observer) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "backupPackageByZip, Service == null");
            return false;
        }
        try {
            return service.backupPackageByZip(pkgName, writeFile, enableDual, dirs, observer);
        } catch (RemoteException e) {
            Log.e(TAG, "backupPackageByZip couldn't connect!");
            return false;
        }
    }

    public boolean restorePackageByZip(String pkgName, ParcelFileDescriptor readFile, boolean enableDual, String[] cmdList, IPackageBackupRestoreObserver observer) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "restorePackageByZip, Service == null");
            return false;
        }
        try {
            return service.restorePackageByZip(pkgName, readFile, enableDual, cmdList, observer);
        } catch (RemoteException e) {
            Log.e(TAG, "restorePackageByZip couldn't connect!");
            return false;
        }
    }

    public boolean checkSupportFeature(int feature) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "checkSupportFeature, Service == null");
            return false;
        }
        try {
            return service.checkSupportFeature(feature);
        } catch (RemoteException e) {
            Log.e(TAG, "checkSupportFeature couldn't connect!");
            return false;
        }
    }

    public boolean isRunningFromVivoBackup(int fd) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "isRunningFromVivoBackup, Service == null");
            return false;
        }
        try {
            return service.isRunningFromVivoBackup(fd);
        } catch (RemoteException e) {
            Log.e(TAG, "isRunningFromVivoBackup couldn't connect!");
            return false;
        }
    }

    public boolean isDualPackageEnabled(String pkgName) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "isDualPackageEnabled, Service == null");
            return false;
        }
        try {
            return service.isDualPackageEnabled(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "isDualPackageEnabled couldn't connect!");
            return false;
        }
    }

    public boolean enableDualPackage(String pkgName) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "enableDualPackage, Service == null");
            return false;
        }
        try {
            return service.enableDualPackage(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "enableDualPackage couldn't connect!");
            return false;
        }
    }

    public int getDualUserId() {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "getDualUserId, Service == null");
            return -1;
        }
        try {
            return service.getDualUserId();
        } catch (RemoteException e) {
            Log.e(TAG, "getDualUserId couldn't connect!");
            return -1;
        }
    }

    public boolean startConfirmationUi(int token, String action, int fd) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "startConfirmationUi, Service == null");
            return false;
        }
        try {
            return service.startConfirmationUi(token, action, fd);
        } catch (RemoteException e) {
            Log.e(TAG, "startConfirmationUi couldn't connect!");
            return false;
        }
    }

    public void addBackupCompleteSize(long size, int fd) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "addBackupCompleteSize, Service == null");
            return;
        }
        try {
            service.addBackupCompleteSize(size, fd);
        } catch (RemoteException e) {
            Log.e(TAG, "addBackupCompleteSize couldn't connect!");
        }
    }

    public void postRestoreCompleteSize(long size, int fd) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "postRestoreCompleteSize, Service == null");
            return;
        }
        try {
            service.postRestoreCompleteSize(size, fd);
        } catch (RemoteException e) {
            Log.e(TAG, "postRestoreCompleteSize couldn't connect!");
        }
    }

    public String[] getRestoreCmdList(int fd) {
        IVivoBackupManager service = getService();
        if (service == null) {
            Log.e(TAG, "getRestoreCmdList, Service == null");
            return null;
        }
        try {
            return service.getRestoreCmdList(fd);
        } catch (RemoteException e) {
            Log.e(TAG, "getRestoreCmdList couldn't connect!");
            return null;
        }
    }
}
