package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.EncodingChunk;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EncodingChunkReader {
    public static EncodingChunk read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_ENCODING.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new EncodingChunkReader().parseData(raf);
        }
    }

    protected EncodingChunkReader() {
    }

    private EncodingChunk parseData(RandomAccessFile raf) throws IOException {
        EncodingChunk result = null;
        long chunkStart = raf.getFilePointer();
        if (GUID.GUID_ENCODING.equals(Utils.readGUID(raf))) {
            result = new EncodingChunk(chunkStart, Utils.readBig64(raf));
            raf.skipBytes(20);
            int stringCount = Utils.readUINT16(raf);
            System.out.println("sunrain parseData stringCount -->  " + stringCount);
            for (int i = 0; i < stringCount; i++) {
                result.addString(Utils.readCharacterSizedString(raf));
            }
        }
        return result;
    }
}
