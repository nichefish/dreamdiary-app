package io.nicheblog.dreamdiary.global;

/**
 * SiteUrl
 * <pre>
 *  공통 상수 :: 웹사이트 URL 정의.
 * </pre>
 *
 * @author nichefish
 * @see Url
 */
public interface SiteUrl {

    /** 메인 */
    String ROOT = "/";
    String MAIN = "/main.do";
    String REACT_MAIN = "/react/main.do";

    String ADMIN_MAIN = Prefix.ADMIN + MAIN;
    String TEST_PAGE = Prefix.ADMIN + "/test-page.do";

    String ALIVE_CHECK_AJAX = "/alive-check-ajax.do";

    /** 저널 일자 */
    String JRNL_DAY_PAGE = Prefix.JRNL_DAY + "/jrnl-day-page.do";
    String JRNL_DAY_LIST_AJAX = Prefix.JRNL_DAY + "/jrnl-day-list-ajax.do";
    String JRNL_DAY_CAL_LIST_AJAX = Prefix.JRNL_DAY + "/jrnl-day-cal-list-ajax.do";
    String JRNL_DAY_REG_AJAX = Prefix.JRNL_DAY + "/jrnl-day-reg-ajax.do";
    String JRNL_DAY_DTL_AJAX = Prefix.JRNL_DAY + "/jrnl-day-dtl-ajax.do";
    String JRNL_DAY_MDF_AJAX = Prefix.JRNL_DAY + "/jrnl-day-mdf-ajax.do";
    String JRNL_DAY_DEL_AJAX = Prefix.JRNL_DAY + "/jrnl-day-del-ajax.do";
    /** 저널 일자 달력 */
    String JRNL_DAY_CAL = Prefix.JRNL_DAY + "/jrnl-day-cal.do";
    /** 저널 일자 태그 */
    String JRNL_DAY_TAG_LIST_AJAX = Prefix.JRNL_DAY + "/jrnl-day-tag-list-ajax.do";
    String JRNL_DAY_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DAY + "/jrnl-day-tag-group-list-ajax.do";
    String JRNL_DAY_TAG_DTL_AJAX = Prefix.JRNL_DAY + "/jrnl-day-tag-dtl-ajax.do";
    String JRNL_DAY_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DAY + "/jrnl-day-tag-ctgr-map-ajax.do";

    /** 저널 꿈 */
    String JRNL_DREAM_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-list-ajax.do";
    String JRNL_DREAM_REG_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-reg-ajax.do";
    String JRNL_DREAM_DTL_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-dtl-ajax.do";
    String JRNL_DREAM_MDF_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-mdf-ajax.do";
    String JRNL_DREAM_DEL_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-del-ajax.do";
    String JRNL_DREAM_RESOLVE_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-resolve-ajax.do";
    String JRNL_DREAM_SET_COLLAPSE_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-set-collapse-ajax.do";
    /** 저널 꿈 태그 */
    String JRNL_DREAM_TAG_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-tag-list-ajax.do";
    String JRNL_DREAM_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-tag-group-list-ajax.do";
    String JRNL_DREAM_TAG_DTL_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-tag-dtl-ajax.do";
    String JRNL_DREAM_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DREAM + "/jrnl-dream-tag-ctgr-map-ajax.do";
    /** 저널 해석 */
    String JRNL_INTRPT_LIST_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-list-ajax.do";
    String JRNL_INTRPT_REG_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-reg-ajax.do";
    String JRNL_INTRPT_DTL_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-dtl-ajax.do";
    String JRNL_INTRPT_MDF_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-mdf-ajax.do";
    String JRNL_INTRPT_DEL_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-del-ajax.do";
    String JRNL_INTRPT_RESOLVE_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-resolve-ajax.do";
    String JRNL_INTRPT_SET_COLLAPSE_AJAX = Prefix.JRNL_DREAM + "/jrnl-intrpt-set-collapse-ajax.do";
    /** 저널 해석 태그 */
    String JRNL_INTRPT_TAG_LIST_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-tag-list-ajax.do";
    String JRNL_INTRPT_TAG_GROUP_LIST_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-tag-group-list-ajax.do";
    String JRNL_INTRPT_TAG_DTL_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-tag-dtl-ajax.do";
    String JRNL_INTRPT_TAG_CTGR_MAP_AJAX = Prefix.JRNL_INTRPT + "/jrnl-intrpt-tag-ctgr-map-ajax.do";

