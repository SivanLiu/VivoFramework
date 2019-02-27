package com.vivo.audiotags.asf.data;

import java.math.BigInteger;

public class Chunk {
    protected final BigInteger chunkLength;
    protected final GUID guid;
    protected final long position;

    public Chunk(GUID headerGuid, long pos, BigInteger chunkLen) {
        if (headerGuid == null) {
            throw new IllegalArgumentException("GUID must not be null nor anything else than 16 entries long.");
        } else if (pos < 0) {
            throw new IllegalArgumentException("Position of header can't be negative.");
        } else if (chunkLen == null || chunkLen.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("chunkLen must not be null nor negative.");
        } else {
            this.guid = headerGuid;
            this.position = pos;
            this.chunkLength = chunkLen;
        }
    }

    public long getChunckEnd() {
        return this.position + this.chunkLength.longValue();
    }

    public BigInteger getChunkLength() {
        return this.chunkLength;
    }

    public GUID getGuid() {
        return this.guid;
    }

    public long getPosition() {
        return this.position;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer();
        result.append("GUID: " + GUID.getGuidDescription(this.guid));
        result.append("\n   Starts at position: " + getPosition() + "\n");
        result.append("   Last byte at: " + (getChunckEnd() - 1) + "\n\n");
        return result.toString();
    }

    public String toString() {
        return prettyPrint();
    }
}
