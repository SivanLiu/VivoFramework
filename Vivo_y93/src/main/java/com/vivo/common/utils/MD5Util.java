package com.vivo.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class MD5Util {
    private static char[] mMD5Chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getFileMD5String(File file) throws Exception {
        MessageDigest mMessageDigest = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        DigestInputStream dis = new DigestInputStream(fis, mMessageDigest);
        do {
            try {
            } catch (Throwable th) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (dis != null) {
                    dis.close();
                }
            }
        } while (dis.read(new byte[4096]) > 0);
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if (dis != null) {
            dis.close();
        }
        return bufferToHex(mMessageDigest.digest());
    }

    public static String getStringMD5String(String str) throws Exception {
        MessageDigest mMessageDigest = MessageDigest.getInstance("MD5");
        mMessageDigest.update(str.getBytes());
        return bufferToHex(mMessageDigest.digest());
    }

    public static boolean check(String str, String md5) throws Exception {
        if (getStringMD5String(str).equals(md5)) {
            return true;
        }
        return false;
    }

    public static boolean check(File f, String md5) throws Exception {
        if (getFileMD5String(f).equals(md5)) {
            return true;
        }
        return false;
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(n * 2);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = mMD5Chars[(bt & 240) >> 4];
        char c1 = mMD5Chars[bt & 15];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}
