package com.vivo.audiotags.exceptions;

public class ModifyVetoException extends Exception {
    public ModifyVetoException(String message) {
        super(message);
    }

    public ModifyVetoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModifyVetoException(Throwable cause) {
        super(cause);
    }
}
