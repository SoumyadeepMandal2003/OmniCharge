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
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/*/v3/api-docs"
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
                return chain.filter(exchange);
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

                // Forward user info to downstream services via headers
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r
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
