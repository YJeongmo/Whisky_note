package com.whisky.note_app.repository;

import com.whisky.note_app.entity.TastingNote;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [TastingNoteRepositoryTest — Step 6 수정]
 *
 * TastingNote.user가 NOT NULL이 되었으므로, 테스트 데이터 생성 시
 * User를 먼저 저장하고 note.setUser(user)를 설정해야 합니다.
 *
 * Repository 메서드도 user 파라미터를 받는 새 메서드로 교체합니다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TastingNoteRepositoryTest {

    @Autowired
    private TastingNoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
                .email("usera@test.com")
                .password("pw")
                .nickname("유저A")
                .role(UserRole.USER)
                .build());

        userB = userRepository.save(User.builder()
                .email("userb@test.com")
                .password("pw")
                .nickname("유저B")
                .role(UserRole.USER)
                .build());
    }

    private TastingNote createNote(User user, String whiskyName) {
        TastingNote note = new TastingNote();
        note.setWhiskyName(whiskyName);
        note.setUser(user);
        return noteRepository.save(note);
    }

    @Test
    @DisplayName("findByUser — 본인 노트만 조회되어야 한다")
    void findByUser() {
        createNote(userA, "아드벡 10년");
        createNote(userB, "라가불린 16년");

        List<TastingNote> result = noteRepository.findByUser(userA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWhiskyName()).isEqualTo("아드벡 10년");
    }

    @Test
    @DisplayName("findByIdAndUser — 본인 노트는 조회되고 타인 노트는 empty를 반환해야 한다")
    void findByIdAndUser() {
        TastingNote note = createNote(userA, "아드벡");

        Optional<TastingNote> found = noteRepository.findByIdAndUser(note.getId(), userA);
        Optional<TastingNote> notFound = noteRepository.findByIdAndUser(note.getId(), userB);

        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("findByUserAndWhiskyNameContaining — 이름 검색은 본인 것만 반환해야 한다")
    void findByUserAndWhiskyNameContaining() {
        createNote(userA, "아드벡 10년");
        createNote(userB, "아드벡 우가달");

        List<TastingNote> result = noteRepository.findByUserAndWhiskyNameContaining(userA, "아드벡");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWhiskyName()).isEqualTo("아드벡 10년");
    }

    @Test
    @DisplayName("findByUserAndCreatedAtBetween — 기간 조회는 본인 것만 반환해야 한다")
    void findByUserAndCreatedAtBetween() {
        TastingNote note = createNote(userA, "라가불린 16년");
        note.setCreatedAt(LocalDate.now());
        noteRepository.save(note);

        List<TastingNote> result = noteRepository.findByUserAndCreatedAtBetween(
                userA, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWhiskyName()).isEqualTo("라가불린 16년");
    }

    @Test
    @DisplayName("findByUserAndCategoryContaining — 카테고리 검색은 본인 것만 반환해야 한다")
    void findByUserAndCategoryContaining() {
        TastingNote note = createNote(userA, "보모어 12년");
        note.setCategory("스카치");
        noteRepository.save(note);

        createNote(userB, "버팔로 트레이스"); // userB는 카테고리 없음

        List<TastingNote> result = noteRepository.findByUserAndCategoryContaining(userA, "스카치");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("스카치");
    }
}
