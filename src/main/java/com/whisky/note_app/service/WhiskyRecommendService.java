package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.User;
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

    public List<MasterWhisky> getPersonalizedRecommendations(Integer maxPrice, User user) {
        // 현재 로그인한 사용자의 선호도 데이터만 가져옴
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
