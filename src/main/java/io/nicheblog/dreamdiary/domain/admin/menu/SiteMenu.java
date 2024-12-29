package io.nicheblog.dreamdiary.domain.admin.menu;

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

    LGN_POLICY("로그인 정책 관리"),
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

    JRNL("저널"),
    JRNL_DAY("저널 일자"),
    JRNL_CAL("저널 달력"),
    JRNL_SBJCT("저널 주제"),
    JRNL_SUMRY("저널 결산"),

    BOARD("일반게시판"),

    SCHDUL("일정"),
    SCHDUL_CAL("일정 달력"),
    VCATN_PAPR("휴가계획서"),
    VCATN_ADMIN("휴가 관리"),
    VCATN_SCHDUL("휴가사용일자 관리"),
    VCATN_STATS("년도별 휴가 관리"),

    LOG("로그 관리"),
    LOG_ACTVTY("활동 로그 관리"),
    LOG_SYS("시스템 로그 관리"),
    LOG_STATS("로그 통계"),

    TAG("태그"),

    FLSYS("파일시스템");

    private final String pageNm;
}