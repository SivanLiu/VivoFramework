package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.generic.Utils;

public class MonkeyHeader {
    byte[] b;

    public MonkeyHeader(byte[] b) {
        this.b = b;
    }

    public int getCompressionLevel() {
        return Utils.getNumber(this.b, 0, 1);
    }

    public int getFormatFlags() {
        return Utils.getNumber(this.b, 2, 3);
    }

    public long getBlocksPerFrame() {
        return Utils.getLongNumber(this.b, 4, 7);
    }

    public long getFinalFrameBlocks() {
        return Utils.getLongNumber(this.b, 8, 11);
    }

    public long getTotalFrames() {
        return Utils.getLongNumber(this.b, 12, 15);
    }

    public int getLength() {
        return ((int) ((((double) getBlocksPerFrame()) * (((double) getTotalFrames()) - 1.0d)) + ((double) getFinalFrameBlocks()))) / getSamplingRate();
    }

    public float getPreciseLength() {
        return (float) (((double) ((getBlocksPerFrame() * (getTotalFrames() - 1)) + getFinalFrameBlocks())) / ((double) getSamplingRate()));
    }

    public int getBitsPerSample() {
        return Utils.getNumber(this.b, 16, 17);
    }

    public int getChannelNumber() {
        return Utils.getNumber(this.b, 18, 19);
    }

    public int getSamplingRate() {
        return Utils.getNumber(this.b, 20, 23);
    }
}
