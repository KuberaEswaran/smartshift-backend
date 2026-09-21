package com.smartshift.smartshift_backend.auth.exception;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String message) { super(message); }
}
