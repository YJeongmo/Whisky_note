package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [UserPreference — Step 7 변경]
 *
 * [변경 전]
 * keyword에 단독 unique 제약 → 앱 전체에서 키워드가 유일해야 함
 * → 사용자 A의 "피트"와 사용자 B의 "피트"가 공존 불가능 (잘못된 설계)
 *
 * [변경 후]
 * (user_id, keyword) 복합 유니크 제약
 * → 같은 사용자 안에서만 키워드가 유일하면 됨
 * → 사용자마다 독립적인 선호도 데이터를 가짐
 *
 * [@Table uniqueConstraints]
 * @Column(unique = true)는 단일 컬럼 유니크만 가능합니다.
 * 복합 유니크는 @Table 레벨에서 uniqueConstraints로 정의합니다.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String keyword; // 예: "피트", "바닐라"

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
