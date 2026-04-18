package com.whisky.note_app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [전역 예외 처리: GlobalExceptionHandler]
 *
 * [@RestControllerAdvice란?]
 * - @ControllerAdvice + @ResponseBody의 조합입니다.
 * - 애플리케이션 전체의 컨트롤러에서 발생하는 예외를 한 곳에서 처리합니다.
 * - 각 컨트롤러마다 try-catch를 쓰지 않아도 됩니다.
 *
 * [예외 계층 구조]
 * IllegalArgumentException (400 Bad Request)
 *   → 클라이언트가 잘못된 값을 보낸 경우 (없는 ID 조회, 잘못된 파라미터 등)
 *
 * Exception (500 Internal Server Error)
 *   → 서버 내부에서 예상치 못한 에러가 발생한 경우
 *   → 위에서 처리되지 않은 모든 예외를 최종적으로 잡아냅니다 (catch-all)
 *
 * [Phase 1 이후 확장 예정]
 * - EntityNotFoundException → 404 Not Found
 * - MethodArgumentNotValidException → 400 Bad Request (입력값 유효성 실패)
 * - AccessDeniedException → 403 Forbidden (Spring Security 추가 후)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [400 Bad Request]
     * 서비스에서 throw new IllegalArgumentException("메시지") 를 던지면 여기서 잡힙니다.
     *
     * 예: findNoteById에서 존재하지 않는 ID 요청 시
     *     throw new IllegalArgumentException("해당 ID의 노트를 찾을 수 없습니다: " + id)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[400 Bad Request] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, e.getMessage()));
    }

    /**
     * [401 Unauthorized]
     * 로그인 실패 시 AuthService에서 던지는 UnauthorizedException을 처리합니다.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        log.warn("[401 Unauthorized] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, e.getMessage()));
    }

    /**
     * [500 Internal Server Error]
     * 예상치 못한 모든 예외를 처리합니다.
     * 클라이언트에게는 일반적인 오류 메시지만 보내고,
     * 실제 스택 트레이스는 서버 로그에만 남깁니다. (보안상 중요)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[500 Internal Server Error] 예상치 못한 오류 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "서버 내부 오류가 발생했습니다."));
    }
}
