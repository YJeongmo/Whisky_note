package com.whisky.note_app.dto.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * [DTO: SignUpResponse — 회원가입 응답]
 *
 * 회원가입 성공 시 클라이언트에게 돌려주는 응답입니다.
 *
 * [왜 id와 email만 반환하는가?]
 * - 비밀번호(암호화 여부와 무관하게)는 절대 응답에 포함하지 않습니다.
 * - 클라이언트가 회원가입 성공을 확인하기 위한 최소한의 정보만 반환합니다.
 * - id: 이후 다른 API에서 사용자를 식별할 때 참고할 수 있습니다.
 */
@Getter
@Builder
public class SignUpResponse {
    private Long id;
    private String email;
    private String nickname;
}
