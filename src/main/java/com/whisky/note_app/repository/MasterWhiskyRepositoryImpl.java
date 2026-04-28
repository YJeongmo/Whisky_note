package com.whisky.note_app.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.QMasterWhisky;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL을 사용한 동적 복합 검색 구현체입니다.
 * 각 조건 메서드는 파라미터가 null이면 null을 반환하며, QueryDSL이 해당 조건을 자동으로 제외합니다.
 */
@Repository
@RequiredArgsConstructor
public class MasterWhiskyRepositoryImpl implements MasterWhiskyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QMasterWhisky mw = QMasterWhisky.masterWhisky;

    @Override
    public List<MasterWhisky> searchByConditions(
            String name,
            String distillery,
            String category,
            String subCategory,
            String priceRange,
            Integer maxPrice,
            String flavorKeyword) {

        return queryFactory
                .selectFrom(mw)
                .where(
                        nameLike(name),
                        distilleryLike(distillery),
                        categoryEq(category),
                        subCategoryLike(subCategory),
                        priceRangeEq(priceRange),
                        maxPriceLoe(maxPrice),
                        flavorContains(flavorKeyword)
                )
                .orderBy(mw.whiskyName.asc())
                .fetch();
    }

    private BooleanExpression nameLike(String name) {
        return (name != null && !name.isBlank()) ? mw.whiskyName.containsIgnoreCase(name) : null;
    }

    private BooleanExpression distilleryLike(String distillery) {
        return (distillery != null && !distillery.isBlank()) ? mw.distillery.containsIgnoreCase(distillery) : null;
    }

    private BooleanExpression categoryEq(String category) {
        return (category != null && !category.isBlank()) ? mw.category.eq(category) : null;
    }

    private BooleanExpression subCategoryLike(String subCategory) {
        return (subCategory != null && !subCategory.isBlank()) ? mw.subCategory.containsIgnoreCase(subCategory) : null;
    }

    private BooleanExpression priceRangeEq(String priceRange) {
        return (priceRange != null && !priceRange.isBlank()) ? mw.priceRange.eq(priceRange) : null;
    }

    // loe = less or equal
    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        return maxPrice != null ? mw.price.loe(maxPrice) : null;
    }

    // nose, palate, finish 통합 검색
    private BooleanExpression flavorContains(String flavorKeyword) {
        if (flavorKeyword == null || flavorKeyword.isBlank()) return null;
        return mw.nose.containsIgnoreCase(flavorKeyword)
                .or(mw.palate.containsIgnoreCase(flavorKeyword))
                .or(mw.finish.containsIgnoreCase(flavorKeyword));
    }
}
