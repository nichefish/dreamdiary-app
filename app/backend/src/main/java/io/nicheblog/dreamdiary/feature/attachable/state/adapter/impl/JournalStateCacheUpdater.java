package io.nicheblog.dreamdiary.feature.attachable.state.adapter.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.adapter.JournalStateApplier;
import io.nicheblog.dreamdiary.feature.attachable.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.attachable.state.model.CacheContext;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JournalStateCacheUpdater
 * 저널 상태 토글 요청에 따라 EhCache 등에 보관된 상태 맵을 갱신한다.
 *
 * @author nichefish
 */
@Component
public class JournalStateCacheUpdater
        implements StateCacheUpdater {

    @Override
    public boolean supports(final ContentType contentType) {
        return JournalStateCacheRegistry.supports(contentType);
    }

    public void update(final StateToggleDto toggle, final Boolean isEnabled) {
        final ContentType contentType = toggle.getContentType();
        final CacheContext cacheContext = toggle.getCacheContext();
        if (cacheContext == null) return;

        final String username = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(username)) return;

        this.updateMonthlyCacheMap(toggle, contentType, cacheContext, username, isEnabled);
        this.updateWeeklyCacheMap(toggle, contentType, cacheContext, username, isEnabled);

        final String evictCacheNm = JournalStateCacheRegistry.annualStateListCacheName(contentType);
        if (evictCacheNm != null) {
            EhCacheUtils.clearMyCache(evictCacheNm);
        }
    }

    private void updateMonthlyCacheMap(
            final StateToggleDto toggle,
            final ContentType contentType,
            final CacheContext cacheContext,
            final String username,
            final Boolean isEnabled
    ) {
        if (cacheContext.getYy() == null || cacheContext.getMnth() == null) return;

        final Object cacheKey = new SimpleKey(username, cacheContext.getYy(), cacheContext.getMnth());
        this.updateCacheMap(toggle, JournalStateCacheRegistry.monthlyMapCacheName(contentType), cacheKey, isEnabled);
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
        this.updateCacheMap(toggle, JournalStateCacheRegistry.weeklyMapCacheName(contentType), cacheKey, isEnabled);
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

        JournalState state = map.get(toggle.getId());
        if (state == null) {
            state = JournalState.builder().build();
            map.put(toggle.getId(), state);
        }

        JournalStateApplier.apply(state, toggle.getStateKey(), isEnabled);
        EhCacheUtils.put(cacheMapNm, cacheKey, map);
    }
}
