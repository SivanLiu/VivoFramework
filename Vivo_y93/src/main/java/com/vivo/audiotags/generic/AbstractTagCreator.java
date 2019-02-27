package com.vivo.audiotags.generic;

import com.vivo.audiotags.Tag;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractTagCreator {
    protected abstract void create(Tag tag, ByteBuffer byteBuffer, List list, int i, int i2) throws UnsupportedEncodingException;

    protected abstract Tag getCompatibleTag(Tag tag);

    protected abstract int getFixedTagLength(Tag tag) throws UnsupportedEncodingException;

    public ByteBuffer convert(Tag tag) throws UnsupportedEncodingException {
        return convert(tag, 0);
    }

    public ByteBuffer convert(Tag tag, int padding) throws UnsupportedEncodingException {
        Tag compatibleTag = getCompatibleTag(tag);
        List fields = createFields(compatibleTag);
        int tagSize = computeTagLength(compatibleTag, fields);
        ByteBuffer buf = ByteBuffer.allocate(tagSize + padding);
        create(compatibleTag, buf, fields, tagSize, padding);
        buf.rewind();
        return buf;
    }

    protected List createFields(Tag tag) throws UnsupportedEncodingException {
        List fields = new LinkedList();
        Iterator it = tag.getFields();
        while (it.hasNext()) {
            fields.add(((TagField) it.next()).getRawContent());
        }
        return fields;
    }

    protected int computeTagLength(Tag tag, List l) throws UnsupportedEncodingException {
        int length = getFixedTagLength(tag);
        for (byte[] length2 : l) {
            length += length2.length;
        }
        return length;
    }

    public int getTagLength(Tag tag) throws UnsupportedEncodingException {
        Tag compatibleTag = getCompatibleTag(tag);
        return computeTagLength(compatibleTag, createFields(compatibleTag));
    }
}
