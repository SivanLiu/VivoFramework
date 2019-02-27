package com.vivo.audiotags.mp3;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.mp3.util.Id3v1TagWriter;
import com.vivo.audiotags.mp3.util.Id3v2TagWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp3FileWriter extends AudioFileWriter {
    private Id3v1TagWriter idv1tw = new Id3v1TagWriter();
    private Id3v2TagWriter idv2tw = new Id3v2TagWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.idv1tw.write(tag, raf);
        this.idv2tw.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile rafTemp) throws IOException {
        this.idv1tw.delete(this.idv2tw.delete(raf, rafTemp));
    }
}
