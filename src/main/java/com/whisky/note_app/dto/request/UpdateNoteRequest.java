package com.whisky.note_app.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: UpdateNoteRequest — 노트 수정 요청]
 *
 * 수정은 부분 업데이트(PATCH 방식)를 따릅니다.
 * 입력된 필드만 변경하고 null인 필드는 기존 값을 유지합니다.
 * 따라서 whiskyName은 @NotBlank를 달지 않습니다 (수정 안 해도 되니까).
 * rating만 범위 검증을 추가합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateNoteRequest {

    private String whiskyName;
    private String category;
    private String subCategory;

    private String nose;
    private String palate;
    private String finish;

    @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "10.0", message = "평점은 10.0 이하여야 합니다.")
    private Double rating;

    private String imageUrl;
}
