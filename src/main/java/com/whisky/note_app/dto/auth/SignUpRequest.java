package com.whisky.note_app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [DTO: SignUpRequest — 회원가입 요청]
 *
 * [@Valid 동작 흐름]
 * 1. 클라이언트가 JSON 요청을 보냄
 * 2. Spring이 JSON → SignUpRequest 객체로 변환
 * 3. @Valid가 있으면 각 필드의 어노테이션을 검사
 * 4. 검증 실패 시 MethodArgumentNotValidException 발생
 * 5. GlobalExceptionHandler가 잡아서 400 Bad Request로 응답
 *
 * [어노테이션 설명]
 * @NotBlank: null, 빈 문자열(""), 공백만 있는 문자열(" ") 모두 거부
 * @Email: 이메일 형식 검증 (xxx@xxx.xxx 패턴)
 * @Size: 문자열 길이 범위 검증
 */
@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
}
