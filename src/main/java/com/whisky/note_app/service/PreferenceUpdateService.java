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
 * [PreferenceUpdateService — Step 9 개선]
 *
 * [왜 self-injection이 필요한가?]
 * @Transactional은 Spring AOP 프록시를 통해 동작합니다.
 * 같은 클래스 내에서 this.doSingleAttempt()를 호출하면
 * 프록시를 거치지 않아 @Transactional이 적용되지 않습니다.
 * self를 @Autowired로 주입받으면 프록시를 통해 호출되어 @Transactional이 정상 동작합니다.
 *
 * [왜 @Transactional을 분리했나?]
 * OptimisticLockingFailureException 발생 시 현재 트랜잭션의
 * JPA 영속성 컨텍스트(Persistence Context)가 오염됩니다.
 * 같은 트랜잭션 안에서 재시도해도 오염된 컨텍스트를 재사용하므로 계속 실패합니다.
 * doSingleAttempt()가 매번 새 트랜잭션을 열어 새 영속성 컨텍스트로 재시도합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceUpdateService {

    private final UserPreferenceRepository preferenceRepository;

    // @Lazy: 앱 시작 시점이 아닌 첫 호출 시점에 주입 → 순환 참조 해결
    @Autowired
    @Lazy
    private PreferenceUpdateService self;

    private static final int MAX_RETRY = 10;

    // @Transactional 없음: 재시도 루프만 담당, 트랜잭션은 doSingleAttempt에서 각각 열림
    public void updateWithRetry(String keyword, int delta, User user) {
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                self.doSingleAttempt(keyword, delta, user); // 프록시를 통해 새 트랜잭션으로 호출
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("낙관적 락 충돌 감지 - keyword: {}, 재시도 {}/{}", keyword, attempt, MAX_RETRY);
                if (attempt >= MAX_RETRY) {
                    throw new RuntimeException(
                            "선호도 업데이트 실패: 동시 요청이 많습니다. 잠시 후 다시 시도해주세요.");
                }
                try {
                    // 랜덤 딜레이: 스레드들이 동시에 재시도하지 않도록 분산
                    Thread.sleep((long) (Math.random() * 50L * attempt));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // 매 호출마다 새 트랜잭션 → 새 영속성 컨텍스트 → 충돌 없이 최신 상태로 재시도
    @Transactional
    public void doSingleAttempt(String keyword, int delta, User user) {
        UserPreference pref = preferenceRepository.findByUserAndKeyword(user, keyword)
                .orElseGet(() -> new UserPreference(user, keyword, 0));
        pref.updateScore(delta);
        preferenceRepository.saveAndFlush(pref);
    }
}
