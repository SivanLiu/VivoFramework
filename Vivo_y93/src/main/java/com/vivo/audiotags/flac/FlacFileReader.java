package com.vivo.audiotags.flac;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.flac.util.FlacInfoReader;
import com.vivo.audiotags.flac.util.FlacTagReader;
import com.vivo.audiotags.generic.AudioFileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FlacFileReader extends AudioFileReader {
    private FlacInfoReader ir = new FlacInfoReader();
    private FlacTagReader tr = new FlacTagReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.tr.read(raf);
    }
}
