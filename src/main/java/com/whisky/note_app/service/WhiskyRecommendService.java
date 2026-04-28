package com.whisky.note_app.service;

import com.whisky.note_app.config.CacheConfig;
import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhiskyRecommendService {

    private final MasterWhiskyRepository masterWhiskyRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * 사용자의 취향 상위 키워드를 기준으로 위스키를 추천합니다.
     * 캐시 키는 userId + maxPrice 조합으로 구성되며, AI 분석 완료 시 {@link WhiskyAnalysisService}에서 무효화합니다.
     */
    @Cacheable(value = CacheConfig.RECOMMENDATIONS_CACHE, key = "#user.id + '_' + #maxPrice")
    public List<MasterWhisky> getPersonalizedRecommendations(Integer maxPrice, User user) {
        List<UserPreference> preferences = userPreferenceRepository.findTop5ByUserOrderByScoreDesc(user);

        List<MasterWhisky> allWhiskies = masterWhiskyRepository.findAll();

        return allWhiskies.stream()
                .filter(w -> {
                    if (maxPrice == null) return true;
                    if (w.getPrice() == null) return false;
                    return w.getPrice() <= maxPrice;
                })
                .sorted(Comparator.comparingDouble((MasterWhisky w) -> calculateMatchScore(w, preferences)).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private double calculateMatchScore(MasterWhisky whisky, List<UserPreference> preferences) {
        double score = 0.0;
        String features = (whisky.getNose() + " " + whisky.getPalate() + " " + whisky.getFinish()).toLowerCase();

        for (UserPreference pref : preferences) {
            if (features.contains(pref.getKeyword().toLowerCase())) {
                score += pref.getScore();
            }
        }
        return score;
    }
}
