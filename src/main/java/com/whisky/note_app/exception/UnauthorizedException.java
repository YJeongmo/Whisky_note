package com.whisky.note_app.exception;

/**
 * [UnauthorizedException — 인증 실패 예외]
 *
 * 로그인 실패(이메일 없음, 비밀번호 불일치) 시 던집니다.
 * GlobalExceptionHandler가 이 예외를 잡아 401 Unauthorized로 응답합니다.
 *
 * [왜 IllegalArgumentException을 쓰지 않는가?]
 * IllegalArgumentException → GlobalExceptionHandler → 400 Bad Request
 * 로그인 실패는 400(잘못된 요청)이 아니라 401(인증 실패)이 의미상 올바릅니다.
 *
 * [보안 원칙: 실패 이유를 구분하지 않는다]
 * "이메일이 존재하지 않습니다" / "비밀번호가 틀렸습니다" 를 따로 알려주면
 * 공격자가 계정 존재 여부를 파악할 수 있습니다.
 * 항상 동일한 메시지로 응답합니다.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
