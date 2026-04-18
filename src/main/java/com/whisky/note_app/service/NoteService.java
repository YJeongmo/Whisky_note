package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * [서비스 인터페이스: NoteService — Step 6 변경]
 *
 * 모든 메서드에 User 파라미터를 추가합니다.
 * 컨트롤러에서 SecurityContext로부터 현재 로그인한 User를 꺼내서 전달합니다.
 * 서비스 레이어는 User를 직접 조회하지 않고 외부에서 주입받습니다.
 *
 * [왜 User 객체를 파라미터로 받는가?]
 * 서비스가 SecurityContext에 직접 접근하면 테스트 시 SecurityContext 설정이 필요해집니다.
 * User를 파라미터로 받으면 서비스 테스트에서 User 객체만 넘겨주면 되어 테스트가 단순해집니다.
 */
public interface NoteService {

    Long saveNote(CreateNoteRequest request, User user);

    List<NoteResponse> findAllNotes(User user);
    NoteResponse findNoteById(Long id, User user);
    List<NoteResponse> searchByWhiskyName(String name, User user);
    List<NoteResponse> searchByCategory(String category, User user);
    List<NoteResponse> searchBySubCategory(String subCategory, User user);
    List<NoteResponse> findByPeriod(LocalDate start, LocalDate end, User user);

    NoteResponse updateNote(Long id, UpdateNoteRequest request, User user);

    void deleteNote(Long id, User user);
}
