package com.vivo.audiotags.wav;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.AudioFileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WavFileWriter extends AudioFileWriter {
    protected void writeTag(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException {
    }

    protected void deleteTag(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
    }
}
