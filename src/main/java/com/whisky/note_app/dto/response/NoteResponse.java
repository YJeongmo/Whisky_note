package com.whisky.note_app.dto.response;

import com.whisky.note_app.entity.TastingNote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 테이스팅 노트 조회 응답 DTO입니다.
 * 엔티티 → DTO 변환은 {@link #from(TastingNote)}을 사용합니다.
 */
@Getter
@Builder
public class NoteResponse {

    private Long id;
    private String whiskyName;
    private String category;
    private String subCategory;

    private String nose;
    private String palate;
    private String finish;

    private Double rating;
    private String imageUrl;
    private LocalDate createdAt;

    private Long masterWhiskyId;
    private String masterWhiskyName;

    public static NoteResponse from(TastingNote note) {
        return NoteResponse.builder()
                .id(note.getId())
                .whiskyName(note.getWhiskyName())
                .category(note.getCategory())
                .subCategory(note.getSubCategory())
                .nose(note.getNose())
                .palate(note.getPalate())
                .finish(note.getFinish())
                .rating(note.getRating())
                .imageUrl(note.getImageUrl())
                .createdAt(note.getCreatedAt())
                .masterWhiskyId(note.getMasterWhisky() != null ? note.getMasterWhisky().getId() : null)
                .masterWhiskyName(note.getMasterWhisky() != null ? note.getMasterWhisky().getWhiskyName() : null)
                .build();
    }
}
