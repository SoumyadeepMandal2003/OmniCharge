package com.omnicharge.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token (without 'Bearer ' prefix)")));
    }
}
