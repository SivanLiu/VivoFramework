package com.vivo.alphaindex;

import java.util.Comparator;

public class VivoComparatorLetterObject implements Comparator {
    public int compare(Object arg0, Object arg1) {
        int flag = Integer.valueOf(((VivoLetterObject) arg0).getCount()).intValue() - Integer.valueOf(((VivoLetterObject) arg1).getCount()).intValue();
        if (flag == 0) {
            return flag;
        }
        return -flag;
    }
}
