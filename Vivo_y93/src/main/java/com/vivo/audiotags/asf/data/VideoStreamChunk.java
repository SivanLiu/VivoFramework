package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;

public class VideoStreamChunk extends StreamChunk {
    private byte[] codecId;
    private long pictureHeight;
    private long pictureWidth;

    public VideoStreamChunk(long pos, BigInteger chunkLen) {
        super(pos, chunkLen);
    }

    public byte[] getCodecId() {
        return this.codecId;
    }

    public String getCodecIdAsString() {
        if (getCodecId() != null) {
            return new String(getCodecId());
        }
        return "Unknown";
    }

    public long getPictureHeight() {
        return this.pictureHeight;
    }

    public long getPictureWidth() {
        return this.pictureWidth;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint().replaceAll(Utils.LINE_SEPARATOR, Utils.LINE_SEPARATOR + "   "));
        result.insert(0, Utils.LINE_SEPARATOR + "VideoStream");
        result.append("Video info:" + Utils.LINE_SEPARATOR);
        result.append("      Width  : " + getPictureWidth() + Utils.LINE_SEPARATOR);
        result.append("      Heigth : " + getPictureHeight() + Utils.LINE_SEPARATOR);
        result.append("      Codec  : " + getCodecIdAsString() + Utils.LINE_SEPARATOR);
        return result.toString();
    }

    public void setCodecId(byte[] codecId) {
        this.codecId = codecId;
    }

    public void setPictureHeight(long picHeight) {
        this.pictureHeight = picHeight;
    }

    public void setPictureWidth(long picWidth) {
        this.pictureWidth = picWidth;
    }
}
