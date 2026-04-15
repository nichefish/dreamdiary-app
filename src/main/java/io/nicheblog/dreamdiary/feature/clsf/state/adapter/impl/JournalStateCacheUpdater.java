package io.nicheblog.dreamdiary.feature.clsf.state.adapter.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.JournalStateApplier;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.clsf.state.model.CacheContext;
import io.nicheblog.dreamdiary.feature.clsf.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JournalStateCacheUpdater
 *
 * @author nichefish
 */
@Component
public class JournalStateCacheUpdater
        implements StateCacheUpdater {

    /**
     * 지원 여부 반환
     * @param contentType ContentType
     * @return 지원 여부
     */
    @Override
    public boolean supports(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_CHAPTER,
                 JOURNAL_DIARY,
                 JOURNAL_DREAM,
                 JOURNAL_INTRPT -> true;
            default -> false;
        };
    }

    /**
     * 캐시 업데이트
     * @param toggle 전달된 toggle 객체
     * @param isEnabled 활성화 여부
     */
    public void update(final StateToggleDto toggle, final Boolean isEnabled) {
        final ContentType contentType = toggle.getContentType();
        final CacheContext cacheContext = toggle.getCacheContext();
        if (cacheContext == null) return;

        final String username = AuthUtils.getLgnUsername();
        if (StringUtils.isBlank(username)) return;

        this.updateMonthlyCacheMap(toggle, contentType, cacheContext, username, isEnabled);
        this.updateWeeklyCacheMap(toggle, contentType, cacheContext, username, isEnabled);

        final String evictCacheNm = this.getEvictCacheNm(contentType);
        if (evictCacheNm != null) {
            EhCacheUtils.clearMyCache(evictCacheNm);
        }
    }

    /**
     * 캐시 맵 업데이트
     * @param contentType 컨텐츠 타입
     * @param toggle 전달된 toggle 객체
     * @param isEnabled 활성화 여부
     */
    private void updateMonthlyCacheMap(
            final StateToggleDto toggle,
            final ContentType contentType,
            final CacheContext cacheContext,
            final String username,
            final Boolean isEnabled
    ) {
        if (cacheContext.getYy() == null || cacheContext.getMnth() == null) return;

        final Object cacheKey = new SimpleKey(username, cacheContext.getYy(), cacheContext.getMnth());
        this.updateCacheMap(toggle, this.getMonthlyCacheMapNm(contentType), cacheKey, isEnabled);
    }

    private void updateWeeklyCacheMap(
            final StateToggleDto toggle,
            final ContentType contentType,
            final CacheContext cacheContext,
            final String username,
            final Boolean isEnabled
    ) {
        if (StringUtils.isBlank(cacheContext.getWeekStartDt())) return;

        final Object cacheKey = new SimpleKey(username, cacheContext.getWeekStartDt());
        this.updateCacheMap(toggle, this.getWeeklyCacheMapNm(contentType), cacheKey, isEnabled);
    }

    private String getEvictCacheNm(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DIARY -> "journalDiaryYyAnnualStatedListByUser";
            case JOURNAL_DREAM -> "journalDreamYyAnnualStatedListByUser";
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private void updateCacheMap(
            final StateToggleDto toggle,
            final String cacheMapNm,
            final Object cacheKey,
            final Boolean isEnabled
    ) {
        final Map<Integer, JournalState> map = (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache(cacheMapNm, cacheKey);
        if (map == null) return;

        final JournalState state = map.get(toggle.getId());
        if (state == null) return;

        JournalStateApplier.apply(state, toggle.getStateCd(), isEnabled);      // 같은 객체 수정. put 필요없음.
        EhCacheUtils.put(cacheMapNm, cacheKey, map);
    }

    /**
     * 컨텐츠 타입별 캐시 이름 반환
     * @param contentType ContentType
     * @return 캐시 이름
     */
    private String getMonthlyCacheMapNm(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_CHAPTER -> "journalChapterStateMapByUser";
            case JOURNAL_DIARY -> "journalDiaryStateMapByUser";
            case JOURNAL_DREAM -> "journalDreamStateMapByUser";
            case JOURNAL_INTRPT -> "journalIntrptStateMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    /**
     * 컨텐츠 타입별 캐시 이름 반환
     * @param contentType ContentType
     * @return 캐시 이름
     */
    private String getWeeklyCacheMapNm(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_CHAPTER -> "journalChapterWeeklyStateMapByUser";
            case JOURNAL_DIARY -> "journalDiaryWeeklyStateMapByUser";
            case JOURNAL_DREAM -> "journalDreamWeeklyStateMapByUser";
            case JOURNAL_INTRPT -> "journalIntrptWeeklyStateMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }
}

