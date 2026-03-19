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
    @Transactional(readOnly = true) // 조회 전용 최적화
    public TastingNote findNoteById(Long id) {
        // 리포지토리를 통해 ID로 조회하고, 없으면 null을 반환 (또는 예외 발생)
        return noteRepository.findById(id).orElse(null);
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

    @Override
    public TastingNote updateNote(Long id, TastingNote updateParam) {
        TastingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다. id=" + id));

        // 내용 수정 (이름, 카테고리, 평점 등 필요한 필드 업데이트)
        note.setWhiskyName(updateParam.getWhiskyName());
        note.setCategory(updateParam.getCategory());
        note.setSubCategory(updateParam.getSubCategory());
        note.setNose(updateParam.getNose());
        note.setPalate(updateParam.getPalate());
        note.setFinish(updateParam.getFinish());
        note.setRating(updateParam.getRating());

        return note; // @Transactional 으로 별도의 저장x
    }

    @Override
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}