package com.omnicharge.api_gateway.filter;

import com.omnicharge.api_gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // ✅ Manual constructor added here for Maven compatibility
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 1️⃣ ENDPOINTS THAT DON'T NEED A TOKEN
    private final List<String> openEndpoints = List.of(
            "/users/register",
            "/users/login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 2️⃣ Let public endpoints pass through without checking
        if (openEndpoints.stream().anyMatch(path::contains)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3️⃣ Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // 4️⃣ Check if token exists and is formatted correctly
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        // 5️⃣ Validate the token
        String token = authHeader.substring(7); // Removes "Bearer " prefix
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired JWT token");
            return;
        }

        // 6️⃣ If the token is valid, forward the request to the microservice!
        filterChain.doFilter(request, response);
    }
}