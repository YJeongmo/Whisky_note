package com.whisky.note_app.dto.response;

import com.whisky.note_app.entity.TastingNote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * [DTO: NoteResponse — 노트 조회 응답]
 *
 * [왜 응답도 DTO로 분리하는가?]
 * 엔티티를 그대로 JSON으로 내보내면:
 * 1. 순환 참조: TastingNote → MasterWhisky → (다시 참조) → 무한 루프로 StackOverflow 발생 가능
 * 2. 불필요한 노출: DB 내부 구조가 그대로 클라이언트에 노출됨 (보안, 유지보수 문제)
 * 3. 지연 로딩 예외: @ManyToOne(fetch=LAZY) 필드를 트랜잭션 밖에서 JSON으로 직렬화하면
 *    LazyInitializationException 발생
 *
 * → 응답 DTO를 만들어 필요한 데이터만 담아서 내보냅니다.
 *
 * [@Builder 사용 이유]
 * - 필드가 많은 객체를 생성할 때 생성자보다 가독성이 훨씬 좋습니다.
 *   NoteResponse.builder().id(1L).whiskyName("맥캘란").rating(8.5).build()
 * - 응답 DTO는 서버가 직접 조립해서 내보내므로 @Setter가 필요 없고 @Builder가 적합합니다.
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

    // 연관된 마스터 위스키 정보 (null이면 임의 입력 노트)
    private Long masterWhiskyId;
    private String masterWhiskyName;

    /**
     * [정적 팩토리 메서드: from()]
     * 엔티티 → DTO 변환 로직을 DTO 내부에 캡슐화합니다.
     *
     * 서비스에서 이렇게 사용합니다:
     *   return NoteResponse.from(note);
     *
     * 장점:
     * - 변환 로직이 한 곳에 모여 있어 유지보수가 쉽습니다.
     * - 서비스 코드가 깔끔해집니다.
     * - masterWhisky가 null인 경우(임의 입력 노트)도 NPE 없이 안전하게 처리합니다.
     */
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
                // masterWhisky가 null(임의 입력 노트)이면 NPE 방지를 위해 삼항 연산자 사용
                .masterWhiskyId(note.getMasterWhisky() != null ? note.getMasterWhisky().getId() : null)
                .masterWhiskyName(note.getMasterWhisky() != null ? note.getMasterWhisky().getWhiskyName() : null)
                .build();
    }
}
