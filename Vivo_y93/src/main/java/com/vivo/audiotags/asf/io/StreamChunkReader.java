package com.vivo.audiotags.asf.io;

import com.vivo.audiotags.asf.data.AudioStreamChunk;
import com.vivo.audiotags.asf.data.Chunk;
import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.asf.data.StreamChunk;
import com.vivo.audiotags.asf.data.VideoStreamChunk;
import com.vivo.audiotags.asf.util.Utils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class StreamChunkReader {
    protected StreamChunkReader() {
    }

    public static StreamChunk read(RandomAccessFile raf, Chunk candidate) throws IOException {
        if (raf == null || candidate == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        } else if (!GUID.GUID_STREAM.equals(candidate.getGuid())) {
            return null;
        } else {
            raf.seek(candidate.getPosition());
            return new StreamChunkReader().parseData(raf);
        }
    }

    private StreamChunk parseData(RandomAccessFile raf) throws IOException {
        StreamChunk result = null;
        long chunkStart = raf.getFilePointer();
        if (GUID.GUID_STREAM.equals(Utils.readGUID(raf))) {
            BigInteger chunkLength = Utils.readBig64(raf);
            GUID streamTypeGUID = Utils.readGUID(raf);
            if (GUID.GUID_AUDIOSTREAM.equals(streamTypeGUID) || GUID.GUID_VIDEOSTREAM.equals(streamTypeGUID)) {
                GUID errorConcealment = Utils.readGUID(raf);
                long timeOffset = Utils.readUINT64(raf);
                long typeSpecificDataSize = Utils.readUINT32(raf);
                long streamSpecificDataSize = Utils.readUINT32(raf);
                int mask = Utils.readUINT16(raf);
                int streamNumber = mask & 127;
                boolean contentEncrypted = (32768 & mask) != 0;
                raf.skipBytes(4);
                if (GUID.GUID_AUDIOSTREAM.equals(streamTypeGUID)) {
                    StreamChunk audioStreamChunk = new AudioStreamChunk(chunkStart, chunkLength);
                    result = audioStreamChunk;
                    long compressionFormat = (long) Utils.readUINT16(raf);
                    long channelCount = (long) Utils.readUINT16(raf);
                    long samplingRate = Utils.readUINT32(raf);
                    long avgBytesPerSec = Utils.readUINT32(raf);
                    long blockAlignment = (long) Utils.readUINT16(raf);
                    int bitsPerSample = Utils.readUINT16(raf);
                    byte[] codecSpecificData = new byte[Utils.readUINT16(raf)];
                    raf.readFully(codecSpecificData);
                    audioStreamChunk.setCompressionFormat(compressionFormat);
                    audioStreamChunk.setChannelCount(channelCount);
                    audioStreamChunk.setSamplingRate(samplingRate);
                    audioStreamChunk.setAverageBytesPerSec(avgBytesPerSec);
                    audioStreamChunk.setErrorConcealment(errorConcealment);
                    audioStreamChunk.setBlockAlignment(blockAlignment);
                    audioStreamChunk.setBitsPerSample(bitsPerSample);
                    audioStreamChunk.setCodecData(codecSpecificData);
                } else if (GUID.GUID_VIDEOSTREAM.equals(streamTypeGUID)) {
                    StreamChunk videoStreamChunk = new VideoStreamChunk(chunkStart, chunkLength);
                    result = videoStreamChunk;
                    long pictureWidth = Utils.readUINT32(raf);
                    long pictureHeight = Utils.readUINT32(raf);
                    raf.skipBytes(1);
                    long formatDataSize = (long) Utils.readUINT16(raf);
                    raf.skipBytes(16);
                    byte[] fourCC = new byte[4];
                    raf.read(fourCC);
                    videoStreamChunk.setPictureWidth(pictureWidth);
                    videoStreamChunk.setPictureHeight(pictureHeight);
                    videoStreamChunk.setCodecId(fourCC);
                }
                result.setStreamNumber(streamNumber);
                result.setStreamSpecificDataSize(streamSpecificDataSize);
                result.setTypeSpecificDataSize(typeSpecificDataSize);
                result.setTimeOffset(timeOffset);
                result.setContentEncrypted(contentEncrypted);
            }
        }
        return result;
    }
}
