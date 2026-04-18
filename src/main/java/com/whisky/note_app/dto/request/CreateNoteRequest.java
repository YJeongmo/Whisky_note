package com.whisky.note_app.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: CreateNoteRequest — 노트 생성 요청]
 *
 * [왜 DTO를 쓰는가?]
 * 컨트롤러가 엔티티(TastingNote)를 직접 @RequestBody로 받으면 여러 문제가 생깁니다:
 *
 * 1. 보안 문제 (Mass Assignment)
 *    - 클라이언트가 'id', 'createdAt' 같은 서버에서 관리해야 할 필드도 임의로 값을 보낼 수 있습니다.
 *    - DTO는 받고 싶은 필드만 정의하므로 이 문제를 원천 차단합니다.
 *
 * 2. 관심사 분리
 *    - 엔티티는 DB와의 매핑 규칙(@Column, @ManyToOne 등)에 집중해야 합니다.
 *    - API 요청/응답 형태는 클라이언트 필요에 따라 언제든 달라질 수 있어,
 *      DTO에서만 변경하면 엔티티 구조는 유지됩니다.
 *
 * 3. 유효성 검사 (@Valid) — Phase 1에서 추가 예정
 *    - DTO 필드에 @NotBlank, @Min 같은 어노테이션을 달아 입력값 검증을 할 수 있습니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateNoteRequest {

    private Long masterWhiskyId; // (선택) 마스터 DB의 위스키 ID — null이면 임의 입력으로 처리

    private String whiskyName;   // 위스키 이름
    private String category;     // 대분류 (스카치, 버번 등)
    private String subCategory;  // 소분류 (피트, 쉐리 등)

    private String nose;   // 향
    private String palate; // 맛
    private String finish; // 여운

    private Double rating;   // 평점
    private String imageUrl; // 이미지 경로 (Phase 6 파일 업로드 시 활성화)
}