    /** 저널 항목 */
    String JRNL_ENTRY_LIST_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-list-ajax.do";
    String JRNL_ENTRY_REG_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-reg-ajax.do";
    String JRNL_ENTRY_DTL_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-dtl-ajax.do";
    String JRNL_ENTRY_MDF_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-mdf-ajax.do";
    String JRNL_ENTRY_DEL_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-del-ajax.do";
    /** 저널 항목 태그 */
    String JRNL_ENTRY_TAG_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnl-entry-tag-list-ajax.do";
    String JRNL_ENTRY_TAG_GROUP_LIST_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-tag-group-list-ajax.do";
    String JRNL_ENTRY_TAG_DTL_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-tag-dtl-ajax.do";
    String JRNL_ENTRY_TAG_CTGR_MAP_AJAX = Prefix.JRNL_ENTRY + "/jrnl-entry-tag-ctgr-map-ajax.do";

    /** 저널 일기 */
    String JRNL_DIARY_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-list-ajax.do";
    String JRNL_DIARY_REG_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-reg-ajax.do";
    String JRNL_DIARY_DTL_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-dtl-ajax.do";
    String JRNL_DIARY_MDF_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-mdf-ajax.do";
    String JRNL_DIARY_DEL_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-del-ajax.do";
    String JRNL_DIARY_IMPRTC_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-imprtc-ajax.do";
    String JRNL_DIARY_RESOLVE_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-resolve-ajax.do";
    String JRNL_DIARY_SET_COLLAPSE_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-set-collapse-ajax.do";
    String JRNL_DIARY_SET_COLLAPSE_BY_ENTRY_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-set-collapse-by-entry-ajax.do";
    /** 저널 일기 태그 */
    String JRNL_DIARY_TAG_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-tag-list-ajax.do";
    String JRNL_DIARY_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-tag-group-list-ajax.do";
    String JRNL_DIARY_TAG_DTL_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-tag-dtl-ajax.do";
    String JRNL_DIARY_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DIARY + "/jrnl-diary-tag-ctgr-map-ajax.do";

    /** 저널 할일 */
    String JRNL_TODO_LIST_AJAX = Prefix.JRNL_TODO + "/jrnl-todo-list-ajax.do";
    String JRNL_TODO_REG_AJAX = Prefix.JRNL_TODO + "/jrnl-todo-reg-ajax.do";
    String JRNL_TODO_DTL_AJAX = Prefix.JRNL_TODO + "/jrnl-todo-dtl-ajax.do";
    String JRNL_TODO_MDF_AJAX = Prefix.JRNL_TODO + "/jrnl-todo-mdf-ajax.do";
    String JRNL_TODO_DEL_AJAX = Prefix.JRNL_TODO + "/jrnl-todo-del-ajax.do";

    /** 저널 주제 */
    String JRNL_SBJCT_LIST = Prefix.JRNL_SBJCT + "/jrnl-sbjct-list.do";
    String JRNL_SBJCT_REG_FORM = Prefix.JRNL_SBJCT + "/jrnl-sbjct-reg-form.do";
    String JRNL_SBJCT_REG_AJAX = Prefix.JRNL_SBJCT + "/jrnl-sbjct-reg-ajax.do";
    String JRNL_SBJCT_REG_PREVIEW_POP = Prefix.JRNL_SBJCT + "/jrnl-sbjct-preview-pop.do";

    String JRNL_SBJCT_DTL = Prefix.JRNL_SBJCT + "/jrnl-sbjct-dtl.do";
    String JRNL_SBJCT_DTL_AJAX = Prefix.JRNL_SBJCT + "/jrnl-sbjct-dtl-ajax.do";
    String JRNL_SBJCT_MDF_FORM = Prefix.JRNL_SBJCT + "/jrnl-sbjct-mdf-form.do";
    String JRNL_SBJCT_MDF_AJAX = Prefix.JRNL_SBJCT + "/jrnl-sbjct-mdf-ajax.do";
    String JRNL_SBJCT_DEL_AJAX = Prefix.JRNL_SBJCT + "/jrnl-sbjct-del-ajax.do";

