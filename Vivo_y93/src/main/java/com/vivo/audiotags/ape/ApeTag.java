package com.vivo.audiotags.ape;

import com.vivo.audiotags.ape.util.ApeTagTextField;
import com.vivo.audiotags.generic.AbstractTag;
import com.vivo.audiotags.generic.TagField;

public class ApeTag extends AbstractTag {
    protected String getArtistId() {
        return "Artist";
    }

    protected String getAlbumId() {
        return "Album";
    }

    protected String getTitleId() {
        return "Title";
    }

    protected String getTrackId() {
        return "Track";
    }

    protected String getYearId() {
        return "Year";
    }

    protected String getCommentId() {
        return "Comment";
    }

    protected String getGenreId() {
        return "Genre";
    }

    protected TagField createArtistField(String content) {
        return new ApeTagTextField("Artist", content);
    }

    protected TagField createAlbumField(String content) {
        return new ApeTagTextField("Album", content);
    }

    protected TagField createTitleField(String content) {
        return new ApeTagTextField("Title", content);
    }

    protected TagField createTrackField(String content) {
        return new ApeTagTextField("Track", content);
    }

    protected TagField createYearField(String content) {
        return new ApeTagTextField("Year", content);
    }

    protected TagField createCommentField(String content) {
        return new ApeTagTextField("Comment", content);
    }

    protected TagField createGenreField(String content) {
        return new ApeTagTextField("Genre", content);
    }

    protected boolean isAllowedEncoding(String enc) {
        return enc.equals("UTF-8");
    }

    public String toString() {
        return "APE " + super.toString();
    }
}
