package io.nicheblog.dreamdiary.extension.cache.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;

/**
 * CacheEvictor
 * <pre>
 *   cacheEvictor 공통 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CacheEvictor<T> {

    /**
     * 캐시 삭제
     *
     * @param object 캐시에서 삭제할 정보를 담은 파라미터
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    void evict(T object) throws Exception;

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictCacheForPeriod(final String cacheName, final Integer yy, final Integer mnth) {
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
    default void evictMyCacheForPeriod(final String cacheName, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth);
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy + "_99");
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + "9999_99");
    }

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyCacheWithKeyForPeriod(final String cacheName, final String key, final String yy, final String mnth) {
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + yy + "_" + mnth);
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + yy + "_99");
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + "9999_99");
    }

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyCacheWithKeyForPeriod(final String cacheName, final String key, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + yy + "_" + mnth);
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + yy + "_99");
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + key + "_" + "9999_99");
    }
}
