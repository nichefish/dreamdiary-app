package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.adapter.AdapterConstant;
import io.nicheblog.dreamdiary.auth.AuthConstant;
import io.nicheblog.dreamdiary.global._common.cd.CdConstant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Constant
 * <pre>
 *  공통으로 사용하는 상수 데이터 정의.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
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

    public static final String SIMPLE_KEY = "SimpleKey()";

    /** URL_ENCODING */
    public static final Boolean URL_ENC_FALSE = false;

    /** 디바이스 정보 */
    public static final String IS_MBL = "IS_MBL";

    /** 스태틱 자원 경로 (인증 불필요) */
    public static final String[] STATIC_PATHS = { "/css/**", "/js/**", "/media/**", "/font/**", "/lib/**", "/metronic/**", "/react/**", "/content/**", "/upfile/public/**" };

    /** 파일업로드 기본 경로 */
    public static final String UPFILE_PATH = "file/upfile/";

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
    public static final String PAGINATION_INFO = "paginationInfo";
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

    /**
     * 리플렉션을 이용해 모든 인터페이스에서 정의된 상수들을 Map으로 반환
     *
     * @return Map<String, String> - 상수들을 key-value 형태로 담은 Map
     */
    public static Map<String, String> getConstantMap() {
        final Map<String, String> constantMap = new HashMap<>();

        // Url 클래스에서 상수들 가져오기
        addConstantsToMap(Url.class, constantMap);

        return constantMap;
    }

    /**
     * 리플렉션을 이용해 클래스의 상수들을 Map으로 반환
     *
     * @param clazz 리플렉션을 수행할 대상 클래스
     * @param constantMap 클래스 상수를 저장할 `Map<String, String>` 객체
     */
    private static void addConstantsToMap(final Class<?> clazz, final Map<String, String> constantMap) {
        // 클래스가 인터페이스인 경우에도 적용
        final Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
            // static final 필드만 필터링
            boolean isStaticFinalStringField = Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getType() == String.class;
            if (!isStaticFinalStringField) continue;

            try {
                // 필드 값 얻기
                final String value = (String) field.get(null);  // static 필드는 null로 접근
                constantMap.put(field.getName(), value);  // 필드 이름을 key로, 필드 값을 value로
            } catch (final IllegalAccessException e) {
                MessageUtils.getExceptionMsg(e);
            }
        }

        // 만약 인터페이스가 있다면, 인터페이스 상수들도 포함해야 하므로
        final Class<?>[] interfaces = clazz.getInterfaces();
        for (final Class<?> iface : interfaces) {
            addConstantsToMap(iface, constantMap);
        }
    }
}
