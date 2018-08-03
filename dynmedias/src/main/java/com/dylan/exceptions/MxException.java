package com.dylan.exceptions;

public class MxException extends RuntimeException {
    public MxException(Throwable cause) {
        super(cause);
    }
    public MxException(String message, Throwable cause) {
        super(message, cause);
    }
    public MxException(String message) {
        super(message);
    }
    public String summary() {
        return "MxException";
    }
}