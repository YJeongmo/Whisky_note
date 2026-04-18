package com.whisky.note_app.service.recommendation;

import com.whisky.note_app.entity.MasterWhisky;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [순수 단위 테스트: KeywordScoreStrategyTest]
 * - Spring 컨텍스트를 로드하지 않아 매우 빠릅니다.
 * - KeywordScoreStrategy의 점수 계산 로직만을 독립적으로 검증합니다.
 * - @SpringBootTest, @DataJpaTest 어노테이션이 없으므로 DB 연결이 불필요합니다.
 */
class KeywordScoreStrategyTest {

    private KeywordScoreStrategy strategy;
    private List<MasterWhisky> mockWhiskies;

    @BeforeEach
    void setUp() {
        strategy = new KeywordScoreStrategy();
        mockWhiskies = new ArrayList<>();

        // [MasterWhisky.builder() 사용]
        // @NoArgsConstructor(access = PROTECTED)이므로 new MasterWhisky()는 불가
        // → builder()를 통해서만 생성 가능

        // 1. 피트 중심 위스키 (키워드: 피트, 스모크)
        MasterWhisky w1 = MasterWhisky.builder()
                .whiskyName("라가불린 16년")
                .nose("강렬한 피트, 해초")
                .palate("스모크, 오크")
                .finish("긴 피니시")
                .category("싱글몰트")
                .build();
        mockWhiskies.add(w1);

        // 2. 달콤한 쉐리 위스키 (키워드: 건포도, 과일)
        MasterWhisky w2 = MasterWhisky.builder()
                .whiskyName("맥캘란 12년")
                .nose("말린 과일, 건포도")
                .palate("달콤한 향신료")
                .finish("부드러운 여운")
                .category("싱글몰트")
                .build();
        mockWhiskies.add(w2);

        // 3. 피트와 바닐라가 섞인 위스키 (키워드: 피트, 바닐라)
        MasterWhisky w3 = MasterWhisky.builder()
                .whiskyName("아드베그 언 오")
                .nose("피트, 바닐라")
                .palate("스모크, 꿀")
                .finish("긴 여운")
                .category("싱글몰트")
                .build();
        mockWhiskies.add(w3);
    }

    @Test
    @DisplayName("단일 키워드 '피트' 검색 시, 관련 위스키가 모두 반환되어야 한다.")
    void singleKeywordTest() {
        List<MasterWhisky> result = strategy.recommend(mockWhiskies, "피트");

        // 라가불린(w1)과 아드베그(w3)가 나와야 함
        assertThat(result).hasSize(2);
        assertThat(result).extracting("whiskyName").containsExactlyInAnyOrder("라가불린 16년", "아드베그 언 오");
    }

    @Test
    @DisplayName("복합 키워드 '피트 바닐라' 검색 시, 두 단어를 모두 포함한 위스키가 1위여야 한다.")
    void multiKeywordScoreTest() {
        // "피트"와 "바닐라"를 모두 가진 아드베그(w3)가 가장 높은 점수를 받아야 함
        List<MasterWhisky> result = strategy.recommend(mockWhiskies, "피트 바닐라");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWhiskyName()).isEqualTo("아드베그 언 오"); // 2점 (피트+바닐라)
        assertThat(result.get(1).getWhiskyName()).isIn("라가불린 16년", "맥캘란 12년"); // 각각 1점
    }

    @Test
    @DisplayName("검색어가 포함되지 않은 경우 빈 리스트를 반환한다.")
    void noMatchTest() {
        List<MasterWhisky> result = strategy.recommend(mockWhiskies, "보드카");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 검색어도 정상적으로 처리되어야 한다.")
    void trimKeywordTest() {
        List<MasterWhisky> resultHangeul = strategy.recommend(mockWhiskies, "  피트  ");
        assertThat(resultHangeul).isNotEmpty();
    }
}
