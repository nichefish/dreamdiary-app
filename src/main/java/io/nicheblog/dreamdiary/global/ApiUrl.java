package io.nicheblog.dreamdiary.global;

/**
 * ApiUrl
 * <pre>
 *  공통 상수 :: 웹사이트 호출 Url 정의.
 * </pre>
 *
 * @author nichefish
 * @see Url
 */
public interface ApiUrl {

    String API_ALIVE_CHECK = Prefix.API + "/alive-check";

    /** 로그인 관련 */
    String API_AUTH_LGN_PROC = Prefix.API + "/auth/login-proc";
    String API_AUTH_LGN_PW_CHG = Prefix.API + "/auth/login-pw-chg";
    String API_AUTH_LGOUT = Prefix.API + "/auth/logout";
    String API_AUTH_REFRESH = Prefix.API + "/auth/refresh";
    String API_AUTH_EXPIRE_SESSION = Prefix.API + "/auth/expire-session";
    String API_AUTH_VERIFY = Prefix.API + "/auth/verify/{token}";
    String API_AUTH_INFO = Prefix.API + "/auth/get-auth-account";

    /** 소셜 로그인 팝업 */
    String OAUTH2_GOOGLE = "/oauth2/authorization/google";
    String OAUTH2_GOOGLE_REDIRECT_URI = "/login/oauth2/code/google";
    String OAUTH2_NAVER = "/oauth2/authorization/naver";
    String OAUTH2_NAVER_REDIRECT_URI = "/login/oauth2/code/naver";

    /** 저널 일자 */
    String JOURNAL_DAYS = Prefix.API + "/journal/days";
    String JOURNAL_DAY = Prefix.API + "/journal/day/{id}";
    /** 저널 일자 태그 */
    String JOURNAL_DAY_TAGS = Prefix.API + "/journal/day/tags";
    String JOURNAL_DAY_TAG_GROUP_LIST = Prefix.API + "/journal/day/tag/group-list";
    String JOURNAL_DAY_TAG = Prefix.API + "/journal/day/tag/{tagId}";
    String JOURNAL_DAY_TAG_YYS = Prefix.API + "/journal/day/tag/{tagId}/years";
    String JOURNAL_DAY_TAG_CTGR_MAP = Prefix.API + "/journal/day/tag/ctgr-map";
    /** 저널 일자 메타 */
    String JOURNAL_DAY_METAS = Prefix.API + "/journal/day/metas";
    String JOURNAL_DAY_META = Prefix.API + "/journal/day/metas/{id}";
    String JOURNAL_DAY_META_YYS = Prefix.API + "/journal/day/metas/{id}/years";
    String JOURNAL_DAY_META_CTGR_MAP = Prefix.API + "/journal/day/meta/ctgr-map";

    /** 저널 챕터 */
    String JOURNAL_CHAPTERS = Prefix.API + "/journal/chapters";
    /** 꿈(DREAM) 챕터 자동 생성 전용 (수동 챕터 등록 API로는 DREAM 생성 불가) */
    String JOURNAL_CHAPTER_DREAM_AUTO = Prefix.API + "/journal/chapters/dream-auto";
    String JOURNAL_CHAPTER = Prefix.API + "/journal/chapter/{id}";
    String JOURNAL_CHAPTER_EXPORT = Prefix.API + "/journal/chapter/{id}/export";

    /** 저널 엔트리(통합) */
    String JOURNAL_ENTRIES = Prefix.API + "/journal/entries";
    String JOURNAL_ENTRY = Prefix.API + "/journal/entry/{id}";
    String JOURNAL_ENTRIES_EXPORT = Prefix.API + "/journal/entries/export";
    /** 저널 엔트리 태그(통합) */
    String JOURNAL_ENTRY_TAGS = Prefix.API + "/journal/entry/tags";
    String JOURNAL_ENTRY_TAG_GROUP_LIST_AJAX = Prefix.API + "/journal/entry/tag/group-list";
    String JOURNAL_ENTRY_TAG_CTGR_MAP = Prefix.API + "/journal/entry/tag/ctgr-map";
    /** 저널 해석 */
    String JOURNAL_INTERPRETATIONS = Prefix.API + "/journal/interpretations";
    String JOURNAL_INTERPRETATION = Prefix.API + "/journal/interpretation/{id}";
    String JOURNAL_INTERPRETATION_SET_COLLAPSE_AJAX = Prefix.API + "/journal/interpretation/set-collapse";

    /** 저널 할일 */
    String JOURNAL_TODOS = Prefix.API + "/journal/todos";
    String JOURNAL_TODO = Prefix.API + "/journal/todo/{id}";

    /** 저널 주제 */
    String JOURNAL_SBJCT_REG_AJAX = Prefix.API + "/journal/sbjct/reg";
    String JOURNAL_SBJCT_DTL_AJAX = Prefix.API + "/journal/sbjct/dtl";
    String JOURNAL_SBJCT_MDF_AJAX = Prefix.API + "/journal/sbjct/mdf";
    String JOURNAL_SBJCT_DEL_AJAX = Prefix.API + "/journal/sbjct/del";

