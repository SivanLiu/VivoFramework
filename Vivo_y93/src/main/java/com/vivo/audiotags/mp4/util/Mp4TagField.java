package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.Utils;
import java.io.UnsupportedEncodingException;

public abstract class Mp4TagField implements TagField {
    protected String id;

    protected abstract void build(byte[] bArr) throws UnsupportedEncodingException;

    public Mp4TagField(String id) {
        this.id = id;
    }

    public Mp4TagField(String id, byte[] raw) throws UnsupportedEncodingException {
        this(id);
        build(raw);
    }

    public String getId() {
        return this.id;
    }

    public void isBinary(boolean b) {
    }

    public boolean isCommon() {
        return (this.id.equals("ART") || this.id.equals("alb") || this.id.equals("nam") || this.id.equals("trkn") || this.id.equals("day") || this.id.equals("cmt")) ? true : this.id.equals("gen");
    }

    protected byte[] getIdBytes() {
        return Utils.getDefaultBytes(getId());
    }
}
