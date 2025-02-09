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
    String TEST_PAGE = Prefix.ADMIN + "/testPage.do";

    /** 저널 일자 */
    String JRNL_DAY_PAGE = Prefix.JRNL_DAY + "/jrnlDayPage.do";
    String JRNL_DAY_LIST_AJAX = Prefix.JRNL_DAY + "/jrnlDayListAjax.do";
    String JRNL_DAY_CAL_LIST_AJAX = Prefix.JRNL_DAY + "/jrnlDayCalListAjax.do";
    String JRNL_DAY_REG_AJAX = Prefix.JRNL_DAY + "/jrnlDayRegAjax.do";
    String JRNL_DAY_DTL_AJAX = Prefix.JRNL_DAY + "/jrnlDayDtlAjax.do";
    String JRNL_DAY_MDF_AJAX = Prefix.JRNL_DAY + "/jrnlDayMdfAjax.do";
    String JRNL_DAY_DEL_AJAX = Prefix.JRNL_DAY + "/jrnlDayDelAjax.do";
    /** 저널 일자 달력 */
    String JRNL_DAY_CAL = Prefix.JRNL_DAY + "/jrnlDayCal.do";
    /** 저널 일자 태그 */
    String JRNL_DAY_TAG_LIST_AJAX = Prefix.JRNL_DAY + "/jrnlDayTagListAjax.do";
    String JRNL_DAY_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DAY + "/jrnlDayTagGroupListAjax.do";
    String JRNL_DAY_TAG_DTL_AJAX = Prefix.JRNL_DAY + "/jrnlDayTagDtlAjax.do";
    String JRNL_DAY_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DAY + "/jrnlDayTagCtgrMapAjax.do";

    /** 저널 꿈 */
    String JRNL_DREAM_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamListAjax.do";
    String JRNL_DREAM_REG_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamRegAjax.do";
    String JRNL_DREAM_DTL_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamDtlAjax.do";
    String JRNL_DREAM_MDF_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamMdfAjax.do";
    String JRNL_DREAM_DEL_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamDelAjax.do";
    /** 저널 꿈 태그 */
    String JRNL_DREAM_TAG_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamTagListAjax.do";
    String JRNL_DREAM_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamTagGroupListAjax.do";
    String JRNL_DREAM_TAG_DTL_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamTagDtlAjax.do";
    String JRNL_DREAM_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DREAM + "/jrnlDreamTagCtgrMapAjax.do";

    /** 저널 일기 */
    String JRNL_DIARY_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryListAjax.do";
    String JRNL_DIARY_REG_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryRegAjax.do";
    String JRNL_DIARY_DTL_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryDtlAjax.do";
    String JRNL_DIARY_MDF_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryMdfAjax.do";
    String JRNL_DIARY_DEL_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryDelAjax.do";
    /** 저널 일기 태그 */
    String JRNL_DIARY_TAG_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryTagListAjax.do";
    String JRNL_DIARY_TAG_GROUP_LIST_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryTagGroupListAjax.do";
    String JRNL_DIARY_TAG_DTL_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryTagDtlAjax.do";
    String JRNL_DIARY_TAG_CTGR_MAP_AJAX = Prefix.JRNL_DIARY + "/jrnlDiaryTagCtgrMapAjax.do";

    /** 저널 주제 */
    String JRNL_SBJCT_LIST = Prefix.JRNL_SBJCT + "/jrnlSbjctList.do";
    String JRNL_SBJCT_REG_FORM = Prefix.JRNL_SBJCT + "/jrnlSbjctRegForm.do";
    String JRNL_SBJCT_REG_AJAX = Prefix.JRNL_SBJCT + "/jrnlSbjctRegAjax.do";
    String JRNL_SBJCT_REG_PREVIEW_POP = Prefix.JRNL_SBJCT + "/jrnlSbjctPreviewPop.do";

    String JRNL_SBJCT_DTL = Prefix.JRNL_SBJCT + "/jrnlSbjctDtl.do";
    String JRNL_SBJCT_DTL_AJAX = Prefix.JRNL_SBJCT + "/jrnlSbjctDtlAjax.do";
    String JRNL_SBJCT_MDF_FORM = Prefix.JRNL_SBJCT + "/jrnlSbjctMdfForm.do";
    String JRNL_SBJCT_MDF_AJAX = Prefix.JRNL_SBJCT + "/jrnlSbjctMdfAjax.do";
    String JRNL_SBJCT_DEL_AJAX = Prefix.JRNL_SBJCT + "/jrnlSbjctDelAjax.do";

    /** 저널 결산 */
    String JRNL_SUMRY_LIST = Prefix.JRNL_SUMRY + "/jrnlSumryList.do";
    String JRNL_SUMRY_LIST_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryListAjax.do";
    String JRNL_SUMRY_DTL = Prefix.JRNL_SUMRY + "/jrnlSumryDtl.do";
    String JRNL_SUMRY_DTL_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryDtlAjax.do";
    String JRNL_SUMRY_MAKE_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryMakeAjax.do";
    String JRNL_SUMRY_MAKE_TOTAL_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryMakeTotalAjax.do";
    String JRNL_SUMRY_DREAM_COMPT_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryDreamComptAjax.do";
    String JRNL_SUMRY_REG_AJAX = Prefix.JRNL_SUMRY + "/jrnlSumryRegAjax.do";

    /** 공지사항 */
    String NOTICE_LIST = Prefix.NOTICE + "/noticeList.do";
    String NOTICE_REG_FORM = Prefix.NOTICE + "/noticeRegForm.do";
    String NOTICE_REG_AJAX = Prefix.NOTICE + "/noticeRegAjax.do";
    String NOTICE_DTL = Prefix.NOTICE + "/noticeDtl.do";
    String NOTICE_DTL_AJAX = Prefix.NOTICE + "/noticeDtlAjax.do";
    String NOTICE_MDF_FORM = Prefix.NOTICE + "/noticeMdfForm.do";
    String NOTICE_MDF_AJAX = Prefix.NOTICE + "/noticeMdfAjax.do";
    String NOTICE_DEL_AJAX = Prefix.NOTICE + "/noticeDelAjax.do";
    String NOTICE_REG_PREVIEW_POP = Prefix.NOTICE + "/noticeRegPreviewPop.do";
    String NOTICE_POPUP_LIST_AJAX = Prefix.NOTICE + "/noticePopupListAjax.do";
    String NOTICE_LIST_XLSX_DOWNLOAD = Prefix.NOTICE + "/noticeListXlsxDownload.do";

    /** 게시판 */
    String BOARD_POST_LIST = Prefix.BOARD_POST + "/boardPostList.do";
    String BOARD_POST_REG_FORM = Prefix.BOARD_POST + "/boardPostRegForm.do";
    String BOARD_POST_REG_AJAX = Prefix.BOARD_POST + "/boardPostRegAjax.do";
    String BOARD_POST_DTL = Prefix.BOARD_POST + "/boardPostDtl.do";
    String BOARD_POST_DTL_AJAX = Prefix.BOARD_POST + "/boardPostDtlAjax.do";
    String BOARD_POST_MDF_FORM = Prefix.BOARD_POST + "/boardPostMdfForm.do";
    String BOARD_POST_MDF_AJAX = Prefix.BOARD_POST + "/boardPostMdfAjax.do";
    String BOARD_POST_DEL_AJAX = Prefix.BOARD_POST + "/boardPostDelAjax.do";
    String BOARD_POST_REG_PREVIEW_POP = Prefix.BOARD_POST + "/boardPostRegPreviewPop.do";

    /** 사용자 관리 */
    String USER_LIST = Prefix.USER + "/userList.do";
    String USER_REG_FORM = Prefix.USER + "/userRegForm.do";
    String USER_REG_AJAX = Prefix.USER + "/userRegAjax.do";
    String USER_DTL = Prefix.USER + "/userDtl.do";
    String USER_MDF_FORM = Prefix.USER + "/userMdfForm.do";
    String USER_MDF_AJAX = Prefix.USER + "/userMdfAjax.do";
    String USER_PW_RESET_AJAX = Prefix.USER + "/passwordResetAjax.do";
    String USER_DEL_AJAX = Prefix.USER + "/userDelAjax.do";
    String USER_LIST_XLSX_DOWNLOAD = Prefix.USER + "/userListXlsxDownload.do";
    String USER_ID_DUP_CHK_AJAX = Prefix.USER + "/userIdDupChkAjax.do";
    String USER_EMAIL_DUP_CHK_AJAX = Prefix.USER + "/userEmailDupChkAjax.do";

    /** 내 정보 관리 */
    String USER_MY_DTL = Prefix.USER_MY + "/userMyDtl.do";
    String USER_MY_UPLOAD_PROFL_IMG_AJAX = Prefix.USER_MY + "/userMyUploadProflImgAjax.do";
    String USER_MY_REMOVE_PROFL_IMG_AJAX = Prefix.USER_MY + "/userMyRemoveProflImgAjax.do";
    String USER_MY_PW_CF_AJAX = Prefix.USER_MY + "/userMyPwCfAjax.do";
    String USER_MY_PW_CHG_AJAX = Prefix.USER_MY + "/userMyPwChgAjax.do";

    /** 댓글 */
    String COMMENT_LIST_AJAX = Prefix.COMMENT + "/commentListAjax.do";
    String COMMENT_REG_AJAX = Prefix.COMMENT + "/commentRegAjax.do";
    String COMMENT_DTL_AJAX = Prefix.COMMENT + "/commentDtlAjax.do";
    String COMMENT_MDF_AJAX = Prefix.COMMENT + "/commentMdfAjax.do";
    String COMMENT_DEL_AJAX = Prefix.COMMENT + "/commentDelAjax.do";

    /** 단락 */
    String SECTN_LIST_AJAX = Prefix.SECTN + "/sectnListAjax.do";
    String SECTN_REG_AJAX = Prefix.SECTN + "/sectnRegAjax.do";
    String SECTN_MDF_AJAX = Prefix.SECTN + "/sectnMdfAjax.do";
    String SECTN_DTL_AJAX = Prefix.SECTN + "/sectnDtlAjax.do";
    String SECTN_DEL_AJAX = Prefix.SECTN + "/sectnDelAjax.do";
    String SECTN_SORT_ORDR_AJAX = Prefix.SECTN + "/sectnSortOrdrAjax.do";

    /** 태그 */
    String TAG_LIST = Prefix.TAG + "/tagList.do";
    String TAG_CLOUD_PAGE = Prefix.TAG + "/tagCloudPage.do";
    String TAG_LIST_AJAX = Prefix.TAG + "/tagListAjax.do";
    String TAG_DTL_AJAX = Prefix.TAG + "/tagDtlAjax.do";

    /** 태그 속성 */
    String TAG_PROPERTY_REG_AJAX = Prefix.TAG_PROPERTY + "/tagPropertyRegAjax.do";
    String TAG_PROPERTY_DTL_AJAX = Prefix.TAG_PROPERTY + "/tagPropertyDtlAjax.do";
    String TAG_PROPERTY_MDF_AJAX = Prefix.TAG_PROPERTY + "/tagPropertyMdfAjax.do";
    String TAG_PROPERTY_DEL_AJAX = Prefix.TAG_PROPERTY + "/tagPropertyDelAjax.do";

    /** 로그인 정책 관리 */
    String LGN_POLICY_FORM = Prefix.LGN_POLICY + "/lgnPolicyForm.do";
    String LGN_POLICY_REG_AJAX = Prefix.LGN_POLICY + "/lgnPolicyRegAjax.do";

    /** 메뉴 관리 */
    String MENU_PAGE = Prefix.MENU + "/menuPage.do";
    String MENU_MAIN_LIST_AJAX = Prefix.MENU + "/menuMainListAjax.do";
    String MENU_REG_AJAX = Prefix.MENU + "/menuList.do";
    String MENU_DTL_AJAX = Prefix.MENU + "/menuDtlAjax.do";
    String MENU_MDF_AJAX = Prefix.MENU + "/menuMdfAjax.do";
    String MENU_DEL_AJAX = Prefix.MENU + "/menuDelAjax.do";
    String MENU_SORT_ORDR_AJAX = Prefix.MENU + "/menuSortOrdrAjax.do";

    /** 게시판 관리 */
    String BOARD_DEF_LIST = Prefix.BOARD_DEF + "/boardDefList.do";
    String BOARD_DEF_REG_AJAX = Prefix.BOARD_DEF + "/boardDefRegAjax.do";
    String BOARD_DEF_DTL_AJAX = Prefix.BOARD_DEF + "/boardDefDtlAjax.do";
    String BOARD_DEF_MDF_ITEM_AJAX = Prefix.BOARD_DEF + "/boardDefMdfItemAjax.do";
    String BOARD_DEF_DEL_AJAX = Prefix.BOARD_DEF + "/boardDefDelAjax.do";
    String BOARD_DEF_USE_AJAX = Prefix.BOARD_DEF + "/boardDefUseAjax.do";
    String BOARD_DEF_UNUSE_AJAX = Prefix.BOARD_DEF + "/boardDefUnuseAjax.do";
    String BOARD_DEF_SORT_ORDR_AJAX = Prefix.MENU + "/boardDefSortOrdrAjax.do";

    /** 템플릿 관리 (TODO) */
    String TMPLAT_DEF_LIST = Prefix.TMPLAT + "/tmplatDefList.do";
    String TMPLAT_DEF_REG_AJAX = Prefix.TMPLAT + "/tmplatDefRegAjaxdo";
    String TMPLAT_DEF_DTL = Prefix.TMPLAT + "/tmplatDefDtl.do";
    String TMPLAT_DEF_DTL_AJAX = Prefix.TMPLAT + "/tmplatDefDtlAjax.do";
    String TMPLAT_DEF_MDF_AJAX = Prefix.TMPLAT + "/tmplatDefMdfAjax.do";
    String TMPLAT_DEF_DEL_AJAX = Prefix.TMPLAT + "/tmplatDefDelAjax.do";

    String TMPLAT_TXT_REG_AJAX = Prefix.TMPLAT + "/tmplatTxtRegAjax.do";
    String TMPLAT_TXT_MDF_AJAX = Prefix.TMPLAT + "/tmplatTxtMdfAjax.do";

    /** 팝업 관리 (TODO) */
    String POPUP_LIST = "";

    /** 코드 관리 */
    String CL_CD_LIST = Prefix.CD + "/clCdList.do";
    String CL_CD_DTL = Prefix.CD + "/clCdDtl.do";
    String CL_CD_REG_AJAX = Prefix.CD + "/clCdRegAjax.do";
    String CL_CD_DTL_AJAX = Prefix.CD + "/clCdDtlAjax.do";
    String CL_CD_MDF_AJAX = Prefix.CD + "/clCdMdfAjax.do";
    String CL_CD_DEL_AJAX = Prefix.CD + "/clCdDelAjax.do";
    String CL_CD_USE_AJAX = Prefix.CD + "/clCdUseAjax.do";
    String CL_CD_UNUSE_AJAX = Prefix.CD + "/clCdUnuseAjax.do";
    String CL_CD_SORT_ORDR_AJAX = Prefix.CD + "/clCdSortOrdrAjax.do";

    String DTL_CD_REG_AJAX = Prefix.CD + "/dtlCdRegAjax.do";
    String DTL_CD_DTL_AJAX = Prefix.CD + "/dtlCdDtlAjax.do";
    String DTL_CD_MDF_AJAX = Prefix.CD + "/dtlCdMdfAjax.do";
    String DTL_CD_LIST_AJAX = Prefix.CD + "/dtlCdListAjax.do";
    String DTL_CD_USE_AJAX = Prefix.CD + "/dtlCdUseAjax.do";
    String DTL_CD_UNUSE_AJAX = Prefix.CD + "/dtlCdUnuseAjax.do";
    String DTL_CD_DEL_AJAX = Prefix.CD + "/dtlCdDelAjax.do";
    String DTL_CD_SORT_ORDR_AJAX = Prefix.CD + "/dtlCdSortOrdrAjax.do";
    
    /** 활동 로그 조회 */
    String LOG_ACTVTY_LIST = Prefix.LOG_ACTVTY + "/logActvtyList.do";
    String LOG_ACTVTY_DTL_AJAX = Prefix.LOG_ACTVTY + "/logActvtyDtlAjax.do";
    String LOG_ACTVTY_LIST_XLSX_DOWNLOAD = Prefix.LOG_ACTVTY + "";

    /** 시스템 로그 조회 */
    String LOG_SYS_LIST = Prefix.LOG_SYS + "/logSysList.do";
    String LOG_SYS_DTL_AJAX = Prefix.LOG_SYS + "/logSysDtlAjax.do";

    /** 로그 통계 조회 (TODO) */
    String LOG_STATS_USER_LIST = Prefix.LOG_STATS + "/logStatsUserList.do";

    /** 파일시스템 */
    String FLSYS_HOME = Prefix.FLSYS + "/flsysHome.do";
    String FLSYS_LIST_AJAX = Prefix.FLSYS + "/flsysListAjax.do";
    String FLSYS_FILE_DOWNLOAD = Prefix.FLSYS + "/flsysFileDownload.do";
    String FLSYS_OPEN_IN_EXPLORER_AJAX = Prefix.FLSYS + "/flsysOpenInExplorerAjax.do";
    String FLSYS_FILE_EXEC_AJAX = Prefix.FLSYS + "/flsysFileExecAjax.do";

    String FLSYS_META_REG_AJAX = Prefix.FLSYS + "/flsysMetaRegAjax.do";
    String FLSYS_META_DTL_AJAX = Prefix.FLSYS + "/flsysMetaDtlAjax.do";
    String FLSYS_META_MDF_AJAX = Prefix.FLSYS + "/flsysMetaMdfAjax.do";
    String FLSYS_META_DEL_AJAX = Prefix.FLSYS + "/flsysMetaDelAjax.do";

    /** (공통) 파일 */
    String FILE_DOWNLOAD_CHK_AJAX = Prefix.FILE + "/fileDownloadChkAjax.do";
    String FILE_INFO_LIST_AJAX = Prefix.FILE + "/fileInfoListAjax.do";
    String FILE_DOWNLOAD = Prefix.FILE + "/fileDownload.do";
    String FILE_UPLOAD_AJAX = Prefix.FILE + "/fileUploadAjax.do";

    /** (공통) 캐시 관리 */
    String CACHE_ACTIVE_MAP_AJAX = Prefix.CACHE + "/cacheActiveMapAjax.do";
    String CACHE_ACTIVE_DTL_AJAX = Prefix.CACHE + "/cacheActiveDtlAjax.do";
    String CACHE_EVICT_AJAX = Prefix.CACHE + "/cacheEvictAjax.do";
    String CACHE_CLEAR_BY_NM_AJAX = Prefix.CACHE + "/cacheClearByNmAjax.do";
    String CACHE_CLEAR_AJAX = Prefix.CACHE + "/cacheClearAjax.do";

    /** ERROR */
    // URL
    String ERROR = "/error";
    String ERROR_PAGE = Prefix.ERROR + "/errorPage.do";
    String ERROR_NOT_FOUND = Prefix.ERROR + "/notFound.do";
    String ERROR_ACCESS_DENIED = Prefix.ERROR + "/accessDenied.do";

    /* ---------- */

    String SCHDUL_CAL = Prefix.SCHDUL + "/schdulCal.do";
    String SCHDUL_CAL_LIST_AJAX = Prefix.SCHDUL + "/schdulCalListAjax.do";
    String SCHDUL_REG_AJAX = Prefix.SCHDUL + "/schdulCalRegAjax.do";
    String SCHDUL_DTL_AJAX = Prefix.SCHDUL + "/schdulCalDtlAjax.do";
    String SCHDUL_MDF_AJAX = Prefix.SCHDUL + "/schdulCalMdfAjax.do";
    String SCHDUL_DEL_AJAX = Prefix.SCHDUL + "/schdulCalDelAjax.do";

    String VCATN_PAPR_LIST = Prefix.VCATN_PAPR + "/vcatnPaprList.do";
    String VCATN_PAPR_REG_FORM = Prefix.VCATN_PAPR + "/vcatnPaprRegForm.do";
    String VCATN_PAPR_REG_AJAX = Prefix.VCATN_PAPR + "/vcatnPaprRegAjax.do";
    String VCATN_PAPR_DTL = Prefix.VCATN_PAPR + "/vcatnPaprDtl.do";
    String VCATN_PAPR_DTL_AJAX = Prefix.VCATN_PAPR + "/vcatnPaprDtlAjax.do";
    String VCATN_PAPR_MDF_FORM = Prefix.VCATN_PAPR + "/vcatnPaprMdfForm.do";
    String VCATN_PAPR_MDF_AJAX = Prefix.VCATN_PAPR + "/vcatnPaprMdfAjax.do";
    String VCATN_PAPR_CF_AJAX = Prefix.VCATN_PAPR + "/vcatnPaprCfAjax.do";
    String VCATN_PAPR_DEL_AJAX = Prefix.VCATN_PAPR + "/vcatnPaprDelAjax.do";

    String VCATN_SCHDUL_LIST = "/vcatnSchdulList.do";
    String VCATN_SCHDUL_REG_AJAX = "/vcatnSchdulRegAjax.do";
    String VCATN_SCHDUL_DTL_AJAX = "/vcatnSchdulDtlAjax.do";
    String VCATN_SCHDUL_MDF_AJAX = "/vcatnSchdulMdfAjax.do";
    String VCATN_SCHDUL_DEL_AJAX = "/vcatnSchdulDelAjax.do";
    String VCATN_SCHDUL_XLSX_DOWNLOAD = "/vcatnSchdulXlsxDownload.do";

    String VCATN_STATS_YY = "/vcatnStatsYy.do";
    String VCATN_STATS_YY_UPDT_AJAX = "/vcatnStatsYyUpdtAjax.do";
    String VCATN_STATS_YY_XLSX_DOWNLOAD = "/vcatnStatsXlsxDownload.do";

    String ADMIN_PAGE = "/admin/adminPage.do";
    String ADMIN_TEST = "/admin/testPage.do";
    String NOTION_HOME = "/notionHome.do";

    String USER_REQST_REG_FORM = Prefix.USER_REQST + "/userReqstRegForm.do";
    String USER_REQST_REG_AJAX = Prefix.USER_REQST + "/userReqstRegAjax.do";
    String USER_REQST_CF_AJAX = Prefix.USER_REQST + "/userReqstCfAjax.do";
    String USER_REQST_UNCF_AJAX = Prefix.USER_REQST + "/userReqstUncfAjax.do";

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
        String JRNL_DREAM = JRNL + DREAM;
        // 저널 일기
        String DIARY = "/diary";
        String JRNL_DIARY = JRNL + DIARY;
        // 저널 주제
        String SBJCT = "/sbjct";
        String JRNL_SBJCT = JRNL + SBJCT;
        // 저널 결산
        String SUMRY = "/sumry";
        String JRNL_SUMRY = JRNL + SUMRY;

        /* 게시판 (board) */
        String BOARD = "/board";
        String POST = "/board";
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
        String TAG_PROPERTY = "/tagProperty";

        String LGN_POLICY = "/lgnPolicy";

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