package com.smartshift.smartshift_backend.auth.controller;

import com.smartshift.smartshift_backend.auth.dto.LoginRequest;
import com.smartshift.smartshift_backend.auth.dto.LoginResponse;
import com.smartshift.smartshift_backend.auth.dto.RegisterRequest;
import com.smartshift.smartshift_backend.auth.exception.InvalidCredentialsException;
import com.smartshift.smartshift_backend.auth.service.AuthService;
import com.smartshift.smartshift_backend.auth.dto.LoginResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);
        return buildTokenResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException("Refresh token is missing. Please log in again.");
        }

        LoginResult result = authService.refreshToken(refreshToken);
        return buildTokenResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in prod
                .path("/api/auth/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    // Helper method to keep your controller clean (DRY principle)
    private ResponseEntity<LoginResponse> buildTokenResponse(LoginResult result) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false) // Set to true in prod
                .path("/api/auth/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        LoginResponse responseBody = new LoginResponse(result.accessToken(), result.role());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }
}