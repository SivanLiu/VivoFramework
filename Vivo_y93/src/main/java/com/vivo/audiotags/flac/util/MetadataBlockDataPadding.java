package com.vivo.audiotags.flac.util;

public class MetadataBlockDataPadding implements MetadataBlockData {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f2-assertionsDisabled = (MetadataBlockDataPadding.class.desiredAssertionStatus() ^ 1);
    private int length;

    public MetadataBlockDataPadding(int length) {
        this.length = length;
    }

    public byte[] getBytes() {
        if (f2-assertionsDisabled) {
            return null;
        }
        throw new AssertionError();
    }

    public int getLength() {
        return this.length;
    }
}
