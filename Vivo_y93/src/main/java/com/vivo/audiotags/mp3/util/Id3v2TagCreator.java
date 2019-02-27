package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.generic.AbstractTagCreator;
import com.vivo.audiotags.mp3.Id3v2Tag;
import java.nio.ByteBuffer;
import java.util.List;

public class Id3v2TagCreator extends AbstractTagCreator {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f5-assertionsDisabled = (Id3v2TagCreator.class.desiredAssertionStatus() ^ 1);
    public static final int DEFAULT_PADDING = 4000;

    public static byte[] getSyncSafe(int value) {
        if (f5-assertionsDisabled || value >= 0) {
            byte[] result = new byte[4];
            for (int i = 0; i < 4; i++) {
                result[i] = (byte) ((value >> ((3 - i) * 7)) & 127);
            }
            if (f5-assertionsDisabled || ((result[0] & 128) == 0 && (result[1] & 128) == 0 && (result[2] & 128) == 0 && (result[3] & 128) == 0)) {
                return result;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    public void create(Tag tag, ByteBuffer buf, List fields, int tagSize, int paddingSize) {
        buf.put((byte) 73).put((byte) 68).put((byte) 51);
        buf.put((byte) 4);
        buf.put((byte) 0);
        boolean[] ID3Flags = new boolean[]{false, false, false, false};
        buf.put(createID3Flags(ID3Flags));
        buf.put(getSyncSafe((tagSize - 10) + paddingSize));
        boolean z = ID3Flags[1];
        for (byte[] put : fields) {
            buf.put(put);
        }
        for (int i = 0; i < paddingSize; i++) {
            buf.put((byte) 0);
        }
    }

    private byte createID3Flags(boolean[] flag) {
        byte b = (byte) 0;
        if (flag[0]) {
            b = (byte) 128;
        }
        if (flag[1]) {
            b = (byte) (b + 64);
        }
        if (flag[2]) {
            b = (byte) (b + 32);
        }
        if (flag[3]) {
            return (byte) (b + 16);
        }
        return b;
    }

    protected Tag getCompatibleTag(Tag tag) {
        if (tag instanceof Id3v2Tag) {
            return tag;
        }
        Id3v2Tag id3Tag = new Id3v2Tag();
        id3Tag.merge(tag);
        return id3Tag;
    }

    protected int getFixedTagLength(Tag tag) {
        return 10;
    }
}
