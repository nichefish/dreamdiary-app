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
    String APP_AUTH_LGN_FORM = Prefix.APP + "/auth/lgn-form.do";
    String API_AUTH_LGN_PROC = Prefix.API + "/auth/lgn-proc";
    String API_AUTH_LGN_PW_CHG = Prefix.API + "/auth/lgn-pw-chg";
    String API_AUTH_LGOUT = Prefix.API + "/auth/lgout";
    String API_AUTH_EXPIRE_SESSION = Prefix.API + "/auth/expire-session";
    String API_AUTH_VERIFY = Prefix.API + "/auth/verify/{token}";
    String API_AUTH_INFO = Prefix.API + "/auth/get-auth-info";

    // 구글 소셜 로그인 팝업
    String OAUTH2_GOOGLE = "/oauth2/authorization/google";
    String OAUTH2_GOOGLE_REDIRECT_URI = "/login/oauth2/code/google";

    String OAUTH2_NAVER = "/oauth2/authorization/naver";
    String OAUTH2_NAVER_REDIRECT_URI = "/login/oauth2/code/naver";

    /**
     * PREFIX 정의 정보
     */
    interface Prefix {
        String APP = "/app";
        String API = "/api";
    }
}