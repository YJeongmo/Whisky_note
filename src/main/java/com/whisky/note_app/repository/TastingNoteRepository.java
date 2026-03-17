package com.whisky.note_app.repository;

import com.whisky.note_app.domain.TastingNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


// JPA가 제공하는 기본 기능을 쓰기 위해 상속받습니다.
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {

    List<TastingNote> findByWhiskyNameContaining(String name); // 이름으로 조회

    List<TastingNote> findByCreatedAtBetween(LocalDate start, LocalDate end); // 시작일 - 종료일 사이의 기간으로 조회
}