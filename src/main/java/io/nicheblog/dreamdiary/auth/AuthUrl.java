package io.nicheblog.dreamdiary.auth;

/**
 * AuthUrl
 * <pre>
 *  공통 상수:: 권한 관련 URL 정의.
 * </pre>
 *
 * @author nichefish
 */
public interface AuthUrl {

    /** 로그인 관련 */
    String AUTH_LGN_FORM = AuthUrl.Prefix.AUTH + "/lgnForm.do";
    String AUTH_LGN_PROC = AuthUrl.Prefix.AUTH + "/lgnProc.do";
    String AUTH_LGN_PW_CHG_AJAX = AuthUrl.Prefix.AUTH + "/lgnPwChgAjax.do";
    String AUTH_LGOUT = AuthUrl.Prefix.AUTH + "/lgout.do";
    String AUTH_EXPIRE_SESSION_AJAX = AuthUrl.Prefix.AUTH + "/expireSession.do";
    String AUTH_VERIFY = AuthUrl.Prefix.AUTH + "/verify.do";
    String AUTH_INFO = "/api/auth/getAuthInfo.do";

    // 구글 소셜 로그인 팝업
    String OAUTH2_GOOGLE = "/oauth2/authorization/google";
    String OAUTH2_GOOGLE_REDIRECT_URI = "/login/oauth2/code/google";

    String OAUTH2_NAVER = "/oauth2/authorization/naver";
    String OAUTH2_NAVER_REDIRECT_URI = "/login/oauth2/code/naver";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String AUTH = "/auth";
    }
}