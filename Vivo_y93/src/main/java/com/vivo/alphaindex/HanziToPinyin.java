package com.vivo.alphaindex;

import android.text.TextUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HanziToPinyin {
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final boolean DEBUG = false;
    private static final char FIRST_ARABIC = 'ب';
    private static final char FIRST_HEBREW = 'א';
    private static final String FIRST_PINYIN_UNIHAN = "阿";
    private static final char FIRST_RUSSIAN_UPPER = 'А';
    private static final char LAST_ARABIC = 'ى';
    private static final char LAST_HEBREW = 'ת';
    private static final String LAST_PINYIN_UNIHAN = "鿿";
    private static final char LAST_RUSSIAN_LOWER = 'я';
    private static final char LAST_RUSSIAN_UPPER = 'Я';
    public static final byte[][] PINYINS = new byte[][]{new byte[]{(byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 66, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 67, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 68, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 69, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 70, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 71, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 77, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 74, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 75, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 76, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 77, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 78, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 80, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 81, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 81, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 82, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 84, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 87, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 88, (byte) 73, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 88, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 73, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 74, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 89, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 65, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 69, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 67, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 65, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 69, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 79, (byte) 78, (byte) 71, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 79, (byte) 85, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 73, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 65, (byte) 78, (byte) 71}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 73, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 72, (byte) 85, (byte) 79, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 79, (byte) 78, (byte) 71, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 79, (byte) 85, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 73, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 78, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 90, (byte) 85, (byte) 79, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 83, (byte) 72, (byte) 65, (byte) 78, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}};
    private static final String TAG = "HanziToPinyin";
    public static final char[] UNIHANS = new char[]{38463, 21710, 23433, 32942, 20985, 20843, 25344, 25203, 37030, 21241, 38466, 22868, 20283, 23620, 36793, 28780, 24971, 27715, 20907, 30326, 23788, 22163, 20594, 21442, 20179, 25761, 20874, 23934, 26365, 26366, 23652, 21449, 33414, 36799, 20261, 25220, 36710, 25275, 27784, 27785, 38455, 21507, 20805, 25277, 20986, 27451, 25571, 24027, 20997, 21561, 26110, 36916, 21618, 21254, 20945, 31895, 27718, 23828, 37032, 25619, 21649, 21574, 20025, 24403, 20992, 22042, 25189, 28783, 27664, 22002, 30008, 20993, 29241, 19969, 19999, 19996, 21562, 21438, 32785, 35176, 21544, 22810, 22968, 35830, 22848, 38821, 20799, 21457, 24070, 21274, 39134, 20998, 20016, 35205, 20175, 32017, 20245, 26094, 20357, 29976, 20872, 30347, 25096, 32473, 26681, 21039, 24037, 21246, 20272, 29916, 20054, 20851, 20809, 24402, 20008, 21593, 21704, 21645, 20292, 22831, 33568, 35779, 40658, 25323, 20136, 22135, 21503, 40769, 20079, 33457, 24576, 29375, 24031, 28784, 26127, 21529, 19980, 21152, 25099, 27743, 33405, 38454, 24062, 22357, 20866, 20009, 20965, 23010, 22104, 20891, 21652, 24320, 21002, 24572, 23611, 21308, 32910, 21157, 31354, 25248, 25181, 22840, 33967, 23485, 21281, 20111, 22372, 25193, 22403, 26469, 20848, 21879, 25438, 32907, 21202, 23834, 21013, 20457, 22849, 33391, 25769, 21015, 25294, 21026, 28316, 22230, 40857, 30620, 22108, 23048, 30055, 25249, 32599, 21603, 22920, 22475, 23258, 29284, 29483, 20040, 21573, 38376, 30015, 21674, 23424, 21941, 20060, 27665, 21517, 35884, 25720, 21726, 27626, 21999, 25295, 33097, 22241, 22228, 23404, 30098, 23070, 24641, 33021, 22958, 25288, 23330, 40479, 25423, 22236, 23425, 22942, 20892, 32698, 22900, 22907, 30111, 40641, 37069, 21908, 35764, 22929, 25293, 30469, 20051, 25243, 21624, 21943, 21257, 19989, 22248, 21117, 27669, 23000, 20050, 38027, 21078, 20166, 19971, 25488, 21315, 21595, 24708, 30335, 20146, 29381, 33422, 19992, 21306, 23761, 32570, 22795, 21605, 31331, 23046, 24825, 20154, 25172, 26085, 33592, 21433, 37018, 25404, 22567, 23121, 30628, 25468, 20200, 27618, 19977, 26706, 25531, 38314, 26862, 20711, 26432, 31579, 23665, 20260, 24368, 22882, 30003, 33688, 25938, 21319, 23608, 21454, 20070, 21047, 34928, 38377, 21452, 35841, 21550, 35828, 21430, 24554, 25436, 33487, 29435, 22794, 23385, 21766, 20182, 22268, 22349, 27748, 22834, 24529, 29093, 21076, 22825, 26091, 24086, 21381, 22258, 20599, 20984, 28237, 25512, 21534, 20039, 31349, 27498, 24367, 23587, 21361, 26167, 32705, 25373, 20044, 22805, 34418, 20186, 20065, 28785, 20123, 24515, 26143, 20982, 20241, 21505, 21509, 21066, 22339, 20011, 24697, 22830, 24186, 20539, 19968, 22233, 24212, 21727, 20323, 20248, 25180, 22246, 26352, 26197, 31584, 31612, 24064, 28797, 20802, 21288, 20654, 21017, 36156, 24590, 22679, 25166, 25434, 27838, 24352, 38271, 38263, 20299, 34567, 36126, 20105, 20043, 23769, 24226, 20013, 24030, 26417, 25235, 25341, 19987, 22918, 38585, 23442, 21331, 20082, 23447, 37049, 31199, 38075, 21404, 23562, 26152, 20825, 40899, 40900};
    private static HanziToPinyin sInstance;
    private static Map<Character, Character> sMuiSupportMap = new HashMap();
    private final boolean mHasChinaCollator;

    public static class Token {
        public static final int ARABIC = 15;
        public static final int HEBREW = 16;
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final int RUSSIAN = 14;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public String source;
        public String target;
        public int type;

        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target = target;
        }
    }

    private class DialerSearchToken extends Token {
        static final int FIRSTCASE = 0;
        static final int LOWERCASE = 2;
        static final int UPPERCASE = 1;

        private DialerSearchToken() {
        }
    }

    static {
        sMuiSupportMap.put(Character.valueOf(FIRST_RUSSIAN_UPPER), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1041), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1042), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1043), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1044), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1045), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1046), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1047), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1048), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1049), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1050), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1051), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1052), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1053), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1054), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1055), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1056), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1057), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1058), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1059), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1060), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1061), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1062), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1063), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1064), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1065), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1066), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1067), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1068), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1069), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1070), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(LAST_RUSSIAN_UPPER), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1072), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1073), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1074), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1075), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1076), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1077), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1078), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1079), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1080), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1081), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1082), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1083), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1084), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1085), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1086), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1087), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1088), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1089), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1090), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1091), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1092), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1093), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1094), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1095), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1096), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1097), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1098), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1099), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1100), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1101), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1102), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(LAST_RUSSIAN_LOWER), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1025), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1105), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(FIRST_ARABIC), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1577), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1578), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1579), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1569), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1575), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1587), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1588), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1589), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1590), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1583), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1584), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1585), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1586), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1580), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1581), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1582), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1606), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1607), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1608), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(LAST_ARABIC), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1601), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1602), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1603), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1604), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1605), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1591), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1592), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1593), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1594), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1491), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1492), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(1493), Character.valueOf('2'));
        sMuiSupportMap.put(Character.valueOf(FIRST_HEBREW), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1489), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1490), Character.valueOf('3'));
        sMuiSupportMap.put(Character.valueOf(1502), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1504), Character.valueOf('4'));
        sMuiSupportMap.put(Character.valueOf(1500), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1499), Character.valueOf('5'));
        sMuiSupportMap.put(Character.valueOf(1494), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1495), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1496), Character.valueOf('6'));
        sMuiSupportMap.put(Character.valueOf(1512), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1513), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(LAST_HEBREW), Character.valueOf('7'));
        sMuiSupportMap.put(Character.valueOf(1510), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1511), Character.valueOf('8'));
        sMuiSupportMap.put(Character.valueOf(1505), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1506), Character.valueOf('9'));
        sMuiSupportMap.put(Character.valueOf(1507), Character.valueOf('9'));
    }

    protected HanziToPinyin(boolean hasChinaCollator) {
        this.mHasChinaCollator = hasChinaCollator;
    }

    public static HanziToPinyin getInstance() {
        synchronized (HanziToPinyin.class) {
            HanziToPinyin hanziToPinyin;
            if (sInstance != null) {
                hanziToPinyin = sInstance;
                return hanziToPinyin;
            }
            Locale[] locale = Collator.getAvailableLocales();
            int i = 0;
            while (i < locale.length) {
                if (locale[i].equals(Locale.CHINESE) || locale[i].equals(Locale.CHINA)) {
                    sInstance = new HanziToPinyin(true);
                    hanziToPinyin = sInstance;
                    return hanziToPinyin;
                }
                i++;
            }
            sInstance = new HanziToPinyin(false);
            hanziToPinyin = sInstance;
            return hanziToPinyin;
        }
    }

    private static boolean doSelfValidation() {
        char lastChar = UNIHANS[0];
        String lastString = Character.toString(lastChar);
        for (char c : UNIHANS) {
            if (lastChar != c) {
                String curString = Character.toString(c);
                if (COLLATOR.compare(lastString, curString) >= 0) {
                    return false;
                }
                lastString = curString;
            }
        }
        return true;
    }

    private Token getToken(char character) {
        Token token = new Token();
        String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        if (character < 256) {
            token.type = 1;
            token.target = letter;
            return token;
        }
        int cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
        if (cmp < 0) {
            token.type = 3;
            token.target = letter;
            return token;
        }
        if (cmp == 0) {
            token.type = 2;
            offset = 0;
        } else {
            cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
            if (cmp > 0) {
                token.type = 3;
                token.target = letter;
                return token;
            } else if (cmp == 0) {
                token.type = 2;
                offset = UNIHANS.length - 1;
            }
        }
        token.type = 2;
        if (offset < 0) {
            int begin = 0;
            int end = UNIHANS.length - 1;
            while (begin <= end) {
                offset = (begin + end) / 2;
                cmp = COLLATOR.compare(letter, Character.toString(UNIHANS[offset]));
                if (cmp == 0) {
                    break;
                } else if (cmp > 0) {
                    begin = offset + 1;
                } else {
                    end = offset - 1;
                }
            }
        }
        if (cmp < 0) {
            offset--;
        }
        StringBuilder pinyin = new StringBuilder();
        int j = 0;
        while (j < PINYINS[offset].length && PINYINS[offset][j] != (byte) 0) {
            pinyin.append((char) PINYINS[offset][j]);
            j++;
        }
        token.target = pinyin.toString();
        if (TextUtils.isEmpty(token.target)) {
            token.type = 3;
            token.target = token.source;
        }
        return token;
    }

    public ArrayList<Token> get(String input) {
        ArrayList<Token> tokens = new ArrayList();
        if (!this.mHasChinaCollator || TextUtils.isEmpty(input)) {
            return tokens;
        }
        int inputLength = input.length();
        StringBuilder sb = new StringBuilder();
        int tokenType = 1;
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (character == ' ') {
                if (sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
            } else if (character < 256) {
                if (tokenType != 1 && sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = 1;
                sb.append(character);
            } else {
                Token t = getToken(character);
                if (t.type == 2) {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(t);
                    tokenType = 2;
                } else {
                    if (tokenType != t.type && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = t.type;
                    sb.append(character);
                }
            }
        }
        if (sb.length() > 0) {
            addToken(sb, tokens, tokenType);
        }
        return tokens;
    }

    private void addToken(StringBuilder sb, ArrayList<Token> tokens, int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }

    public String getTokensForDialerSearch(String input, StringBuilder offsets) {
        if (offsets == null || input == null || TextUtils.isEmpty(input)) {
            return null;
        }
        StringBuilder subStrSet = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList();
        ArrayList<String> shortSubStrOffset = new ArrayList();
        int inputLength = input.length();
        StringBuilder subString = new StringBuilder();
        StringBuilder subStrOffset = new StringBuilder();
        int tokenType = 1;
        int caseTypePre = 0;
        int caseTypeCurr = 1;
        int mPos = 0;
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            Character c;
            if (character == '-' || character == ',') {
                mPos++;
            } else if (character == ' ') {
                if (subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                addSubString(tokens, shortSubStrOffset, subStrSet, offsets);
                mPos++;
                caseTypePre = 0;
            } else if (character < 256) {
                if (tokenType != 1 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypeCurr = (character < 'A' || character > 'Z') ? 2 : 1;
                if (caseTypePre == 2 && caseTypeCurr == 1) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 1;
                c = Character.valueOf(Character.toUpperCase(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character >= 1040 && character <= 1103) {
                if (tokenType != 14 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                if (character < 1040 || character > 1071) {
                    caseTypeCurr = 2;
                } else {
                    caseTypeCurr = 1;
                }
                if (caseTypePre == 2 && caseTypeCurr == 1) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 14;
                c = (Character) sMuiSupportMap.get(Character.valueOf(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character >= 1576 && character <= 1609) {
                if (tokenType != 15 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 15;
                c = (Character) sMuiSupportMap.get(Character.valueOf(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            } else if (character < 1488 || character > 1514) {
                Token t = getToken(character);
                int tokenSize = t.target.length();
                if (t.type == 2) {
                    if (subString.length() > 0) {
                        addToken(subString, tokens, tokenType);
                        addOffsets(subStrOffset, shortSubStrOffset);
                    }
                    tokens.add(t);
                    for (int j = 0; j < tokenSize; j++) {
                        subStrOffset.append((char) mPos);
                    }
                    addOffsets(subStrOffset, shortSubStrOffset);
                    tokenType = 2;
                    caseTypePre = 0;
                    mPos++;
                } else {
                    mPos++;
                }
            } else {
                if (tokenType != 16 && subString.length() > 0) {
                    addToken(subString, tokens, tokenType);
                    addOffsets(subStrOffset, shortSubStrOffset);
                }
                caseTypePre = caseTypeCurr;
                tokenType = 16;
                c = (Character) sMuiSupportMap.get(Character.valueOf(character));
                if (c != null) {
                    subString.append(c);
                    subStrOffset.append((char) mPos);
                }
                mPos++;
            }
            if (mPos > 127) {
                break;
            }
        }
        if (subString.length() > 0) {
            addToken(subString, tokens, tokenType);
            addOffsets(subStrOffset, shortSubStrOffset);
        }
        addSubString(tokens, shortSubStrOffset, subStrSet, offsets);
        return subStrSet.toString();
    }

    private void addOffsets(StringBuilder sb, ArrayList<String> shortSubStrOffset) {
        shortSubStrOffset.add(sb.toString());
        sb.setLength(0);
    }

    private void addSubString(ArrayList<Token> tokens, ArrayList<String> shortSubStrOffset, StringBuilder subStrSet, StringBuilder offsets) {
        if (tokens != null && !tokens.isEmpty()) {
            int size = tokens.size();
            int len = 0;
            StringBuilder mShortSubStr = new StringBuilder();
            StringBuilder mShortSubStrOffsets = new StringBuilder();
            StringBuilder mShortSubStrSet = new StringBuilder();
            StringBuilder mShortSubStrOffsetsSet = new StringBuilder();
            for (int i = size - 1; i >= 0; i--) {
                String mTempStr = ((Token) tokens.get(i)).target;
                len += mTempStr.length();
                String mTempOffset = (String) shortSubStrOffset.get(i);
                if (mShortSubStr.length() > 0) {
                    mShortSubStr.deleteCharAt(0);
                    mShortSubStrOffsets.deleteCharAt(0);
                }
                mShortSubStr.insert(0, mTempStr);
                mShortSubStr.insert(0, (char) len);
                mShortSubStrOffsets.insert(0, mTempOffset);
                mShortSubStrOffsets.insert(0, (char) len);
                mShortSubStrSet.insert(0, mShortSubStr);
                mShortSubStrOffsetsSet.insert(0, mShortSubStrOffsets);
            }
            subStrSet.append(mShortSubStrSet);
            offsets.append(mShortSubStrOffsetsSet);
            tokens.clear();
            shortSubStrOffset.clear();
        }
    }
}
