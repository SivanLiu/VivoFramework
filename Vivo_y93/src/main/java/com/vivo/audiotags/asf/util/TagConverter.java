package com.vivo.audiotags.asf.util;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.asf.data.AsfHeader;
import com.vivo.audiotags.asf.data.ContentDescription;
import com.vivo.audiotags.asf.data.ContentDescriptor;
import com.vivo.audiotags.asf.data.ExtendedContentDescription;
import com.vivo.audiotags.asf.data.wrapper.ContentDescriptorTagField;
import com.vivo.audiotags.generic.GenericTag;
import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.generic.TagTextField;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;

public class TagConverter {
    public static void assignCommonTagValues(Tag tag, ExtendedContentDescription description) {
        ContentDescriptor tmp;
        if (tag.getFirstAlbum() == null || tag.getFirstAlbum().length() <= 0) {
            description.remove(ContentDescriptor.ID_ALBUM);
        } else {
            tmp = new ContentDescriptor(ContentDescriptor.ID_ALBUM, 0);
            tmp.setStringValue(tag.getFirstAlbum());
            description.addOrReplace(tmp);
        }
        if (tag.getFirstTrack() == null || tag.getFirstTrack().length() <= 0) {
            description.remove(ContentDescriptor.ID_TRACKNUMBER);
        } else {
            tmp = new ContentDescriptor(ContentDescriptor.ID_TRACKNUMBER, 0);
            tmp.setStringValue(tag.getFirstTrack());
            description.addOrReplace(tmp);
        }
        if (tag.getFirstYear() == null || tag.getFirstYear().length() <= 0) {
            description.remove(ContentDescriptor.ID_YEAR);
        } else {
            tmp = new ContentDescriptor(ContentDescriptor.ID_YEAR, 0);
            tmp.setStringValue(tag.getFirstYear());
            description.addOrReplace(tmp);
        }
        if (tag.getFirstGenre() == null || tag.getFirstGenre().length() <= 0) {
            description.remove(ContentDescriptor.ID_GENRE);
            description.remove(ContentDescriptor.ID_GENREID);
            return;
        }
        tmp = new ContentDescriptor(ContentDescriptor.ID_GENRE, 0);
        tmp.setStringValue(tag.getFirstGenre());
        description.addOrReplace(tmp);
        int index = Arrays.asList(Tag.DEFAULT_GENRES).indexOf(tag.getFirstGenre());
        if (index != -1) {
            tmp = new ContentDescriptor(ContentDescriptor.ID_GENREID, 0);
            tmp.setStringValue("(" + index + ")");
            description.addOrReplace(tmp);
            return;
        }
        description.remove(ContentDescriptor.ID_GENREID);
    }

    public static void assignOptionalTagValues(Tag tag, ExtendedContentDescription descriptor) {
        UnsupportedEncodingException uee;
        Iterator it = tag.getFields();
        while (it.hasNext()) {
            try {
                TagField currentField = (TagField) it.next();
                if (!currentField.isCommon()) {
                    ContentDescriptor tmp = new ContentDescriptor(currentField.getId(), 0);
                    try {
                        if (currentField.isBinary()) {
                            tmp.setBinaryValue(currentField.getRawContent());
                        } else {
                            tmp.setStringValue(currentField.toString());
                        }
                        descriptor.addOrReplace(tmp);
                    } catch (UnsupportedEncodingException e) {
                        uee = e;
                        ContentDescriptor contentDescriptor = tmp;
                        uee.printStackTrace();
                    }
                }
            } catch (UnsupportedEncodingException e2) {
                uee = e2;
                uee.printStackTrace();
            }
        }
    }

    public static ContentDescription createContentDescription(Tag tag) {
        ContentDescription result = new ContentDescription();
        result.setAuthor(tag.getFirstArtist());
        result.setTitle(tag.getFirstTitle());
        result.setComment(tag.getFirstComment());
        TagTextField cpField = AsfCopyrightField.getCopyright(tag);
        if (cpField != null) {
            result.setCopyRight(cpField.getContent());
        }
        return result;
    }

    public static ExtendedContentDescription createExtendedContentDescription(Tag tag) {
        ExtendedContentDescription result = new ExtendedContentDescription();
        assignCommonTagValues(tag, result);
        return result;
    }

    public static Tag createTagOf(AsfHeader source) {
        GenericTag result = new GenericTag();
        if (source.getContentDescription() != null) {
            result.setArtist(source.getContentDescription().getAuthor());
            result.setComment(source.getContentDescription().getComment());
            result.setTitle(source.getContentDescription().getTitle());
            AsfCopyrightField cpField = new AsfCopyrightField();
            cpField.setContent(source.getContentDescription().getCopyRight());
            result.set(cpField);
        }
        if (source.getExtendedContentDescription() != null) {
            result.setTrack(source.getExtendedContentDescription().getTrack());
            result.setYear(source.getExtendedContentDescription().getYear());
            result.setGenre(source.getExtendedContentDescription().getGenre());
            result.setAlbum(source.getExtendedContentDescription().getAlbum());
            for (ContentDescriptor current : source.getExtendedContentDescription().getDescriptors()) {
                if (!current.isCommon()) {
                    result.add(new ContentDescriptorTagField(current));
                }
            }
        }
        return result;
    }
}
