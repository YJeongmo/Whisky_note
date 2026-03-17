package com.whisky.note_app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 날짜 자동 기록
public class TastingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName; // 위스키 이름

    private String category;      // 대분류 (스카치, 버번 등)
    private String subCategory;   // 소분류 (피트, 쉐리 등)

    @CreatedDate // 생성 시 날짜 자동 입력
    @Column(updatable = false) // 수정 시에는 날짜가 변하지 않도록 고정
    private LocalDate createdAt;

    @Column(columnDefinition = "TEXT") // 내용이 길 수 있으므로 TEXT 타입 지정
    private String nose;    // 향

    @Column(columnDefinition = "TEXT")
    private String palate;  // 맛

    @Column(columnDefinition = "TEXT")
    private String finish;  // 여운

    private Double rating;     // 평점
    private String imageUrl;   // 사진 경로
}