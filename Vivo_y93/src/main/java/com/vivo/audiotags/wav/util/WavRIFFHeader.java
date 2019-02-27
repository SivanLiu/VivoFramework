package com.vivo.audiotags.wav.util;

public class WavRIFFHeader {
    private boolean isValid = false;

    public WavRIFFHeader(byte[] b) {
        String RIFF = new String(b, 0, 4);
        String WAVE = new String(b, 8, 4);
        if (RIFF.equals("RIFF") && WAVE.equals("WAVE")) {
            this.isValid = true;
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String toString() {
        return "RIFF-WAVE Header:\n" + "Is valid?: " + this.isValid;
    }
}
