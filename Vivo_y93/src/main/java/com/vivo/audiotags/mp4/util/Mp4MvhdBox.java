package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.Utils;

public class Mp4MvhdBox {
    private long timeLength;
    private int timeScale;
    private byte version;

    public Mp4MvhdBox(byte[] raw) {
        this.version = raw[0];
        if (this.version == (byte) 1) {
            this.timeScale = Utils.getNumberBigEndian(raw, 20, 23);
            this.timeLength = Utils.getLongNumberBigEndian(raw, 24, 31);
            return;
        }
        this.timeScale = Utils.getNumberBigEndian(raw, 12, 15);
        this.timeLength = (long) Utils.getNumberBigEndian(raw, 16, 19);
    }

    public int getLength() {
        return (int) (this.timeLength / ((long) this.timeScale));
    }
}
