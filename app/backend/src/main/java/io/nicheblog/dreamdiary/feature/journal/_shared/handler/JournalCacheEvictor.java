package io.nicheblog.dreamdiary.feature.journal._shared.handler;

import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.springframework.cache.interceptor.SimpleKey;

/**
 * JournalCacheEvictor
 * <pre>
 *   cacheEvictor 공통 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JournalCacheEvictor
        extends CacheEvictor<JournalCacheEvictParam> {

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
    default void evictMyYyCache(final String username, final String cacheName, final Integer yy) {
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, yy));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, 9999, 99));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyYyMnthCache(final String username, final String cacheName, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, 9999, 99));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(문자열 형식)
     * @param mnth - 삭제할 월(문자열 형식)
     */
    default void evictMyYyMnthCacheByKey(final String username, final String cacheName, final String key, final String yy, final String mnth) {
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, yy, "99"));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, "9999", "99"));
    }

    /**
     * 사용자 캐시 이름에 대해서 기간 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(숫자 형식)
     * @param mnth - 삭제할 월(숫자 형식)
     */
    default void evictMyYyMnthCacheByKey(final String username, final String cacheName, final String key, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, yy, mnth));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, yy, 99));
        EhCacheUtils.evictCacheByKey(cacheName, new SimpleKey(username, key, 9999, 99));
    }

    /**
     * prefix 기반 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도(정수 형식)
     * @param mnth - 삭제할 월(정수 형식)
     */
    default void evictMyYnMnthCacheByPrefix(final String username, final String cacheName, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            EhCacheUtils.evictUserCacheByPrefix(cacheName, username, yy + "_" + mnth);
            EhCacheUtils.evictUserCacheByPrefix(cacheName, username, yy + "_99");
            EhCacheUtils.evictUserCacheByPrefix(cacheName, username, "9999_99");
            return;
        }
        EhCacheUtils.clearUserCache(cacheName, username);
    }

    /**
     * 연도 prefix 기준 사용자 캐시 삭제
     *
     * @param cacheName - 삭제할 캐시 이름
     * @param yy - 삭제할 연도
     */
    default void evictMyYyCacheByYyPrefix(final String username, final String cacheName, final Integer yy) {
        if (yy != null) {
            EhCacheUtils.evictUserCacheByPrefix(cacheName, username, yy.toString());
            return;
        }
        EhCacheUtils.clearUserCache(cacheName, username);
    }

    /**
     * 일 단위 캐시 묶음 삭제
     *
     * @param yy - 삭제할 연도
     * @param mnth - 삭제할 월
     */
    default void evictMyJournalDayYyMnthCaches(final String username, final Integer yy, final Integer mnth) {
        if (yy != null && mnth != null) {
            // Current journal month list cache and companion state maps share the same key scope.
            this.evictMyYyMnthCache(username, "journalDayYyMnthListByUser", yy, mnth);
            JournalStateCacheRegistry.stateContentTypes().forEach(
                    contentType -> this.evictMyYyMnthCache(
                            username,
                            JournalStateCacheRegistry.monthlyMapCacheName(contentType),
                            yy,
                            mnth
                    )
            );
            // Legacy cache names kept as no-op-safe fallback during migration.
            this.evictMyYyMnthCache(username, "myJournalDayList", yy, mnth);
            this.evictMyYyMnthCache(username, "myJournalDayCalList", yy, mnth);
            return;
        }
        EhCacheUtils.clearUserCache("journalDayYyMnthListByUser", username);
        JournalStateCacheRegistry.stateContentTypes().forEach(
                contentType -> EhCacheUtils.clearUserCache(
                        JournalStateCacheRegistry.monthlyMapCacheName(contentType),
                        username
                )
        );
        EhCacheUtils.clearUserCache("myJournalDayList", username);
        EhCacheUtils.clearUserCache("myJournalDayCalList", username);
    }

    /**
     * 일 단위 주간 캐시 묶음 삭제
     */
    default void evictMyJournalDayWeeklyCaches(final String username) {
        EhCacheUtils.clearUserCache("journalDayWeeklyListByUser", username);
        JournalStateCacheRegistry.stateContentTypes().forEach(
                contentType -> EhCacheUtils.clearUserCache(
                        JournalStateCacheRegistry.weeklyMapCacheName(contentType),
                        username
                )
        );
    }

    /**
     * 일 단위 주간 캐시 묶음 삭제
     *
     * @param weekStartDts 삭제할 주 시작일자 목록
     */
    default void evictMyJournalDayWeeklyCaches(final String username, final String... weekStartDts) {
        if (weekStartDts == null || weekStartDts.length == 0) {
            this.evictMyJournalDayWeeklyCaches(username);
            return;
        }

        boolean hasTarget = false;
        for (final String weekStartDt : weekStartDts) {
            if (weekStartDt == null || weekStartDt.isBlank()) continue;
            hasTarget = true;
            EhCacheUtils.evictUserCacheByKey("journalDayWeeklyListByUser", username, weekStartDt);
            JournalStateCacheRegistry.stateContentTypes().forEach(
                    contentType -> EhCacheUtils.evictUserCacheByKey(
                            JournalStateCacheRegistry.weeklyMapCacheName(contentType),
                            username,
                            weekStartDt
                    )
            );
        }

        if (!hasTarget) this.evictMyJournalDayWeeklyCaches(username);
    }
}
