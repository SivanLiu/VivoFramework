package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.FileHeader;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class FileHeaderReader {
    public static FileHeader read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_FILE.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new FileHeaderReader().parseData(raf);
        }
    }

    protected FileHeaderReader() {
    }

    private FileHeader parseData(RandomAccessFile raf) throws IOException {
        long fileHeaderStart = raf.getFilePointer();
        if (!GUID.GUID_FILE.equals(Utils.readGUID(raf))) {
            return null;
        }
        BigInteger chunckLen = Utils.readBig64(raf);
        raf.skipBytes(16);
        BigInteger fileSize = Utils.readBig64(raf);
        if (((long) fileSize.intValue()) != raf.length()) {
            System.err.println("Filesize of file doesn't match len of Fileheader. (" + fileSize.toString() + ", file: " + raf.length() + ")");
        }
        return new FileHeader(fileHeaderStart, chunckLen, fileSize, Utils.readBig64(raf), Utils.readBig64(raf), Utils.readBig64(raf), Utils.readBig64(raf), Utils.readBig64(raf), Utils.readUINT32(raf), Utils.readUINT32(raf), Utils.readUINT32(raf), Utils.readUINT32(raf));
    }
}
