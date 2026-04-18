package com.whisky.note_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * [엔티티: User — 회원]
 *
 * [핵심 설계: UserDetails 구현]
 * Spring Security는 인증된 사용자를 UserDetails 인터페이스로 관리합니다.
 * User 엔티티가 직접 UserDetails를 구현하면:
 * → Spring Security가 DB 회원 정보를 바로 인증 객체로 사용 가능
 * → 별도의 변환 클래스(어댑터) 없이 코드가 단순해집니다
 *
 * [비밀번호 저장 방식]
 * password 필드에는 절대 평문을 저장하지 않습니다.
 * 회원가입 시 BCryptPasswordEncoder로 암호화한 값만 저장합니다.
 * 로그인 시에는 입력된 평문을 같은 방식으로 해시해서 저장된 값과 비교합니다.
 * BCrypt는 단방향 함수라 해시값에서 원문을 역산할 수 없습니다.
 *
 * [@Builder 패턴 선택 이유]
 * User 엔티티는 여러 필드가 있는데 생성자로 만들면 순서를 헷갈리기 쉽습니다.
 * 빌더는 필드명을 명시하므로 실수를 방지합니다.
 * MasterWhisky와 동일한 패턴: @NoArgsConstructor(PROTECTED) + @AllArgsConstructor + @Builder
 */
@Entity
@Table(name = "users") // 테이블명 명시: "user"는 PostgreSQL 예약어라 충돌 방지
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID로 사용 (unique 보장)

    @Column(nullable = false)
    private String password; // BCrypt 암호화된 값만 저장

    @Column(nullable = false)
    private String nickname; // 화면에 표시할 이름

    @Enumerated(EnumType.STRING) // Enum을 숫자(ORDINAL)가 아닌 문자열로 저장
    @Column(nullable = false)
    @Builder.Default // @Builder와 함께 기본값을 지정할 때 필요한 어노테이션
    private UserRole role = UserRole.USER; // 기본값: 일반 사용자

    // =========================================================
    // Spring Security - UserDetails 인터페이스 구현부
    // =========================================================

    /**
     * [getAuthorities — 권한 목록 반환]
     * Spring Security가 이 유저의 권한을 물어볼 때 호출합니다.
     * "ROLE_USER", "ROLE_ADMIN" 형태로 반환해야 합니다.
     *
     * SimpleGrantedAuthority: 문자열을 권한 객체로 감싸는 가장 단순한 구현체
     * "ROLE_" + role.name() → UserRole.USER → "ROLE_USER"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Spring Security가 로그인 ID로 사용할 값을 반환합니다.
     * 이 프로젝트에서는 이메일을 로그인 ID로 사용합니다.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 아래 4개 메서드는 계정 상태를 나타냅니다.
     * 지금은 모두 true(정상 상태)로 고정합니다.
     * 추후 계정 정지/잠금 기능을 추가할 때 이 부분을 수정합니다.
     */
    @Override
    public boolean isAccountNonExpired() { return true; }  // 계정 만료 여부

    @Override
    public boolean isAccountNonLocked() { return true; }   // 계정 잠금 여부

    @Override
    public boolean isCredentialsNonExpired() { return true; } // 비밀번호 만료 여부

    @Override
    public boolean isEnabled() { return true; } // 계정 활성화 여부
}
