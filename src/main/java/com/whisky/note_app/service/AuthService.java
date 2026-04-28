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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * @throws IllegalArgumentException 이메일 중복 시
     */
    public SignUpResponse signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .build();

        User savedUser = userRepository.save(user);

        return SignUpResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .build();
    }

    /**
     * 이메일/비밀번호 검증 후 JWT 토큰을 발급합니다.
     * 이메일 존재 여부와 비밀번호 불일치를 같은 메시지로 처리합니다 — 계정 열거 공격 방지.
     *
     * @throws UnauthorizedException 이메일 없음 또는 비밀번호 불일치 시
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
