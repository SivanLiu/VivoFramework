package com.vivo.audiotags;

import com.vivo.audiotags.exceptions.CannotWriteException;
import com.vivo.audiotags.generic.GenericTag;
import java.io.File;

public class AudioFile extends File {
    private EncodingInfo info;
    private Tag tag;

    public AudioFile(File f, EncodingInfo info) {
        super(f.getAbsolutePath());
        this.info = info;
        this.tag = new GenericTag();
    }

    public AudioFile(File f, EncodingInfo info, Tag tag) {
        super(f.getAbsolutePath());
        this.info = info;
        this.tag = tag;
    }

    public AudioFile(String s, EncodingInfo info) {
        super(s);
        this.info = info;
        this.tag = new GenericTag();
    }

    public AudioFile(String s, EncodingInfo info, Tag tag) {
        super(s);
        this.info = info;
        this.tag = tag;
    }

    public void commit() throws CannotWriteException {
        AudioFileIO.write(this);
    }

    public int getBitrate() {
        return this.info.getBitrate();
    }

    public int getChannelNumber() {
        return this.info.getChannelNumber();
    }

    public String getEncodingType() {
        return this.info.getEncodingType();
    }

    public String getExtraEncodingInfos() {
        return this.info.getExtraEncodingInfos();
    }

    public int getLength() {
        return this.info.getLength();
    }

    public float getPreciseLength() {
        return this.info.getPreciseLength();
    }

    public int getSamplingRate() {
        return this.info.getSamplingRate();
    }

    public Tag getTag() {
        return this.tag == null ? new GenericTag() : this.tag;
    }

    public boolean isVbr() {
        return this.info.isVbr();
    }

    public String toString() {
        return "AudioFile " + getAbsolutePath() + "  --------\n" + this.info.toString() + "\n" + (this.tag == null ? "" : this.tag.toString()) + "\n-------------------";
    }
}
