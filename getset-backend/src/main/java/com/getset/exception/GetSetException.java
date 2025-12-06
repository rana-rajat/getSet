package com.getset.exception;

/**
 * Base exception class for GetSet application.
 * All custom exceptions should extend this class.
 */
public class GetSetException extends RuntimeException {
    
    private final String errorCode;
    
    public GetSetException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public GetSetException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public GetSetException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public GetSetException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
