package com.whisky.note_app.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateNoteRequest {

    private Long masterWhiskyId; // (선택) 마스터 DB의 위스키 ID

    @NotBlank(message = "위스키 이름을 입력해주세요.")
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
