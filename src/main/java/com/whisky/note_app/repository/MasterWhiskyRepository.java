package com.whisky.note_app.repository;

import com.whisky.note_app.entity.MasterWhisky;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterWhiskyRepository extends JpaRepository<MasterWhisky, Long>, MasterWhiskyRepositoryCustom {

    boolean existsByWhiskyName(String whiskyName);

    List<MasterWhisky> findByWhiskyNameContaining(String name);

    List<MasterWhisky> findByDistilleryContaining(String distillery);

    List<MasterWhisky> findByCategory(String category);

    List<MasterWhisky> findBySubCategoryContaining(String subCategory);

    List<MasterWhisky> findByPriceRange(String priceRange);

    @Query("SELECT m FROM MasterWhisky m WHERE " +
            "m.nose LIKE %:kw% OR m.palate LIKE %:kw% OR m.finish LIKE %:kw% OR " +
            "m.category LIKE %:kw% OR m.subCategory LIKE %:kw%")
    List<MasterWhisky> searchByTasteKeyword(@Param("kw") String keyword);
}
