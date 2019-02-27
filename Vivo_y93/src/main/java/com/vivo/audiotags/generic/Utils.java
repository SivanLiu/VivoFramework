package com.vivo.audiotags.generic;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class Utils {
    public static void copy(byte[] src, byte[] dst, int dstOffset) {
        System.arraycopy(src, 0, dst, dstOffset, src.length);
    }

    public static byte[] getDefaultBytes(String s) {
        return s.getBytes();
    }

    public static String getExtension(File f) {
        String name = f.getName().toLowerCase();
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return "";
        }
        return name.substring(i + 1);
    }

    public static long getLongNumber(byte[] b, int start, int end) {
        long number = 0;
        for (int i = 0; i < (end - start) + 1; i++) {
            number += (long) ((b[start + i] & 255) << (i * 8));
        }
        return number;
    }

    public static long getLongNumberBigEndian(byte[] b, int start, int end) {
        int number = 0;
        for (int i = 0; i < (end - start) + 1; i++) {
            number += (b[end - i] & 255) << (i * 8);
        }
        return (long) number;
    }

    public static int getNumber(byte[] b, int start, int end) {
        return (int) getLongNumber(b, start, end);
    }

    public static int getNumberBigEndian(byte[] b, int start, int end) {
        return (int) getLongNumberBigEndian(b, start, end);
    }

    public static byte[] getSizeBigEndian(int size) {
        return new byte[]{(byte) ((size >> 24) & 255), (byte) ((size >> 16) & 255), (byte) ((size >> 8) & 255), (byte) (size & 255)};
    }

    public static String getString(byte[] b, int offset, int length) {
        return new String(b, offset, length);
    }

    public static String getString(byte[] b, int offset, int length, String encoding) throws UnsupportedEncodingException {
        return new String(b, offset, length, encoding);
    }

    public static byte[] getUTF8Bytes(String s) throws UnsupportedEncodingException {
        return s.getBytes("UTF-8");
    }
}
