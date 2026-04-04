package io.nicheblog.dreamdiary.feature.jrnl._shared.handler;

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
    default void evictMyYyCache(final String userId, final String cacheName, final Integer yy) {
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
    default void evictMyYyMnthCache(final String userId, final String cacheName, final Integer yy, final Integer mnth) {
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
    default void evictMyYyMnthCacheByKey(final String userId, final String cacheName, final String key, final String yy, final String mnth) {
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
    default void evictMyYyMnthCacheByKey(final String userId, final String cacheName, final String key, final Integer yy, final Integer mnth) {
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
    default void evictMyYnMnthCacheByPrefix(final String userId, final String cacheName, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            EhCacheUtils.evictUserCacheByPrefix(cacheName, userId, yy + "_" + mnth);
            EhCacheUtils.evictUserCacheByPrefix(cacheName, userId, yy + "_99");
            EhCacheUtils.evictUserCacheByPrefix(cacheName, userId, "9999_99");
            return;
        }
        EhCacheUtils.clearUserCache(cacheName, userId);
    }

    /**
     * 연도 prefix 기준 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도
     */
    default void evictMyYyCacheByYyPrefix(final String userId, final String cacheName, final Integer yy) {
        if (yy != null) {
            EhCacheUtils.evictUserCacheByPrefix(cacheName, userId, yy.toString());
            return;
        }
        EhCacheUtils.clearUserCache(cacheName, userId);
    }

    /**
     * 일 단위 캐시 묶음 삭제
     *
     * @param yy - 삭제할 연도
     * @param mnth - 삭제할 월
     */
    default void evictMyJrnlDayYyMnthCaches(final String userId, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            // Current journal month list cache and companion state maps share the same key scope.
            this.evictMyYyMnthCache(userId, "jrnlDayYyMnthListByUser", yy, mnth);
            this.evictMyYyMnthCache(userId, "jrnlEntryStateMapByUser", yy, mnth);
            this.evictMyYyMnthCache(userId, "jrnlDiaryStateMapByUser", yy, mnth);
            this.evictMyYyMnthCache(userId, "jrnlDreamStateMapByUser", yy, mnth);
            this.evictMyYyMnthCache(userId, "jrnlIntrptStateMapByUser", yy, mnth);
            // Legacy cache names kept as no-op-safe fallback during migration.
            this.evictMyYyMnthCache(userId, "myJrnlDayList", yy, mnth);
            this.evictMyYyMnthCache(userId, "myJrnlDayCalList", yy, mnth);
            return;
        }
        EhCacheUtils.clearUserCache("jrnlDayYyMnthListByUser", userId);
        EhCacheUtils.clearUserCache("jrnlEntryStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlDiaryStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlDreamStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlIntrptStateMapByUser", userId);
        EhCacheUtils.clearUserCache("myJrnlDayList", userId);
        EhCacheUtils.clearUserCache("myJrnlDayCalList", userId);
    }

    /**
     * 일 단위 주간 캐시 묶음 삭제
     */
    default void evictMyJrnlDayWeeklyCaches(final String userId) {
        EhCacheUtils.clearUserCache("jrnlDayWeeklyListByUser", userId);
        EhCacheUtils.clearUserCache("jrnlEntryWeeklyStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlDiaryWeeklyStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlDreamWeeklyStateMapByUser", userId);
        EhCacheUtils.clearUserCache("jrnlIntrptWeeklyStateMapByUser", userId);
    }

    /**
     * 일 단위 주간 캐시 묶음 삭제
     *
     * @param weekStartDts 삭제할 주 시작일자 목록
     */
    default void evictMyJrnlDayWeeklyCaches(final String userId, final String... weekStartDts) {
        if (weekStartDts == null || weekStartDts.length == 0) {
            this.evictMyJrnlDayWeeklyCaches(userId);
            return;
        }

        boolean hasTarget = false;
        for (final String weekStartDt : weekStartDts) {
            if (weekStartDt == null || weekStartDt.isBlank()) continue;
            hasTarget = true;
            EhCacheUtils.evictUserCacheByKey("jrnlDayWeeklyListByUser", userId, weekStartDt);
            EhCacheUtils.evictUserCacheByKey("jrnlEntryWeeklyStateMapByUser", userId, weekStartDt);
            EhCacheUtils.evictUserCacheByKey("jrnlDiaryWeeklyStateMapByUser", userId, weekStartDt);
            EhCacheUtils.evictUserCacheByKey("jrnlDreamWeeklyStateMapByUser", userId, weekStartDt);
            EhCacheUtils.evictUserCacheByKey("jrnlIntrptWeeklyStateMapByUser", userId, weekStartDt);
        }

        if (!hasTarget) this.evictMyJrnlDayWeeklyCaches(userId);
    }
}
