package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.mp3.Id3v2Tag;
import com.vivo.audiotags.mp3.util.Id3v2TagCreator;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public abstract class Id3Frame implements TagField {
    protected byte[] flags;
    protected byte version;

    protected abstract byte[] build() throws UnsupportedEncodingException;

    public abstract String getId();

    public abstract boolean isBinary();

    public abstract boolean isCommon();

    protected abstract void populate(byte[] bArr) throws UnsupportedEncodingException;

    public Id3Frame() {
        this.version = Id3v2Tag.ID3V23;
        createDefaultFlags();
    }

    public Id3Frame(byte[] raw, byte version) throws UnsupportedEncodingException {
        byte[] rawNew;
        if (version == Id3v2Tag.ID3V23 || version == Id3v2Tag.ID3V24) {
            int size = 2;
            if ((raw[1] & 128) == 128) {
                size = (byte) 6;
            }
            if ((raw[1] & 128) == 64) {
                size = (byte) (size + 1);
            }
            if ((raw[1] & 128) == 32) {
                size = (byte) (size + 1);
            }
            this.flags = new byte[size];
            for (int i = 0; i < size; i++) {
                this.flags[i] = raw[i];
            }
            rawNew = raw;
        } else {
            createDefaultFlags();
            rawNew = new byte[(this.flags.length + raw.length)];
            copy(this.flags, rawNew, 0);
            copy(raw, rawNew, this.flags.length);
        }
        this.version = version;
        populate(rawNew);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected void copy(byte[] src, byte[] dst, int dstOffset) {
        for (int i = 0; i < src.length; i++) {
            dst[i + dstOffset] = src[i];
        }
    }

    private void createDefaultFlags() {
        this.flags = new byte[2];
        this.flags[0] = (byte) 0;
        this.flags[1] = (byte) 0;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Id3Frame) {
            try {
                return Arrays.equals(build(), ((Id3Frame) obj).build());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected byte[] getBytes(String s, String encoding) throws UnsupportedEncodingException {
        byte[] result;
        byte[] tmp;
        if ("UTF-16".equalsIgnoreCase(encoding)) {
            result = s.getBytes("UTF-16LE");
            tmp = new byte[(result.length + 4)];
            System.arraycopy(result, 0, tmp, 2, result.length);
            tmp[0] = (byte) -1;
            tmp[1] = (byte) -2;
            return tmp;
        }
        result = s.getBytes(encoding);
        int zeroTerm = 1;
        if ("UTF-16BE".equals(encoding)) {
            zeroTerm = 2;
        }
        tmp = new byte[(result.length + zeroTerm)];
        System.arraycopy(result, 0, tmp, 0, result.length);
        return tmp;
    }

    public byte[] getFlags() {
        return this.flags;
    }

    protected byte[] getIdBytes() {
        return getId().getBytes();
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        return build();
    }

    protected byte[] getSize(int size) {
        if (this.version == Id3v2Tag.ID3V24) {
            return Id3v2TagCreator.getSyncSafe(size);
        }
        return new byte[]{(byte) ((size >> 24) & 255), (byte) ((size >> 16) & 255), (byte) ((size >> 8) & 255), (byte) (size & 255)};
    }

    protected String getString(byte[] b, int offset, int length, String encoding) throws UnsupportedEncodingException {
        int zerochars;
        if ("UTF-16".equalsIgnoreCase(encoding)) {
            zerochars = 0;
            if (b[(offset + length) - 2] == (byte) 0 && b[(offset + length) - 1] == (byte) 0) {
                zerochars = 2;
            }
            if (b[offset] == (byte) -2 && b[offset + 1] == (byte) -1) {
                return new String(b, offset + 2, (length - 2) - zerochars, "UTF-16BE");
            }
            if (b[offset] == (byte) -1 && b[offset + 1] == (byte) -2) {
                return new String(b, offset + 2, (length - 2) - zerochars, "UTF-16LE");
            }
            return new String(b, offset, length - zerochars, "UTF-16LE");
        }
        zerochars = 0;
        if ("UTF-16BE".equals(encoding)) {
            if (b[(offset + length) - 2] == (byte) 0 && b[(offset + length) - 1] == (byte) 0) {
                zerochars = 2;
            }
        } else if (b[(offset + length) - 1] == (byte) 0) {
            zerochars = 1;
        }
        if (length == 0 || offset + length > b.length) {
            return "";
        }
        return new String(b, offset, length - zerochars, encoding);
    }

    protected int indexOfFirstNull(byte[] b, int offset) {
        for (int i = offset; i < b.length; i++) {
            if (b[i] == (byte) 0) {
                return i;
            }
        }
        return -1;
    }

    public void isBinary(boolean b) {
    }
}
