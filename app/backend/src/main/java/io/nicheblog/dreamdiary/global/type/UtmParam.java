package io.nicheblog.dreamdiary.global.type;

import lombok.AllArgsConstructor;

/**
 * UtmParam
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum UtmParam {
    UTM_SOURCE("utm_source", "등록자"),
    UTM_MEDIUM("utm_medium", "관리자"),
    UTM_CAMPAIGN("utm_campaign", "사용자"),
    UTM_TERM("utm_term", "사용자"),
    UTM_CONTENT("utm_content", "전체");

    public final String key;
    public final String desc;
}
