package com.whisky.note_app.service;

import com.whisky.note_app.domain.TastingNote;
import com.whisky.note_app.repository.TastingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final TastingNoteRepository noteRepository;

    @Override
    public Long saveNote(TastingNote note) {
        // TODO: 구현 예정
        return null;
    }

    @Override
    public List<TastingNote> findAllNotes() {
        // TODO: 구현 예정
        return null;
    }

    @Override
    public TastingNote findNoteById(Long id) {
        // TODO: 구현 예정
        return null;
    }
}