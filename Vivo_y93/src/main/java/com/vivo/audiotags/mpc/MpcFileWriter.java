package com.vivo.audiotags.mpc;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.util.ApeTagWriter;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.mp3.Mp3FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MpcFileWriter extends AudioFileWriter {
    private Mp3FileWriter mp3tw = new Mp3FileWriter();
    private ApeTagWriter tw = new ApeTagWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.tw.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotWriteException {
        this.mp3tw.delete(raf, tempRaf);
        if (tempRaf.length() > 0) {
            this.tw.delete(tempRaf);
        } else {
            this.tw.delete(raf);
        }
    }
}
