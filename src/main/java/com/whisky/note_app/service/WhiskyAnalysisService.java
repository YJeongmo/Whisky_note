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
 * [WhiskyAnalysisService — Step 12 변경]
 *
 * [@Async 적용]
 * analyzeAndSavePreference()가 별도 스레드(analysisExecutor)에서 실행됩니다.
 * 호출 즉시 반환되어 API가 블로킹되지 않습니다.
 *
 * [@Transactional 제거 이유]
 * @Async는 별도 스레드에서 실행되므로 호출자의 트랜잭션을 이어받을 수 없습니다.
 * 실제 DB 저장은 PreferenceUpdateService.doSingleAttempt()의 @Transactional이 처리합니다.
 *
 * [왜 User 엔티티 대신 userId를 받는가?]
 * @Async 메서드는 별도 스레드에서 실행됩니다.
 * HTTP 요청 스레드의 JPA 세션은 요청이 끝나면 닫히는데,
 * User 엔티티를 그대로 넘기면 async 스레드에서 Lazy Loading 시 세션이 없어 오류가 납니다.
 * userId만 넘기고 async 메서드 안에서 새 JPA 세션으로 다시 조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhiskyAnalysisService {

    private final ChatClient.Builder chatClientBuilder;
    private final PreferenceUpdateService preferenceUpdateService;
    private final UserRepository userRepository;

    /**
     * [@CacheEvict — Step 17]
     *
     * AI 분석이 완료되면 해당 유저의 취향이 바뀌므로
     * 기존에 캐싱된 추천 결과를 모두 삭제합니다.
     * allEntries = true → 이 유저의 모든 maxPrice 조건 캐시를 한 번에 삭제
     * (userId_null, userId_100000 등 maxPrice가 달라도 모두 무효화)
     *
     * 다음 추천 요청 때 @Cacheable이 DB에서 새로 계산 후 Redis에 저장합니다.
     */
    @Async("analysisExecutor")
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public void analyzeAndSavePreference(String noteContent, Double rating, Long userId) {
        // 새 JPA 세션으로 User 재조회
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
     * [병렬 처리 — Phase 2 개선]
     * 키워드 간 의존성이 없으므로 CompletableFuture로 병렬 저장
     * 순차 46ms → 병렬 14ms (3.3배 단축)
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
