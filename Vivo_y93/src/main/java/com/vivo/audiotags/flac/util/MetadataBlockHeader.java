package com.vivo.audiotags.flac.util;

public class MetadataBlockHeader {
    public static final int APPLICATION = 2;
    public static final int CUESHEET = 5;
    public static final int PADDING = 1;
    public static final int SEEKTABLE = 3;
    public static final int STREAMINFO = 0;
    public static final int UNKNOWN = 6;
    public static final int VORBIS_COMMENT = 4;
    private int blockType;
    private byte[] bytes;
    private int dataLength;
    private boolean isLastBlock;

    public MetadataBlockHeader(byte[] b) {
        boolean z;
        if (((b[0] & 128) >>> 7) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isLastBlock = z;
        switch (b[0] & 127) {
            case 0:
                this.blockType = 0;
                break;
            case 1:
                this.blockType = 1;
                break;
            case 2:
                this.blockType = 2;
                break;
            case 3:
                this.blockType = 3;
                break;
            case 4:
                this.blockType = 4;
                break;
            case 5:
                this.blockType = 5;
                break;
            default:
                this.blockType = 6;
                break;
        }
        this.dataLength = ((u(b[1]) << 16) + (u(b[2]) << 8)) + u(b[3]);
        this.bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            this.bytes[i] = b[i];
        }
    }

    private int u(int i) {
        return i & 255;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public int getBlockType() {
        return this.blockType;
    }

    public String getBlockTypeString() {
        switch (this.blockType) {
            case 0:
                return "STREAMINFO";
            case 1:
                return "PADDING";
            case 2:
                return "APPLICATION";
            case 3:
                return "SEEKTABLE";
            case 4:
                return "VORBIS_COMMENT";
            case 5:
                return "CUESHEET";
            default:
                return "UNKNOWN-RESERVED";
        }
    }

    public boolean isLastBlock() {
        return this.isLastBlock;
    }

    public byte[] getBytes() {
        this.bytes[0] = (byte) (this.bytes[0] & 127);
        return this.bytes;
    }
}
