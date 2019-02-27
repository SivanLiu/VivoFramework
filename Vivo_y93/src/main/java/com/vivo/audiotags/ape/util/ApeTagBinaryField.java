package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public class ApeTagBinaryField extends ApeTagField {
    private byte[] content;

    public ApeTagBinaryField(String id, byte[] content) {
        super(id, true);
        this.content = new byte[content.length];
        for (int i = 0; i < content.length; i++) {
            this.content[i] = content[i];
        }
    }

    public boolean isEmpty() {
        return this.content.length == 0;
    }

    public String toString() {
        return "Binary field";
    }

    public void copyContent(TagField field) {
        if (field instanceof ApeTagBinaryField) {
            this.content = ((ApeTagBinaryField) field).getContent();
        }
    }

    public byte[] getContent() {
        return this.content;
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        byte[] idBytes = getBytes(getId(), "ISO-8859-1");
        byte[] buf = new byte[(((idBytes.length + 8) + 1) + this.content.length)];
        byte[] flags = new byte[]{(byte) 2, (byte) 0, (byte) 0, (byte) 0};
        copy(getSize(this.content.length), buf, 0);
        copy(flags, buf, 4);
        copy(idBytes, buf, 4 + 4);
        int offset = idBytes.length + 8;
        buf[offset] = (byte) 0;
        offset++;
        copy(this.content, buf, offset);
        offset += this.content.length;
        return buf;
    }
}
