package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MonkeyInfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        EncodingInfo info = new EncodingInfo();
        if (raf.length() == 0) {
            System.err.println("Error: File empty");
            throw new CannotReadException("File is empty");
        }
        raf.seek(0);
        byte[] b = new byte[4];
        raf.read(b);
        if (new String(b).equals("MAC ")) {
            b = new byte[4];
            raf.read(b);
            int version = Utils.getNumber(b, 0, 3);
            if (version < 3970) {
                throw new CannotReadException("Monkey Audio version <= 3.97 is not supported");
            }
            b = new byte[44];
            raf.read(b);
            MonkeyDescriptor md = new MonkeyDescriptor(b);
            b = new byte[24];
            raf.read(b);
            MonkeyHeader mh = new MonkeyHeader(b);
            raf.seek((long) md.getRiffWavOffset());
            raf.read(new byte[12]);
            raf.read(new byte[24]);
            info.setLength(mh.getLength());
            info.setPreciseLength(mh.getPreciseLength());
            info.setBitrate(computeBitrate(info.getLength(), raf.length()));
            info.setEncodingType("Monkey Audio v" + (((double) version) / 1000.0d) + ", compression level " + mh.getCompressionLevel());
            info.setExtraEncodingInfos("");
            return info;
        }
        throw new CannotReadException("'MAC ' Header not found");
    }

    private int computeBitrate(int length, long size) {
        return (int) (((size / 1000) * 8) / ((long) length));
    }
}
