package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.mp3.Id3v1Tag;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Hashtable;

public class Id3v1TagCreator {
    private static Hashtable translateTable = new Hashtable(130);

    static {
        for (int i = 0; i < Id3v1Tag.GENRES.length; i++) {
            translateTable.put(Id3v1Tag.GENRES[i].toLowerCase(), new Byte((byte) i));
        }
    }

    public ByteBuffer convert(Tag tag) throws UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.allocate(128);
        buf.put((byte) 84).put((byte) 65).put((byte) 71);
        put(buf, tag.getFirstTitle(), 30);
        put(buf, tag.getFirstArtist(), 30);
        put(buf, tag.getFirstAlbum(), 30);
        put(buf, tag.getFirstYear(), 4);
        if (tag.getTrack().size() != 0) {
            int integ;
            put(buf, tag.getFirstComment(), 28);
            buf.put((byte) 0);
            try {
                integ = Integer.parseInt(tag.getFirstTrack());
            } catch (NumberFormatException e) {
                integ = 0;
            }
            buf.put((byte) integ);
        } else {
            put(buf, tag.getFirstComment(), 30);
        }
        buf.put(translateGenre(tag.getFirstGenre()));
        buf.rewind();
        return buf;
    }

    private void put(ByteBuffer buf, String s, int length) throws UnsupportedEncodingException {
        int i;
        byte[] b = new byte[length];
        byte[] text = truncate(s, length).getBytes("ISO-8859-1");
        for (i = 0; i < text.length; i++) {
            b[i] = text[i];
        }
        for (i = text.length; i < length - text.length; i++) {
            b[i] = (byte) 0;
        }
        buf.put(b, 0, length);
    }

    private String truncate(String s, int len) {
        return s.length() > len ? s.substring(0, len) : s;
    }

    private byte translateGenre(String genre) {
        Byte b = (Byte) translateTable.get(genre.toLowerCase());
        if (b == null) {
            return (byte) -1;
        }
        return b.byteValue();
    }
}
