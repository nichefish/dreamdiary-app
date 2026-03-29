package io.nicheblog.dreamdiary.feature.jrnl._shared.handler;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.springframework.cache.interceptor.SimpleKey;

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
    default void evictYyMnthCache(final String cacheName, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCacheByKey(cacheName, yy + "_" + mnth);
        EhCacheUtils.evictCacheByKey(cacheName, yy + "_99");
        EhCacheUtils.evictCacheByKey(cacheName, "9999_99");
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     */
    default void evictMyYyCache(final String cacheName, final Integer yy) {
        final String userId = AuthUtils.getLgnUserId();
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, yy));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, 9999, 99));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyYyMnthCache(final String cacheName, final Integer yy, final Integer mnth) {
        final String userId = AuthUtils.getLgnUserId();
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, 9999, 99));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyYyMnthCacheByKey(final String cacheName, final String key, final String yy, final String mnth) {
        final String userId = AuthUtils.getLgnUserId();
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, yy, "99"));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, "9999", "99"));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(숫자 형식)
     * @param mnth - 삭제할 월(숫자 형식)
     */
    default void evictMyYyMnthCacheByKey(final String cacheName, final String key, final Integer yy, final Integer mnth) {
        final String userId = AuthUtils.getLgnUserId();
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(userId, key, 9999, 99));
    }

    /**
     * prefix 기반 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(정수 형식)
     * @param mnth - 삭제할 월(정수 형식)
     */
    default void evictMyYnMnthCacheByPrefix(final String cacheName, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy + "_" + mnth);
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy + "_99");
            EhCacheUtils.evictMyCacheByPrefix(cacheName, "9999_99");
            return;
        }
        EhCacheUtils.clearMyCache(cacheName);
    }

    /**
     * 연도 prefix 기준 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도
     */
    default void evictMyYyCacheByYyPrefix(final String cacheName, final Integer yy) {
        if (yy != null) {
            EhCacheUtils.evictMyCacheByPrefix(cacheName, yy.toString());
            return;
        }
        EhCacheUtils.clearMyCache(cacheName);
    }

    /**
     * 일 단위 캐시 묶음 삭제
     *
     * @param yy - 삭제할 연도
     * @param mnth - 삭제할 월
     */
    default void evictMyJrnlDayYyMnthCaches(final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            this.evictMyYyMnthCache("myJrnlDayList", yy, mnth);
            this.evictMyYyMnthCache("myJrnlDayCalList", yy, mnth);
            return;
        }
        EhCacheUtils.clearMyCache("myJrnlDayList");
        EhCacheUtils.clearMyCache("myJrnlDayCalList");
    }
}
