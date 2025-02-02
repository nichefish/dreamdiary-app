package io.nicheblog.dreamdiary.global._common.cache.service;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;

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
     * 캐시에서 (부재시 실제) 객체를 조회하여 반환한다.
     *
     * @param key 조회할 객체의 키 (Integer)
     * @return {@link Identifiable<Key>} 조회된 객체, 조회 실패 시 {@code null}
     * @throws Exception 조회 과정에서 발생한 예외
     */
    Identifiable<Key> getDataByKey(final Key key) throws Exception;

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

    /**
     * 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyCacheForPeriod(final String cacheName, final String yy, final String mnth) {
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
    default void evictMyCacheForPeriod(final String cacheName, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth);
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy + "_99");
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + "9999_99");
    }
}
