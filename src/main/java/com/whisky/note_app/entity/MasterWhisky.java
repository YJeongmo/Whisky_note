package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * CSV 파일에서 로드된 위스키 마스터 데이터 엔티티입니다.
 * TastingNote가 FK로 참조합니다.
 *
 * @NoArgsConstructor는 public으로 열어둡니다 — Redis 역직렬화 시 Jackson이 기본 생성자를 필요로 합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterWhisky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName;
    private String distillery;
    private String category;
    private String subCategory;
    private Integer price;
    private String priceRange;

    @Column(length = 500)
    private String nose;

    @Column(length = 500)
    private String palate;

    @Column(length = 500)
    private String finish;
}
