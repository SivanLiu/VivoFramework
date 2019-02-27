package com.vivo.audiotags.generic;

public class GenericTag extends AbstractTag {
    public static final int ALBUM = 1;
    public static final int ARTIST = 0;
    public static final int COMMENT = 6;
    public static final int GENRE = 5;
    public static final int TITLE = 2;
    public static final int TRACK = 3;
    public static final int YEAR = 4;
    private static final String[] keys = new String[]{"ARTIST", "ALBUM", "TITLE", "TRACK", "YEAR", "GENRE", "COMMENT"};

    private class GenericTagTextField implements TagTextField {
        private String content;
        private final String id;

        public GenericTagTextField(String fieldId, String initialContent) {
            this.id = fieldId;
            this.content = initialContent;
        }

        public void copyContent(TagField field) {
            if (field instanceof TagTextField) {
                this.content = ((TagTextField) field).getContent();
            }
        }

        public String getContent() {
            return this.content;
        }

        public String getEncoding() {
            return "ISO-8859-1";
        }

        public String getId() {
            return this.id;
        }

        public byte[] getRawContent() {
            return this.content == null ? new byte[0] : this.content.getBytes();
        }

        public boolean isBinary() {
            return false;
        }

        public void isBinary(boolean b) {
        }

        public boolean isCommon() {
            return true;
        }

        public boolean isEmpty() {
            return this.content.equals("");
        }

        public void setContent(String s) {
            this.content = s;
        }

        public void setEncoding(String s) {
        }

        public String toString() {
            return getId() + " : " + getContent();
        }
    }

    protected TagField createAlbumField(String content) {
        return new GenericTagTextField(keys[1], content);
    }

    protected TagField createArtistField(String content) {
        return new GenericTagTextField(keys[0], content);
    }

    protected TagField createCommentField(String content) {
        return new GenericTagTextField(keys[6], content);
    }

    protected TagField createGenreField(String content) {
        return new GenericTagTextField(keys[5], content);
    }

    protected TagField createTitleField(String content) {
        return new GenericTagTextField(keys[2], content);
    }

    protected TagField createTrackField(String content) {
        return new GenericTagTextField(keys[3], content);
    }

    protected TagField createYearField(String content) {
        return new GenericTagTextField(keys[4], content);
    }

    protected String getAlbumId() {
        return keys[1];
    }

    protected String getArtistId() {
        return keys[0];
    }

    protected String getCommentId() {
        return keys[6];
    }

    protected String getGenreId() {
        return keys[5];
    }

    protected String getTitleId() {
        return keys[2];
    }

    protected String getTrackId() {
        return keys[3];
    }

    protected String getYearId() {
        return keys[4];
    }

    protected boolean isAllowedEncoding(String enc) {
        return true;
    }
}
