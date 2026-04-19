package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [엔티티: MasterWhisky — 위스키 마스터 데이터]
 * CSV 파일에서 로드된 위스키 정보(이름, 증류소, 향미 등)를 저장합니다.
 * 사용자의 TastingNote가 이 엔티티를 FK로 참조할 수 있습니다.
 *
 * [@Builder + @NoArgsConstructor(PROTECTED) + @AllArgsConstructor 조합]
 *
 * 이 세 가지를 함께 쓰는 이유:
 *
 * 1. @Builder
 *    - 필드가 많은 객체를 가독성 있게 생성할 수 있습니다.
 *      예: MasterWhisky.builder().whiskyName("맥캘란").category("싱글몰트").build()
 *    - 내부적으로 @AllArgsConstructor를 필요로 합니다 (모든 필드를 받는 생성자가 있어야 빌더가 동작)
 *
 * 2. @NoArgsConstructor(access = AccessLevel.PROTECTED)
 *    - JPA는 엔티티를 DB에서 읽어올 때 기본 생성자(인자 없는 생성자)가 필요합니다.
 *    - PROTECTED로 설정하면 외부에서 new MasterWhisky() 호출을 막을 수 있습니다.
 *    - 즉, JPA 내부 사용은 허용하고, 개발자가 실수로 빈 객체를 만드는 것은 방지합니다.
 *
 * 3. @AllArgsConstructor
 *    - @Builder가 사용할 내부 생성자입니다.
 *    - @Builder와 @NoArgsConstructor를 함께 쓸 때 Lombok이 충돌을 일으키므로,
 *      @AllArgsConstructor를 명시적으로 추가해야 합니다.
 *
 * [DataInitializer에서의 사용 예]
 * MasterWhisky master = MasterWhisky.builder()
 *     .whiskyName(name)
 *     .distillery(distillery)
 *     .category(category)
 *     .build();
 */
@Entity
@Getter
@Builder
@NoArgsConstructor  // Redis 역직렬화(Jackson)를 위해 public 기본 생성자 필요
@AllArgsConstructor
public class MasterWhisky {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String whiskyName; // 위스키 이름
    private String distillery; // 증류소
    private String category;   // 대분류 (버번, 스카치 등)
    private String subCategory; // 소분류 (캐스크 종류 등)
    private Integer price;      // 가격 (숫자, 예: 150000)
    private String priceRange;  // 가격대 문자열 (예: "10만원대", "20만원대")

    @Column(length = 500)
    private String nose;   // 향

    @Column(length = 500)
    private String palate; // 맛

    @Column(length = 500)
    private String finish; // 여운
}
