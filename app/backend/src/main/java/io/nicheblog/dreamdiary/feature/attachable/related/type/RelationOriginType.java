package io.nicheblog.dreamdiary.feature.attachable.related.type;

import io.nicheblog.dreamdiary.global.type.LocalizedEnum;
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
public enum RelationOriginType implements LocalizedEnum {

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