    /** 저널 결산 */
    String JRNL_SUMRY_LIST = Prefix.JRNL_SUMRY + "/jrnl-sumry-list.do";
    String JRNL_SUMRY_LIST_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-list-ajax.do";
    String JRNL_SUMRY_DTL = Prefix.JRNL_SUMRY + "/jrnl-sumry-dtl.do";
    String JRNL_SUMRY_DTL_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-dtl-ajax.do";
    String JRNL_SUMRY_MAKE_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-make-ajax.do";
    String JRNL_SUMRY_MAKE_TOTAL_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-make-total-ajax.do";
    String JRNL_SUMRY_DREAM_COMPT_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-dream-compt-ajax.do";
    String JRNL_SUMRY_REG_AJAX = Prefix.JRNL_SUMRY + "/jrnl-sumry-reg-ajax.do";

    /** 공지사항 */
    String NOTICE_LIST = Prefix.NOTICE + "/notice-list.do";
    String NOTICE_REG_FORM = Prefix.NOTICE + "/notice-reg-form.do";
    String NOTICE_REG_AJAX = Prefix.NOTICE + "/notice-reg-ajax.do";
    String NOTICE_DTL = Prefix.NOTICE + "/notice-dtl.do";
    String NOTICE_DTL_AJAX = Prefix.NOTICE + "/notice-dtl-ajax.do";
    String NOTICE_MDF_FORM = Prefix.NOTICE + "/notice-mdf-form.do";
    String NOTICE_MDF_AJAX = Prefix.NOTICE + "/notice-mdf-ajax.do";
    String NOTICE_DEL_AJAX = Prefix.NOTICE + "/notice-del-ajax.do";
    String NOTICE_REG_PREVIEW_POP = Prefix.NOTICE + "/notice-reg-preview-pop.do";
    String NOTICE_POPUP_LIST_AJAX = Prefix.NOTICE + "/notice-popup-list-ajax.do";
    String NOTICE_LIST_XLSX_DOWNLOAD = Prefix.NOTICE + "/notice-list-xlsx-download.do";

    /** 게시판 */
    String BOARD_POST_LIST = Prefix.BOARD_POST + "/board-post-list.do";
    String BOARD_POST_REG_FORM = Prefix.BOARD_POST + "/board-post-reg-form.do";
    String BOARD_POST_REG_AJAX = Prefix.BOARD_POST + "/board-post-reg-ajax.do";
    String BOARD_POST_DTL = Prefix.BOARD_POST + "/board-post-dtl.do";
    String BOARD_POST_DTL_AJAX = Prefix.BOARD_POST + "/board-post-dtl-ajax.do";
    String BOARD_POST_MDF_FORM = Prefix.BOARD_POST + "/board-post-mdf-form.do";
    String BOARD_POST_MDF_AJAX = Prefix.BOARD_POST + "/board-post-mdf-ajax.do";
    String BOARD_POST_DEL_AJAX = Prefix.BOARD_POST + "/board-post-del-ajax.do";
    String BOARD_POST_REG_PREVIEW_POP = Prefix.BOARD_POST + "/board-post-reg-preview-pop.do";

    /** 사용자 관리 */
    String USER_LIST = Prefix.USER + "/user-list.do";
    String USER_REG_FORM = Prefix.USER + "/user-reg-form.do";
    String USER_REG_AJAX = Prefix.USER + "/user-reg-ajax.do";
    String USER_DTL = Prefix.USER + "/user-dtl.do";
    String USER_MDF_FORM = Prefix.USER + "/user-mdf-form.do";
    String USER_MDF_AJAX = Prefix.USER + "/user-mdf-ajax.do";
    String USER_PW_RESET_AJAX = Prefix.USER + "/user-password-reset-ajax.do";
    String USER_DEL_AJAX = Prefix.USER + "/user-del-ajax.do";
    String USER_LIST_XLSX_DOWNLOAD = Prefix.USER + "/user-list-xlsx-download.do";
    String USER_ID_DUP_CHK_AJAX = Prefix.USER + "/user-id-dup-chk-ajax.do";
    String USER_EMAIL_DUP_CHK_AJAX = Prefix.USER + "/user-email-dup-chk-ajax.do";

