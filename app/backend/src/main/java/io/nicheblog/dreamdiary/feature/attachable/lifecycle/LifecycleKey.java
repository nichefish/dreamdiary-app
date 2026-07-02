package io.nicheblog.dreamdiary.feature.attachable.lifecycle;

import io.nicheblog.dreamdiary.global.type.LocalizedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 부착 가능 컨텐츠의 라이프사이클 키.
 *
 * <p>{@code StateKey}와 달리 라이프사이클은 단일 현재값이다.
 * 컨텐츠 하나는 한 시점에 {@code OPEN}, {@code PENDING}, {@code RESOLVED} 중 하나로 해석된다.</p>
 */
@Getter
@RequiredArgsConstructor
public enum LifecycleKey implements LocalizedEnum {

    OPEN("OPEN", "열림"),
    PENDING("PENDING", "보류"),
    RESOLVED("RESOLVED", "완료");

    public final String key;
    public final String desc;

    /**
     * 저장된 라이프사이클 키를 열거형 값으로 변환한다.
     *
     * @param key 저장된 라이프사이클 키
     * @return 일치하는 {@link LifecycleKey}. 없으면 {@code null}
     */
    public static LifecycleKey getByKey(final String key) {
        if (key == null) return null;
        for (final LifecycleKey lifecycleKey : LifecycleKey.values()) {
            if (lifecycleKey.key.equals(key)) return lifecycleKey;
        }
        return null;
    }
}
