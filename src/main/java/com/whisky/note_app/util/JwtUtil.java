package com.whisky.note_app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * [JwtUtil — JWT 토큰 생성/검증 유틸리티]
 *
 * [담당하는 세 가지 역할]
 * 1. generateToken(): 로그인 성공 시 JWT 토큰 생성
 * 2. validateToken(): 요청이 올 때마다 토큰 유효성 검증
 * 3. getEmailFromToken(): 토큰에서 사용자 이메일 추출
 *
 * [@Value 주입]
 * application.properties의 jwt.secret, jwt.expiration 값을 필드에 주입합니다.
 * 하드코딩 대신 외부 설정으로 관리하는 것이 보안과 유지보수에 좋습니다.
 *
 * [HMAC SHA-256 (HS256) 알고리즘]
 * 대칭키 알고리즘입니다. 하나의 Secret Key로 서명하고 검증합니다.
 * 비대칭키(RS256)는 개인키로 서명하고 공개키로 검증하는 방식으로
 * MSA 환경에서 주로 사용합니다. 지금은 단일 서버이므로 HS256으로 충분합니다.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    /**
     * [생성자 주입 + SecretKey 초기화]
     * @Value로 주입받은 문자열을 HMAC-SHA 알고리즘용 SecretKey 객체로 변환합니다.
     * Keys.hmacShaKeyFor()는 키 길이를 자동으로 검증합니다. (256bit 미만이면 예외)
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * [JWT 토큰 생성]
     *
     * Payload에 담기는 정보 (Claims):
     * - subject: 사용자 이메일 (토큰의 주인을 식별하는 값)
     * - issuedAt: 토큰 발급 시각
     * - expiration: 토큰 만료 시각 (발급 시각 + expiration 밀리초)
     *
     * [왜 userId 대신 email을 subject로?]
     * 이후 Step 5 JWT 필터에서 토큰 → 이메일 추출 → DB에서 User 조회 흐름으로 사용합니다.
     * email은 unique하므로 식별자로 충분합니다.
     *
     * @param email 로그인한 사용자의 이메일
     * @return 서명된 JWT 토큰 문자열
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)          // Payload의 sub 클레임
                .issuedAt(now)           // Payload의 iat 클레임
                .expiration(expiryDate)  // Payload의 exp 클레임
                .signWith(secretKey)     // Signature: secretKey로 HS256 서명
                .compact();              // Header.Payload.Signature 형태로 직렬화
    }

    /**
     * [이메일 추출]
     * 토큰의 Payload에서 subject(이메일)를 꺼냅니다.
     * Step 5 JWT 필터에서 "이 토큰 주인이 누구인지" 알아낼 때 사용합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰에 담긴 이메일
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * [토큰 유효성 검증]
     * Step 5 JWT 필터에서 모든 요청마다 이 메서드를 호출합니다.
     *
     * 검증 항목:
     * - 서명이 올바른가 (Secret Key로 검증)
     * - 만료되지 않았는가 (exp 클레임 확인)
     * - 형식이 올바른가 (JWT 구조 확인)
     *
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 파싱 과정에서 서명 검증 + 만료 확인이 자동으로 이루어집니다
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰 만료 - 클라이언트에게 재로그인 요청
            return false;
        } catch (SignatureException e) {
            // 서명 불일치 - 다른 Secret Key로 서명된 토큰이거나 위조된 토큰
            return false;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // 토큰 형식 오류 또는 지원하지 않는 토큰
            return false;
        }
    }

    /**
     * [토큰 파싱 — 내부 공통 메서드]
     * generateToken()에서 서명할 때 사용한 secretKey로 검증합니다.
     * 서명이 다르거나 만료되면 예외를 던집니다.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // 서명 검증에 사용할 키 지정
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
