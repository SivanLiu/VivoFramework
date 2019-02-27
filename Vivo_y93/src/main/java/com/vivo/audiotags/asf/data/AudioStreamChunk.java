package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.math.BigInteger;

public class AudioStreamChunk extends StreamChunk {
    public static final String[][] CODEC_DESCRIPTIONS;
    private long averageBytesPerSec;
    private int bitsPerSample;
    private long blockAlignment;
    private long channelCount;
    private byte[] codecData;
    private long compressionFormat;
    private GUID errorConcealment;
    private long samplingRate;

    static {
        String[][] strArr = new String[5][];
        strArr[0] = new String[]{"161", " (Windows Media Audio (ver 7,8,9))"};
        strArr[1] = new String[]{"162", " (Windows Media Audio 9 series (Professional))"};
        strArr[2] = new String[]{"163", "(Windows Media Audio 9 series (Lossless))"};
        strArr[3] = new String[]{"7A21", " (GSM-AMR (CBR))"};
        strArr[4] = new String[]{"7A22", " (GSM-AMR (VBR))"};
        CODEC_DESCRIPTIONS = strArr;
    }

    public AudioStreamChunk(long pos, BigInteger chunkLen) {
        super(pos, chunkLen);
    }

    public long getAverageBytesPerSec() {
        return this.averageBytesPerSec;
    }

    public int getBitsPerSample() {
        return this.bitsPerSample;
    }

    public long getBlockAlignment() {
        return this.blockAlignment;
    }

    public long getChannelCount() {
        return this.channelCount;
    }

    public byte[] getCodecData() {
        return this.codecData;
    }

    public String getCodecDescription() {
        StringBuffer result = new StringBuffer(Long.toHexString(getCompressionFormat()));
        String furtherDesc = " (Unknown)";
        for (int i = 0; i < CODEC_DESCRIPTIONS.length; i++) {
            if (CODEC_DESCRIPTIONS[i][0].equalsIgnoreCase(result.toString())) {
                furtherDesc = CODEC_DESCRIPTIONS[i][1];
                break;
            }
        }
        if (result.length() % 2 != 0) {
            result.insert(0, "0x0");
        } else {
            result.insert(0, "0x");
        }
        result.append(furtherDesc);
        return result.toString();
    }

    public long getCompressionFormat() {
        return this.compressionFormat;
    }

    public GUID getErrorConcealment() {
        return this.errorConcealment;
    }

    public int getKbps() {
        return (((int) getAverageBytesPerSec()) * 8) / 1000;
    }

    public long getSamplingRate() {
        return this.samplingRate;
    }

    public boolean isErrorConcealed() {
        return getErrorConcealment().equals(GUID.GUID_AUDIO_ERROR_CONCEALEMENT_INTERLEAVED);
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint().replaceAll(Utils.LINE_SEPARATOR, Utils.LINE_SEPARATOR + "   "));
        result.insert(0, Utils.LINE_SEPARATOR + "AudioStream");
        result.append("Audio info:" + Utils.LINE_SEPARATOR);
        result.append("      Bitrate : " + getKbps() + Utils.LINE_SEPARATOR);
        result.append("      Channels : " + getChannelCount() + " at " + getSamplingRate() + " Hz" + Utils.LINE_SEPARATOR);
        result.append("      Bits per Sample: " + getBitsPerSample() + Utils.LINE_SEPARATOR);
        result.append("      Formatcode: " + getCodecDescription() + Utils.LINE_SEPARATOR);
        return result.toString();
    }

    public void setAverageBytesPerSec(long avgeBytesPerSec) {
        this.averageBytesPerSec = avgeBytesPerSec;
    }

    public void setBitsPerSample(int bps) {
        this.bitsPerSample = bps;
    }

    public void setBlockAlignment(long align) {
        this.blockAlignment = align;
    }

    public void setChannelCount(long channels) {
        this.channelCount = channels;
    }

    public void setCodecData(byte[] codecSpecificData) {
        this.codecData = codecSpecificData;
    }

    public void setCompressionFormat(long cFormatCode) {
        this.compressionFormat = cFormatCode;
    }

    public void setErrorConcealment(GUID errConc) {
        this.errorConcealment = errConc;
    }

    public void setSamplingRate(long sampRate) {
        this.samplingRate = sampRate;
    }
}
