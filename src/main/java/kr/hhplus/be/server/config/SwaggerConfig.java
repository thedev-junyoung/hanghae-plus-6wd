package kr.hhplus.be.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Sneakers Commerce API\n")
                .description("Sneakers Commerce API 명세서")
                .version("v1.0.0");

        // JWT 인증 스키마 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // 개발/운영 환경 서버 설정
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        Server prodServer = new Server()
                .url("https://api.ecommerce.example.com")
                .description("Production Server");

        return new OpenAPI()
                .info(info)
                .servers(Arrays.asList(devServer, prodServer))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}