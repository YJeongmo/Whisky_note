package com.whisky.note_app.service;

import com.whisky.note_app.dto.auth.SignUpRequest;
import com.whisky.note_app.dto.auth.SignUpResponse;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * [AuthServiceTest — 서비스 단위 테스트]
 *
 * [@ExtendWith(MockitoExtension.class)]
 * Spring 컨텍스트 없이 Mockito만으로 테스트합니다. 가장 빠른 테스트입니다.
 *
 * [Mock 이란?]
 * 실제 구현체 대신 "가짜 객체"를 주입합니다.
 * UserRepository Mock: 실제 DB 없이 "save하면 이 값을 반환해라" 같은 동작을 지정합니다.
 * PasswordEncoder Mock: 실제 BCrypt 계산 없이 "encode하면 'encoded_pw'를 반환해라"로 설정합니다.
 *
 * [BDDMockito.given()]
 * given(mock.method()).willReturn(value) 형태로 Mock 동작을 지정합니다.
 * BDD(Behavior Driven Development) 스타일로 given/when/then이 코드와 일치해서 가독성이 좋습니다.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // @Mock들을 AuthService 생성자에 주입합니다
    private AuthService authService;

    @Test
    @DisplayName("정상적인 회원가입 시 SignUpResponse가 반환되어야 한다")
    void signup_success() {
        // given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@test.com");
        request.setPassword("plainPassword");
        request.setNickname("테스터");

        // Mock 동작 설정
        given(userRepository.existsByEmail("test@test.com")).willReturn(false); // 중복 없음
        given(passwordEncoder.encode("plainPassword")).willReturn("$2a$encodedPassword"); // 암호화 결과
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            // save()가 호출되면 입력받은 User를 그대로 반환 (id만 1L로 설정)
            User user = invocation.getArgument(0);
            return User.builder()
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .nickname(user.getNickname())
                    .role(UserRole.USER)
                    .build();
        });

        // when
        SignUpResponse response = authService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("테스터");

        // 비밀번호 암호화가 실제로 호출됐는지 검증
        verify(passwordEncoder).encode("plainPassword");
        // userRepository.save()가 한 번 호출됐는지 검증
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 가입 시 IllegalArgumentException이 발생해야 한다")
    void signup_duplicateEmail() {
        // given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("dup@test.com");
        request.setPassword("password");
        request.setNickname("중복유저");

        // 이미 존재하는 이메일로 Mock 설정
        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일");

        // 중복 이메일이면 save()가 절대 호출되면 안 됩니다
        verify(userRepository, never()).save(any(User.class));
        // 암호화도 호출되면 안 됩니다 (불필요한 연산 방지)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("회원가입 시 비밀번호는 암호화되어 저장되어야 한다")
    void signup_passwordEncoded() {
        // given
        SignUpRequest request = new SignUpRequest();
        request.setEmail("test@test.com");
        request.setPassword("myPlainPassword");
        request.setNickname("테스터");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode("myPlainPassword")).willReturn("$2a$hashedValue");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        authService.signup(request);

        // then: save()에 전달된 User의 비밀번호가 암호화된 값인지 검증
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$hashedValue") // 평문이 아닌 암호화된 값
        ));
    }

    // verify(mock).save(argThat(...)) 에서 사용하는 헬퍼
    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
