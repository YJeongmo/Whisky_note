package com.whisky.note_app.controller;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.service.WhiskyRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommend", description = "위스키 추천 API — JWT 인증 필요")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final WhiskyRecommendService recommendService;

    @Operation(summary = "개인화 위스키 추천", description = "사용자의 취향 선호도를 기반으로 위스키를 추천합니다. maxPrice 입력 시 해당 금액 이하만 추천됩니다.")
    @GetMapping
    public List<MasterWhisky> getRecommendations(
            @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        return recommendService.getPersonalizedRecommendations(maxPrice, user);
    }
}