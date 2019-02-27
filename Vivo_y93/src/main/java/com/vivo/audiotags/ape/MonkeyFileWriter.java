package com.vivo.audiotags.ape;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.util.ApeTagWriter;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.mp3.Mp3FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MonkeyFileWriter extends AudioFileWriter {
    private ApeTagWriter ape = new ApeTagWriter();
    private Mp3FileWriter mp3tw = new Mp3FileWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.ape.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        this.mp3tw.delete(raf, tempRaf);
        if (tempRaf.length() > 0) {
            this.ape.delete(tempRaf);
        } else {
            this.ape.delete(raf);
        }
    }
}
