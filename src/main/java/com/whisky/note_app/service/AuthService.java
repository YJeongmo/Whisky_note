package com.whisky.note_app.service;

import com.whisky.note_app.dto.auth.LoginRequest;
import com.whisky.note_app.dto.auth.LoginResponse;
import com.whisky.note_app.dto.auth.SignUpRequest;
import com.whisky.note_app.dto.auth.SignUpResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.exception.UnauthorizedException;
import com.whisky.note_app.repository.UserRepository;
import com.whisky.note_app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [AuthService — 인증 관련 비즈니스 로직]
 *
 * [회원가입 처리 흐름]
 * 1. 이메일 중복 확인 → 중복이면 IllegalArgumentException
 * 2. 비밀번호 BCrypt 암호화
 * 3. User 엔티티 생성 및 저장
 * 4. SignUpResponse DTO 반환
 *
 * [왜 서비스에서 암호화하는가?]
 * 암호화는 비즈니스 로직의 일부입니다.
 * - Controller는 요청/응답 처리만 담당
 * - Repository는 DB 저장만 담당
 * - "평문 비밀번호를 받아서 암호화한 뒤 저장한다"는 규칙 → Service가 담당
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * [회원가입]
     *
     * @throws IllegalArgumentException 이메일 중복 시
     */
    public SignUpResponse signup(SignUpRequest request) {
        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 2. 비밀번호 암호화
        // passwordEncoder.encode(): 평문 → BCrypt 해시값
        // 매번 다른 salt가 적용되어 같은 비밀번호도 다른 해시값이 나옵니다.
        // 검증 시에는 passwordEncoder.matches(평문, 해시값)으로 비교합니다.
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성 (Builder 패턴)
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword) // 반드시 암호화된 값만 저장
                .nickname(request.getNickname())
                // role은 기본값 USER (@Builder.Default)
                .build();

        User savedUser = userRepository.save(user);

        // 4. 응답 DTO 반환 (비밀번호는 응답에 포함하지 않음)
        return SignUpResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .build();
    }

    /**
     * [로그인]
     * 이메일/비밀번호 검증 후 JWT 토큰을 발급합니다.
     *
     * @throws UnauthorizedException 이메일 없음 또는 비밀번호 불일치 시
     *         (이유를 구분하지 않아 공격자가 계정 존재 여부를 알 수 없게 합니다)
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        // 존재하지 않으면 UnauthorizedException (404가 아닌 401 — 계정 존재 여부 숨김)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증
        // passwordEncoder.matches(평문, 암호화된값): 동일한 평문이면 true 반환
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. JWT 토큰 발급
        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