    /** 내 정보 관리 */
    String USER_MY_DTL = Prefix.USER_MY + "/user-my-dtl.do";
    String USER_MY_UPLOAD_PROFL_IMG_AJAX = Prefix.USER_MY + "/user-my-upload-profl-img-ajax.do";
    String USER_MY_REMOVE_PROFL_IMG_AJAX = Prefix.USER_MY + "/user-my-remove-profl-img-ajax.do";
    String USER_MY_PW_CF_AJAX = Prefix.USER_MY + "/user-my-pw-cf-ajax.do";
    String USER_MY_PW_CHG_AJAX = Prefix.USER_MY + "/user-my-pw-chg-ajax.do";

    /** 댓글 */
    String COMMENT_LIST_AJAX = Prefix.COMMENT + "/comment-list-ajax.do";
    String COMMENT_REG_AJAX = Prefix.COMMENT + "/comment-reg-ajax.do";
    String COMMENT_DTL_AJAX = Prefix.COMMENT + "/comment-dtl-ajax.do";
    String COMMENT_MDF_AJAX = Prefix.COMMENT + "/comment-mdf-ajax.do";
    String COMMENT_DEL_AJAX = Prefix.COMMENT + "/comment-del-ajax.do";

    /** 단락 */
    String SECTN_LIST_AJAX = Prefix.SECTN + "/sectn-list-ajax.do";
    String SECTN_REG_AJAX = Prefix.SECTN + "/sectn-reg-ajax.do";
    String SECTN_MDF_AJAX = Prefix.SECTN + "/sectn-mdf-ajax.do";
    String SECTN_DTL_AJAX = Prefix.SECTN + "/sectn-dtl-ajax.do";
    String SECTN_DEL_AJAX = Prefix.SECTN + "/sectn-del-ajax.do";
    String SECTN_SORT_ORDR_AJAX = Prefix.SECTN + "/sectn-sort-ordr-ajax.do";

    /** 태그 */
    String TAG_LIST = Prefix.TAG + "/tag-list.do";
    String TAG_CLOUD_PAGE = Prefix.TAG + "/tag-cloud-page.do";
    String TAG_LIST_AJAX = Prefix.TAG + "/tag-list-ajax.do";
    String TAG_DTL_AJAX = Prefix.TAG + "/tag-dtl-ajax.do";

    /** 태그 속성 */
    String TAG_PROPERTY_REG_AJAX = Prefix.TAG_PROPERTY + "/tag-property-reg-ajax.do";
    String TAG_PROPERTY_DTL_AJAX = Prefix.TAG_PROPERTY + "/tag-property-dtl-ajax.do";
    String TAG_PROPERTY_MDF_AJAX = Prefix.TAG_PROPERTY + "/tag-property-mdf-ajax.do";
    String TAG_PROPERTY_DEL_AJAX = Prefix.TAG_PROPERTY + "/tag-property-del-ajax.do";

    /** 로그인 정책 관리 */
    String LGN_POLICY_FORM = Prefix.LGN_POLICY + "/lgn-policy-form.do";
    String LGN_POLICY_REG_AJAX = Prefix.LGN_POLICY + "/lgn-policy-reg-ajax.do";

    /** 메뉴 관리 */
    String MENU_PAGE = Prefix.MENU + "/menu-page.do";
    String MENU_MAIN_LIST_AJAX = Prefix.MENU + "/menu-main-list-ajax.do";
    String MENU_REG_AJAX = Prefix.MENU + "/menu-list.do";
    String MENU_DTL_AJAX = Prefix.MENU + "/menu-dtl-ajax.do";
    String MENU_MDF_AJAX = Prefix.MENU + "/menu-mdf-ajax.do";
    String MENU_DEL_AJAX = Prefix.MENU + "/menu-del-ajax.do";
    String MENU_SORT_ORDR_AJAX = Prefix.MENU + "/menu-sort-ordr-ajax.do";

    /** 게시판 관리 */
    String BOARD_DEF_LIST = Prefix.BOARD_DEF + "/board-def-list.do";
    String BOARD_DEF_REG_AJAX = Prefix.BOARD_DEF + "/board-def-reg-ajax.do";
    String BOARD_DEF_DTL_AJAX = Prefix.BOARD_DEF + "/board-def-dtl-ajax.do";
    String BOARD_DEF_MDF_ITEM_AJAX = Prefix.BOARD_DEF + "/board-def-mdf-item-ajax.do";
    String BOARD_DEF_DEL_AJAX = Prefix.BOARD_DEF + "/board-def-del-ajax.do";
    String BOARD_DEF_USE_AJAX = Prefix.BOARD_DEF + "/board-def-use-ajax.do";
    String BOARD_DEF_UNUSE_AJAX = Prefix.BOARD_DEF + "/board-def-unuse-ajax.do";
    String BOARD_DEF_SORT_ORDR_AJAX = Prefix.MENU + "/board-def-sort-ordr-ajax.do";

