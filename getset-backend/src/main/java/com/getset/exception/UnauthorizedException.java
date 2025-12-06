package com.getset.exception;

/**
 * Exception thrown when user is not authorized to perform an operation.
 */
public class UnauthorizedException extends GetSetException {
    
    private static final String ERROR_CODE = "UNAUTHORIZED";
    
    public UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
