package com.smartshift.smartshift_backend.auth.exception;

public class BusinessException extends RuntimeException{
    public BusinessException(String message){
        super(message);
    }
}



// For "Has account, but lacks admin/access rights" -> 403




// For "Email already exists" -> 409

