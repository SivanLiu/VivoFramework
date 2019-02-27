package com.vivo.audiotags.mp3.util;

public class MPEGFrame {
    public static final int CHANNEL_MODE_DUAL_CHANNEL = 2;
    public static final int CHANNEL_MODE_JOINT_STEREO = 1;
    public static final int CHANNEL_MODE_MONO = 3;
    public static final int CHANNEL_MODE_STEREO = 0;
    public static final int LAYER_I = 3;
    public static final int LAYER_II = 2;
    public static final int LAYER_III = 1;
    public static final int LAYER_RESERVED = 0;
    private static final int[] MPEGVersionTable = new int[]{0, 1, 2, 3};
    private static final String[] MPEGVersionTable_String = new String[]{"MPEG Version 2.5", "reserved", "MPEG Version 2 (ISO/IEC 13818-3)", "MPEG Version 1 (ISO/IEC 11172-3)"};
    public static final int MPEG_VERSION_1 = 3;
    public static final int MPEG_VERSION_2 = 2;
    public static final int MPEG_VERSION_2_5 = 0;
    public static final int MPEG_VERSION_RESERVED = 1;
    private static final int[] SAMPLE_NUMBERS = new int[]{-1, 1152, 1152, 384};
    private static final int[][][] bitrateTable;
    private static final String[] channelModeTable_String = new String[]{"Stereo", "Joint stereo (Stereo)", "Dual channel (2 mono channels)", "Single channel (Mono)"};
    private static final String[] emphasisTable = new String[]{"none", "50/15 ms", "reserved", "CCIT J.17"};
    private static final int[] layerDescriptionTable = new int[]{0, 1, 2, 3};
    private static final String[] layerDescriptionTable_String = new String[]{"reserved", "Layer III", "Layer II", "Layer I"};
    private static final String[][] modeExtensionTable;
    private static final int[][] samplingRateTable = new int[][]{new int[]{44100, 48000, 32000, 0}, new int[]{22050, 24000, 16000, 0}, new int[]{11025, 12000, 8000, 0}};
    private int MPEGVersion;
    private int bitrate;
    private int channelMode;
    private String emphasis;
    private boolean hasPadding;
    private boolean isCopyrighted;
    private boolean isOriginal;
    private boolean isProtected;
    private boolean isValid;
    private int layer;
    private String modeExtension;
    private byte[] mpegBytes;
    private int samplingRate;

    static {
        r0 = new int[2][][];
        r0[0] = new int[][]{new int[]{0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, -1}, new int[]{0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, -1}, new int[]{0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -1}};
        r0[1] = new int[][]{new int[]{0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -1}, new int[]{0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1}, new int[]{0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1}};
        bitrateTable = r0;
        r0 = new String[2][];
        r0[0] = new String[]{"4-31", "8-31", "12-31", "16-31"};
        r0[1] = new String[]{"off-off", "on-off", "off-on", "on-on"};
        modeExtensionTable = r0;
    }

    public MPEGFrame(byte[] b) {
        this.mpegBytes = b;
        if (isMPEGFrame()) {
            this.MPEGVersion = MPEGVersion();
            this.layer = layerDescription();
            this.isProtected = isProtected();
            this.bitrate = bitrate();
            this.samplingRate = samplingRate();
            this.hasPadding = hasPadding();
            this.channelMode = channelMode();
            this.modeExtension = modeExtension();
            this.isCopyrighted = isCopyrighted();
            this.isOriginal = isOriginal();
            this.emphasis = emphasis();
            this.isValid = true;
        } else {
            this.isValid = false;
        }
        this.mpegBytes = null;
    }

    public int getBitrate() {
        return this.bitrate;
    }

    public int getChannelNumber() {
        switch (this.channelMode) {
            case 0:
                return 2;
            case 1:
                return 2;
            case 2:
                return 2;
            case 3:
                return 1;
            default:
                return 0;
        }
    }

    public int getChannelMode() {
        return this.channelMode;
    }

    public int getLayerVersion() {
        return this.layer;
    }

    public int getMPEGVersion() {
        return this.MPEGVersion;
    }

    public int getPaddingLength() {
        if (this.hasPadding && this.layer != 3) {
            return 1;
        }
        if (this.hasPadding && this.layer == 3) {
            return 4;
        }
        return 0;
    }

    public int getSamplingRate() {
        return this.samplingRate;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public int getFrameLength() {
        if (this.layer == 3) {
            return ((((getBitrate() * 1000) * 12) / getSamplingRate()) + getPaddingLength()) * 4;
        }
        return (((getBitrate() * 1000) * 144) / getSamplingRate()) + getPaddingLength();
    }

    public int getSampleNumber() {
        return SAMPLE_NUMBERS[this.layer];
    }

    public String MPEGVersionToString(int i) {
        return MPEGVersionTable_String[i];
    }

    public String channelModeToString(int i) {
        return channelModeTable_String[i];
    }

    public String layerToString(int i) {
        return layerDescriptionTable_String[i];
    }

    public String toString() {
        return (((("\n----MPEGFrame--------------------\n" + "MPEG Version: " + MPEGVersionToString(this.MPEGVersion) + "\tLayer: " + layerToString(this.layer) + "\n") + "Bitrate: " + this.bitrate + "\tSamp.Freq.: " + this.samplingRate + "\tChan.Mode: " + channelModeToString(this.channelMode) + "\n") + "Mode Extension: " + this.modeExtension + "\tEmphasis: " + this.emphasis + "\n") + "Padding? " + this.hasPadding + "\tProtected? " + this.isProtected + "\tCopyright? " + this.isCopyrighted + "\tOriginal? " + this.isOriginal + "\n") + "--------------------------------";
    }

    private boolean isCopyrighted() {
        return (this.mpegBytes[3] & 8) == 8;
    }

    private boolean isMPEGFrame() {
        return (this.mpegBytes[0] & 255) == 255 && (this.mpegBytes[1] & 224) == 224;
    }

    private boolean isOriginal() {
        return (this.mpegBytes[3] & 4) == 4;
    }

    private boolean isProtected() {
        return (this.mpegBytes[1] & 1) == 0;
    }

    private int MPEGVersion() {
        return MPEGVersionTable[(this.mpegBytes[1] & 24) >>> 3];
    }

    private int bitrate() {
        int index2;
        int index3 = (this.mpegBytes[2] & 240) >>> 4;
        int index1 = this.MPEGVersion == 3 ? 0 : 1;
        if (this.layer == 3) {
            index2 = 0;
        } else if (this.layer == 2) {
            index2 = 1;
        } else {
            index2 = 2;
        }
        return bitrateTable[index1][index2][index3];
    }

    private int channelMode() {
        return (this.mpegBytes[3] & 192) >>> 6;
    }

    private String emphasis() {
        return emphasisTable[this.mpegBytes[3] & 3];
    }

    private boolean hasPadding() {
        return (this.mpegBytes[2] & 2) == 2;
    }

    private int layerDescription() {
        return layerDescriptionTable[(this.mpegBytes[1] & 6) >>> 1];
    }

    private String modeExtension() {
        return modeExtensionTable[this.layer == 1 ? 1 : 0][(this.mpegBytes[3] & 48) >>> 4];
    }

    private int samplingRate() {
        int index1;
        int index2 = (this.mpegBytes[2] & 12) >>> 2;
        if (this.MPEGVersion == 3) {
            index1 = 0;
        } else if (this.MPEGVersion == 2) {
            index1 = 1;
        } else {
            index1 = 2;
        }
        return samplingRateTable[index1][index2];
    }
}
