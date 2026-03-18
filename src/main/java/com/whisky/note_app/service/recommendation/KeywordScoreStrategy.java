package com.whisky.note_app.service.recommendation;

import com.whisky.note_app.entity.MasterWhisky;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeywordScoreStrategy implements RecommendationStrategy {

    @Override
    public List<MasterWhisky> recommend(List<MasterWhisky> allWhiskies, String input) {
        if (input == null || input.isBlank()) return List.of();

        // 1. 입력 키워드 분리 (쉼표, 공백 기준)
        List<String> keywords = Arrays.stream(input.split("[,\\s]+"))
                .filter(k -> !k.isEmpty())
                .map(String::toLowerCase)
                .toList();

        // 2. 점수 계산 및 정렬
        return allWhiskies.stream()
                .map(whisky -> new WhiskyScorePair(whisky, calculateScore(whisky, keywords)))
                .filter(pair -> pair.score > 0) // 연관성 없는(0점) 위스키 제외
                .sorted(Comparator.comparingInt(WhiskyScorePair::getScore).reversed()) // 높은 점수순
                .map(WhiskyScorePair::getWhisky)
                .collect(Collectors.toList());
    }

    private int calculateScore(MasterWhisky whisky, List<String> keywords) {
        int score = 0;
        // 검색 대상 텍스트 결합 (이름, 카테고리, N, P, F)
        String targetText = (whisky.getWhiskyName() + " " + whisky.getCategory() + " " +
                whisky.getNose() + " " + whisky.getPalate() + " " +
                whisky.getFinish()).toLowerCase();

        for (String kw : keywords) {
            if (targetText.contains(kw)) {
                score++;
            }
        }
        return score;
    }

    // 내부 도우미 클래스
    private static class WhiskyScorePair {
        private final MasterWhisky whisky;
        private final int score;
        public WhiskyScorePair(MasterWhisky whisky, int score) { this.whisky = whisky; this.score = score; }
        public MasterWhisky getWhisky() { return whisky; }
        public int getScore() { return score; }
    }
}