package com.whisky.note_app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password; // 로그인은 비밀번호 길이 검증 불필요 (형식만 확인)
}
