package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.TastingNote;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.TastingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [서비스 구현체: NoteServiceImpl — Step 6 변경]
 *
 * 모든 메서드에 User 파라미터가 추가되었습니다.
 * Repository 조회 시 user 조건을 함께 넘겨 "본인 노트만" 접근하도록 강제합니다.
 *
 * [데이터 격리 원칙]
 * 서비스 레이어에서 user 파라미터를 받아 Repository에 전달합니다.
 * 이렇게 하면 다른 사용자의 노트 ID를 직접 요청해도 "찾을 수 없습니다" 처리가 됩니다.
 * (DB에 데이터가 있어도 user 조건 불일치로 Optional.empty() 반환)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private final TastingNoteRepository noteRepository;
    private final MasterWhiskyRepository masterWhiskyRepository;

    @Override
    public Long saveNote(CreateNoteRequest request, User user) {
        TastingNote note = new TastingNote();

        note.setWhiskyName(request.getWhiskyName());
        note.setCategory(request.getCategory());
        note.setSubCategory(request.getSubCategory());
        note.setNose(request.getNose());
        note.setPalate(request.getPalate());
        note.setFinish(request.getFinish());
        note.setRating(request.getRating());
        note.setImageUrl(request.getImageUrl());
        note.setUser(user); // 작성자 설정

        if (request.getMasterWhiskyId() != null) {
            MasterWhisky master = masterWhiskyRepository.findById(request.getMasterWhiskyId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 마스터 위스키를 찾을 수 없습니다: " + request.getMasterWhiskyId()));
            note.setMasterWhisky(master);
        }

        return noteRepository.save(note).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> findAllNotes(User user) {
        return noteRepository.findByUser(user)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse findNoteById(Long id, User user) {
        TastingNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 노트를 찾을 수 없습니다: " + id));
        return NoteResponse.from(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchByWhiskyName(String name, User user) {
        return noteRepository.findByUserAndWhiskyNameContaining(user, name)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchByCategory(String category, User user) {
        return noteRepository.findByUserAndCategoryContaining(user, category)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchBySubCategory(String subCategory, User user) {
        return noteRepository.findByUserAndSubCategoryContaining(user, subCategory)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> findByPeriod(LocalDate start, LocalDate end, User user) {
        return noteRepository.findByUserAndCreatedAtBetween(user, start, end)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponse updateNote(Long id, UpdateNoteRequest request, User user) {
        // findByIdAndUser: 본인 노트인지 확인 후 수정
        TastingNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다. id=" + id));

        if (request.getWhiskyName() != null) note.setWhiskyName(request.getWhiskyName());
        if (request.getCategory() != null) note.setCategory(request.getCategory());
        if (request.getSubCategory() != null) note.setSubCategory(request.getSubCategory());
        if (request.getNose() != null) note.setNose(request.getNose());
        if (request.getPalate() != null) note.setPalate(request.getPalate());
        if (request.getFinish() != null) note.setFinish(request.getFinish());
        if (request.getRating() != null) note.setRating(request.getRating());
        if (request.getImageUrl() != null) note.setImageUrl(request.getImageUrl());

        return NoteResponse.from(note);
    }

    @Override
    public void deleteNote(Long id, User user) {
        // 본인 노트인지 확인 후 삭제
        TastingNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다. id=" + id));
        noteRepository.delete(note);
    }
}
