package com.vivo.audiotags.mpc;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.util.ApeTagReader;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.mpc.util.MpcInfoReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MpcFileReader extends AudioFileReader {
    private MpcInfoReader ir = new MpcInfoReader();
    private ApeTagReader tr = new ApeTagReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.tr.read(raf);
    }
}
