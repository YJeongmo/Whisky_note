package com.whisky.note_app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [Spring Security 설정: SecurityConfig]
 *
 * [현재 단계 (Step 2) — 최소 설정]
 * JWT 필터는 Step 5에서 추가합니다.
 * 지금은 회원가입/로그인 API가 동작할 수 있도록 최소한만 설정합니다.
 *
 * [왜 SecurityFilterChain Bean으로 등록하는가?]
 * Spring Security 5.7 이후 WebSecurityConfigurerAdapter를 상속하는 방식은 deprecated됩니다.
 * 대신 SecurityFilterChain을 Bean으로 등록하는 방식을 사용합니다.
 * 더 유연하고 여러 SecurityFilterChain을 조합하기 쉽습니다.
 *
 * [BCryptPasswordEncoder Bean 등록 위치]
 * SecurityConfig에 두는 이유: PasswordEncoder는 Security 설정의 일부이기 때문입니다.
 * AuthService에서 @Autowired로 주입받아 사용합니다.
 *
 * [Step 5에서 추가될 것들]
 * - JwtAuthenticationFilter (JWT 검증 필터)
 * - SessionCreationPolicy.STATELESS (세션 비활성화)
 * - 경로별 인가 규칙 세분화
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * [BCryptPasswordEncoder Bean]
     * 비밀번호 단방향 암호화에 사용합니다.
     * Bean으로 등록하면:
     * 1. 애플리케이션 전체에서 같은 인스턴스를 공유합니다 (싱글턴)
     * 2. 테스트에서 Mock으로 교체하기 쉽습니다
     * 3. 순환 참조 문제를 피할 수 있습니다
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [SecurityFilterChain — 현재는 최소 설정]
     *
     * Step 2 현재:
     * - CSRF 비활성화: REST API는 세션을 사용하지 않아 CSRF 공격에 취약하지 않습니다.
     * - 모든 요청 허용 (permitAll): JWT 필터가 없는 지금은 일단 열어둡니다.
     *   Step 5에서 경로별 인가 규칙으로 교체합니다.
     * - 폼 로그인 비활성화: REST API이므로 HTML 폼 로그인 불필요
     * - HTTP Basic 비활성화: 브라우저 팝업 로그인창 불필요
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Step 5에서 세분화 예정
            );

        return http.build();
    }
}
