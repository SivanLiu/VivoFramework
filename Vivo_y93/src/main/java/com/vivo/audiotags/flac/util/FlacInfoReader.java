package com.vivo.audiotags.flac.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FlacInfoReader {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f1-assertionsDisabled = (FlacInfoReader.class.desiredAssertionStatus() ^ 1);

    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        if (raf.length() == 0) {
            throw new CannotReadException("Error: File empty");
        }
        raf.seek(0);
        byte[] b = new byte[4];
        raf.read(b);
        if (new String(b).equals("fLaC")) {
            EncodingInfo info;
            MetadataBlockDataStreamInfo mbdsi = null;
            boolean isLastBlock = false;
            while (!isLastBlock) {
                b = new byte[4];
                raf.read(b);
                MetadataBlockHeader mbh = new MetadataBlockHeader(b);
                if (mbh.getBlockType() == 0) {
                    b = new byte[mbh.getDataLength()];
                    raf.read(b);
                    mbdsi = new MetadataBlockDataStreamInfo(b);
                    if (!mbdsi.isValid()) {
                        throw new CannotReadException("FLAC StreamInfo not valid");
                    }
                    if (f1-assertionsDisabled && mbdsi == null) {
                        throw new AssertionError();
                    }
                    info = new EncodingInfo();
                    info.setLength(mbdsi.getLength());
                    info.setPreciseLength(mbdsi.getPreciseLength());
                    info.setChannelNumber(mbdsi.getChannelNumber());
                    info.setSamplingRate(mbdsi.getSamplingRate());
                    info.setEncodingType(mbdsi.getEncodingType());
                    info.setExtraEncodingInfos("");
                    info.setBitrate(computeBitrate(mbdsi.getLength(), raf.length()));
                    return info;
                }
                raf.seek(raf.getFilePointer() + ((long) mbh.getDataLength()));
                isLastBlock = mbh.isLastBlock();
            }
            if (f1-assertionsDisabled) {
            }
            info = new EncodingInfo();
            info.setLength(mbdsi.getLength());
            info.setPreciseLength(mbdsi.getPreciseLength());
            info.setChannelNumber(mbdsi.getChannelNumber());
            info.setSamplingRate(mbdsi.getSamplingRate());
            info.setEncodingType(mbdsi.getEncodingType());
            info.setExtraEncodingInfos("");
            info.setBitrate(computeBitrate(mbdsi.getLength(), raf.length()));
            return info;
        }
        throw new CannotReadException("fLaC Header not found");
    }

    private int computeBitrate(int length, long size) {
        return (int) (((size / 1000) * 8) / ((long) length));
    }
}
