package com.getset.exception;

/**
 * Exception thrown when user input validation fails.
 */
public class ValidationException extends GetSetException {
    
    private static final String ERROR_CODE = "VALIDATION_ERROR";
    
    public ValidationException(String message) {
        super(ERROR_CODE, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
