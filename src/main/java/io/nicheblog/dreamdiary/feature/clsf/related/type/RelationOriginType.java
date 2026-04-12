package io.nicheblog.dreamdiary.feature.clsf.related.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RelationOriginType
 * <pre>
 *  관련글 생성 출처.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum RelationOriginType {

    MANUAL("MANUAL", "수동");

    public final String key;
    public final String desc;

    public static RelationOriginType from(final String value) {
        for (final RelationOriginType type : values()) {
            if (type.key.equalsIgnoreCase(value)) return type;
        }
        return MANUAL;
    }
}
