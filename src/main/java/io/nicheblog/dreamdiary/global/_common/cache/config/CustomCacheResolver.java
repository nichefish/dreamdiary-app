package io.nicheblog.dreamdiary.global._common.cache.config;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * CustomCacheResolver
 * <pre>
 *  `CacheableConfig` 어노테이션을 기반으로 특정 캐시 대상(MEMORY, SHARED, MEMORY_AND_SHARED)으로 캐시를 처리합니다.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
public class CustomCacheResolver extends AbstractCacheResolver {

    private final CacheManager memoryCacheManager;
    private final CacheManager remoteCacheManager;

    /**
     * 생성자.
     *
     * @param memoryCacheManager 메모리 캐시를 관리하는 CacheManager
     * @param remoteCacheManager 원격 캐시를 관리하는 CacheManager
     */
    public CustomCacheResolver(CacheManager memoryCacheManager,CacheManager remoteCacheManager) {
        super(memoryCacheManager);
        this.memoryCacheManager = memoryCacheManager;
        this.remoteCacheManager = remoteCacheManager;
    }

    /**
     * 캐시 설정에 따라 메모리 캐시 또는 원격 캐시에서 캐시를 가져옵니다.
     *
     * @param context 캐시 호출 맥락 (annotation)
     * @return {@link Collection} -- 설정된 캐시 목록
     */
    @Override
    public @NotNull Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();

        CacheableConfig cacheableConfig = context.getMethod().getAnnotation(CacheableConfig.class);

        boolean hasMemory = cacheableConfig == null
                || cacheableConfig.cacheTarget() == CacheableConfig.CacheTarget.MEMORY
                || cacheableConfig.cacheTarget() == CacheableConfig.CacheTarget.MEMORY_AND_SHARED;

        boolean hasShared = cacheableConfig == null
                || cacheableConfig.cacheTarget() == CacheableConfig.CacheTarget.SHARED
                || cacheableConfig.cacheTarget() == CacheableConfig.CacheTarget.MEMORY_AND_SHARED;

        Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null) return caches;
        for (String cacheName: cacheNames) {
            if (hasMemory) {
                Optional.ofNullable(memoryCacheManager.getCache(cacheName))
                        .ifPresent(caches::add);
            }
            if (hasShared) {
                Optional.ofNullable(remoteCacheManager.getCache(cacheName))
                        .ifPresent(caches::add);
            }
        }

        log.info("Cache resolved count: {}" , caches.size());
        log.info("Cache configuration: MEMORY = {}, SHARED = {}" , hasMemory, hasShared);
        return caches;
    }

    /**
     * 캐시 이름을 반환합니다.
     *
     * @param context 캐시 연산 맥락 (annotation)
     * @return {@link Collection} -- 캐시 이름 목록
     */
    @Override
    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        return context.getOperation().getCacheNames();
    }
}