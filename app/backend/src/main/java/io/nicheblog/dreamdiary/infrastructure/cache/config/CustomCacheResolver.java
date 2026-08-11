package io.nicheblog.dreamdiary.infrastructure.cache.config;

import io.nicheblog.dreamdiary.infrastructure.cache.service.RedisConnChecker;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;

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
        final Collection<Cache> caches = new ArrayList<>();
        final CacheableConfig.CacheTarget cacheTarget = this.resolveCacheTarget(context);

        final boolean hasMemory = cacheTarget == CacheableConfig.CacheTarget.MEMORY
                || cacheTarget == CacheableConfig.CacheTarget.MEMORY_AND_SHARED;

        final boolean hasShared = cacheTarget == CacheableConfig.CacheTarget.SHARED
                || cacheTarget == CacheableConfig.CacheTarget.MEMORY_AND_SHARED;

        // Redis 연결 상태를 매번 체크하지 않고, 이미 캐시된 상태를 사용합니다.
        final boolean isRedisAvailable;
        if (hasShared) {
            redisConnChecker.checkRedisConnection(); // Redis 연결 상태를 주기적으로 확인만 함
            isRedisAvailable = redisConnChecker.isAvailable();
            if (!isRedisAvailable) {
                log.warn("Shared cache is unavailable. cacheTarget={}, method={}", cacheTarget, context.getMethod());
            }
        } else {
            isRedisAvailable = false;
        }

        final Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null || cacheNames.isEmpty()) {
            final String message = "At least one cache name is required: " + context.getMethod();
            log.error(message);
            throw new IllegalStateException(message);
        }
        for (final String cacheName: cacheNames) {
            if (hasMemory) {
                this.addConfiguredCache(caches, memoryCacheManager, cacheName, CacheableConfig.CacheTarget.MEMORY, context);
            }
            if (hasShared && isRedisAvailable) {
                this.addConfiguredCache(caches, remoteCacheManager, cacheName, CacheableConfig.CacheTarget.SHARED, context);
            }
        }

        if (caches.isEmpty()) {
            final String message = "No cache could be resolved. cacheTarget=" + cacheTarget + ", method=" + context.getMethod();
            log.error(message);
            throw new IllegalStateException(message);
        }
        log.debug("Cache resolved. count={}, target={}, method={}", caches.size(), cacheTarget, context.getMethod());
        return caches;
    }

    /**
     * 호출 메소드 또는 대상 클래스의 캐시 저장소 설정을 반환한다.
     * 명시 설정이 없는 일반 {@code @Cacheable} 호출은 로컬 메모리 캐시를 사용한다.
     *
     * @param context 캐시 호출 맥락
     * @return 적용할 캐시 저장소
     */
    private CacheableConfig.CacheTarget resolveCacheTarget(final CacheOperationInvocationContext<?> context) {
        final Class<?> targetClass = context.getTarget() == null
                ? context.getMethod().getDeclaringClass()
                : ClassUtils.getUserClass(context.getTarget());
        final CacheableConfig methodConfig = AnnotatedElementUtils.findMergedAnnotation(
                AopUtils.getMostSpecificMethod(context.getMethod(), targetClass),
                CacheableConfig.class
        );
        if (methodConfig != null) return methodConfig.cacheTarget();

        final CacheableConfig classConfig = AnnotatedElementUtils.findMergedAnnotation(targetClass, CacheableConfig.class);
        return classConfig == null ? CacheableConfig.CacheTarget.MEMORY : classConfig.cacheTarget();
    }

    /**
     * CacheManager에 등록된 캐시 namespace를 resolver 결과에 추가한다.
     * 등록되지 않은 namespace는 설정 결함이므로 오류 로그를 남기며 최종 빈 결과 검증에서 실패한다.
     *
     * @param caches resolver 결과 캐시 목록
     * @param cacheManager 조회할 CacheManager
     * @param cacheName 캐시 namespace
     * @param cacheTarget 조회 대상 저장소
     * @param context 캐시 호출 맥락
     */
    private void addConfiguredCache(
            final Collection<Cache> caches,
            final CacheManager cacheManager,
            final String cacheName,
            final CacheableConfig.CacheTarget cacheTarget,
            final CacheOperationInvocationContext<?> context
    ) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.error("Cache namespace is not configured. cacheName={}, cacheTarget={}, method={}",
                    cacheName, cacheTarget, context.getMethod());
            return;
        }
        caches.add(cache);
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
