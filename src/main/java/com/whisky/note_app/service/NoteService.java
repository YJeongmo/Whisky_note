package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 테이스팅 노트 비즈니스 로직 인터페이스입니다.
 * User를 파라미터로 받아 서비스가 SecurityContext에 직접 의존하지 않도록 합니다.
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
