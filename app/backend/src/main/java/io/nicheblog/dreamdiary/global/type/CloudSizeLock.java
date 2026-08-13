package io.nicheblog.dreamdiary.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * CloudSizeLock
 * <pre>
 *  태그클라우드 크기 고정 상태를 저장하는 enum.
 *  빈도 산출 결과를 덮어 클라우드 크기 클래스(ts-1~ts-9)를 고정할지 결정한다.
 *  {@code AUTO} 는 빈도 산출을 그대로 쓰고, {@code MIN}/{@code MAX} 는 각각 최소/최대로 고정한다.
 *  실제 크기 클래스 매핑은 렌더링 시점에 {@link io.nicheblog.dreamdiary.feature.attachable.tag.service.TagCloudSizeSupport} 가 담당한다.
 *  Bootstrap fs-* 와 무관하다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum CloudSizeLock {

    /** 빈도 산출 결과를 그대로 사용 (고정 없음) */
    AUTO("AUTO"),
    /** 빈도와 무관하게 최소 크기로 고정 */
    MIN("MIN"),
    /** 빈도와 무관하게 최대 크기로 고정 */
    MAX("MAX");

    private final String key;

    /**
     * {@code null} 이면 {@link #AUTO} 로 정규화한다.
     *
     * @param lock 정규화 대상
     * @return {@code null} 은 {@code AUTO}, 그 외는 원본
     */
    public static CloudSizeLock getOrDefault(final CloudSizeLock lock) {
        return lock == null ? AUTO : lock;
    }

    /**
     * 코드 문자열을 enum 으로 변환한다. 매칭 실패·공백은 {@link #AUTO}.
     *
     * @param code 코드 문자열 (AUTO/MIN/MAX)
     * @return 매칭 enum, 실패 시 {@code AUTO}
     */
    public static CloudSizeLock fromCode(final String code) {
        if (StringUtils.isBlank(code)) return AUTO;
        for (final CloudSizeLock lock : values()) {
            if (StringUtils.equalsIgnoreCase(lock.getKey(), code)) return lock;
            if (StringUtils.equalsIgnoreCase(lock.name(), code)) return lock;
        }
        return AUTO;
    }
}
