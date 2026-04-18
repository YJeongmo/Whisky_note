package com.whisky.note_app.security;

import com.whisky.note_app.config.SecurityConfig;
import com.whisky.note_app.controller.NoteController;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import com.whisky.note_app.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [JwtAuthenticationFilterTest — 필터 통합 테스트]
 *
 * [@WebMvcTest(NoteController.class)]
 * NoteController의 GET /api/notes 는 인증이 필요한 경로입니다.
 * JWT 필터의 "통과 / 차단" 동작을 검증하기에 적합합니다.
 *
 * [@Import({SecurityConfig.class, JwtAuthenticationFilter.class})]
 * @WebMvcTest는 Security 관련 Bean을 자동 로드하지 않습니다.
 * 실제 SecurityConfig(인가 규칙)와 JwtAuthenticationFilter를 Import해서
 * 실제와 동일한 Security 설정이 동작하도록 합니다.
 *
 * [테스트 시나리오]
 * 1. 유효한 토큰 → 필터 통과 → 200 OK
 * 2. 토큰 없음   → SecurityConfig의 .authenticated() 규칙에 걸림 → 403 Forbidden
 * 3. 잘못된 토큰 → 필터에서 인증 설정 안 함 → .authenticated() 에 걸림 → 403 Forbidden
 */
@WebMvcTest(NoteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private NoteService noteService;

    @MockBean
    private WhiskyAnalysisService whiskyAnalysisService;

    @Test
    @DisplayName("유효한 JWT 토큰으로 요청하면 통과해야 한다")
    void validToken_shouldPass() throws Exception {
        // given
        String validToken = "valid.jwt.token";
        String email = "test@test.com";

        User mockUser = User.builder()
                .email(email)
                .password("encodedPassword")
                .nickname("테스터")
                .role(UserRole.USER)
                .build();

        given(jwtUtil.validateToken(validToken)).willReturn(true);
        given(jwtUtil.getEmailFromToken(validToken)).willReturn(email);
        given(userDetailsService.loadUserByUsername(email)).willReturn(mockUser);
        given(noteService.findAllNotes()).willReturn(List.of()); // 빈 목록 반환

        // when & then: Bearer 토큰 포함 → 필터 통과 → 200 OK
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JWT 토큰 없이 요청하면 403을 반환해야 한다")
    void noToken_shouldReturn403() throws Exception {
        // Authorization 헤더 없이 인증 필요 경로 접근 → 403
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 요청하면 403을 반환해야 한다")
    void invalidToken_shouldReturn403() throws Exception {
        // given: JwtUtil이 false 반환 (만료/변조된 토큰)
        String invalidToken = "invalid.jwt.token";
        given(jwtUtil.validateToken(invalidToken)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }
}
