package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OggInfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        long oldPos;
        int pageSegments;
        byte[] b;
        EncodingInfo info = new EncodingInfo();
        raf.seek(0);
        double PCMSamplesNumber = -1.0d;
        raf.seek(raf.length() - 2);
        while (raf.getFilePointer() >= 4) {
            if (raf.read() == 83) {
                raf.seek(raf.getFilePointer() - 4);
                byte[] ogg = new byte[3];
                raf.readFully(ogg);
                if (ogg[0] == (byte) 79 && ogg[1] == (byte) 103 && ogg[2] == (byte) 103) {
                    raf.seek(raf.getFilePointer() - 3);
                    oldPos = raf.getFilePointer();
                    raf.seek(raf.getFilePointer() + 26);
                    pageSegments = raf.readByte() & 255;
                    raf.seek(oldPos);
                    b = new byte[(pageSegments + 27)];
                    raf.readFully(b);
                    OggPageHeader pageHeader = new OggPageHeader(b);
                    raf.seek(0);
                    PCMSamplesNumber = pageHeader.getAbsoluteGranulePosition();
                    break;
                }
            }
            raf.seek(raf.getFilePointer() - 2);
        }
        if (PCMSamplesNumber == -1.0d) {
            throw new CannotReadException("Error: Could not find the Ogg Setup block");
        }
        b = new byte[4];
        oldPos = raf.getFilePointer();
        raf.seek(26);
        pageSegments = raf.read() & 255;
        raf.seek(oldPos);
        b = new byte[(pageSegments + 27)];
        raf.read(b);
        byte[] vorbisData = new byte[new OggPageHeader(b).getPageLength()];
        raf.read(vorbisData);
        VorbisCodecHeader vorbisCodecHeader = new VorbisCodecHeader(vorbisData);
        info.setPreciseLength((float) (PCMSamplesNumber / ((double) vorbisCodecHeader.getSamplingRate())));
        info.setChannelNumber(vorbisCodecHeader.getChannelNumber());
        info.setSamplingRate(vorbisCodecHeader.getSamplingRate());
        info.setEncodingType(vorbisCodecHeader.getEncodingType());
        info.setExtraEncodingInfos("");
        if (vorbisCodecHeader.getNominalBitrate() != 0 && vorbisCodecHeader.getMaxBitrate() == vorbisCodecHeader.getNominalBitrate() && vorbisCodecHeader.getMinBitrate() == vorbisCodecHeader.getNominalBitrate()) {
            info.setBitrate(vorbisCodecHeader.getNominalBitrate() / 1000);
            info.setVbr(false);
        } else if (vorbisCodecHeader.getNominalBitrate() != 0 && vorbisCodecHeader.getMaxBitrate() == 0 && vorbisCodecHeader.getMinBitrate() == 0) {
            info.setBitrate(vorbisCodecHeader.getNominalBitrate() / 1000);
            info.setVbr(true);
        } else {
            info.setBitrate(computeBitrate(info.getLength(), raf.length()));
            info.setVbr(true);
        }
        return info;
    }

    private int computeBitrate(int length, long size) {
        return (int) (((size / 1000) * 8) / ((long) length));
    }
}