    /** 템플릿 관리 (TODO) */
    String TMPLAT_DEF_LIST = Prefix.TMPLAT + "/tmplat-def-list.do";
    String TMPLAT_DEF_REG_AJAX = Prefix.TMPLAT + "/tmplat-def-reg-ajax.do";
    String TMPLAT_DEF_DTL = Prefix.TMPLAT + "/tmplat-def-dtl.do";
    String TMPLAT_DEF_DTL_AJAX = Prefix.TMPLAT + "/tmplat-def-dtl-ajax.do";
    String TMPLAT_DEF_MDF_AJAX = Prefix.TMPLAT + "/tmplat-def-mdf-ajax.do";
    String TMPLAT_DEF_DEL_AJAX = Prefix.TMPLAT + "/tmplat-def-del-ajax.do";

    String TMPLAT_TXT_REG_AJAX = Prefix.TMPLAT + "/tmplat-txt-reg-ajax.do";
    String TMPLAT_TXT_MDF_AJAX = Prefix.TMPLAT + "/tmplat-txt-mdf-ajax.do";

    /** 팝업 관리 (TODO) */
    String POPUP_LIST = "";

    /** 코드 관리 */
    String CL_CD_LIST = Prefix.CD + "/cl-cd-list.do";
    String CL_CD_DTL = Prefix.CD + "/cl-cd-dtl.do";
    String CL_CD_REG_AJAX = Prefix.CD + "/cl-cd-reg-ajax.do";
    String CL_CD_DTL_AJAX = Prefix.CD + "/cl-cd-dtl-ajax.do";
    String CL_CD_MDF_AJAX = Prefix.CD + "/cl-cd-mdf-ajax.do";
    String CL_CD_DEL_AJAX = Prefix.CD + "/cl-cd-del-ajax.do";
    String CL_CD_USE_AJAX = Prefix.CD + "/cl-cd-use-ajax.do";
    String CL_CD_UNUSE_AJAX = Prefix.CD + "/cl-cd-unuse-ajax.do";
    String CL_CD_SORT_ORDR_AJAX = Prefix.CD + "/cl-cd-sort-ordr-ajax.do";

    String DTL_CD_REG_AJAX = Prefix.CD + "/dtl-cd-reg-ajax.do";
    String DTL_CD_DTL_AJAX = Prefix.CD + "/dtl-cd-dtl-ajax.do";
    String DTL_CD_MDF_AJAX = Prefix.CD + "/dtl-cd-mdf-ajax.do";
    String DTL_CD_LIST_AJAX = Prefix.CD + "/dtl-cd-list-ajax.do";
    String DTL_CD_USE_AJAX = Prefix.CD + "/dtl-cd-use-ajax.do";
    String DTL_CD_UNUSE_AJAX = Prefix.CD + "/dtl-cd-unuse-ajax.do";
    String DTL_CD_DEL_AJAX = Prefix.CD + "/dtl-cd-del-ajax.do";
    String DTL_CD_SORT_ORDR_AJAX = Prefix.CD + "/dtl-cd-sort-ordr-ajax.do";
    
    /** 활동 로그 조회 */
    String LOG_ACTVTY_LIST = Prefix.LOG_ACTVTY + "/log-actvty-list.do";
    String LOG_ACTVTY_DTL_AJAX = Prefix.LOG_ACTVTY + "/log-actvty-dtl-ajax.do";
    String LOG_ACTVTY_LIST_XLSX_DOWNLOAD = Prefix.LOG_ACTVTY;

    /** 시스템 로그 조회 */
    String LOG_SYS_LIST = Prefix.LOG_SYS + "/log-sys-list.do";
    String LOG_SYS_DTL_AJAX = Prefix.LOG_SYS + "/log-sys-dtl-ajax.do";

    /** 로그 통계 조회 (TODO) */
    String LOG_STATS_USER_LIST = Prefix.LOG_STATS + "/log-stats-user-list.do";

