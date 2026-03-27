package com.omnicharge.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSignKey() {
        // Converts your string secret into a cryptographic key
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public void validateToken(final String token) {
        // If the token is invalid, tampered with, or expired, this will throw an Exception
        Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }
}