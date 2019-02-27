package com.vivo.audiotags.mp3;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.mp3.util.id3frames.TextId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.TimeId3Frame;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class Id3V2TagConverter {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f3-assertionsDisabled = (Id3V2TagConverter.class.desiredAssertionStatus() ^ 1);
    public static final String DATE = "TDAT";
    public static final String RECORDING_TIME = "TDRC";
    public static final String RECORD_DAT = "TRDA";
    public static final String TIME = "TIME";
    public static final String YEAR = "TYER";
    private static final HashMap conversion22to23 = new HashMap();
    private static final HashSet discard24 = new HashSet(Arrays.asList(new String[]{RECORD_DAT}));
    private static final HashSet specialStore24 = new HashSet(Arrays.asList(new String[]{TIME, YEAR, DATE}));

    static {
        String[] v22 = new String[]{"BUF", "CNT", "COM", "CRA", "CRM", "ETC", "EQU", "GEO", "IPL", "LNK", "MCI", "MLL", "PIC", "POP", "REV", "RVA", "SLT", "STC", "TAL", "TBP", "TCM", "TCO", "TCR", "TDA", "TDY", "TEN", "TFT", "TIM", "TKE", "TLA", "TLE", "TMT", "TOA", "TOF", "TOL", "TOR", "TOT", "TP1", "TP2", "TP3", "TP4", "TPA", "TPB", "TRC", "TRD", "TRK", "TSI", "TSS", "TT1", "TT2", "TT3", "TXT", "TXX", "TYE", "UFI", "ULT", "WAF", "WAR", "WAS", "WCM", "WCP", "WPB", "WXX"};
        String[] v23 = new String[]{"RBUF", "PCNT", "COMM", "AENC", "", "ETCO", "EQUA", "GEOB", "IPLS", "LINK", "MCDI", "MLLT", "APIC", "POPM", "RVRB", "RVAD", "SYLT", "SYTC", "TALB", "TBPM", "TCOM", "TCON", "TCOP", DATE, "TDLY", "TENC", "TFLT", TIME, "TKEY", "TLAN", "TLEN", "TMED", "TOPE", "TOFN", "TOLY", "TORY", "TOAL", "TPE1", "TPE2", "TPE3", "TPE4", "TPOS", "TPUB", "TSRC", RECORD_DAT, "TRCK", "TSIZ", "TSSE", "TIT1", "TIT2", "TIT3", "TEXT", "TXXX", YEAR, "UFID", "USLT", "WOAF", "WOAR", "WOAS", "WCOM", "WCOP", "WPUB", "WXXX"};
        for (int i = 0; i < v22.length; i++) {
            conversion22to23.put(v22[i], v23[i]);
        }
        discard24.addAll(specialStore24);
    }

    public static Id3v2Tag convert(Id3v2Tag tag, int targetVersion) {
        if (f3-assertionsDisabled || (tag != null && ((targetVersion == Id3v2Tag.ID3V22 || targetVersion == Id3v2Tag.ID3V23 || targetVersion == Id3v2Tag.ID3V24) && (tag.getRepresentedVersion() == Id3v2Tag.ID3V22 || tag.getRepresentedVersion() == Id3v2Tag.ID3V23 || tag.getRepresentedVersion() == Id3v2Tag.ID3V24)))) {
            Id3v2Tag result = null;
            if (targetVersion <= tag.getRepresentedVersion()) {
                result = tag;
            } else {
                if (tag.getRepresentedVersion() < Id3v2Tag.ID3V23) {
                    result = convert22to23(tag);
                }
                if (tag.getRepresentedVersion() < Id3v2Tag.ID3V24 && targetVersion <= Id3v2Tag.ID3V24) {
                    result = convert23to24(result);
                }
            }
            if (f3-assertionsDisabled || result != null) {
                return result;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private static Id3v2Tag convert22to23(Id3v2Tag source) {
        if (f3-assertionsDisabled || (source != null && source.getRepresentedVersion() == Id3v2Tag.ID3V22)) {
            Iterator fields = source.getFields();
            while (fields.hasNext()) {
                TagField current = (TagField) fields.next();
                String currentId = current.getId();
                String conv = (String) conversion22to23.get(currentId);
                if (currentId.equals(conv)) {
                    fields.remove();
                    if (current instanceof TextId3Frame) {
                        source.add(new TextId3Frame(conv, ((TextId3Frame) current).getContent()));
                    }
                }
            }
            source.setRepresentedVersion(Id3v2Tag.ID3V23);
            return source;
        }
        throw new AssertionError();
    }

    private static Id3v2Tag convert23to24(Id3v2Tag source) {
        if (f3-assertionsDisabled || (source != null && source.getRepresentedVersion() == Id3v2Tag.ID3V22)) {
            Iterator fields = source.getFields();
            HashMap specialStore = new HashMap();
            while (fields.hasNext()) {
                TagField current = (TagField) fields.next();
                if (specialStore24.contains(current.getId())) {
                    specialStore.put(current.getId(), current);
                }
                if (discard24.contains(current.getId())) {
                    fields.remove();
                }
            }
            source.set(createTimeField((TextId3Frame) specialStore.get(DATE), (TextId3Frame) specialStore.get(TIME), (TextId3Frame) specialStore.get(YEAR)));
            source.setRepresentedVersion(Id3v2Tag.ID3V24);
            return source;
        }
        throw new AssertionError();
    }

    private static TimeId3Frame createTimeField(TextId3Frame tdat, TextId3Frame time, TextId3Frame tyer) {
        Calendar calendar = new GregorianCalendar();
        calendar.clear();
        if (tdat != null) {
            try {
                if (tdat.getContent().length() == 4) {
                    calendar.set(5, Integer.parseInt(tdat.getContent().substring(0, 2)));
                    calendar.set(2, Integer.parseInt(tdat.getContent().substring(2, 4)) - 1);
                } else {
                    System.err.println("Field TDAT ignroed, since it is not spec conform: \"" + tdat.getContent() + "\"");
                }
            } catch (NumberFormatException e) {
                System.err.println("Numberformatexception occured in timestamp interpretation, date is set to zero.");
                e.printStackTrace();
                calendar.clear();
                return null;
            }
        }
        if (time != null) {
            if (time.getContent().length() == 4) {
                calendar.set(11, Integer.parseInt(time.getContent().substring(0, 2)));
                calendar.set(12, Integer.parseInt(time.getContent().substring(2, 4)));
            } else {
                System.err.println("Field TIME ignroed, since it is not spec conform: \"" + time.getContent() + "\"");
            }
        }
        if (tyer != null) {
            if (tyer.getContent().length() == 4) {
                calendar.set(1, Integer.parseInt(tyer.getContent()));
            } else {
                System.err.println("Field TYER ignroed, since it is not spec conform: \"" + tyer.getContent() + "\"");
            }
        }
        return new TimeId3Frame(RECORD_DAT, calendar);
    }
}
