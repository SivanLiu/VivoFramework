package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.Utils;

public class Mp4Box {
    private String id;
    private int offset;

    public void update(byte[] b) {
        this.offset = Utils.getNumberBigEndian(b, 0, 3);
        this.id = Utils.getString(b, 4, 4);
    }

    public String getId() {
        return this.id;
    }

    public int getOffset() {
        return this.offset;
    }

    public String toString() {
        return "Box " + this.id + ":" + this.offset;
    }
}
