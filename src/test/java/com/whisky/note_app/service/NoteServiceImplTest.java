package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.TastingNote; // entity 패키지
import com.whisky.note_app.repository.TastingNoteRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * [@SpringBootTest + @Transactional]
 * - @SpringBootTest: 전체 Spring 컨텍스트를 로드합니다 (Service, Repository 등 모두 포함)
 * - @Transactional: 각 테스트가 끝나면 자동 롤백합니다 → 테스트 간 데이터 간섭 없음
 * - @ActiveProfiles("test"): application-test.yml 활성화 → H2 인메모리 DB 사용
 *
 * [NoteServiceImpl 통합 테스트]
 * 서비스 레이어가 Repository와 올바르게 협력하는지 검증합니다.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class NoteServiceImplTest {

    @Autowired NoteService noteService;
    @Autowired TastingNoteRepository noteRepository;

    @Test
    @DisplayName("종료일(end)을 입력하지 않고 조회하면 내부적으로 오늘까지 포함해 조회한다")
    void findByPeriodWithoutEnd() {
        // 1. Given: 오늘 날짜 데이터 하나 저장
        TastingNote note = new TastingNote();
        note.setWhiskyName("오늘의 위스키");
        note.setCreatedAt(LocalDate.now());
        noteRepository.save(note);

        // 2. When: 시작일(어제)만 있고 종료일은 null인 경우
        List<NoteResponse> result = noteService.findByPeriod(LocalDate.now().minusDays(1), LocalDate.now());

        // 3. Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("노트 수정 테스트")
    void updateNoteTest() {
        // Given: 기존 데이터 저장
        TastingNote oldNote = new TastingNote();
        oldNote.setWhiskyName("옛날 위스키");
        TastingNote savedNote = noteRepository.save(oldNote);

        // When: UpdateNoteRequest DTO로 수정 요청
        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setWhiskyName("새로운 위스키");
        noteService.updateNote(savedNote.getId(), updateRequest);

        // Then: 값이 바뀌었는지 확인
        TastingNote updated = noteRepository.findById(savedNote.getId()).get();
        assertThat(updated.getWhiskyName()).isEqualTo("새로운 위스키");
    }

    @Test
    @DisplayName("노트 삭제 테스트")
    void deleteNoteTest() {
        // Given
        TastingNote note = new TastingNote();
        note.setWhiskyName("지워질 위스키");
        TastingNote savedNote = noteRepository.save(note);

        // When
        noteService.deleteNote(savedNote.getId());

        // Then
        assertThat(noteRepository.findById(savedNote.getId())).isEmpty();
    }
}
