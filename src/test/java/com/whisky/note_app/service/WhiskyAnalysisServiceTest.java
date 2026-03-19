package com.whisky.note_app.service;

import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.UserPreferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WhiskyAnalysisServiceTest {

    @Autowired
    private WhiskyAnalysisService analysisService;

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Test
    @DisplayName("시음 노트 분석 후 긍정 키워드의 점수가 DB에 저장되어야 한다.")
    void analyzeAndSavePreferenceTest() {
        // given: 피트향을 좋아하고 바닐라는 싫어하는 시음 내용
        String combinedContent = "향: 강렬한 피트와 연기, 맛: 부드러운 오크, 피니시: 너무 느끼한 바닐라 별로임";
        Double rating = 4.5; // 높은 평점

        // when: 분석 실행
        analysisService.analyzeAndSavePreference(combinedContent, rating);

        // then: DB에 '피트'나 '오크' 관련 키워드가 생성되고 점수가 올라갔는지 확인
        List<UserPreference> allPreferences = preferenceRepository.findAll();

        // 결과가 하나라도 있어야 함 (LLM 응답에 따라 키워드는 달라질 수 있음)
        assertThat(allPreferences).isNotEmpty();

        // 특정 키워드(예: 피트)가 포함되어 있는지 확인 (LLM 특성상 단어가 바뀔 수 있어 포함 여부로 확인)
        boolean hasPeat = allPreferences.stream()
                .anyMatch(p -> p.getKeyword().contains("피트") && p.getScore() > 0);

        System.out.println("=== 분석된 키워드 결과 ===");
        allPreferences.forEach(p -> System.out.println(p.getKeyword() + ": " + p.getScore()));

        assertThat(hasPeat).isTrue();
    }

    @Test
    @DisplayName("낮은 평점의 시음기는 비선호(dislike)로 분류되어 점수가 깎여야 한다.")
    void analyzeDislikeTest() {
        // given: 아주 낮은 평점과 피트에 대한 부정적인 내용
        String combinedContent = "향: 소독약 같은 지독한 피트, 맛: 쓰고 맛없음, 피니시: 최악임";
        Double rating = 1.0;

        // when
        analysisService.analyzeAndSavePreference(combinedContent, rating);

        // then: '피트' 점수가 마이너스가 되었는지 확인
        Optional<UserPreference> peatPref = preferenceRepository.findByKeyword("피트");

        // '피트'를 싫어하는 것으로 분류했다면 점수는 음수
        peatPref.ifPresent(p -> {
            System.out.println("부정 리뷰 분석 점수: " + p.getScore());
            assertThat(p.getScore()).isLessThan(0);
        });
    }
}
