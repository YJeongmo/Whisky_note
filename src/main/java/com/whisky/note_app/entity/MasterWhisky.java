package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MasterWhisky {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName; // 위스키 이름
    private String distillery; // 증류소
    private String category; // 버번, 스카치등 대분류
    private String subCategory; // 캐스크등 소분류
    private Integer price; // 숫자로 저장 (예: 150000)
    private String priceRange; // 문자열로 저장 (예: "10만원대", "20만원대")

    @Column(length = 500)
    private String nose;

    @Column(length = 500)
    private String palate;

    @Column(length = 500)
    private String finish;
}
