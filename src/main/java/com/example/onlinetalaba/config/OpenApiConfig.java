package com.example.onlinetalaba.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Online Talaba Support",
                        email = "info@onlinetalaba.uz",
                        url = "talaba.pulhisob.uz"
                ),
                description = "OpenApi documentation for Spring Security",
                title = "OnlineTalaba API Documentation",
                version = "1.0.0",
                license = @License(
                        name = "OnlineTalaba Docs"
//                        url = "https://api.yasinmebel.uz/be/swagger-ui/index.html"
                ),
                termsOfService = "Terms of services"
        ),
        servers = {
                @Server(
                        description = "Prod ENV"
//                        url = "https://api.yasinmebel.uz/be"
                ),
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8080"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth"),
                @SecurityRequirement(name = "basicAuth")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                description = "JWT auth",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
        ),
        @SecurityScheme(
                name = "basicAuth",
                description = "Basic authentication",
                scheme = "basic",
                type = SecuritySchemeType.HTTP,
                in = SecuritySchemeIn.HEADER
        )
})
public class OpenApiConfig {
}