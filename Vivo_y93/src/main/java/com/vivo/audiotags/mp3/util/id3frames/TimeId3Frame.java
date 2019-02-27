package com.vivo.audiotags.mp3.util.id3frames;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Calendar;

public class TimeId3Frame extends TextId3Frame {
    private static String createString(Calendar calendar) {
        StringBuffer result = new StringBuffer();
        result.append(new DecimalFormat("0000").format((long) calendar.get(1)));
        return result.toString();
    }

    public TimeId3Frame(String fieldId, byte[] rawContent, byte version) throws UnsupportedEncodingException {
        super(fieldId, rawContent, version);
    }

    public TimeId3Frame(String fieldId, Calendar calendar) {
        super(fieldId, createString(calendar));
    }

    public TimeId3Frame(String fieldId, String content) {
        super(fieldId, content);
    }

    protected void populate(byte[] raw) throws UnsupportedEncodingException {
        super.populate(raw);
        this.content = this.content.substring(0, 4);
    }
}
