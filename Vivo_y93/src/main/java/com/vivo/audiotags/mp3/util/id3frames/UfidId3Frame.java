package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public class UfidId3Frame extends Id3Frame {
    private byte[] identifier;
    private String ownerId;

    public UfidId3Frame(byte[] raw, byte version) throws UnsupportedEncodingException {
        super(raw, version);
    }

    protected void populate(byte[] raw) {
        int i = indexOfFirstNull(raw, this.flags.length);
        if (i != -1) {
            this.ownerId = new String(raw, this.flags.length, i - this.flags.length);
        } else {
            this.ownerId = new String(raw, this.flags.length, raw.length - this.flags.length);
            this.identifier = new byte[0];
        }
        this.identifier = new byte[((raw.length - i) - 1)];
        for (int j = 0; j < this.identifier.length; j++) {
            this.identifier[j] = raw[(i + 1) + j];
        }
    }

    protected byte[] build() {
        byte[] own = this.ownerId.getBytes();
        byte[] b = new byte[((((this.flags.length + 8) + own.length) + 1) + this.identifier.length)];
        copy(getIdBytes(), b, 0);
        copy(getSize(b.length - 10), b, 4);
        copy(this.flags, b, 4 + 4);
        int offset = this.flags.length + 8;
        copy(own, b, offset);
        offset += own.length;
        b[offset] = (byte) 0;
        copy(this.identifier, b, offset + 1);
        return b;
    }

    public boolean isBinary() {
        return true;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public byte[] getIdentifier() {
        return this.identifier;
    }

    public String getId() {
        return "UFID";
    }

    public boolean isCommon() {
        return false;
    }

    public void copyContent(TagField field) {
        if (field instanceof UfidId3Frame) {
            this.ownerId = ((UfidId3Frame) field).getOwnerId();
            this.identifier = ((UfidId3Frame) field).getIdentifier();
        }
    }

    public boolean isEmpty() {
        return this.ownerId.equals("") || this.identifier.length == 0;
    }

    public String toString() {
        return "UFID : " + getOwnerId();
    }
}
