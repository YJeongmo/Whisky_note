package com.whisky.note_app.repository;

import com.whisky.note_app.domain.TastingNote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TastingNoteRepositoryTest {

    @Autowired
    private TastingNoteRepository noteRepository;

    @Test
    @DisplayName("위스키 이름 검색 테스트")
    void searchByNameTest() {
        // 1. Given: 테스트용 데이터
        TastingNote note = new TastingNote();
        note.setWhiskyName("아드벡 10년");
        noteRepository.save(note);

        // 2. When: 검색 실행
        List<TastingNote> result = noteRepository.findByWhiskyNameContaining("아드");

        // 3. Then: 검증
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWhiskyName()).isEqualTo("아드벡 10년");
    }

    @Test
    @DisplayName("날짜 범위 조회 테스트 - 오늘 저장한 데이터가 오늘 범위 내에 조회되어야 함")
    void findByPeriodTest() {
        // 1. Given: 오늘 날짜로 데이터 저장
        TastingNote note = new TastingNote();
        note.setWhiskyName("라가불린 16년");
        note.setCreatedAt(LocalDate.now());
        noteRepository.save(note);

        // 2. When: 오늘을 포함하는 범위로 조회
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);
        List<TastingNote> result = noteRepository.findByCreatedAtBetween(start, end);

        // 3. Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWhiskyName()).isEqualTo("라가불린 16년");
    }
}