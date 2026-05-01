package com.omnicharge.recharge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    // This method generates a signing key from the secret string using the HMAC SHA algorithm.
    //  It converts the secret string into a byte array and creates a Key object that can be used for signing and verifying JWTs.
    private Key key() { 
        return Keys.hmacShaKeyFor(secret.getBytes()); 
    }

    public String extractUsername(String token) {
        return extract(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extract(token, c -> c.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extract(token, c -> c.get("role", String.class));
    }

    // This method is a generic method that takes a JWT token and a resolver function as parameters.
    // It parses the JWT token, validates its signature using the signing key, and applies the resolver function
    // to the claims (payload) of the token to extract specific information (such as username, user ID, or role) based on the provided resolver function.
    private <T> T extract(String token, Function<Claims, T> resolver) {
        return resolver.apply(
                Jwts.parserBuilder()
                        .setSigningKey(key())
                        .build()
                .parseClaimsJws(token).getBody());
    }
}
