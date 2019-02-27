package com.vivo.audiotags.mp4;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.mp4.util.Mp4TagWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp4FileWriter extends AudioFileWriter {
    private Mp4TagWriter tw = new Mp4TagWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.tw.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile rafTemp) throws IOException {
        this.tw.delete(raf, rafTemp);
    }
}
