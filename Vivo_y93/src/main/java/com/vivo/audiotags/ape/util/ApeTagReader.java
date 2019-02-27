package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.ApeTag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ApeTagReader {
    public Tag read(RandomAccessFile raf) throws CannotReadException, IOException {
        ApeTag tag = new ApeTag();
        raf.seek(raf.length() - 32);
        byte[] b = new byte[8];
        raf.read(b);
        if (new String(b).equals("APETAGEX")) {
            b = new byte[4];
            raf.read(b);
            if (Utils.getNumber(b, 0, 3) != 2000) {
                throw new CannotReadException("APE Tag other than version 2.0 are not supported");
            }
            b = new byte[4];
            raf.read(b);
            long tagSize = Utils.getLongNumber(b, 0, 3);
            b = new byte[4];
            raf.read(b);
            int itemNumber = Utils.getNumber(b, 0, 3);
            raf.read(new byte[4]);
            raf.seek(raf.length() - tagSize);
            for (int i = 0; i < itemNumber; i++) {
                b = new byte[4];
                raf.read(b);
                int contentLength = Utils.getNumber(b, 0, 3);
                if (contentLength > 500000) {
                    throw new CannotReadException("Item size is much too large: " + contentLength + " bytes");
                }
                b = new byte[4];
                raf.read(b);
                boolean binary = ((b[0] & 6) >> 1) == 1;
                int j = 0;
                while (raf.readByte() != (byte) 0) {
                    j++;
                }
                raf.seek((raf.getFilePointer() - ((long) j)) - 1);
                int fieldSize = j;
                b = new byte[j];
                raf.read(b);
                raf.skipBytes(1);
                String field = new String(b);
                b = new byte[contentLength];
                raf.read(b);
                if (binary) {
                    tag.add(new ApeTagBinaryField(field, b));
                } else {
                    tag.add(new ApeTagTextField(field, new String(b, "UTF-8")));
                }
            }
            return tag;
        }
        throw new CannotReadException("There is no APE Tag in this file");
    }
}