    /** 저널 연간 */
    String JOURNAL_ANNUALS = Prefix.API + "/journal/annuals";
    String JOURNAL_ANNUAL = Prefix.API + "/journal/annual/{yy}";
    String JOURNAL_ANNUAL_DIARIES = Prefix.API + "/journal/annual/{yy}/diaries";
    String JOURNAL_ANNUAL_DREAMS = Prefix.API + "/journal/annual/{yy}/dreams";
    String JOURNAL_ANNUAL_TAGS = Prefix.API + "/journal/annual/{yy}/tags";

    String JOURNAL_ANNUAL_MAKE_AJAX = Prefix.API + "/journal/annual/make";
    String JOURNAL_ANNUAL_MAKE_TOTAL_AJAX = Prefix.API + "/journal/annual/make-total";
    String JOURNAL_ANNUAL_DREAM_COMPT_AJAX = Prefix.API + "/journal/annual/dream-compt";
    String JOURNAL_ANNUAL_REG_AJAX = Prefix.API + "/journal/annual/reg";

    /** 저널 연간 리뷰 */
    String JOURNAL_ANNUAL_REVIEWS = Prefix.API + "/journal/annual/reviews";
    String JOURNAL_ANNUAL_REVIEW = Prefix.API + "/journal/annual/review/{id}";

    /** 공지사항 */
    String NOTICE_REG_AJAX = Prefix.API + "/notice/reg";
    String NOTICE = Prefix.API + "/notice/{id}";
    String NOTICE_MDF_AJAX = Prefix.API + "/notice/mdf";
    String NOTICE_POPUP_LIST_AJAX = Prefix.API + "/notice/popup-list";
    String NOTICE_LIST_XLSX_DOWNLOAD = Prefix.API + "/notice/list-xlsx-download.do";

    /** 게시판 */
    String BOARD_POST_REG_AJAX = Prefix.API + "/board/post/reg";
    String BOARD_POST_DTL_AJAX = Prefix.API + "/board/post/dtl";
    String BOARD_POST_MDF_AJAX = Prefix.API + "/board/post/mdf";
    String BOARD_POST_DEL_AJAX = Prefix.API + "/board/post/del";

    /** 사용자 관리 */
    String USER_REG_AJAX = Prefix.API + "/user/reg";
    String USER_MDF_AJAX = Prefix.API + "/user/mdf";
    String USER_PW_RESET_AJAX = Prefix.API + "/user/password-reset";
    String USER_DEL_AJAX = Prefix.API + "/user/del";
    String USER_LIST_XLSX_DOWNLOAD = Prefix.API + "/user/list-xlsx-download.do";
    String USERNAME_DUP_CHK_AJAX = Prefix.API + "/user/id-dup-chk";
    String USER_EMAIL_DUP_CHK_AJAX = Prefix.API + "/user/email-dup-chk";

    /** 내 정보 관리 */
    String USER_MY_UPLOAD_PROFL_IMG_AJAX = Prefix.API + "/user/my/upload-profl-img";
    String USER_MY_REMOVE_PROFL_IMG_AJAX = Prefix.API + "/user/my/remove-profl-img";
    String USER_MY_PW_CF_AJAX = Prefix.API + "/user/my/pw-cf";
    String USER_MY_PW_CHG_AJAX = Prefix.API + "/user/my/pw-chg";

    /** 댓글 */
    String COMMENTS = Prefix.API + "/comments";
    String COMMENT = Prefix.API + "/comment/{id}";

    /** 이력 */
    String HISTORIES = Prefix.API + "/history/{contentType}/{id}";
    String HISTORY = Prefix.API + "/history/{contentType}/{id}/{historyId}";
    String HISTORY_RESTORE = Prefix.API + "/history/{contentType}/{id}/{historyId}/restore";
    String HISTORY_CLEAR = Prefix.API + "/history/{contentType}/{id}/clear";

    /** 관련글 */
    String RELATEDS = Prefix.API + "/related/{contentType}/{id}";
    String RELATED = Prefix.API + "/related/{relatedContentId}";

    /** 단락 */
    /** 상태 */
    String STATES = Prefix.API + "/states";

    /** 태그 */
    String TAGS = Prefix.API + "/tags";
    String TAG_DTL_AJAX = Prefix.API + "/tag/tag-dtl";
    String TAG_PROFILES = Prefix.API + "/tag/profiles";
    String TAG_PROFILE = Prefix.API + "/tag-profile/{id}";

    /** 인증 정책 관리 */
    String AUTH_POLICY_REG_AJAX = Prefix.API + "/login-policy/reg";

    /** 메뉴 관리 */
    String MENU_MAIN_LIST_AJAX = Prefix.API + "/menu/menu-main-list";
    String MENUS = Prefix.API + "/menus";
    String MENUS_SORT_ORDERS = Prefix.API + "/menus/sort-orders";
    String MENUS_TREE = Prefix.API + "/menus/tree";
    String MENU = Prefix.API + "/menu/{id}";

