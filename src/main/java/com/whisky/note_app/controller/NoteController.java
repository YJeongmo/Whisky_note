package com.whisky.note_app.controller;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * [컨트롤러: NoteController — Step 6 변경]
 *
 * [@AuthenticationPrincipal]
 * Spring Security가 SecurityContext에 저장한 UserDetails 객체를 파라미터로 바로 주입합니다.
 * User 엔티티가 UserDetails를 구현하고 있으므로 User 타입으로 바로 받을 수 있습니다.
 *
 * 사용 전:
 *   SecurityContextHolder.getContext().getAuthentication().getPrincipal() — 매번 직접 꺼내야 함
 * 사용 후:
 *   @AuthenticationPrincipal User user — Spring이 자동으로 주입
 *
 * [데이터 격리]
 * 모든 노트 관련 API에서 현재 로그인한 user를 서비스에 전달합니다.
 * 서비스 → Repository 에서 user 조건이 추가되어 본인 노트만 접근 가능합니다.
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final WhiskyAnalysisService analysisService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody CreateNoteRequest request,
            @AuthenticationPrincipal User user) {
        Long id = noteService.saveNote(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "subCategory", required = false) String subCategory,
            @AuthenticationPrincipal User user) {

        List<NoteResponse> result;

        if (name != null && !name.isBlank()) {
            result = noteService.searchByWhiskyName(name, user);
        } else if (category != null && !category.isBlank()) {
            result = noteService.searchByCategory(category, user);
        } else if (subCategory != null && !subCategory.isBlank()) {
            result = noteService.searchBySubCategory(subCategory, user);
        } else {
            result = noteService.findAllNotes(user);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/period")
    public ResponseEntity<List<NoteResponse>> getByPeriod(
            @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal User user) {

        if (end == null) {
            end = LocalDate.now();
        }

        return ResponseEntity.ok(noteService.findByPeriod(start, end, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getById(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(noteService.findNoteById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateNoteRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(noteService.updateNote(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal User user) {
        noteService.deleteNote(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * [POST /api/notes/{id}/analyze] AI 취향 분석
     * Phase 3 (@Async) 적용 전까지는 동기 방식으로 동작합니다.
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeNote(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal User user) {
        NoteResponse note = noteService.findNoteById(id, user);

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
