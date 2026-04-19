package com.whisky.note_app.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whisky.note_app.entity.MasterWhisky;
import com.whisky.note_app.entity.QMasterWhisky;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * [MasterWhiskyRepositoryImpl — Step 19]
 *
 * QueryDSL을 사용한 동적 쿼리 구현체입니다.
 *
 * [Q클래스란?]
 * QueryDSL이 컴파일 시 엔티티를 분석해 자동 생성하는 메타 클래스입니다.
 * QMasterWhisky.masterWhisky.whiskyName 같은 방식으로 필드를 타입 안전하게 참조합니다.
 * 오타가 있으면 런타임이 아닌 컴파일 시점에 오류가 발생합니다.
 *
 * [BooleanExpression이란?]
 * QueryDSL의 WHERE 조건을 나타내는 타입입니다.
 * null을 반환하면 해당 조건이 쿼리에서 자동으로 제외됩니다.
 * 이 특성을 이용해 null인 파라미터는 조건에서 빠지는 동적 쿼리를 만들 수 있습니다.
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

    // 이름 부분 일치 — null이면 조건 제외
    private BooleanExpression nameLike(String name) {
        return (name != null && !name.isBlank()) ? mw.whiskyName.containsIgnoreCase(name) : null;
    }

    // 증류소 부분 일치
    private BooleanExpression distilleryLike(String distillery) {
        return (distillery != null && !distillery.isBlank()) ? mw.distillery.containsIgnoreCase(distillery) : null;
    }

    // 대분류 완전 일치
    private BooleanExpression categoryEq(String category) {
        return (category != null && !category.isBlank()) ? mw.category.eq(category) : null;
    }

    // 소분류 부분 일치
    private BooleanExpression subCategoryLike(String subCategory) {
        return (subCategory != null && !subCategory.isBlank()) ? mw.subCategory.containsIgnoreCase(subCategory) : null;
    }

    // 가격대 완전 일치 (예: "10만원대")
    private BooleanExpression priceRangeEq(String priceRange) {
        return (priceRange != null && !priceRange.isBlank()) ? mw.priceRange.eq(priceRange) : null;
    }

    // 최대 가격 이하 (loe = less or equal)
    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        return maxPrice != null ? mw.price.loe(maxPrice) : null;
    }

    // 향미 키워드 — nose, palate, finish 통합 검색
    private BooleanExpression flavorContains(String flavorKeyword) {
        if (flavorKeyword == null || flavorKeyword.isBlank()) return null;
        return mw.nose.containsIgnoreCase(flavorKeyword)
                .or(mw.palate.containsIgnoreCase(flavorKeyword))
                .or(mw.finish.containsIgnoreCase(flavorKeyword));
    }
}
