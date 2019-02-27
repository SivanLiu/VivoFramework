package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.data.StreamBitratePropertiesChunk;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

public class StreamBitratePropertiesReader {
    public static StreamBitratePropertiesChunk read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_STREAM_BITRATE_PROPERTIES.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new StreamBitratePropertiesReader().parseData(raf);
        }
    }

    protected StreamBitratePropertiesReader() {
    }

    private StreamBitratePropertiesChunk parseData(RandomAccessFile raf) throws IOException {
        StreamBitratePropertiesChunk result = null;
        long chunkStart = raf.getFilePointer();
        if (GUID.GUID_STREAM_BITRATE_PROPERTIES.equals(Utils.readGUID(raf))) {
            result = new StreamBitratePropertiesChunk(chunkStart, Utils.readBig64(raf));
            long recordCount = (long) Utils.readUINT16(raf);
            for (int i = 0; ((long) i) < recordCount; i++) {
                result.addBitrateRecord(Utils.readUINT16(raf) & 255, Utils.readUINT32(raf));
            }
        }
        return result;
    }
}
