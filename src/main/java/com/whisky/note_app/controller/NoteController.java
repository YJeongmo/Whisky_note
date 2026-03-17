package com.whisky.note_app.controller;

import com.whisky.note_app.domain.TastingNote;
import com.whisky.note_app.service.NoteService;
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

    @PostMapping
    public Long create(@RequestBody TastingNote note) {
        return noteService.saveNote(note);
    }

    @GetMapping
    public List<TastingNote> list(@RequestParam(name = "name", required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return noteService.serchByWhiskyName(name);
        }
        return noteService.findAllNotes();
    }

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
}