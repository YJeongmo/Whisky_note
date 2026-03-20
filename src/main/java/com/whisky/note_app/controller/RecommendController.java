package com.whisky.note_app.controller;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.service.WhiskyRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final WhiskyRecommendService recommendService;

    @GetMapping
    public List<MasterWhisky> getRecommendations(
            @RequestParam(name = "maxPrice", required = false) Integer maxPrice) {
        return recommendService.getPersonalizedRecommendations(maxPrice);
    }
}