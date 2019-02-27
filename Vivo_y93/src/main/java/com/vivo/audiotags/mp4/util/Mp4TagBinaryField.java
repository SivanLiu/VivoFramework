package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.Utils;
import java.io.UnsupportedEncodingException;

public class Mp4TagBinaryField extends Mp4TagField {
    protected byte[] dataBytes;
    protected boolean isBinary = false;

    public Mp4TagBinaryField(String id) {
        super(id);
    }

    public Mp4TagBinaryField(String id, byte[] raw) throws UnsupportedEncodingException {
        super(id, raw);
    }

    public byte[] getRawContent() {
        int i = 0;
        byte[] data = this.dataBytes;
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

    protected void build(byte[] raw) {
        boolean z = false;
        int dataSize = Utils.getNumberBigEndian(raw, 0, 3);
        this.dataBytes = new byte[(dataSize - 16)];
        for (int i = 16; i < dataSize; i++) {
            this.dataBytes[i - 16] = raw[i];
        }
        if ((raw[11] & 1) == 0) {
            z = true;
        }
        this.isBinary = z;
    }

    public boolean isBinary() {
        return this.isBinary;
    }

    public boolean isEmpty() {
        return this.dataBytes.length == 0;
    }

    public byte[] getData() {
        return this.dataBytes;
    }

    public void setData(byte[] d) {
        this.dataBytes = d;
    }

    public void copyContent(TagField field) {
        if (field instanceof Mp4TagBinaryField) {
            this.dataBytes = ((Mp4TagBinaryField) field).getData();
            this.isBinary = ((Mp4TagBinaryField) field).isBinary();
        }
    }
}
