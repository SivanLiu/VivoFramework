package com.vivo.audiotags.ogg.util;

public class VorbisCodecHeader {
    private int audioChannels;
    private int audioSampleRate;
    private int bitrateMaximal;
    private int bitrateMinimal;
    private int bitrateNominal;
    private boolean isValid = false;
    private int vorbisVersion;

    public VorbisCodecHeader(byte[] vorbisData) {
        generateCodecHeader(vorbisData);
    }

    public int getChannelNumber() {
        return this.audioChannels;
    }

    public String getEncodingType() {
        return "Ogg Vorbis Version " + this.vorbisVersion;
    }

    public int getSamplingRate() {
        return this.audioSampleRate;
    }

    public int getNominalBitrate() {
        return this.bitrateNominal;
    }

    public int getMaxBitrate() {
        return this.bitrateMaximal;
    }

    public int getMinBitrate() {
        return this.bitrateMinimal;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public void generateCodecHeader(byte[] b) {
        int packetType = b[0];
        String vorbis = new String(b, 1, 6);
        if (packetType == 1 && vorbis.equals("vorbis")) {
            this.vorbisVersion = ((b[7] + (b[8] << 8)) + (b[9] << 16)) + (b[10] << 24);
            this.audioChannels = u(b[11]);
            this.audioSampleRate = ((u(b[12]) + (u(b[13]) << 8)) + (u(b[14]) << 16)) + (u(b[15]) << 24);
            this.bitrateMinimal = ((u(b[16]) + (u(b[17]) << 8)) + (u(b[18]) << 16)) + (u(b[19]) << 24);
            this.bitrateNominal = ((u(b[20]) + (u(b[21]) << 8)) + (u(b[22]) << 16)) + (u(b[23]) << 24);
            this.bitrateMaximal = ((u(b[24]) + (u(b[25]) << 8)) + (u(b[26]) << 16)) + (u(b[27]) << 24);
            if (b[29] != 0) {
                this.isValid = true;
            }
        }
    }

    private int u(int i) {
        return i & 255;
    }
}
