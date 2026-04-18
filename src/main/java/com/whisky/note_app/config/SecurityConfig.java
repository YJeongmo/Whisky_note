package com.whisky.note_app.config;

import com.whisky.note_app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [Spring Security 설정: SecurityConfig — Step 5 완성]
 *
 * [전체 인증/인가 흐름]
 * HTTP 요청
 *   → JwtAuthenticationFilter (토큰 파싱 → SecurityContext 저장)
 *   → UsernamePasswordAuthenticationFilter (건너뜀 — 비활성화 상태)
 *   → Controller
 *   → (인증 필요 경로인데 토큰 없으면 403 Forbidden)
 *
 * [SessionCreationPolicy.STATELESS]
 * JWT는 서버가 세션을 유지할 필요 없습니다.
 * STATELESS로 설정하면 Spring Security가 HttpSession을 생성하지 않습니다.
 * 수평 확장(Scale-out)에 유리합니다.
 *
 * [경로별 인가 규칙]
 * - /api/auth/**  : 인증 없이 접근 가능 (회원가입, 로그인)
 * - 나머지        : 인증 필요 (유효한 JWT 토큰 있어야 함)
 *
 * [CORS 설정]
 * 프론트엔드 개발 시 별도 도메인에서 API를 호출할 경우 CORS 정책이 필요합니다.
 * 현재는 주석으로 위치만 표시해두고, 프론트엔드 연결 시 활성화합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // REST API이므로 CSRF/폼 로그인/HTTP Basic 모두 비활성화
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // JWT는 stateless — 서버가 세션을 유지하지 않음
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 경로별 인가 규칙
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // 회원가입, 로그인은 인증 불필요
                .anyRequest().authenticated()                 // 나머지는 JWT 인증 필요
            )

            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            // [CORS — 프론트엔드 연결 시 활성화]
            // .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // [CORS 설정 — 프론트엔드 도메인 확정 후 활성화]
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowedOrigins(List.of("http://localhost:3000"));
    //     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     config.setAllowedHeaders(List.of("*"));
    //     config.setAllowCredentials(true);
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }
}
