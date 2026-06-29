package com.dimazak.workload.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI workloadOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trainer Workload Service API")
                        .description("Microservice for calculating trainer monthly workload")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME, new SecurityScheme()
                                .name(SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste JWT obtained from gym-crm /api/users/login")));
    }
}