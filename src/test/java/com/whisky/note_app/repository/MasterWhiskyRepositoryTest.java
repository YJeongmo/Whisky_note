package com.whisky.note_app.repository;

import com.whisky.note_app.entity.MasterWhisky;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MasterWhiskyRepositoryTest {

    @Autowired
    private MasterWhiskyRepository masterRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 미리 저장
        MasterWhisky m1 = new MasterWhisky();
        m1.setWhiskyName("맥캘란 12년 쉐리");
        m1.setDistillery("맥캘란");
        m1.setCategory("싱글몰트 스카치");
        m1.setSubCategory("쉐리 캐스크");
        m1.setPriceRange("10만원대");
        masterRepository.save(m1);

        MasterWhisky m2 = new MasterWhisky();
        m2.setWhiskyName("와일드 터키 101");
        m2.setDistillery("와일드 터키");
        m2.setCategory("버번");
        m2.setSubCategory("버진 오크");
        m2.setPriceRange("5만원대");
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