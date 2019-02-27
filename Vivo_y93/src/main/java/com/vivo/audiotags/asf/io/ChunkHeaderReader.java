package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;

class ChunkHeaderReader {
    ChunkHeaderReader() {
    }

    public static Chunk readChunckHeader(RandomAccessFile input) throws IOException {
        return new Chunk(Utils.readGUID(input), input.getFilePointer(), Utils.readBig64(input));
    }
}
