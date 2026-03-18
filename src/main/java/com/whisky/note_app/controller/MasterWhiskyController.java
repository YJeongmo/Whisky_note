package com.whisky.note_app.controller;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.service.MasterWhiskyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterWhiskyController {
    private final MasterWhiskyService masterService;

    @GetMapping("/search")
    public List<MasterWhisky> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String distillery,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String priceRange) {

        return masterService.searchWhiskies(name, distillery, category, subCategory, priceRange);
    }

    // 전체 목록 조회
    @GetMapping
    public List<MasterWhisky> getAll() {
        return masterService.searchWhiskies(null, null, null, null, null);
    }
}
