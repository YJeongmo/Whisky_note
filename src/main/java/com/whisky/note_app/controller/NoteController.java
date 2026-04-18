package com.whisky.note_app.controller;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * [컨트롤러: NoteController]
 * 테이스팅 노트 CRUD API를 처리합니다.
 *
 * [주요 변경 사항]
 *
 * 1. ResponseEntity 사용
 *    - 기존: 반환 타입이 TastingNote, Long, String 등 HTTP 상태 코드 없이 반환
 *    - 변경: ResponseEntity<T>로 감싸서 HTTP 상태 코드를 명시적으로 설정
 *    - 이유: REST API는 적절한 HTTP 상태 코드로 결과를 알려주는 것이 표준입니다.
 *      예) 생성: 201 Created / 삭제: 204 No Content / 조회: 200 OK
 *
 * 2. @DateTimeFormat 버그 수정
 *    - 기존: @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) — 날짜+시간 형식 요구
 *    - 수정: @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) — 날짜만 (2025-01-15)
 *    - LocalDate는 날짜만 있으므로 DATE_TIME 형식을 요구하면 파싱 오류 발생
 *
 * 3. DTO 전환
 *    - 기존: TastingNote 엔티티를 직접 @RequestBody/@ResponseBody로 사용
 *    - 변경: CreateNoteRequest, UpdateNoteRequest (요청), NoteResponse (응답) DTO 사용
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final WhiskyAnalysisService analysisService;

    /**
     * [POST /api/notes] 노트 생성
     * 201 Created: 자원이 새로 생성되었음을 나타냅니다.
     */
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateNoteRequest request) {
        Long id = noteService.saveNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    /**
     * [GET /api/notes] 노트 목록 조회 (이름/카테고리 검색 포함)
     * 200 OK: 조회 성공
     */
    @GetMapping
    public ResponseEntity<List<NoteResponse>> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "subCategory", required = false) String subCategory) {

        List<NoteResponse> result;

        if (name != null && !name.isBlank()) {
            result = noteService.searchByWhiskyName(name);
        } else if (category != null && !category.isBlank()) {
            result = noteService.searchByCategory(category);
        } else if (subCategory != null && !subCategory.isBlank()) {
            result = noteService.searchBySubCategory(subCategory);
        } else {
            result = noteService.findAllNotes();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * [GET /api/notes/period] 기간 조회
     *
     * [@DateTimeFormat(ISO.DATE) 수정 이유]
     * ISO.DATE_TIME → "2025-01-15T00:00:00" 형식을 요구 (LocalDate에 부적합)
     * ISO.DATE      → "2025-01-15" 형식으로 파싱 (LocalDate에 올바른 형식)
     */
    @GetMapping("/period")
    public ResponseEntity<List<NoteResponse>> getByPeriod(
            @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (end == null) {
            end = LocalDate.now(); // 종료일 미입력 시 오늘까지 조회
        }

        return ResponseEntity.ok(noteService.findByPeriod(start, end));
    }

    /**
     * [GET /api/notes/{id}] ID로 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(noteService.findNoteById(id));
    }

    /**
     * [PUT /api/notes/{id}] 노트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateNoteRequest request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    /**
     * [DELETE /api/notes/{id}] 노트 삭제
     * 204 No Content: 성공적으로 처리했지만 반환할 내용이 없음
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * [POST /api/notes/{id}/analyze] AI 취향 분석 실행
     * 특정 노트의 내용을 AI가 분석하여 선호/비선호 키워드를 추출하고 점수를 업데이트합니다.
     * (Ollama가 실행 중이어야 동작합니다)
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeNote(@PathVariable(name = "id") Long id) {
        NoteResponse note = noteService.findNoteById(id);
        // GlobalExceptionHandler가 없을 경우를 대비한 조기 반환 방어 코드 제거
        // → findNoteById가 없는 경우 IllegalArgumentException을 던지고
        //   GlobalExceptionHandler가 400으로 처리합니다.

        String combinedContent = String.format("향(Nose): %s, 맛(Palate): %s, 여운(Finish): %s",
                note.getNose(), note.getPalate(), note.getFinish());

        if (combinedContent.length() < 10) {
            return ResponseEntity.badRequest().body("노트 내용이 너무 짧아 분석할 수 없습니다.");
        }

        try {
            analysisService.analyzeAndSavePreference(combinedContent, note.getRating());
            return ResponseEntity.ok("분석 완료! 당신의 취향 점수가 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("분석 중 오류 발생: " + e.getMessage());
        }
    }
}
