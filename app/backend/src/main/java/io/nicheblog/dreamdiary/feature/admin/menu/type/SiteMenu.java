package io.nicheblog.dreamdiary.feature.admin.menu.type;

import lombok.RequiredArgsConstructor;

/**
 * SiteMenu
 * <pre>
 *  메뉴 라벨 정의 Enum
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum SiteMenu {

    LGN_PAGE("로그인"),
    MAIN("메인"),

    ADMIN_MAIN("메인"),
    ADMIN("사이트 관리"),
    ADMIN_PAGE("사이트 관리"),

    ERROR("에러"),

    AUTH_POLICY("인증 정책 관리"),
    MENU_ADMIN("메뉴 관리"),
    CODE_ADMIN("코드 관리"),

    CONTENT("컨텐츠 관리"),
    BOARD_ADMIN("게시판 관리"),
    POPUP("팝업 관리"),

    USER("사용자 관리"),
    USER_ACCOUNT("계정 관리"),
    USER_SIGNUP_APPROVAL("계정 신청 승인관리"),
    USER_SIGNUP("신규계정 신청"),
    USER_MY("내 정보"),

    JOURNAL("저널"),
    JOURNAL_DAY("저널 일자"),
    JOURNAL_CAL("저널 달력"),
    JOURNAL_THREAD("저널 스레드"),
    JOURNAL_ANNUAL("저널 연간"),

    BOARD("일반게시판"),

    SCHEDULE("일정"),
    SCHEDULE_CAL("일정 달력"),

    LOG("로그 관리"),
    LOG_LIST("로그 목록"),
    LOG_STATS("로그 통계");

    private final String pageName;
}
