package com.whisky.note_app.service;

import com.whisky.note_app.domain.TastingNote;
import java.util.List;

public interface NoteService {
    Long saveNote(TastingNote note);
    List<TastingNote> findAllNotes();
    TastingNote findNoteById(Long id);
    List<TastingNote> serchByWhiskyName(String name);
}