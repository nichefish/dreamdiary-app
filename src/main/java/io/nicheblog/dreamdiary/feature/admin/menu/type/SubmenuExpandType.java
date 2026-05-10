package io.nicheblog.dreamdiary.feature.admin.menu.type;

import lombok.AllArgsConstructor;

/**
 * SubmenuExpandType
 * 관리자 메뉴에서 하위 항목을 어떻게 펼칠지(확장·목록·없음) 정의한다.
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum SubmenuExpandType {
    EXTEND("우측으로 확장"),
    LIST("하단에 목록 표시"),
    NO_SUB("하위메뉴 없음"),
    COLLAPSE("글접기"),
    BOARD("일반게시판");

    public final String desc;

    public static String getDesc(final String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return SubmenuExpandType.valueOf(code).desc;
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
