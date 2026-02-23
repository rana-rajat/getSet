package com.getset.common.exception;

public class ForbiddenException extends GetSetException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }
}
