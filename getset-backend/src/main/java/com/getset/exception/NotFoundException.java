package com.getset.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends GetSetException {
    
    private static final String ERROR_CODE = "NOT_FOUND";
    
    public NotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
