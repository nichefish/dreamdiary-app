package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.adapter.AdapterConstant;
import io.nicheblog.dreamdiary.auth.AuthConstant;
import io.nicheblog.dreamdiary.global._common.cd.CdConstant;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Constant
 * <pre>
 *  공통으로 사용하는 상수 데이터 정의.
 * </pre>
 *
 * @author nichefish
 */
public final class Constant
        implements AuthConstant, CdConstant, AdapterConstant {

    /**
     * TimeZone
     */
    public static final String LOC_SEOUL = "Asia/Seoul";
    public static final TimeZone TZ_SEOUL = TimeZone.getTimeZone(Constant.LOC_SEOUL);
    public static final Locale LC_KO = Locale.KOREA;

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String SITE_NM = "dreamdiary";
    public static final String SITE_DESC = "'tis my site!";
    public static final String SITE_KEYWORDS = "dream, diary, journal";

    /** blank avatar image url */
    public static final String BLANK_AVATAR_URL = "/metronic/assets/media/avatars/avatar_blank.png";

    public static final String PREFIX = "PREFIX";
    public static final String SUFFIX = "SUFFIX";

    /** URL_ENCODING */
    public static final Boolean URL_ENC_FALSE = false;

    /** 디바이스 정보 */
    public static final String IS_MBL = "IS_MBL";

    /** 스태틱 자원 경로 (인증 불필요) */
    public static final String[] STATIC_PATHS = { "/css/**", "/js/**", "/media/**", "/font/**", "/lib/**", "/metronic/**", "/react/**", "/content/**", "/upfile/public/**" };

    /** IP 헤더 목록 */
    public static final String[] IP_HEADERS = { "X-FORWARDED-FOR", "Proxy-Client-IP",  "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };
    public static final String REMOTE_ADDR = "remoteAddr";
    public static final String REFERER = "referer";

    /** 쿠키 */
    public static final String KT_SIDEBAR_MINIMIZE = "data-kt-app-sidebar-minimize";
    public static final String KT_SIDEBAR_MINIMIZE_STATE = "sidebar_minimize_state";
    public static final String KT_ASIDE_MINIMIZE = "data-kt-aside-minimize";

    /** Page Static Strings */
    public static final String ERROR_MSG = "errorMsg";
    public static final String MESSAGE = "message";
    public static final String PAGINATION_INFO = "paginationInfo";
    public static final String IS_REG = "isReg";
    public static final String IS_MDF = "isMdf";
    public static final String LIST_URL = "listUrl";
    public static final String FORM_MODE = "formMode";

    /** 각 메뉴 번호 */
    public static final String USER = "user";              // 계정 관리
    public static final String HOME_FLSYS = "flsys";

    /** 부트스트랩 클래스 */
    public static final String BS_PRIMARY = "primary";
    public static final String BS_PRIMARY_700 = "primary-700";
    public static final String BS_SECONDARY = "secondary";
    public static final String BS_SUCCESS = "success";
    public static final String BS_INFO = "info";
    public static final String BS_INFO_700 = "info-700";
    public static final String BS_DANGER = "danger";
    public static final String BS_WARNING = "warning";
    public static final String BS_DARK = "dark";
    public static final String BS_MUTED = "muted";
}
