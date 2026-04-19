package com.whisky.note_app.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * [CacheConfig — Step 15]
 *
 * [@EnableCaching]
 * @Cacheable, @CacheEvict 등 캐시 애너테이션이 동작하도록 활성화합니다.
 *
 * [왜 기본 캐시를 쓰지 않나?]
 * Spring 기본 캐시(ConcurrentHashMap)는 서버 재시작 시 데이터가 사라지고
 * 서버가 여러 대일 때 캐시가 공유되지 않습니다.
 * Redis를 캐시 저장소로 사용하면 서버 재시작과 무관하게 캐시가 유지됩니다.
 *
 * [TTL (Time To Live)]
 * 캐시 만료 시간을 10분으로 설정합니다.
 * 취향 업데이트가 없어도 10분 후에는 자동으로 만료되어 최신 데이터가 반영됩니다.
 *
 * [직렬화 설정]
 * Redis는 데이터를 바이트 배열로 저장합니다.
 * - 키: StringRedisSerializer → "recommendations::1" 같이 읽기 쉬운 문자열로 저장
 * - 값: GenericJackson2JsonRedisSerializer → 객체를 JSON으로 변환해 저장
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String RECOMMENDATIONS_CACHE = "recommendations";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))                       // 캐시 TTL: 10분
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
