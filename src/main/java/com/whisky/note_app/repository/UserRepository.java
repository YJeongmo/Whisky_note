package com.whisky.note_app.repository;

import com.whisky.note_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [UserRepository]
 *
 * [findByEmail — 왜 이 메서드가 핵심인가?]
 * Spring Security는 로그인 처리 시 UserDetailsService.loadUserByUsername()을 호출합니다.
 * 이 메서드에서 "이메일로 회원 조회 → UserDetails 반환"을 해야 합니다.
 * findByEmail이 그 역할을 담당합니다. (Step 4에서 사용)
 *
 * [existsByEmail — 왜 필요한가?]
 * 회원가입 시 중복 이메일을 체크합니다.
 * findByEmail().isPresent()로도 같은 결과지만,
 * existsByEmail은 SELECT 1만 하므로 불필요한 데이터를 가져오지 않아 더 효율적입니다.
 *
 * [Optional<User> 반환 이유]
 * 이메일로 회원을 찾지 못할 경우 null 대신 Optional.empty()를 반환합니다.
 * 호출하는 쪽에서 null 체크 대신 .orElseThrow()로 명확하게 예외를 처리할 수 있습니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 회원 조회 — 로그인(loadUserByUsername)에서 사용
    Optional<User> findByEmail(String email);

    // 이메일 중복 확인 — 회원가입 시 중복 체크에 사용
    boolean existsByEmail(String email);
}
