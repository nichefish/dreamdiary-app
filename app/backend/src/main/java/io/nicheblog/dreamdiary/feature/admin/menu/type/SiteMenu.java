package io.nicheblog.dreamdiary.feature.admin.menu.type;

/**
 * SiteMenu
 * <pre>
 *  컨트롤러 ↔ 메뉴 매핑 키 정의 Enum.
 *  상수명이 곧 {@code menu.menu_label} 값이며, {@code MenuService.getMenuByLabel()} 이
 *  {@code name()} 으로 해당 메뉴 행을 찾는다. 상수를 추가·개명하면 메뉴 시드
 *  (data-required-menu-mariadb.sql)의 menu_label 과 반드시 함께 맞춘다.
 *
 *  변경 전: {@code implements LocalizedEnum} 과 한글 {@code pageName} 필드를 갖고 있었다.
 *  그러나 {@code pageName} 은 getter 가 없어 읽을 수 없는 죽은 필드였고,
 *  {@code getLabel()}(→ {@code enum.site-menu.*}) 도 호출부가 없어 죽은 번역이었다.
 *  변경 후: 매핑 키 역할만 남긴다. 메뉴 표시명은 {@code menu.menu_name}(DB) 이 단일 원천이며
 *  번역은 메시지 번들이 아니라 메뉴 데이터 쪽에서 관리한다.
 * </pre>
 *
 * @author nichefish
 */
public enum SiteMenu {

    LGN_PAGE,
    MAIN,

    ADMIN_MAIN,
    ADMIN,
    ADMIN_PAGE,

    ERROR,

    AUTH_POLICY,
    MENU_ADMIN,
    CODE_ADMIN,

    CONTENT,
    BOARD_ADMIN,
    POPUP,

    USER,
    USER_ACCOUNT,
    USER_SIGNUP_APPROVAL,
    USER_SIGNUP,
    USER_MY,

    JOURNAL,
    JOURNAL_DAY,
    JOURNAL_CAL,
    JOURNAL_THREAD,
    JOURNAL_ANNUAL,

    BOARD,

    SCHEDULE,
    SCHEDULE_CAL,

    LOG,
    LOG_LIST,
    LOG_STATS
}
