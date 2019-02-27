package com.vivo.audiotags.wav.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WavInfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        EncodingInfo info = new EncodingInfo();
        if (raf.length() < 12) {
            throw new CannotReadException("This is not a WAV File (<12 bytes)");
        }
        byte[] b = new byte[12];
        raf.read(b);
        if (new WavRIFFHeader(b).isValid()) {
            b = new byte[24];
            raf.read(b);
            WavFormatHeader wfh = new WavFormatHeader(b);
            if (wfh.isValid()) {
                info.setPreciseLength((((float) raf.length()) - 36.0f) / ((float) wfh.getBytesPerSecond()));
                info.setChannelNumber(wfh.getChannelNumber());
                info.setSamplingRate(wfh.getSamplingRate());
                info.setEncodingType("WAV-RIFF " + wfh.getBitrate() + " bits");
                info.setExtraEncodingInfos("");
                info.setBitrate((wfh.getBytesPerSecond() * 8) / 1000);
                info.setVbr(false);
                return info;
            }
            throw new CannotReadException("Wav Format Header not valid");
        }
        throw new CannotReadException("Wav RIFF Header not valid");
    }
}
