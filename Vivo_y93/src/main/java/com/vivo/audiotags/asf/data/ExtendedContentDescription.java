package com.vivo.audiotags.asf.data;

import com.vivo.audiotags.Tag;
import com.vivo.audiotags.asf.util.Utils;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ExtendedContentDescription extends Chunk {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f0-assertionsDisabled = (ExtendedContentDescription.class.desiredAssertionStatus() ^ 1);
    private final ArrayList descriptors;
    private HashMap indexMap;

    public ExtendedContentDescription() {
        this(0, BigInteger.valueOf(0));
    }

    public ExtendedContentDescription(long pos, BigInteger chunkLen) {
        super(GUID.GUID_EXTENDED_CONTENT_DESCRIPTION, pos, chunkLen);
        this.indexMap = null;
        this.descriptors = new ArrayList();
    }

    public void addDescriptor(ContentDescriptor toAdd) {
        if (!f0-assertionsDisabled && toAdd == null) {
            throw new AssertionError("Argument must not be null.");
        } else if (getDescriptor(toAdd.getName()) != null) {
            throw new RuntimeException(toAdd.getName() + " is already present");
        } else {
            this.descriptors.add(toAdd);
            this.indexMap.put(toAdd.getName(), new Integer(this.descriptors.size() - 1));
        }
    }

    public void addOrReplace(ContentDescriptor descriptor) {
        if (f0-assertionsDisabled || descriptor != null) {
            if (getDescriptor(descriptor.getName()) != null) {
                remove(descriptor.getName());
            }
            addDescriptor(descriptor);
            return;
        }
        throw new AssertionError("Argument must not be null");
    }

    public String getAlbum() {
        ContentDescriptor result = getDescriptor(ContentDescriptor.ID_ALBUM);
        if (result == null) {
            return "";
        }
        return result.getString();
    }

    public String getArtist() {
        ContentDescriptor result = getDescriptor(ContentDescriptor.ID_ARTIST);
        if (result == null) {
            return "";
        }
        return result.getString();
    }

    public byte[] getBytes() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            content.write(Utils.getBytes((long) this.descriptors.size(), 2));
            Iterator it = this.descriptors.iterator();
            while (it.hasNext()) {
                content.write(((ContentDescriptor) it.next()).getBytes());
            }
            byte[] contentBytes = content.toByteArray();
            result.write(GUID.GUID_EXTENDED_CONTENT_DESCRIPTION.getBytes());
            result.write(Utils.getBytes((long) (contentBytes.length + 24), 8));
            result.write(contentBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toByteArray();
    }

    public ContentDescriptor getDescriptor(String name) {
        if (this.indexMap == null) {
            this.indexMap = new HashMap();
            for (int i = 0; i < this.descriptors.size(); i++) {
                this.indexMap.put(((ContentDescriptor) this.descriptors.get(i)).getName(), new Integer(i));
            }
        }
        Integer pos = (Integer) this.indexMap.get(name);
        if (pos != null) {
            return (ContentDescriptor) this.descriptors.get(pos.intValue());
        }
        return null;
    }

    public long getDescriptorCount() {
        return (long) this.descriptors.size();
    }

    public Collection getDescriptors() {
        return new ArrayList(this.descriptors);
    }

    public String getGenre() {
        ContentDescriptor prop = getDescriptor(ContentDescriptor.ID_GENRE);
        if (prop != null) {
            return prop.getString();
        }
        prop = getDescriptor(ContentDescriptor.ID_GENREID);
        if (prop == null) {
            return "";
        }
        String result = prop.getString();
        if (!result.startsWith("(") || !result.endsWith(")")) {
            return result;
        }
        result = result.substring(1, result.length() - 1);
        try {
            int genreNum = Integer.parseInt(result);
            if (genreNum < 0 || genreNum >= Tag.DEFAULT_GENRES.length) {
                return result;
            }
            return Tag.DEFAULT_GENRES[genreNum];
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public String getTrack() {
        ContentDescriptor result = getDescriptor(ContentDescriptor.ID_TRACKNUMBER);
        if (result == null) {
            return "";
        }
        return result.getString();
    }

    public String getYear() {
        ContentDescriptor result = getDescriptor(ContentDescriptor.ID_YEAR);
        if (result == null) {
            return "";
        }
        return result.getString();
    }

    public String prettyPrint() {
        StringBuffer result = new StringBuffer(super.prettyPrint());
        result.insert(0, "\nExtended Content Description:\n");
        ContentDescriptor[] list = (ContentDescriptor[]) this.descriptors.toArray(new ContentDescriptor[this.descriptors.size()]);
        Arrays.sort(list);
        for (Object append : list) {
            result.append("   ");
            result.append(append);
            result.append(Utils.LINE_SEPARATOR);
        }
        return result.toString();
    }

    public ContentDescriptor remove(String id) {
        ContentDescriptor result = getDescriptor(id);
        if (result != null) {
            this.descriptors.remove(result);
        }
        this.indexMap = null;
        return result;
    }
}
