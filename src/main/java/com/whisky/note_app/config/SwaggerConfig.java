package com.whisky.note_app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("로그인 후 발급받은 JWT 토큰을 입력하세요. (Bearer 접두사 제외)"));

        return new OpenAPI()
                .info(new Info()
                        .title("Whisky Tasting Note API")
                        .description("위스키 테이스팅 노트 관리 API입니다. 회원가입 → 로그인 → Authorize 순서로 진행하세요.")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
