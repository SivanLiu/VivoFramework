package com.vivo.audiotags.exceptions;

public class CannotReadException extends Exception {
    public CannotReadException(String message) {
        super(message);
    }

    public CannotReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
