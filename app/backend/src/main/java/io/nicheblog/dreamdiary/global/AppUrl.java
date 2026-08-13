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
    String APP_AUTH_LGN_FORM = Prefix.APP + "/auth/login-form.do";
    /** Vue SPA 로그인 화면 URL */
    String VUE_SIGN_IN = "/vue-app/sign-in";

    /** 메인 */
    String ROOT = "/";
    String DEVTOOLS = ".well-known/appspecific/com.chrome.devtools.json";
    String MAIN = Prefix.APP + "/main.do";
    String REACT_MAIN = "/react/main.do";

    String ADMIN_MAIN = Prefix.APP + "/admin/main.do";
    String ADMIN_PAGE = Prefix.APP + "/admin/admin-page.do";
    /** 저널 일자 (리스트) */
    String JOURNAL_DAY_MONTHLY = Prefix.APP + "/journal/day/monthly.do";
    String JOURNAL_DAY_WEEKLY = Prefix.APP + "/journal/day/weekly.do";
    String JOURNAL_DAY_META_VIEW = Prefix.APP + "/journal/day/meta.do";
    String JOURNAL_DAY_DAILY_VIEW_TODAY = Prefix.APP + "/journal/day.do";
    String JOURNAL_DAY_DAILY_VIEW = Prefix.APP + "/journal/day/{stdrdDt}.do";
    /** 저널 일자 (달력) */
    String JOURNAL_DAY_CAL = Prefix.APP + "/journal/day/cal.do";

    /** 저널 스레드 */
    String JOURNAL_THREAD_LIST = Prefix.APP + "/journal/thread/list.do";
    String JOURNAL_THREAD_REGIST_FORM = Prefix.APP + "/journal/thread/regist-form.do";
    String JOURNAL_THREAD_REGIST_PREVIEW_POP = Prefix.APP + "/journal/thread/preview-pop.do";
    String JOURNAL_THREAD_DETAIL = Prefix.APP + "/journal/thread/detail.do";
    String JOURNAL_THREAD_MODIFY_FORM = Prefix.APP + "/journal/thread/modify-form.do";

    /** 저널 연간 */
    String JOURNAL_ANNUAL_LIST = Prefix.APP + "/journal/annual/list.do";
    String JOURNAL_ANNUAL_VIEW = Prefix.APP + "/journal/annual/{yy}.do";

    /** 게시판 */
    String BOARD_POST_LIST = Prefix.APP + "/board/post/list.do";
    String BOARD_POST_REGIST_FORM = Prefix.APP + "/board/post/regist-form.do";
    String BOARD_POST_DETAIL = Prefix.APP + "/board/post/detail.do";
    String BOARD_POST_MODIFY_FORM = Prefix.APP + "/board/post/modify-form.do";
    String BOARD_POST_REGIST_PREVIEW_POP = Prefix.APP + "/board/post/preview-pop.do";

    /** 사용자 관리 */
    String USER_LIST = Prefix.APP + "/user/list.do";
    String USER_SIGNUP_LIST = Prefix.APP + "/user/signup/list.do";
    String USER_REGIST_FORM = Prefix.APP + "/user/regist-form.do";
    String USER_DETAIL = Prefix.APP + "/user/detail.do";
    String USER_MODIFY_FORM = Prefix.APP + "/user/modify-form.do";

    /** 내 정보 관리 */
    String USER_MY_PAGE = Prefix.APP + "/user/my/page.do";

    /** 태그 */
    String TAG_CLOUD_PAGE = Prefix.APP + "/tag/tag-cloud-page.do";

    /** 인증 정책 관리 화면 (싱글톤 설정 1건, MVC는 타 페이지와 동일하게 *.do) */
    String AUTH_POLICY_PAGE = Prefix.APP + "/auth/policy/page.do";

    /** 사용자 그룹 관리 화면 */
    String USER_GROUP_PAGE = Prefix.APP + "/user/group/page.do";

    /** 메뉴 관리 */
    String MENU_ADMIN_PAGE = Prefix.APP + "/admin/menu/page.do";

    /** 게시판 관리 */
    String BOARD_ADMIN_PAGE = Prefix.APP + "/admin/board/page.do";

    /** 팝업 관리 (TODO) */
    String POPUP_LIST = "";

    /** 코드 관리 */
    String CODE_ADMIN_PAGE = Prefix.APP + "/admin/code/page.do";
    String TMPLAT_ADMIN_PAGE = Prefix.APP + "/admin/tmplat/page.do";

    /** 로그 조회 */
    String LOG_LIST = Prefix.APP + "/log/list.do";

    /** 로그 통계 조회 (TODO) */
    String LOG_STATS_USER_LIST = Prefix.APP + "/log/stats/list.do";

    /** ERROR */
    // URL
    String ERROR = Prefix.APP + "/error";
    String ERROR_PAGE = Prefix.APP + "/error/error-page.do";
    String ERROR_NOT_FOUND = Prefix.APP + "/error/not-found.do";
    String ERROR_ACCESS_DENIED = Prefix.APP + "/error/access-denied.do";

    /* ---------- */

    /** 일정 달력 화면 진입 (Vue SPA 리다이렉트 대상) */
    String SCHEDULE_CAL = Prefix.APP + "/schedule/calendar.do";
    /** 레거시 북마크·알림 링크 호환 */
    String SCHEDULE_CAL_LEGACY = Prefix.APP + "/schedule/cal.do";

    String USER_SIGNUP_PAGE = Prefix.APP + "/user/signup/page.do";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String APP = "/app";
        String API = "/api";
    }
}
