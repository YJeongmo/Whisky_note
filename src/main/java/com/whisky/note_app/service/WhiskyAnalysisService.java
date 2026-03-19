package com.whisky.note_app.service;

import com.whisky.note_app.dto.WhiskyAnalysisResult;
import com.whisky.note_app.entity.UserPreference;
import com.whisky.note_app.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WhiskyAnalysisService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserPreferenceRepository preferenceRepository;

    @Transactional
    public WhiskyAnalysisResult analyzeAndSavePreference(String noteContent, Double rating) {
        // 1. JSON 변환기 설정
        var converter = new BeanOutputConverter<>(WhiskyAnalysisResult.class);

        // 2. ChatClient 생성 및 프롬프트 실행
        ChatClient chatClient = chatClientBuilder.build();

        WhiskyAnalysisResult result = chatClient.prompt()
                .user(u -> u.text("""
                    당신은 위스키 전문가입니다. 다음 시음 정보와 평점을 분석하여 사용자가 긍정적으로 느낀 맛 키워드(like)와 부정적으로 느낀 맛 키워드(dislike)를 추출하세요.
                    
                    [데이터]
                    시음 정보: {note}
                    사용자 평점: {rating}점 (5점 만점)
                    
                    [규칙]
                    1. 평점이 높을수록(4~5점) 언급된 특징은 'like'일 확률이 높습니다.
                    2. 평점이 낮을수록(1~2점) 언급된 특징은 'dislike'일 확률이 높습니다.
                    3. 결과는 반드시 한국어 단어로만 추출하세요.
                    
                    {format}
                    """)
                        .param("note", noteContent) // 합쳐진 N, P, F 텍스트
                        .param("rating", rating)        // 평점
                        .param("format", converter.getFormat())) // JSON 포맷 가이드
                .call()
                .entity(converter);

        // 3. 점수 업데이트 로직
        if (result != null) {
            updatePreferenceScores(result.like(), 1);   // 선호 키워드 +1점
            updatePreferenceScores(result.dislike(), -1); // 비선호 키워드 -1점
        }

        return result;
    }

    private void updatePreferenceScores(java.util.List<String> keywords, int delta) {
        if (keywords == null) return;
        for (String kw : keywords) {
            UserPreference pref = preferenceRepository.findByKeyword(kw)
                    .orElseGet(() -> new UserPreference(kw, 0));
            pref.updateScore(delta);
            preferenceRepository.save(pref);
        }
    }
}