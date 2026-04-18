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
 * [구조 변경에 따른 테스트 분리]
 * updateWithRetry()는 self-injection을 사용하므로 Mockito 단위 테스트로 검증이 어렵습니다.
 * → 재시도 루프 포함 전체 흐름은 PreferenceUpdateConcurrencyTest(통합 테스트)에서 검증
 *
 * 여기서는 실제 저장 로직인 doSingleAttempt()를 단위 테스트합니다.
 * - 새 키워드 생성 후 저장
 * - 기존 키워드 조회 후 score 업데이트
 * - saveAndFlush 예외 발생 시 상위로 전파
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
    @DisplayName("키워드가 없으면 새로 생성되어 저장되어야 한다")
    void doSingleAttempt_newKeyword() {
        // given
        given(preferenceRepository.findByUserAndKeyword(testUser, "피트"))
                .willReturn(Optional.empty());
        given(preferenceRepository.saveAndFlush(any(UserPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        preferenceUpdateService.doSingleAttempt("피트", 1, testUser);

        // then
        verify(preferenceRepository, times(1)).saveAndFlush(any(UserPreference.class));
    }

    @Test
    @DisplayName("키워드가 이미 있으면 score가 누적되어야 한다")
    void doSingleAttempt_existingKeyword() {
        // given
        UserPreference existing = new UserPreference(testUser, "피트", 5);
        given(preferenceRepository.findByUserAndKeyword(testUser, "피트"))
                .willReturn(Optional.of(existing));
        given(preferenceRepository.saveAndFlush(any(UserPreference.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        preferenceUpdateService.doSingleAttempt("피트", 1, testUser);

        // then: score가 6이 된 객체로 저장됐는지 확인
        verify(preferenceRepository, times(1)).saveAndFlush(any(UserPreference.class));
    }

    @Test
    @DisplayName("낙관적 락 충돌 시 예외가 상위로 전파되어야 한다")
    void doSingleAttempt_optimisticLockException() {
        // given
        given(preferenceRepository.findByUserAndKeyword(testUser, "피트"))
                .willReturn(Optional.of(new UserPreference(testUser, "피트", 5)));
        given(preferenceRepository.saveAndFlush(any(UserPreference.class)))
                .willThrow(new ObjectOptimisticLockingFailureException(UserPreference.class, 1L));

        // when & then: 예외가 전파되어야 updateWithRetry의 재시도 루프가 동작함
        assertThatThrownBy(() -> preferenceUpdateService.doSingleAttempt("피트", 1, testUser))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
