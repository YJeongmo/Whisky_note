package com.whisky.note_app.repository;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    Optional<UserPreference> findByUserAndKeyword(User user, String keyword);

    List<UserPreference> findTop5ByUserOrderByScoreDesc(User user);

    List<UserPreference> findAllByUser(User user);
}
