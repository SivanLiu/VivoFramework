package com.vivo.audiotags.wav.util;

public class WavFormatHeader {
    private int bitrate;
    private int bytesPerSecond;
    private int channels;
    private boolean isValid = false;
    private int sampleRate;

    public WavFormatHeader(byte[] b) {
        if (new String(b, 0, 3).equals("fmt") && b[8] == (byte) 1) {
            this.channels = b[10];
            this.sampleRate = (((u(b[15]) * 16777216) + (u(b[14]) * 65536)) + (u(b[13]) * 256)) + u(b[12]);
            this.bytesPerSecond = (((u(b[19]) * 16777216) + (u(b[18]) * 65536)) + (u(b[17]) * 256)) + u(b[16]);
            this.bitrate = u(b[22]);
            this.isValid = true;
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    public int getChannelNumber() {
        return this.channels;
    }

    public int getSamplingRate() {
        return this.sampleRate;
    }

    public int getBytesPerSecond() {
        return this.bytesPerSecond;
    }

    public int getBitrate() {
        return this.bitrate;
    }

    private int u(int n) {
        return n & 255;
    }

    public String toString() {
        return "RIFF-WAVE Header:\n" + "Is valid?: " + this.isValid;
    }
}
