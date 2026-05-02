package com.omnicharge.user.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the incoming JWT Authorization header to all outgoing Feign calls.
 * Without this, Feign calls from user-service to recharge-service / payment-service
 * would be rejected with 401 because they carry no token.
 */
@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor jwtForwardingInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }
}
