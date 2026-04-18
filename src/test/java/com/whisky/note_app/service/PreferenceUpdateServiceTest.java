package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * [PreferenceUpdateServiceTest — Step 9]
 *
 * [@ExtendWith(MockitoExtension.class)]
 * Spring 컨텍스트 없이 Mockito만으로 단위 테스트합니다.
 * @SpringBootTest보다 훨씬 빠르고, 외부 의존성(DB, AI)이 필요 없습니다.
 *
 * [왜 Mockito로 테스트하나?]
 * 실제 DB 환경에서 낙관적 락 충돌을 의도적으로 만들려면
 * 멀티스레드 타이밍을 정밀하게 제어해야 해서 매우 어렵습니다.
 * 대신 Repository를 모킹하여 saveAndFlush() 호출 시
 * ObjectOptimisticLockingFailureException을 강제로 발생시켜 재시도 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class PreferenceUpdateServiceTest {

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @InjectMocks
    private PreferenceUpdateService preferenceUpdateService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@test.com")
                .password("pw")
                .nickname("테스터")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("정상 케이스: 키워드가 없으면 새로 생성되고 score가 저장되어야 한다")
    void updateWithRetry_success_newKeyword() {
        // given
        given(preferenceRepository.findByUserAndKeyword(testUser, "피트"))
                .willReturn(Optional.empty()); // 키워드 없음 → 새로 생성
        given(preferenceRepository.saveAndFlush(any(UserPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0)); // 저장된 객체 그대로 반환

        // when
        preferenceUpdateService.updateWithRetry("피트", 1, testUser);

        // then
        verify(preferenceRepository, times(1)).saveAndFlush(any(UserPreference.class));
    }

    @Test
    @DisplayName("재시도 초과: 3번 연속 낙관적 락 충돌 시 RuntimeException이 발생해야 한다")
    void updateWithRetry_exhausted() {
        // given
        given(preferenceRepository.findByUserAndKeyword(testUser, "피트"))
                .willReturn(Optional.of(new UserPreference(testUser, "피트", 5)));
        given(preferenceRepository.saveAndFlush(any(UserPreference.class)))
                .willThrow(new ObjectOptimisticLockingFailureException(UserPreference.class, 1L)); // 항상 충돌

        // when & then
        assertThatThrownBy(() -> preferenceUpdateService.updateWithRetry("피트", 1, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("동시 요청이 많습니다");

        // 3번 재시도 후 포기했는지 확인
        verify(preferenceRepository, times(3)).saveAndFlush(any(UserPreference.class));
    }
}
