package com.vivo.audiotags;

import java.util.Enumeration;
import java.util.Hashtable;

public class EncodingInfo {
    public static final String FIELD_BITRATE = "BITRATE";
    public static final String FIELD_CHANNEL = "CHANNB";
    public static final String FIELD_INFOS = "INFOS";
    public static final String FIELD_LENGTH = "LENGTH";
    public static final String FIELD_SAMPLERATE = "SAMPLING";
    public static final String FIELD_TYPE = "TYPE";
    public static final String FIELD_VBR = "VBR";
    private Hashtable content = new Hashtable(6);

    public EncodingInfo() {
        this.content.put(FIELD_BITRATE, new Integer(-1));
        this.content.put(FIELD_CHANNEL, new Integer(-1));
        this.content.put(FIELD_TYPE, "");
        this.content.put(FIELD_INFOS, "");
        this.content.put(FIELD_SAMPLERATE, new Integer(-1));
        this.content.put(FIELD_LENGTH, new Float(-1.0f));
        this.content.put(FIELD_VBR, new Boolean(true));
    }

    public int getBitrate() {
        return ((Integer) this.content.get(FIELD_BITRATE)).intValue();
    }

    public int getChannelNumber() {
        return ((Integer) this.content.get(FIELD_CHANNEL)).intValue();
    }

    public String getEncodingType() {
        return (String) this.content.get(FIELD_TYPE);
    }

    public String getExtraEncodingInfos() {
        return (String) this.content.get(FIELD_INFOS);
    }

    public int getLength() {
        return (int) getPreciseLength();
    }

    public float getPreciseLength() {
        return ((Float) this.content.get(FIELD_LENGTH)).floatValue() * 1000.0f;
    }

    public int getSamplingRate() {
        return ((Integer) this.content.get(FIELD_SAMPLERATE)).intValue();
    }

    public boolean isVbr() {
        return ((Boolean) this.content.get(FIELD_VBR)).booleanValue();
    }

    public void setBitrate(int bitrate) {
        this.content.put(FIELD_BITRATE, new Integer(bitrate));
    }

    public void setChannelNumber(int chanNb) {
        this.content.put(FIELD_CHANNEL, new Integer(chanNb));
    }

    public void setEncodingType(String encodingType) {
        this.content.put(FIELD_TYPE, encodingType);
    }

    public void setExtraEncodingInfos(String infos) {
        this.content.put(FIELD_INFOS, infos);
    }

    public void setLength(int length) {
        this.content.put(FIELD_LENGTH, new Float((float) length));
    }

    public void setPreciseLength(float seconds) {
        this.content.put(FIELD_LENGTH, new Float(seconds));
    }

    public void setSamplingRate(int samplingRate) {
        this.content.put(FIELD_SAMPLERATE, new Integer(samplingRate));
    }

    public void setVbr(boolean b) {
        this.content.put(FIELD_VBR, new Boolean(b));
    }

    public String toString() {
        StringBuffer out = new StringBuffer(50);
        out.append("Encoding infos content:\n");
        Enumeration en = this.content.keys();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            Object val = this.content.get(key);
            out.append("\t");
            out.append(key);
            out.append(" : ");
            out.append(val);
            out.append("\n");
        }
        return out.toString().substring(0, out.length() - 1);
    }
}
