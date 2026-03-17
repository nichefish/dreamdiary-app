package io.nicheblog.dreamdiary.domain.clsf.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * StateCd
 * <pre>
 *  상태 코드 정보
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum StateCd {

    RESOLVED("RESOLVED", "정리완료"),
    COLLAPSED("COLLAPSED", "접기"),
    IMPRTC("IMPRTC", "중요"),
    REFRNC("REFRNC", "참조");

    public final String key;
    public final String desc;
    public String icon;

    /**
     * 키와 일치하는 컨텐츠 타입 enum 반환
     * @param contentType 문자열
     * @return ContentType enum
     */
    public static StateCd get(final String contentType) {
        for (final StateCd type : StateCd.values()) {
            if (type.key.equals(contentType)) return type;
        }
        return null;
    }
}
