package io.nicheblog.dreamdiary.feature.attachable.state.adapter;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;

/**
 * StateCacheUpdater
 * 콘텐츠 타입별로 상태 캐시 갱신 로직을 분리하기 위한 SPI.
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
