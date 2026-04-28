package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자별 향미 키워드 선호도 엔티티입니다.
 * (user_id, keyword) 복합 유니크 — 같은 사용자 안에서만 키워드가 유일합니다.
 * 낙관적 락(@Version)으로 동시 업데이트 충돌을 감지합니다.
 */
@Entity
@Getter
@NoArgsConstructor
@Table(
    name = "user_preference",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_preference_user_keyword",
        columnNames = {"user_id", "keyword"}
    )
)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String keyword;

    private int score; // -10 ~ 10

    public UserPreference(User user, String keyword, int initialScore) {
        this.user = user;
        this.keyword = keyword;
        this.score = initialScore;
    }

    public void updateScore(int delta) {
        this.score = Math.max(-10, Math.min(10, this.score + delta));
    }
}
