package com.whisky.note_app.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: LoginRequest — 로그인 요청]
 * POST /api/auth/login 요청 바디입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password; // 평문 비밀번호 (서비스에서 BCrypt 검증 후 즉시 버림)
}
