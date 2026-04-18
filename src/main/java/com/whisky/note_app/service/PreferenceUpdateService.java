package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [PreferenceUpdateService — Step 9]
 *
 * [단일 책임 원칙(SRP)]
 * WhiskyAnalysisService는 AI 분석에만 집중하고,
 * UserPreference 저장 + 낙관적 락 재시도는 이 서비스가 전담합니다.
 *
 * [재시도 로직이 필요한 이유]
 * @Version 낙관적 락 충돌 시 OptimisticLockException이 발생합니다.
 * 이를 그대로 사용자에게 500 에러로 돌려주면 안 되므로,
 * 최대 MAX_RETRY 횟수만큼 재시도한 후 최종 실패 시 예외를 던집니다.
 *
 * [왜 saveAndFlush인가?]
 * save()는 트랜잭션 커밋 시점에 flush됩니다.
 * 재시도 루프 안에서는 즉시 DB에 반영해야 버전 충돌을 바로 감지할 수 있으므로
 * saveAndFlush()를 사용합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceUpdateService {

    private final UserPreferenceRepository preferenceRepository;
    private static final int MAX_RETRY = 3;

    @Transactional
    public void updateWithRetry(String keyword, int delta, User user) {
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                UserPreference pref = preferenceRepository.findByUserAndKeyword(user, keyword)
                        .orElseGet(() -> new UserPreference(user, keyword, 0));
                pref.updateScore(delta);
                preferenceRepository.saveAndFlush(pref);
                return; // 성공 시 즉시 반환
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("낙관적 락 충돌 감지 - keyword: {}, 재시도 {}/{}", keyword, attempt, MAX_RETRY);
                if (attempt >= MAX_RETRY) {
                    throw new RuntimeException(
                            "선호도 업데이트 실패: 동시 요청이 많습니다. 잠시 후 다시 시도해주세요.");
                }
            }
        }
    }
}
