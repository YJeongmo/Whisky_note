package com.whisky.note_app.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: UpdateNoteRequest — 노트 수정 요청]
 *
 * [왜 Create와 Update를 분리하는가?]
 * 지금은 필드가 동일해 보이지만, 실제로는 다음과 같이 달라질 수 있습니다:
 *
 * - 생성 시에만 필요한 필드: masterWhiskyId (연결할 마스터 위스키 선택)
 * - 수정 시에만 필요한 필드: (예) 수정 사유, 버전 번호(@Version) 등
 * - 생성 시엔 필수, 수정 시엔 선택: whiskyName (이름은 바꾸지 않는 경우도 있음)
 *
 * DTO를 분리하면 이러한 요구사항 변화에 유연하게 대응할 수 있습니다.
 * (나중에 @Valid 유효성 검사를 추가할 때도 Create/Update 규칙이 다를 수 있습니다)
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateNoteRequest {

    private String whiskyName;   // 위스키 이름
    private String category;     // 대분류
    private String subCategory;  // 소분류

    private String nose;   // 향
    private String palate; // 맛
    private String finish; // 여운

    private Double rating;   // 평점
    private String imageUrl; // 이미지 경로
}
