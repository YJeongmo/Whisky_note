package com.whisky.note_app.exception;

import java.time.LocalDateTime;

/**
 * API 에러 응답 형식입니다.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, message);
    }
}
