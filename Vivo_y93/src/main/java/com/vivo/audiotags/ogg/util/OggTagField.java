package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import java.io.UnsupportedEncodingException;

public class OggTagField implements TagTextField {
    private boolean common;
    private String content;
    private String id;

    public OggTagField(byte[] raw) throws UnsupportedEncodingException {
        String field = new String(raw, "UTF-8");
        String[] splitField = field.split("=");
        if (splitField.length > 1) {
            this.id = splitField[0].toUpperCase();
            this.content = splitField[1];
        } else {
            int i = field.indexOf("=");
            if (i != -1) {
                this.id = field.substring(0, i + 1);
                this.content = "";
            } else {
                this.id = "ERRONEOUS";
                this.content = field;
            }
        }
        checkCommon();
    }

    public OggTagField(String fieldId, String fieldContent) {
        this.id = fieldId.toUpperCase();
        this.content = fieldContent;
        checkCommon();
    }

    private void checkCommon() {
        boolean z;
        if (this.id.equals("TITLE") || this.id.equals("ALBUM") || this.id.equals("ARTIST") || this.id.equals("GENRE") || this.id.equals("TRACKNUMBER") || this.id.equals("DATE") || this.id.equals("DESCRIPTION") || this.id.equals("COMMENT")) {
            z = true;
        } else {
            z = this.id.equals("TRACK");
        }
        this.common = z;
    }

    protected void copy(byte[] src, byte[] dst, int dstOffset) {
        System.arraycopy(src, 0, dst, dstOffset, src.length);
    }

    public void copyContent(TagField field) {
        if (field instanceof TagTextField) {
            this.content = ((TagTextField) field).getContent();
        }
    }

    protected byte[] getBytes(String s, String encoding) throws UnsupportedEncodingException {
        return s.getBytes(encoding);
    }

    public String getContent() {
        return this.content;
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public String getId() {
        return this.id;
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        byte[] size = new byte[4];
        byte[] idBytes = this.id.getBytes();
        byte[] contentBytes = getBytes(this.content, "UTF-8");
        byte[] b = new byte[(((idBytes.length + 4) + 1) + contentBytes.length)];
        int length = (idBytes.length + 1) + contentBytes.length;
        size[3] = (byte) ((-16777216 & length) >> 24);
        size[2] = (byte) ((16711680 & length) >> 16);
        size[1] = (byte) ((65280 & length) >> 8);
        size[0] = (byte) (length & 255);
        copy(size, b, 0);
        copy(idBytes, b, 4);
        int offset = idBytes.length + 4;
        b[offset] = (byte) 61;
        copy(contentBytes, b, offset + 1);
        return b;
    }

    public boolean isBinary() {
        return false;
    }

    public void isBinary(boolean b) {
        if (b) {
            throw new UnsupportedOperationException("OggTagFields cannot be changed to binary.\nbinary data should be stored elsewhere according to Vorbis_I_spec.");
        }
    }

    public boolean isCommon() {
        return this.common;
    }

    public boolean isEmpty() {
        return this.content.equals("");
    }

    public void setContent(String s) {
        this.content = s;
    }

    public void setEncoding(String s) {
        if (s == null || (s.equalsIgnoreCase("UTF-8") ^ 1) != 0) {
            throw new UnsupportedOperationException("The encoding of OggTagFields cannot be changed.(specified to be UTF-8)");
        }
    }

    public String toString() {
        return getContent();
    }
}
