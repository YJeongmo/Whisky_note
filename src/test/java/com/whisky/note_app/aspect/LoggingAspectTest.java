package com.whisky.note_app.aspect;

import com.whisky.note_app.dto.auth.LoginRequest;
import com.whisky.note_app.dto.auth.LoginResponse;
import com.whisky.note_app.exception.UnauthorizedException;
import com.whisky.note_app.config.SecurityConfig;
import com.whisky.note_app.security.JwtAuthenticationEntryPoint;
import com.whisky.note_app.security.JwtAuthenticationFilter;
import com.whisky.note_app.service.AuthService;
import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.whisky.note_app.controller.AuthController;

/**
 * [LoggingAspectTest — AOP 적용 검증]
 *
 * [테스트 전략]
 * 로그 출력 자체를 검증하는 것은 복잡하고 유지보수가 어렵습니다.
 * 대신 "AOP가 적용된 상태에서 컨트롤러가 정상 동작하는가"를 검증합니다.
 * - 정상 요청 → AOP가 메서드 실행을 방해하지 않고 200/201 응답
 * - 예외 발생 → AOP가 예외를 가로채도 GlobalExceptionHandler까지 전달되는가
 *
 * [@Import(LoggingAspect.class)]
 * @WebMvcTest는 기본적으로 @Aspect Bean을 로드하지 않습니다.
 * 명시적으로 Import해야 AOP가 실제로 적용된 환경을 테스트할 수 있습니다.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, LoggingAspect.class})
class LoggingAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("AOP가 적용된 상태에서 정상 요청은 그대로 처리되어야 한다")
    @WithMockUser
    void aop_doesNotInterruptNormalFlow() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        given(authService.login(any(LoginRequest.class)))
                .willReturn(LoginResponse.builder()
                        .token("jwt.token")
                        .email("test@test.com")
                        .nickname("테스터")
                        .build());

        // when & then
        // AOP가 메서드 실행을 가로채더라도 결과는 동일해야 합니다
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AOP가 적용된 상태에서 예외 발생 시 GlobalExceptionHandler까지 전달되어야 한다")
    @WithMockUser
    void aop_doesNotSwallowException() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        // 예외를 던지도록 설정
        willThrow(new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."))
                .given(authService).login(any(LoginRequest.class));

        // when & then
        // AOP에서 예외를 다시 throw했기 때문에 GlobalExceptionHandler가 401로 처리해야 합니다
        // 만약 AOP가 예외를 삼켜버리면 이 테스트가 실패합니다
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401
    }
}
