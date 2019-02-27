package com.vivo.audiotags.mp3;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.generic.GenericTag;

public class Id3v1Tag extends GenericTag {
    public static final String[] GENRES = Tag.DEFAULT_GENRES;

    protected boolean isAllowedEncoding(String enc) {
        return enc.equals("ISO-8859-1");
    }

    public String translateGenre(byte b) {
        int i = b & 255;
        if (i == 255 || i > GENRES.length - 1) {
            return "";
        }
        return GENRES[i];
    }

    public String toString() {
        return "Id3v1 " + super.toString();
    }
}
