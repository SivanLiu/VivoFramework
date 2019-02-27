package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp3InfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        EncodingInfo encodingInfo = new EncodingInfo();
        if (raf.length() == 0) {
            System.err.println("Error: File empty");
            throw new CannotReadException("File is empty");
        }
        int id3TagSize = 0;
        raf.seek(0);
        byte[] bbb = new byte[3];
        raf.read(bbb);
        raf.seek(0);
        if (new String(bbb).equals("ID3")) {
            raf.seek(6);
            id3TagSize = read_syncsafe_integer(raf);
            raf.seek((long) (id3TagSize + 10));
        }
        byte[] b = new byte[4];
        raf.read(b);
        while (true) {
            if (((b[0] & 255) != 255 || (b[1] & 224) != 224 || (b[1] & 6) == 0 || (b[2] & 240) == 240 || (b[2] & 12) == 12) && raf.getFilePointer() < raf.length() - 4) {
                raf.seek(raf.getFilePointer() - 3);
                raf.read(b);
            }
        }
        MPEGFrame firstFrame = new MPEGFrame(b);
        if (firstFrame == null || (firstFrame.isValid() ^ 1) != 0 || firstFrame.getSamplingRate() == 0) {
            throw new CannotReadException("Error: could not synchronize to first mp3 frame");
        }
        double lengthInSeconds;
        int firstFrameLength = firstFrame.getFrameLength();
        int skippedLength = 0;
        if (firstFrame.getMPEGVersion() == 3 && firstFrame.getChannelMode() == 3) {
            raf.seek(raf.getFilePointer() + 17);
            skippedLength = 17;
        } else if (firstFrame.getMPEGVersion() == 3) {
            raf.seek(raf.getFilePointer() + 32);
            skippedLength = 32;
        } else if (firstFrame.getMPEGVersion() == 2 && firstFrame.getChannelMode() == 3) {
            raf.seek(raf.getFilePointer() + 9);
            skippedLength = 9;
        } else if (firstFrame.getMPEGVersion() == 2) {
            raf.seek(raf.getFilePointer() + 17);
            skippedLength = 17;
        }
        byte[] xingPart1 = new byte[16];
        raf.read(xingPart1);
        raf.seek(raf.getFilePointer() + 100);
        byte[] xingPart2 = new byte[4];
        raf.read(xingPart2);
        VbrInfoFrame xingMPEGFrame = new XingMPEGFrame(xingPart1, xingPart2);
        if (xingMPEGFrame.isValid()) {
            int optionalFrameLength = 120;
            byte[] lameHeader = new byte[36];
            raf.read(lameHeader);
            if (new LameMPEGFrame(lameHeader).isValid()) {
                optionalFrameLength = 120 + 36;
            } else {
                raf.seek(raf.getFilePointer() - 36);
            }
            raf.seek((raf.getFilePointer() + ((long) firstFrameLength)) - ((long) ((skippedLength + optionalFrameLength) + 4)));
        } else {
            raf.seek(((raf.getFilePointer() - 120) - ((long) skippedLength)) + 32);
            byte[] vbriHeader = new byte[18];
            raf.read(vbriHeader);
            xingMPEGFrame = new VBRIMPEGFrame(vbriHeader);
            raf.seek((raf.getFilePointer() - 18) - 4);
        }
        double timePerFrame = ((double) firstFrame.getSampleNumber()) / ((double) firstFrame.getSamplingRate());
        if (vbrInfoFrame.isValid()) {
            long length;
            lengthInSeconds = timePerFrame * ((double) vbrInfoFrame.getFrameCount());
            encodingInfo.setVbr(vbrInfoFrame.isVbr());
            int fs = vbrInfoFrame.getFileSize();
            if (fs == 0) {
                length = raf.length() - ((long) id3TagSize);
            } else {
                length = (long) fs;
            }
            encodingInfo.setBitrate((int) (((double) (length * 8)) / ((((double) vbrInfoFrame.getFrameCount()) * timePerFrame) * 1000.0d)));
        } else {
            int frameLength = firstFrame.getFrameLength();
            if (frameLength == 0) {
                throw new CannotReadException("Error while reading header(maybe file is corrupted, or missing first mpeg frame before xing header)");
            }
            lengthInSeconds = timePerFrame * ((double) ((raf.length() - ((long) id3TagSize)) / ((long) frameLength)));
            encodingInfo.setVbr(false);
            encodingInfo.setBitrate(firstFrame.getBitrate());
        }
        encodingInfo.setPreciseLength((float) lengthInSeconds);
        encodingInfo.setChannelNumber(firstFrame.getChannelNumber());
        encodingInfo.setSamplingRate(firstFrame.getSamplingRate());
        encodingInfo.setEncodingType(firstFrame.MPEGVersionToString(firstFrame.getMPEGVersion()) + " || " + firstFrame.layerToString(firstFrame.getLayerVersion()));
        encodingInfo.setExtraEncodingInfos("");
        return encodingInfo;
    }

    private int read_syncsafe_integer(RandomAccessFile raf) throws IOException {
        return (((((raf.read() & 255) << 21) + 0) + ((raf.read() & 255) << 14)) + ((raf.read() & 255) << 7)) + (raf.read() & 255);
    }
}
