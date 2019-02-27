package com.vivo.alphaindex;

import java.util.Comparator;

public class VivoComparatorString implements Comparator {
    public int compare(Object arg0, Object arg1) {
        String letter0 = (String) arg0;
        String letter1 = (String) arg1;
        if (isTaLanguage(letter1) && isTaLanguage(letter0)) {
            letter0 = NameNormalizer.normalize(letter0);
            letter1 = NameNormalizer.normalize(letter1);
        }
        return letter0.compareTo(letter1);
    }

    public boolean isTaLanguage(String letter) {
        if ((letter.charAt(0) < 2944 || letter.charAt(0) > 3071) && (letter.charAt(0) < 1536 || letter.charAt(0) > 1791)) {
            return false;
        }
        return true;
    }
}
