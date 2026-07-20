package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * TagCloudSizeSupport
 * <pre>
 *  태그클라우드 크기 클래스(ts-1~ts-9)와 프로필 forceMax 적용.
 *  forceMax 가 true 이면 빈도 산출과 무관하게 ts-9 로 고정한다.
 *  Bootstrap fs-* 와 무관하다.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class TagCloudSizeSupport {

    /** 클라우드 크기 클래스 최대 (forceMax 시 고정값) */
    public static final String SIZE_MAX_CLASS = "ts-9";

    /**
     * forceMax 이면 {@link #SIZE_MAX_CLASS}, 아니면 빈도 산출 {@code tagClass} 를 그대로 반환한다.
     *
     * @param tagClass 빈도 산출 결과 클래스
     * @param forceMax 프로필 최대 고정 여부
     * @return {@code ts-1}~{@code ts-9}
     */
    public static String applyForceMax(final String tagClass, final Boolean forceMax) {
        if (Boolean.TRUE.equals(forceMax)) {
            return SIZE_MAX_CLASS;
        }
        if (StringUtils.isBlank(tagClass)) {
            return "ts-1";
        }
        return tagClass;
    }
}
