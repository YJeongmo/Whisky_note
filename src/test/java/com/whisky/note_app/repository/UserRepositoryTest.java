package com.whisky.note_app.repository;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * [UserRepositoryTest — @DataJpaTest 슬라이스 테스트]
 *
 * JPA 레이어만 로드하므로 @SpringBootTest보다 훨씬 빠릅니다.
 * Service, Controller는 로드되지 않습니다.
 *
 * 검증 항목:
 * 1. 회원 저장 및 이메일로 조회
 * 2. 존재하지 않는 이메일 조회 시 Optional.empty() 반환
 * 3. existsByEmail — true/false 정확성
 * 4. email unique 제약 조건 동작
 * 5. 기본 role이 USER로 저장되는지
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // 테스트용 User 생성 헬퍼 메서드
    // 실제 비밀번호 암호화는 서비스 레이어에서 담당합니다.
    // Repository 테스트에서는 암호화 여부와 무관하게 저장/조회 동작만 검증합니다.
    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password("$2a$10$dummyBcryptHashedPassword") // BCrypt 형식 더미값
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("저장한 회원을 이메일로 조회할 수 있어야 한다")
    void findByEmail_success() {
        // given
        User user = createUser("test@test.com", "테스터");
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("test@test.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회 시 Optional.empty()를 반환해야 한다")
    void findByEmail_notFound() {
        // when
        Optional<User> found = userRepository.findByEmail("nobody@test.com");

        // then
        // null이 아닌 Optional.empty()를 반환해야 합니다.
        // 호출부에서 .orElseThrow()로 안전하게 처리할 수 있습니다.
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail — 존재하는 이메일은 true를 반환해야 한다")
    void existsByEmail_true() {
        // given
        userRepository.save(createUser("exists@test.com", "기존유저"));

        // when & then
        assertThat(userRepository.existsByEmail("exists@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail — 존재하지 않는 이메일은 false를 반환해야 한다")
    void existsByEmail_false() {
        // when & then
        assertThat(userRepository.existsByEmail("new@test.com")).isFalse();
    }

    @Test
    @DisplayName("role을 명시하지 않으면 기본값 USER로 저장되어야 한다")
    void defaultRoleSavedAsUser() {
        // given: role을 builder에서 생략 (@Builder.Default 동작 확인)
        User user = User.builder()
                .email("default@test.com")
                .password("hashedPw")
                .nickname("기본유저")
                .build(); // role 미지정

        userRepository.save(user);

        // when
        User found = userRepository.findByEmail("default@test.com").get();

        // then
        assertThat(found.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("동일한 이메일로 두 번 저장하면 unique 제약 위반 예외가 발생해야 한다")
    void duplicateEmail_throwsException() {
        // given
        userRepository.save(createUser("dup@test.com", "유저1"));

        // when & then
        // unique 제약 위반 시 DataIntegrityViolationException 또는 그 상위 예외 발생
        // 정확한 예외 타입보다 "예외가 발생한다"는 사실 자체를 검증합니다.
        User duplicate = createUser("dup@test.com", "유저2");
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicate); // flush해야 즉시 DB에 반영되어 예외 발생
        });
    }
}
