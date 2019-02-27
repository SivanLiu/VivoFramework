package com.vivo.audiotags.ape.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.ape.ApeTag;
import com.vivo.audiotags.generic.AbstractTagCreator;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class ApeTagCreator extends AbstractTagCreator {
    public void create(Tag tag, ByteBuffer buf, List fields, int tagSize, int paddingSize) throws UnsupportedEncodingException {
        buf.put("APETAGEX".getBytes());
        buf.put(new byte[]{(byte) -48, (byte) 7, (byte) 0, (byte) 0});
        int size = tagSize - 32;
        buf.put(new byte[]{(byte) ((-16777216 & size) >>> 24), (byte) ((16711680 & size) >>> 16), (byte) ((65280 & size) >>> 8), (byte) (size & 255)});
        int listLength = fields.size();
        buf.put(new byte[]{(byte) ((-16777216 & listLength) >>> 24), (byte) ((16711680 & listLength) >>> 16), (byte) ((65280 & listLength) >>> 8), (byte) (listLength & 255)});
        buf.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) -96});
        buf.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0});
        for (byte[] put : fields) {
            buf.put(put);
        }
        buf.put("APETAGEX".getBytes());
        buf.put(new byte[]{(byte) -48, (byte) 7, (byte) 0, (byte) 0});
        buf.put(new byte[]{(byte) ((-16777216 & size) >>> 24), (byte) ((16711680 & size) >>> 16), (byte) ((65280 & size) >>> 8), (byte) (size & 255)});
        buf.put(new byte[]{(byte) ((-16777216 & listLength) >>> 24), (byte) ((16711680 & listLength) >>> 16), (byte) ((65280 & listLength) >>> 8), (byte) (listLength & 255)});
        buf.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, Byte.MIN_VALUE});
        buf.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0});
    }

    protected Tag getCompatibleTag(Tag tag) {
        if (tag instanceof ApeTag) {
            return tag;
        }
        ApeTag apeTag = new ApeTag();
        apeTag.merge(tag);
        return apeTag;
    }

    protected int getFixedTagLength(Tag tag) {
        return 64;
    }
}
