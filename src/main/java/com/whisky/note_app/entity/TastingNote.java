package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * [엔티티: TastingNote]
 * 사용자가 작성한 위스키 테이스팅 노트 한 건을 나타냅니다.
 *
 * [패키지 이동: domain → entity]
 * Spring Boot 관례상 DB 테이블과 1:1 대응하는 JPA 엔티티는 entity 패키지에 둡니다.
 * domain 패키지는 보통 비즈니스 규칙/도메인 서비스 같은 순수 비즈니스 로직용으로 남겨둡니다.
 *
 * [연관관계: MasterWhisky와 @ManyToOne]
 * - 테이스팅 노트 여러 개(Many)가 마스터 위스키 하나(One)를 참조할 수 있습니다.
 * - fetch = LAZY: MasterWhisky 정보를 즉시 JOIN하지 않고, 실제로 접근할 때만 쿼리합니다.
 *   → 불필요한 쿼리 방지, 성능 최적화의 기본 원칙
 * - optional = true: 노트를 저장할 때 MasterWhisky가 없어도 됩니다 (nullable FK).
 *   → 사용자가 자유롭게 임의 위스키 이름으로 노트를 쓸 수 있게 허용
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // @CreatedDate 자동 처리를 위해 필요
public class TastingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName; // 위스키 이름 (직접 입력 또는 마스터에서 가져온 이름)

    private String category;    // 대분류 (스카치, 버번 등)
    private String subCategory; // 소분류 (피트, 쉐리 등)

    /**
     * [@CreatedDate + @Column(updatable = false)]
     * - @CreatedDate: JPA Auditing이 엔티티 최초 저장 시 자동으로 현재 날짜를 넣어줍니다.
     * - updatable = false: 한 번 기록된 생성일은 update 쿼리에서 제외됩니다. (불변)
     * - 이 기능이 동작하려면 @EnableJpaAuditing 설정과 @EntityListeners 가 있어야 합니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdAt;

    @Column(columnDefinition = "TEXT") // VARCHAR 기본값은 255자 → 긴 텍스트는 TEXT 타입 명시
    private String nose;    // 향 (Nose)

    @Column(columnDefinition = "TEXT")
    private String palate;  // 맛 (Palate)

    @Column(columnDefinition = "TEXT")
    private String finish;  // 여운 (Finish)

    private Double rating;   // 평점 (0.0 ~ 10.0)
    private String imageUrl; // 사진 저장 경로 (Phase 6에서 파일 업로드 기능 추가 예정)

    /**
     * [MasterWhisky와의 연관관계]
     * - @ManyToOne(fetch=LAZY, optional=true): 지연 로딩, nullable FK
     * - @JoinColumn(name="master_whisky_id"): DB 컬럼명을 명시적으로 지정
     *   (지정 안 하면 JPA가 자동으로 만들지만, 명시하면 가독성과 유지보수가 좋아집니다)
     *
     * 사용자가 마스터 DB에 있는 위스키를 선택하면 이 FK가 채워지고,
     * 임의 이름으로만 입력하면 null이 됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "master_whisky_id")
    private MasterWhisky masterWhisky;

    /**
     * [User와의 연관관계 — Step 6 추가]
     * - @ManyToOne(fetch=LAZY, optional=false): 노트는 반드시 작성자가 있어야 합니다.
     * - optional=false → DB에서 NOT NULL FK로 생성됩니다.
     * - LAZY: 노트 조회 시 User 정보를 즉시 JOIN하지 않습니다.
     *   NoteResponse 변환 시 user.getId()만 사용하므로 프록시 객체로 충분합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
