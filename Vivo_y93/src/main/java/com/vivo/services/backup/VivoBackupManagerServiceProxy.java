package com.vivo.services.backup;

import android.app.backup.IBackupManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;

public class VivoBackupManagerServiceProxy {
    static void fullBackup(ParcelFileDescriptor writeFile, String[] pkgNames, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, boolean doKeyValue) throws RemoteException {
        ((IBackupManager) ServiceManager.getService("backup")).adbBackup(writeFile, includeApks, includeObbs, includeShared, doWidgets, doAllApps, includeSystem, compress, doKeyValue, pkgNames);
    }

    static void fullRestore(ParcelFileDescriptor readFile) throws RemoteException {
        ((IBackupManager) ServiceManager.getService("backup")).adbRestore(readFile);
    }
}
