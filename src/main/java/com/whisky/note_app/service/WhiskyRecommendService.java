package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhiskyRecommendService {

    private final MasterWhiskyRepository masterWhiskyRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public List<MasterWhisky> getPersonalizedRecommendations(Integer maxPrice) {
        // 1. 사용자의 모든 선호도 데이터 가져오기
        List<UserPreference> preferences = userPreferenceRepository.findAll();

        // 2. 모든 위스키 가져오기 (추후 가격 필터링 적용 가능)
        List<MasterWhisky> allWhiskies = masterWhiskyRepository.findAll();

        return allWhiskies.stream()
                // 가격 필터링 (파라미터가 있을 경우)
                .filter(w -> {
                    if (maxPrice == null) return true; // 필터링 조건이 없으면 통과
                    if (w.getPrice() == null) return false; // 필터링 조건은 있는데 위스키 가격이 없으면 제외
                    return w.getPrice() <= maxPrice;
                })
                .sorted(Comparator.comparingDouble((MasterWhisky w) -> calculateMatchScore(w, preferences)).reversed())
                .limit(5) // 상위 5개 추천
                .collect(Collectors.toList());
    }

    private double calculateMatchScore(MasterWhisky whisky, List<UserPreference> preferences) {
        double score = 0.0;

        // 위스키의 모든 특징 텍스트를 하나로 합침
        String features = (whisky.getNose() + " " + whisky.getPalate() + " " + whisky.getFinish()).toLowerCase();

        for (UserPreference pref : preferences) {
            String keyword = pref.getKeyword().toLowerCase();
            if (features.contains(keyword)) {
                // 키워드가 포함되어 있으면 해당 키워드의 선호도 점수를 더함 (음수면 감점)
                score += pref.getScore();
            }
        }
        return score;
    }
}