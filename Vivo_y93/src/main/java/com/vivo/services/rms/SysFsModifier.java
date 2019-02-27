package com.vivo.services.rms;

import android.util.Log;
import com.vivo.services.rms.sdk.Consts.ProcessStates;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import libcore.io.IoUtils;

public class SysFsModifier {
    private static final byte[] BUFFER = new byte[ProcessStates.HASSERVICE];
    private static final Object LOCK = new Object();
    private static final HashMap<String, String> RESTOREMAP = new HashMap();
    private static final String TAG = "SysFsModifier";

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:11:0x0017, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean modify(ArrayList<String> fileNames, ArrayList<String> values) {
        synchronized (LOCK) {
            if (fileNames == null || values == null) {
            } else {
                int size = fileNames.size();
                if (values.size() != size || size <= 0) {
                } else {
                    int i = 0;
                    while (i < size) {
                        if (modifyLocked((String) fileNames.get(i), (String) values.get(i))) {
                            i++;
                        } else {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
    }

    public static boolean modifyLocked(String fileName, String value) {
        if (fileName == null || value == null) {
            return false;
        }
        File file = new File(fileName);
        if (file.exists()) {
            String oldValue = readSysFs(file);
            if (oldValue != null) {
                if (oldValue.equals(value)) {
                    return true;
                }
                if (writeSysFs(file, value)) {
                    if (!RESTOREMAP.containsKey(fileName)) {
                        RESTOREMAP.put(fileName, oldValue);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static void restore(ArrayList<String> fileNames) {
        Throwable th;
        synchronized (LOCK) {
            File file = null;
            try {
                ArrayList<String> removes = new ArrayList();
                Iterator fileName$iterator = fileNames.iterator();
                while (true) {
                    File file2;
                    try {
                        file2 = file;
                        if (!fileName$iterator.hasNext()) {
                            break;
                        }
                        String fileName = (String) fileName$iterator.next();
                        if (RESTOREMAP.containsKey(fileName)) {
                            file = new File(fileName);
                            if (file.exists() && writeSysFs(file, (String) RESTOREMAP.get(fileName))) {
                                removes.add(fileName);
                            }
                        } else {
                            file = file2;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        file = file2;
                        throw th;
                    }
                }
                for (String key : removes) {
                    RESTOREMAP.remove(key);
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x0094, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void restore() {
        File file;
        Throwable th;
        synchronized (LOCK) {
            File file2 = null;
            try {
                String fileName;
                ArrayList<String> removes = new ArrayList();
                Iterator fileName$iterator = RESTOREMAP.keySet().iterator();
                while (true) {
                    try {
                        file = file2;
                        if (!fileName$iterator.hasNext()) {
                            break;
                        }
                        fileName = (String) fileName$iterator.next();
                        file2 = new File(fileName);
                        if (file2.exists() && writeSysFs(file2, (String) RESTOREMAP.get(fileName))) {
                            removes.add(fileName);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        file2 = file;
                        throw th;
                    }
                }
                for (String key : removes) {
                    RESTOREMAP.remove(key);
                }
                if (!RESTOREMAP.isEmpty()) {
                    for (String fileName2 : RESTOREMAP.keySet()) {
                        Log.e(TAG, String.format("restore fail  %s %s", new Object[]{fileName2, RESTOREMAP.get(fileName2)}));
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    private static String readSysFs(File file) {
        Exception e;
        Object in;
        Throwable th;
        AutoCloseable in2 = null;
        String result = null;
        try {
            FileInputStream in3 = new FileInputStream(file);
            try {
                int len = in3.read(BUFFER);
                if (len > 0) {
                    result = StringFactory.newStringFromBytes(BUFFER, 0, len);
                }
                IoUtils.closeQuietly(in3);
                FileInputStream fileInputStream = in3;
            } catch (Exception e2) {
                e = e2;
                in2 = in3;
                try {
                    Log.e(TAG, String.format("readSysFs fail %s %s", new Object[]{file.getName(), e.toString()}));
                    IoUtils.closeQuietly(in2);
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(in2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in2 = in3;
                IoUtils.closeQuietly(in2);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, String.format("readSysFs fail %s %s", new Object[]{file.getName(), e.toString()}));
            IoUtils.closeQuietly(in2);
            return result;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean writeSysFs(File file, String value) {
        Exception e;
        Throwable th;
        PrintWriter pw = null;
        boolean result = false;
        try {
            PrintWriter pw2 = new PrintWriter(new FileOutputStream(file));
            try {
                pw2.write(value);
                result = true;
                if (pw2 != null) {
                    pw2.close();
                }
                pw = pw2;
            } catch (Exception e2) {
                e = e2;
                pw = pw2;
                try {
                    Log.e(TAG, String.format("writeSysFs fail %s %s", new Object[]{file.getName(), e.toString()}));
                    if (pw != null) {
                        pw.close();
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (pw != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                pw = pw2;
                if (pw != null) {
                    pw.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, String.format("writeSysFs fail %s %s", new Object[]{file.getName(), e.toString()}));
            if (pw != null) {
            }
            return result;
        }
        return result;
    }
}
