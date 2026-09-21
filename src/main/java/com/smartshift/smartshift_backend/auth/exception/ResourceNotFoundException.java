package com.smartshift.smartshift_backend.auth.exception;

// For "User not found" -> 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
