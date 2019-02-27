package com.vivo.alphaindex;

import com.vivo.common.provider.Calendar.Events;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Locale;

public class NameNormalizer {
    private static final String TAG = "NameNormalizer";
    private static RuleBasedCollator sCachedComplexityCollator;
    private static RuleBasedCollator sCachedCompressingCollator;
    private static Locale sCollatorLocale;
    private static final Object sCollatorLock = new Object();

    private static void ensureCollators() {
        Locale locale = Locale.CHINA;
        if (!locale.equals(sCollatorLocale)) {
            sCollatorLocale = locale;
            sCachedCompressingCollator = (RuleBasedCollator) Collator.getInstance(locale);
            sCachedCompressingCollator.setStrength(0);
            sCachedCompressingCollator.setDecomposition(1);
            sCachedComplexityCollator = (RuleBasedCollator) Collator.getInstance(locale);
            sCachedComplexityCollator.setStrength(1);
        }
    }

    static RuleBasedCollator getCompressingCollator() {
        RuleBasedCollator ruleBasedCollator;
        synchronized (sCollatorLock) {
            ensureCollators();
            ruleBasedCollator = sCachedCompressingCollator;
        }
        return ruleBasedCollator;
    }

    static RuleBasedCollator getComplexityCollator() {
        RuleBasedCollator ruleBasedCollator;
        synchronized (sCollatorLock) {
            ensureCollators();
            ruleBasedCollator = sCachedComplexityCollator;
        }
        return ruleBasedCollator;
    }

    public static String normalize(String name) {
        return Hex.encodeHex(encodeByteMore(getCompressingCollator().getCollationKey(lettersAndDigitsOnly(name)).toByteArray()), true);
    }

    public static int compareComplexity(String name1, String name2) {
        String clean1 = lettersAndDigitsOnly(name1);
        String clean2 = lettersAndDigitsOnly(name2);
        int diff = getComplexityCollator().compare(clean1, clean2);
        if (diff != 0) {
            return diff;
        }
        diff = -clean1.compareTo(clean2);
        if (diff != 0) {
            return diff;
        }
        return name1.length() - name2.length();
    }

    private static String lettersAndDigitsOnly(String name) {
        if (name == null) {
            return Events.DEFAULT_SORT_ORDER;
        }
        char[] letters = name.toCharArray();
        int length = 0;
        for (char c : letters) {
            if (Character.isLetterOrDigit(c)) {
                int length2 = length + 1;
                letters[length] = c;
                length = length2;
            }
        }
        if (length != letters.length) {
            return new String(letters, 0, length);
        }
        return name;
    }

    private static byte[] encodeByteMore(byte[] key) {
        ArrayList<Byte> byteList = new ArrayList();
        boolean isLastWith2Byte = false;
        int i = 0;
        while (i < key.length) {
            byte k = key[i];
            if (k != (byte) 0) {
                if (isLastWith2Byte) {
                    byteList.add(new Byte(k));
                    isLastWith2Byte = false;
                } else if (k >= (byte) 20 && k <= (byte) 38) {
                    byteList.add(new Byte((byte) 42));
                    byteList.add(new Byte(k));
                } else if (k < (byte) 0) {
                    byteList.add(new Byte((byte) -33));
                    byteList.add(new Byte(k));
                    if (i + 1 < key.length && key[i + 1] != (byte) 0) {
                        byteList.add(new Byte(key[i + 1]));
                        i++;
                    }
                } else {
                    byteList.add(new Byte(k));
                    isLastWith2Byte = true;
                }
            }
            i++;
        }
        byte[] keyByte = new byte[byteList.size()];
        for (int j = 0; j < byteList.size(); j++) {
            keyByte[j] = ((Byte) byteList.get(j)).byteValue();
        }
        return keyByte;
    }
}
