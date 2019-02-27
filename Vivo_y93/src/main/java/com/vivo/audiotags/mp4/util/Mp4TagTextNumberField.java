package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.Utils;
import java.io.UnsupportedEncodingException;

public class Mp4TagTextNumberField extends Mp4TagTextField {
    public Mp4TagTextNumberField(String id, String n) {
        super(id, n);
    }

    public Mp4TagTextNumberField(String id, byte[] raw) throws UnsupportedEncodingException {
        super(id, raw);
    }

    protected byte[] getDataBytes() {
        return Utils.getSizeBigEndian(Integer.parseInt(this.content));
    }

    protected void build(byte[] raw) throws UnsupportedEncodingException {
        this.content = Utils.getNumberBigEndian(raw, 16, 19) + "";
    }
}
