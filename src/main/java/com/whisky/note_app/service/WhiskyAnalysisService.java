package com.whisky.note_app.service;

import com.whisky.note_app.config.CacheConfig;
import com.whisky.note_app.dto.WhiskyAnalysisResult;
import com.whisky.note_app.entity.User;
import com.whisky.note_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 노트 내용을 AI로 분석하여 취향 키워드를 추출하고 선호도 점수를 갱신합니다.
 *
 * userId를 파라미터로 받는 이유: @Async는 별도 스레드에서 실행되므로 호출자의 JPA 세션이 닫힌 후
 * User 엔티티를 사용하면 LazyInitializationException이 발생합니다. 메서드 내부에서 재조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhiskyAnalysisService {

    private final ChatClient.Builder chatClientBuilder;
    private final PreferenceUpdateService preferenceUpdateService;
    private final UserRepository userRepository;

    /**
     * AI 분석 완료 후 해당 유저의 추천 캐시를 전체 무효화합니다.
     */
    @Async("analysisExecutor")
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public void analyzeAndSavePreference(String noteContent, Double rating, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + userId));

        var converter = new BeanOutputConverter<>(WhiskyAnalysisResult.class);
        ChatClient chatClient = chatClientBuilder.build();

        log.info("[AI 분석 시작] 스레드: {}, userId: {}", Thread.currentThread().getName(), userId);

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
                        .param("note", noteContent)
                        .param("rating", rating)
                        .param("format", converter.getFormat()))
                .call()
                .entity(converter);

        if (result != null) {
            updatePreferenceScores(result.like(), 1, user);
            updatePreferenceScores(result.dislike(), -1, user);
        }

        log.info("[AI 분석 완료] 스레드: {}, userId: {}", Thread.currentThread().getName(), userId);
    }

    /**
     * 키워드 간 의존성이 없으므로 CompletableFuture로 병렬 처리합니다.
     */
    private void updatePreferenceScores(List<String> keywords, int delta, User user) {
        if (keywords == null) return;
        CompletableFuture<?>[] futures = keywords.stream()
                .map(kw -> CompletableFuture.runAsync(
                        () -> preferenceUpdateService.updateWithRetry(kw, delta, user)))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
    }
}
