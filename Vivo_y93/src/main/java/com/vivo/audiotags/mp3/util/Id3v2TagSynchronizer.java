package com.vivo.audiotags.mp3.util;

import java.nio.ByteBuffer;

public class Id3v2TagSynchronizer {
    public ByteBuffer synchronize(ByteBuffer b) {
        ByteBuffer bb = ByteBuffer.allocate(b.capacity());
        int cap = b.capacity();
        while (b.remaining() >= 1) {
            byte cur = b.get();
            bb.put(cur);
            if ((cur & 255) == 255 && b.remaining() >= 1 && b.get(b.position()) == (byte) 0) {
                b.get();
            }
        }
        bb.limit(bb.position());
        bb.rewind();
        return bb;
    }
}
