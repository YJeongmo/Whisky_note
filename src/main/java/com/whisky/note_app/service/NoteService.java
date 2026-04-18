package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * [서비스 인터페이스: NoteService]
 *
 * [인터페이스를 분리하는 이유]
 * 1. 테스트 용이성
 *    - @WebMvcTest(컨트롤러 테스트) 시 NoteService 인터페이스를 Mockito로 쉽게 목(Mock) 처리 가능
 *    - 구현체(NoteServiceImpl)를 몰라도 인터페이스만 보면 계약(contract)을 알 수 있음
 *
 * 2. 느슨한 결합 (DIP: 의존관계 역전 원칙)
 *    - 컨트롤러가 NoteServiceImpl이 아닌 NoteService(인터페이스)에 의존
 *    - 나중에 구현체를 바꿔도 컨트롤러 코드는 수정 불필요
 *
 * [변경 사항]
 * - 모든 파라미터/반환 타입을 TastingNote 엔티티 → DTO로 변경
 * - 오타 수정: serchByWhiskyName → searchByWhiskyName
 * - 오타 수정: 노트 삭체 → 노트 삭제
 */
public interface NoteService {

    Long saveNote(CreateNoteRequest request); // 노트 생성

    List<NoteResponse> findAllNotes();              // 전체 조회
    NoteResponse findNoteById(Long id);             // ID를 통한 조회 (없으면 IllegalArgumentException)
    List<NoteResponse> searchByWhiskyName(String name);     // 이름으로 조회 (오타 수정)
    List<NoteResponse> searchByCategory(String category);   // 대분류로 조회
    List<NoteResponse> searchBySubCategory(String subCategory); // 소분류로 조회
    List<NoteResponse> findByPeriod(LocalDate start, LocalDate end); // 기간 조회

    NoteResponse updateNote(Long id, UpdateNoteRequest request); // 노트 수정

    void deleteNote(Long id); // 노트 삭제
}
