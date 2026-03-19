package com.whisky.note_app.controller;

import com.whisky.note_app.domain.TastingNote;
import com.whisky.note_app.service.NoteService;
import com.whisky.note_app.service.WhiskyAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final WhiskyAnalysisService analysisService;

    @PostMapping
    public Long create(@RequestBody TastingNote note) {

        return noteService.saveNote(note);
    }

    // 이름, 카테고리 조회
    @GetMapping
    public List<TastingNote> list(@RequestParam(name = "name", required = false) String name,
                                  @RequestParam(name = "category", required = false) String category,
                                  @RequestParam(name = "subCategory", required = false) String subCategory) {

        if (name != null && !name.isEmpty()) {
            return noteService.serchByWhiskyName(name);
        }

        if (category != null && !category.isEmpty()) {
            return noteService.searchByCategory(category); // 서비스 호출로 변경
        }

        if (subCategory != null && !subCategory.isEmpty()) {
            return noteService.searchBySubCategory(subCategory); // 서비스 호출로 변경
        }

        return noteService.findAllNotes();
    }

    // 기간 조회
    @GetMapping("/period")
    public List<TastingNote> getByperiod(
            @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate end) {

        // 종료일이 없으면 자동으로 종료일 = 현재시간
        if (end == null) {
            end = LocalDate.now();
        }

        return noteService.findByPeriod(start, end);
    }

    // 업데이트
    @PutMapping("/{id}")
    public TastingNote update(@PathVariable(name = "id") Long id, @RequestBody TastingNote note) {
        return noteService.updateNote(id, note);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public String delete(@PathVariable(name = "id") Long id) {
        noteService.deleteNote(id);
        return "ok";
    }

    // 특정 노트를 지정하여 AI 취향 분석 실행
    @PostMapping("/{id}/analyze")
    public String analyzeNote(@PathVariable(name = "id") Long id) {
        TastingNote note = noteService.findNoteById(id);

        if (note == null) return "해당 노트를 찾을 수 없습니다.";

        // Nose, Palate, Finish를 하나로 합쳐서 제공
        String combinedContent = String.format("향(Nose): %s, 맛(Palate): %s, 여운(Finish): %s",
                note.getNose(), note.getPalate(), note.getFinish());

        // 내용이 존재여부 확인
        if (combinedContent.length() < 10) { // 최소한의 텍스트가 있는지 체크
            return "노트 내용이 너무 짧아 분석할 수 없습니다.";
        }

        try {
            analysisService.analyzeAndSavePreference(combinedContent, note.getRating());
            return "분석 완료! 당신의 취향 점수가 업데이트되었습니다.";
        } catch (Exception e) {
            return "분석 중 오류 발생: " + e.getMessage();
        }
    }

}