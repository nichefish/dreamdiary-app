package io.nicheblog.dreamdiary.extension.cache.config;

import io.nicheblog.dreamdiary.extension.cache.service.RedisConnChecker;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * CustomCacheResolver
 * <pre>
 *  `CacheableConfig` 어노테이션을 기반으로 특정 캐시 대상(MEMORY, SHARED, MEMORY_AND_SHARED)으로 캐시를 처리합니다.
 *   레디스 연결 불가시 SHARED 캐시 처리하지 않음.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
public class CustomCacheResolver extends AbstractCacheResolver {

    private final CacheManager memoryCacheManager;
    private final CacheManager remoteCacheManager;

    private final RedisConnChecker redisConnChecker;

    /**
     * 생성자.
     *
     * @param memoryCacheManager 메모리 캐시를 관리하는 CacheManager
     * @param remoteCacheManager 원격 캐시를 관리하는 CacheManager
     */
    public CustomCacheResolver(final CacheManager memoryCacheManager, final CacheManager remoteCacheManager, final RedisConnChecker redisConnChecker) {
        super(memoryCacheManager);
        this.memoryCacheManager = memoryCacheManager;
        this.remoteCacheManager = remoteCacheManager;
        this.redisConnChecker = redisConnChecker;
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

        // Redis 연결 상태를 매번 체크하지 않고, 이미 캐시된 상태를 사용합니다.
        redisConnChecker.checkRedisConnection(); // Redis 연결 상태를 주기적으로 확인만 함
        boolean isRedisAvailable = redisConnChecker.isAvailable();

        Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null) return caches;
        for (String cacheName: cacheNames) {
            if (hasMemory) {
                Optional.ofNullable(memoryCacheManager.getCache(cacheName))
                        .filter(cache -> cache.get(cacheName) != null) // 캐시가 null이 아니면만 추가
                        .ifPresent(caches::add);
            }
            if (hasShared) {
                if (!isRedisAvailable) {
                    log.warn("Redis is not available, using memory cache only.");
                    return Collections.singletonList(new Cache() {
                        @Override
                        public String getName() {
                            return "DummyCache";  // 캐시 이름
                        }

                        @Override
                        public Object getNativeCache() {
                            return null;  // 실제 캐시 객체를 반환하지 않음
                        }

                        @Override
                        public ValueWrapper get(Object key) {
                            return null;  // 값이 없으면 null 반환
                        }

                        @Override
                        public <T> T get(Object key, Class<T> type) {
                            return null;
                        }

                        @Override
                        public <T> T get(Object key, Callable<T> valueLoader) {
                            return null;
                        }

                        @Override
                        public void put(Object key, Object value) {
                            // 캐시에 아무것도 저장하지 않음
                        }

                        @Override
                        public void evict(Object key) {
                            // 캐시에서 제거할 것도 없음
                        }

                        @Override
                        public void clear() {
                            // 캐시 초기화 없음
                        }
                    });
                }
                Optional.ofNullable(remoteCacheManager.getCache(cacheName))
                        .filter(cache -> cache.get(cacheName) != null) // 캐시가 null이 아니면만 추가
                        .ifPresent(caches::add);
            }
        }

        log.info("Cache resolved count: {}" , caches.size());
        log.info("Cache configuration: MEMORY = {}, SHARED = {}" , hasMemory, hasShared);
        if (caches.isEmpty()) return Collections.emptyList();
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