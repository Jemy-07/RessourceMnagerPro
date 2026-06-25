package com.cuea.rmp.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * <p>
 * Declares a JWT <em>bearer</em> security scheme so the Swagger UI "Authorize"
 * button can attach an access token, and splits the endpoints into logical
 * groups (a dropdown in the UI). Operations are auto-tagged per controller.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI rmpOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resource Manager Pro API")
                        .version("v1")
                        .description("""
                                Backend REST API for Resource Manager Pro.
                                Authenticate via POST /api/v1/auth/login, then click "Authorize"
                                and paste the access token to call protected endpoints."""))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste a JWT access token from /api/v1/auth/login (no 'Bearer ' prefix).")))
                // Show the lock on operations and send the token by default.
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    @Bean
    public GroupedOpenApi authAndUsersGroup() {
        return GroupedOpenApi.builder()
                .group("01 · auth & users")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi resourcesAndProjectsGroup() {
        return GroupedOpenApi.builder()
                .group("02 · resources & projects")
                .pathsToMatch("/api/v1/resources/**", "/api/v1/skills/**",
                        "/api/v1/projects/**", "/api/v1/assignments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi workflowGroup() {
        return GroupedOpenApi.builder()
                .group("03 · requests & timesheets")
                .pathsToMatch("/api/v1/requests/**", "/api/v1/timesheets/**")
                .build();
    }

    @Bean
    public GroupedOpenApi platformGroup() {
        return GroupedOpenApi.builder()
                .group("04 · notifications, sync & reports")
                .pathsToMatch("/api/v1/notifications/**", "/api/v1/devices/**",
                        "/api/v1/sync/**", "/api/v1/reports/**")
                .build();
    }
}
