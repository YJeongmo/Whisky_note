package com.whisky.note_app.controller;

import com.whisky.note_app.config.SecurityConfig;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.security.JwtAuthenticationEntryPoint;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.security.JwtAuthenticationFilter;
import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String EMAIL = "test@test.com";

    @Test
    @DisplayName("인증된 사용자가 /me를 호출하면 본인 정보를 반환해야 한다")
    void getMe_authenticated() throws Exception {
        User mockUser = User.builder()
                .email(EMAIL)
                .password("encodedPw")
                .nickname("테스터")
                .role(UserRole.USER)
                .build();

        given(jwtUtil.validateToken(VALID_TOKEN)).willReturn(true);
        given(jwtUtil.getEmailFromToken(VALID_TOKEN)).willReturn(EMAIL);
        given(userDetailsService.loadUserByUsername(EMAIL)).willReturn(mockUser);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("토큰 없이 /me를 호출하면 401을 반환해야 한다")
    void getMe_noToken_401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized()); // 401 (EntryPoint 적용)
    }
}
