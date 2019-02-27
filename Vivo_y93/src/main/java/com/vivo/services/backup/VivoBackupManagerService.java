package com.vivo.services.backup;

import android.app.IBackupAgent;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.Context;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.backup.Trampoline;
import com.vivo.server.backup.VivoBackupReflectUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipOutputStream;
import vivo.app.backup.BRTimeoutMonitor;
import vivo.app.backup.IPackageBackupRestoreObserver;
import vivo.app.backup.IVivoBackupManager;
import vivo.app.backup.IVivoBackupManager.Stub;
import vivo.app.backup.utils.BackupZipUtils;
import vivo.app.backup.utils.DoubleInstanceUtil;

public class VivoBackupManagerService extends Stub {
    public static HashSet<Integer> BACKUP_SUPPORT_FEATURE = new HashSet<Integer>() {
        {
            add(Integer.valueOf(VivoBackupManagerService.SUPPORT_BACKUP_ZIP));
            add(Integer.valueOf(VivoBackupManagerService.SUPPORT_FULL_BACKUP));
        }
    };
    public static int ERROR_COMMON = 1;
    public static int ERROR_NOT_SUPPORT = 2;
    public static int ERROR_TIMEOUT = 3;
    public static int ERROR_WAIT_PROCESS = 4;
    public static int SUPPORT_BACKUP_ZIP = 2;
    public static int SUPPORT_FULL_BACKUP = 1;
    private static final String TAG = "VivoBackupManagerService";
    private Context mContext;
    final AtomicInteger mNextToken = new AtomicInteger();
    private SparseArray<BackupRestoreParams> mParmslist;
    private PowerManager mPowerManager;
    final Random mTokenGenerator = new Random();
    private WakeLock mWakelock;

    public class BackupRestoreParams {
        private String[] cmdList = null;
        private long completeSize = 0;
        private String currentPackage;
        private CountDownLatch mLatch = null;
        private IPackageBackupRestoreObserver packageBackupRestoreObserver;
        private boolean result = true;

        public BackupRestoreParams(String pkg, IPackageBackupRestoreObserver observer) {
            this.currentPackage = pkg;
            this.packageBackupRestoreObserver = observer;
        }

        public long setCompleteSize(long size) {
            this.completeSize = size;
            return this.completeSize;
        }

        public IPackageBackupRestoreObserver setObserver(IPackageBackupRestoreObserver observer) {
            this.packageBackupRestoreObserver = observer;
            return this.packageBackupRestoreObserver;
        }

        public String setCurrentPackage(String pkg) {
            this.currentPackage = pkg;
            return this.currentPackage;
        }

        public String[] setCmdList(String[] list) {
            this.cmdList = list;
            return this.cmdList;
        }

        public long getCompleteSize() {
            return this.completeSize;
        }

        public IPackageBackupRestoreObserver getObserver() {
            return this.packageBackupRestoreObserver;
        }

        public String getCurrentPackage() {
            return this.currentPackage;
        }

        public String[] getCmdList() {
            return this.cmdList;
        }

        public CountDownLatch getLatch() {
            return this.mLatch;
        }

        public CountDownLatch setLatch(CountDownLatch latch) {
            this.mLatch = latch;
            return this.mLatch;
        }

        public boolean getResult() {
            return this.result;
        }

        public boolean setResult(boolean ret) {
            this.result = ret;
            return this.result;
        }
    }

    class BackupZipRunner implements Runnable {
        IBackupAgent mAgent;
        boolean mAsDual;
        String[] mDirs;
        int mFd;
        IVivoBackupManager mListener;
        ParcelFileDescriptor mPipe;
        String mPkgName;

        BackupZipRunner(IVivoBackupManager listener, int fd, ParcelFileDescriptor pipe, String pkgName, String[] dirs, boolean asDual, IBackupAgent agent) {
            this.mListener = listener;
            this.mPkgName = pkgName;
            this.mPipe = pipe;
            this.mDirs = dirs;
            this.mAgent = agent;
            this.mAsDual = asDual;
            this.mFd = fd;
        }

        public void run() {
            try {
                VivoBackupReflectUtil.callDoBackupByZip(this.mAgent, this.mFd, this.mPipe, this.mDirs, this.mAsDual, this.mListener);
            } catch (RemoteException e) {
                Slog.e(VivoBackupManagerService.TAG, "Remote agent vanished during backupZip " + this.mPkgName);
            }
        }
    }

    public class FullBackupRestoreObserver extends IFullBackupRestoreObserver.Stub {
        int callerFd;

        public FullBackupRestoreObserver(int fd) {
            this.callerFd = fd;
        }

        public void onStartBackup() {
        }

        public void onBackupPackage(String pkgName) {
            synchronized (VivoBackupManagerService.this.mParmslist) {
                BackupRestoreParams params = (BackupRestoreParams) VivoBackupManagerService.this.mParmslist.get(this.callerFd);
                if (!(params == null || params.getObserver() == null)) {
                    try {
                        params.getObserver().onStart(params.getCurrentPackage(), 0);
                    } catch (RemoteException e) {
                        Slog.e(VivoBackupManagerService.TAG, "cant connect PackageBackupRestoreObserver backup onBackupPackage.");
                    }
                }
            }
            return;
        }

        public void onEndBackup() {
            synchronized (VivoBackupManagerService.this.mParmslist) {
                BackupRestoreParams params = (BackupRestoreParams) VivoBackupManagerService.this.mParmslist.get(this.callerFd);
                if (!(params == null || params.getObserver() == null)) {
                    try {
                        params.getObserver().onEnd(params.getCurrentPackage(), 0);
                    } catch (RemoteException e) {
                        Slog.d(VivoBackupManagerService.TAG, "cant connect PackageBackupRestoreObserver backup onEndBackup.");
                    }
                }
            }
            return;
        }

        public void onStartRestore() {
        }

        public void onRestorePackage(String pkgName) {
            synchronized (VivoBackupManagerService.this.mParmslist) {
                BackupRestoreParams params = (BackupRestoreParams) VivoBackupManagerService.this.mParmslist.get(this.callerFd);
                if (!(params == null || params.getObserver() == null)) {
                    try {
                        params.getObserver().onStart(params.getCurrentPackage(), 0);
                    } catch (RemoteException e) {
                        Slog.d(VivoBackupManagerService.TAG, "cant connect PackagBackupRestoreObserver onRestorePackage");
                    }
                }
            }
            return;
        }

        public void onEndRestore() {
            synchronized (VivoBackupManagerService.this.mParmslist) {
                BackupRestoreParams params = (BackupRestoreParams) VivoBackupManagerService.this.mParmslist.get(this.callerFd);
                if (!(params == null || params.getObserver() == null)) {
                    try {
                        params.getObserver().onEnd(params.getCurrentPackage(), 0);
                    } catch (RemoteException e) {
                        Slog.d(VivoBackupManagerService.TAG, "cant connect PackageBackupRestoreObserver restore onEndRestore.");
                    }
                }
            }
            return;
        }

