package com.chinatelecom.security.emm.exception;

public class IllegalParamaterException extends RuntimeException {
    private static final long serialVersionUID = -5057893735251442822L;

    public IllegalParamaterException(String detailMessage) {
        super(detailMessage);
    }

    public IllegalParamaterException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalParamaterException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str, cause);
    }
}
