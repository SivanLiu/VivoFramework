package com.vivo.common;

import android.os.FileUtils;
import android.security.keystore.KeyProperties;
import android.telecom.Logging.Session;
import android.telephony.SubscriptionPlan;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VivoCloudFile {
    private static final long MAX_SIZE = 1048576;
    private static final int RESERVED_SPACE = 104857600;
    private static final String ROOT_DIR = "/data/logData/modules/";
    private static final String TAG = ToolUtils.makeTag("CloudFile");

    public static void setMaxSize(long size) {
    }

    public static final String write(int moduleId, int extype, int subtype, String content) {
        Exception e;
        Throwable th;
        if (ToolUtils.isEmpty(content)) {
            return "-1";
        }
        if (checkDir(moduleId) < 0) {
            return "-2";
        }
        try {
            byte[] buff = content.getBytes(Charset.forName("UTF-8"));
            if (((long) buff.length) > MAX_SIZE) {
                return "-3";
            }
            StringBuffer fn = new StringBuffer();
            fn.append(extype).append(Session.SESSION_SEPARATION_CHAR_CHILD);
            fn.append(subtype).append(Session.SESSION_SEPARATION_CHAR_CHILD);
            fn.append(computeStrHash(content));
            fn.append("@").append(System.currentTimeMillis()).append(".info");
            File targetFile = new File(ROOT_DIR + moduleId, fn.toString());
            OutputStream fos = null;
            try {
                OutputStream fos2 = new BufferedOutputStream(new FileOutputStream(targetFile));
                try {
                    fos2.write(buff);
                    fos2.flush();
                    FileUtils.setPermissions(targetFile, 511, -1, -1);
                    String absolutePath = targetFile.getAbsolutePath();
                    if (fos2 != null) {
                        fos2.close();
                    }
                    return absolutePath;
                } catch (Exception e2) {
                    e = e2;
                    fos = fos2;
                } catch (Throwable th2) {
                    th = th2;
                    fos = fos2;
                    if (fos != null) {
                        fos.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                try {
                    throw e;
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } catch (Throwable e4) {
            Log.w(TAG, e4);
            return null;
        }
    }

    public static final String write(int moduleId, String extype, String subtype, String content) {
        Exception e;
        Throwable th;
        if (ToolUtils.isEmpty(content)) {
            return "-1";
        }
        if (checkDir(moduleId) < 0) {
            return "-2";
        }
        try {
            byte[] buff = content.getBytes(Charset.forName("UTF-8"));
            if (((long) buff.length) > MAX_SIZE) {
                return "-3";
            }
            StringBuffer fn = new StringBuffer();
            fn.append(extype).append(Session.SESSION_SEPARATION_CHAR_CHILD);
            fn.append(subtype).append(Session.SESSION_SEPARATION_CHAR_CHILD);
            fn.append(computeStrHash(content));
            fn.append("@").append(System.currentTimeMillis()).append(".info");
            File targetFile = new File(ROOT_DIR + moduleId, fn.toString());
            OutputStream fos = null;
            try {
                OutputStream fos2 = new BufferedOutputStream(new FileOutputStream(targetFile));
                try {
                    fos2.write(buff);
                    fos2.flush();
                    FileUtils.setPermissions(targetFile, 511, -1, -1);
                    String absolutePath = targetFile.getAbsolutePath();
                    if (fos2 != null) {
                        fos2.close();
                    }
                    return absolutePath;
                } catch (Exception e2) {
                    e = e2;
                    fos = fos2;
                } catch (Throwable th2) {
                    th = th2;
                    fos = fos2;
                    if (fos != null) {
                        fos.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                try {
                    throw e;
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } catch (Throwable e4) {
            Log.w(TAG, e4);
            return null;
        }
    }

    public static String computeStrHash(String content) throws NoSuchAlgorithmException {
        MessageDigest complete = MessageDigest.getInstance(KeyProperties.DIGEST_MD5);
        complete.update(content.getBytes());
        StringBuilder sb = new StringBuilder();
        byte[] bits = complete.digest();
        for (int a : bits) {
            int a2;
            if (a2 < 0) {
                a2 += 256;
            }
            if (a2 < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(a2));
        }
        return sb.toString();
    }

    private static int checkDir(int moduleId) {
        if (new File("/data").getFreeSpace() <= 104857600) {
            return -1;
        }
        File modulesDir = new File(ROOT_DIR);
        if (!modulesDir.exists() && (modulesDir.mkdir() ^ 1) != 0) {
            return -1;
        }
        if (modulesDir.isFile() && modulesDir.delete() && (modulesDir.mkdir() ^ 1) != 0) {
            return -1;
        }
        modulesDir = new File(ROOT_DIR);
        FileUtils.setPermissions(modulesDir, 511, -1, -1);
        deleteOldDir(modulesDir);
        File moduleDir = new File(ROOT_DIR, String.valueOf(moduleId));
        if (moduleDir.exists()) {
            deleteOldFile(moduleDir, MAX_SIZE);
        } else if (!moduleDir.mkdir()) {
            return -1;
        }
        return FileUtils.setPermissions(moduleDir, 511, -1, -1);
    }

    private static void deleteOldDir(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            long size = 0;
            long modifyTime = SubscriptionPlan.BYTES_UNLIMITED;
            File oldFile = null;
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.lastModified() < modifyTime) {
                        oldFile = f;
                        modifyTime = f.lastModified();
                    }
                    File[] tmpFiles = f.listFiles();
                    if (tmpFiles == null || tmpFiles.length <= 0) {
                        f.delete();
                    } else {
                        for (File t : tmpFiles) {
                            if (t.isFile()) {
                                size += t.length();
                            } else {
                                f.delete();
                            }
                        }
                    }
                } else {
                    f.delete();
                }
            }
            if (size >= MAX_SIZE && oldFile != null) {
                files = oldFile.listFiles();
                if (files != null) {
                    for (File f2 : files) {
                        f2.delete();
                    }
                }
                oldFile.delete();
            }
        }
    }

    private static void deleteOldFile(File file, long maxSize) {
        int i = 0;
        File[] files = file.listFiles();
        if (files != null) {
            int length;
            File f;
            long size = 0;
            for (File f2 : files) {
                if (f2.isFile()) {
                    size += f2.length();
                } else {
                    f2.delete();
                }
            }
            if (size >= maxSize) {
                File oldFile = null;
                long modifyTime = SubscriptionPlan.BYTES_UNLIMITED;
                length = files.length;
                while (i < length) {
                    f2 = files[i];
                    if (f2.lastModified() < modifyTime) {
                        oldFile = f2;
                        modifyTime = f2.lastModified();
                    }
                    i++;
                }
                if (oldFile != null) {
                    oldFile.delete();
                }
            }
        }
    }
}
