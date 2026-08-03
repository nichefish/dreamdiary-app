package io.nicheblog.dreamdiary.feature.attachable.lifecycle.adapter.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.adapter.LifecycleCacheUpdater;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleCacheRegistry;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 저널 일기 및 해석 화면용 라이프사이클 캐시 updater.
 *
 * <p>저널 목록 화면은 state를 컨텐츠 ID 기준 보조 맵으로 캐시한다.
 * 라이프사이클도 같은 캐시 구조를 따르되, 단일 현재값이므로 라이프사이클 키 문자열만 저장한다.</p>
 */
@Component
public class JournalLifecycleCacheUpdater
        implements LifecycleCacheUpdater {

    /**
     * 이 updater가 해당 컨텐츠 타입의 라이프사이클 캐시를 담당하는지 확인한다.
     *
     * @param contentType 변경된 컨텐츠 타입
     * @return 저널 라이프사이클 캐시 대상이면 {@code true}
     */
    @Override
    public boolean supports(final ContentType contentType) {
        return JournalLifecycleCacheRegistry.supports(contentType);
    }

    /**
     * 월간/주간 라이프사이클 맵을 갱신하고 연간 목록 캐시를 비운다.
     *
     * @param dto 라이프사이클 변경 요청
     * @param previousKey 변경 전 라이프사이클
     * @param currentKey 변경 후 라이프사이클
     */
    @Override
    public void update(final LifecycleSetDto dto, final LifecycleKey previousKey, final LifecycleKey currentKey) {
        final ContentType contentType = dto.getContentType();
        final AttachableCacheContext cacheContext = dto.getCacheContext();
        if (cacheContext == null) return;

        final String username = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(username)) return;

        this.updateMonthlyCacheMap(dto, contentType, cacheContext, username, currentKey);
        this.updateWeeklyCacheMap(dto, contentType, cacheContext, username, currentKey);

        final String evictCacheNm = JournalLifecycleCacheRegistry.annualLifecycleListCacheName(contentType);
        if (evictCacheNm != null) {
            EhCacheUtils.clearMyCache(evictCacheNm);
        }
    }

    /**
     * 년/월 컨텍스트가 있을 때 월간 라이프사이클 캐시 맵을 갱신한다.
     *
     * @param dto 라이프사이클 변경 요청
     * @param contentType 변경된 컨텐츠 타입
     * @param cacheContext 클라이언트가 전달한 저널 캐시 컨텍스트
     * @param username 캐시 목록 소유자
     * @param currentKey 변경 후 라이프사이클
     */
    private void updateMonthlyCacheMap(
            final LifecycleSetDto dto,
            final ContentType contentType,
            final AttachableCacheContext cacheContext,
            final String username,
            final LifecycleKey currentKey
    ) {
        if (cacheContext.getYy() == null || cacheContext.getMnth() == null) return;

        final Object cacheKey = new SimpleKey(username, cacheContext.getYy(), cacheContext.getMnth());
        this.updateCacheMap(dto, JournalLifecycleCacheRegistry.monthlyMapCacheName(contentType), cacheKey, currentKey);
    }

    /**
     * 주 시작일 컨텍스트가 있을 때 주간 라이프사이클 캐시 맵을 갱신한다.
     *
     * @param dto 라이프사이클 변경 요청
     * @param contentType 변경된 컨텐츠 타입
     * @param cacheContext 클라이언트가 전달한 저널 캐시 컨텍스트
     * @param username 캐시 목록 소유자
     * @param currentKey 변경 후 라이프사이클
     */
    private void updateWeeklyCacheMap(
            final LifecycleSetDto dto,
            final ContentType contentType,
            final AttachableCacheContext cacheContext,
            final String username,
            final LifecycleKey currentKey
    ) {
        if (StringUtils.isBlank(cacheContext.getWeekStartDt())) return;

        final Object cacheKey = new SimpleKey(username, cacheContext.getWeekStartDt());
        this.updateCacheMap(dto, JournalLifecycleCacheRegistry.weeklyMapCacheName(contentType), cacheKey, currentKey);
    }

    /**
     * 이미 존재하는 라이프사이클 보조 맵 캐시를 부분 갱신한다.
     * {@code OPEN}은 저장 row와 동일하게 맵 항목 부재로 표현한다.
     *
     * @param dto 라이프사이클 변경 요청
     * @param cacheMapNm {@code id -> lifecycleKey}를 저장하는 캐시명
     * @param cacheKey 사용자/날짜 기준 캐시 key
     * @param currentKey 변경 후 라이프사이클
     */
    @SuppressWarnings("unchecked")
    private void updateCacheMap(
            final LifecycleSetDto dto,
            final String cacheMapNm,
            final Object cacheKey,
            final LifecycleKey currentKey
    ) {
        final Map<Integer, String> map = (Map<Integer, String>) EhCacheUtils.getObjectFromCache(cacheMapNm, cacheKey);
        if (map == null) return;

        applyCurrentKey(map, dto.getId(), currentKey);
        EhCacheUtils.put(cacheMapNm, cacheKey, map);
    }

    /**
     * 라이프사이클 현재값을 보조 맵 저장 계약에 맞춰 적용한다.
     *
     * @param map 라이프사이클 보조 맵
     * @param refId 컨텐츠 ID
     * @param currentKey 현재 라이프사이클
     */
    static void applyCurrentKey(
            final Map<Integer, String> map,
            final Integer refId,
            final LifecycleKey currentKey
    ) {
        if (LifecycleKey.OPEN.equals(currentKey)) {
            map.remove(refId);
            return;
        }
        map.put(refId, currentKey.key);
    }
}
