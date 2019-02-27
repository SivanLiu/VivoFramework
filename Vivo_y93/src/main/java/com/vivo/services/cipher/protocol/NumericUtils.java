package com.vivo.services.cipher.protocol;

public class NumericUtils {
    public static int bytesToInt(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data received  must have value");
        } else if (data.length > 4) {
            throw new IllegalArgumentException("Size of data received  must less than 4");
        } else {
            int value = 0;
            for (byte b : data) {
                value = (value << 8) | (b & 255);
            }
            return value;
        }
    }

    public static long bytesToLong(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data received  must have value");
        } else if (data.length > 8) {
            throw new IllegalArgumentException("Size of data received  must less than 8");
        } else {
            long value = 0;
            for (byte b : data) {
                value = (value << 8) | ((long) (b & 255));
            }
            return value;
        }
    }

    public static byte[] shortToBytes(short value) {
        return new byte[]{(byte) (value >>> 8), (byte) value};
    }

    public static byte[] intToBytes(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    public static byte[] longToBytes(long value) {
        return new byte[]{(byte) ((int) (value >>> 56)), (byte) ((int) (value >>> 48)), (byte) ((int) (value >>> 40)), (byte) ((int) (value >>> 32)), (byte) ((int) (value >>> 24)), (byte) ((int) (value >>> 16)), (byte) ((int) (value >>> 8)), (byte) ((int) value)};
    }
}
