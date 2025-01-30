package io.nicheblog.dreamdiary.global._common.log.actvty;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * ActvtyCtgr
 * <pre>
 *  활동 카테고리 Enum.
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum ActvtyCtgr {

    // DEFAULT
    DEFAULT("DEFAULT"),
    SYSTEM("SYSTEM"),

    // API
    API("API"),
    API_JANDI("API_잔디"),
    API_KASI("API_특일"),
    API_DREAM("API_꿈 관리"),
    JANDI("잔디"),

    // ADMIN
    LGN("로그인"),
    ADMIN("관리"),
    CACHE("캐시"),

    // NOTICE
    NOTICE("공지사항"),

    // JRNL
    JRNL("꿈 관리"),
    BOARD_POST("게시판"),

    VCATN_PAPR("휴가계획서"),
    VCATN_SCHDUL("휴가사용일자"),
    VCATN_STATS("년도별 휴가관리"),

    TAG("태그"),

    // USER
    USER("사용자 관리"),
    USER_MY("내 정보 관리"),
    USER_REQST("신규계정 신청"),

    // MANAGE
    LGN_POLICY("로그인 정책 관리"),
    MENU("메뉴 관리"),
    BOARD_DEF("게시판 관리"),
    TMPLAT("템플릿 관리"),
    CD("코드 관리"),
    // LOG
    LOG_ACTVTY("활동 로그"),
    LOG_SYS("시스템 로그"),
    LOG_STATS("로그 통계"),
    // CMM
    FILE("파일"),
    FLSYS("파일 시스템"),

    // DEPRECATED
    SCHDUL("일정 관리"),
    NOTION("노션");

    public final String desc;
}