        public void onTimeout() {
            Slog.d(VivoBackupManagerService.TAG, "onTimeout");
            synchronized (VivoBackupManagerService.this.mParmslist) {
                BackupRestoreParams params = (BackupRestoreParams) VivoBackupManagerService.this.mParmslist.get(this.callerFd);
                if (params != null) {
                    params.setResult(false);
                    if (params.getObserver() != null) {
                        try {
                            params.getObserver().onError(params.getCurrentPackage(), 0, VivoBackupManagerService.ERROR_TIMEOUT);
                            if (VivoBackupManagerService.this.mParmslist.indexOfKey(this.callerFd) >= 0) {
                                VivoBackupManagerService.this.mParmslist.remove(this.callerFd);
                                Slog.d(VivoBackupManagerService.TAG, "======== onTimeout ======== , " + params.getCurrentPackage() + ", total: " + params.getCompleteSize() + ", remove params , fd == : " + this.callerFd + " ,paramsList size == " + VivoBackupManagerService.this.mParmslist.size());
                            }
                        } catch (RemoteException e) {
                            Slog.d(VivoBackupManagerService.TAG, "cant connect PackageBackupRestoreObserver onTimeout.");
                            if (VivoBackupManagerService.this.mParmslist.indexOfKey(this.callerFd) >= 0) {
                                VivoBackupManagerService.this.mParmslist.remove(this.callerFd);
                                Slog.d(VivoBackupManagerService.TAG, "======== onTimeout ======== , " + params.getCurrentPackage() + ", total: " + params.getCompleteSize() + ", remove params , fd == : " + this.callerFd + " ,paramsList size == " + VivoBackupManagerService.this.mParmslist.size());
                            }
                        } catch (Throwable th) {
                            if (VivoBackupManagerService.this.mParmslist.indexOfKey(this.callerFd) >= 0) {
                                VivoBackupManagerService.this.mParmslist.remove(this.callerFd);
                                Slog.d(VivoBackupManagerService.TAG, "======== onTimeout ======== , " + params.getCurrentPackage() + ", total: " + params.getCompleteSize() + ", remove params , fd == : " + this.callerFd + " ,paramsList size == " + VivoBackupManagerService.this.mParmslist.size());
                            }
                        }
                    }
                }
            }
        }
    }

    class RestoreZipRunner implements Runnable {
        IBackupAgent mAgent;
        String[] mCmdList;
        int mFd;
        IVivoBackupManager mListener;
        ParcelFileDescriptor mPipe;
        String mPkgName;

        RestoreZipRunner(IVivoBackupManager listener, int fd, ParcelFileDescriptor pipe, String pkgName, IBackupAgent agent, String[] cmdList) {
            this.mListener = listener;
            this.mPkgName = pkgName;
            this.mPipe = pipe;
            this.mAgent = agent;
            this.mCmdList = cmdList;
            this.mFd = fd;
        }

        public void run() {
            try {
                VivoBackupReflectUtil.callDoRestoreByZip(this.mAgent, this.mFd, this.mPipe, this.mListener, this.mCmdList);
            } catch (RemoteException e) {
                Slog.e(VivoBackupManagerService.TAG, "Remote agent vanished during RestoreZip " + this.mPkgName);
            }
        }
    }

    public VivoBackupManagerService(Context context) {
        this.mContext = context;
        this.mParmslist = new SparseArray();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mWakelock = this.mPowerManager.newWakeLock(1, "*vivo backup*");
    }

    public boolean checkSupportFeature(int feature) {
        return BACKUP_SUPPORT_FEATURE.contains(Integer.valueOf(feature));
    }

