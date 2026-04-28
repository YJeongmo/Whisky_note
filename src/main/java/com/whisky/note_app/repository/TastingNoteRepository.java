package com.whisky.note_app.repository;

import com.whisky.note_app.entity.TastingNote;
import com.whisky.note_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 모든 조회 메서드에 user 조건이 포함되어 있습니다.
 * 다른 사용자의 노트 ID로 직접 접근하더라도 결과가 반환되지 않습니다.
 */
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {

    List<TastingNote> findByUser(User user);

    Optional<TastingNote> findByIdAndUser(Long id, User user);

    List<TastingNote> findByUserAndWhiskyNameContaining(User user, String name);

    List<TastingNote> findByUserAndCreatedAtBetween(User user, LocalDate start, LocalDate end);

    List<TastingNote> findByUserAndCategoryContaining(User user, String category);

    List<TastingNote> findByUserAndSubCategoryContaining(User user, String subCategory);
}
