package com.vivo.audiotags.flac.util;

import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.ogg.OggTag;
import com.vivo.audiotags.ogg.util.OggTagReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FlacTagReader {
    private OggTagReader oggTagReader = new OggTagReader();

    public OggTag read(RandomAccessFile raf) throws CannotReadException, IOException {
        if (raf.length() == 0) {
            throw new CannotReadException("Error: File empty");
        }
        raf.seek(0);
        byte[] b = new byte[4];
        raf.read(b);
        if (new String(b).equals("fLaC")) {
            boolean isLastBlock = false;
            while (!isLastBlock) {
                b = new byte[4];
                raf.read(b);
                MetadataBlockHeader mbh = new MetadataBlockHeader(b);
                switch (mbh.getBlockType()) {
                    case 4:
                        return handleVorbisComment(mbh, raf);
                    default:
                        raf.seek(raf.getFilePointer() + ((long) mbh.getDataLength()));
                        isLastBlock = mbh.isLastBlock();
                }
            }
            throw new CannotReadException("FLAC Tag could not be found or read..");
        }
        throw new CannotReadException("fLaC Header not found, not a flac file");
    }

    private OggTag handleVorbisComment(MetadataBlockHeader mbh, RandomAccessFile raf) throws IOException, CannotReadException {
        long oldPos = raf.getFilePointer();
        OggTag tag = this.oggTagReader.read(raf);
        if (raf.getFilePointer() - oldPos == ((long) mbh.getDataLength())) {
            return tag;
        }
        throw new CannotReadException("Tag length do not match with flac comment data length");
    }
}
