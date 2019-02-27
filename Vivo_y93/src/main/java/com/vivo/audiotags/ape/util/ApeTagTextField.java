package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import java.io.UnsupportedEncodingException;

public class ApeTagTextField extends ApeTagField implements TagTextField {
    private String content;

    public ApeTagTextField(String id, String content) {
        super(id, false);
        this.content = content;
    }

    public boolean isEmpty() {
        return this.content.equals("");
    }

    public String toString() {
        return this.content;
    }

    public void copyContent(TagField field) {
        if (field instanceof ApeTagTextField) {
            this.content = ((ApeTagTextField) field).getContent();
        }
    }

    public String getContent() {
        return this.content;
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public void setEncoding(String s) {
    }

    public void setContent(String s) {
        this.content = s;
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        byte[] idBytes = getBytes(getId(), "ISO-8859-1");
        byte[] contentBytes = getBytes(this.content, getEncoding());
        byte[] buf = new byte[(((idBytes.length + 8) + 1) + contentBytes.length)];
        byte[] flags = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0};
        copy(getSize(contentBytes.length), buf, 0);
        copy(flags, buf, 4);
        copy(idBytes, buf, 4 + 4);
        int offset = idBytes.length + 8;
        buf[offset] = (byte) 0;
        offset++;
        copy(contentBytes, buf, offset);
        offset += contentBytes.length;
        return buf;
    }
}
