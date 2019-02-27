package com.vivo.audiotags.mp3.util;

public interface VbrInfoFrame {
    int getFileSize();

    int getFrameCount();

    boolean isValid();

    boolean isVbr();
}
