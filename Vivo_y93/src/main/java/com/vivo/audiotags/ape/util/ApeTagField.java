package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public abstract class ApeTagField implements TagField {
    private boolean binary;
    private String id;

    public abstract void copyContent(TagField tagField);

    public abstract byte[] getRawContent() throws UnsupportedEncodingException;

    public abstract boolean isEmpty();

    public abstract String toString();

    public ApeTagField(String id, boolean binary) {
        this.id = id;
        this.binary = binary;
    }

    public String getId() {
        return this.id;
    }

    public boolean isBinary() {
        return this.binary;
    }

    public void isBinary(boolean b) {
        this.binary = b;
    }

    public boolean isCommon() {
        return (this.id.equals("Title") || this.id.equals("Album") || this.id.equals("Artist") || this.id.equals("Genre") || this.id.equals("Track") || this.id.equals("Year")) ? true : this.id.equals("Comment");
    }

    protected void copy(byte[] src, byte[] dst, int dstOffset) {
        for (int i = 0; i < src.length; i++) {
            dst[i + dstOffset] = src[i];
        }
    }

    protected byte[] getSize(int size) {
        return new byte[]{(byte) ((-16777216 & size) >> 24), (byte) ((16711680 & size) >> 16), (byte) ((65280 & size) >> 8), (byte) (size & 255)};
    }

    protected byte[] getBytes(String s, String encoding) throws UnsupportedEncodingException {
        return s.getBytes(encoding);
    }
}
