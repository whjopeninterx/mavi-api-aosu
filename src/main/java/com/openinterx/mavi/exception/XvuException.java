package com.openinterx.mavi.exception;

public class XvuException extends RuntimeException {


    public XvuException() {
        super();
    }

    public XvuException(String message) {
        super(message);
    }

    public XvuException(String message, Throwable cause) {
        super(message, cause);
    }

    public XvuException(Throwable cause) {
        super(cause);
    }
}