    /** 파일시스템 */
    String FLSYS_HOME = Prefix.FLSYS + "/flsys-home.do";
    String FLSYS_LIST_AJAX = Prefix.FLSYS + "/flsys-list-ajax.do";
    String FLSYS_FILE_DOWNLOAD = Prefix.FLSYS + "/flsys-file-download.do";
    String FLSYS_OPEN_IN_EXPLORER_AJAX = Prefix.FLSYS + "/flsys-open-in-explorer-ajax.do";
    String FLSYS_FILE_EXEC_AJAX = Prefix.FLSYS + "/flsys-file-exec-ajax.do";

    String FLSYS_META_REG_AJAX = Prefix.FLSYS + "/flsys-meta-reg-ajax.do";
    String FLSYS_META_DTL_AJAX = Prefix.FLSYS + "/flsys-meta-dtl-ajax.do";
    String FLSYS_META_MDF_AJAX = Prefix.FLSYS + "/flsys-meta-mdf-ajax.do";
    String FLSYS_META_DEL_AJAX = Prefix.FLSYS + "/flsys-meta-del-ajax.do";

    /** (공통) 파일 */
    String FILE_DOWNLOAD_CHK_AJAX = Prefix.FILE + "/file-download-chk-ajax.do";
    String FILE_INFO_LIST_AJAX = Prefix.FILE + "/file-info-list-ajax.do";
    String FILE_DOWNLOAD = Prefix.FILE + "/file-download.do";
    String FILE_UPLOAD_AJAX = Prefix.FILE + "/file-upload-ajax.do";

    /** (공통) 캐시 관리 */
    String CACHE_ACTIVE_MAP_AJAX = Prefix.CACHE + "/cache-active-map-ajax.do";
    String CACHE_ACTIVE_DTL_AJAX = Prefix.CACHE + "/cache-active-dtl-ajax.do";
    String CACHE_EVICT_AJAX = Prefix.CACHE + "/cache-evict-ajax.do";
    String CACHE_CLEAR_BY_NM_AJAX = Prefix.CACHE + "/cache-clear-by-nm-ajax.do";
    String CACHE_CLEAR_AJAX = Prefix.CACHE + "/cache-clear-ajax.do";

    /** ERROR */
    // URL
    String ERROR = "/error";
    String ERROR_PAGE = Prefix.ERROR + "/error-page.do";
    String ERROR_NOT_FOUND = Prefix.ERROR + "/not-found.do";
    String ERROR_ACCESS_DENIED = Prefix.ERROR + "/access-denied.do";

    /* ---------- */

    String SCHDUL_CAL = Prefix.SCHDUL + "/schdul-cal.do";
    String SCHDUL_CAL_LIST_AJAX = Prefix.SCHDUL + "/schdul-cal-list-ajax.do";
    String SCHDUL_REG_AJAX = Prefix.SCHDUL + "/schdul-cal-reg-ajax.do";
    String SCHDUL_DTL_AJAX = Prefix.SCHDUL + "/schdul-cal-dtl-ajax.do";
    String SCHDUL_MDF_AJAX = Prefix.SCHDUL + "/schdul-cal-mdf-ajax.do";
    String SCHDUL_DEL_AJAX = Prefix.SCHDUL + "/schdul-cal-del-ajax.do";

    String VCATN_PAPR_LIST = Prefix.VCATN_PAPR + "/vcatn-papr-list.do";
    String VCATN_PAPR_REG_FORM = Prefix.VCATN_PAPR + "/vcatn-papr-reg-form.do";
    String VCATN_PAPR_REG_AJAX = Prefix.VCATN_PAPR + "/vcatn-papr-reg-ajax.do";
    String VCATN_PAPR_DTL = Prefix.VCATN_PAPR + "/vcatn-papr-dtl.do";
    String VCATN_PAPR_DTL_AJAX = Prefix.VCATN_PAPR + "/vcatn-papr-dtl-ajax.do";
    String VCATN_PAPR_MDF_FORM = Prefix.VCATN_PAPR + "/vcatn-papr-mdf-form.do";
    String VCATN_PAPR_MDF_AJAX = Prefix.VCATN_PAPR + "/vcatn-papr-mdf-ajax.do";
    String VCATN_PAPR_CF_AJAX = Prefix.VCATN_PAPR + "/vcatn-papr-cf-ajax.do";
    String VCATN_PAPR_DEL_AJAX = Prefix.VCATN_PAPR + "/vcatn-papr-del-ajax.do";

