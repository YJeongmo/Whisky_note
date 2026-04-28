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

        List<String> keywords = Arrays.stream(input.split("[,\\s]+"))
                .filter(k -> !k.isEmpty())
                .map(String::toLowerCase)
                .toList();

        return allWhiskies.stream()
                .map(whisky -> new WhiskyScorePair(whisky, calculateScore(whisky, keywords)))
                .filter(pair -> pair.score > 0)
                .sorted(Comparator.comparingInt(WhiskyScorePair::getScore).reversed())
                .map(WhiskyScorePair::getWhisky)
                .collect(Collectors.toList());
    }

    private int calculateScore(MasterWhisky whisky, List<String> keywords) {
        int score = 0;
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

    private static class WhiskyScorePair {
        private final MasterWhisky whisky;
        private final int score;
        public WhiskyScorePair(MasterWhisky whisky, int score) { this.whisky = whisky; this.score = score; }
        public MasterWhisky getWhisky() { return whisky; }
        public int getScore() { return score; }
    }
}
