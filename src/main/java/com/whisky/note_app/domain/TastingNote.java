package com.whisky.note_app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class TastingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName; // 위스키 이름

    @Column(columnDefinition = "TEXT") // 내용이 길 수 있으므로 TEXT 타입 지정
    private String nose;    // 향

    @Column(columnDefinition = "TEXT")
    private String palate;  // 맛

    @Column(columnDefinition = "TEXT")
    private String finish;  // 여운

    private Double rating;     // 평점
    private String imageUrl;   // 사진 경로
}