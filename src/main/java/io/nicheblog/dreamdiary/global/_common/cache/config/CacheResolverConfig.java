package io.nicheblog.dreamdiary.global._common.cache.config;

import io.nicheblog.dreamdiary.global._common.cache.service.RedisConnChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

/**
 * CacheResolverConfig
 * <pre>
 *  Cache 관련 설정 커스터마이즈 (공톨 Resolver 부분 분리)
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@RequiredArgsConstructor
public class CacheResolverConfig {

    private final JCacheCacheManager jCacheCacheManager;
    private final RedisCacheManager redisCacheManager;

    private final RedisConnChecker redisConnChecker;

    /**
     * 빈 등록:: cacheResolver
     */
    @Bean
    public CacheResolver cacheResolver() {
        return new CustomCacheResolver(jCacheCacheManager, redisCacheManager, redisConnChecker);
    }
}