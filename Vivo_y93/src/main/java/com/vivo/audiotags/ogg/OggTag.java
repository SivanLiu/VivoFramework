package com.vivo.audiotags.ogg;

import com.vivo.audiotags.generic.AbstractTag;
import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.ogg.util.OggTagField;

public class OggTag extends AbstractTag {
    public static final String DEFAULT_VENDOR = "Entagged - The Musical Box";
    private String vendor = "";

    protected TagField createAlbumField(String content) {
        return new OggTagField("ALBUM", content);
    }

    protected TagField createArtistField(String content) {
        return new OggTagField("ARTIST", content);
    }

    protected TagField createCommentField(String content) {
        return new OggTagField("DESCRIPTION", content);
    }

    protected TagField createGenreField(String content) {
        return new OggTagField("GENRE", content);
    }

    protected TagField createTitleField(String content) {
        return new OggTagField("TITLE", content);
    }

    protected TagField createTrackField(String content) {
        return new OggTagField("TRACKNUMBER", content);
    }

    protected TagField createYearField(String content) {
        return new OggTagField("DATE", content);
    }

    protected String getAlbumId() {
        return "ALBUM";
    }

    protected String getArtistId() {
        return "ARTIST";
    }

    protected String getCommentId() {
        return "DESCRIPTION";
    }

    protected String getGenreId() {
        return "GENRE";
    }

    protected String getTitleId() {
        return "TITLE";
    }

    protected String getTrackId() {
        return "TRACKNUMBER";
    }

    public String getVendor() {
        if (this.vendor.trim().equals("")) {
            return DEFAULT_VENDOR;
        }
        return this.vendor;
    }

    protected String getYearId() {
        return "DATE";
    }

    public void setVendor(String vendor) {
        if (vendor == null) {
            this.vendor = "";
        } else {
            this.vendor = vendor;
        }
    }

    protected boolean isAllowedEncoding(String enc) {
        return enc.equals("UTF-8");
    }

    public String toString() {
        return "OGG " + super.toString();
    }
}
