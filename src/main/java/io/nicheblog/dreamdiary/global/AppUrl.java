package io.nicheblog.dreamdiary.global;

/**
 * AppUrl
 * <pre>
 *  공통 상수 :: 웹사이트 페이지 URL 정의.
 * </pre>
 *
 * @author nichefish
 * @see Url
 */
public interface AppUrl {

    /** 로그인 관련 */
    String APP_AUTH_LGN_FORM = Prefix.APP + "/auth/lgn-form.do";

    /** 메인 */
    String ROOT = "/";
    String DEVTOOLS = ".well-known/appspecific/com.chrome.devtools.json";
    String MAIN = Prefix.APP + "/main.do";
    String REACT_MAIN = "/react/main.do";

    String ADMIN_MAIN = Prefix.APP + "/admin/main.do";
    String ADMIN_PAGE = Prefix.APP + "/admin/admin-page.do";
    String ADMIN_TEST = Prefix.APP + "/admin/test-page.do";
    String NOTION_HOME = Prefix.APP + "/notion-home.do";

    /** 저널 일자 (리스트) */
    String JOURNAL_DAY_MONTHLY = Prefix.APP + "/journal/day/monthly.do";
    String JOURNAL_DAY_WEEKLY = Prefix.APP + "/journal/day/weekly.do";
    String JOURNAL_DAY_META_VIEW = Prefix.APP + "/journal/day/meta.do";
    String JOURNAL_DAY_DAILY_VIEW_TODAY = Prefix.APP + "/journal/day.do";
    String JOURNAL_DAY_DAILY_VIEW = Prefix.APP + "/journal/day/{stdrdDt}.do";
    /** 저널 일자 (달력) */
    String JOURNAL_DAY_CAL = Prefix.APP + "/journal/day/cal.do";

    /** 저널 일기 (검색) */
    String JOURNAL_DIARY_SEARCH = Prefix.APP + "/journal/diary/search.do";
    /** 저널 꿈 (검색) */
    String JOURNAL_DREAM_SEARCH = Prefix.APP + "/journal/dream/search.do";

    /** 저널 주제 */
    String JOURNAL_SBJCT_LIST = Prefix.APP + "/journal/sbjct/list.do";
    String JOURNAL_SBJCT_REG_FORM = Prefix.APP + "/journal/sbjct/reg-form.do";
    String JOURNAL_SBJCT_REG_PREVIEW_POP = Prefix.APP + "/journal/sbjct/preview-pop.do";
    String JOURNAL_SBJCT_DTL = Prefix.APP + "/journal/sbjct/dtl.do";
    String JOURNAL_SBJCT_MDF_FORM = Prefix.APP + "/journal/sbjct/mdf-form.do";

    /** 저널 연간 */
    String JOURNAL_ANNUAL_LIST = Prefix.APP + "/journal/annual/list.do";
    String JOURNAL_ANNUAL_VIEW = Prefix.APP + "/journal/annual/{yy}.do";

    /** 공지사항 */
    String NOTICE_LIST = Prefix.APP + "/notice/list.do";
    String NOTICE_REG_FORM = Prefix.APP + "/notice/reg-form.do";
    String NOTICE_DTL = Prefix.APP + "/notice/dtl.do";
    String NOTICE_MDF_FORM = Prefix.APP + "/notice/mdf-form.do";
    String NOTICE_REG_PREVIEW_POP = Prefix.APP + "/notice/preview-pop.do";

    /** 게시판 */
    String BOARD_POST_LIST = Prefix.APP + "/board/post/list.do";
    String BOARD_POST_REG_FORM = Prefix.APP + "/board/post/reg-form.do";
    String BOARD_POST_DTL = Prefix.APP + "/board/post/dtl.do";
    String BOARD_POST_MDF_FORM = Prefix.APP + "/board/post/mdf-form.do";
    String BOARD_POST_REG_PREVIEW_POP = Prefix.APP + "/board/post/preview-pop.do";

    /** 사용자 관리 */
    String USER_LIST = Prefix.APP + "/user/list.do";
    String USER_REG_FORM = Prefix.APP + "/user/reg-form.do";
    String USER_DTL = Prefix.APP + "/user/dtl.do";
    String USER_MDF_FORM = Prefix.APP + "/user/mdf-form.do";

    /** 내 정보 관리 */
    String USER_MY_DTL = Prefix.APP + "/user/my/dtl.do";

    /** 태그 */
    String TAG_CLOUD_PAGE = Prefix.APP + "/tag/tag-cloud-page.do";

    /** 인증 정책 관리 */
    String AUTH_POLICY_FORM = Prefix.APP + "/auth/policy/form.do";

    /** 메뉴 관리 */
    String MENU_PAGE = Prefix.APP + "/menu/page.do";

    /** 게시판 관리 */
    String BOARD_DEF_LIST = Prefix.APP + "/board/def/list.do";

    /** 템플릿 관리 (TODO) */
    String TMPLAT_DEF_LIST = Prefix.APP + "/tmplat/list.do";
    String TMPLAT_DEF_DTL = Prefix.APP + "/tmplat/dtl.do";

    /** 팝업 관리 (TODO) */
    String POPUP_LIST = "";

    /** 코드 관리 */
    String CODE_GROUP_LIST = Prefix.APP + "/code/list.do";
    String CL_CD_LIST = CODE_GROUP_LIST; // backward compatibility

    /** 활동 로그 조회 */
    String LOG_ACTVTY_LIST = Prefix.APP + "/log/actvty/list.do";

    /** 시스템 로그 조회 */
    String LOG_SYS_LIST = Prefix.APP + "/log/sys/list.do";

    /** 로그 통계 조회 (TODO) */
    String LOG_STATS_USER_LIST = Prefix.APP + "/log/stats/list.do";

    /** 파일시스템 */
    String FLSYS_HOME = Prefix.APP + "/flsys/page.do";

    /** ERROR */
    // URL
    String ERROR = Prefix.APP + "/error";
    String ERROR_PAGE = Prefix.APP + "/error/error-page.do";
    String ERROR_NOT_FOUND = Prefix.APP + "/error/not-found.do";
    String ERROR_ACCESS_DENIED = Prefix.APP + "/error/access-denied.do";

    /* ---------- */

    String SCHEDULE_CAL = Prefix.APP + "/schedule/cal.do";

    String USER_REQST_REG_FORM = Prefix.APP + "/user/reqst/form.do";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String APP = "/app";
        String API = "/api";
    }
}

