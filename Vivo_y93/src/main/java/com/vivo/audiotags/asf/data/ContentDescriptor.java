package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;

public final class ContentDescriptor implements Comparable {
    public static final HashSet COMMON_FIELD_IDS = new HashSet();
    public static final String ID_ALBUM = "WM/AlbumTitle";
    public static final String ID_ARTIST = "WM/AlbumArtist";
    public static final String ID_GENRE = "WM/Genre";
    public static final String ID_GENREID = "WM/GenreID";
    public static final String ID_TRACKNUMBER = "WM/TrackNumber";
    public static final String ID_YEAR = "WM/Year";
    public static final int TYPE_BINARY = 1;
    public static final int TYPE_BOOLEAN = 2;
    public static final int TYPE_DWORD = 3;
    public static final int TYPE_QWORD = 4;
    public static final int TYPE_STRING = 0;
    public static final int TYPE_WORD = 5;
    protected byte[] content = new byte[0];
    private int descriptorType;
    private final String name;

    static {
        COMMON_FIELD_IDS.add(ID_ALBUM);
        COMMON_FIELD_IDS.add(ID_ARTIST);
        COMMON_FIELD_IDS.add(ID_GENRE);
        COMMON_FIELD_IDS.add(ID_GENREID);
        COMMON_FIELD_IDS.add(ID_TRACKNUMBER);
        COMMON_FIELD_IDS.add(ID_YEAR);
    }

    public ContentDescriptor(String propName, int propType) {
        if (propName == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        }
        Utils.checkStringLengthNullSafe(propName);
        this.name = propName;
        this.descriptorType = propType;
    }

    public Object clone() throws CloneNotSupportedException {
        return createCopy();
    }

    public int compareTo(Object o) {
        if (!(o instanceof ContentDescriptor)) {
            return 0;
        }
        return getName().compareTo(((ContentDescriptor) o).getName());
    }

    public ContentDescriptor createCopy() {
        ContentDescriptor result = new ContentDescriptor(getName(), getType());
        result.content = getRawData();
        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ContentDescriptor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ContentDescriptor other = (ContentDescriptor) obj;
        if (other.getName().equals(getName()) && other.descriptorType == this.descriptorType) {
            return Arrays.equals(this.content, other.content);
        }
        return false;
    }

    public boolean getBoolean() {
        return this.content.length > 0 && this.content[0] != (byte) 0;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            byte[] nameBytes = getName().getBytes("UTF-16LE");
            result.write(Utils.getBytes((long) (nameBytes.length + 2), 2));
            result.write(nameBytes);
            result.write(Utils.getBytes(0, 2));
            result.write(Utils.getBytes((long) getType(), 2));
            if (getType() == 0) {
                result.write(Utils.getBytes((long) (this.content.length + 2), 2));
                result.write(this.content);
                result.write(Utils.getBytes(0, 2));
            } else {
                result.write(Utils.getBytes((long) this.content.length, 2));
                result.write(this.content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toByteArray();
    }

    public String getName() {
        return this.name;
    }

    public long getNumber() {
        int bytesNeeded;
        long result = 0;
        switch (getType()) {
            case 2:
                bytesNeeded = 1;
                break;
            case 3:
                bytesNeeded = 4;
                break;
            case 4:
                bytesNeeded = 8;
                break;
            case 5:
                bytesNeeded = 2;
                break;
            default:
                throw new UnsupportedOperationException("The current type doesn't allow an interpretation as a number.");
        }
        if (bytesNeeded > this.content.length) {
            throw new IllegalStateException("The stored data cannot represent the type of current object.");
        }
        for (int i = 0; i < bytesNeeded; i++) {
            result |= (long) (this.content[i] << (i * 8));
        }
        return result;
    }

    public byte[] getRawData() {
        byte[] copy = new byte[this.content.length];
        System.arraycopy(copy, 0, this.content, 0, this.content.length);
        return copy;
    }

    public String getString() {
        String result = "";
        switch (getType()) {
            case 0:
                try {
                    return new String(this.content, "UTF-16LE");
                } catch (Exception e) {
                    e.printStackTrace();
                    return result;
                }
            case 1:
                return "binary data";
            case 2:
                return String.valueOf(getBoolean());
            case 3:
            case 4:
            case 5:
                return String.valueOf(getNumber());
            default:
                throw new IllegalStateException("Current type is not known.");
        }
    }

    public int getType() {
        return this.descriptorType;
    }

    public boolean isCommon() {
        return COMMON_FIELD_IDS.contains(getName());
    }

    public boolean isEmpty() {
        return this.content.length == 0;
    }

    public void setBinaryValue(byte[] data) throws IllegalArgumentException {
        if (data.length > 65535) {
            throw new IllegalArgumentException("Too many bytes. 65535 is maximum.");
        }
        this.content = data;
        this.descriptorType = 1;
    }

    public void setBooleanValue(boolean value) {
        byte b;
        byte[] bArr = new byte[4];
        if (value) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        bArr[0] = b;
        bArr[1] = (byte) 0;
        bArr[2] = (byte) 0;
        bArr[3] = (byte) 0;
        this.content = bArr;
        this.descriptorType = 2;
    }

    public void setDWordValue(long value) {
        this.content = Utils.getBytes(value, 4);
        this.descriptorType = 3;
    }

    public void setQWordValue(long value) {
        this.content = Utils.getBytes(value, 8);
        this.descriptorType = 4;
    }

    public void setStringValue(String value) throws IllegalArgumentException {
        try {
            byte[] tmp = value.getBytes("UTF-16LE");
            if (tmp.length > 65535) {
                throw new IllegalArgumentException("Byte representation of String in \"UTF-16LE\" is to great. (Maximum is 65535 Bytes)");
            }
            this.content = tmp;
            this.descriptorType = 0;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.content = new byte[0];
        }
    }

    public void setWordValue(int value) {
        this.content = Utils.getBytes((long) value, 2);
        this.descriptorType = 5;
    }

    public String toString() {
        return getName() + " : " + new String[]{"String: ", "Binary: ", "Boolean: ", "DWORD: ", "QWORD:", "WORD:"}[this.descriptorType] + getString();
    }
}
