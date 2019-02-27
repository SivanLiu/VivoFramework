package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.generic.Utils;

public class MonkeyDescriptor {
    byte[] b;

    public MonkeyDescriptor(byte[] b) {
        this.b = b;
    }

    public int getRiffWavOffset() {
        return (getDescriptorLength() + getHeaderLength()) + getSeekTableLength();
    }

    public int getDescriptorLength() {
        return Utils.getNumber(this.b, 0, 3);
    }

    public int getHeaderLength() {
        return Utils.getNumber(this.b, 4, 7);
    }

    public int getSeekTableLength() {
        return Utils.getNumber(this.b, 8, 11);
    }

    public int getRiffWavLength() {
        return Utils.getNumber(this.b, 12, 15);
    }

    public long getApeFrameDataLength() {
        return Utils.getLongNumber(this.b, 16, 19);
    }

    public long getApeFrameDataHighLength() {
        return Utils.getLongNumber(this.b, 20, 23);
    }

    public int getTerminatingDataLength() {
        return Utils.getNumber(this.b, 24, 27);
    }
}
