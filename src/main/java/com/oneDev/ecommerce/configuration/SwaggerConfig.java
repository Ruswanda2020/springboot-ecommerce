package com.oneDev.ecommerce.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("Ecommerce API")
                        .version("1.0")
                        .description("Application Ecommerce API"))
                .servers(List.of(
                        new Server().url("https://entirely-dynamic-penguin.ngrok-free.app/api/v1").description("Ngrok Server"),
                        new Server().url("http://localhost:8080").description("Localhost Server")
                ));
    }
}
