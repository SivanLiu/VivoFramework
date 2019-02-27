package com.vivo.audiotags;

import com.vivo.audiotags.generic.TagField;
import java.util.Iterator;
import java.util.List;

public interface Tag {
    public static final String[] DEFAULT_GENRES = new String[]{"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall"};

    void add(TagField tagField);

    void addAlbum(String str);

    void addArtist(String str);

    void addComment(String str);

    void addGenre(String str);

    void addTitle(String str);

    void addTrack(String str);

    void addYear(String str);

    List get(String str);

    List getAlbum();

    List getArtist();

    List getComment();

    Iterator getFields();

    String getFirstAlbum();

    String getFirstArtist();

    String getFirstComment();

    String getFirstGenre();

    String getFirstTitle();

    String getFirstTrack();

    String getFirstYear();

    List getGenre();

    List getTitle();

    List getTrack();

    List getYear();

    boolean hasCommonFields();

    boolean hasField(String str);

    boolean isEmpty();

    void merge(Tag tag);

    void set(TagField tagField);

    void setAlbum(String str);

    void setArtist(String str);

    void setComment(String str);

    boolean setEncoding(String str);

    void setGenre(String str);

    void setTitle(String str);

    void setTrack(String str);

    void setYear(String str);

    String toString();
}
