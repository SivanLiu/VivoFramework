package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import com.vivo.audiotags.generic.Utils;
import java.io.UnsupportedEncodingException;

public class Mp4TagTextField extends Mp4TagField implements TagTextField {
    protected String content;

    public Mp4TagTextField(String id, byte[] raw) throws UnsupportedEncodingException {
        super(id, raw);
    }

    public Mp4TagTextField(String id, String content) {
        super(id);
        this.content = content;
    }

    protected void build(byte[] raw) throws UnsupportedEncodingException {
        this.content = Utils.getString(raw, 16, (Utils.getNumberBigEndian(raw, 0, 3) - 8) - 8, getEncoding());
    }

    public void copyContent(TagField field) {
        if (field instanceof Mp4TagTextField) {
            this.content = ((Mp4TagTextField) field).getContent();
        }
    }

    public String getContent() {
        return this.content;
    }

    protected byte[] getDataBytes() throws UnsupportedEncodingException {
        return this.content.getBytes(getEncoding());
    }

    public String getEncoding() {
        return "ISO-8859-1";
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        int i = 0;
        byte[] data = getDataBytes();
        byte[] b = new byte[(data.length + 24)];
        Utils.copy(Utils.getSizeBigEndian(b.length), b, 0);
        Utils.copy(Utils.getDefaultBytes(getId()), b, 4);
        int offset = 4 + 4;
        Utils.copy(Utils.getSizeBigEndian(data.length + 16), b, offset);
        offset += 4;
        Utils.copy(Utils.getDefaultBytes("data"), b, offset);
        offset += 4;
        byte[] bArr = new byte[4];
        bArr[0] = (byte) 0;
        bArr[1] = (byte) 0;
        bArr[2] = (byte) 0;
        if (!isBinary()) {
            i = 1;
        }
        bArr[3] = (byte) i;
        Utils.copy(bArr, b, offset);
        offset += 4;
        Utils.copy(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}, b, offset);
        Utils.copy(data, b, offset + 4);
        offset = data.length + 24;
        return b;
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isEmpty() {
        return this.content.trim().equals("");
    }

    public void setContent(String s) {
        this.content = s;
    }

    public void setEncoding(String s) {
    }

    public String toString() {
        return this.content;
    }
}
