package com.whisky.note_app.entity;

/**
 * 회원 권한 타입입니다.
 * DB 저장 시 EnumType.STRING(문자열)을 사용합니다 — ORDINAL은 Enum 순서 변경 시 데이터가 깨집니다.
 */
public enum UserRole {
    USER,
    ADMIN
}
