package com.vivo.audiotags.flac.util;

public class MetadataBlock {
    private MetadataBlockData mbd;
    private MetadataBlockHeader mbh;

    public MetadataBlock(MetadataBlockHeader mbh, MetadataBlockData mbd) {
        this.mbh = mbh;
        this.mbd = mbd;
    }

    public MetadataBlockHeader getHeader() {
        return this.mbh;
    }

    public MetadataBlockData getData() {
        return this.mbd;
    }

    public int getLength() {
        return this.mbh.getDataLength() + 4;
    }
}
