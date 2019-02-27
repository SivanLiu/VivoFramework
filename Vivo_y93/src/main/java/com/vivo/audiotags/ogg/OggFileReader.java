package com.vivo.audiotags.ogg;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.ogg.util.OggInfoReader;
import com.vivo.audiotags.ogg.util.VorbisTagReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OggFileReader extends AudioFileReader {
    private OggInfoReader ir = new OggInfoReader();
    private VorbisTagReader otr = new VorbisTagReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.otr.read(raf);
    }
}
