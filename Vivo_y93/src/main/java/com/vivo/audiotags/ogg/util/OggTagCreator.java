package com.vivo.audiotags.ogg.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.generic.AbstractTagCreator;
import com.vivo.audiotags.generic.Utils;
import com.vivo.audiotags.ogg.OggTag;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class OggTagCreator extends AbstractTagCreator {
    public void create(Tag tag, ByteBuffer buf, List fields, int tagSize, int padding) throws UnsupportedEncodingException {
        String vendorString = ((OggTag) tag).getVendor();
        int vendorLength = Utils.getUTF8Bytes(vendorString).length;
        buf.put(new byte[]{(byte) (vendorLength & 255), (byte) ((65280 & vendorLength) >> 8), (byte) ((16711680 & vendorLength) >> 16), (byte) ((-16777216 & vendorLength) >> 24)});
        buf.put(Utils.getUTF8Bytes(vendorString));
        int listLength = fields.size();
        buf.put(new byte[]{(byte) ((-16777216 & listLength) >> 24), (byte) ((16711680 & listLength) >> 16), (byte) ((65280 & listLength) >> 8), (byte) (listLength & 255)});
        for (byte[] put : fields) {
            buf.put(put);
        }
    }

    protected Tag getCompatibleTag(Tag tag) {
        if (tag instanceof OggTag) {
            return tag;
        }
        OggTag oggTag = new OggTag();
        oggTag.merge(tag);
        return oggTag;
    }

    protected int getFixedTagLength(Tag tag) throws UnsupportedEncodingException {
        return Utils.getUTF8Bytes(((OggTag) tag).getVendor()).length + 8;
    }
}
