package org.shop.sportwebstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Store API")
                                .version("1.0")
                                .description("API store backend")
                )
                .schemaRequirement("JWT", createJWTSecurityScheme())
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }

    private SecurityScheme createJWTSecurityScheme() {
        return new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Add token: Bearer <token>");
    }
}
