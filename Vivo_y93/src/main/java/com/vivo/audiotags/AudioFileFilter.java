package com.vivo.audiotags;

import com.vivo.audiotags.generic.Utils;
import java.io.File;
import java.io.FileFilter;

public class AudioFileFilter implements FileFilter {
    public boolean accept(File f) {
        if (f.isHidden() || (f.canRead() ^ 1) != 0) {
            return false;
        }
        if (f.isDirectory()) {
            return true;
        }
        String ext = Utils.getExtension(f);
        return ext.equals("mp3") || ext.equals("flac") || ext.equals("ogg") || ext.equals("mpc") || ext.equals("mp+") || ext.equals("ape") || ext.equals("wav") || ext.equals("wma");
    }
}
