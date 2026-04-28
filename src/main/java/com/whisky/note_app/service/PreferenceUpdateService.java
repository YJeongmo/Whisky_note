package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 낙관적 락 충돌 시 재시도 로직을 담당합니다.
 *
 * self-injection 사용 이유: @Transactional은 AOP 프록시를 통해서만 동작하므로,
 * 같은 클래스 내부 호출로는 트랜잭션이 적용되지 않습니다.
 * self 참조를 통해 프록시를 경유하여 doSingleAttempt()마다 독립적인 트랜잭션을 엽니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceUpdateService {

    private final UserPreferenceRepository preferenceRepository;

    // @Lazy: 순환 참조 방지를 위해 첫 호출 시점에 주입
    @Autowired
    @Lazy
    private PreferenceUpdateService self;

    private static final int MAX_RETRY = 10;

    public void updateWithRetry(String keyword, int delta, User user) {
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                self.doSingleAttempt(keyword, delta, user);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("낙관적 락 충돌 감지 - keyword: {}, 재시도 {}/{}", keyword, attempt, MAX_RETRY);
                if (attempt >= MAX_RETRY) {
                    throw new RuntimeException(
                            "선호도 업데이트 실패: 동시 요청이 많습니다. 잠시 후 다시 시도해주세요.");
                }
                try {
                    Thread.sleep((long) (Math.random() * 50L * attempt));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // 매 호출마다 새 트랜잭션 — 이전 충돌로 오염된 영속성 컨텍스트를 재사용하지 않기 위함
    @Transactional
    public void doSingleAttempt(String keyword, int delta, User user) {
        UserPreference pref = preferenceRepository.findByUserAndKeyword(user, keyword)
                .orElseGet(() -> new UserPreference(user, keyword, 0));
        pref.updateScore(delta);
        preferenceRepository.saveAndFlush(pref);
    }
}
