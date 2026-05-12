package io.nicheblog.dreamdiary.feature.attachable.lifecycle.adapter;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;

/**
 * 컨텐츠별 라이프사이클 캐시 갱신 전략 인터페이스.
 *
 * <p>라이프사이클 저장은 공통 서비스가 담당하고, 각 feature는 자신이 가진 목록 캐시를 어떻게
 * 부분 갱신하거나 비울지 알고 있으므로 updater로 분리한다.</p>
 */
public interface LifecycleCacheUpdater {

    /**
     * 이 updater가 해당 컨텐츠 타입을 처리할 수 있는지 확인한다.
     *
     * @param contentType 변경된 컨텐츠 타입
     * @return 이 updater가 캐시 경로를 담당하면 {@code true}
     */
    boolean supports(final ContentType contentType);

    /**
     * 라이프사이클 값 변경 후 feature 캐시를 갱신한다.
     *
     * @param dto 라이프사이클 변경 요청
     * @param previousKey 변경 전 라이프사이클
     * @param currentKey 변경 후 라이프사이클
     */
    void update(final LifecycleSetDto dto, final LifecycleKey previousKey, final LifecycleKey currentKey);
}
