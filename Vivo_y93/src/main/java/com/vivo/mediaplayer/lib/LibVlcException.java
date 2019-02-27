package com.vivo.mediaplayer.lib;

public class LibVlcException extends Exception {
    private static final long serialVersionUID = -1909522348226924189L;

    public LibVlcException(String detailMessage) {
        super(detailMessage);
    }

    public LibVlcException(Throwable throwable) {
        super(throwable);
    }

    public LibVlcException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
