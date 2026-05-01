package com.omnicharge.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protects internal endpoints (e.g. /api/users/internal/**) by requiring
 * a shared secret header X-Internal-Secret that only trusted services know.
 * This prevents external callers from bypassing auth-service and creating
 * user profiles directly.
 */
@Component
@Slf4j
public class InternalRequestFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/api/users/internal/";
    private static final String SECRET_HEADER = "X-Internal-Secret";

    @Value("${internal.secret:omnicharge-internal-secret-2024}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith(INTERNAL_PATH_PREFIX)) {
            String providedSecret = request.getHeader(SECRET_HEADER);
            if (!internalSecret.equals(providedSecret)) {
                log.warn("Blocked unauthorized access to internal endpoint: {}", path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"status\":403,\"message\":\"Access denied\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
