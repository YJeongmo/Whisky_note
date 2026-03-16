package com.whisky.note_app.controller;

import com.whisky.note_app.domain.TastingNote;
import com.whisky.note_app.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public List<TastingNote> list() {
        return noteService.findAllNotes();
    }
}