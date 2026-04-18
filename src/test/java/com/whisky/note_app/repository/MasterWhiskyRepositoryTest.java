package com.whisky.note_app.repository;

import com.whisky.note_app.entity.MasterWhisky;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [@DataJpaTest]
 * - JPA 관련 빈만 로드하는 슬라이스 테스트입니다. (@SpringBootTest보다 훨씬 빠릅니다)
 * - JpaRepository, @Entity 등은 로드되지만 @Service, @Controller는 로드되지 않습니다.
 *
 * [@AutoConfigureTestDatabase(replace = NONE)]
 * - 기본값은 내장 H2로 자동 교체(replace = ANY)입니다.
 * - NONE으로 설정하면 application-test.yml에서 설정한 DB를 그대로 사용합니다.
 * - @ActiveProfiles("test")와 함께 사용해 H2 설정을 application-test.yml에서 관리합니다.
 *
 * [@ActiveProfiles("test")]
 * - application-test.yml을 활성화합니다.
 * - 이 설정 없이는 application.yml (PostgreSQL)을 사용하려 해서 테스트가 실패합니다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MasterWhiskyRepositoryTest {

    @Autowired
    private MasterWhiskyRepository masterRepository;

    @BeforeEach
    void setUp() {
        // [MasterWhisky.builder() 사용 이유]
        // MasterWhisky에 @NoArgsConstructor(access = PROTECTED)가 적용되어
        // new MasterWhisky()로는 생성할 수 없습니다.
        // → @Builder 패턴으로만 생성 가능 (의도적인 설계: 불완전한 객체 생성 방지)
        MasterWhisky m1 = MasterWhisky.builder()
                .whiskyName("맥캘란 12년 쉐리")
                .distillery("맥캘란")
                .category("싱글몰트 스카치")
                .subCategory("쉐리 캐스크")
                .priceRange("10만원대")
                .build();
        masterRepository.save(m1);

        MasterWhisky m2 = MasterWhisky.builder()
                .whiskyName("와일드 터키 101")
                .distillery("와일드 터키")
                .category("버번")
                .subCategory("버진 오크")
                .priceRange("5만원대")
                .build();
        masterRepository.save(m2);
    }

    @Test
    @DisplayName("이름 일부 검색 테스트")
    void findByNameTest() {
        List<MasterWhisky> result = masterRepository.findByWhiskyNameContaining("맥캘란");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWhiskyName()).contains("맥캘란");
    }

    @Test
    @DisplayName("증류소 검색 테스트")
    void findByDistilleryTest() {
        List<MasterWhisky> result = masterRepository.findByDistilleryContaining("와일드");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getDistillery()).isEqualTo("와일드 터키");
    }

    @Test
    @DisplayName("대분류 일치 검색 테스트")
    void findByCategoryTest() {
        List<MasterWhisky> result = masterRepository.findByCategory("버번");
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("소분류(캐스크) 포함 검색 테스트")
    void findBySubCategoryTest() {
        List<MasterWhisky> result = masterRepository.findBySubCategoryContaining("쉐리");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSubCategory()).contains("쉐리");
    }

    @Test
    @DisplayName("가격대 검색 테스트 (미래 대비)")
    void findByPriceRangeTest() {
        List<MasterWhisky> result = masterRepository.findByPriceRange("10만원대");
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getPriceRange()).isEqualTo("10만원대");
    }
}