    /** 게시판 관리 */
    String BOARD_GROUP_REG_AJAX = Prefix.API + "/board/group/board-reg";
    String BOARD_GROUP_DTL_AJAX = Prefix.API + "/board/group/board-dtl";
    String BOARD_GROUP_MDF_ITEM_AJAX = Prefix.API + "/board/group/board-mdf-item";
    String BOARD_GROUP_DEL_AJAX = Prefix.API + "/board/group/board-del";
    String BOARD_GROUP_USE_AJAX = Prefix.API + "/board/group/board-use";
    String BOARD_GROUP_UNUSE_AJAX = Prefix.API + "/board/group/board-unuse";
    String BOARD_GROUP_SORT_ORDR_AJAX = Prefix.API + "/board/group/board-sort-ordr";

    /** 템플릿 관리 (TODO) */
    String TMPLAT_DEF_REG_AJAX = Prefix.API + "/tmplat/tmplat-def-reg";
    String TMPLAT_DEF_DTL_AJAX = Prefix.API + "/tmplat/tmplat-def-dtl";
    String TMPLAT_DEF_MDF_AJAX = Prefix.API + "/tmplat/tmplat-def-mdf";
    String TMPLAT_DEF_DEL_AJAX = Prefix.API + "/tmplat/tmplat-def-del";

    String TMPLAT_TXT_REG_AJAX = Prefix.API + "/tmplat/tmplat-txt-reg";
    String TMPLAT_TXT_MDF_AJAX = Prefix.API + "/tmplat/tmplat-txt-mdf";

    /** 팝업 관리 (TODO) */
    String POPUP_LIST = "";

    /** 코드 관리 */
    String CODE_GROUPS = Prefix.API + "/code/groups";
    String CODE_GROUP = Prefix.API + "/code/group/{id}";
    String CODE_ITEMS = Prefix.API + "/code/items";
    String CODE_ITEM = Prefix.API + "/code/item";
    String CODE_ITEMS_SORT_ORDERS = Prefix.API + "/code/items/sort-orders";
    String CODE_ITEM_USE = Prefix.API + "/code/item/use";
    String CODE_ITEM_UNUSE = Prefix.API + "/code/item/unuse";

    /** 로그 조회 */
    String LOG_DTL_AJAX = Prefix.API + "/log/dtl";
    String LOG_LIST_XLSX_DOWNLOAD = Prefix.API + "/log/list-xlsx-download.do";

    /** (공통) 파일 */
    String FILE_DOWNLOAD_CHK_AJAX = Prefix.API + "/file/file-download-chk";
    String FILE_INFO_LIST_AJAX = Prefix.API + "/file/file-account-list";
    String FILE_DOWNLOAD = Prefix.API + "/file/file-download.do";
    String FILE_UPLOAD_AJAX = Prefix.API + "/file/file-upload";

    /** (공통) 캐시 관리 */
    String CACHE_ACTIVE_MAP_AJAX = Prefix.API + "/cache/cache-active-map";
    String CACHE_ACTIVE_DTL_AJAX = Prefix.API + "/cache/cache-active-dtl";
    String CACHE_EVICT_AJAX = Prefix.API + "/cache/cache-evict";
    String CACHE_CLEAR_BY_NM_AJAX = Prefix.API + "/cache/cache-clear-by-nm";
    String CACHE_CLEAR_AJAX = Prefix.API + "/cache-clear";

    /* ---------- */

    String SCHEDULE_CAL_LIST_AJAX = Prefix.API + "/schedule/cal-list";
    String SCHEDULE_REG_AJAX = Prefix.API + "/schedule/cal-reg";
    String SCHEDULE_DTL_AJAX = Prefix.API + "/schedule/cal-dtl";
    String SCHEDULE_MDF_AJAX = Prefix.API + "/schedule/cal-mdf";
    String SCHEDULE_DEL_AJAX = Prefix.API + "/schedule/cal-del";

    String USER_REQST_REG_AJAX = Prefix.API + "/user/reqst/reqst-reg";
    String USER_REQST_CF_AJAX = Prefix.API + "/user/reqst/reqst-cf";
    String USER_REQST_UNCF_AJAX = Prefix.API + "/user/reqst/reqst-uncf";

    String JANDI_CONNECT_WH = "https://wh.jandi.com/connect-api/webhook";

    /** (API) 한국천문연구원 : 특일 정보 조회 */
    String API_HOLYDAY_GET = Prefix.API + "/holyday/get-holyday-account.do";
    
    /** (API) SNMP : 메세지 발신 */
    String URL_API_SNMP_SEND_AJAX = Prefix.API +"/snmp/send";

    /** (API) JANDI : 메세지 송수신 */
    String API_JANDI_SND_MSG = Prefix.API + "/jandi/send-msg.do";
    String API_JANDI_RCV_MSG = Prefix.API + "/jandi/receive-msg.do";

    /** (API) NOTION */
    String API_NOTION_GET = Prefix.API + "/notion/notion.do";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String APP = "/app";
        String API = "/api";
    }
}
