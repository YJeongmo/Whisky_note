package com.whisky.note_app.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [JwtUtilTest — 순수 단위 테스트]
 *
 * Spring 컨텍스트 없이 JwtUtil만 직접 생성해서 테스트합니다.
 * @Value 대신 생성자로 값을 직접 주입합니다.
 *
 * 검증 항목:
 * 1. 토큰 생성 및 이메일 추출
 * 2. 유효한 토큰 검증
 * 3. 만료된 토큰 처리
 * 4. 위조된 토큰 처리
 * 5. 다른 Secret Key로 서명한 토큰 처리
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // 테스트용 설정값 (application-test.yml과 동일하게 맞춥니다)
    private static final String TEST_SECRET =
            "test-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long EXPIRATION = 86400000L; // 24시간

    @BeforeEach
    void setUp() {
        // Spring 없이 생성자로 직접 주입
        jwtUtil = new JwtUtil(TEST_SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("토큰 생성 후 이메일을 정상적으로 추출할 수 있어야 한다")
    void generateAndExtractEmail() {
        // given
        String email = "test@test.com";

        // when
        String token = jwtUtil.generateToken(email);
        String extracted = jwtUtil.getEmailFromToken(token);

        // then
        assertThat(token).isNotBlank();
        assertThat(extracted).isEqualTo(email);
    }

    @Test
    @DisplayName("정상 토큰은 validateToken()이 true를 반환해야 한다")
    void validateToken_valid() {
        // given
        String token = jwtUtil.generateToken("test@test.com");

        // when & then
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken()이 false를 반환해야 한다")
    void validateToken_expired() {
        // given: 만료 시간을 -1ms(이미 만료)로 설정한 JwtUtil
        JwtUtil expiredJwtUtil = new JwtUtil(TEST_SECRET, -1L);
        String expiredToken = expiredJwtUtil.generateToken("test@test.com");

        // when & then
        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("위조된 토큰(변조된 문자열)은 validateToken()이 false를 반환해야 한다")
    void validateToken_malformed() {
        // given: 올바르지 않은 형식의 문자열
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // when & then
        assertThat(jwtUtil.validateToken(malformedToken)).isFalse();
    }

    @Test
    @DisplayName("다른 Secret Key로 서명된 토큰은 validateToken()이 false를 반환해야 한다")
    void validateToken_wrongSecret() {
        // given: 다른 Secret Key를 사용하는 JwtUtil로 만든 토큰
        JwtUtil otherJwtUtil = new JwtUtil(
                "completely-different-secret-key-also-256-bits-long-yes-it-is",
                EXPIRATION
        );
        String tokenFromOtherServer = otherJwtUtil.generateToken("test@test.com");

        // when: 원래 JwtUtil로 검증 시도 → 서명이 달라서 실패해야 함
        assertThat(jwtUtil.validateToken(tokenFromOtherServer)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열은 validateToken()이 false를 반환해야 한다")
    void validateToken_emptyString() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("토큰은 Header.Payload.Signature 세 부분으로 구성되어야 한다")
    void tokenStructure() {
        // given
        String token = jwtUtil.generateToken("test@test.com");

        // when
        String[] parts = token.split("\\.");

        // then: JWT는 반드시 점(.)으로 구분된 3개 파트여야 합니다
        assertThat(parts).hasSize(3);
    }
}
