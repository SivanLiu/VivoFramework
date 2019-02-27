package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;

public class StreamChunk extends Chunk {
    private boolean contentEncrypted;
    private int streamNumber;
    private long streamSpecificDataSize;
    private long timeOffset;
    private long typeSpecificDataSize;

    public StreamChunk(long pos, BigInteger chunkLen) {
        super(GUID.GUID_AUDIOSTREAM, pos, chunkLen);
    }

    public int getStreamNumber() {
        return this.streamNumber;
    }

    public long getStreamSpecificDataSize() {
        return this.streamSpecificDataSize;
    }

    public long getTimeOffset() {
        return this.timeOffset;
    }

    public long getTypeSpecificDataSize() {
        return this.typeSpecificDataSize;
    }

    public boolean isContentEncrypted() {
        return this.contentEncrypted;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, Utils.LINE_SEPARATOR + "Stream Data:" + Utils.LINE_SEPARATOR);
        result.append("   Stream number: " + getStreamNumber() + Utils.LINE_SEPARATOR);
        result.append("   Type specific data size  : " + getTypeSpecificDataSize() + Utils.LINE_SEPARATOR);
        result.append("   Stream specific data size: " + getStreamSpecificDataSize() + Utils.LINE_SEPARATOR);
        result.append("   Time Offset              : " + getTimeOffset() + Utils.LINE_SEPARATOR);
        result.append("   Content Encryption       : " + isContentEncrypted() + Utils.LINE_SEPARATOR);
        return result.toString();
    }

    public void setContentEncrypted(boolean cntEnc) {
        this.contentEncrypted = cntEnc;
    }

    public void setStreamNumber(int streamNum) {
        this.streamNumber = streamNum;
    }

    public void setStreamSpecificDataSize(long strSpecDataSize) {
        this.streamSpecificDataSize = strSpecDataSize;
    }

    public void setTimeOffset(long timeOffs) {
        this.timeOffset = timeOffs;
    }

    public void setTypeSpecificDataSize(long typeSpecDataSize) {
        this.typeSpecificDataSize = typeSpecDataSize;
    }
}
