package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.service.recommendation.RecommendationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterWhiskySearchService {

    private final MasterWhiskyRepository masterRepository;
    private final RecommendationStrategy recommendationStrategy;

    public List<MasterWhisky> searchWhiskies(
            String name,
            String distillery,
            String category,
            String subCategory,
            String priceRange,
            Integer maxPrice,
            String flavorKeyword) {

        return masterRepository.searchByConditions(
                name, distillery, category, subCategory, priceRange, maxPrice, flavorKeyword);
    }

    public List<MasterWhisky> getRecommendations(String keywords) {
        List<MasterWhisky> allWhiskies = masterRepository.findAll();
        return recommendationStrategy.recommend(allWhiskies, keywords);
    }
}
