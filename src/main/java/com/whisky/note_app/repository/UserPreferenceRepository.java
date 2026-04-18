package com.whisky.note_app.repository;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    // 특정 사용자의 특정 키워드 조회 (복합 유니크 기준)
    Optional<UserPreference> findByUserAndKeyword(User user, String keyword);

    // 특정 사용자의 상위 선호 키워드 (점수 높은 순)
    List<UserPreference> findTop5ByUserOrderByScoreDesc(User user);
}
