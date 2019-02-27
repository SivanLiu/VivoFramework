package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.mp3.Id3v2Tag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class Id3v2TagReader {
    private boolean[] ID3Flags;
    private final Id3v2TagSynchronizer synchronizer = new Id3v2TagSynchronizer();
    private final Id3v24TagReader tagReader = new Id3v24TagReader();

    private boolean[] processID3Flags(byte b) {
        if (b != (byte) 0) {
            boolean[] flags = new boolean[4];
            if ((b & 128) == 128) {
                flags[0] = true;
            } else {
                flags[0] = false;
            }
            if ((b & 64) == 64) {
                flags[1] = true;
            } else {
                flags[1] = false;
            }
            if ((b & 32) == 32) {
                flags[2] = true;
            } else {
                flags[2] = false;
            }
            if ((b & 16) == 16) {
                flags[3] = true;
                return flags;
            }
            flags[3] = false;
            return flags;
        }
        return new boolean[]{false, false, false, false};
    }

    public synchronized Id3v2Tag read(RandomAccessFile raf) throws CannotReadException, IOException {
        Id3v2Tag tag;
        byte[] b = new byte[3];
        raf.read(b);
        if (new String(b).equals("ID3")) {
            String versionHigh = String.valueOf(raf.read());
            String versionID3 = versionHigh + "." + raf.read();
            this.ID3Flags = processID3Flags(raf.readByte());
            b = new byte[(readSyncsafeInteger(raf) + 2)];
            raf.readFully(b);
            ByteBuffer bb = ByteBuffer.wrap(b);
            if (this.ID3Flags[0]) {
                bb = this.synchronizer.synchronize(bb);
            }
            if (versionHigh.equals("2")) {
                tag = this.tagReader.read(bb, this.ID3Flags, Id3v2Tag.ID3V22);
            } else if (versionHigh.equals("3")) {
                tag = this.tagReader.read(bb, this.ID3Flags, Id3v2Tag.ID3V23);
            } else if (versionHigh.equals("4")) {
                tag = this.tagReader.read(bb, this.ID3Flags, Id3v2Tag.ID3V24);
            } else {
                throw new CannotReadException("ID3v2 tag version " + versionID3 + " not supported !");
            }
        }
        throw new CannotReadException("Not an ID3 tag");
        return tag;
    }

    private int readSyncsafeInteger(RandomAccessFile raf) throws IOException {
        return (((((raf.read() & 255) << 21) + 0) + ((raf.read() & 255) << 14)) + ((raf.read() & 255) << 7)) + (raf.read() & 255);
    }
}
