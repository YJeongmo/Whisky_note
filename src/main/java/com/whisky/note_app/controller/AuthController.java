package com.whisky.note_app.controller;

import com.whisky.note_app.dto.auth.LoginRequest;
import com.whisky.note_app.dto.auth.LoginResponse;
import com.whisky.note_app.dto.auth.SignUpRequest;
import com.whisky.note_app.dto.auth.SignUpResponse;
import com.whisky.note_app.service.AuthService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [AuthController — 인증 API]
 *
 * [경로: /api/auth]
 * 인증 관련 API를 /api/auth 하위로 모읍니다.
 * Step 5 Security 설정에서 /api/auth/** 는 인증 없이 접근 가능하도록 열어둘 예정입니다.
 *
 * 현재 구현된 API:
 * - POST /api/auth/signup  : 회원가입
 *
 * Step 4에서 추가될 API:
 * - POST /api/auth/login   : 로그인 (JWT 발급)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * [POST /api/auth/signup — 회원가입]
     *
     * 성공: 201 Created + SignUpResponse
     * 실패 (이메일 중복): 400 Bad Request + ErrorResponse (GlobalExceptionHandler 처리)
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [POST /api/auth/login — 로그인]
     *
     * 성공: 200 OK + LoginResponse (JWT 토큰 포함)
     * 실패 (이메일/비밀번호 불일치): 401 Unauthorized + ErrorResponse
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
