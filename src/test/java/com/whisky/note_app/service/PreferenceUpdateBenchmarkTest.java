package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.UserPreferenceRepository;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * [PreferenceUpdateBenchmarkTest]
 *
 * [측정 목적]
 * AI 분석 후 추출된 키워드를 UserPreference에 저장할 때
 * 순차 처리 vs 병렬 처리의 응답 시간을 비교합니다.
 *
 * [측정 환경]
 * - 실제 PostgreSQL (H2 인메모리는 I/O 비용이 없어 의미 있는 수치 불가)
 * - @ActiveProfiles("test") 미사용
 *
 * [20개 키워드를 선택한 이유]
 * Ollama AI가 한 번 분석 시 like 5~10개 + dislike 5~10개 = 평균 20개 추출
 * → 실제 분석 요청 1건과 동일한 조건
 *
 * [5회 반복 평균을 사용하는 이유]
 * JVM 웜업, 커넥션 풀 초기화, GC 등의 영향으로
 * 단일 측정은 편차가 클 수 있어 5회 평균으로 안정적인 수치를 확보합니다.
 */
@SpringBootTest
class PreferenceUpdateBenchmarkTest {

    @Autowired private PreferenceUpdateService preferenceUpdateService;
    @Autowired private UserPreferenceRepository preferenceRepository;
    @Autowired private UserRepository userRepository;

    private static final String BENCHMARK_EMAIL = "benchmark@test.com";

    private static final int KEYWORD_COUNT = 20;
    private static final int REPEAT = 5; // 5회 반복 후 평균

    private static final List<String> KEYWORDS = List.of(
            "피트", "오크", "바닐라", "꿀", "과일",
            "스모키", "달콤", "스파이시", "시트러스", "초콜릿",
            "너트", "캐러멜", "플로럴", "허브", "민트",
            "가죽", "흙", "소금", "연기", "건포도"
    );

    private User testUser;

    @BeforeEach
    void setUp() {
        // 이전 테스트 데이터만 정리 (실제 앱 데이터 보호)
        userRepository.findByEmail(BENCHMARK_EMAIL).ifPresent(u -> {
            preferenceRepository.deleteAll(preferenceRepository.findAllByUser(u));
            userRepository.delete(u);
        });

        testUser = userRepository.save(User.builder()
                .email(BENCHMARK_EMAIL)
                .password("pw")
                .nickname("벤치유저")
                .role(UserRole.USER)
                .build());
    }

    @AfterEach
    void tearDown() {
        // 테스트 유저의 데이터만 정리
        preferenceRepository.deleteAll(preferenceRepository.findAllByUser(testUser));
        userRepository.delete(testUser);
    }

    @Test
    @DisplayName("[순차 처리] 키워드 20개 순차 저장 응답 시간 측정 (5회 평균) ← baseline")
    void benchmark_sequential() {
        long totalElapsed = 0;

        for (int i = 0; i < REPEAT; i++) {
            preferenceRepository.deleteAll(); // 매 회차 초기화

            long start = System.currentTimeMillis();

            for (String keyword : KEYWORDS) {
                preferenceUpdateService.updateWithRetry(keyword, 1, testUser);
            }

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            System.out.println("  " + (i + 1) + "회차: " + elapsed + "ms");
        }

        long average = totalElapsed / REPEAT;

        System.out.println("\n========================================");
        System.out.println("        순차 처리 벤치마크 결과");
        System.out.println("========================================");
        System.out.println("키워드 수     : " + KEYWORD_COUNT + "개");
        System.out.println("반복 횟수     : " + REPEAT + "회");
        System.out.println("평균 소요시간 : " + average + "ms  ← baseline");
        System.out.println("키워드당 평균 : " + (average / KEYWORD_COUNT) + "ms");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("[병렬 처리] 키워드 20개 병렬 저장 응답 시간 측정 (5회 평균)")
    void benchmark_parallel() {
        long totalElapsed = 0;

        for (int i = 0; i < REPEAT; i++) {
            preferenceRepository.deleteAll();

            long start = System.currentTimeMillis();

            // 키워드를 동시에 저장
            CompletableFuture<?>[] futures = KEYWORDS.stream()
                    .map(kw -> CompletableFuture.runAsync(
                            () -> preferenceUpdateService.updateWithRetry(kw, 1, testUser)))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();

            long elapsed = System.currentTimeMillis() - start;
            totalElapsed += elapsed;
            System.out.println("  " + (i + 1) + "회차: " + elapsed + "ms");
        }

        long average = totalElapsed / REPEAT;

        System.out.println("\n========================================");
        System.out.println("        병렬 처리 벤치마크 결과");
        System.out.println("========================================");
        System.out.println("키워드 수     : " + KEYWORD_COUNT + "개");
        System.out.println("반복 횟수     : " + REPEAT + "회");
        System.out.println("평균 소요시간 : " + average + "ms");
        System.out.println("키워드당 평균 : " + (average / KEYWORD_COUNT) + "ms");
        System.out.println("========================================\n");
    }
}
