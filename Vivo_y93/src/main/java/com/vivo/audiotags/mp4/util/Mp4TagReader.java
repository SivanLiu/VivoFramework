package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.exceptions.CannotReadException;
import com.vivo.audiotags.mp4.Mp4Tag;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class Mp4TagReader {
    public Mp4Tag read(RandomAccessFile raf) throws CannotReadException, IOException {
        Mp4Tag tag = new Mp4Tag();
        Mp4Box box = new Mp4Box();
        byte[] b = new byte[4];
        seek(raf, box, "moov");
        seek(raf, box, "udta");
        seek(raf, box, "meta");
        raf.read(b);
        if (b[0] != (byte) 0) {
            throw new CannotReadException();
        }
        seek(raf, box, "ilst");
        int length = box.getOffset() - 8;
        int read = 0;
        while (read < length) {
            b = new byte[8];
            raf.read(b);
            box.update(b);
            int fieldLength = box.getOffset() - 8;
            b = new byte[fieldLength];
            raf.read(b);
            tag.add(createMp4Field(box.getId(), b));
            read += fieldLength + 8;
        }
        System.out.println(tag);
        return tag;
    }

    private Mp4TagField createMp4Field(String id, byte[] raw) throws UnsupportedEncodingException {
        if (id.equals("trkn") || id.equals("tmpo")) {
            return new Mp4TagTextNumberField(id, raw);
        }
        if (id.equals("©ART") || id.equals("©alb") || id.equals("©nam") || id.equals("©day") || id.equals("©cmt") || id.equals("©gen") || id.equals("©too") || id.equals("©wrt")) {
            return new Mp4TagTextField(id, raw);
        }
        if (id.equals("covr")) {
            return new Mp4TagCoverField(raw);
        }
        return new Mp4TagBinaryField(id, raw);
    }

    private void seek(RandomAccessFile raf, Mp4Box box, String id) throws IOException {
        byte[] b = new byte[8];
        raf.read(b);
        box.update(b);
        while (!box.getId().equals(id)) {
            raf.skipBytes(box.getOffset() - 8);
            raf.read(b);
            box.update(b);
        }
    }

    public static void main(String[] args) throws Exception {
        new Mp4TagReader().read(new RandomAccessFile(new File("/home/kikidonk/test.mp4"), "r"));
    }
}
