package com.getset.common.exception;

public class NotFoundException extends GetSetException {
    public NotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}
