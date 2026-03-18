package com.whisky.note_app.service;

import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.repository.MasterWhiskyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterWhiskyService {
    private final MasterWhiskyRepository masterRepository;

    public List<MasterWhisky> searchWhiskies(String name, String distillery, String category, String subCategory, String priceRange) {
        // 우선은 이름 검색을 최우선으로 하고, 나머지는 단순 필터링으로 구현
        // 나중에 조건이 많아지면 QueryDSL로 업그레이드.
        if (name != null && !name.isEmpty()) return masterRepository.findByWhiskyNameContaining(name);
        if (distillery != null && !distillery.isEmpty()) return masterRepository.findByDistilleryContaining(distillery);
        if (category != null && !category.isEmpty()) return masterRepository.findByCategory(category);
        if (subCategory != null && !subCategory.isEmpty()) return masterRepository.findBySubCategoryContaining(subCategory);
        if (priceRange != null && !priceRange.isEmpty()) return masterRepository.findByPriceRange(priceRange);

        return masterRepository.findAll();
    }
}