    /* JADX WARNING: Missing block: B:45:0x0123, code:
            r14 = r21.getFd();
            r0 = new com.vivo.services.backup.VivoBackupManagerService.BackupRestoreParams(r19, r20, r22);
            r5 = r19.mParmslist;
     */
    /* JADX WARNING: Missing block: B:46:0x013a, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r19.mParmslist.put(r14, r0);
            android.util.Slog.d(TAG, "backupPackages ->" + r20 + ", fd = " + r14 + " , num = " + r19.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:49:0x017e, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            com.vivo.services.backup.VivoBackupManagerServiceProxy.fullBackup(r21, new java.lang.String[]{r20}, r23, r24, r25, r26, r27, r28, r29, r30);
     */
    /* JADX WARNING: Missing block: B:53:0x019a, code:
            r5 = r19.mParmslist;
     */
    /* JADX WARNING: Missing block: B:54:0x019e, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:55:0x019f, code:
            if (1 == null) goto L_0x0211;
     */
    /* JADX WARNING: Missing block: B:57:?, code:
            r18 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:59:0x01ad, code:
            if (r19.mParmslist.indexOfKey(r14) < 0) goto L_0x020c;
     */
    /* JADX WARNING: Missing block: B:60:0x01af, code:
            r19.mParmslist.remove(r14);
            android.util.Slog.d(TAG, "Finish backupPackages " + r20 + " , total: " + r0.getCompleteSize() + " , fd = " + r14 + ", result: " + r18 + " , num = " + r19.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:61:0x020c, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:62:0x020d, code:
            return r18;
     */
    /* JADX WARNING: Missing block: B:66:0x0211, code:
            r18 = false;
     */
    /* JADX WARNING: Missing block: B:70:0x0217, code:
            r15 = move-exception;
     */
    /* JADX WARNING: Missing block: B:72:?, code:
            android.util.Slog.e(TAG, "connect backup service failed!", r15);
     */
    /* JADX WARNING: Missing block: B:73:0x0221, code:
            r5 = r19.mParmslist;
     */
    /* JADX WARNING: Missing block: B:74:0x0227, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:75:0x0228, code:
            if (false != false) goto L_0x022a;
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            r18 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:79:0x0236, code:
            if (r19.mParmslist.indexOfKey(r14) >= 0) goto L_0x0238;
     */
    /* JADX WARNING: Missing block: B:80:0x0238, code:
            r19.mParmslist.remove(r14);
            android.util.Slog.d(TAG, "Finish backupPackages " + r20 + " , total: " + r0.getCompleteSize() + " , fd = " + r14 + ", result: " + r18 + " , num = " + r19.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:84:0x029a, code:
            r18 = false;
     */
    /* JADX WARNING: Missing block: B:87:0x02a2, code:
            monitor-enter(r19.mParmslist);
     */
    /* JADX WARNING: Missing block: B:88:0x02a3, code:
            if (1 != null) goto L_0x02a5;
     */
    /* JADX WARNING: Missing block: B:90:?, code:
            r18 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:92:0x02b1, code:
            if (r19.mParmslist.indexOfKey(r14) >= 0) goto L_0x02b3;
     */
    /* JADX WARNING: Missing block: B:93:0x02b3, code:
            r19.mParmslist.remove(r14);
            android.util.Slog.d(TAG, "Finish backupPackages " + r20 + " , total: " + r0.getCompleteSize() + " , fd = " + r14 + ", result: " + r18 + " , num = " + r19.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:96:0x0312, code:
            r18 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean backupPackage(String pkgName, ParcelFileDescriptor writeFile, IPackageBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, boolean doKeyValue) throws RemoteException {
        Slog.d(TAG, "\n####################  [ " + pkgName + " ]  ####################");
        if (pkgName == null || writeFile == null || observer == null) {
            Slog.e(TAG, "get null params");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_COMMON);
                } catch (RemoteException e) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e);
                }
            }
            return false;
        } else if (BACKUP_SUPPORT_FEATURE.contains(Integer.valueOf(SUPPORT_FULL_BACKUP))) {
            this.mContext.enforceCallingPermission("android.permission.BACKUP", "backupPackages");
            synchronized (this.mParmslist) {
                for (int i = 0; i < this.mParmslist.size(); i++) {
                    if (((BackupRestoreParams) this.mParmslist.valueAt(i)).getCurrentPackage().equals(pkgName)) {
                        Slog.w(TAG, "failed to backupPackage because other BackupRestoreProcess is running for package:" + pkgName);
                        if (observer != null) {
                            try {
                                observer.onError(pkgName, 0, ERROR_WAIT_PROCESS);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2);
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            Slog.e(TAG, "not support feature in backupPackages");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_NOT_SUPPORT);
                } catch (RemoteException e22) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22);
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:46:0x010c, code:
            r2 = r16.getFd();
            r5 = new com.vivo.services.backup.VivoBackupManagerService.BackupRestoreParams(r14, r15, r18);
            r5.setCmdList(r17);
            r8 = r14.mParmslist;
     */
    /* JADX WARNING: Missing block: B:47:0x011f, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r14.mParmslist.put(r2, r5);
            android.util.Slog.d(TAG, "restoreBackupFile -> " + r15 + " , fd = " + r2 + " , num = " + r14.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:50:0x015b, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            com.vivo.services.backup.VivoBackupManagerServiceProxy.fullRestore(r16);
     */
    /* JADX WARNING: Missing block: B:53:0x015f, code:
            r8 = r14.mParmslist;
     */
    /* JADX WARNING: Missing block: B:54:0x0161, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:55:0x0162, code:
            if (1 == null) goto L_0x01ca;
     */
    /* JADX WARNING: Missing block: B:57:?, code:
            r6 = r5.getResult();
     */
    /* JADX WARNING: Missing block: B:59:0x016e, code:
            if (r14.mParmslist.indexOfKey(r2) < 0) goto L_0x01c5;
     */
    /* JADX WARNING: Missing block: B:60:0x0170, code:
            r14.mParmslist.remove(r2);
            android.util.Slog.d(TAG, "Finish restoreBackupFile " + r15 + " , total: " + r5.getCompleteSize() + ", fd = " + r2 + ", result: " + r6 + " , num = " + r14.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:61:0x01c5, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:62:0x01c6, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:66:0x01ca, code:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:70:0x01cf, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:72:?, code:
            android.util.Slog.e(TAG, "connect backup service failed!", r3);
     */
    /* JADX WARNING: Missing block: B:73:0x01d9, code:
            r8 = r14.mParmslist;
     */
    /* JADX WARNING: Missing block: B:74:0x01dc, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:75:0x01dd, code:
            if (false != false) goto L_0x01df;
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            r6 = r5.getResult();
     */
    /* JADX WARNING: Missing block: B:79:0x01e9, code:
            if (r14.mParmslist.indexOfKey(r2) >= 0) goto L_0x01eb;
     */
    /* JADX WARNING: Missing block: B:80:0x01eb, code:
            r14.mParmslist.remove(r2);
            android.util.Slog.d(TAG, "Finish restoreBackupFile " + r15 + " , total: " + r5.getCompleteSize() + ", fd = " + r2 + ", result: " + r6 + " , num = " + r14.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:84:0x0244, code:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:87:0x0249, code:
            monitor-enter(r14.mParmslist);
     */
    /* JADX WARNING: Missing block: B:88:0x024a, code:
            if (1 != null) goto L_0x024c;
     */
    /* JADX WARNING: Missing block: B:90:?, code:
            r6 = r5.getResult();
     */
    /* JADX WARNING: Missing block: B:92:0x0256, code:
            if (r14.mParmslist.indexOfKey(r2) >= 0) goto L_0x0258;
     */
    /* JADX WARNING: Missing block: B:93:0x0258, code:
            r14.mParmslist.remove(r2);
            android.util.Slog.d(TAG, "Finish restoreBackupFile " + r15 + " , total: " + r5.getCompleteSize() + ", fd = " + r2 + ", result: " + r6 + " , num = " + r14.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:96:0x02af, code:
            r6 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean restoreBackupFile(String pkgName, ParcelFileDescriptor readFile, String[] cmdList, IPackageBackupRestoreObserver observer) throws RemoteException {
        Slog.d(TAG, "\n####################  [ " + pkgName + " ]  ####################");
        if (pkgName == null || readFile == null || observer == null || cmdList == null) {
            Slog.e(TAG, "get null params");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_COMMON);
                } catch (RemoteException e) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e);
                }
            }
            return false;
        } else if (BACKUP_SUPPORT_FEATURE.contains(Integer.valueOf(SUPPORT_FULL_BACKUP))) {
            this.mContext.enforceCallingPermission("android.permission.BACKUP", "backupPackages");
            synchronized (this.mParmslist) {
                for (int i = 0; i < this.mParmslist.size(); i++) {
                    if (((BackupRestoreParams) this.mParmslist.valueAt(i)).getCurrentPackage().equals(pkgName)) {
                        Slog.w(TAG, "failed to restoreBackupFile because other BackupRestoreProcess is running for package:" + pkgName);
                        if (observer != null) {
                            try {
                                observer.onError(pkgName, 0, ERROR_WAIT_PROCESS);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2);
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            Slog.e(TAG, "not support feature in restoreBackupFile");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_NOT_SUPPORT);
                } catch (RemoteException e22) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22);
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:318:0x091b A:{SYNTHETIC, Splitter: B:318:0x091b} */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0932  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x09b2  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x09d3  */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x09e7 A:{SYNTHETIC, Splitter: B:342:0x09e7} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x09ec A:{Catch:{ IOException -> 0x0a3b }} */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0a51 A:{SYNTHETIC, Splitter: B:364:0x0a51} */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x0a68  */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x0ae8  */
    /* JADX WARNING: Removed duplicated region for block: B:383:0x0b09  */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0b1d A:{SYNTHETIC, Splitter: B:388:0x0b1d} */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0b22 A:{Catch:{ IOException -> 0x0b71 }} */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0a51 A:{SYNTHETIC, Splitter: B:364:0x0a51} */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x0a68  */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x0ae8  */
    /* JADX WARNING: Removed duplicated region for block: B:383:0x0b09  */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0b1d A:{SYNTHETIC, Splitter: B:388:0x0b1d} */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0b22 A:{Catch:{ IOException -> 0x0b71 }} */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x091b A:{SYNTHETIC, Splitter: B:318:0x091b} */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0932  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x09b2  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x09d3  */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x09e7 A:{SYNTHETIC, Splitter: B:342:0x09e7} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x09ec A:{Catch:{ IOException -> 0x0a3b }} */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0a51 A:{SYNTHETIC, Splitter: B:364:0x0a51} */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x0a68  */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x0ae8  */
    /* JADX WARNING: Removed duplicated region for block: B:383:0x0b09  */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0b1d A:{SYNTHETIC, Splitter: B:388:0x0b1d} */
    /* JADX WARNING: Removed duplicated region for block: B:391:0x0b22 A:{Catch:{ IOException -> 0x0b71 }} */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x0714 A:{SYNTHETIC, Splitter: B:247:0x0714} */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x0719 A:{Catch:{ IOException -> 0x0768 }} */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0426  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043a A:{SYNTHETIC, Splitter: B:148:0x043a} */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x043f A:{Catch:{ IOException -> 0x048e }} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02a7  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02bb A:{SYNTHETIC, Splitter: B:94:0x02bb} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02c0 A:{Catch:{ IOException -> 0x033c }} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0206  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0286  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x02a7  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02bb A:{SYNTHETIC, Splitter: B:94:0x02bb} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02c0 A:{Catch:{ IOException -> 0x033c }} */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0385  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x0405  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0426  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043a A:{SYNTHETIC, Splitter: B:148:0x043a} */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x043f A:{Catch:{ IOException -> 0x048e }} */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x050e  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x058e  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x05af  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x05c3 A:{SYNTHETIC, Splitter: B:199:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x05c8 A:{Catch:{ IOException -> 0x0617 }} */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x065f  */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x06df  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0700  */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x0714 A:{SYNTHETIC, Splitter: B:247:0x0714} */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x0719 A:{Catch:{ IOException -> 0x0768 }} */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x07cf  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x084f  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0870  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0884 A:{SYNTHETIC, Splitter: B:294:0x0884} */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0889 A:{Catch:{ IOException -> 0x08d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x05af  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x05c3 A:{SYNTHETIC, Splitter: B:199:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x05c8 A:{Catch:{ IOException -> 0x0617 }} */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0870  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0884 A:{SYNTHETIC, Splitter: B:294:0x0884} */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0889 A:{Catch:{ IOException -> 0x08d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x043a A:{SYNTHETIC, Splitter: B:148:0x043a} */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x043f A:{Catch:{ IOException -> 0x048e }} */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02bb A:{SYNTHETIC, Splitter: B:94:0x02bb} */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02c0 A:{Catch:{ IOException -> 0x033c }} */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x05c3 A:{SYNTHETIC, Splitter: B:199:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x05c8 A:{Catch:{ IOException -> 0x0617 }} */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0884 A:{SYNTHETIC, Splitter: B:294:0x0884} */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x0889 A:{Catch:{ IOException -> 0x08d8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0700  */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x0714 A:{SYNTHETIC, Splitter: B:247:0x0714} */
    /* JADX WARNING: Removed duplicated region for block: B:250:0x0719 A:{Catch:{ IOException -> 0x0768 }} */
    /* JADX WARNING: Missing block: B:46:0x012d, code:
            r34 = android.os.Binder.clearCallingIdentity();
            r41.mWakelock.acquire();
            r7 = r43.getFd();
            r0 = new com.vivo.services.backup.VivoBackupManagerService.BackupRestoreParams(r41, r42, r46);
            r5 = r41.mParmslist;
     */
    /* JADX WARNING: Missing block: B:47:0x014d, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r41.mParmslist.put(r7, r0);
            android.util.Slog.d(TAG, "backupPackageByZip -> " + r42 + ", fd = " + r7 + " , num = " + r41.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:50:0x0191, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:52:0x019b, code:
            if (r46 == null) goto L_0x01a5;
     */
    /* JADX WARNING: Missing block: B:55:?, code:
            r46.onStart(r42, 0);
     */
    /* JADX WARNING: Missing block: B:103:0x02c9, code:
            r27 = move-exception;
     */
    /* JADX WARNING: Missing block: B:104:0x02ca, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onStart." + r27);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean backupPackageByZip(String pkgName, ParcelFileDescriptor writeFile, boolean enableDual, String[] dirs, IPackageBackupRestoreObserver observer) {
        boolean z;
        IOException e;
        OutputStream outputStream;
        Throwable th;
        Slog.d(TAG, "\n####################  [ " + pkgName + " ]  ####################");
        if (pkgName == null || writeFile == null || observer == null || dirs == null) {
            Slog.e(TAG, "get null params");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_COMMON);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2);
                }
            }
            return false;
        } else if (BACKUP_SUPPORT_FEATURE.contains(Integer.valueOf(SUPPORT_BACKUP_ZIP))) {
            this.mContext.enforceCallingPermission("android.permission.BACKUP", "backupPackageByZip");
            z = true;
            synchronized (this.mParmslist) {
                for (int i = 0; i < this.mParmslist.size(); i++) {
                    if (((BackupRestoreParams) this.mParmslist.valueAt(i)).getCurrentPackage().equals(pkgName)) {
                        Slog.w(TAG, "failed to backupPackageByZip because other BackupRestoreProcess is running for package:" + pkgName);
                        if (observer != null) {
                            try {
                                observer.onError(pkgName, 0, ERROR_WAIT_PROCESS);
                            } catch (RemoteException e22) {
                                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22);
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            Slog.e(TAG, "not support feature in backupPackageByZip");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_NOT_SUPPORT);
                } catch (RemoteException e222) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e222);
                }
            }
            return false;
        }
        ZipOutputStream out;
        synchronized (this.mParmslist) {
            z = false ? r0.getResult() : false;
            if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                this.mParmslist.remove(callerFd);
                Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
            }
        }
        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
        if (enableDual) {
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
        }
        if (!(z || observer == null)) {
            try {
                observer.onError(pkgName, 0, ERROR_COMMON);
            } catch (RemoteException e2222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2222);
            }
        }
        if (observer != null) {
            try {
                observer.onEnd(pkgName, 0);
            } catch (RemoteException e22222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e22222);
            }
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
            try {
                out.close();
            } catch (Throwable e3) {
                Slog.w(TAG, "close pipe failed after backupPackageByZip", e3);
                return false;
            }
        }
        if (writeFile != null) {
            writeFile.close();
        }
        return false;
        synchronized (this.mParmslist) {
            z = false ? r0.getResult() : false;
            if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                this.mParmslist.remove(callerFd);
                Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
            }
        }
        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
        if (enableDual) {
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
        }
        if (!(z || observer == null)) {
            try {
                observer.onError(pkgName, 0, ERROR_COMMON);
            } catch (RemoteException e222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e222222);
            }
        }
        if (observer != null) {
            try {
                observer.onEnd(pkgName, 0);
            } catch (RemoteException e2222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e2222222);
            }
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
            try {
                out.close();
            } catch (Throwable e32) {
                Slog.w(TAG, "close pipe failed after backupPackageByZip", e32);
                return false;
            }
        }
        if (writeFile != null) {
            writeFile.close();
        }
        return false;
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
            try {
                out.close();
            } catch (Throwable e322) {
                Slog.w(TAG, "close pipe failed after backupPackageByZip", e322);
                return false;
            }
        }
        if (writeFile != null) {
            writeFile.close();
        }
        return false;
        synchronized (this.mParmslist) {
            z = false ? r0.getResult() : false;
            if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                this.mParmslist.remove(callerFd);
                Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
            }
        }
        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
        if (enableDual) {
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
        }
        if (!(z || observer == null)) {
            try {
                observer.onError(pkgName, 0, ERROR_COMMON);
            } catch (RemoteException e22222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22222222);
            }
        }
        if (observer != null) {
            try {
                observer.onEnd(pkgName, 0);
            } catch (RemoteException e222222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e222222222);
            }
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
            try {
                out.close();
            } catch (Throwable e3222) {
                Slog.w(TAG, "close pipe failed after backupPackageByZip", e3222);
                return false;
            }
        }
        if (writeFile != null) {
            writeFile.close();
        }
        return false;
        synchronized (this.mParmslist) {
            z = z ? r0.getResult() : false;
            if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                this.mParmslist.remove(callerFd);
                Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
            }
        }
        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
        if (enableDual) {
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
        }
        if (!(z || observer == null)) {
            try {
                observer.onError(pkgName, 0, ERROR_COMMON);
            } catch (RemoteException e2222222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2222222222);
            }
        }
        if (observer != null) {
            try {
                observer.onEnd(pkgName, 0);
            } catch (RemoteException e22222222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e22222222222);
            }
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
            try {
                out.close();
            } catch (Throwable e32222) {
                Slog.w(TAG, "close pipe failed after backupPackageByZip", e32222);
                return z;
            }
        }
        if (writeFile != null) {
            writeFile.close();
        }
        return z;
        synchronized (this.mParmslist) {
            z = false ? r0.getResult() : false;
            if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                this.mParmslist.remove(callerFd);
                Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
            }
        }
        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
        if (enableDual) {
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
        }
        if (!(z || observer == null)) {
            try {
                observer.onError(pkgName, 0, ERROR_COMMON);
            } catch (RemoteException e222222222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e222222222222);
            }
        }
        if (observer != null) {
            try {
                observer.onEnd(pkgName, 0);
            } catch (RemoteException e2222222222222) {
                Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e2222222222222);
            }
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        if (observer != null) {
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        if (observer != null) {
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        BRTimeoutMonitor bRTimeoutMonitor = new BRTimeoutMonitor(TAG, generateRandomIntegerToken());
        CountDownLatch countDownLatch;
        if (enableDual) {
            countDownLatch = new CountDownLatch(2);
        } else {
            countDownLatch = new CountDownLatch(1);
        }
        r0.setLatch(latch);
        try {
            OutputStream fileOutputStream = new FileOutputStream(writeFile.getFileDescriptor());
            try {
                out = new ZipOutputStream(fileOutputStream);
                try {
                    ParcelFileDescriptor[] pipes = ParcelFileDescriptor.createPipe();
                    if (pipes == null) {
                        Slog.e(TAG, "Create pipe in backupPackageByZip failed !  asDual: false");
                        bRTimeoutMonitor.cancel();
                        if (latch != null) {
                            try {
                                Slog.d(TAG, "wait for agent complete !");
                                latch.await(5000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e4) {
                                e4.printStackTrace();
                            }
                        }
                        synchronized (this.mParmslist) {
                        }
                        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                        if (enableDual) {
                        }
                        observer.onError(pkgName, 0, ERROR_COMMON);
                        if (observer != null) {
                        }
                        this.mWakelock.release();
                        Binder.restoreCallingIdentity(oldId);
                        if (out != null) {
                        }
                        if (writeFile != null) {
                        }
                        return false;
                    }
                    IBackupAgent agent = VivoBackupReflectUtil.callInitializeAgent((Trampoline) ServiceManager.getService("backup"), pkgName, 1, false, callerFd);
                    if (agent == null) {
                        Slog.e(TAG, "get agent failed, asDual: false");
                        bRTimeoutMonitor.cancel();
                        if (latch != null) {
                            try {
                                Slog.d(TAG, "wait for agent complete !");
                                latch.await(5000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e42) {
                                e42.printStackTrace();
                            }
                        }
                        synchronized (this.mParmslist) {
                        }
                        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                        if (enableDual) {
                        }
                        observer.onError(pkgName, 0, ERROR_COMMON);
                        if (observer != null) {
                        }
                        this.mWakelock.release();
                        Binder.restoreCallingIdentity(oldId);
                        if (out != null) {
                        }
                        if (writeFile != null) {
                        }
                        return false;
                    }
                    new Thread(new BackupZipRunner(this, callerFd, pipes[1], pkgName, dirs, false, agent)).start();
                    Slog.d(TAG, "mergeStream start , userId : 0");
                    bRTimeoutMonitor.start(30000);
                    z = BackupZipUtils.mergeToZipOutputStream(pkgName, pipes[0], out, observer, bRTimeoutMonitor, callerFd);
                    Slog.d(TAG, "mergeStream finish! , userId : 0");
                    if (enableDual && z) {
                        pipes = ParcelFileDescriptor.createPipe();
                        if (pipes == null) {
                            Slog.e(TAG, "Create pipe in backupPackageByZip failed !  asDual: true");
                            bRTimeoutMonitor.cancel();
                            if (latch != null) {
                                try {
                                    Slog.d(TAG, "wait for agent complete !");
                                    latch.await(5000, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e422) {
                                    e422.printStackTrace();
                                }
                            }
                            synchronized (this.mParmslist) {
                            }
                            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                            if (enableDual) {
                            }
                            observer.onError(pkgName, 0, ERROR_COMMON);
                            if (observer != null) {
                            }
                            this.mWakelock.release();
                            Binder.restoreCallingIdentity(oldId);
                            if (out != null) {
                            }
                            if (writeFile != null) {
                            }
                            return false;
                        }
                        IBackupAgent agentForDual = VivoBackupReflectUtil.callInitializeAgent((Trampoline) ServiceManager.getService("backup"), pkgName, 1, true, callerFd);
                        if (agentForDual == null) {
                            Slog.e(TAG, "get agent failed, asDual:true ");
                            bRTimeoutMonitor.cancel();
                            if (latch != null) {
                                try {
                                    Slog.d(TAG, "wait for agent complete !");
                                    latch.await(5000, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e4222) {
                                    e4222.printStackTrace();
                                }
                            }
                            synchronized (this.mParmslist) {
                            }
                            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                            if (enableDual) {
                            }
                            observer.onError(pkgName, 0, ERROR_COMMON);
                            if (observer != null) {
                            }
                            this.mWakelock.release();
                            Binder.restoreCallingIdentity(oldId);
                            if (out != null) {
                            }
                            if (writeFile != null) {
                            }
                            return false;
                        }
                        new Thread(new BackupZipRunner(this, callerFd, pipes[1], pkgName, dirs, true, agentForDual)).start();
                        Slog.d(TAG, "mergeStream start , userId : 999");
                        BackupZipUtils.mergeToZipOutputStream(pkgName, pipes[0], out, observer, bRTimeoutMonitor, callerFd);
                        Slog.d(TAG, "mergeStream finish! , userId : 999");
                    }
                    bRTimeoutMonitor.cancel();
                    if (latch != null) {
                        try {
                            Slog.d(TAG, "wait for agent complete !");
                            latch.await(5000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e42222) {
                            e42222.printStackTrace();
                        }
                    }
                    synchronized (this.mParmslist) {
                    }
                    VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                    if (enableDual) {
                    }
                    observer.onError(pkgName, 0, ERROR_COMMON);
                    if (observer != null) {
                    }
                    this.mWakelock.release();
                    Binder.restoreCallingIdentity(oldId);
                    if (out != null) {
                    }
                    if (writeFile != null) {
                    }
                    return z;
                } catch (IOException e5) {
                    e = e5;
                    outputStream = fileOutputStream;
                    try {
                        Slog.e(TAG, "backupPackageByZip exception " + pkgName + ": " + e.getMessage());
                        bRTimeoutMonitor.cancel();
                        if (latch != null) {
                        }
                        synchronized (this.mParmslist) {
                        }
                        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                        if (enableDual) {
                        }
                        try {
                            observer.onError(pkgName, 0, ERROR_COMMON);
                        } catch (RemoteException e22222222222222) {
                            Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22222222222222);
                        }
                        if (observer != null) {
                        }
                        this.mWakelock.release();
                        Binder.restoreCallingIdentity(oldId);
                        if (out != null) {
                        }
                        if (writeFile != null) {
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        bRTimeoutMonitor.cancel();
                        if (latch != null) {
                        }
                        synchronized (this.mParmslist) {
                        }
                        VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                        if (enableDual) {
                        }
                        try {
                            observer.onError(pkgName, 0, ERROR_COMMON);
                        } catch (RemoteException e222222222222222) {
                            Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e222222222222222);
                        }
                        if (observer != null) {
                        }
                        this.mWakelock.release();
                        Binder.restoreCallingIdentity(oldId);
                        if (out != null) {
                        }
                        if (writeFile != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th22) {
                    th = th22;
                    outputStream = fileOutputStream;
                    bRTimeoutMonitor.cancel();
                    if (latch != null) {
                    }
                    synchronized (this.mParmslist) {
                    }
                    VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                    if (enableDual) {
                    }
                    observer.onError(pkgName, 0, ERROR_COMMON);
                    if (observer != null) {
                    }
                    this.mWakelock.release();
                    Binder.restoreCallingIdentity(oldId);
                    if (out != null) {
                    }
                    if (writeFile != null) {
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e = e6;
                out = null;
                outputStream = fileOutputStream;
                Slog.e(TAG, "backupPackageByZip exception " + pkgName + ": " + e.getMessage());
                bRTimeoutMonitor.cancel();
                if (latch != null) {
                }
                synchronized (this.mParmslist) {
                }
                VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                if (enableDual) {
                }
                observer.onError(pkgName, 0, ERROR_COMMON);
                if (observer != null) {
                }
                this.mWakelock.release();
                Binder.restoreCallingIdentity(oldId);
                if (out != null) {
                }
                if (writeFile != null) {
                }
                return false;
            } catch (Throwable th222) {
                th = th222;
                out = null;
                outputStream = fileOutputStream;
                bRTimeoutMonitor.cancel();
                if (latch != null) {
                }
                synchronized (this.mParmslist) {
                }
                VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
                if (enableDual) {
                }
                observer.onError(pkgName, 0, ERROR_COMMON);
                if (observer != null) {
                }
                this.mWakelock.release();
                Binder.restoreCallingIdentity(oldId);
                if (out != null) {
                }
                if (writeFile != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            out = null;
            Slog.e(TAG, "backupPackageByZip exception " + pkgName + ": " + e.getMessage());
            bRTimeoutMonitor.cancel();
            if (latch != null) {
                try {
                    Slog.d(TAG, "wait for agent complete !");
                    latch.await(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e422222) {
                    e422222.printStackTrace();
                }
            }
            synchronized (this.mParmslist) {
                z = false ? r0.getResult() : false;
                if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                    this.mParmslist.remove(callerFd);
                    Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
                }
            }
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
            if (enableDual) {
                VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
            }
            if (!(z || observer == null)) {
                observer.onError(pkgName, 0, ERROR_COMMON);
            }
            if (observer != null) {
                try {
                    observer.onEnd(pkgName, 0);
                } catch (RemoteException e2222222222222222) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e2222222222222222);
                }
            }
            this.mWakelock.release();
            Binder.restoreCallingIdentity(oldId);
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable e322222) {
                    Slog.w(TAG, "close pipe failed after backupPackageByZip", e322222);
                    return false;
                }
            }
            if (writeFile != null) {
                writeFile.close();
            }
            return false;
        } catch (Throwable th2222) {
            th = th2222;
            out = null;
            bRTimeoutMonitor.cancel();
            if (latch != null) {
                try {
                    Slog.d(TAG, "wait for agent complete !");
                    latch.await(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e4222222) {
                    e4222222.printStackTrace();
                }
            }
            synchronized (this.mParmslist) {
                z = z ? r0.getResult() : false;
                if (this.mParmslist.indexOfKey(callerFd) >= 0) {
                    this.mParmslist.remove(callerFd);
                    Slog.d(TAG, "Finish BackupByZip " + pkgName + ", total: " + r0.getCompleteSize() + ", fd = " + callerFd + " , result: " + z + " , num = " + this.mParmslist.size());
                }
            }
            VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, false, callerFd);
            if (enableDual) {
                VivoBackupReflectUtil.callTearDownAgent((Trampoline) ServiceManager.getService("backup"), pkgName, true, callerFd);
            }
            if (!(z || observer == null)) {
                observer.onError(pkgName, 0, ERROR_COMMON);
            }
            if (observer != null) {
                try {
                    observer.onEnd(pkgName, 0);
                } catch (RemoteException e22222222222222222) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver backup onEnd." + e22222222222222222);
                }
            }
            this.mWakelock.release();
            Binder.restoreCallingIdentity(oldId);
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable e3222222) {
                    Slog.w(TAG, "close pipe failed after backupPackageByZip", e3222222);
                    throw th;
                }
            }
            if (writeFile != null) {
                writeFile.close();
            }
            throw th;
        }
        if (observer != null) {
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        if (observer != null) {
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return z;
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return z;
        if (observer != null) {
        }
        this.mWakelock.release();
        Binder.restoreCallingIdentity(oldId);
        if (out != null) {
        }
        if (writeFile != null) {
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:46:0x012d, code:
            r26 = android.os.Binder.clearCallingIdentity();
            r33.mWakelock.acquire();
            r7 = r35.getFd();
            r0 = new com.vivo.services.backup.VivoBackupManagerService.BackupRestoreParams(r33, r34, r38);
            r0.setCmdList(r37);
            r5 = r33.mParmslist;
     */
    /* JADX WARNING: Missing block: B:47:0x0154, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            r33.mParmslist.put(r7, r0);
            android.util.Slog.d(TAG, "restorePackageByZip -> " + r34 + ", fd = " + r7 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:50:0x0198, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:51:0x0199, code:
            r30 = null;
            r22 = false;
     */
    /* JADX WARNING: Missing block: B:52:0x01a2, code:
            if (r36 == false) goto L_0x02f7;
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            r22 = isDualPackageEnabled(r34);
     */
    /* JADX WARNING: Missing block: B:55:0x01a8, code:
            android.util.Slog.d(TAG, "enableDual: " + r22);
     */
    /* JADX WARNING: Missing block: B:56:0x01c4, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callClearApplicationDataSyncAsUser((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, 0);
     */
    /* JADX WARNING: Missing block: B:57:0x01d3, code:
            if (r22 != false) goto L_0x01d5;
     */
    /* JADX WARNING: Missing block: B:58:0x01d5, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callClearApplicationDataSyncAsUser((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, getDualUserId());
     */
    /* JADX WARNING: Missing block: B:59:0x01e7, code:
            if (r38 != null) goto L_0x01e9;
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            r38.onStart(r34, 0);
     */
    /* JADX WARNING: Missing block: B:63:0x01f1, code:
            r0 = new vivo.app.backup.BRTimeoutMonitor(TAG, generateRandomIntegerToken());
     */
    /* JADX WARNING: Missing block: B:64:0x0201, code:
            if (r22 != false) goto L_0x0203;
     */
    /* JADX WARNING: Missing block: B:65:0x0203, code:
            r0 = new java.util.concurrent.CountDownLatch(2);
     */
    /* JADX WARNING: Missing block: B:66:0x020b, code:
            r0.setLatch(r24);
     */
    /* JADX WARNING: Missing block: B:68:?, code:
            r29 = android.os.ParcelFileDescriptor.createPipe();
     */
    /* JADX WARNING: Missing block: B:69:0x0216, code:
            if (r29 == null) goto L_0x0218;
     */
    /* JADX WARNING: Missing block: B:70:0x0218, code:
            android.util.Slog.e(TAG, "Create pipe in restorePackageByZip failed !  asDual: false");
     */
    /* JADX WARNING: Missing block: B:71:0x0221, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:72:0x0227, code:
            if (r24 != null) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:74:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:76:0x023f, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:77:0x0240, code:
            if (false != false) goto L_0x0242;
     */
    /* JADX WARNING: Missing block: B:79:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:81:0x024e, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x0250;
     */
    /* JADX WARNING: Missing block: B:82:0x0250, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:84:0x02ae, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:85:0x02be, code:
            if (r22 != false) goto L_0x02c0;
     */
    /* JADX WARNING: Missing block: B:86:0x02c0, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:90:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:91:0x02df, code:
            if (r38 != null) goto L_0x02e1;
     */
    /* JADX WARNING: Missing block: B:94:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:95:0x02e9, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:96:0x02f3, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:100:0x02f7, code:
            r22 = false;
     */
    /* JADX WARNING: Missing block: B:101:0x02fb, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:102:0x02fc, code:
            android.util.Slog.e(TAG, "RemoteException" + r19);
     */
    /* JADX WARNING: Missing block: B:103:0x031a, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:104:0x031b, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onStart." + r19);
     */
    /* JADX WARNING: Missing block: B:105:0x0339, code:
            r0 = new java.util.concurrent.CountDownLatch(1);
     */
    /* JADX WARNING: Missing block: B:106:0x0343, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:107:0x0344, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:108:0x0349, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:112:0x0350, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:113:0x0351, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:114:0x036f, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:115:0x0370, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:117:?, code:
            r10 = com.vivo.server.backup.VivoBackupReflectUtil.callInitializeAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, 3, false, r7);
     */
    /* JADX WARNING: Missing block: B:118:0x039f, code:
            if (r10 == null) goto L_0x03a1;
     */
    /* JADX WARNING: Missing block: B:119:0x03a1, code:
            android.util.Slog.e(TAG, "get agent failed, asDual: false");
     */
    /* JADX WARNING: Missing block: B:120:0x03aa, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:121:0x03b0, code:
            if (r24 != null) goto L_0x03b2;
     */
    /* JADX WARNING: Missing block: B:123:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:125:0x03c8, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:126:0x03c9, code:
            if (false != false) goto L_0x03cb;
     */
    /* JADX WARNING: Missing block: B:128:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:130:0x03d7, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x03d9;
     */
    /* JADX WARNING: Missing block: B:131:0x03d9, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:133:0x0437, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:134:0x0447, code:
            if (r22 != false) goto L_0x0449;
     */
    /* JADX WARNING: Missing block: B:135:0x0449, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:139:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:140:0x0468, code:
            if (r38 != null) goto L_0x046a;
     */
    /* JADX WARNING: Missing block: B:143:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:144:0x0472, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:145:0x047c, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:146:0x047d, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:147:0x047e, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:148:0x0483, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:152:0x048a, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:153:0x048b, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:154:0x04a8, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:155:0x04a9, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:157:?, code:
            new java.lang.Thread(new com.vivo.services.backup.VivoBackupManagerService.RestoreZipRunner(r33, r33, r7, r29[0], r34, r10, r37)).start();
     */
    /* JADX WARNING: Missing block: B:158:0x04de, code:
            if (r22 != false) goto L_0x04e0;
     */
    /* JADX WARNING: Missing block: B:159:0x04e0, code:
            r30 = android.os.ParcelFileDescriptor.createPipe();
     */
    /* JADX WARNING: Missing block: B:160:0x04e4, code:
            if (r30 == null) goto L_0x04e6;
     */
    /* JADX WARNING: Missing block: B:161:0x04e6, code:
            android.util.Slog.e(TAG, "Create pipe in restorePackageByZip failed !  asDual: true");
     */
    /* JADX WARNING: Missing block: B:162:0x04ef, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:163:0x04f5, code:
            if (r24 != null) goto L_0x04f7;
     */
    /* JADX WARNING: Missing block: B:165:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:167:0x050d, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:168:0x050e, code:
            if (false != false) goto L_0x0510;
     */
    /* JADX WARNING: Missing block: B:170:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:172:0x051c, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x051e;
     */
    /* JADX WARNING: Missing block: B:173:0x051e, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:175:0x057c, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:176:0x058c, code:
            if (r22 != false) goto L_0x058e;
     */
    /* JADX WARNING: Missing block: B:177:0x058e, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:181:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:182:0x05ad, code:
            if (r38 != null) goto L_0x05af;
     */
    /* JADX WARNING: Missing block: B:185:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:186:0x05b7, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:187:0x05c1, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:188:0x05c2, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:189:0x05c3, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:190:0x05c8, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:194:0x05cf, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:195:0x05d0, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:196:0x05ed, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:197:0x05ee, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:199:?, code:
            r17 = com.vivo.server.backup.VivoBackupReflectUtil.callInitializeAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, 3, true, r7);
     */
    /* JADX WARNING: Missing block: B:200:0x061c, code:
            if (r17 == null) goto L_0x061e;
     */
    /* JADX WARNING: Missing block: B:201:0x061e, code:
            android.util.Slog.e(TAG, "get agent failed,  asDual: true");
     */
    /* JADX WARNING: Missing block: B:202:0x0627, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:203:0x062d, code:
            if (r24 != null) goto L_0x062f;
     */
    /* JADX WARNING: Missing block: B:205:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:207:0x0645, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:208:0x0646, code:
            if (false != false) goto L_0x0648;
     */
    /* JADX WARNING: Missing block: B:210:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:212:0x0654, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x0656;
     */
    /* JADX WARNING: Missing block: B:213:0x0656, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:215:0x06b4, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:216:0x06c4, code:
            if (r22 != false) goto L_0x06c6;
     */
    /* JADX WARNING: Missing block: B:217:0x06c6, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:221:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:222:0x06e5, code:
            if (r38 != null) goto L_0x06e7;
     */
    /* JADX WARNING: Missing block: B:225:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:226:0x06ef, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:227:0x06f9, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:228:0x06fa, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:229:0x06fb, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:230:0x0700, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:234:0x0707, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:235:0x0708, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:236:0x0725, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:237:0x0726, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:239:?, code:
            new java.lang.Thread(new com.vivo.services.backup.VivoBackupManagerService.RestoreZipRunner(r33, r33, r7, r30[0], r34, r17, r37)).start();
     */
    /* JADX WARNING: Missing block: B:240:0x075c, code:
            android.util.Slog.d(TAG, "splitStream start");
            r0.start(30000);
     */
    /* JADX WARNING: Missing block: B:241:0x076c, code:
            if (r22 != false) goto L_0x076e;
     */
    /* JADX WARNING: Missing block: B:242:0x076e, code:
            r31 = vivo.app.backup.utils.BackupZipUtils.splitZipInputStream(r35, r29[1], r30[1], r34, r38, r0);
     */
    /* JADX WARNING: Missing block: B:243:0x0780, code:
            android.util.Slog.d(TAG, "splitStream finish !");
     */
    /* JADX WARNING: Missing block: B:244:0x0789, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:245:0x078c, code:
            if (r24 != null) goto L_0x078e;
     */
    /* JADX WARNING: Missing block: B:247:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:249:0x07a4, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:250:0x07a5, code:
            if (r31 != false) goto L_0x07a7;
     */
    /* JADX WARNING: Missing block: B:252:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:254:0x07b3, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x07b5;
     */
    /* JADX WARNING: Missing block: B:255:0x07b5, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:257:0x0813, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:258:0x0823, code:
            if (r22 != false) goto L_0x0825;
     */
    /* JADX WARNING: Missing block: B:259:0x0825, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:263:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:264:0x0844, code:
            if (r38 != null) goto L_0x0846;
     */
    /* JADX WARNING: Missing block: B:267:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:268:0x084e, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:269:0x0858, code:
            return r31;
     */
    /* JADX WARNING: Missing block: B:272:?, code:
            r31 = vivo.app.backup.utils.BackupZipUtils.splitZipInputStream(r35, r29[1], null, r34, r38, r0);
     */
    /* JADX WARNING: Missing block: B:273:0x086b, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:274:0x086c, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:275:0x0871, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:279:0x0878, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:280:0x0879, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:281:0x0896, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:282:0x0897, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:283:0x08b4, code:
            r20 = move-exception;
     */
    /* JADX WARNING: Missing block: B:285:?, code:
            android.util.Slog.e(TAG, "restorePackageByZip exception " + r34 + ": " + r20.getMessage(), r20);
     */
    /* JADX WARNING: Missing block: B:286:0x08e2, code:
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:287:0x08e8, code:
            if (r24 != null) goto L_0x08ea;
     */
    /* JADX WARNING: Missing block: B:289:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:291:0x0900, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:292:0x0901, code:
            if (false != false) goto L_0x0903;
     */
    /* JADX WARNING: Missing block: B:294:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:296:0x090f, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x0911;
     */
    /* JADX WARNING: Missing block: B:297:0x0911, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:299:0x096f, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:300:0x097f, code:
            if (r22 != false) goto L_0x0981;
     */
    /* JADX WARNING: Missing block: B:301:0x0981, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:305:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:306:0x09a0, code:
            if (r38 != null) goto L_0x09a2;
     */
    /* JADX WARNING: Missing block: B:309:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:310:0x09aa, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:311:0x09b4, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:312:0x09b5, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:313:0x09b6, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:314:0x09bb, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:318:0x09c2, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:319:0x09c3, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:320:0x09e0, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:321:0x09e1, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* JADX WARNING: Missing block: B:322:0x09fe, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:323:0x09ff, code:
            r5 = r4;
            r0.cancel();
     */
    /* JADX WARNING: Missing block: B:324:0x0a03, code:
            if (r24 != null) goto L_0x0a05;
     */
    /* JADX WARNING: Missing block: B:326:?, code:
            android.util.Slog.d(TAG, "wait for agent complete !");
            r24.await(10000, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
    /* JADX WARNING: Missing block: B:328:0x0a1b, code:
            monitor-enter(r33.mParmslist);
     */
    /* JADX WARNING: Missing block: B:329:0x0a1c, code:
            if (r31 != null) goto L_0x0a1e;
     */
    /* JADX WARNING: Missing block: B:331:?, code:
            r31 = r0.getResult();
     */
    /* JADX WARNING: Missing block: B:333:0x0a2a, code:
            if (r33.mParmslist.indexOfKey(r7) >= 0) goto L_0x0a2c;
     */
    /* JADX WARNING: Missing block: B:334:0x0a2c, code:
            r33.mParmslist.remove(r7);
            android.util.Slog.d(TAG, "Finish RestoreByZip " + r34 + ", total: " + r0.getCompleteSize() + ", fd = " + r7 + " , result: " + r31 + " , num = " + r33.mParmslist.size());
     */
    /* JADX WARNING: Missing block: B:336:0x0a8a, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, false, r7);
     */
    /* JADX WARNING: Missing block: B:337:0x0a9a, code:
            if (r22 != false) goto L_0x0a9c;
     */
    /* JADX WARNING: Missing block: B:338:0x0a9c, code:
            com.vivo.server.backup.VivoBackupReflectUtil.callTearDownAgent((com.android.server.backup.Trampoline) android.os.ServiceManager.getService("backup"), r34, true, r7);
     */
    /* JADX WARNING: Missing block: B:342:?, code:
            r38.onError(r34, 0, ERROR_COMMON);
     */
    /* JADX WARNING: Missing block: B:343:0x0abb, code:
            if (r38 != null) goto L_0x0abd;
     */
    /* JADX WARNING: Missing block: B:346:?, code:
            r38.onEnd(r34, 0);
     */
    /* JADX WARNING: Missing block: B:347:0x0ac5, code:
            r33.mWakelock.release();
            android.os.Binder.restoreCallingIdentity(r26);
     */
    /* JADX WARNING: Missing block: B:349:0x0ad0, code:
            r21 = move-exception;
     */
    /* JADX WARNING: Missing block: B:350:0x0ad1, code:
            r21.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:351:0x0ad6, code:
            r31 = false;
     */
    /* JADX WARNING: Missing block: B:355:0x0add, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:356:0x0ade, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + r19);
     */
    /* JADX WARNING: Missing block: B:357:0x0afb, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:358:0x0afc, code:
            android.util.Slog.e(TAG, "cant connect PackageBackupRestoreObserver restore onEnd." + r19);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean restorePackageByZip(String pkgName, ParcelFileDescriptor readFile, boolean dual, String[] cmdList, IPackageBackupRestoreObserver observer) {
        Slog.d(TAG, "\n####################  [ " + pkgName + " ]  ####################");
        if (pkgName == null || readFile == null || observer == null || cmdList == null) {
            Slog.e(TAG, "get null params");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_COMMON);
                } catch (RemoteException e) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e);
                }
            }
            return false;
        } else if (BACKUP_SUPPORT_FEATURE.contains(Integer.valueOf(SUPPORT_BACKUP_ZIP))) {
            this.mContext.enforceCallingPermission("android.permission.BACKUP", "restorePackageByZip");
            Object obj = 1;
            synchronized (this.mParmslist) {
                for (int i = 0; i < this.mParmslist.size(); i++) {
                    if (((BackupRestoreParams) this.mParmslist.valueAt(i)).getCurrentPackage().equals(pkgName)) {
                        Slog.w(TAG, "failed  to restorePackageByZip because other BackupRestoreProcess is running for package:" + pkgName);
                        if (observer != null) {
                            try {
                                observer.onError(pkgName, 0, ERROR_WAIT_PROCESS);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e2);
                            }
                        }
                        return false;
                    }
                }
            }
        } else {
            Slog.e(TAG, "not support feature in restorePackageByZip");
            if (observer != null) {
                try {
                    observer.onError(pkgName, 0, ERROR_NOT_SUPPORT);
                } catch (RemoteException e22) {
                    Slog.e(TAG, "cant connect PackageBackupRestoreObserver onError." + e22);
                }
            }
            return false;
        }
    }

    public boolean isRunningFromVivoBackup(int fd) throws RemoteException {
        synchronized (this.mParmslist) {
            if (this.mParmslist.indexOfKey(fd) < 0) {
                return false;
            }
            return true;
        }
    }

    public boolean isDualPackageEnabled(String pkgName) throws RemoteException {
        return DoubleInstanceUtil.isCloneEnabled(pkgName);
    }

    public boolean enableDualPackage(String pkgName) throws RemoteException {
        if (isDualPackageEnabled(pkgName)) {
            return true;
        }
        return DoubleInstanceUtil.enableClone(pkgName);
    }

    public int getDualUserId() {
        return DoubleInstanceUtil.getDualUserId();
    }

    public boolean startConfirmationUi(final int token, String action, int fd) throws RemoteException {
        final IFullBackupRestoreObserver observer = new FullBackupRestoreObserver(fd);
        new Thread(new Runnable() {
            public void run() {
                try {
                    ((Trampoline) ServiceManager.getService("backup")).acknowledgeFullBackupOrRestore(token, true, "", "", observer);
                } catch (RemoteException e) {
                    Slog.e(VivoBackupManagerService.TAG, "invoke acknowledgeFullBackupOrRestore failed", e);
                }
            }
        }).start();
        return true;
    }

    public void addBackupCompleteSize(long size, int fd) {
        synchronized (this.mParmslist) {
            BackupRestoreParams params = (BackupRestoreParams) this.mParmslist.get(fd);
            if (params != null) {
                params.setCompleteSize(params.getCompleteSize() + size);
                if (params.getObserver() != null) {
                    try {
                        params.getObserver().onProgress(params.getCurrentPackage(), 0, params.getCompleteSize(), -1);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "cant connect PackageBackupRestoreObserver onProgress.");
                    }
                }
            }
        }
        return;
    }

    public void postRestoreCompleteSize(long size, int fd) {
        synchronized (this.mParmslist) {
            BackupRestoreParams params = (BackupRestoreParams) this.mParmslist.get(fd);
            if (params != null) {
                params.setCompleteSize(size);
                if (params.getObserver() != null) {
                    try {
                        params.getObserver().onProgress(params.getCurrentPackage(), 0, params.getCompleteSize(), -1);
                    } catch (RemoteException e) {
                        Slog.e(TAG, "cant connect PackageBackupRestoreObserver onProgress.");
                    }
                }
            }
        }
        return;
    }

    public void onAgentBackupZipComplete(int fd, long result) {
        Slog.d(TAG, "onAgentBackupZipComplete, fd=" + fd);
        synchronized (this.mParmslist) {
            BackupRestoreParams params = (BackupRestoreParams) this.mParmslist.get(fd);
            if (!(params == null || params.getLatch() == null)) {
                params.getLatch().countDown();
            }
        }
    }

    public void onAgentRestoreZipComplete(int fd, long result) {
        Slog.d(TAG, "onAgentRestoreZipComplete, fd=" + fd);
        synchronized (this.mParmslist) {
            BackupRestoreParams params = (BackupRestoreParams) this.mParmslist.get(fd);
            if (!(params == null || params.getLatch() == null)) {
                params.getLatch().countDown();
            }
        }
    }

    public String[] getRestoreCmdList(int fd) {
        synchronized (this.mParmslist) {
            BackupRestoreParams params = (BackupRestoreParams) this.mParmslist.get(fd);
            if (params != null) {
                String[] cmdList = params.getCmdList();
                return cmdList;
            }
            return null;
        }
    }

    public int generateRandomIntegerToken() {
        int token = this.mTokenGenerator.nextInt();
        if (token < 0) {
            token = -token;
        }
        return (token & -256) | (this.mNextToken.incrementAndGet() & 255);
    }
}
