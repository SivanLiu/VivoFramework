package com.vivo.audiotags.asf.data.wrapper;

import com.vivo.audiotags.asf.data.ContentDescriptor;
import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public class ContentDescriptorTagField implements TagField {
    private ContentDescriptor toWrap;

    public ContentDescriptorTagField(ContentDescriptor source) {
        this.toWrap = source.createCopy();
    }

    public void copyContent(TagField field) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String getId() {
        return this.toWrap.getName();
    }

    public byte[] getRawContent() throws UnsupportedEncodingException {
        return this.toWrap.getRawData();
    }

    public boolean isBinary() {
        return this.toWrap.getType() == 1;
    }

    public void isBinary(boolean b) {
        if (b || !isBinary()) {
            this.toWrap.setBinaryValue(this.toWrap.getRawData());
            return;
        }
        throw new UnsupportedOperationException("No conversion supported.");
    }

    public boolean isCommon() {
        return this.toWrap.isCommon();
    }

    public boolean isEmpty() {
        return this.toWrap.isEmpty();
    }

    public String toString() {
        return this.toWrap.getString();
    }
}
