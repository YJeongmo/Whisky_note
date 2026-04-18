package com.whisky.note_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whisky.note_app.config.SecurityConfig;
import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.security.JwtAuthenticationFilter;
import com.whisky.note_app.service.CustomUserDetailsService;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import com.whisky.note_app.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [NoteControllerTest — Step 6 수정]
 *
 * JWT 인증이 적용된 상태에서의 NoteController 테스트입니다.
 * 모든 /api/notes 요청은 유효한 JWT 토큰이 필요합니다.
 *
 * [테스트 전략]
 * JwtUtil과 CustomUserDetailsService를 Mock으로 설정하여
 * 실제 JWT 없이 "유효한 토큰" 시나리오를 만들어냅니다.
 *
 * [공통 셋업 — @BeforeEach]
 * 매 테스트마다 동일한 "인증된 사용자" 셋업이 필요하므로 @BeforeEach로 분리합니다.
 */
@WebMvcTest(NoteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @MockBean
    private WhiskyAnalysisService whiskyAnalysisService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String EMAIL = "test@test.com";

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email(EMAIL)
                .password("encodedPw")
                .nickname("테스터")
                .role(UserRole.USER)
                .build();

        // 매 테스트마다 "유효한 토큰" 셋업
        given(jwtUtil.validateToken(VALID_TOKEN)).willReturn(true);
        given(jwtUtil.getEmailFromToken(VALID_TOKEN)).willReturn(EMAIL);
        given(userDetailsService.loadUserByUsername(EMAIL)).willReturn(mockUser);
    }

    @Test
    @DisplayName("인증된 사용자가 노트 목록을 조회하면 200 OK를 반환해야 한다")
    void list_authenticated_200() throws Exception {
        // given
        given(noteService.findAllNotes(any(User.class))).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("토큰 없이 노트 목록을 조회하면 403을 반환해야 한다")
    void list_noToken_403() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증된 사용자가 노트를 생성하면 201 Created를 반환해야 한다")
    void create_authenticated_201() throws Exception {
        // given
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("아드벡 10년");
        request.setCategory("스카치");

        given(noteService.saveNote(any(CreateNoteRequest.class), any(User.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
