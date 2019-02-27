package com.vivo.audiotags.asf.util;

import com.vivo.audiotags.asf.data.Chunk;
import java.util.Comparator;

public class ChunkPositionComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Chunk) || !(o2 instanceof Chunk)) {
            return 0;
        }
        return (int) (((Chunk) o1).getPosition() - ((Chunk) o2).getPosition());
    }
}
