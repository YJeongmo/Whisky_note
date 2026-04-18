package com.whisky.note_app.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: SignUpRequest — 회원가입 요청]
 *
 * 클라이언트가 POST /api/auth/signup 으로 보내는 요청 바디입니다.
 *
 * [password를 여기서 받는 이유]
 * 평문 비밀번호는 요청 DTO에서만 존재하고,
 * 서비스 레이어에서 즉시 BCrypt로 암호화한 뒤 User 엔티티에 저장합니다.
 * 암호화된 값만 DB에 들어가고, 평문은 어디에도 저장되지 않습니다.
 *
 * [추후 @Valid 유효성 검사 추가 예정]
 * - @NotBlank: 이메일, 비밀번호, 닉네임 필수 입력
 * - @Email: 이메일 형식 검증
 * - @Size(min=8): 비밀번호 최소 길이
 * Phase 1 완성 후 추가할 예정입니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {
    private String email;
    private String password;
    private String nickname;
}
