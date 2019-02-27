package com.vivo.common.utils;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;

public class HanziToPinyin2 {
    private static final String TAG = "HanziToPinyin2";
    private static HanziToPinyin2 sInstance;
    private Transliterator mAsciiTransliterator;
    private Transliterator mPinyinTransliterator;

    public static class Token {
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
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

    private HanziToPinyin2() {
        try {
            this.mPinyinTransliterator = Transliterator.getInstance("Han-Latin/Names; Latin-Ascii; Any-Upper");
            this.mAsciiTransliterator = Transliterator.getInstance("Latin-Ascii");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Han-Latin/Names transliterator data is missing, HanziToPinyin2 is disabled");
        }
    }

    private boolean hasChineseTransliterator() {
        return this.mPinyinTransliterator != null;
    }

    public static HanziToPinyin2 getInstance() {
        HanziToPinyin2 hanziToPinyin2;
        synchronized (HanziToPinyin2.class) {
            if (sInstance == null) {
                sInstance = new HanziToPinyin2();
            }
            hanziToPinyin2 = sInstance;
        }
        return hanziToPinyin2;
    }

    private void tokenize(char character, Token token) {
        token.source = Character.toString(character);
        if (character < 128) {
            token.type = 1;
            token.target = token.source;
        } else if (character < 592 || (7680 <= character && character < 7935)) {
            String str;
            token.type = 1;
            if (this.mAsciiTransliterator == null) {
                str = token.source;
            } else {
                str = this.mAsciiTransliterator.transliterate(token.source);
            }
            token.target = str;
        } else {
            token.type = 2;
            token.target = this.mPinyinTransliterator.transliterate(token.source);
            if (TextUtils.isEmpty(token.target) || TextUtils.equals(token.source, token.target)) {
                token.type = 3;
                token.target = token.source;
            }
        }
    }

    private String transliterate(String input) {
        if (!hasChineseTransliterator() || TextUtils.isEmpty(input)) {
            return null;
        }
        return this.mPinyinTransliterator.transliterate(input);
    }

    public ArrayList<Token> getTokens(String input) {
        ArrayList<Token> tokens = new ArrayList();
        if (!hasChineseTransliterator() || TextUtils.isEmpty(input)) {
            return tokens;
        }
        int inputLength = input.length();
        StringBuilder sb = new StringBuilder();
        int tokenType = 1;
        Token token = new Token();
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (!Character.isSpaceChar(character)) {
                tokenize(character, token);
                if (token.type == 2) {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(token);
                    token = new Token();
                } else {
                    if (tokenType != token.type && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    sb.append(token.target);
                }
                tokenType = token.type;
            } else if (sb.length() > 0) {
                addToken(sb, tokens, tokenType);
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
}
