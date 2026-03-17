package com.whisky.note_app.service;

import com.whisky.note_app.domain.TastingNote;

import java.time.LocalDate;
import java.util.List;

public interface NoteService {
    Long saveNote(TastingNote note); // 노트 생성

    List<TastingNote> findAllNotes(); // 전체 조회
    TastingNote findNoteById(Long id); // ID를 통한 조회
    List<TastingNote> serchByWhiskyName(String name); // 이름을 통한 조회
    List<TastingNote> searchByCategory(String category); // 대분류를 통한 조회 (스카치, 버번등)
    List<TastingNote> searchBySubCategory(String subCategory); // 소분류를 통한 조회 (피트, 쉐리등)
    List<TastingNote> findByPeriod(LocalDate start, LocalDate end); // 날짜를 통한 조회

    TastingNote updateNote(Long id, TastingNote updateParam); // 노트 업데이트

    void deleteNote(Long id); // 노트 삭체
}