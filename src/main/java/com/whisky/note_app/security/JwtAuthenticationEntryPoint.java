package com.whisky.note_app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * [JwtAuthenticationEntryPoint — 미인증 요청 처리]
 *
 * [AuthenticationEntryPoint란?]
 * 인증되지 않은 사용자가 보호된 경로에 접근할 때 Spring Security가 호출하는 핸들러입니다.
 *
 * [기존 문제]
 * Spring Security 기본 동작: 미인증 → 403 Forbidden 반환
 *
 * [올바른 동작]
 * - 401 Unauthorized: 인증 정보가 없거나 유효하지 않음 (토큰 없음, 만료 등)
 * - 403 Forbidden: 인증은 됐지만 해당 리소스에 대한 권한이 없음
 *
 * [SecurityConfig에 등록 방법]
 * .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // GlobalExceptionHandler의 ErrorResponse 형식과 동일하게 맞춥니다
        Map<String, Object> body = Map.of(
                "status", 401,
                "message", "인증이 필요합니다. 로그인 후 JWT 토큰을 Authorization 헤더에 포함해주세요."
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
