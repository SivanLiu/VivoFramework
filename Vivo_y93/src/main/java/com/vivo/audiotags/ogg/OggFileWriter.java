package com.vivo.audiotags.ogg;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import com.vivo.audiotags.ogg.util.VorbisTagWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OggFileWriter extends AudioFileWriter {
    private VorbisTagWriter otw = new VorbisTagWriter();

    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
        this.otw.write(tag, raf, rafTemp);
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        this.otw.delete(raf, tempRaf);
    }
}
