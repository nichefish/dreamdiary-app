package io.nicheblog.dreamdiary.extension.clsf.state.adapter.impl;

import io.nicheblog.dreamdiary.domain.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.state.adapter.JrnlStateApplier;
import io.nicheblog.dreamdiary.extension.clsf.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.extension.clsf.state.model.StateToggleDto;
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
            case JRNL_DAY,
                 JRNL_ENTRY,
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
    @SuppressWarnings("unchecked")
    public void update(final StateToggleDto toggle, final String cacheKey, final Boolean isEnabled) {
        final ContentType contentType = toggle.getContentType();
        final String cacheNm = this.getCacheNm(contentType);

        final Map<Integer, JrnlState> map = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache(cacheNm, cacheKey);
        if (map == null) return;

        final JrnlState state = map.get(toggle.getPostNo());
        if (state == null) return;

        JrnlStateApplier.apply(state, toggle.getStateCd(), isEnabled);      // 같은 객체 수정. put 필요없음.
        EhCacheUtils.put(cacheNm, cacheKey, map);
    }
    
    /**
     * 컨텐츠 타입별 캐시 이름 반환
     * @param contentType ContentType
     * @return 캐시 이름
     */
    private String getCacheNm(final ContentType contentType) {
        return switch (contentType) {
            case JRNL_DAY -> "myDayStateMap";
            case JRNL_ENTRY -> "myEntryStateMap";
            case JRNL_DIARY -> "myDiaryStateMap";
            case JRNL_DREAM -> "myDreamStateMap";
            case JRNL_INTRPT -> "myIntrptStateMap";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

}
