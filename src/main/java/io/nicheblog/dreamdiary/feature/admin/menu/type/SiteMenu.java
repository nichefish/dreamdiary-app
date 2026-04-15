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
    MENU("메뉴 관리"),
    CD("코드 관리"),

    CONTENT("컨텐츠 관리"),
    BOARD_DEF("게시판 관리"),
    TMPLAT("템플릿 관리"),
    POPUP("팝업 관리"),

    USER("사용자 관리"),
    USER_INFO("계정 관리"),
    USER_REQST("신규계정 신청"),
    USER_MY("내 정보"),

    NOTICE("공지사항"),

    JOURNAL("저널"),
    JOURNAL_DAY("저널 일자"),
    JOURNAL_CAL("저널 달력"),
    JOURNAL_SBJCT("저널 주제"),
    JOURNAL_ANNUAL("저널 연간"),

    BOARD("일반게시판"),

    SCHEDULE("일정"),
    SCHEDULE_CAL("일정 달력"),

    LOG("로그 관리"),
    LOG_ACTVTY("활동 로그 관리"),
    LOG_SYS("시스템 로그 관리"),
    LOG_STATS("로그 통계"),

    FLSYS("파일시스템");

    private final String pageNm;
}

