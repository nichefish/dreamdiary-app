package io.nicheblog.dreamdiary.feature.clsf.related.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RelationType
 * <pre>
 *  관련글 관계 타입.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum RelationType {

    REFERENCE("REFERENCE", "참조"),
    EXTENSION("EXTENSION", "확장"),
    PARALLEL("PARALLEL", "병렬"),
    CAUSE("CAUSE", "원인");

    public final String key;
    public final String desc;

    public static RelationType from(final String value) {
        for (final RelationType type : values()) {
            if (type.key.equalsIgnoreCase(value)) return type;
        }
        return REFERENCE;
    }
}
