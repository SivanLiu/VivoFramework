package com.vivo.audiotags.ape;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.util.ApeTagReader;
import com.vivo.audiotags.ape.util.MonkeyInfoReader;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MonkeyFileReader extends AudioFileReader {
    private ApeTagReader ape = new ApeTagReader();
    private MonkeyInfoReader ir = new MonkeyInfoReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ape.read(raf);
    }
}
