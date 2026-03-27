package io.nicheblog.dreamdiary.feature.jrnl._shared.handler;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;

/**
 * JrnlCacheEvictor
 * <pre>
 *   cacheEvictor 공통 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlCacheEvictor
        extends CacheEvictor<JrnlCacheEvictParam> {

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
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     */
    default void evictMyCacheForPeriod(final String cacheName, final Integer yy) {
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy);
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + yy + "_99");
        EhCacheUtils.evictCache(cacheName, AuthUtils.getLgnUserId() + "_" + "9999_99");
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
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
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
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
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
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

    /**
     * prefix 기반 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(정수 형식)
     * @param mnth - 삭제할 월(정수 형식)
     */
    default void evictMyCacheByPeriodPrefix(final String cacheName, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy + "_" + mnth);
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy + "_99");
            EhCacheUtils.evictMyCacheByPrefix(cacheName, "9999_99");
            return;
        }
        EhCacheUtils.evictMyCacheAll(cacheName);
    }

    /**
     * 연도 prefix 기준 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도
     */
    default void evictMyCacheByYearPrefix(final String cacheName, final Integer yy) {
        if (yy != null) {
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy.toString());
            return;
        }
        EhCacheUtils.evictMyCacheAll(cacheName);
    }

    /**
     * 일 단위 캐시 묶음 삭제
     *
     * @param yy - 삭제할 연도
     * @param mnth - 삭제할 월
     */
    default void evictMyDayPeriodCaches(final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
            this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            return;
        }
        EhCacheUtils.evictMyCacheAll("myJrnlDayList");
        EhCacheUtils.evictMyCacheAll("myJrnlDayCalList");
    }
}
