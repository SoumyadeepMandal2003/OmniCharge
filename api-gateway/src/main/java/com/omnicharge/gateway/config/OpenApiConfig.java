package com.omnicharge.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // Defaults to localhost for local dev; set API_GATEWAY_URL env var in production
    @Value("${API_GATEWAY_URL:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OmniCharge API Gateway")
                        .version("1.0.0")
                        .description("Aggregated API documentation for all OmniCharge microservices. " +
                                "Use the dropdown at the top-right to switch between services.")
                        .contact(new Contact()
                                .name("OmniCharge Team")
                                .email("support@omnicharge.com")))
                // This tells Swagger UI where to send "Try it out" requests —
                // always through the gateway, never directly to internal service IPs
                .servers(List.of(new Server().url(gatewayUrl).description("API Gateway")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token (without 'Bearer ' prefix)")));
    }
}
