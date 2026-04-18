package com.whisky.note_app.entity;

/**
 * [UserRole — 회원 권한 Enum]
 *
 * [왜 Enum을 쓰는가?]
 * DB에 "ADMIN", "USER" 문자열을 그냥 저장하면 오타가 생겨도 컴파일 에러가 없습니다.
 * Enum으로 정의하면 컴파일 타임에 타입 안전성이 보장됩니다.
 *
 * [DB 저장 방식 — @Enumerated(EnumType.STRING)]
 * Enum을 DB에 저장할 때 두 가지 방식이 있습니다:
 * - EnumType.ORDINAL: 0, 1, 2... 숫자로 저장 (위험: Enum 순서가 바뀌면 데이터 깨짐)
 * - EnumType.STRING: "USER", "ADMIN" 문자열로 저장 (권장: 순서에 무관하고 가독성 좋음)
 * → 항상 STRING을 사용합니다.
 *
 * [Spring Security와의 연결]
 * Spring Security는 권한을 "ROLE_" 접두사로 관리합니다.
 * 예: UserRole.USER → "ROLE_USER"로 변환해서 Security에 등록합니다.
 * (Step 5 JWT 필터 구현 시 사용)
 */
public enum UserRole {
    USER,   // 일반 사용자 → Spring Security에서 "ROLE_USER"
    ADMIN   // 관리자 → Spring Security에서 "ROLE_ADMIN" (추후 관리 기능 추가 시 사용)
}
