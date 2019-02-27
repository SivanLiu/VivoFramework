package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CommId3Frame extends TextId3Frame implements TagTextField {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f4-assertionsDisabled = (CommId3Frame.class.desiredAssertionStatus() ^ 1);
    private String lang;
    private String shortDesc;

    public CommId3Frame(String content) {
        super("COMM", content);
        this.shortDesc = "";
        this.lang = Locale.getDefault().getISO3Language();
    }

    public CommId3Frame(byte[] rawContent, byte version) throws UnsupportedEncodingException {
        super("COMM", rawContent, version);
    }

    public String getLangage() {
        return this.lang;
    }

    protected void populate(byte[] raw) throws UnsupportedEncodingException {
        this.encoding = raw[this.flags.length];
        if ((this.flags.length + 1) + 3 > raw.length - 1) {
            this.lang = "XXX";
            this.content = "";
            this.shortDesc = "";
            return;
        }
        this.lang = new String(raw, this.flags.length + 1, 3);
        int commentStart = getCommentStart(raw, this.flags.length + 4, getEncoding());
        this.shortDesc = getString(raw, this.flags.length + 4, (commentStart - this.flags.length) - 4, getEncoding());
        this.content = getString(raw, commentStart, raw.length - commentStart, getEncoding());
        if (!f4-assertionsDisabled && (this.lang == null || this.shortDesc == null || this.content == null)) {
            throw new AssertionError();
        }
    }

    public int getCommentStart(byte[] content, int offset, String encoding) {
        int result;
        if ("UTF-16".equals(encoding)) {
            result = offset;
            while (result < content.length) {
                if (content[result] == (byte) 0 && content[result + 1] == (byte) 0) {
                    return result + 2;
                }
                result += 2;
            }
            return result;
        }
        result = offset;
        while (result < content.length) {
            if (content[result] == (byte) 0) {
                return result + 1;
            }
            result++;
        }
        return result;
    }

    protected byte[] build() throws UnsupportedEncodingException {
        byte[] shortDescData = getBytes(this.shortDesc, getEncoding());
        byte[] contentData = getBytes(this.content, getEncoding());
        byte[] data = new byte[(shortDescData.length + contentData.length)];
        System.arraycopy(shortDescData, 0, data, 0, shortDescData.length);
        System.arraycopy(contentData, 0, data, shortDescData.length, contentData.length);
        byte[] lan = this.lang.getBytes();
        byte[] b = new byte[((((this.flags.length + 8) + 1) + 3) + data.length)];
        copy(getIdBytes(), b, 0);
        copy(getSize(b.length - 10), b, 4);
        copy(this.flags, b, 4 + 4);
        int offset = this.flags.length + 8;
        b[offset] = this.encoding;
        offset++;
        copy(lan, b, offset);
        copy(data, b, offset + lan.length);
        return b;
    }

    public String getShortDescription() {
        return this.shortDesc;
    }

    public boolean isEmpty() {
        return this.content.equals("") ? this.shortDesc.equals("") : false;
    }

    public void copyContent(TagField field) {
        super.copyContent(field);
        if (field instanceof CommId3Frame) {
            this.shortDesc = ((CommId3Frame) field).getShortDescription();
            this.lang = ((CommId3Frame) field).getLangage();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(getLangage()).append("] ").append("(").append(getShortDescription()).append(") ").append(getContent());
        return sb.toString();
    }
}
