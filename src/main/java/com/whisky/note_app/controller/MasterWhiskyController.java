package com.whisky.note_app.controller;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.service.MasterWhiskySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [MasterWhiskyController — Step 19 변경]
 *
 * maxPrice, flavorKeyword 파라미터 추가
 * 모든 조건을 동시에 적용하는 복합 검색 지원
 *
 * 예시:
 *   GET /api/master/search?category=스카치&maxPrice=100000&flavorKeyword=피트
 *   → 스카치 위스키 중 10만원 이하 + 피트 향 포함
 */
@Tag(name = "Master Whisky", description = "위스키 마스터 데이터 검색 API")
@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterWhiskyController {

    private final MasterWhiskySearchService masterService;

    @Operation(summary = "위스키 복합 검색",
            description = "name, distillery, category, subCategory, priceRange, maxPrice, flavorKeyword 조건을 동시에 적용할 수 있습니다.")
    @GetMapping("/search")
    public List<MasterWhisky> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String distillery,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String flavorKeyword) {

        return masterService.searchWhiskies(
                name, distillery, category, subCategory, priceRange, maxPrice, flavorKeyword);
    }

    @Operation(summary = "위스키 전체 목록 조회")
    @GetMapping
    public List<MasterWhisky> getAll() {
        return masterService.searchWhiskies(null, null, null, null, null, null, null);
    }

    @Operation(summary = "위스키 추천", description = "키워드 기반 추천 알고리즘")
    @GetMapping("/recommend")
    public List<MasterWhisky> recommend(@RequestParam String keywords) {
        return masterService.getRecommendations(keywords);
    }
}
