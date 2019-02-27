package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.EncodingInfo;
import com.vivo.audiotags.exceptions.CannotReadException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp4InfoReader {
    public EncodingInfo read(RandomAccessFile raf) throws CannotReadException, IOException {
        EncodingInfo info = new EncodingInfo();
        Mp4Box box = new Mp4Box();
        seek(raf, box, "moov");
        seek(raf, box, "mvhd");
        byte[] b = new byte[(box.getOffset() - 8)];
        raf.read(b);
        info.setLength(new Mp4MvhdBox(b).getLength());
        System.out.println(info);
        return info;
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
        new Mp4InfoReader().read(new RandomAccessFile(new File("/home/kikidonk/test.mp4"), "r"));
    }
}
