package io.nicheblog.dreamdiary.feature.clsf.state.adapter;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.state.model.StateToggleDto;

/**
 * StateCacheUpdater
 *
 * @author nichefish
 */
public interface StateCacheUpdater {

    /**
     * 지원 여부 반환
     * @param contentType ContentType
     * @return 지원 여부
     */
    boolean supports(final ContentType contentType);

    /**
     * 캐시 업데이트
     * @param toggle 전달된 toggle 객체
     * @param isEnabled 활성화 여부
     */
    void update(final StateToggleDto toggle, final Boolean isEnabled);
}
