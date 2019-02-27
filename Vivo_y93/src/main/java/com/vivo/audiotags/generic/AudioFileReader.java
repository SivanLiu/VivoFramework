package com.vivo.audiotags.generic;

import com.vivo.audiotags.AudioFile;
import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class AudioFileReader {
    protected abstract EncodingInfo getEncodingInfo(RandomAccessFile randomAccessFile) throws CannotReadException, IOException;

    protected abstract Tag getTag(RandomAccessFile randomAccessFile) throws CannotReadException, IOException;

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00df A:{SYNTHETIC, Splitter: B:34:0x00df} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AudioFile read(File f) throws CannotReadException {
        Exception e;
        Throwable th;
        if (!f.canRead()) {
            throw new CannotReadException("Can't read file \"" + f.getAbsolutePath() + "\"");
        } else if (f.length() <= 150) {
            throw new CannotReadException("Less than 150 byte \"" + f.getAbsolutePath() + "\"");
        } else {
            RandomAccessFile raf = null;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(f, "r");
                try {
                    Tag tag;
                    raf2.seek(0);
                    EncodingInfo info = getEncodingInfo(raf2);
                    try {
                        raf2.seek(0);
                        tag = getTag(raf2);
                    } catch (CannotReadException e2) {
                        System.err.println(e2.getMessage());
                        tag = new GenericTag();
                    }
                    AudioFile audioFile = new AudioFile(f, info, tag);
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        } catch (Exception ex) {
                            System.err.println("\"" + f + "\" :" + ex);
                        }
                    }
                    return audioFile;
                } catch (Exception e3) {
                    e = e3;
                    raf = raf2;
                } catch (Throwable th2) {
                    th = th2;
                    raf = raf2;
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (Exception ex2) {
                            System.err.println("\"" + f + "\" :" + ex2);
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    throw new CannotReadException("\"" + f + "\" :" + e, e);
                } catch (Throwable th3) {
                    th = th3;
                    if (raf != null) {
                    }
                    throw th;
                }
            }
        }
    }
}
