package io.nicheblog.dreamdiary.infrastructure.cd;

import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.auth.type.Mdfable;
import io.nicheblog.dreamdiary.feature.schdul.Schdul;
import io.nicheblog.dreamdiary.feature.user.emplym.Emplym;
import io.nicheblog.dreamdiary.feature.user.emplym.Rank;
import io.nicheblog.dreamdiary.global.type.Device;
import io.nicheblog.dreamdiary.global.type.UtmParam;

/**
 * Code
 * <pre>
 *  공통으로 사용하는 코드성 데이터 정의
 * </pre>
 *
 * @author nichefish
 */
public interface Code {

    /** 사용자 권한 코드 */
    String AUTH_CD = "AUTH_CD";

    /** AUTH */
    String AUTH_MNGR = Auth.MNGR.name();
    String AUTH_USER = Auth.USER.name();
    String AUTH_DEV = Auth.DEV.name();

    /** 수정 권한 코드 */
    String MDFABLE_CD = "MDFABLE_CD";

    /** MDFABLE */
    String MDFABLE_REGSTR = Mdfable.REGSTR.name();
    String MDFABLE_MNGR = Mdfable.MNGR.name();
    String MDFABLE_USER = Mdfable.USER.name();
    String MDFABLE_ALL = Mdfable.ALL.name();

    /** 디바이스 정보 */
    String DVC_PC = Device.PC.name();
    String DVC_MOBILE = Device.MOBILE.name();
    String DVC_TABLET = Device.TABLET.name();

    /** 분류 분류 코드 */
    String CL_CTGR_CD = "CL_CTGR_CD";

    /** 하위메뉴 확장 유형 코드 */
    String MENU_SUB_EXTEND_TY_CD = "MENU_SUB_EXTEND_TY_CD";

    /** 텍스트 클래스 코드 */
    String TEXT_CLASS_CD = "TEXT_CLASS_CD";

    /** 소속(팀) 코드 */
    String TEAM_CD = "TEAM_CD";

    /** 재직 구분 코드 */
    String EMPLYM_CD = "EMPLYM_CD";
    String EMPLYM_FREE = Emplym.FREE.name();

    String RANK_CD = "JOB_TITLE_CD";       // 직급 코드
    String RANK_STAFF = Rank.STAFF.name();           // 직급:사원

    String NOTICE_CTGR_CD = "NOTICE_CTGR_CD";   // 공지사항 글분류 코드
    String POST_CTGR_CD = "POST_CTGR_CD";       // 게시판
    String JANDI_TOPIC_CD = "JANDI_TOPIC_CD";   // 잔디 토픽 코드
    String JRNL_ENTRY_CTGR_CD = "JRNL_ENTRY_CTGR_CD";   // 저널 항목 글분류 코드
    String JRNL_SBJCT_CTGR_CD = "JRNL_SBJCT_CTGR_CD";   // 저널 주제 글분류 코드

    String YY_CD = "YY_CD";                 // 사용자 권한 코드
    String MNTH_CD = "MNTH_CD";                 // 사용자 권한 코드

    String ACTVTY_CTGR_CD = "ACTVTY_CTGR_CD";     // 작업 카테고리 코드
    String ACTION_TY_CD = "ACTION_TY_CD";         // 액션 유형 코드

    /* 메뉴 분류 코드 */
    String MENU_TY_CD = "MENU_TY_CD";
    String MENU_TY_MAIN = "MAIN";
    String MENU_TY_SUB = "SUB";

    /** 꿈 결산 구분 코드 */
    String JRNL_SUMRY_TY_CD = "JRNL_SUMRY_TY_CD";

    /** 일정 분류 코드 */
    String SCHDUL_CD = "SCHDUL_CD";       // 일정 구분 코드
    String SCHDUL_HLDY = Schdul.HLDY.name();
    String SCHDUL_CEREMONY = Schdul.CEREMONY.name();
    String SCHDUL_TLCMMT = Schdul.TLCMMT.name();
    String SCHDUL_OUTDT = Schdul.OUTDT.name();
    String SCHDUL_INDT = Schdul.INDT.name();
    String SCHDUL_VCATN = Schdul.VCATN.name();
    String SCHDUL_BRTHDY = Schdul.BRTHDY.name();
    String SCHDUL_ETC = Schdul.ETC.name();

    /** 활동 구분 코드 (로그) */
    String ACTION_TY_SEARCH = "SEARCH";
    String ACTION_TY_MY_PAPR = "MY_PAPR";
    String ACTION_TY_VIEW = "VIEW";
    String ACTION_TY_SUBMIT = "SUBMIT";
    String ACTION_TY_DOWNLOAD = "DOWNLOAD";

    /** UTM 파라미터 코드 */
    String UTM_SOURCE = UtmParam.UTM_SOURCE.key;
    String UTM_MEDIUM = UtmParam.UTM_MEDIUM.key;
    String UTM_CAMPAIGN = UtmParam.UTM_CAMPAIGN.key;
    String UTM_TERM = UtmParam.UTM_TERM.key;
    String UTM_CONTENT = UtmParam.UTM_CONTENT.key;

    /* 코드 정보 */

    /* 소속(회사) 코드 */
    String CMPY_CD = "CMPY_CD";
}
