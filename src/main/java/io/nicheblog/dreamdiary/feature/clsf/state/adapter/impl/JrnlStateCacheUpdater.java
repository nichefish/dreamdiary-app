package io.nicheblog.dreamdiary.feature.clsf.state.adapter.impl;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.JrnlStateApplier;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.clsf.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JrnlStateCacheUpdater
 *
 * @author nichefish
 */
@Component
public class JrnlStateCacheUpdater
        implements StateCacheUpdater {

    /**
     * 지원 여부 반환
     * @param contentType ContentType
     * @return 지원 여부
     */
    @Override
    public boolean supports(final ContentType contentType) {
        return switch (contentType) {
            case JRNL_ENTRY,
                 JRNL_DIARY,
                 JRNL_DREAM,
                 JRNL_INTRPT -> true;
            default -> false;
        };
    }

    /**
     * 캐시 업데이트
     * @param toggle 전달된 toggle 객체
     * @param cacheKey 캐시 키
     * @param isEnabled 활성화 여부
     */
    public void update(final StateToggleDto toggle, final String cacheKey, final Boolean isEnabled) {
        final ContentType contentType = toggle.getContentType();

        this.updataCacheMap(toggle, cacheKey, isEnabled, contentType);

        final String evictCacheNm = this.getEvictCacheNm(contentType);
        if (evictCacheNm != null) {
            EhCacheUtils.clearMyCache(evictCacheNm);
        }
    }

    /**
     * 캐시 맵 업데이트
     * @param contentType 컨텐츠 타입
     * @param toggle 전달된 toggle 객체
     * @param cacheKey 캐시 키
     * @param isEnabled 활성화 여부
     */
    @SuppressWarnings("unchecked")
    private void updataCacheMap(StateToggleDto toggle, String cacheKey, Boolean isEnabled, ContentType contentType) {
        final String cacheMapNm = this.getCacheMapNm(contentType);

        final Map<Integer, JrnlState> map = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache(cacheMapNm, cacheKey);
        if (map == null) return;

        final JrnlState state = map.get(toggle.getPostNo());
        if (state == null) return;

        JrnlStateApplier.apply(state, toggle.getStateCd(), isEnabled);      // 같은 객체 수정. put 필요없음.
        EhCacheUtils.put(cacheMapNm, cacheKey, map);
    }

    private String getEvictCacheNm(final ContentType contentType) {
        return switch (contentType) {
            case JRNL_DIARY -> "jrnlDiaryYySumryStatedListByUser";
            case JRNL_DREAM -> "jrnlDreamYySumryStatedListByUser";
            default -> null;
        };
    }

    /**
     * 컨텐츠 타입별 캐시 이름 반환
     * @param contentType ContentType
     * @return 캐시 이름
     */
    private String getCacheMapNm(final ContentType contentType) {
        return switch (contentType) {
            case JRNL_ENTRY -> "jrnlEntryStateMapByUser";
            case JRNL_DIARY -> "jrnlDiaryStateMapByUser";
            case JRNL_DREAM -> "jrnlDreamStateMapByUser";
            case JRNL_INTRPT -> "jrnlIntrptStateMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }
}
