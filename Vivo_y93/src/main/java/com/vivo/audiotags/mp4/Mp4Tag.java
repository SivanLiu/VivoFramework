package com.vivo.audiotags.mp4;

import com.vivo.audiotags.generic.AbstractTag;
import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.mp4.util.Mp4TagTextField;
import com.vivo.audiotags.mp4.util.Mp4TagTextNumberField;

public class Mp4Tag extends AbstractTag {
    protected String getArtistId() {
        return "ART";
    }

    protected String getAlbumId() {
        return "alb";
    }

    protected String getTitleId() {
        return "nam";
    }

    protected String getTrackId() {
        return "trkn";
    }

    protected String getYearId() {
        return "day";
    }

    protected String getCommentId() {
        return "cmt";
    }

    protected String getGenreId() {
        return "gen";
    }

    protected TagField createArtistField(String content) {
        return new Mp4TagTextField("ART", content);
    }

    protected TagField createAlbumField(String content) {
        return new Mp4TagTextField("alb", content);
    }

    protected TagField createTitleField(String content) {
        return new Mp4TagTextField("nam", content);
    }

    protected TagField createTrackField(String content) {
        return new Mp4TagTextNumberField("trkn", content);
    }

    protected TagField createYearField(String content) {
        return new Mp4TagTextField("day", content);
    }

    protected TagField createCommentField(String content) {
        return new Mp4TagTextField("cmt", content);
    }

    protected TagField createGenreField(String content) {
        return new Mp4TagTextField("gen", content);
    }

    protected boolean isAllowedEncoding(String enc) {
        return enc.equals("ISO-8859-1");
    }

    public String toString() {
        return "Mpeg4 " + super.toString();
    }
}
