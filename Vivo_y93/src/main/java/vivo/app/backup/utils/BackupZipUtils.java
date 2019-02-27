package vivo.app.backup.utils;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import vivo.app.VivoFrameworkFactory;
import vivo.app.backup.AbsVivoBackupManager;
import vivo.app.backup.BRTimeoutMonitor;
import vivo.app.backup.BRTimeoutMonitor.OnTimeoutListener;
import vivo.app.backup.IPackageBackupRestoreObserver;

public class BackupZipUtils {
    private static final int BUFSIZE = 32768;
    private static final boolean DEBUG = false;
    public static int ERROR_COMMON = 1;
    public static int ERROR_NOT_SUPPORT = 2;
    public static int ERROR_TIMEOUT = 3;
    public static int ERROR_WAIT_PROCESS = 4;
    private static final long POST_PROGRESS_INTERVAL = 1000;
    private static final String TAG = "BackupZipUtils";

    public static void compress(String srcFilePath, ParcelFileDescriptor pipe, String[] dirs, boolean asDual) {
        Exception e;
        Throwable th;
        File src = new File(srcFilePath);
        if (src.exists()) {
            Closeable closeable = null;
            try {
                Slog.d(TAG, "start compress! , srcFilePath:  " + srcFilePath);
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(pipe.getFileDescriptor()));
                try {
                    String baseDir = src.getName() + File.separator;
                    if (asDual) {
                        baseDir = baseDir + ".1" + File.separator;
                    }
                    byte[] buf = new byte[BUFSIZE];
                    File f;
                    if (dirs == null || dirs.length <= 0) {
                        File[] files = src.listFiles();
                        if (files != null) {
                            for (File f2 : files) {
                                if (!(f2.getName().equals("cache") || f2.getName().equals("code_cache") || !f2.exists())) {
                                    compressbyType(f2, zos, baseDir, buf);
                                }
                            }
                        }
                    } else {
                        for (String s : dirs) {
                            f2 = new File(srcFilePath + File.separator + s);
                            if (f2.exists()) {
                                compressbyType(f2, zos, baseDir, buf);
                            }
                        }
                    }
                    closeSafety(zos);
                    closeSafety(pipe);
                    Slog.d(TAG, "finish compress!");
                } catch (Exception e2) {
                    e = e2;
                    closeable = zos;
                    try {
                        Slog.e(TAG, "compress failed!", e);
                        closeSafety(closeable);
                        closeSafety(pipe);
                        Slog.d(TAG, "finish compress!");
                    } catch (Throwable th2) {
                        th = th2;
                        closeSafety(closeable);
                        closeSafety(pipe);
                        Slog.d(TAG, "finish compress!");
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeable = zos;
                    closeSafety(closeable);
                    closeSafety(pipe);
                    Slog.d(TAG, "finish compress!");
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                Slog.e(TAG, "compress failed!", e);
                closeSafety(closeable);
                closeSafety(pipe);
                Slog.d(TAG, "finish compress!");
            }
        }
    }

    private static void compressbyType(File src, ZipOutputStream zos, String baseDir, byte[] buf) {
        if (src.isFile()) {
            compressFile(src, zos, baseDir, buf);
        } else if (src.isDirectory()) {
            compressDir(src, zos, baseDir, buf);
        }
    }

    private static void compressFile(File file, ZipOutputStream zos, String baseDir, byte[] buf) {
        Throwable th;
        Closeable bis = null;
        try {
            BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));
            try {
                zos.putNextEntry(new ZipEntry(baseDir + file.getName()));
                while (true) {
                    int count = bis2.read(buf);
                    if (count != -1) {
                        zos.write(buf, 0, count);
                    } else {
                        zos.closeEntry();
                        closeSafety(bis2);
                        return;
                    }
                }
            } catch (IOException e) {
                bis = bis2;
                try {
                    Slog.e(TAG, "compressFile failed in BackupZipUtils, at: " + file.getAbsolutePath());
                    closeSafety(bis);
                } catch (Throwable th2) {
                    th = th2;
                    closeSafety(bis);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Object bis3 = bis2;
                closeSafety(bis);
                throw th;
            }
        } catch (IOException e2) {
            Slog.e(TAG, "compressFile failed in BackupZipUtils, at: " + file.getAbsolutePath());
            closeSafety(bis);
        }
    }

