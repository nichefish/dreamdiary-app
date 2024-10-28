package io.nicheblog.dreamdiary.global._common.cache.service;

import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;

import java.io.Serializable;

/**
 * CacheEvictor
 * <pre>
 *   cacheEvictor 공통 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CacheEvictor<Key extends Serializable> {

    /**
     * 캐시 삭제
     *
     * @param key 캐시에서 삭제할 대상 키
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    void evict(Key key) throws Exception;

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictCacheForPeriod(final String cacheName, final String yy, final String mnth) {
        EhCacheUtils.evictCache(cacheName, yy + "_" + mnth);
        EhCacheUtils.evictCache(cacheName, yy + "_99");
        EhCacheUtils.evictCache(cacheName, "9999_99");
    }

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictCacheForPeriod(final String cacheName, final Integer yy, final Integer mnth) {
        this.evictCacheForPeriod(cacheName, yy.toString(), mnth.toString());
    }
}
