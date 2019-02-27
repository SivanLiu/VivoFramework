package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.generic.Utils;
import com.vivo.audiotags.ogg.OggTag;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OggTagReader {
    public OggTag read(RandomAccessFile raf) throws IOException {
        OggTag tag = new OggTag();
        byte[] b = new byte[4];
        raf.read(b);
        b = new byte[Utils.getNumber(b, 0, 3)];
        raf.read(b);
        tag.setVendor(new String(b, "UTF-8"));
        b = new byte[4];
        raf.read(b);
        int userComments = Utils.getNumber(b, 0, 3);
        for (int i = 0; i < userComments; i++) {
            b = new byte[4];
            raf.read(b);
            b = new byte[Utils.getNumber(b, 0, 3)];
            raf.read(b);
            tag.add(new OggTagField(b));
        }
        return tag;
    }
}
