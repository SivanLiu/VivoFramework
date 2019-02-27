package com.vivo.services.cipher.protocol;

public class CryptoEntryParseException extends RuntimeException {
    public CryptoEntryParseException(String message) {
        super(message);
    }

    public CryptoEntryParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoEntryParseException(Throwable cause) {
        super(cause);
    }
}
