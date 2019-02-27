package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.Tag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Id3v1TagWriter {
    private Id3v1TagCreator tc = new Id3v1TagCreator();

    public void delete(RandomAccessFile raf) throws IOException {
        if (tagExists(raf)) {
            raf.setLength(raf.length() - 128);
        }
    }

    private boolean tagExists(RandomAccessFile raf) throws IOException {
        if (raf.length() <= 128) {
            return false;
        }
        raf.seek(raf.length() - 128);
        byte[] b = new byte[3];
        raf.read(b);
        return new String(b).equals("TAG");
    }

    public void write(Tag tag, RandomAccessFile raf) throws IOException {
        FileChannel fc = raf.getChannel();
        ByteBuffer tagBuffer = this.tc.convert(tag);
        if (tagExists(raf)) {
            fc.position(fc.size() - 128);
            fc.write(tagBuffer);
            return;
        }
        fc.position(fc.size());
        fc.write(tagBuffer);
    }
}