    private static void compressDir(File dir, ZipOutputStream zos, String baseDir, byte[] buf) {
        File[] files = dir.listFiles();
        if (files != null) {
            if (files.length == 0) {
                try {
                    zos.putNextEntry(new ZipEntry(baseDir + dir.getName() + File.separator));
                    zos.closeEntry();
                } catch (IOException e) {
                    Slog.e(TAG, "compressDir failed in BackupZipUtils, at: " + dir.getAbsolutePath());
                }
            }
            for (File file : files) {
                compressbyType(file, zos, baseDir + dir.getName() + File.separator, buf);
            }
        }
    }

    public static boolean mergeToZipOutputStream(String pkgName, ParcelFileDescriptor pipe, ZipOutputStream output, IPackageBackupRestoreObserver observer, BRTimeoutMonitor mergeTimeoutMonitor, int fd) {
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(true);
        final ParcelFileDescriptor parcelFileDescriptor = pipe;
        final BRTimeoutMonitor bRTimeoutMonitor = mergeTimeoutMonitor;
        final ZipOutputStream zipOutputStream = output;
        final int i = fd;
        final IPackageBackupRestoreObserver iPackageBackupRestoreObserver = observer;
        final String str = pkgName;
        new Thread(new Runnable() {
            public void run() {
                IOException e;
                Throwable th;
                Closeable closeable = null;
                Slog.d(BackupZipUtils.TAG, "mergeStream start");
                try {
                    byte[] buf = new byte[BackupZipUtils.BUFSIZE];
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
                    try {
                        BRTimeoutMonitor bRTimeoutMonitor = bRTimeoutMonitor;
                        final AtomicBoolean atomicBoolean = result;
                        final CountDownLatch countDownLatch = mLatch;
                        final IPackageBackupRestoreObserver iPackageBackupRestoreObserver = iPackageBackupRestoreObserver;
                        final String str = str;
                        final ParcelFileDescriptor parcelFileDescriptor = parcelFileDescriptor;
                        bRTimeoutMonitor.setOnTimeoutListener(new OnTimeoutListener() {
                            public void onTimeout(int token, String lastFlag, String tag) {
                                atomicBoolean.set(false);
                                countDownLatch.countDown();
                                Slog.d(tag, "======== onTimeout ======== ,token=" + Integer.toHexString(token));
                                if (iPackageBackupRestoreObserver != null) {
                                    try {
                                        iPackageBackupRestoreObserver.onError(str, 0, BackupZipUtils.ERROR_TIMEOUT);
                                    } catch (RemoteException e) {
                                        Slog.e(BackupZipUtils.TAG, "cant connect PackageBackupRestoreObserver onError, Timeout" + e);
                                    }
                                }
                                BackupZipUtils.closeSafety(parcelFileDescriptor);
                            }
                        });
                        long tmp = 0;
                        long lastPostTime = 0;
                        while (true) {
                            ZipEntry entry = zipInputStream.getNextEntry();
                            if (entry == null) {
                                BackupZipUtils.addBackupCompleteSize(tmp, i);
                                mLatch.countDown();
                                BackupZipUtils.closeSafety(zipInputStream);
                                BackupZipUtils.closeSafety(parcelFileDescriptor);
                                Slog.d(BackupZipUtils.TAG, "mergeStream end");
                                ZipInputStream zipInputStream2 = zipInputStream;
                                return;
                            }
                            bRTimeoutMonitor.pulse("/* getEntry */");
                            if (BackupZipUtils.isDir(entry.getName())) {
                                zipOutputStream.putNextEntry(entry);
                                zipOutputStream.closeEntry();
                                bRTimeoutMonitor.pulse("/* putEntry */");
                            } else {
                                zipOutputStream.putNextEntry(entry);
                                bRTimeoutMonitor.pulse("/* putEntry */");
                                while (true) {
                                    int count = zipInputStream.read(buf);
                                    if (count == -1) {
                                        break;
                                    }
                                    bRTimeoutMonitor.pulse("/* [read] */");
                                    zipOutputStream.write(buf, 0, count);
                                    tmp += (long) count;
                                    bRTimeoutMonitor.pulse("/* [write] */");
                                    if (System.currentTimeMillis() - lastPostTime > BackupZipUtils.POST_PROGRESS_INTERVAL) {
                                        BackupZipUtils.addBackupCompleteSize(tmp, i);
                                        tmp = 0;
                                        lastPostTime = System.currentTimeMillis();
                                    }
                                }
                                zipOutputStream.closeEntry();
                            }
                            zipInputStream.closeEntry();
                        }
                    } catch (IOException e2) {
                        e = e2;
                        closeable = zipInputStream;
                        try {
                            Slog.e(BackupZipUtils.TAG, "mergeToZipOutputStream failed!", e);
                            result.set(false);
                            mLatch.countDown();
                            BackupZipUtils.closeSafety(closeable);
                            BackupZipUtils.closeSafety(parcelFileDescriptor);
                            Slog.d(BackupZipUtils.TAG, "mergeStream end");
                        } catch (Throwable th2) {
                            th = th2;
                            mLatch.countDown();
                            BackupZipUtils.closeSafety(closeable);
                            BackupZipUtils.closeSafety(parcelFileDescriptor);
                            Slog.d(BackupZipUtils.TAG, "mergeStream end");
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        closeable = zipInputStream;
                        mLatch.countDown();
                        BackupZipUtils.closeSafety(closeable);
                        BackupZipUtils.closeSafety(parcelFileDescriptor);
                        Slog.d(BackupZipUtils.TAG, "mergeStream end");
                        throw th;
                    }
                } catch (IOException e3) {
                    e = e3;
                    Slog.e(BackupZipUtils.TAG, "mergeToZipOutputStream failed!", e);
                    result.set(false);
                    mLatch.countDown();
                    BackupZipUtils.closeSafety(closeable);
                    BackupZipUtils.closeSafety(parcelFileDescriptor);
                    Slog.d(BackupZipUtils.TAG, "mergeStream end");
                }
            }
        }).start();
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.get();
    }

    public static void deCompress(ParcelFileDescriptor srcPipe, String dest) {
        IOException e;
        Throwable th;
        Closeable closeable = null;
        try {
            Slog.d(TAG, "start decompress! ,  destPath: " + dest);
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(srcPipe.getFileDescriptor()));
            try {
                byte[] buf = new byte[BUFSIZE];
                while (true) {
                    ZipEntry entry = zipInputStream.getNextEntry();
                    if (entry == null) {
                        closeSafety(zipInputStream);
                        closeSafety(srcPipe);
                        Slog.d(TAG, "finish decompress!");
                        ZipInputStream zipInputStream2 = zipInputStream;
                        return;
                    }
                    if (isDir(entry.getName())) {
                        new File(dest + File.separator + entry.getName()).mkdirs();
                    } else {
                        File f = new File(dest + File.separator + entry.getName());
                        if (!f.exists()) {
                            new File(f.getParent()).mkdirs();
                        }
                        Closeable fos = null;
                        try {
                            f.createNewFile();
                            BufferedOutputStream fos2 = new BufferedOutputStream(new FileOutputStream(f));
                            while (true) {
                                try {
                                    int count = zipInputStream.read(buf);
                                    if (count == -1) {
                                        break;
                                    }
                                    fos2.write(buf, 0, count);
                                } catch (IOException e2) {
                                    e = e2;
                                    fos = fos2;
                                    try {
                                        e.printStackTrace();
                                        closeSafety(fos);
                                        zipInputStream.closeEntry();
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    Object fos3 = fos2;
                                    closeSafety(fos);
                                    throw th;
                                }
                            }
                            closeSafety(fos2);
                        } catch (IOException e3) {
                            e = e3;
                            e.printStackTrace();
                            closeSafety(fos);
                            zipInputStream.closeEntry();
                        }
                    }
                    zipInputStream.closeEntry();
                }
            } catch (IOException e4) {
                e = e4;
                closeable = zipInputStream;
            } catch (Throwable th4) {
                th = th4;
                closeable = zipInputStream;
            }
        } catch (IOException e5) {
            e = e5;
            try {
                Slog.e(TAG, "decompress failed !", e);
                closeSafety(closeable);
                closeSafety(srcPipe);
                Slog.d(TAG, "finish decompress!");
            } catch (Throwable th5) {
                th = th5;
                closeSafety(closeable);
                closeSafety(srcPipe);
                Slog.d(TAG, "finish decompress!");
                throw th;
            }
        }
    }

    public static boolean splitZipInputStream(ParcelFileDescriptor srcPipe, ParcelFileDescriptor destPipe1, ParcelFileDescriptor destPipe2, String pkgName, IPackageBackupRestoreObserver observer, BRTimeoutMonitor splitTimeoutMonitor) {
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(true);
        final ParcelFileDescriptor parcelFileDescriptor = srcPipe;
        final ParcelFileDescriptor parcelFileDescriptor2 = destPipe1;
        final ParcelFileDescriptor parcelFileDescriptor3 = destPipe2;
        final BRTimeoutMonitor bRTimeoutMonitor = splitTimeoutMonitor;
        final String str = pkgName;
        final IPackageBackupRestoreObserver iPackageBackupRestoreObserver = observer;
        new Thread(new Runnable() {
            public void run() {
                IOException e;
                Throwable th;
                Object zipInputStream;
                boolean enableDual = false;
                Closeable zipInputStream2 = null;
                ZipOutputStream zipOutputStream1 = null;
                Closeable zipOutputStream2 = null;
                int fd = parcelFileDescriptor.getFd();
                Slog.d(BackupZipUtils.TAG, "splitStream start");
                try {
                    byte[] buf = new byte[BackupZipUtils.BUFSIZE];
                    ZipInputStream zipInputStream3 = new ZipInputStream(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
                    try {
                        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(parcelFileDescriptor2.getFileDescriptor()));
                        try {
                            if (parcelFileDescriptor3 != null) {
                                enableDual = true;
                                zipOutputStream2 = new ZipOutputStream(new FileOutputStream(parcelFileDescriptor3.getFileDescriptor()));
                            }
                            BRTimeoutMonitor bRTimeoutMonitor = bRTimeoutMonitor;
                            final AtomicBoolean atomicBoolean = result;
                            final CountDownLatch countDownLatch = mLatch;
                            final IPackageBackupRestoreObserver iPackageBackupRestoreObserver = iPackageBackupRestoreObserver;
                            final String str = str;
                            final ParcelFileDescriptor parcelFileDescriptor = parcelFileDescriptor2;
                            final ParcelFileDescriptor parcelFileDescriptor2 = parcelFileDescriptor3;
                            bRTimeoutMonitor.setOnTimeoutListener(new OnTimeoutListener() {
                                public void onTimeout(int token, String lastFlag, String tag) {
                                    atomicBoolean.set(false);
                                    countDownLatch.countDown();
                                    Slog.d(tag, "======== onTimeout ======== ,token=" + Integer.toHexString(token));
                                    if (iPackageBackupRestoreObserver != null) {
                                        try {
                                            iPackageBackupRestoreObserver.onError(str, 0, BackupZipUtils.ERROR_TIMEOUT);
                                        } catch (RemoteException e) {
                                            Slog.e(BackupZipUtils.TAG, "cant connect PackageBackupRestoreObserver onError, Timeout" + e);
                                        }
                                    }
                                    BackupZipUtils.closeSafety(parcelFileDescriptor);
                                    BackupZipUtils.closeSafety(parcelFileDescriptor2);
                                }
                            });
                            String prefixForDual = str + File.separator + ".1" + File.separator;
                            String prefix = str + File.separator;
                            long lastPostTime = 0;
                            long mBytes = 0;
                            while (true) {
                                ZipEntry entry = zipInputStream3.getNextEntry();
                                if (entry == null) {
                                    BackupZipUtils.postRestoreCompleteSize(mBytes, fd);
                                    mLatch.countDown();
                                    BackupZipUtils.closeSafety(zipInputStream3);
                                    BackupZipUtils.closeSafety(parcelFileDescriptor);
                                    BackupZipUtils.closeSafety(zipOutputStream);
                                    BackupZipUtils.closeSafety(parcelFileDescriptor2);
                                    BackupZipUtils.closeSafety(zipOutputStream2);
                                    BackupZipUtils.closeSafety(parcelFileDescriptor3);
                                    Slog.d(BackupZipUtils.TAG, "splitStream end");
                                    zipOutputStream1 = zipOutputStream;
                                    ZipInputStream zipInputStream4 = zipInputStream3;
                                    return;
                                }
                                int beginIdx;
                                bRTimeoutMonitor.pulse("/* getEntry */");
                                String entryName = entry.getName();
                                if (enableDual) {
                                    beginIdx = entryName.indexOf(prefixForDual);
                                } else {
                                    beginIdx = -1;
                                }
                                int count;
                                if (beginIdx < 0) {
                                    beginIdx = entryName.indexOf(prefix);
                                    if (beginIdx < 0) {
                                        zipInputStream3.closeEntry();
                                    } else {
                                        entryName = entryName.substring(beginIdx + prefix.length(), entryName.length());
                                        if (entryName.startsWith(".1/")) {
                                            while (true) {
                                                count = zipInputStream3.read(buf);
                                                if (count == -1) {
                                                    break;
                                                }
                                                bRTimeoutMonitor.pulse("/* [read] */");
                                                mBytes += (long) count;
                                                if (System.currentTimeMillis() - lastPostTime > BackupZipUtils.POST_PROGRESS_INTERVAL) {
                                                    BackupZipUtils.postRestoreCompleteSize(mBytes, fd);
                                                    lastPostTime = System.currentTimeMillis();
                                                }
                                            }
                                            zipInputStream3.closeEntry();
                                        } else {
                                            if (entryName.equals("")) {
                                                zipInputStream3.closeEntry();
                                            } else if (BackupZipUtils.isDir(entry.getName())) {
                                                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                                                zipOutputStream.closeEntry();
                                                bRTimeoutMonitor.pulse("/* putEntry */");
                                            } else {
                                                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                                                bRTimeoutMonitor.pulse("/* putEntry */");
                                                while (true) {
                                                    count = zipInputStream3.read(buf);
                                                    if (count == -1) {
                                                        break;
                                                    }
                                                    bRTimeoutMonitor.pulse("/* [read] */");
                                                    zipOutputStream.write(buf, 0, count);
                                                    bRTimeoutMonitor.pulse("/* [write] */");
                                                    mBytes += (long) count;
                                                    if (System.currentTimeMillis() - lastPostTime > BackupZipUtils.POST_PROGRESS_INTERVAL) {
                                                        BackupZipUtils.postRestoreCompleteSize(mBytes, fd);
                                                        lastPostTime = System.currentTimeMillis();
                                                    }
                                                }
                                                zipOutputStream.closeEntry();
                                            }
                                        }
                                    }
                                } else {
                                    entryName = entryName.substring(beginIdx + prefixForDual.length(), entryName.length());
                                    if (entryName.equals("")) {
                                        bRTimeoutMonitor.pulse();
                                        zipInputStream3.closeEntry();
                                    } else if (BackupZipUtils.isDir(entry.getName())) {
                                        zipOutputStream2.putNextEntry(new ZipEntry(entryName));
                                        zipOutputStream2.closeEntry();
                                        bRTimeoutMonitor.pulse("/* putEntry */");
                                    } else {
                                        zipOutputStream2.putNextEntry(new ZipEntry(entryName));
                                        bRTimeoutMonitor.pulse("/* putEntry */");
                                        while (true) {
                                            count = zipInputStream3.read(buf);
                                            if (count == -1) {
                                                break;
                                            }
                                            bRTimeoutMonitor.pulse("/* [read] */");
                                            zipOutputStream2.write(buf, 0, count);
                                            bRTimeoutMonitor.pulse("/* [write] */");
                                            mBytes += (long) count;
                                            if (System.currentTimeMillis() - lastPostTime > BackupZipUtils.POST_PROGRESS_INTERVAL) {
                                                BackupZipUtils.postRestoreCompleteSize(mBytes, fd);
                                                lastPostTime = System.currentTimeMillis();
                                            }
                                        }
                                        zipOutputStream2.closeEntry();
                                    }
                                }
                                zipInputStream3.closeEntry();
                            }
                        } catch (IOException e2) {
                            e = e2;
                            zipOutputStream1 = zipOutputStream;
                            zipInputStream2 = zipInputStream3;
                            try {
                                Slog.e(BackupZipUtils.TAG, "splitZipInputStream failed !", e);
                                result.set(false);
                                mLatch.countDown();
                                BackupZipUtils.closeSafety(zipInputStream2);
                                BackupZipUtils.closeSafety(parcelFileDescriptor);
                                BackupZipUtils.closeSafety(zipOutputStream1);
                                BackupZipUtils.closeSafety(parcelFileDescriptor2);
                                BackupZipUtils.closeSafety(zipOutputStream2);
                                BackupZipUtils.closeSafety(parcelFileDescriptor3);
                                Slog.d(BackupZipUtils.TAG, "splitStream end");
                            } catch (Throwable th2) {
                                th = th2;
                                mLatch.countDown();
                                BackupZipUtils.closeSafety(zipInputStream2);
                                BackupZipUtils.closeSafety(parcelFileDescriptor);
                                BackupZipUtils.closeSafety(zipOutputStream1);
                                BackupZipUtils.closeSafety(parcelFileDescriptor2);
                                BackupZipUtils.closeSafety(zipOutputStream2);
                                BackupZipUtils.closeSafety(parcelFileDescriptor3);
                                Slog.d(BackupZipUtils.TAG, "splitStream end");
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            zipOutputStream1 = zipOutputStream;
                            zipInputStream2 = zipInputStream3;
                            mLatch.countDown();
                            BackupZipUtils.closeSafety(zipInputStream2);
                            BackupZipUtils.closeSafety(parcelFileDescriptor);
                            BackupZipUtils.closeSafety(zipOutputStream1);
                            BackupZipUtils.closeSafety(parcelFileDescriptor2);
                            BackupZipUtils.closeSafety(zipOutputStream2);
                            BackupZipUtils.closeSafety(parcelFileDescriptor3);
                            Slog.d(BackupZipUtils.TAG, "splitStream end");
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        zipInputStream2 = zipInputStream3;
                        Slog.e(BackupZipUtils.TAG, "splitZipInputStream failed !", e);
                        result.set(false);
                        mLatch.countDown();
                        BackupZipUtils.closeSafety(zipInputStream2);
                        BackupZipUtils.closeSafety(parcelFileDescriptor);
                        BackupZipUtils.closeSafety(zipOutputStream1);
                        BackupZipUtils.closeSafety(parcelFileDescriptor2);
                        BackupZipUtils.closeSafety(zipOutputStream2);
                        BackupZipUtils.closeSafety(parcelFileDescriptor3);
                        Slog.d(BackupZipUtils.TAG, "splitStream end");
                    } catch (Throwable th4) {
                        th = th4;
                        zipInputStream2 = zipInputStream3;
                        mLatch.countDown();
                        BackupZipUtils.closeSafety(zipInputStream2);
                        BackupZipUtils.closeSafety(parcelFileDescriptor);
                        BackupZipUtils.closeSafety(zipOutputStream1);
                        BackupZipUtils.closeSafety(parcelFileDescriptor2);
                        BackupZipUtils.closeSafety(zipOutputStream2);
                        BackupZipUtils.closeSafety(parcelFileDescriptor3);
                        Slog.d(BackupZipUtils.TAG, "splitStream end");
                        throw th;
                    }
                } catch (IOException e4) {
                    e = e4;
                    Slog.e(BackupZipUtils.TAG, "splitZipInputStream failed !", e);
                    result.set(false);
                    mLatch.countDown();
                    BackupZipUtils.closeSafety(zipInputStream2);
                    BackupZipUtils.closeSafety(parcelFileDescriptor);
                    BackupZipUtils.closeSafety(zipOutputStream1);
                    BackupZipUtils.closeSafety(parcelFileDescriptor2);
                    BackupZipUtils.closeSafety(zipOutputStream2);
                    BackupZipUtils.closeSafety(parcelFileDescriptor3);
                    Slog.d(BackupZipUtils.TAG, "splitStream end");
                }
            }
        }).start();
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.get();
    }

    private static boolean isDir(String path) {
        return path.endsWith(File.separator);
    }

    public static void closeSafety(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addBackupCompleteSize(long size, int fd) {
        if (VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
            AbsVivoBackupManager vivoBackupManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoBackupManager();
            if (vivoBackupManager == null) {
                Slog.e(TAG, "getVivoBackupManager = null");
                return;
            } else if (vivoBackupManager.isRunningFromVivoBackup(fd)) {
                vivoBackupManager.addBackupCompleteSize(size, fd);
                return;
            } else {
                return;
            }
        }
        Slog.e(TAG, "getFrameworkFactoryImpl = null");
    }

    public static void postRestoreCompleteSize(long size, int fd) {
        if (VivoFrameworkFactory.getFrameworkFactoryImpl() != null) {
            AbsVivoBackupManager vivoBackupManager = VivoFrameworkFactory.getFrameworkFactoryImpl().getVivoBackupManager();
            if (vivoBackupManager == null) {
                Slog.e(TAG, "getVivoBackupManager = null");
                return;
            } else if (vivoBackupManager.isRunningFromVivoBackup(fd)) {
                vivoBackupManager.postRestoreCompleteSize(size, fd);
                return;
            } else {
                return;
            }
        }
        Slog.e(TAG, "getFrameworkFactoryImpl = null");
    }
}
