package io.nicheblog.dreamdiary.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * TextClass
 * <pre>
 *  CSS class를 직접 저장하지 않고, 태그의 시각적 의미를 저장하는 enum.
 *  실제 Bootstrap text utility는 렌더링 시점에만 매핑한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum TextClass {

    DEFAULT("DEFAULT", ""),
    SUCCESS("SUCCESS", "text-success"),
    INFO("INFO", "text-account"),
    WARNING("WARNING",  "text-warning"),
    DANGER("DANGER", "text-danger"),
    PRIMARY("PRIMARY", "text-primary"),
    SECONDARY("SECONDARY", "text-secondary"),
    DARK("DARK", "text-dark"),
    MUTED("MUTED", "text-muted"),
    DIALOG("DIALOG", "text-dialog"),
    NOTI("NOTI", "text-noti"),
    EMOTION("EMOTION", "text-emotion"),
    BURNT("BURNT", "text-burnt");

    private final String key;
    private final String textClass;

    public static TextClass getOrDefault(final TextClass semantic) {
        return semantic == null ? DEFAULT : semantic;
    }

    public static TextClass fromCode(final String code) {
        if (StringUtils.isBlank(code)) return DEFAULT;

        if (StringUtils.equalsIgnoreCase("ALERT", code)) return DANGER;

        for (final TextClass semantic : values()) {
            if (StringUtils.equalsIgnoreCase(semantic.getKey(), code)) return semantic;
            if (StringUtils.equalsIgnoreCase(semantic.name(), code)) return semantic;
            if (StringUtils.equalsIgnoreCase(semantic.getTextClass(), code)) return semantic;
        }
        return DEFAULT;
    }
}
