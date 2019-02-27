package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.ogg.OggTag;
import java.io.IOException;
import java.io.RandomAccessFile;

public class VorbisTagReader {
    private OggTagReader oggTagReader = new OggTagReader();

    public Tag read(RandomAccessFile raf) throws CannotReadException, IOException {
        raf.seek(0);
        byte[] b = new byte[4];
        raf.read(b);
        if (new String(b).equals("OggS")) {
            raf.seek(0);
            b = new byte[4];
            long oldPos = raf.getFilePointer();
            raf.seek(26);
            int pageSegments = raf.readByte() & 255;
            raf.seek(oldPos);
            b = new byte[(pageSegments + 27)];
            raf.read(b);
            raf.seek(raf.getFilePointer() + ((long) new OggPageHeader(b).getPageLength()));
            oldPos = raf.getFilePointer();
            raf.seek(raf.getFilePointer() + 26);
            pageSegments = raf.readByte() & 255;
            raf.seek(oldPos);
            b = new byte[(pageSegments + 27)];
            raf.read(b);
            OggPageHeader pageHeader = new OggPageHeader(b);
            b = new byte[7];
            raf.read(b);
            String vorbis = new String(b, 1, 6);
            if (b[0] == (byte) 3 && (vorbis.equals("vorbis") ^ 1) == 0) {
                OggTag tag = this.oggTagReader.read(raf);
                if (raf.readByte() != (byte) 0) {
                    return tag;
                }
                throw new CannotReadException("Error: The OGG Stream isn't valid, could not extract the tag");
            }
            throw new CannotReadException("Cannot find comment block (no vorbis header)");
        }
        throw new CannotReadException("OggS Header could not be found, not an ogg stream");
    }
}
