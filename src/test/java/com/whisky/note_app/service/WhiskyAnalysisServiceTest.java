package com.whisky.note_app.service;

import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.UserPreferenceRepository;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.whisky.note_app.config.TestAsyncConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [WhiskyAnalysisServiceTest]
 *
 * [@Transactional 미사용 이유]
 * analyzeAndSavePreference() 내부에서 CompletableFuture.runAsync()로
 * ForkJoinPool 워커 스레드가 생성됩니다.
 * 테스트에 @Transactional이 있으면 testUser가 미커밋 상태여서
 * 워커 스레드의 새 트랜잭션이 user_id를 찾지 못해 FK 위반이 발생합니다.
 * → @Transactional 제거 후 @AfterEach에서 직접 정리합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class WhiskyAnalysisServiceTest {

    @Autowired
    private WhiskyAnalysisService analysisService;

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 이전 테스트 잔여 데이터 정리
        userRepository.findByEmail("analyst@test.com").ifPresent(u -> {
            preferenceRepository.deleteAll(preferenceRepository.findAllByUser(u));
            userRepository.delete(u);
        });

        // testUser 커밋 완료 후 저장 (ForkJoinPool 워커가 조회 가능)
        testUser = userRepository.save(User.builder()
                .email("analyst@test.com")
                .password("encodedPw")
                .nickname("분석가")
                .role(UserRole.USER)
                .build());
    }

    @AfterEach
    void tearDown() {
        preferenceRepository.deleteAll(preferenceRepository.findAllByUser(testUser));
        userRepository.delete(testUser);
    }

    @Test
    @DisplayName("시음 노트 분석 후 긍정 키워드의 점수가 DB에 저장되어야 한다")
    void analyzeAndSavePreferenceTest() {
        String combinedContent = "향: 강렬한 피트와 연기, 맛: 부드러운 오크, 피니시: 너무 느끼한 바닐라 별로임";
        Double rating = 4.5;

        analysisService.analyzeAndSavePreference(combinedContent, rating, testUser.getId());

        List<UserPreference> allPreferences = preferenceRepository.findTop5ByUserOrderByScoreDesc(testUser);

        assertThat(allPreferences).isNotEmpty();

        boolean hasPeat = allPreferences.stream()
                .anyMatch(p -> p.getKeyword().contains("피트") && p.getScore() > 0);

        System.out.println("=== 분석된 키워드 결과 ===");
        allPreferences.forEach(p -> System.out.println(p.getKeyword() + ": " + p.getScore()));

        assertThat(hasPeat).isTrue();
    }

    @Test
    @DisplayName("낮은 평점의 시음기는 비선호로 분류되어 점수가 깎여야 한다")
    void analyzeDislikeTest() {
        String combinedContent = "향: 소독약 같은 지독한 피트, 맛: 쓰고 맛없음, 피니시: 최악임";
        Double rating = 1.0;

        analysisService.analyzeAndSavePreference(combinedContent, rating, testUser.getId());

        Optional<UserPreference> peatPref = preferenceRepository.findByUserAndKeyword(testUser, "피트");

        peatPref.ifPresent(p -> {
            System.out.println("부정 리뷰 분석 점수: " + p.getScore());
            assertThat(p.getScore()).isLessThan(0);
        });
    }
}
