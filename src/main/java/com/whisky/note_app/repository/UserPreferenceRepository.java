package com.whisky.note_app.repository;

import com.whisky.note_app.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByKeyword(String keyword);

    // 점수가 높은 상위 키워드들을 가져와서 추천에 사용
    List<UserPreference> findTop5ByOrderByScoreDesc();
}
