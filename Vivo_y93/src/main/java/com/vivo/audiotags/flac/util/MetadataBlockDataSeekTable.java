package com.vivo.audiotags.flac.util;

public class MetadataBlockDataSeekTable implements MetadataBlockData {
    private byte[] data;

    public MetadataBlockDataSeekTable(byte[] b) {
        this.data = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            this.data[i] = b[i];
        }
    }

    public byte[] getBytes() {
        return this.data;
    }

    public int getLength() {
        return this.data.length;
    }
}
