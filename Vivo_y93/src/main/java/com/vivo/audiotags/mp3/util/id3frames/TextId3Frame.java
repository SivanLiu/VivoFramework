package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import com.vivo.audiotags.mp3.Id3V2TagConverter;
import com.vivo.audiotags.mp3.Id3v2Tag;
import java.io.UnsupportedEncodingException;

public class TextId3Frame extends Id3Frame implements TagTextField {
    protected boolean common;
    protected String content;
    protected byte encoding;
    protected String id;

    public TextId3Frame(String id, String content) {
        this.id = id;
        checkCommon();
        this.content = content;
        setEncoding(Id3v2Tag.DEFAULT_ENCODING);
    }

    public TextId3Frame(String id, byte[] rawContent, byte version) throws UnsupportedEncodingException {
        super(rawContent, version);
        this.id = id;
        checkCommon();
    }

    private void checkCommon() {
        boolean z;
        if (this.id.equals("TIT2") || this.id.equals("TALB") || this.id.equals("TPE1") || this.id.equals("TCON") || this.id.equals("TRCK") || this.id.equals(Id3V2TagConverter.RECORDING_TIME)) {
            z = true;
        } else {
            z = this.id.equals("COMM");
        }
        this.common = z;
    }

    public String getEncoding() {
        if (this.encoding == (byte) 0) {
            return "ISO-8859-1";
        }
        if (this.encoding == (byte) 1) {
            return "UTF-16";
        }
        if (this.encoding == (byte) 2) {
            return "UTF-16BE";
        }
        if (this.encoding == (byte) 3) {
            return "UTF-8";
        }
        return "ISO-8859-1";
    }

    public void setEncoding(String enc) {
        if ("ISO-8859-1".equals(enc)) {
            this.encoding = (byte) 0;
        } else if ("UTF-16".equals(enc)) {
            this.encoding = (byte) 1;
        } else if ("UTF-16BE".equals(enc)) {
            this.encoding = (byte) 2;
        } else if ("UTF-8".equals(enc)) {
            this.encoding = (byte) 3;
        } else {
            this.encoding = (byte) 1;
        }
    }

    public String getContent() {
        return this.content;
    }

    public boolean isBinary() {
        return false;
    }

    public String getId() {
        return this.id;
    }

    public boolean isCommon() {
        return this.common;
    }

    public void setContent(String s) {
        this.content = s;
    }

    public boolean isEmpty() {
        return this.content.equals("");
    }

    public void copyContent(TagField field) {
        if (field instanceof TextId3Frame) {
            this.content = ((TextId3Frame) field).getContent();
            setEncoding(((TextId3Frame) field).getEncoding());
        }
    }

    protected void populate(byte[] raw) throws UnsupportedEncodingException {
        this.encoding = raw[this.flags.length];
        if (!(this.encoding == (byte) 0 || this.encoding == (byte) 1 || this.encoding == (byte) 2 || this.encoding == (byte) 3)) {
            this.encoding = (byte) 0;
        }
        this.content = getString(raw, this.flags.length + 1, (raw.length - this.flags.length) - 1, getEncoding());
        int i = this.content.indexOf("\u0000");
        if (i != -1) {
            this.content = this.content.substring(0, i);
        }
    }

    protected byte[] build() throws UnsupportedEncodingException {
        byte[] data = getBytes(this.content, getEncoding());
        byte[] b = new byte[(((this.flags.length + 8) + 1) + data.length)];
        copy(getIdBytes(), b, 0);
        copy(getSize(b.length - 10), b, 4);
        copy(this.flags, b, 4 + 4);
        int offset = this.flags.length + 8;
        b[offset] = this.encoding;
        copy(data, b, offset + 1);
        return b;
    }

    public String toString() {
        return getContent();
    }
}
