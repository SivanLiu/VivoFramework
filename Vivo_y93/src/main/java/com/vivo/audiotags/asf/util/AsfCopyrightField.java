package com.vivo.audiotags.asf.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import java.io.UnsupportedEncodingException;
import java.util.List;

public final class AsfCopyrightField implements TagTextField {
    public static final String FIELD_ID = "SPECIAL/WM/COPYRIGHT";
    private String value = "";

    public static TagTextField getCopyright(Tag tag) {
        List list = tag.get(FIELD_ID);
        if (list == null || list.size() <= 0) {
            return null;
        }
        TagField field = (TagField) list.get(0);
        if (field instanceof TagTextField) {
            return (TagTextField) field;
        }
        return null;
    }

    public void copyContent(TagField field) {
        if (field instanceof TagTextField) {
            this.value = ((TagTextField) field).getContent();
        }
    }

    public String getContent() {
        return this.value;
    }

    public String getEncoding() {
        return "UTF-16LE";
    }

    public String getId() {
        return FIELD_ID;
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        return this.value.getBytes("UTF-16LE");
    }

    public boolean isBinary() {
        return false;
    }

    public void isBinary(boolean b) {
        if (b) {
            throw new UnsupportedOperationException("No conversion supported. Copyright is a String");
        }
    }

    public boolean isCommon() {
        return true;
    }

    public boolean isEmpty() {
        return this.value.length() == 0;
    }

    public void setContent(String s) {
        try {
            setString(s);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            this.value = "Conversion Exception occured.";
        }
    }

    public void setEncoding(String s) {
        if (s == null || (s.equalsIgnoreCase("UTF-16LE") ^ 1) != 0) {
            throw new UnsupportedOperationException("The encoding of Asf tags cannot be changed.(specified to be UTF-16LE)");
        }
    }

    public void setString(String s) {
        this.value = s;
        Utils.checkStringLengthNullSafe(this.value);
    }

    public String toString() {
        return "SPECIAL/WM/COPYRIGHT:\"" + getContent() + "\"";
    }
}
