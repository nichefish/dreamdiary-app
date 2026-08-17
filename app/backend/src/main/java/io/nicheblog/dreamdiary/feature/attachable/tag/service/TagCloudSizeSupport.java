package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import io.nicheblog.dreamdiary.global.type.CloudSizeLock;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * TagCloudSizeSupport
 * <pre>
 *  태그클라우드 크기 클래스(ts-1~ts-9)와 프로필 크기 고정(CloudSizeLock) 적용.
 *  MAX 이면 빈도 산출과 무관하게 ts-9, MIN 이면 ts-1 로 고정한다. AUTO 는 빈도 산출 결과를 그대로 쓴다.
 *  Bootstrap fs-* 와 무관하다.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class TagCloudSizeSupport {

    /** 클라우드 크기 클래스 최소 (MIN 고정값·빈도 미산출 기본값) */
    public static final String SIZE_MIN_CLASS = "ts-1";

    /** 클라우드 크기 클래스 최대 (MAX 고정값) */
    public static final String SIZE_MAX_CLASS = "ts-9";

    /**
     * 크기 고정 상태를 빈도 산출 {@code tagClass} 에 적용한다.
     * MAX 는 {@link #SIZE_MAX_CLASS}, MIN 은 {@link #SIZE_MIN_CLASS} 로 고정하고,
     * AUTO(또는 {@code null})는 {@code tagClass} 를 그대로 두되 비어 있으면 {@link #SIZE_MIN_CLASS} 로 보정한다.
     *
     * @param tagClass 빈도 산출 결과 클래스
     * @param lock 프로필 크기 고정 상태 (AUTO/MIN/MAX, {@code null}=AUTO)
     * @return {@code ts-1}~{@code ts-9}
     */
    public static String applyCloudSizeLock(final String tagClass, final CloudSizeLock lock) {
        if (lock == CloudSizeLock.MAX) {
            return SIZE_MAX_CLASS;
        }
        if (lock == CloudSizeLock.MIN) {
            return SIZE_MIN_CLASS;
        }
        if (StringUtils.isBlank(tagClass)) {
            return SIZE_MIN_CLASS;
        }
        return tagClass;
    }
}
