package com.omnicharge.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1️⃣ Disable CSRF so POST requests from Postman aren't blocked automatically
                .csrf(AbstractHttpConfigurer::disable)

                // 2️⃣ Tell Spring Security to allow the requests through.
                // (We are perfectly safe doing this because your custom JwtAuthenticationFilter
                // is still going to run and block invalid tokens!)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}