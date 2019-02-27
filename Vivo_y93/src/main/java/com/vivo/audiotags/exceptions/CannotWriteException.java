package com.vivo.audiotags.exceptions;

public class CannotWriteException extends Exception {
    public CannotWriteException(String message) {
        super(message);
    }

    public CannotWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotWriteException(Throwable cause) {
        super(cause);
    }
}
