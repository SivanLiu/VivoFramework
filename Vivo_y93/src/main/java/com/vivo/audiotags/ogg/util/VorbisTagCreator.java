package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.Tag;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class VorbisTagCreator {
    private OggTagCreator creator = new OggTagCreator();

    public ByteBuffer convert(Tag tag) throws UnsupportedEncodingException {
        ByteBuffer ogg = this.creator.convert(tag);
        ByteBuffer buf = ByteBuffer.allocate(ogg.capacity() + 8);
        buf.put(new byte[]{(byte) 3, (byte) 118, (byte) 111, (byte) 114, (byte) 98, (byte) 105, (byte) 115});
        buf.put(ogg);
        buf.put((byte) 1);
        buf.rewind();
        return buf;
    }
}
