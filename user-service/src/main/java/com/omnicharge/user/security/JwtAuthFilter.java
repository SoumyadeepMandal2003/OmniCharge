package com.omnicharge.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                Claims claims = Jwts
                        .parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(header.substring(7))
                        .getBody();


                String role = claims.get("role", String.class);

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))));
            } catch (Exception ignored) {}
        }
        chain.doFilter(request, response);
    }
}
