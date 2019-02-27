package com.vivo.audiotags.mp4.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.generic.AbstractTagCreator;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class Mp4TagCreator extends AbstractTagCreator {
    protected int getFixedTagLength(Tag tag) throws UnsupportedEncodingException {
        throw new RuntimeException("Not implemented");
    }

    protected Tag getCompatibleTag(Tag tag) {
        throw new RuntimeException("Not implemented");
    }

    protected void create(Tag tag, ByteBuffer buf, List fields, int tagSize, int padding) throws UnsupportedEncodingException {
        throw new RuntimeException("Not implemented");
    }
}
