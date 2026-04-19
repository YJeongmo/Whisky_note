package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.UserPreferenceRepository;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [PreferenceUpdateConcurrencyTest — Step 10]
 *
 * [테스트 케이스 구분]
 * Case 1. 레코드 존재 → UPDATE 동시 요청 → 낙관적 락 충돌 → 재시도로 모두 성공
 * Case 2. 레코드 없음 → INSERT 동시 요청 → 유니크 제약 충돌 → 1개만 INSERT 성공, 나머지 UPDATE 성공 or 실패
 *
 * Case 2는 오류가 아니라 의도된 동작입니다.
 * 같은 유저가 같은 키워드로 동시에 INSERT하는 상황은 현실에서 거의 발생하지 않으며,
 * 낙관적 락은 INSERT가 아닌 UPDATE 충돌을 방어하는 도구입니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PreferenceUpdateConcurrencyTest {

    @Autowired private PreferenceUpdateService preferenceUpdateService;
    @Autowired private UserPreferenceRepository preferenceRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        preferenceRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .email("concurrency@test.com")
                .password("pw")
                .nickname("동시성유저")
                .role(UserRole.USER)
                .build());
    }

    @Test
    @DisplayName("Case 1: 레코드가 이미 존재할 때 — 10개 동시 UPDATE 요청이 모두 성공해야 한다")
    void concurrentUpdate_existingRecord() throws InterruptedException {
        // given: "피트" 레코드 미리 생성 → 모든 스레드가 UPDATE를 시도
        preferenceRepository.save(new UserPreference(testUser, "피트", 0));

        int threadCount = 10;
        int failCount = runConcurrent(threadCount, "피트");

        UserPreference result = preferenceRepository
                .findByUserAndKeyword(testUser, "피트")
                .orElseThrow();

        System.out.println("=== Case 1: UPDATE 동시 요청 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("성공: " + (threadCount - failCount));
        System.out.println("실패(재시도 초과): " + failCount);
        System.out.println("최종 score: " + result.getScore());

        // 낙관적 락 + 재시도로 10개 모두 성공 → score = 10
        assertThat(failCount).isEqualTo(0);
        assertThat(result.getScore()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("Case 2: 레코드가 없을 때 — 빠른 스레드는 INSERT 충돌로 실패, 느린 스레드는 UPDATE로 성공할 수 있다")
    void concurrentUpdate_noExistingRecord() throws InterruptedException {
        // given: 레코드 없음 → 스레드 타이밍에 따라 INSERT 또는 UPDATE 시도
        int threadCount = 10;
        int failCount = runConcurrent(threadCount, "바닐라");

        UserPreference result = preferenceRepository
                .findByUserAndKeyword(testUser, "바닐라")
                .orElseThrow();

        int successCount = threadCount - failCount;

        System.out.println("=== Case 2: INSERT/UPDATE 혼재 동시 요청 결과 ===");
        System.out.println("총 요청: " + threadCount);
        System.out.println("성공(INSERT 1개 + UPDATE " + (successCount - 1) + "개): " + successCount);
        System.out.println("실패(INSERT 유니크 충돌): " + failCount);
        System.out.println("최종 score: " + result.getScore());
        System.out.println("※ 타이밍에 따라 성공/실패 비율이 달라질 수 있음 (non-deterministic)");

        // 성공한 수만큼 정확히 score가 누적되었는지만 검증
        assertThat(result.getScore()).isEqualTo(successCount);
    }

    // 공통 동시성 실행 로직 → failCount 반환
    private int runConcurrent(int threadCount, String keyword) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    preferenceUpdateService.updateWithRetry(keyword, 1, testUser);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        int failCount = 0;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                failCount++;
            }
        }
        return failCount;
    }
}
