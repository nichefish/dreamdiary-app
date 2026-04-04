package io.nicheblog.dreamdiary.feature.clsf.history;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HistoryType
 * <pre>
 *  이력 타입 구분값.
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum HistoryType {

    CHANGE("CHANGE"),
    RESTORE("RESTORE");

    public final String key;

    public static HistoryType from(final String value) {
        for (final HistoryType type : values()) {
            if (type.key.equalsIgnoreCase(value)) return type;
        }
        return CHANGE;
    }
}
