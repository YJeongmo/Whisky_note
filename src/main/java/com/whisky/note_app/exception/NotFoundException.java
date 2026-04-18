package com.whisky.note_app.exception;

/**
 * [404 Not Found 전용 예외]
 *
 * 기존에는 존재하지 않는 리소스 조회 시 IllegalArgumentException(400)을 던졌습니다.
 * REST 설계 원칙상 리소스가 없는 경우는 404 Not Found가 올바른 응답입니다.
 *
 * [IllegalArgumentException과의 구분]
 * - NotFoundException    : 리소스가 DB에 없음 (없는 ID 조회 등) → 404
 * - IllegalArgumentException : 요청 자체가 잘못됨 (중복 이메일, 잘못된 파라미터 등) → 400
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
