package com.whisky.note_app.repository;

import com.whisky.note_app.entity.TastingNote;
import com.whisky.note_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * [TastingNoteRepository — Step 6 변경]
 * 모든 조회 메서드에 user 조건을 추가합니다.
 * "본인의 노트만 볼 수 있다"는 데이터 격리 원칙을 Repository 레벨에서 보장합니다.
 *
 * [메서드 네이밍 규칙 — Spring Data JPA]
 * findBy[필드명]And[필드명]: AND 조건으로 조회
 * findBy[필드명]Containing: LIKE %값% 쿼리
 * findBy[필드명]Between: BETWEEN 쿼리
 */
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {

    // 전체 조회 — 본인 것만
    List<TastingNote> findByUser(User user);

    // 단건 조회 — 본인 것만 (다른 사람 ID로 접근 차단)
    Optional<TastingNote> findByIdAndUser(Long id, User user);

    // 이름 검색 — 본인 것만
    List<TastingNote> findByUserAndWhiskyNameContaining(User user, String name);

    // 기간 조회 — 본인 것만
    List<TastingNote> findByUserAndCreatedAtBetween(User user, LocalDate start, LocalDate end);

    // 카테고리 검색 — 본인 것만
    List<TastingNote> findByUserAndCategoryContaining(User user, String category);

    // 소분류 검색 — 본인 것만
    List<TastingNote> findByUserAndSubCategoryContaining(User user, String subCategory);
}