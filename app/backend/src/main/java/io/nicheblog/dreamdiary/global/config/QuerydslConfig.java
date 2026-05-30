package io.nicheblog.dreamdiary.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * QuerydslConfig
 * <pre>
 *  Querydsl 관련 설정 커스터마이즈.
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class QuerydslConfig
        implements BeanPostProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 빈 등록:: jpaQueryFactory
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(entityManager);
    }
}
