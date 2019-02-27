package com.vivo.audiotags.flac.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ogg.util.OggTagCreator;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class FlacTagCreator {
    public static final int DEFAULT_PADDING = 4000;
    private static final OggTagCreator creator = new OggTagCreator();

    public ByteBuffer convert(Tag tag, int paddingSize) throws UnsupportedEncodingException {
        ByteBuffer ogg = creator.convert(tag);
        int tagLength = ogg.capacity() + 4;
        ByteBuffer buf = ByteBuffer.allocate(tagLength + paddingSize);
        buf.put(paddingSize > 0 ? (byte) 4 : (byte) -124);
        int commentLength = tagLength - 4;
        buf.put(new byte[]{(byte) ((16711680 & commentLength) >>> 16), (byte) ((65280 & commentLength) >>> 8), (byte) (commentLength & 255)});
        buf.put(ogg);
        if (paddingSize >= 4) {
            int paddingDataSize = paddingSize - 4;
            buf.put((byte) -127);
            buf.put(new byte[]{(byte) ((16711680 & paddingDataSize) >>> 16), (byte) ((65280 & paddingDataSize) >>> 8), (byte) (paddingDataSize & 255)});
            for (int i = 0; i < paddingDataSize; i++) {
                buf.put((byte) 0);
            }
        }
        buf.rewind();
        return buf;
    }

    public int getTagLength(Tag tag) throws UnsupportedEncodingException {
        return creator.convert(tag).capacity() + 4;
    }
}
