package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.TastingNoteRepository;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [NoteServiceImplTest — Step 6 수정]
 *
 * TastingNote에 user(NOT NULL FK)가 추가되어,
 * 모든 테스트에서 User를 먼저 생성하고 noteService에 전달합니다.
 *
 * [데이터 격리 검증]
 * userA의 노트를 userB로 조회하면 예외가 발생하는지도 확인합니다.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class NoteServiceImplTest {

    @Autowired NoteService noteService;
    @Autowired TastingNoteRepository noteRepository;
    @Autowired UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 두 명의 사용자를 생성합니다
        userA = userRepository.save(User.builder()
                .email("usera@test.com")
                .password("encodedPw")
                .nickname("유저A")
                .role(UserRole.USER)
                .build());

        userB = userRepository.save(User.builder()
                .email("userb@test.com")
                .password("encodedPw")
                .nickname("유저B")
                .role(UserRole.USER)
                .build());
    }

    @Test
    @DisplayName("노트 저장 후 본인 노트 목록에서 조회되어야 한다")
    void saveAndFindAllNotes() {
        // given
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("아드벡 10년");
        request.setCategory("스카치");

        // when
        noteService.saveNote(request, userA);
        List<NoteResponse> result = noteService.findAllNotes(userA);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWhiskyName()).isEqualTo("아드벡 10년");
    }

    @Test
    @DisplayName("다른 사용자의 노트는 조회되지 않아야 한다 (데이터 격리)")
    void userIsolation_otherUserNoteNotVisible() {
        // given: userA의 노트 생성
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("아드벡 10년");
        noteService.saveNote(request, userA);

        // when: userB로 전체 조회
        List<NoteResponse> userBNotes = noteService.findAllNotes(userB);

        // then: userB에게는 보이지 않음
        assertThat(userBNotes).isEmpty();
    }

    @Test
    @DisplayName("기간 조회는 본인 노트만 반환해야 한다")
    void findByPeriod_onlyOwnNotes() {
        // given: userA의 노트 저장
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("오늘의 위스키");
        noteService.saveNote(request, userA);

        // when: userA로 기간 조회
        List<NoteResponse> result = noteService.findByPeriod(
                LocalDate.now().minusDays(1), LocalDate.now(), userA);

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("노트 수정은 본인 것만 가능해야 한다")
    void updateNote_onlyOwner() {
        // given: userA의 노트 생성
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("옛날 위스키");
        Long noteId = noteService.saveNote(request, userA);

        // when: userA가 수정
        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setWhiskyName("새로운 위스키");
        NoteResponse updated = noteService.updateNote(noteId, updateRequest, userA);

        // then
        assertThat(updated.getWhiskyName()).isEqualTo("새로운 위스키");
    }

    @Test
    @DisplayName("다른 사용자의 노트를 수정하려 하면 예외가 발생해야 한다")
    void updateNote_otherUser_throwsException() {
        // given: userA의 노트 생성
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("아드벡");
        Long noteId = noteService.saveNote(request, userA);

        // when & then: userB가 수정 시도 → 예외
        UpdateNoteRequest updateRequest = new UpdateNoteRequest();
        updateRequest.setWhiskyName("해킹 시도");

        assertThatThrownBy(() -> noteService.updateNote(noteId, updateRequest, userB))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("노트 삭제는 본인 것만 가능해야 한다")
    void deleteNote_onlyOwner() {
        // given
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("지워질 위스키");
        Long noteId = noteService.saveNote(request, userA);

        // when
        noteService.deleteNote(noteId, userA);

        // then
        assertThat(noteRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 노트를 삭제하려 하면 예외가 발생해야 한다")
    void deleteNote_otherUser_throwsException() {
        // given: userA의 노트
        CreateNoteRequest request = new CreateNoteRequest();
        request.setWhiskyName("아드벡");
        Long noteId = noteService.saveNote(request, userA);

        // when & then: userB가 삭제 시도 → 예외
        assertThatThrownBy(() -> noteService.deleteNote(noteId, userB))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
