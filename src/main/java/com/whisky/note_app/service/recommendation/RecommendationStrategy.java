package com.whisky.note_app.service.recommendation;

import com.whisky.note_app.entity.MasterWhisky;
import java.util.List;

public interface RecommendationStrategy {
    // 전체 위스키 리스트와 입력된 키워드들을 받아 정렬된 리스트를 반환함
    List<MasterWhisky> recommend(List<MasterWhisky> allWhiskies, String input);
}
