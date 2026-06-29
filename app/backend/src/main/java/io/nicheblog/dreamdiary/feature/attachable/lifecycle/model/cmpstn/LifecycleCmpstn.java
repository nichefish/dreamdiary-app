package io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import lombok.*;

import java.io.Serializable;

/**
 * 화면 DTO에 포함되는 라이프사이클 조합 객체.
 *
 * <p>템플릿에서 {@code state}와 나란히 사용한다. 둘을 분리해 두면 JSON 구조만 봐도
 * 라이프사이클이 단일 현재값 축이라는 점이 드러난다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleCmpstn
        implements Serializable {

    private String lifecycleKey;

    private String lifecycleDesc;

    /**
     * 현재 라이프사이클 값이 주어진 키와 같은지 확인한다.
     *
     * @param key 비교할 라이프사이클 키
     * @return 현재 key와 같으면 {@code true}
     */
    public boolean is(final LifecycleKey key) {
        return key != null && key.key.equals(this.lifecycleKey);
    }

    /**
     * 열거형 값으로 라이프사이클 조합 객체를 만든다.
     *
     * @param key 라이프사이클 열거형 값
     * @return 라이프사이클 조합 객체. key가 없으면 {@code null}
     */
    public static LifecycleCmpstn of(final LifecycleKey key) {
        if (key == null) return null;
        return LifecycleCmpstn.builder()
                .lifecycleKey(key.key)
                .lifecycleDesc(key.getLabel())
                .build();
    }

    /**
     * 기본값인 {@code OPEN} 라이프사이클 조합 객체를 만든다.
     *
     * @return {@code OPEN} 라이프사이클 조합 객체
     */
    public static LifecycleCmpstn open() {
        return of(LifecycleKey.OPEN);
    }
}
