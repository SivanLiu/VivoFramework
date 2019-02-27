package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.AsfHeader;
import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.ContentDescription;
import com.vivo.audiotags.asf.data.EncodingChunk;
import com.vivo.audiotags.asf.data.ExtendedContentDescription;
import com.vivo.audiotags.asf.data.FileHeader;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.data.StreamBitratePropertiesChunk;
import com.vivo.audiotags.asf.data.StreamChunk;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class AsfHeaderReader {
    public static AsfHeader readHeader(RandomAccessFile in) throws IOException {
        return new AsfHeaderReader().parseData(in);
    }

    protected AsfHeaderReader() {
    }

    private AsfHeader parseData(RandomAccessFile in) throws IOException {
        AsfHeader result = null;
        long chunkStart = in.getFilePointer();
        if (GUID.GUID_HEADER.equals(Utils.readGUID(in))) {
            BigInteger chunkLen = Utils.readBig64(in);
            long chunkCount = Utils.readUINT32(in);
            in.skipBytes(2);
            ArrayList chunks = new ArrayList();
            while (chunkLen.compareTo(BigInteger.valueOf(in.getFilePointer())) > 0) {
                Chunk chunk = ChunkHeaderReader.readChunckHeader(in);
                chunks.add(chunk);
                in.seek(chunk.getChunckEnd());
            }
            result = new AsfHeader(chunkStart, chunkLen, chunkCount);
            FileHeader fileHeader = null;
            ExtendedContentDescription extendedDescription = null;
            EncodingChunk encodingChunk = null;
            StreamChunk streamChunk = null;
            ContentDescription contentDescription = null;
            StreamBitratePropertiesChunk bitratePropertiesChunk = null;
            Iterator iterator = chunks.iterator();
            while (iterator.hasNext()) {
                Chunk currentChunk = (Chunk) iterator.next();
                if (fileHeader == null) {
                    fileHeader = FileHeaderReader.read(in, currentChunk);
                    if (fileHeader != null) {
                    }
                }
                if (extendedDescription == null) {
                    extendedDescription = ExtContentDescReader.read(in, currentChunk);
                    if (extendedDescription != null) {
                    }
                }
                if (encodingChunk == null) {
                    encodingChunk = EncodingChunkReader.read(in, currentChunk);
                    if (encodingChunk != null) {
                    }
                }
                if (streamChunk == null) {
                    streamChunk = StreamChunkReader.read(in, currentChunk);
                    if (streamChunk != null) {
                        result.addStreamChunk(streamChunk);
                        streamChunk = null;
                    }
                }
                if (contentDescription == null) {
                    contentDescription = ContentDescriptionReader.read(in, currentChunk);
                    if (contentDescription != null) {
                    }
                }
                if (bitratePropertiesChunk == null) {
                    bitratePropertiesChunk = StreamBitratePropertiesReader.read(in, currentChunk);
                    if (bitratePropertiesChunk != null) {
                    }
                }
                result.addUnspecifiedChunk(currentChunk);
            }
            result.setFileHeader(fileHeader);
            result.setEncodingChunk(encodingChunk);
            result.setExtendedContentDescription(extendedDescription);
            result.setContentDescription(contentDescription);
            result.setStreamBitratePropertiesChunk(bitratePropertiesChunk);
        }
        return result;
    }
}
