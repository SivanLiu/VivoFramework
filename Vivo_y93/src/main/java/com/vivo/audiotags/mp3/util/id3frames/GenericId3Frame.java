package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public class GenericId3Frame extends Id3Frame {
    private byte[] data;
    private String id;

    public GenericId3Frame(String id, byte[] raw, byte version) throws UnsupportedEncodingException {
        super(raw, version);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public boolean isBinary() {
        return true;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean isCommon() {
        return false;
    }

    public void copyContent(TagField field) {
        if (field instanceof GenericId3Frame) {
            this.data = ((GenericId3Frame) field).getData();
        }
    }

    public boolean isEmpty() {
        return this.data.length == 0;
    }

    protected void populate(byte[] raw) {
        this.data = new byte[(raw.length - this.flags.length)];
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = raw[this.flags.length + i];
        }
    }

    protected byte[] build() {
        byte[] b = new byte[((this.data.length + 8) + this.flags.length)];
        copy(getIdBytes(), b, 0);
        copy(getSize(b.length - 10), b, 4);
        copy(this.flags, b, 4 + 4);
        copy(this.data, b, this.flags.length + 8);
        return b;
    }

    public String toString() {
        return this.id + " : No associated view";
    }
}
