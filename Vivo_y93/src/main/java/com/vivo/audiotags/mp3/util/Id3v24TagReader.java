package com.vivo.audiotags.mp3.util;

import com.vivo.audiotags.generic.TagField;
import com.vivo.audiotags.mp3.Id3V2TagConverter;
import com.vivo.audiotags.mp3.Id3v2Tag;
import com.vivo.audiotags.mp3.util.id3frames.ApicId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.CommId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.GenericId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.Id3Frame;
import com.vivo.audiotags.mp3.util.id3frames.TextId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.TimeId3Frame;
import com.vivo.audiotags.mp3.util.id3frames.UfidId3Frame;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Hashtable;

public class Id3v24TagReader {
    private Hashtable conversion22to23;

    public Id3v24TagReader() {
        initConversionTable();
    }

    private String convertFromId3v22(String field) {
        String s = (String) this.conversion22to23.get(field);
        if (s == null) {
            return "";
        }
        return s;
    }

    private Id3Frame createId3Frame(String field, byte[] data, byte version) throws UnsupportedEncodingException {
        if (version == Id3v2Tag.ID3V22) {
            field = convertFromId3v22(field);
        }
        if (!field.startsWith("T") || (field.startsWith("TX") ^ 1) == 0) {
            if (field.startsWith("COMM")) {
                return new CommId3Frame(data, version);
            }
            if (field.startsWith("UFID")) {
                return new UfidId3Frame(data, version);
            }
            if (field.startsWith("APIC")) {
                return new ApicId3Frame(data, version);
            }
            return new GenericId3Frame(field, data, version);
        } else if (field.equalsIgnoreCase(Id3V2TagConverter.RECORDING_TIME)) {
            return new TimeId3Frame(field, data, version);
        } else {
            return new TextId3Frame(field, data, version);
        }
    }

    private void initConversionTable() {
        this.conversion22to23 = new Hashtable(100);
        String[] v22 = new String[]{"BUF", "CNT", "COM", "CRA", "CRM", "ETC", "EQU", "GEO", "IPL", "LNK", "MCI", "MLL", "PIC", "POP", "REV", "RVA", "SLT", "STC", "TAL", "TBP", "TCM", "TCO", "TCR", "TDA", "TDY", "TEN", "TFT", "TIM", "TKE", "TLA", "TLE", "TMT", "TOA", "TOF", "TOL", "TOR", "TOT", "TP1", "TP2", "TP3", "TP4", "TPA", "TPB", "TRC", "TRD", "TRK", "TSI", "TSS", "TT1", "TT2", "TT3", "TXT", "TXX", "TYE", "UFI", "ULT", "WAF", "WAR", "WAS", "WCM", "WCP", "WPB", "WXX"};
        String[] v23 = new String[]{"RBUF", "PCNT", "COMM", "AENC", "", "ETCO", "EQUA", "GEOB", "IPLS", "LINK", "MCDI", "MLLT", "APIC", "POPM", "RVRB", "RVAD", "SYLT", "SYTC", "TALB", "TBPM", "TCOM", "TCON", "TCOP", Id3V2TagConverter.DATE, "TDLY", "TENC", "TFLT", Id3V2TagConverter.TIME, "TKEY", "TLAN", "TLEN", "TMED", "TOPE", "TOFN", "TOLY", "TORY", "TOAL", "TPE1", "TPE2", "TPE3", "TPE4", "TPOS", "TPUB", "TSRC", Id3V2TagConverter.RECORD_DAT, "TRCK", "TSIZ", "TSSE", "TIT1", "TIT2", "TIT3", "TEXT", "TXXX", Id3V2TagConverter.YEAR, "UFID", "USLT", "WOAF", "WOAR", "WOAS", "WCOM", "WCOP", "WPUB", "WXXX"};
        for (int i = 0; i < v22.length; i++) {
            this.conversion22to23.put(v22[i], v23[i]);
        }
    }

    private int processExtendedHeader(ByteBuffer data, byte version) {
        data.get(new byte[4]);
        int extsize;
        if (version == Id3v2Tag.ID3V23) {
            extsize = readSize(data, Id3v2Tag.ID3V23);
            data.position(data.position() + extsize);
            return extsize;
        }
        extsize = readSyncsafeInteger(data);
        data.position(data.position() + extsize);
        return extsize;
    }

    public Id3v2Tag read(ByteBuffer data, boolean[] ID3Flags, byte version) throws UnsupportedEncodingException {
        int tagSize = data.limit();
        Id3v2Tag tag = new Id3v2Tag();
        if ((version == Id3v2Tag.ID3V23 || version == Id3v2Tag.ID3V24) && ID3Flags[1]) {
            processExtendedHeader(data, version);
        }
        int specSize = version == Id3v2Tag.ID3V22 ? 3 : 4;
        int a = 0;
        while (a < tagSize) {
            byte[] b = new byte[specSize];
            if (data.remaining() > specSize) {
                data.get(b);
                String field = new String(b);
                if (b[0] == (byte) 0) {
                    break;
                }
                int frameSize = readSize(data, version);
                if (frameSize > data.remaining() || frameSize <= 0) {
                    System.err.println(field + " Frame size error, skiping the rest of the tag:" + frameSize);
                    break;
                }
                int i;
                if (version == Id3v2Tag.ID3V23 || version == Id3v2Tag.ID3V24) {
                    i = 2;
                } else {
                    i = 0;
                }
                b = new byte[(i + frameSize)];
                data.get(b);
                if (!"".equals(field)) {
                    TagField f = null;
                    try {
                        f = createId3Frame(field, b, version);
                    } catch (UnsupportedEncodingException uee) {
                        throw uee;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (f != null) {
                        tag.add(f);
                    }
                }
                a++;
            } else {
                break;
            }
        }
        return tag;
    }

    private int readSize(ByteBuffer bb, int version) {
        int value = 0;
        if (version == Id3v2Tag.ID3V24) {
            return readSyncsafeInteger(bb);
        }
        if (version == Id3v2Tag.ID3V23) {
            value = ((bb.get() & 255) << 24) + 0;
        }
        return ((value + ((bb.get() & 255) << 16)) + ((bb.get() & 255) << 8)) + (bb.get() & 255);
    }

    private int readSyncsafeInteger(ByteBuffer buffer) {
        return (((((buffer.get() & 255) << 21) + 0) + ((buffer.get() & 255) << 14)) + ((buffer.get() & 255) << 7)) + (buffer.get() & 255);
    }
}
