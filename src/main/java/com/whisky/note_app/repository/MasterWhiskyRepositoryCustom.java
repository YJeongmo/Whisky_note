package com.whisky.note_app.repository;

import com.whisky.note_app.entity.MasterWhisky;

import java.util.List;

/**
 * [MasterWhiskyRepositoryCustom]
 *
 * QueryDSL 동적 쿼리를 위한 커스텀 Repository 인터페이스입니다.
 * MasterWhiskyRepository가 이 인터페이스를 상속하면
 * Spring Data JPA의 기본 메서드와 QueryDSL 메서드를 함께 사용할 수 있습니다.
 */
public interface MasterWhiskyRepositoryCustom {

    /**
     * 복합 조건 동적 검색
     * 전달된 조건 중 null이 아닌 것만 WHERE 절에 포함됩니다.
     *
     * @param name       위스키 이름 (부분 일치)
     * @param distillery 증류소 (부분 일치)
     * @param category   대분류 (완전 일치)
     * @param subCategory 소분류 (부분 일치)
     * @param priceRange 가격대 문자열 (완전 일치)
     * @param maxPrice   최대 가격 (이하 조건)
     * @param flavorKeyword 향미 키워드 (nose + palate + finish 통합 검색)
     */
    List<MasterWhisky> searchByConditions(
            String name,
            String distillery,
            String category,
            String subCategory,
            String priceRange,
            Integer maxPrice,
            String flavorKeyword
    );
}
