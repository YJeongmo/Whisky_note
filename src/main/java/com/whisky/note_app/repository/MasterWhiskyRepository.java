package com.whisky.note_app.repository;

import com.whisky.note_app.entity.MasterWhisky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterWhiskyRepository extends JpaRepository<MasterWhisky, Long> {

    boolean existsByWhiskyName(String whiskyName); // 기존 데이터 확인

    // 이름 포함 검색 (부분 일치)
    List<MasterWhisky> findByWhiskyNameContaining(String name);

    // 증류소 검색
    List<MasterWhisky> findByDistilleryContaining(String distillery);

    // 대분류 검색
    List<MasterWhisky> findByCategory(String category);

    // 소분류(캐스크 등) 검색
    List<MasterWhisky> findBySubCategoryContaining(String subCategory);

    // 가격대 검색 (추후용)
    List<MasterWhisky> findByPriceRange(String priceRange);

    // 위스키 추천 알고리즘용
    @Query("SELECT m FROM MasterWhisky m WHERE " +
            "m.nose LIKE %:kw% OR m.palate LIKE %:kw% OR m.finish LIKE %:kw% OR " +
            "m.category LIKE %:kw% OR m.subCategory LIKE %:kw%")
    List<MasterWhisky> searchByTasteKeyword(@Param("kw") String keyword);
}
