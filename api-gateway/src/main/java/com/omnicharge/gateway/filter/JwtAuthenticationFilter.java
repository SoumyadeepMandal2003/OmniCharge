package com.omnicharge.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    private final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/**",
            "/api/operators",
            "/api/plans",
            "/actuator/**",
            // Swagger UI static assets and config
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            // Per-service API docs proxied through gateway
            "/*/v3/api-docs",
            "/auth-service/v3/api-docs",
            "/user-service/v3/api-docs",
            "/recharge-service/v3/api-docs",
            "/payment-service/v3/api-docs",
            "/operator-service/v3/api-docs"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Allow open endpoints through without token (using AntPathMatcher for pattern matching)
            if (OPEN_ENDPOINTS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
                // Still strip any spoofed identity headers even on open endpoints
                ServerWebExchange stripped = exchange.mutate()
                        .request(r -> r.headers(headers -> {
                            headers.remove("X-User-Id");
                            headers.remove("X-User-Email");
                            headers.remove("X-User-Role");
                        }))
                        .build();
                return chain.filter(stripped);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            try {
                String token = authHeader.substring(7);
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token).getBody();

                // Strip any client-supplied identity headers to prevent spoofing,
                // then inject trusted values extracted from the validated JWT
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r
                                .headers(headers -> {
                                    headers.remove("X-User-Id");
                                    headers.remove("X-User-Email");
                                    headers.remove("X-User-Role");
                                })
                                .header("X-User-Email", claims.getSubject())
                                .header("X-User-Id", String.valueOf(claims.get("userId")))
                                .header("X-User-Role", String.valueOf(claims.get("role"))))
                        .build();

                return chain.filter(mutatedExchange);

            } catch (ExpiredJwtException e) {
                return unauthorized(exchange, "Token has expired");
            } catch (Exception e) {
                return unauthorized(exchange, "Invalid token");
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized request: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}
