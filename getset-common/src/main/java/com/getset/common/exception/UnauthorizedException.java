package com.getset.common.exception;

public class UnauthorizedException extends GetSetException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }
}
