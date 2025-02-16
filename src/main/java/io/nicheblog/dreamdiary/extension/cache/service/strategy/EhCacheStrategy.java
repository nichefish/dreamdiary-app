package io.nicheblog.dreamdiary.extension.cache.service.strategy;

import io.nicheblog.dreamdiary.extension.cache.service.CacheStrategy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 *EhCacheStrategy
 * <pre>
 *  EhCache 캐시 구현체에 대한 전략 패턴을 적용한 클래스.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class EhCacheStrategy
        implements CacheStrategy {

    @Resource(name="jCacheManager")
    CacheManager cacheManager;

    /**
     * EhCache 캐시에서 특정 키에 해당하는 객체를 가져온다.
     *
     * @param cacheName 캐시 이름
     * @param cacheKey  조회할 캐시 키
     * @return 캐시에서 조회된 객체, 없을 경우 {@code null} 반환
     */
    @Override
    public Object getObject(final String cacheName, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return null;

        javax.cache.Cache<Object, Object> ehCache = (javax.cache.Cache<Object, Object>) cache.getNativeCache();
        return ehCache.get(cacheKey);
    }

    /**
     * EhCache 캐시에 저장된 모든 값을 조회하여 주어진 {@link Map}에 추가한다.
     *
     * @param cacheName     캐시 이름
     * @param cacheValueMap 캐시 값을 저장할 {@link Map} 객체
     */
    @Override
    public void addCacheValue(final String cacheName, final Map<Object, Object> cacheValueMap) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        final javax.cache.Cache<Object, Object> ehCache = (javax.cache.Cache<Object, Object>) cache.getNativeCache();

        ehCache.iterator().forEachRemaining(entry -> {
            try {
                final String readableKey = stringifyKey(entry.getKey());
                cacheValueMap.put(readableKey, entry.getValue());
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 해당 캐시가 EhCache 캐시인지 확인한다.
     *
     * @param cache {@link Cache} 객체
     * @return Caffeine 캐시인 경우 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean supports(Cache cache) {
        return cache.getNativeCache() instanceof javax.cache.Cache;
    }
}
