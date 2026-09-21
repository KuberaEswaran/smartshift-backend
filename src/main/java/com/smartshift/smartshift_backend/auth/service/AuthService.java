package com.smartshift.smartshift_backend.auth.service;

import com.smartshift.smartshift_backend.auth.dto.LoginRequest;
import com.smartshift.smartshift_backend.auth.dto.LoginResult;
import com.smartshift.smartshift_backend.auth.dto.RegisterRequest;
import com.smartshift.smartshift_backend.auth.security.JwtUtil;
import com.smartshift.smartshift_backend.auth.exception.ResourceConflictException;
import com.smartshift.smartshift_backend.auth.exception.ResourceNotFoundException;
import com.smartshift.smartshift_backend.auth.exception.InvalidCredentialsException;
import com.smartshift.smartshift_backend.auth.entity.Role;
import com.smartshift.smartshift_backend.auth.entity.User;
import com.smartshift.smartshift_backend.auth.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate; // <-- Injected Redis
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate; // <-- Replaced the Token Repo
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constants to keep Redis keys clean and consistent
    private static final String REDIS_PREFIX = "refreshToken:";
    private static final String REVOKED_STATUS = "REVOKED";

    public void register(RegisterRequest request) {
        String emailToCheck = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(emailToCheck).isPresent()) {
            throw new ResourceConflictException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);

        userRepository.save(user);
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password provided");
        }

        return generateAndSaveTokens(user);
    }

    public LoginResult refreshToken(String refreshTokenStr) {
        try {
            // 1. Math check (Signature & Expiry)
            jwtUtil.extractEmail(refreshTokenStr);

            // 2. RAM State check: Does the token exist in Redis?
            String redisKey = REDIS_PREFIX + refreshTokenStr;
            String redisValue = redisTemplate.opsForValue().get(redisKey);

            if (redisValue == null) {
                throw new InvalidCredentialsException("Refresh token not found or expired.");
            }

            if (redisValue.equals(REVOKED_STATUS)) {
                // MASSIVE SECURITY FEATURE: Someone tried to use a revoked token!
                throw new InvalidCredentialsException("Compromised token detected. Please log in again.");
            }

            // 3. Token Rotation: Revoke (poison) the old token so it can never be used again
            poisonRedisKey(redisKey);

            // 4. Find the user and generate brand-new tokens
            Long userId = Long.valueOf(redisValue);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return generateAndSaveTokens(user);

        } catch (JwtException e) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }
    }

    public void logout(String refreshTokenStr) {
        // Find the token in Redis and trigger the "Kill Switch" (Poison it)
        String redisKey = REDIS_PREFIX + refreshTokenStr;
        poisonRedisKey(redisKey);
    }

    // Helper method to generate strings AND save to Redis
    private LoginResult generateAndSaveTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Save to Redis instead of DB (Key: refreshToken:eyJh..., Value: userId, TTL: 7 Days)
        String redisKey = REDIS_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(
                redisKey,
                String.valueOf(user.getId()),
                Duration.ofDays(7)
        );

        return new LoginResult(accessToken, refreshToken, user.getRole().name());
    }

    // Helper method to execute the Soft Delete / Revoke logic
    private void poisonRedisKey(String redisKey) {
        Long remainingSeconds = redisTemplate.getExpire(redisKey);

        if (remainingSeconds != null && remainingSeconds > 0) {
            // Overwrites the User ID with "REVOKED", but keeps the countdown timer going
            redisTemplate.opsForValue().set(
                    redisKey,
                    REVOKED_STATUS,
                    Duration.ofSeconds(remainingSeconds)
            );
        }
    }
}