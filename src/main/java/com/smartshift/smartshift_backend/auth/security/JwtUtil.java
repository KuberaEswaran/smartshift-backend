package com.smartshift.smartshift_backend.auth.security;


import com.smartshift.smartshift_backend.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtil {
    private static final String ISSUER = "smart-shift";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpiry;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiry;

    public String generateAccessToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role",user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ accessExpiry))
                .signWith(key)
                .compact();
    }
    public String generateRefreshToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        return Jwts.builder()
                .subject(String.valueOf(user.getEmail()))
                .claim("role",user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ refreshExpiry))
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token); // 1. Parses & checks math here
        return true;
    }

    public Claims extractAccessClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .require(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getSubject() == null || claims.get("role", String.class) == null) {
            throw new IllegalArgumentException("Access token is missing required claims");
        }

        return claims;
    }

    public String extractEmail(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token) // 2. Parses & checks math AGAIN here
                .getPayload().getSubject();
    }

}
