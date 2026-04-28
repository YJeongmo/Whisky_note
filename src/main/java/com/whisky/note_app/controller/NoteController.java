package com.whisky.note_app.controller;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Notes", description = "테이스팅 노트 CRUD API — JWT 인증 필요")
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final WhiskyAnalysisService analysisService;

    @Operation(summary = "노트 생성", description = "새로운 테이스팅 노트를 작성합니다.")
    @ApiResponse(responseCode = "201", description = "노트 생성 성공 — 생성된 노트 ID 반환")
    @PostMapping
    public ResponseEntity<Long> create(
            @Valid @RequestBody CreateNoteRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        Long id = noteService.saveNote(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "노트 목록 조회",
            description = "본인의 노트 목록을 조회합니다. name/category/subCategory 파라미터로 검색 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<NoteResponse>> list(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "subCategory", required = false) String subCategory,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

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

    @Operation(summary = "기간별 노트 조회", description = "시작일(start)~종료일(end) 범위의 노트를 조회합니다. end 미입력 시 오늘까지.")
    @GetMapping("/period")
    public ResponseEntity<List<NoteResponse>> getByPeriod(
            @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        if (end == null) {
            end = LocalDate.now();
        }

        return ResponseEntity.ok(noteService.findByPeriod(start, end, user));
    }

    @Operation(summary = "노트 단건 조회", description = "ID로 노트를 조회합니다. 본인 노트만 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 ID 또는 본인 노트 아님")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getById(
            @PathVariable(name = "id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(noteService.findNoteById(id, user));
    }

    @Operation(summary = "노트 수정", description = "노트를 수정합니다. 입력한 필드만 변경되고 나머지는 유지됩니다.")
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(noteService.updateNote(id, request, user));
    }

    @Operation(summary = "노트 삭제", description = "노트를 삭제합니다. 본인 노트만 삭제 가능합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable(name = "id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        noteService.deleteNote(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 노트 내용을 AI로 분석하여 취향 점수를 업데이트합니다.
     * 분석은 백그라운드에서 비동기 실행되며, 202 Accepted를 즉시 반환합니다.
     */
    @Operation(summary = "노트 AI 분석", description = "노트를 AI로 분석하여 취향 점수를 업데이트합니다. 분석은 백그라운드에서 진행됩니다.")
    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeNote(
            @PathVariable(name = "id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        NoteResponse note = noteService.findNoteById(id, user);

        String combinedContent = String.format("향(Nose): %s, 맛(Palate): %s, 여운(Finish): %s",
                note.getNose(), note.getPalate(), note.getFinish());

        if (combinedContent.length() < 10) {
            return ResponseEntity.badRequest().body("노트 내용이 너무 짧아 분석할 수 없습니다.");
        }

        // userId만 전달 — async 스레드에서 JPA 세션 분리를 위해 엔티티 직접 전달 금지
        analysisService.analyzeAndSavePreference(combinedContent, note.getRating(), user.getId());
        return ResponseEntity.accepted().body("분석 요청이 접수되었습니다. 잠시 후 취향 점수가 업데이트됩니다.");
    }
}
