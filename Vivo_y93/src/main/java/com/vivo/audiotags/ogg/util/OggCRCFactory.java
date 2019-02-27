package com.vivo.audiotags.ogg.util;

public class OggCRCFactory {
    private static long[] crc_lookup = new long[256];
    private static boolean init = false;

    public static void init() {
        for (int i = 0; i < 256; i++) {
            long r = (long) (i << 24);
            for (int j = 0; j < 8; j++) {
                if ((2147483648L & r) != 0) {
                    r = (r << 1) ^ 79764919;
                } else {
                    r <<= 1;
                }
            }
            crc_lookup[i] = -1 & r;
        }
        init = true;
    }

    public boolean checkCRC(byte[] data, byte[] crc) {
        return new String(crc).equals(new String(computeCRC(data)));
    }

    public static byte[] computeCRC(byte[] data) {
        if (!init) {
            init();
        }
        long crc_reg = 0;
        for (byte u : data) {
            crc_reg = ((crc_reg << 8) ^ crc_lookup[(int) (((crc_reg >>> 24) & 255) ^ ((long) u(u)))]) & -1;
        }
        return new byte[]{(byte) ((int) (crc_reg & 255)), (byte) ((int) ((crc_reg >>> 8) & 255)), (byte) ((int) ((crc_reg >>> 16) & 255)), (byte) ((int) ((crc_reg >>> 24) & 255))};
    }

    private static int u(int n) {
        return n & 255;
    }
}
