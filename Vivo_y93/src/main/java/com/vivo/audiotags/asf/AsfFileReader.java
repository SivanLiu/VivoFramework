package com.vivo.audiotags.asf;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.asf.data.AsfHeader;
import com.vivo.audiotags.asf.io.AsfHeaderReader;
import com.vivo.audiotags.asf.util.TagConverter;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AsfFileReader extends AudioFileReader {
    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        raf.seek(0);
        EncodingInfo info = new EncodingInfo();
        try {
            AsfHeader header = AsfHeaderReader.readHeader(raf);
            if (header == null) {
                System.out.println("sunrain  AsfFileReader getEncodingInfo header == null");
                throw new CannotReadException("Some values must have been incorrect for interpretation as asf with wma content.");
            }
            info.setBitrate(header.getAudioStreamChunk().getKbps());
            info.setChannelNumber((int) header.getAudioStreamChunk().getChannelCount());
            info.setEncodingType("ASF (audio): " + header.getAudioStreamChunk().getCodecDescription());
            info.setPreciseLength(header.getFileHeader().getPreciseDuration());
            info.setSamplingRate((int) header.getAudioStreamChunk().getSamplingRate());
            return info;
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw ((IOException) e);
            } else if (e instanceof CannotReadException) {
                throw ((CannotReadException) e);
            } else {
                throw new CannotReadException("Failed to read. Cause: " + e.getMessage());
            }
        }
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        raf.seek(0);
        try {
            AsfHeader header = AsfHeaderReader.readHeader(raf);
            if (header != null) {
                return TagConverter.createTagOf(header);
            }
            System.out.println("sunrain  AsfFileReader getTag header == null");
            throw new CannotReadException("Some values must have been incorrect for interpretation as asf with wma content.");
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw ((IOException) e);
            } else if (e instanceof CannotReadException) {
                throw ((CannotReadException) e);
            } else {
                throw new CannotReadException("Failed to read. Cause: " + e.getMessage());
            }
        }
    }
}
