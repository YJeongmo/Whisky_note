package com.whisky.note_app.repository;

import com.whisky.note_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // SELECT 1 쿼리 — 회원가입 시 이메일 중복 체크에 사용
    boolean existsByEmail(String email);
}
