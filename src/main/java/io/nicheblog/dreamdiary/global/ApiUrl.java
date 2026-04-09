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
    String API_AUTH_LGN_PROC = Prefix.API + "/auth/lgn-proc";
    String API_AUTH_LGN_PW_CHG = Prefix.API + "/auth/lgn-pw-chg";
    String API_AUTH_LGOUT = Prefix.API + "/auth/lgout";
    String API_AUTH_REFRESH = Prefix.API + "/auth/refresh";
    String API_AUTH_EXPIRE_SESSION = Prefix.API + "/auth/expire-session";
    String API_AUTH_VERIFY = Prefix.API + "/auth/verify/{token}";
    String API_AUTH_INFO = Prefix.API + "/auth/get-auth-info";

    /** 소셜 로그인 팝업 */
    String OAUTH2_GOOGLE = "/oauth2/authorization/google";
    String OAUTH2_GOOGLE_REDIRECT_URI = "/login/oauth2/code/google";
    String OAUTH2_NAVER = "/oauth2/authorization/naver";
    String OAUTH2_NAVER_REDIRECT_URI = "/login/oauth2/code/naver";

    /** 저널 일자 */
    String JRNL_DAYS = Prefix.API + "/jrnl/days";
    String JRNL_DAY = Prefix.API + "/jrnl/day/{postNo}";
    /** 저널 일자 태그 */
    String JRNL_DAY_TAGS = Prefix.API + "/jrnl/day/tags";
    String JRNL_DAY_TAG_GROUP_LIST = Prefix.API + "/jrnl/day/tag/group-list";
    String JRNL_DAY_TAG = Prefix.API + "/jrnl/day/tag/{tagNo}";
    String JRNL_DAY_TAG_YYS = Prefix.API + "/jrnl/day/tag/{tagNo}/years";
    String JRNL_DAY_TAG_CTGR_MAP = Prefix.API + "/jrnl/day/tag/ctgr-map";
    /** 저널 일자 메타 */
    String JRNL_DAY_METAS = Prefix.API + "/jrnl/day/metas";
    String JRNL_DAY_META = Prefix.API + "/jrnl/day/metas/{metaNo}";
    String JRNL_DAY_META_YYS = Prefix.API + "/jrnl/day/metas/{metaNo}/years";
    String JRNL_DAY_META_CTGR_MAP = Prefix.API + "/jrnl/day/meta/ctgr-map";

    /** 저널 꿈 */
    String JRNL_DREAMS = Prefix.API + "/jrnl/dreams";
    String JRNL_DREAM = Prefix.API + "/jrnl/dream/{postNo}";
    String JRNL_DREAMS_EXPORT = Prefix.API + "/jrnl/dreams/export";
    /** 저널 꿈 태그 */
    String JRNL_DREAM_TAGS = Prefix.API + "/jrnl/dream/tags";
    String JRNL_DREAM_TAG_GROUP_LIST_AJAX = Prefix.API + "/jrnl/dream/tag-group-list";
    String JRNL_DREAM_TAG_CTGR_MAP = Prefix.API + "/jrnl/dream/tag/ctgr-map";
    /** 저널 해석 */
    String JRNL_INTRPTS = Prefix.API + "/jrnl/intrpts";
    String JRNL_INTRPT = Prefix.API + "/jrnl/intrpt/{postNo}";
    String JRNL_INTRPT_SET_COLLAPSE_AJAX = Prefix.API + "/jrnl/intrpt/set-collapse";
    /** 저널 해석 태그 */
    String JRNL_INTRPT_TAGS = Prefix.API + "/jrnl/intrpt/tags";
    String JRNL_INTRPT_TAG_GROUP_LIST_AJAX = Prefix.API + "/jrnl/intrpt/tag/group-list";
    String JRNL_INTRPT_TAG = Prefix.API + "/jrnl/intrpt/tag/{tagNo}";
    String JRNL_INTRPT_TAG_CTGR_MAP = Prefix.API + "/jrnl/intrpt/tag/ctgr-map";

    /** 저널 항목 */
    String JRNL_ENTRIES = Prefix.API + "/jrnl/entries";
    String JRNL_ENTRY = Prefix.API + "/jrnl/entry/{postNo}";
    String JRNL_ENTRY_EXPORT = Prefix.API + "/jrnl/entry/{postNo}/export";

    /** 저널 일기 */
    String JRNL_DIARIES = Prefix.API + "/jrnl/diaries";
    String JRNL_DIARY = Prefix.API + "/jrnl/diary/{postNo}";
    String JRNL_DIARIES_EXPORT = Prefix.API + "/jrnl/diaries/export";
    /** 저널 일기 태그 */
    String JRNL_DIARY_TAGS = Prefix.API + "/jrnl/diary/tags";
    String JRNL_DIARY_TAG_GROUP_LIST_AJAX = Prefix.API + "/jrnl/diary/tag/group-list";
    String JRNL_DIARY_TAG_CTGR_MAP = Prefix.API + "/jrnl/diary/tag/ctgr-map";

    /** 저널 할일 */
    String JRNL_TODOS = Prefix.API + "/jrnl/todos";
    String JRNL_TODO = Prefix.API + "/jrnl/todo/{postNo}";

    /** 저널 주제 */
    String JRNL_SBJCT_REG_AJAX = Prefix.API + "/jrnl/sbjct/reg";
    String JRNL_SBJCT_DTL_AJAX = Prefix.API + "/jrnl/sbjct/dtl";
    String JRNL_SBJCT_MDF_AJAX = Prefix.API + "/jrnl/sbjct/mdf";
    String JRNL_SBJCT_DEL_AJAX = Prefix.API + "/jrnl/sbjct/del";

    /** 저널 결산 */
    String JRNL_SUMRIES = Prefix.API + "/jrnl/sumries";
    String JRNL_SUMRY = Prefix.API + "/jrnl/sumry/{yy}";
    String JRNL_SUMRY_DIARIES = Prefix.API + "/jrnl/sumry/{yy}/diaries";
    String JRNL_SUMRY_DREAMS = Prefix.API + "/jrnl/sumry/{yy}/dreams";
    String JRNL_SUMRY_TAGS = Prefix.API + "/jrnl/sumry/{yy}/tags";

    String JRNL_SUMRY_MAKE_AJAX = Prefix.API + "/jrnl/sumry/make";
    String JRNL_SUMRY_MAKE_TOTAL_AJAX = Prefix.API + "/jrnl/sumry/make-total";
    String JRNL_SUMRY_DREAM_COMPT_AJAX = Prefix.API + "/jrnl/sumry/dream-compt";
    String JRNL_SUMRY_REG_AJAX = Prefix.API + "/jrnl/sumry/reg";

    /** 저널 결산 리뷰 */
    String JRNL_SUMRY_REVIEWS = Prefix.API + "/jrnl/sumry/reviews";
    String JRNL_SUMRY_REVIEW = Prefix.API + "/jrnl/sumry/review/{postNo}";

    /** 공지사항 */
    String NOTICE_REG_AJAX = Prefix.API + "/notice/reg";
    String NOTICE = Prefix.API + "/notice/{postNo}";
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
    String USER_ID_DUP_CHK_AJAX = Prefix.API + "/user/id-dup-chk";
    String USER_EMAIL_DUP_CHK_AJAX = Prefix.API + "/user/email-dup-chk";

    /** 내 정보 관리 */
    String USER_MY_UPLOAD_PROFL_IMG_AJAX = Prefix.API + "/user/my/upload-profl-img";
    String USER_MY_REMOVE_PROFL_IMG_AJAX = Prefix.API + "/user/my/remove-profl-img";
    String USER_MY_PW_CF_AJAX = Prefix.API + "/user/my/pw-cf";
    String USER_MY_PW_CHG_AJAX = Prefix.API + "/user/my/pw-chg";

    /** 댓글 */
    String COMMENTS = Prefix.API + "/comments";
    String COMMENT = Prefix.API + "/comment/{postNo}";

    /** 이력 */
    String HISTORIES = Prefix.API + "/history/{contentType}/{postNo}";
    String HISTORY = Prefix.API + "/history/{contentType}/{postNo}/{historyNo}";
    String HISTORY_RESTORE = Prefix.API + "/history/{contentType}/{postNo}/{historyNo}/restore";
    String HISTORY_CLEAR = Prefix.API + "/history/{contentType}/{postNo}/clear";

    /** 단락 */
    String SECTNS = Prefix.API + "/sectns";
    String SECTN = Prefix.API + "/sectn/{postNo}";
    String SECTN_SORT_ORDR = Prefix.API + "/sectn/sort-ordr";

    /** 상태 */
    String STATES = Prefix.API + "/states";

    /** 태그 */
    String TAGS = Prefix.API + "/tags";
    String TAG_DTL_AJAX = Prefix.API + "/tag/tag-dtl";
    String TAG_PROFILES = Prefix.API + "/tag/profiles";
    String TAG_PROFILE = Prefix.API + "/tag-profile/{tagProfileNo}";

    /** 태그 속성 */
    String TAG_PROPERTY_REG_AJAX = Prefix.API + "/tag-property/tag-property-reg";
    String TAG_PROPERTY_DTL_AJAX = Prefix.API + "/tag-property/tag-property-dtl";
    String TAG_PROPERTY_MDF_AJAX = Prefix.API + "/tag-property/tag-property-mdf";
    String TAG_PROPERTY_DEL_AJAX = Prefix.API + "/tag-property/tag-property-del";

    /** 인증 정책 관리 */
    String AUTH_POLICY_REG_AJAX = Prefix.API + "/lgn-policy/reg";

    /** 메뉴 관리 */
    String MENU_MAIN_LIST_AJAX = Prefix.API + "/menu/menu-main-list";
    String MENUS = Prefix.API + "/menus";
    String MENUS_IDX = Prefix.API + "/menus/idx";
    String MENUS_TREE = Prefix.API + "/menus/tree";
    String MENU = Prefix.API + "/menu/{menuNo}";

    /** 게시판 관리 */
    String BOARD_DEF_REG_AJAX = Prefix.API + "/board/def/board-def-reg";
    String BOARD_DEF_DTL_AJAX = Prefix.API + "/board/def/board-def-dtl";
    String BOARD_DEF_MDF_ITEM_AJAX = Prefix.API + "/board/def/board-def-mdf-item";
    String BOARD_DEF_DEL_AJAX = Prefix.API + "/board/def/board-def-del";
    String BOARD_DEF_USE_AJAX = Prefix.API + "/board/def/board-def-use";
    String BOARD_DEF_UNUSE_AJAX = Prefix.API + "/board/def/board-def-unuse";
    String BOARD_DEF_SORT_ORDR_AJAX = Prefix.API + "/board/def/board-def-sort-ordr";

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
    String CD_CLS = Prefix.API + "cd/cls";
    String CD_CL = Prefix.API + "/cd/cl/{clCd}";

    String DTL_CD_REG_AJAX = Prefix.API + "/cd/dtl-cd-reg";
    String DTL_CD_DTL_AJAX = Prefix.API + "/cd/dtl-cd-dtl";
    String DTL_CD_MDF_AJAX = Prefix.API + "/cd/dtl-cd-mdf";
    String DTL_CD_LIST_AJAX = Prefix.API + "/cd/dtl-cd-list";
    String DTL_CD_USE_AJAX = Prefix.API + "/cd/dtl-cd-use";
    String DTL_CD_UNUSE_AJAX = Prefix.API + "/cd/dtl-cd-unuse";
    String DTL_CD_DEL_AJAX = Prefix.API + "/cd/dtl-cd-del";
    String DTL_CD_SORT_ORDR_AJAX = Prefix.API + "/cd/dtl-cd-sort-ordr";
    
    /** 활동 로그 조회 */
    String LOG_ACTVTY_DTL_AJAX = Prefix.API + "/log/actvty/log-actvty-dtl";

    /** 시스템 로그 조회 */
    String LOG_SYS_DTL_AJAX = Prefix.API + "/log/sys/log-sys-dtl";

    /** 파일시스템 */
    String FLSYS_LIST_AJAX = Prefix.API + "/flsys/flsys-list";
    String FLSYS_FILE_DOWNLOAD = Prefix.API + "/flsys/flsys-file-download.do";
    String FLSYS_OPEN_IN_EXPLORER_AJAX = Prefix.API + "/flsys/flsys-open-in-explorer";
    String FLSYS_FILE_EXEC_AJAX = Prefix.API + "/flsys/flsys-file-exec";

    String FLSYS_META_REG_AJAX = Prefix.API + "/flsys/flsys-meta-reg";
    String FLSYS_META_DTL_AJAX = Prefix.API + "/flsys/flsys-meta-dtl";
    String FLSYS_META_MDF_AJAX = Prefix.API + "/flsys/flsys-meta-mdf";
    String FLSYS_META_DEL_AJAX = Prefix.API + "/flsys/flsys-meta-del";

    /** (공통) 파일 */
    String FILE_DOWNLOAD_CHK_AJAX = Prefix.API + "/file/file-download-chk";
    String FILE_INFO_LIST_AJAX = Prefix.API + "/file/file-info-list";
    String FILE_DOWNLOAD = Prefix.API + "/file/file-download.do";
    String FILE_UPLOAD_AJAX = Prefix.API + "/file/file-upload";

    /** (공통) 캐시 관리 */
    String CACHE_ACTIVE_MAP_AJAX = Prefix.API + "/cache/cache-active-map";
    String CACHE_ACTIVE_DTL_AJAX = Prefix.API + "/cache/cache-active-dtl";
    String CACHE_EVICT_AJAX = Prefix.API + "/cache/cache-evict";
    String CACHE_CLEAR_BY_NM_AJAX = Prefix.API + "/cache/cache-clear-by-nm";
    String CACHE_CLEAR_AJAX = Prefix.API + "/cache-clear";

    /* ---------- */

    String SCHDUL_CAL_LIST_AJAX = Prefix.API + "/schdul/cal-list";
    String SCHDUL_REG_AJAX = Prefix.API + "/schdul/cal-reg";
    String SCHDUL_DTL_AJAX = Prefix.API + "/schdul/cal-dtl";
    String SCHDUL_MDF_AJAX = Prefix.API + "/schdul/cal-mdf";
    String SCHDUL_DEL_AJAX = Prefix.API + "/schdul/cal-del";

    String USER_REQST_REG_AJAX = Prefix.API + "/user/reqst/reqst-reg";
    String USER_REQST_CF_AJAX = Prefix.API + "/user/reqst/reqst-cf";
    String USER_REQST_UNCF_AJAX = Prefix.API + "/user/reqst/reqst-uncf";

    String JANDI_CONNECT_WH = "https://wh.jandi.com/connect-api/webhook";

    /** (API) 한국천문연구원 : 특일 정보 조회 */
    String API_HLDY_GET = Prefix.API + "/hldy/get-hldy-info.do";
    
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
