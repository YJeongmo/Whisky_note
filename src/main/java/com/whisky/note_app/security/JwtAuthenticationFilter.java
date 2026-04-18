package com.whisky.note_app.security;

import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [JwtAuthenticationFilter — JWT 인증 필터]
 *
 * [OncePerRequestFilter란?]
 * HTTP 요청당 딱 한 번만 실행되는 필터입니다.
 * Spring Security Filter Chain에 등록되어 Controller 도달 전에 실행됩니다.
 *
 * [처리 흐름]
 * 1. Authorization 헤더에서 "Bearer {token}" 추출
 * 2. JwtUtil로 토큰 유효성 검증
 * 3. 유효하면 이메일 꺼내서 DB에서 User 조회
 * 4. UsernamePasswordAuthenticationToken 생성 → SecurityContext에 저장
 * 5. 다음 필터로 이동 (filterChain.doFilter)
 *
 * [토큰이 없거나 유효하지 않은 경우]
 * SecurityContext를 비워둔 채로 다음 필터로 진행합니다.
 * 인증이 필요한 경로라면 SecurityConfig의 인가 규칙에서 401/403으로 차단됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        // 2. 토큰이 있고 유효하면 인증 처리
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);

            // 3. SecurityContext에 아직 인증 정보가 없을 때만 처리
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 4. 인증 토큰 생성 (credentials는 null — 이미 JWT로 검증 완료)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[JWT 인증 성공] 사용자: {}", email);
            }
        }

        // 6. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     * "Bearer {token}" 형식이 아니면 null을 반환합니다.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
