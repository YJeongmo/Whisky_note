package com.whisky.note_app.repository;

import com.whisky.note_app.domain.TastingNote;
import org.springframework.data.jpa.repository.JpaRepository;

// JPA가 제공하는 기본 기능을 쓰기 위해 상속받습니다.
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {
}