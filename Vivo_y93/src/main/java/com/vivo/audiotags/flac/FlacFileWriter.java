package com.vivo.audiotags.flac;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.flac.util.FlacTagWriter;
import com.vivo.audiotags.generic.AudioFileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FlacFileWriter extends AudioFileWriter {
    private FlacTagWriter tw = new FlacTagWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.tw.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        this.tw.delete(raf, tempRaf);
    }
}
