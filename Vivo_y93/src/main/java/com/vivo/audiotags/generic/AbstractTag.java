package com.vivo.audiotags.generic;

import com.vivo.audiotags.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public abstract class AbstractTag implements Tag {
    protected int commonNumber = 0;
    protected HashMap fields = new HashMap();

    protected abstract TagField createAlbumField(String str);

    protected abstract TagField createArtistField(String str);

    protected abstract TagField createCommentField(String str);

    protected abstract TagField createGenreField(String str);

    protected abstract TagField createTitleField(String str);

    protected abstract TagField createTrackField(String str);

    protected abstract TagField createYearField(String str);

    protected abstract String getAlbumId();

    protected abstract String getArtistId();

    protected abstract String getCommentId();

    protected abstract String getGenreId();

    protected abstract String getTitleId();

    protected abstract String getTrackId();

    protected abstract String getYearId();

    protected abstract boolean isAllowedEncoding(String str);

    public void add(TagField field) {
        if (field != null && !field.isEmpty()) {
            List list = (List) this.fields.get(field.getId());
            if (list == null) {
                list = new ArrayList();
                list.add(field);
                this.fields.put(field.getId(), list);
                if (field.isCommon()) {
                    this.commonNumber++;
                }
            } else {
                list.add(field);
            }
        }
    }

    public void addAlbum(String s) {
        add(createAlbumField(s));
    }

    public void addArtist(String s) {
        add(createArtistField(s));
    }

    public void addComment(String s) {
        add(createCommentField(s));
    }

    public void addGenre(String s) {
        add(createGenreField(s));
    }

    public void addTitle(String s) {
        add(createTitleField(s));
    }

    public void addTrack(String s) {
        add(createTrackField(s));
    }

    public void addYear(String s) {
        add(createYearField(s));
    }

    public List get(String id) {
        List list = (List) this.fields.get(id);
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    public List getAlbum() {
        return get(getAlbumId());
    }

    public List getArtist() {
        return get(getArtistId());
    }

    public List getComment() {
        return get(getCommentId());
    }

    public Iterator getFields() {
        final Iterator it = this.fields.entrySet().iterator();
        return new Iterator() {
            private Iterator fieldsIt;

            private void changeIt() {
                if (it.hasNext()) {
                    this.fieldsIt = ((List) ((Entry) it.next()).getValue()).iterator();
                }
            }

            public boolean hasNext() {
                if (this.fieldsIt == null) {
                    changeIt();
                }
                if (it.hasNext()) {
                    return true;
                }
                return this.fieldsIt != null ? this.fieldsIt.hasNext() : false;
            }

            public Object next() {
                if (!this.fieldsIt.hasNext()) {
                    changeIt();
                }
                return this.fieldsIt.next();
            }

            public void remove() {
                this.fieldsIt.remove();
            }
        };
    }

    public String getFirstAlbum() {
        List l = get(getAlbumId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstArtist() {
        List l = get(getArtistId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstComment() {
        List l = get(getCommentId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstGenre() {
        List l = get(getGenreId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstTitle() {
        List l = get(getTitleId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstTrack() {
        List l = get(getTrackId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public String getFirstYear() {
        List l = get(getYearId());
        return l.size() != 0 ? ((TagTextField) l.get(0)).getContent() : "";
    }

    public List getGenre() {
        return get(getGenreId());
    }

    public List getTitle() {
        return get(getTitleId());
    }

    public List getTrack() {
        return get(getTrackId());
    }

    public List getYear() {
        return get(getYearId());
    }

    public boolean hasCommonFields() {
        return this.commonNumber != 0;
    }

    public boolean hasField(String id) {
        return get(id).size() != 0;
    }

    public boolean isEmpty() {
        return this.fields.size() == 0;
    }

    public void merge(Tag tag) {
        if (getTitle().size() == 0) {
            setTitle(tag.getFirstTitle());
        }
        if (getArtist().size() == 0) {
            setArtist(tag.getFirstArtist());
        }
        if (getAlbum().size() == 0) {
            setAlbum(tag.getFirstAlbum());
        }
        if (getYear().size() == 0) {
            setYear(tag.getFirstYear());
        }
        if (getComment().size() == 0) {
            setComment(tag.getFirstComment());
        }
        if (getTrack().size() == 0) {
            setTrack(tag.getFirstTrack());
        }
        if (getGenre().size() == 0) {
            setGenre(tag.getFirstGenre());
        }
    }

    public void set(TagField field) {
        if (field != null) {
            if (field.isEmpty()) {
                if (this.fields.remove(field.getId()) != null && field.isCommon()) {
                    this.commonNumber--;
                }
                return;
            }
            List l = (List) this.fields.get(field.getId());
            if (l != null) {
                ((TagField) l.get(0)).copyContent(field);
                return;
            }
            l = new ArrayList();
            l.add(field);
            this.fields.put(field.getId(), l);
            if (field.isCommon()) {
                this.commonNumber++;
            }
        }
    }

    public void setAlbum(String s) {
        set(createAlbumField(s));
    }

    public void setArtist(String s) {
        set(createArtistField(s));
    }

    public void setComment(String s) {
        set(createCommentField(s));
    }

    public boolean setEncoding(String enc) {
        if (!isAllowedEncoding(enc)) {
            return false;
        }
        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            if (field instanceof TagTextField) {
                ((TagTextField) field).setEncoding(enc);
            }
        }
        return true;
    }

    public void setGenre(String s) {
        set(createGenreField(s));
    }

    public void setTitle(String s) {
        set(createTitleField(s));
    }

    public void setTrack(String s) {
        set(createTrackField(s));
    }

    public void setYear(String s) {
        set(createYearField(s));
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Tag content:\n");
        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            out.append("\t");
            out.append(field.getId());
            out.append(" : ");
            out.append(field.toString());
            out.append("\n");
        }
        return out.toString().substring(0, out.length() - 1);
    }
}
