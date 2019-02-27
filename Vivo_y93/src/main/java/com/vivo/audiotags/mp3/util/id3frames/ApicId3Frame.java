package com.vivo.audiotags.mp3.util.id3frames;

import com.vivo.audiotags.asf.data.GUID;
import com.vivo.audiotags.generic.TagField;
import java.io.UnsupportedEncodingException;

public class ApicId3Frame extends TextId3Frame {
    private byte[] data;
    private String mime;
    private byte pictureType;
    private boolean unsupportedState = false;

    public ApicId3Frame(byte[] rawContent, byte version) throws UnsupportedEncodingException {
        super("APIC", rawContent, version);
    }

    public ApicId3Frame(String description, String mime, byte pictureType, byte[] data) {
        super("APIC", description);
        this.mime = mime;
        this.pictureType = pictureType;
        this.data = data;
    }

    protected byte[] build() throws UnsupportedEncodingException {
        if (this.unsupportedState) {
            return this.data;
        }
        byte[] contentB = getBytes(this.content, getEncoding());
        byte[] mimeB = getBytes(this.mime, "ISO-8859-1");
        byte[] b = new byte[((((((this.flags.length + 8) + 1) + mimeB.length) + 1) + contentB.length) + this.data.length)];
        copy(getIdBytes(), b, 0);
        copy(getSize(b.length - 10), b, 4);
        copy(this.flags, b, 4 + 4);
        int offset = this.flags.length + 8;
        b[offset] = this.encoding;
        offset++;
        copy(mimeB, b, offset);
        offset += mimeB.length;
        b[offset] = this.pictureType;
        offset++;
        copy(contentB, b, offset);
        offset += contentB.length;
        copy(this.data, b, offset);
        offset += this.data.length;
        return b;
    }

    public void copyContent(TagField field) {
        super.copyContent(field);
        if (!(field instanceof ApicId3Frame)) {
            return;
        }
        if (((ApicId3Frame) field).unsupportedState) {
            this.data = ((ApicId3Frame) field).data;
            this.unsupportedState = true;
            return;
        }
        this.mime = ((ApicId3Frame) field).getMimeType();
        this.pictureType = ((ApicId3Frame) field).getPictureType();
        this.data = ((ApicId3Frame) field).getData();
    }

    public byte[] getData() {
        return this.data;
    }

    public String getMimeType() {
        return this.mime;
    }

    public byte getPictureType() {
        return this.pictureType;
    }

    public String getPictureTypeAsString() {
        switch (this.pictureType & 255) {
            case 0:
                return "Other";
            case 1:
                return "32x32 pixels file icon";
            case 2:
                return "Other file icon";
            case 3:
                return "Cover (front)";
            case 4:
                return "Cover (back)";
            case 5:
                return "Leaflet page";
            case 6:
                return "Media (e.g. lable side of CD)";
            case 7:
                return "Lead artist/lead performer/soloist";
            case 8:
                return "Artist/performer";
            case 9:
                return "Conductor";
            case 10:
                return "Band/Orchestra";
            case 11:
                return "Composer";
            case 12:
                return "Lyricist/text writer";
            case 13:
                return "Recording Location";
            case 14:
                return "During recording";
            case 15:
                return "During performance";
            case GUID.GUID_LENGTH /*16*/:
                return "Movie/video screen capture";
            case 17:
                return "A bright coloured fish";
            case 18:
                return "Illustration";
            case 19:
                return "Band/artist logotype";
            case 20:
                return "Publisher/Studio logotype";
            default:
                return "Unknown";
        }
    }

    public boolean isBinary() {
        return true;
    }

    public boolean isEmpty() {
        return (super.isEmpty() && this.data.length == 0) ? this.mime.equals("") : false;
    }

    protected void populate(byte[] raw) throws UnsupportedEncodingException {
        this.data = new byte[0];
        this.encoding = raw[this.flags.length];
        if (this.encoding < (byte) 0 || this.encoding > (byte) 3) {
            this.encoding = (byte) 0;
        }
        int offset = indexOfFirstNull(raw, this.flags.length + 1);
        this.mime = getString(raw, this.flags.length + 1, (offset - this.flags.length) - 1, "ISO-8859-1");
        if (this.mime != null) {
            if (this.mime.trim().equals("-->")) {
                this.unsupportedState = true;
                this.data = raw;
                return;
            }
            this.pictureType = raw[offset + 1];
            int nextoffset = indexOfFirstNull(raw, offset + 2);
            this.content = getString(raw, offset + 2, (nextoffset - offset) - 2, getEncoding());
            if (this.encoding == (byte) 2 || this.encoding == (byte) 3) {
                nextoffset++;
            }
            nextoffset++;
            if (raw.length > nextoffset) {
                this.data = new byte[(raw.length - nextoffset)];
                System.arraycopy(raw, nextoffset, this.data, 0, this.data.length);
            } else {
                System.err.println("ApicId3Frame-> No space for picture data left.");
            }
        }
    }

    public String toString() {
        return "[" + this.mime + " (" + getPictureTypeAsString() + ")] " + super.toString();
    }
}
