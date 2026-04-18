package com.whisky.note_app.service;

import com.whisky.note_app.dto.request.CreateNoteRequest;
import com.whisky.note_app.dto.request.UpdateNoteRequest;
import com.whisky.note_app.dto.response.NoteResponse;
import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.TastingNote;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.TastingNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [서비스 구현체: NoteServiceImpl]
 *
 * [@Transactional 전략]
 * - 클래스 레벨 @Transactional: 모든 메서드에 기본으로 트랜잭션 적용 (쓰기 가능)
 * - 조회 메서드에 @Transactional(readOnly = true): 읽기 전용 최적화
 *   → JPA가 변경 감지(dirty checking)를 생략해 성능이 좋아집니다.
 *   → DB 레플리케이션 환경에서 읽기 전용 슬레이브 DB로 라우팅도 가능합니다.
 *
 * [DTO 변환 위치]
 * - 서비스에서 엔티티 → DTO 변환을 담당합니다.
 * - 컨트롤러는 DTO만 알면 되고, 엔티티 구조를 알 필요가 없습니다.
 * - NoteResponse.from(note)로 변환: 변환 로직이 NoteResponse에 캡슐화되어 있습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨: 쓰기 메서드들의 기본값 (DB 오류 시 롤백)
public class NoteServiceImpl implements NoteService {

    private final TastingNoteRepository noteRepository;
    private final MasterWhiskyRepository masterWhiskyRepository; // 마스터 위스키 FK 연결용

    @Override
    public Long saveNote(CreateNoteRequest request) {
        TastingNote note = new TastingNote();

        // 요청 DTO의 값을 엔티티에 매핑
        note.setWhiskyName(request.getWhiskyName());
        note.setCategory(request.getCategory());
        note.setSubCategory(request.getSubCategory());
        note.setNose(request.getNose());
        note.setPalate(request.getPalate());
        note.setFinish(request.getFinish());
        note.setRating(request.getRating());
        note.setImageUrl(request.getImageUrl());

        // masterWhiskyId가 있으면 MasterWhisky 엔티티와 연결
        // null이면 임의 입력 노트로 처리 (FK 없음)
        if (request.getMasterWhiskyId() != null) {
            MasterWhisky master = masterWhiskyRepository.findById(request.getMasterWhiskyId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 마스터 위스키를 찾을 수 없습니다: " + request.getMasterWhiskyId()));
            note.setMasterWhisky(master);
        }

        return noteRepository.save(note).getId();
    }

    @Override
    @Transactional(readOnly = true) // 조회 전용: 변경 감지 생략 → 성능 최적화
    public List<NoteResponse> findAllNotes() {
        return noteRepository.findAll()
                .stream()
                .map(NoteResponse::from) // 메서드 참조: note -> NoteResponse.from(note) 와 동일
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse findNoteById(Long id) {
        TastingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 노트를 찾을 수 없습니다: " + id));
        return NoteResponse.from(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchByWhiskyName(String name) {
        return noteRepository.findByWhiskyNameContaining(name)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchByCategory(String category) {
        return noteRepository.findByCategoryContaining(category)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> searchBySubCategory(String subCategory) {
        return noteRepository.findBySubCategoryContaining(subCategory)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> findByPeriod(LocalDate start, LocalDate end) {
        return noteRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponse updateNote(Long id, UpdateNoteRequest request) {
        TastingNote note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다. id=" + id));

        // 값이 있는 경우에만 업데이트 (null이면 기존 값 유지)
        // Phase 1 이후에는 @Valid + 유효성 검사로 발전할 수 있습니다.
        if (request.getWhiskyName() != null) note.setWhiskyName(request.getWhiskyName());
        if (request.getCategory() != null) note.setCategory(request.getCategory());
        if (request.getSubCategory() != null) note.setSubCategory(request.getSubCategory());
        if (request.getNose() != null) note.setNose(request.getNose());
        if (request.getPalate() != null) note.setPalate(request.getPalate());
        if (request.getFinish() != null) note.setFinish(request.getFinish());
        if (request.getRating() != null) note.setRating(request.getRating());
        if (request.getImageUrl() != null) note.setImageUrl(request.getImageUrl());

        // @Transactional이 적용되어 있으므로 별도 save() 호출 불필요
        // → 트랜잭션 종료 시 JPA 변경 감지(dirty checking)가 자동으로 UPDATE 쿼리를 실행합니다.
        return NoteResponse.from(note);
    }

    @Override
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}
