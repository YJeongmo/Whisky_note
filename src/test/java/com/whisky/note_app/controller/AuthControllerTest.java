package com.whisky.note_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whisky.note_app.dto.auth.LoginRequest;
import com.whisky.note_app.dto.auth.LoginResponse;
import com.whisky.note_app.dto.auth.SignUpRequest;
import com.whisky.note_app.dto.auth.SignUpResponse;
import com.whisky.note_app.config.SecurityConfig;
import com.whisky.note_app.exception.UnauthorizedException;
import com.whisky.note_app.security.JwtAuthenticationFilter;
import com.whisky.note_app.service.AuthService;
import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.util.JwtUtil;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [AuthControllerTest — 웹 레이어 테스트]
 *
 * [@WebMvcTest(AuthController.class)]
 * AuthController만 로드합니다. Service, Repository는 로드되지 않습니다.
 * MockMvc로 HTTP 요청을 가상으로 보내서 응답 상태코드, JSON 내용을 검증합니다.
 *
 * [@MockBean]
 * Spring 컨텍스트 안에 Mock Bean을 등록합니다.
 * @Mock(Mockito)과 달리, @MockBean은 Spring이 관리하는 Bean을 가짜로 대체합니다.
 * @WebMvcTest에서는 Service Bean이 없으므로 @MockBean으로 등록해야 합니다.
 *
 * [@WithMockUser]
 * Spring Security가 활성화된 상태에서 "인증된 사용자"로 요청을 보낼 때 사용합니다.
 * 지금은 모든 요청을 허용(permitAll)했지만, Step 5 이후 인증 필요 API 테스트에 활용합니다.
 *
 * [csrf().with(...)]
 * @WebMvcTest는 기본적으로 CSRF 보호가 활성화됩니다.
 * SecurityConfig에서 csrf().disable()을 했지만 테스트 환경에서는 명시적으로 처리합니다.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 객체 → JSON 변환

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("회원가입 성공 시 201 Created와 사용자 정보를 반환해야 한다")
    @WithMockUser
    void signup_success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");
        request.setNickname("테스터");

        SignUpResponse response = SignUpResponse.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("테스터")
                .build();

        given(authService.signup(any(SignUpRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf()) // CSRF 토큰 포함
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())             // 201
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.id").value(1));
    }

    // ===================== @Valid 검증 테스트 =====================

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환해야 한다")
    @WithMockUser
    void signup_invalidEmail() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("not-an-email"); // 이메일 형식 아님
        request.setPassword("password123");
        request.setNickname("테스터");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400을 반환해야 한다")
    @WithMockUser
    void signup_shortPassword() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@test.com");
        request.setPassword("1234567"); // 7자 (8자 미만)
        request.setNickname("테스터");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    @DisplayName("필수 필드가 비어있으면 400을 반환해야 한다")
    @WithMockUser
    void signup_blankFields() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(""); // 빈 문자열
        request.setPassword("");
        request.setNickname("");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.message").exists()); // 오류 메시지 존재 확인
    }

    // ===================== 로그인 테스트 =====================

    @Test
    @DisplayName("로그인 성공 시 200 OK와 JWT 토큰을 반환해야 한다")
    @WithMockUser
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        LoginResponse response = LoginResponse.builder()
                .token("mocked.jwt.token")
                .email("test@test.com")
                .nickname("테스터")
                .build();

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("로그인 실패 시 401 Unauthorized를 반환해야 한다")
    @WithMockUser
    void login_fail() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongPassword");

        willThrow(new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."))
                .given(authService).login(any(LoginRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("중복 이메일로 가입 시 400 Bad Request를 반환해야 한다")
    @WithMockUser
    void signup_duplicateEmail() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("dup@test.com");
        request.setPassword("password123");
        request.setNickname("중복유저");

        // AuthService가 예외를 던지도록 Mock 설정
        // GlobalExceptionHandler가 이 예외를 잡아서 400으로 응답합니다
        willThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다: dup@test.com"))
                .given(authService).signup(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다: dup@test.com"));
    }
}
