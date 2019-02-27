package com.vivo.audiotags.mp4.util;

import java.io.UnsupportedEncodingException;

public class Mp4TagCoverField extends Mp4TagBinaryField {
    public Mp4TagCoverField() {
        super("covr");
    }

    public Mp4TagCoverField(byte[] raw) throws UnsupportedEncodingException {
        super("covr", raw);
    }

    public boolean isBinary() {
        return true;
    }
}
