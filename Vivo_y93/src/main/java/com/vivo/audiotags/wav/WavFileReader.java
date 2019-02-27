package com.vivo.audiotags.wav;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.generic.GenericTag;
import com.vivo.audiotags.wav.util.WavInfoReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WavFileReader extends AudioFileReader {
    private WavInfoReader ir = new WavInfoReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException {
        return new GenericTag();
    }
}
