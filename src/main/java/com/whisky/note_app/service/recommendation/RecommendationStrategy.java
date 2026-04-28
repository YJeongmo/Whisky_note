package com.whisky.note_app.service.recommendation;

import com.whisky.note_app.entity.MasterWhisky;
import java.util.List;

public interface RecommendationStrategy {
    List<MasterWhisky> recommend(List<MasterWhisky> allWhiskies, String input);
}
