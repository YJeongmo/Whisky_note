package com.whisky.note_app.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [QueryDslConfig — Step 19]
 *
 * [JPAQueryFactory란?]
 * QueryDSL로 쿼리를 작성할 때 사용하는 핵심 객체입니다.
 * EntityManager를 주입받아 JPQL 대신 타입 안전한 Java 코드로 쿼리를 작성할 수 있습니다.
 *
 * [왜 Bean으로 등록하나?]
 * JPAQueryFactory는 EntityManager에 의존합니다.
 * Bean으로 등록하면 Spring이 EntityManager의 생명주기를 관리하고
 * 트랜잭션 범위에 맞게 EntityManager를 자동으로 주입합니다.
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
