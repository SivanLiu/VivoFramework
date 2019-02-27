package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class EncodingChunk extends Chunk {
    private final ArrayList strings = new ArrayList();

    public EncodingChunk(long pos, BigInteger chunkLen) {
        super(GUID.GUID_ENCODING, pos, chunkLen);
    }

    public void addString(String toAdd) {
        this.strings.add(toAdd);
    }

    public Collection getStrings() {
        return new ArrayList(this.strings);
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, Utils.LINE_SEPARATOR + "Encoding:" + Utils.LINE_SEPARATOR);
        Iterator iterator = this.strings.iterator();
        while (iterator.hasNext()) {
            result.append("   " + iterator.next() + Utils.LINE_SEPARATOR);
        }
        return result.toString();
    }
}
