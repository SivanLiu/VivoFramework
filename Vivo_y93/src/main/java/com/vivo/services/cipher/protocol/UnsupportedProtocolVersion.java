package com.vivo.services.cipher.protocol;

public class UnsupportedProtocolVersion extends RuntimeException {
    public UnsupportedProtocolVersion(String message) {
        super(message);
    }

    public UnsupportedProtocolVersion(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedProtocolVersion(Throwable cause) {
        super(cause);
    }
}
