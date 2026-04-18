package com.whisky.note_app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // 나중에 JSON 내용 검사할 때 필요
import static org.assertj.core.api.Assertions.assertThat; // 검증(Assertion)용
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc // API 테스트를 위한 가상 브라우저 설정 (실제 서버 없이 HTTP 요청/응답 테스트)
@ActiveProfiles("test") // H2 인메모리 DB 사용 (PostgreSQL 연결 없이 실행)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("이름으로 검색 시 200 OK와 함께 결과가 반환되어야 한다")
    void searchByNameApiTest() throws Exception {
        mockMvc.perform(get("/api/notes")
                        .param("name", "아드벡"))
                .andExpect(status().isOk()); // 상태 코드 확인
    }
}