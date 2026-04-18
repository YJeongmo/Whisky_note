package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.entity.UserRole;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.UserPreferenceRepository;
import com.whisky.note_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class WhiskyRecommendServiceTest {

    @Autowired private WhiskyRecommendService recommendService;
    @Autowired private MasterWhiskyRepository masterWhiskyRepository;
    @Autowired private UserPreferenceRepository preferenceRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("recommender@test.com")
                .password("encodedPw")
                .nickname("추천유저")
                .role(UserRole.USER)
                .build());

        createMasterWhisky("피트", "아일라", "강렬한 피트 향과 소독약 맛, 긴 피니시", 150000);
        createMasterWhisky("바닐라", "스페이사이드", "부드러운 바닐라와 꿀, 과일향", 80000);
        createMasterWhisky("오크", "버번", "강한 오크향과 스파이시한 맛", 120000);
    }

    private void createMasterWhisky(String name, String cat, String desc, Integer price) {
        masterWhiskyRepository.save(MasterWhisky.builder()
                .whiskyName(name)
                .category(cat)
                .nose(desc).palate(desc).finish(desc)
                .price(price)
                .build());
    }

    @Test
    @DisplayName("피트 점수가 높으면 피트 위스키가 1위로 추천되어야 한다")
    void recommendPeatLover() {
        savePreference("피트", 10);
        savePreference("바닐라", 2);

        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(null, testUser);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getWhiskyName()).isEqualTo("피트");
    }

    @Test
    @DisplayName("가격 필터링이 정상적으로 동작해야 한다")
    void recommendWithPriceFilter() {
        savePreference("피트", 5);
        savePreference("바닐라", 5);
        savePreference("오크", 5);

        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(100000, testUser);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWhiskyName()).isEqualTo("바닐라");
    }

    @Test
    @DisplayName("싫어하는 키워드가 있으면 해당 위스키 순위가 밀려야 한다")
    void recommendWithDislike() {
        savePreference("피트", 10);
        savePreference("스파이시", -20);

        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(null, testUser);

        assertThat(results.get(0).getWhiskyName()).isEqualTo("피트");
        boolean isOakLast = results.get(results.size() - 1).getWhiskyName().equals("오크");
        assertThat(isOakLast).isTrue();
    }

    private void savePreference(String keyword, int score) {
        preferenceRepository.save(new UserPreference(testUser, keyword, score));
    }
}
