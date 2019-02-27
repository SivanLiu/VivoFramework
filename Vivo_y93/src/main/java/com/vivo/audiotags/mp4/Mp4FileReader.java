package com.vivo.audiotags.mp4;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.mp4.util.Mp4InfoReader;
import com.vivo.audiotags.mp4.util.Mp4TagReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp4FileReader extends AudioFileReader {
    private Mp4InfoReader ir = new Mp4InfoReader();
    private Mp4TagReader tr = new Mp4TagReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.tr.read(raf);
    }
}
