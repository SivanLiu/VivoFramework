package com.vivo.audiotags.mp3;

import com.vivo.audiotags.generic.AbstractTag;
import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.mp3.util.id3frames.CommId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.TextId3Frame;
import java.util.List;
import java.util.Locale;

public class Id3v2Tag extends AbstractTag {
    public static String DEFAULT_ENCODING = "UTF-16";
    public static byte ID3V22 = (byte) 0;
    public static byte ID3V23 = (byte) 1;
    public static byte ID3V24 = (byte) 2;
    private boolean hasV1 = false;
    private byte representedVersion = ID3V23;

    public Id3v2Tag(byte version) {
        this.representedVersion = version;
    }

    protected TagField createAlbumField(String content) {
        return new TextId3Frame("TALB", content);
    }

    protected TagField createArtistField(String content) {
        return new TextId3Frame("TPE1", content);
    }

    protected TagField createCommentField(String content) {
        return new CommId3Frame(content);
    }

    protected TagField createGenreField(String content) {
        return new TextId3Frame("TCON", content);
    }

    protected TagField createTitleField(String content) {
        return new TextId3Frame("TIT2", content);
    }

    protected TagField createTrackField(String content) {
        return new TextId3Frame("TRCK", content);
    }

    protected TagField createYearField(String content) {
        return new TextId3Frame(Id3V2TagConverter.RECORDING_TIME, content);
    }

    protected String getAlbumId() {
        return "TALB";
    }

    protected String getArtistId() {
        return "TPE1";
    }

    public List getComment() {
        List comments = super.getComment();
        String currIso = Locale.getDefault().getISO3Language();
        Object top = null;
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i) instanceof CommId3Frame) {
                top = (CommId3Frame) comments.get(i);
                if (top.getLangage().equals(currIso)) {
                    comments.remove(i);
                    break;
                }
                top = null;
            }
        }
        if (top != null) {
            comments.add(0, top);
        }
        return comments;
    }

    protected String getCommentId() {
        return "COMM";
    }

    protected String getGenreId() {
        return "TCON";
    }

    public byte getRepresentedVersion() {
        return this.representedVersion;
    }

    protected String getTitleId() {
        return "TIT2";
    }

    protected String getTrackId() {
        return "TRCK";
    }

    protected String getYearId() {
        return Id3V2TagConverter.RECORDING_TIME;
    }

    public boolean hasId3v1() {
        return this.hasV1;
    }

    protected void hasId3v1(boolean b) {
        this.hasV1 = b;
    }

    protected boolean isAllowedEncoding(String enc) {
        boolean result = !enc.equals("ISO-8859-1") ? enc.startsWith("UTF-16") : true;
        if (result || this.representedVersion != ID3V24) {
            return result;
        }
        return !enc.equals("UTF-16BE") ? enc.equals("UTF-8") : true;
    }

    public String toString() {
        return "Id3v2 " + super.toString();
    }

    protected void setRepresentedVersion(byte representedVersion) {
        this.representedVersion = representedVersion;
    }
}
