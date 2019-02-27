package vivo.app.backup;

import android.os.ParcelFileDescriptor;

public abstract class AbsVivoBackupManager {
    public abstract void addBackupCompleteSize(long j, int i);

    public abstract boolean backupPackage(String str, ParcelFileDescriptor parcelFileDescriptor, IPackageBackupRestoreObserver iPackageBackupRestoreObserver);

    public abstract boolean backupPackage(String str, ParcelFileDescriptor parcelFileDescriptor, IPackageBackupRestoreObserver iPackageBackupRestoreObserver, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6);

    public abstract boolean backupPackageByZip(String str, ParcelFileDescriptor parcelFileDescriptor, boolean z, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver);

    public abstract boolean checkSupportFeature(int i);

    public abstract boolean enableDualPackage(String str);

    public abstract int getDualUserId();

    public abstract String[] getRestoreCmdList(int i);

    public abstract boolean isDualPackageEnabled(String str);

    public abstract boolean isRunningFromVivoBackup(int i);

    public abstract void postRestoreCompleteSize(long j, int i);

    public abstract boolean restoreBackupFile(ParcelFileDescriptor parcelFileDescriptor, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver);

    public abstract boolean restoreBackupFile(String str, ParcelFileDescriptor parcelFileDescriptor, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver);

    public abstract boolean restorePackageByZip(String str, ParcelFileDescriptor parcelFileDescriptor, boolean z, String[] strArr, IPackageBackupRestoreObserver iPackageBackupRestoreObserver);

    public abstract boolean startConfirmationUi(int i, String str, int i2);
}
