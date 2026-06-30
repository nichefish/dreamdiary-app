package io.nicheblog.dreamdiary.infrastructure.code.model;

import lombok.*;

import java.util.Collections;
import java.util.Map;

/**
 * CodeLookupItem.
 * <pre>
 *  화면/캐시용 코드 상세 한 줄.
 *  {@code i18nNames} 에 locale → 번역명 매핑을 보관한다. (ko 기본값 제외)
 *  locale 에 맞는 번역이 없으면 {@code codeName}(한국어)을 fallback 으로 사용한다.
 * </pre>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeLookupItem {

    private Integer id;
    private String groupCode;
    private String code;
    /** 한국어 기본 코드명 */
    private String codeName;
    private String description;
    private Integer sortOrder;
    private String useYn;
    private String protectedYn;
    /** 다국어 번역명 (locale → codeName). ko 는 포함하지 않음. */
    @Builder.Default
    private Map<String, String> i18nNames = Collections.emptyMap();

    /**
     * locale 에 맞는 코드명을 반환한다.
     * 번역이 없으면 {@code codeName}(한국어)을 fallback 으로 반환한다.
     *
     * @param locale 언어 코드 (예: "en", "ko")
     * @return locale 에 맞는 코드명
     */
    public String getLocalizedCodeName(final String locale) {
        if (locale == null || "ko".equals(locale) || i18nNames == null) return codeName;
        final String translated = i18nNames.get(locale);
        return translated != null ? translated : codeName;
    }
}
