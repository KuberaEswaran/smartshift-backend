package com.smartshift.smartshift_backend.auth.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String role) {

}

