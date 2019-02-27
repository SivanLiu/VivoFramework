package com.vivo.audiotags.mpc.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MpcInfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        EncodingInfo info = new EncodingInfo();
        if (raf.length() == 0) {
            System.err.println("Error: File empty");
            throw new CannotReadException("File is empty");
        }
        raf.seek(0);
        byte[] b = new byte[3];
        raf.read(b);
        String mpc = new String(b);
        if (!mpc.equals("MP+") && mpc.equals("ID3")) {
            raf.seek(6);
            raf.seek((long) (read_syncsafe_integer(raf) + 10));
            b = new byte[3];
            raf.read(b);
            if (!new String(b).equals("MP+")) {
                throw new CannotReadException("MP+ Header not found");
            }
        } else if (!mpc.equals("MP+")) {
            throw new CannotReadException("MP+ Header not found");
        }
        b = new byte[25];
        raf.read(b);
        MpcHeader mpcH = new MpcHeader(b);
        info.setPreciseLength((float) ((1152.0d * ((double) mpcH.getSamplesNumber())) / ((double) mpcH.getSamplingRate())));
        info.setChannelNumber(mpcH.getChannelNumber());
        info.setSamplingRate(mpcH.getSamplingRate());
        info.setEncodingType(mpcH.getEncodingType());
        info.setExtraEncodingInfos(mpcH.getEncoderInfo());
        info.setBitrate(computeBitrate(info.getLength(), raf.length()));
        return info;
    }

    private int read_syncsafe_integer(RandomAccessFile raf) throws IOException {
        return (((((raf.read() & 255) << 21) + 0) + ((raf.read() & 255) << 14)) + ((raf.read() & 255) << 7)) + (raf.read() & 255);
    }

    private int computeBitrate(int length, long size) {
        return (int) (((size / 1000) * 8) / ((long) length));
    }
}
