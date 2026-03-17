package com.whisky.note_app.service;

import com.whisky.note_app.domain.TastingNote;
import com.whisky.note_app.repository.TastingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // DB 에러시 롤백
public class NoteServiceImpl implements NoteService {

    private final TastingNoteRepository noteRepository;

    @Override
    public Long saveNote(TastingNote note) {
        TastingNote saveNote = noteRepository.save(note);
        return saveNote.getId();
    }

    @Override
    @Transactional(readOnly = true) // 조회 전용
    public List<TastingNote> findAllNotes() {
        return noteRepository.findAll();
    }

    @Override
    public TastingNote findNoteById(Long id) {
        // TODO: 구현 예정
        return null;
    }

    @Override
    public List<TastingNote> serchByWhiskyName(String name) {
        return noteRepository.findByWhiskyNameContaining(name);
    }

    @Override
    public List<TastingNote> searchByCategory(String category) {
       return noteRepository.findByCategoryContaining(category);
    }

    @Override
    public List<TastingNote> searchBySubCategory(String subCategory) {
        return noteRepository.findBySubCategoryContaining(subCategory);
    }

    @Override
    public List<TastingNote> findByPeriod(LocalDate start, LocalDate end) {
        return noteRepository.findByCreatedAtBetween(start, end);
    }
}