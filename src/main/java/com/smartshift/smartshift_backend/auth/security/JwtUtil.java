package com.smartshift.smartshift_backend.auth.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.smartshift.smartshift_backend.auth.entity.User;

@Component
public class JwtUtil {

    private static final String ISSUER = "smart-shift";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.access.expiration}")
    private long accessExpiry;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiry;

    private final JwtEncoder accessJwtEncoder;
    private final JwtEncoder refreshJwtEncoder;
    private final JwtDecoder refreshJwtDecoder;

    public JwtUtil(
            @Qualifier("accessJwtEncoder") JwtEncoder accessJwtEncoder,
            @Qualifier("refreshJwtEncoder") JwtEncoder refreshJwtEncoder,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) {
        this.accessJwtEncoder = accessJwtEncoder;
        this.refreshJwtEncoder = refreshJwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
    }

    public String generateAccessToken(User user) {
        return encodeToken(accessJwtEncoder, String.valueOf(user.getId()), ACCESS_TOKEN_TYPE, accessExpiry, user);
    }

    public String generateRefreshToken(User user) {
        return encodeToken(refreshJwtEncoder, user.getEmail(), REFRESH_TOKEN_TYPE, refreshExpiry, user);
    }

    private String encodeToken(JwtEncoder encoder, String subject, String tokenType, long expiry, User user) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("role", user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusMillis(expiry))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public String extractEmail(String token) {
        return refreshJwtDecoder.decode(token).getSubject();
    }

}