    String VCATN_SCHDUL_LIST = "/vcatn-schdul-list.do";
    String VCATN_SCHDUL_REG_AJAX = "/vcatn-schdul-reg-ajax.do";
    String VCATN_SCHDUL_DTL_AJAX = "/vcatn-schdul-dtl-ajax.do";
    String VCATN_SCHDUL_MDF_AJAX = "/vcatn-schdul-mdf-ajax.do";
    String VCATN_SCHDUL_DEL_AJAX = "/vcatn-schdul-del-ajax.do";
    String VCATN_SCHDUL_XLSX_DOWNLOAD = "/vcatn-schdul-xlsx-download.do";

    String VCATN_STATS_YY = "/vcatn-stats-yy.do";
    String VCATN_STATS_YY_UPDT_AJAX = "/vcatn-stats-yy-updt-ajax.do";
    String VCATN_STATS_YY_XLSX_DOWNLOAD = "/vcatn-stats-xlsx-download.do";

    String ADMIN_PAGE = "/admin/admin-page.do";
    String ADMIN_TEST = "/admin/test-page.do";
    String NOTION_HOME = "/notion-home.do";

    String USER_REQST_REG_FORM = Prefix.USER_REQST + "/user-reqst-reg-form.do";
    String USER_REQST_REG_AJAX = Prefix.USER_REQST + "/user-reqst-reg-ajax.do";
    String USER_REQST_CF_AJAX = Prefix.USER_REQST + "/user-reqst-cf-ajax.do";
    String USER_REQST_UNCF_AJAX = Prefix.USER_REQST + "/user-reqst-uncf-ajax.do";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String AUTH = "/auth";
        String ADMIN = "/admin";

        /* 공지사항 (notice) */
        String NOTICE = "/notice";

        /* 저널 (jrnl) */
        String JRNL = "/jrnl";
        // 저널 일자
        String DAY = "/day";
        String JRNL_DAY = JRNL + DAY;
        // 저널 꿈
        String DREAM = "/dream";
        String INTRPT = "/intrpt";
        String JRNL_DREAM = JRNL + DREAM;
        String JRNL_INTRPT = JRNL + INTRPT;
        // 저널 일기
        String ENTRY = "/entry";
        String DIARY = "/diary";
        String JRNL_ENTRY = JRNL + ENTRY;
        String JRNL_DIARY = JRNL + DIARY;
        // 저널 할일
        String TODO = "/todo";
        String JRNL_TODO = JRNL + TODO;
        // 저널 주제
        String SBJCT = "/sbjct";
        String JRNL_SBJCT = JRNL + SBJCT;
        // 저널 결산
        String SUMRY = "/sumry";
        String JRNL_SUMRY = JRNL + SUMRY;

        /* 게시판 (board) */
        String BOARD = "/board";
        String POST = "/post";
        String BOARD_POST = BOARD + POST;
        String BOARD_DEF = BOARD + "/def";

        /* 일정 (schdul) */
        String SCHDUL = "/schdul";

        /* 휴가 (vcatn) */
        String VCATN = "/vcatn";
        String PAPR = "/papr";
        String VCATN_PAPR = VCATN + PAPR;

        String COMMENT = "/comment";
        String SECTN = "/sectn";
        String TAG = "/tag";
        String TAG_PROPERTY = "/tag-property";

        String LGN_POLICY = "/lgn-policy";

        String MENU = "/menu";
        String TMPLAT = "/tmplat";
        String CD = "/cd";

        /* 로그 (log) */
        String LOG = "/log";
        String ACTVTY = "/actvty";
        String LOG_ACTVTY = LOG + ACTVTY;
        String SYS = "/sys";
        String LOG_SYS = LOG + SYS;
        String STATS = "/stats";
        String LOG_STATS = LOG + STATS;

        /* 사용자 (user) */
        String USER = "/user";
        String REQST = "/reqst";
        String MY = "/my";
        String USER_MY = USER + MY;
        String USER_REQST = USER + REQST;

        String FILE = "/file";
        String FLSYS = "/flsys";

        String ERROR = "/error";
        String CACHE = "/cache";
    }
}