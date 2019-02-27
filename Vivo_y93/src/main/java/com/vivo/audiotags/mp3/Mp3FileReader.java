package com.vivo.audiotags.mp3;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.generic.AudioFileReader;
import com.vivo.audiotags.generic.GenericTag;
import com.vivo.audiotags.mp3.util.Id3v1TagReader;
import com.vivo.audiotags.mp3.util.Id3v2TagReader;
import com.vivo.audiotags.mp3.util.Mp3InfoReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp3FileReader extends AudioFileReader {
    private Id3v1TagReader idv1tr = new Id3v1TagReader();
    private Id3v2TagReader idv2tr = new Id3v2TagReader();
    private Mp3InfoReader ir = new Mp3InfoReader();

    protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        return this.ir.read(raf);
    }

    protected Tag getTag(RandomAccessFile raf) throws IOException {
        Tag v2;
        Tag v1;
        String error = "";
        try {
            v2 = this.idv2tr.read(raf);
        } catch (CannotReadException e) {
            v2 = null;
            error = error + "(" + e.getMessage() + ")";
        }
        try {
            v1 = this.idv1tr.read(raf);
        } catch (CannotReadException e2) {
            v1 = null;
            error = error + "(" + e2.getMessage() + ")";
        }
        if (v1 == null && v2 == null) {
            return new GenericTag();
        }
        if (v2 == null) {
            return v1;
        }
        if (v1 != null) {
            v2.merge(v1);
            v2.hasId3v1(true);
        }
        return v2;
    }
}
