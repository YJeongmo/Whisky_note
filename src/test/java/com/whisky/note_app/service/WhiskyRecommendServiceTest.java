package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@ActiveProfiles("test")
@Transactional 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class WhiskyRecommendServiceTest {

    @Autowired
    private WhiskyRecommendService recommendService;

    @Autowired
    private MasterWhiskyRepository masterWhiskyRepository;

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @BeforeEach
    void setUp() {
        // 1. 테스트용 마스터 위스키 데이터 삽입
        createMasterWhisky("피트", "아일라", "강렬한 피트 향과 소독약 맛, 긴 피니시", 150000);
        createMasterWhisky("바닐라", "스페이사이드", "부드러운 바닐라와 꿀, 과일향", 80000);
        createMasterWhisky("오크", "버번", "강한 오크향과 스파이시한 맛", 120000);
    }

    private void createMasterWhisky(String name, String cat, String desc, Integer price) {
        MasterWhisky whisky = new MasterWhisky();
        whisky.setWhiskyName(name);
        whisky.setCategory(cat);
        // 테스트 편의상 Nose/Palate/Finish에 동일한 설명을 넣어 검색
        whisky.setNose(desc);
        whisky.setPalate(desc);
        whisky.setFinish(desc);
        whisky.setPrice(price);
        masterWhiskyRepository.save(whisky);
    }

    @Test
    @DisplayName("피트 점수가 높으면 피트 위스키가 1위로 추천되어야 한다.")
    void recommendPeatLover() {
        // given: 사용자가 '피트'를 매우 좋아함 (+10점)
        savePreference("피트", 10.0);
        savePreference("바닐라", 2.0);

        // when: 추천 실행
        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(null);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getWhiskyName()).isEqualTo("피트");
    }

    @Test
    @DisplayName("가격 필터링이 정상적으로 동작해야 한다.")
    void recommendWithPriceFilter() {
        // given: 모든 키워드를 좋아하지만 예산이 적음
        savePreference("피트", 5.0);
        savePreference("바닐라", 5.0);
        savePreference("오크", 5.0);

        // when: 10만원 이하로 필터링
        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(100000);

        // then: 8만원인 '달콤 바닐라'만 나와야 함
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getWhiskyName()).isEqualTo("바닐라");
    }

    @Test
    @DisplayName("싫어하는 키워드(마이너스 점수)가 있으면 순위가 밀려야 한다.")
    void recommendWithDislike() {
        // given: 피트는 좋아하지만(+10), 스파이시는 싫어함(-20)
        savePreference("피트", 10.0);
        savePreference("스파이시", -20.0);

        // when
        List<MasterWhisky> results = recommendService.getPersonalizedRecommendations(null);

        // then: 피트 대장이 1위여야 하고, 스파이시가 포함된 위스키는 점수가 낮아야 함
        assertThat(results.get(0).getWhiskyName()).isEqualTo("피트");
        // 오크 스파이스는 점수가 마이너스라 리스트 하단에 있거나 점수가 낮음
        boolean isOakLast = results.get(results.size()-1).getWhiskyName().equals("오크");
        assertThat(isOakLast).isTrue();
    }

    private void savePreference(String keyword, Double score) {
        UserPreference pref = new UserPreference(keyword, score.intValue());
        preferenceRepository.save(pref);
    }
}