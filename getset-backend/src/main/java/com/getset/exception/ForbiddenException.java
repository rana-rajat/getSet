package com.getset.exception;

/**
 * Exception thrown when user doesn't have required permissions.
 */
public class ForbiddenException extends GetSetException {
    
    private static final String ERROR_CODE = "FORBIDDEN";
    
    public ForbiddenException(String message) {
        super(ERROR_CODE, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
