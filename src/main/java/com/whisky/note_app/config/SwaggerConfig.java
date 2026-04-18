package com.whisky.note_app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [SwaggerConfig — API 문서 설정]
 *
 * [springdoc-openapi란?]
 * Spring Boot 3.x에서 Swagger UI를 사용하기 위한 라이브러리입니다.
 * 컨트롤러의 @GetMapping, @PostMapping 등 어노테이션을 읽어
 * API 명세(OpenAPI 3.0 스펙)를 자동으로 생성합니다.
 *
 * [접근 경로]
 * - Swagger UI:    http://localhost:8080/swagger-ui/index.html
 * - OpenAPI JSON:  http://localhost:8080/v3/api-docs
 *
 * [JWT 인증 설정]
 * Swagger UI에서 "Authorize" 버튼을 통해 JWT 토큰을 입력하면
 * 이후 모든 API 요청에 Authorization: Bearer {token} 헤더가 자동으로 붙습니다.
 * 로그인 → 토큰 복사 → Authorize 입력 → 인증 필요 API 테스트 가능
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT Bearer 인증 스키마 정의
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
                .addSecurityItem(securityRequirement) // 전체 API에 JWT 인증 적용
                .components(components);
    }
}
