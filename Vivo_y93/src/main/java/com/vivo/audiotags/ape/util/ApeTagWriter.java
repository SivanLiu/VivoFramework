package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ApeTagWriter {
    private ApeTagCreator tc = new ApeTagCreator();

    public void delete(RandomAccessFile raf) throws IOException {
        if (tagExists(raf)) {
            raf.seek(raf.length() - 20);
            byte[] b = new byte[4];
            raf.read(b);
            raf.setLength(raf.length() - Utils.getLongNumber(b, 0, 3));
            if (tagExists(raf)) {
                raf.setLength(raf.length() - 32);
            }
        }
    }

    private boolean tagExists(RandomAccessFile raf) throws IOException {
        raf.seek(raf.length() - 32);
        byte[] b = new byte[8];
        raf.read(b);
        return new String(b).equals("APETAGEX");
    }

    public void write(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        FileChannel fc = raf.getChannel();
        ByteBuffer tagBuffer = this.tc.convert(tag, 0);
        if (tagExists(raf)) {
            raf.seek((raf.length() - 32) + 8);
            byte[] b = new byte[4];
            raf.read(b);
            if (Utils.getNumber(b, 0, 3) != 2000) {
                throw new CannotWriteException("APE Tag other than version 2.0 are not supported");
            }
            b = new byte[4];
            raf.read(b);
            long oldSize = Utils.getLongNumber(b, 0, 3) + 32;
            if (oldSize <= ((long) tagBuffer.capacity())) {
                System.err.println("Overwriting old tag in mpc file");
                fc.position(fc.size() - oldSize);
                fc.write(tagBuffer);
                return;
            }
            System.err.println("Shrinking mpc file");
            FileChannel tempFC = rafTemp.getChannel();
            tempFC.position(0);
            fc.position(0);
            tempFC.transferFrom(fc, 0, fc.size() - oldSize);
            tempFC.position(tempFC.size());
            tempFC.write(tagBuffer);
            return;
        }
        fc.position(fc.size());
        fc.write(tagBuffer);
    }
}
