package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import com.whisky.note_app.service.recommendation.RecommendationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * [MasterWhiskySearchService — Step 19 변경]
 *
 * [기존 방식의 문제]
 * if-else 체인으로 조건 하나씩만 처리:
 *   if (name != null) → 이름 검색만
 *   else if (category != null) → 카테고리 검색만
 *   → "스카치 + 피트 향 + 10만원 이하" 복합 조건 불가
 *
 * [QueryDSL 적용 후]
 * 전달된 조건 중 null이 아닌 것만 WHERE 절에 AND로 조합:
 *   name + category + maxPrice + flavorKeyword 동시 적용 가능
 *   조건이 없으면 전체 조회
 */
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
