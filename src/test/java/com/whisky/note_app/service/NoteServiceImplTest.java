package com.whisky.note_app.service;

import com.whisky.note_app.repository.TastingNoteRepository;
import org.springframework.boot.test.context.SpringBootTest;
import com.whisky.note_app.domain.TastingNote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NoteServiceImplTest {

    @Autowired NoteService noteService;
    @Autowired
    TastingNoteRepository noteRepository;

    @Test
    @DisplayName("종료일(end)을 입력하지 않고 조회하면 내부적으로 오늘까지 포함해 조회한다")
    void findByPeriodWithoutEnd() {
        // 1. Given: 오늘 날짜 데이터 하나 저장
        TastingNote note = new TastingNote();
        note.setWhiskyName("오늘의 위스키");
        note.setCreatedAt(LocalDate.now());
        noteRepository.save(note);

        // 2. When: 시작일(어제)만 있고 종료일은 null인 경우
        List<TastingNote> result = noteService.findByPeriod(LocalDate.now().minusDays(1), LocalDate.now());

        // 3. Then
        assertThat(result).isNotEmpty();
    }
}