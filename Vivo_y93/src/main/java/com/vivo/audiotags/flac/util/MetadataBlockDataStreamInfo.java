package com.vivo.audiotags.flac.util;

public class MetadataBlockDataStreamInfo {
    private int bitsPerSample;
    private int channelNumber;
    private boolean isValid = true;
    private float length;
    private int samplingRate;

    public MetadataBlockDataStreamInfo(byte[] b) {
        if (b.length < 19) {
            this.isValid = false;
            return;
        }
        this.samplingRate = readSamplingRate(b[10], b[11], b[12]);
        this.channelNumber = ((u(b[12]) & 14) >>> 1) + 1;
        this.samplingRate /= this.channelNumber;
        this.bitsPerSample = (((u(b[12]) & 1) << 4) + ((u(b[13]) & 240) >>> 4)) + 1;
        this.length = (float) (((double) readSampleNumber(b[13], b[14], b[15], b[16], b[17])) / ((double) this.samplingRate));
    }

    public int getLength() {
        return (int) this.length;
    }

    public float getPreciseLength() {
        return this.length;
    }

    public int getChannelNumber() {
        return this.channelNumber;
    }

    public int getSamplingRate() {
        return this.samplingRate;
    }

    public String getEncodingType() {
        return "FLAC " + this.bitsPerSample + " bits";
    }

    public boolean isValid() {
        return this.isValid;
    }

    private int readSamplingRate(byte b1, byte b2, byte b3) {
        return (((u(b3) & 240) >>> 3) + (u(b2) << 5)) + (u(b1) << 13);
    }

    private int readSampleNumber(byte b1, byte b2, byte b3, byte b4, byte b5) {
        return (((u(b5) + (u(b4) << 8)) + (u(b3) << 16)) + (u(b2) << 24)) + ((u(b1) & 15) << 0);
    }

    private int u(int i) {
        return i & 255;
    }
}
