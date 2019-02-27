package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.mp3.Id3v1Tag;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Id3v1TagReader {
    public Id3v1Tag read(RandomAccessFile raf) throws CannotReadException, IOException {
        Id3v1Tag tag = new Id3v1Tag();
        raf.seek(raf.length() - 128);
        byte[] b = new byte[3];
        raf.read(b);
        raf.seek(0);
        if (new String(b).equals("TAG")) {
            raf.seek((raf.length() - 128) + 3);
            String songName = read(raf, 30);
            String artist = read(raf, 30);
            String album = read(raf, 30);
            String year = read(raf, 4);
            String comment = read(raf, 30);
            String trackNumber = "";
            raf.seek(raf.getFilePointer() - 2);
            b = new byte[2];
            raf.read(b);
            if (b[0] == (byte) 0) {
                trackNumber = new Integer(b[1]).toString();
            }
            byte genreByte = raf.readByte();
            raf.seek(0);
            tag.setTitle(songName);
            tag.setArtist(artist);
            tag.setAlbum(album);
            tag.setYear(year);
            tag.setComment(comment);
            tag.setTrack(trackNumber);
            tag.setGenre(tag.translateGenre(genreByte));
            return tag;
        }
        throw new CannotReadException("There is no Id3v1 Tag in this file");
    }

    private String read(RandomAccessFile raf, int length) throws IOException {
        byte[] b = new byte[length];
        raf.read(b);
        String ret = new String(b).trim();
        int i = ret.indexOf("\u0000");
        if (i != -1) {
            return ret.substring(0, i + 1);
        }
        return ret;
    }
}
