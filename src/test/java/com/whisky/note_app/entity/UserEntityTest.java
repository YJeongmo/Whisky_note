package com.whisky.note_app.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [순수 단위 테스트: UserEntityTest]
 *
 * Spring 컨텍스트, DB 연결이 전혀 필요 없습니다.
 * User 엔티티가 UserDetails 인터페이스를 올바르게 구현했는지만 검증합니다.
 *
 * [왜 이 테스트가 중요한가?]
 * Step 5에서 JWT 필터가 SecurityContext에 User를 등록할 때
 * getAuthorities(), getUsername() 값을 그대로 사용합니다.
 * 여기서 잘못된 값이 반환되면 인가(Authorization)가 전부 깨집니다.
 * 지금 단계에서 확실히 검증해두는 것이 중요합니다.
 */
class UserEntityTest {

    @Test
    @DisplayName("USER 역할을 가진 사용자의 권한은 'ROLE_USER'여야 한다")
    void userRoleAuthority() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("테스터")
                .role(UserRole.USER)
                .build();

        // when
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("ADMIN 역할을 가진 사용자의 권한은 'ROLE_ADMIN'이어야 한다")
    void adminRoleAuthority() {
        // given
        User admin = User.builder()
                .email("admin@test.com")
                .password("encodedPassword")
                .nickname("관리자")
                .role(UserRole.ADMIN)
                .build();

        // when
        Collection<? extends GrantedAuthority> authorities = admin.getAuthorities();

        // then
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("getUsername()은 이메일을 반환해야 한다")
    void getUsernameReturnsEmail() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("테스터")
                .build();

        // when & then
        // Spring Security는 getUsername()을 로그인 ID로 사용합니다.
        // 이 프로젝트는 이메일을 로그인 ID로 쓰므로 email이 반환되어야 합니다.
        assertThat(user.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("기본 role은 USER여야 한다")
    void defaultRoleIsUser() {
        // given: role을 명시하지 않고 생성
        // @Builder.Default 덕분에 role = UserRole.USER가 기본값으로 들어갑니다.
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("테스터")
                .build();

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("계정 상태 메서드들은 모두 true를 반환해야 한다")
    void accountStatusIsAllTrue() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .nickname("테스터")
                .build();

        // then: 현재는 계정 잠금/만료 기능이 없으므로 모두 true
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }
}
