package com.getset.common.exception;

public class GetSetException extends RuntimeException {
    private final String errorCode;

    public GetSetException(String message) {
        super(message);
        this.errorCode = "GETSET_ERROR";
    }

    public GetSetException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public GetSetException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GETSET_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
