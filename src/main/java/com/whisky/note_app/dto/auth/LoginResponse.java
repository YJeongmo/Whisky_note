package com.whisky.note_app.dto.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * [DTO: LoginResponse — 로그인 응답]
 *
 * 로그인 성공 시 JWT 토큰을 발급해서 반환합니다.
 * 클라이언트는 이후 모든 API 요청 시 헤더에 토큰을 포함합니다:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
@Getter
@Builder
public class LoginResponse {
    private String token;     // JWT 액세스 토큰
    private String email;     // 로그인한 사용자 이메일
    private String nickname;  // 화면 표시용 닉네임
}
