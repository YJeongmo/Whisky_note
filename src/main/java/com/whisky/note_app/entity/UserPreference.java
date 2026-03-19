package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keyword;  // 예: "피트", "바닐라"

    private int score; // -10 ~ 10

    public UserPreference(String keyword, int initialScore) {
        this.keyword = keyword;
        this.score = initialScore;
    }

    public void updateScore(int delta) {
        this.score = Math.max(-10, Math.min(10, this.score + delta));
    }
}