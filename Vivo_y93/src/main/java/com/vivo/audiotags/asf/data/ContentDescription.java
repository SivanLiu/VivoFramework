package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.asf.util.Utils;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class ContentDescription extends Chunk {
    private String author;
    private String copyRight;
    private String description;
    private String rating;
    private String title;

    public ContentDescription() {
        this(0, BigInteger.valueOf(0));
    }

    public ContentDescription(long pos, BigInteger chunkLen) {
        super(GUID.GUID_CONTENTDESCRIPTION, pos, chunkLen);
        this.author = null;
        this.copyRight = null;
        this.description = null;
        this.rating = null;
        this.title = null;
    }

    public String getAuthor() {
        if (this.author == null) {
            return "";
        }
        return this.author;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            int i;
            ByteArrayOutputStream tags = new ByteArrayOutputStream();
            String[] toWrite = new String[]{getTitle(), getAuthor(), getCopyRight(), getComment(), getRating()};
            byte[][] stringRepresentations = new byte[toWrite.length][];
            for (i = 0; i < toWrite.length; i++) {
                stringRepresentations[i] = toWrite[i].getBytes("UTF-16LE");
            }
            for (byte[] length : stringRepresentations) {
                tags.write(Utils.getBytes((long) (length.length + 2), 2));
            }
            for (i = 0; i < toWrite.length; i++) {
                tags.write(stringRepresentations[i]);
                tags.write(Utils.getBytes(0, 2));
            }
            byte[] tagContent = tags.toByteArray();
            result.write(GUID.GUID_CONTENTDESCRIPTION.getBytes());
            result.write(Utils.getBytes((long) (tagContent.length + 24), 8));
            result.write(tagContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toByteArray();
    }

    public String getComment() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public String getCopyRight() {
        if (this.copyRight == null) {
            return "";
        }
        return this.copyRight;
    }

    public String getRating() {
        if (this.rating == null) {
            return "";
        }
        return this.rating;
    }

    public String getTitle() {
        if (this.title == null) {
            return "";
        }
        return this.title;
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, Utils.LINE_SEPARATOR + "Content Description:" + Utils.LINE_SEPARATOR);
        result.append("   Title      : " + getTitle() + Utils.LINE_SEPARATOR);
        result.append("   Author     : " + getAuthor() + Utils.LINE_SEPARATOR);
        result.append("   Copyright  : " + getCopyRight() + Utils.LINE_SEPARATOR);
        result.append("   Description: " + getComment() + Utils.LINE_SEPARATOR);
        result.append("   Rating     :" + getRating() + Utils.LINE_SEPARATOR);
        return result.toString();
    }

    public void setAuthor(String fileAuthor) throws IllegalArgumentException {
        Utils.checkStringLengthNullSafe(fileAuthor);
        this.author = fileAuthor;
    }

    public void setComment(String tagComment) throws IllegalArgumentException {
        Utils.checkStringLengthNullSafe(tagComment);
        this.description = tagComment;
    }

    public void setCopyRight(String cpright) throws IllegalArgumentException {
        Utils.checkStringLengthNullSafe(cpright);
        this.copyRight = cpright;
    }

    public void setRating(String ratingText) throws IllegalArgumentException {
        Utils.checkStringLengthNullSafe(ratingText);
        this.rating = ratingText;
    }

    public void setTitle(String songTitle) throws IllegalArgumentException {
        Utils.checkStringLengthNullSafe(songTitle);
        this.title = songTitle;
    }
}
