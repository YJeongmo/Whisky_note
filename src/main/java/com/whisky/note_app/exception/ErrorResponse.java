package com.whisky.note_app.exception;

import java.time.LocalDateTime;

/**
 * [Java Record: ErrorResponse — API 에러 응답 형식]
 *
 * [Java Record란?]
 * Java 16에서 정식 도입된 기능으로, 불변(immutable) 데이터 클래스를 간결하게 만들 수 있습니다.
 * 아래 한 줄이 다음 모든 코드를 자동 생성합니다:
 * - private final 필드
 * - 생성자
 * - getter (timestamp(), status(), message() — 메서드 이름이 필드명과 동일)
 * - equals(), hashCode(), toString()
 *
 * [왜 Record를 쓰는가?]
 * - ErrorResponse는 한 번 만들면 내용이 바뀌지 않는 불변 객체입니다.
 * - @Getter @AllArgsConstructor @EqualsAndHashCode 등 Lombok 어노테이션 없이 깔끔합니다.
 * - 에러 응답같이 단순히 데이터를 담는 객체에 매우 적합합니다.
 *
 * [응답 JSON 예시]
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 400,
 *   "message": "해당 ID의 노트를 찾을 수 없습니다: 99"
 * }
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
    /**
     * [정적 팩토리 메서드: of()]
     * GlobalExceptionHandler에서 편하게 사용하기 위한 메서드입니다.
     * timestamp는 항상 현재 시각으로 자동 설정됩니다.
     *
     * 사용 예:
     *   ErrorResponse.of(400, "잘못된 요청입니다.");
     */
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, message);
    }
}
