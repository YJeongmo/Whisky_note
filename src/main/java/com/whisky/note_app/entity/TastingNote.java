package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * 사용자가 작성한 위스키 테이스팅 노트 엔티티입니다.
 * masterWhisky는 nullable FK — 마스터 DB에 없는 위스키도 자유롭게 입력할 수 있습니다.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TastingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName;
    private String category;
    private String subCategory;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdAt;

    @Column(columnDefinition = "TEXT")
    private String nose;

    @Column(columnDefinition = "TEXT")
    private String palate;

    @Column(columnDefinition = "TEXT")
    private String finish;

    private Double rating;
    private String imageUrl;

    // 마스터 DB 연결 — 임의 입력 노트는 null
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "master_whisky_id")
    private MasterWhisky masterWhisky;

    // 작성자 — 데이터 격리의 기준이 되는 필드
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
