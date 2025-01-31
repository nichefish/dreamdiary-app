package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.adapter.AdapterUrl;
import io.nicheblog.dreamdiary.auth.AuthUrl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Url
 * <pre>
 *  공통 상수 :: URL 정의.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public class Url
    implements SiteUrl, AuthUrl, AdapterUrl {

    /** 도메인 */
    public static final String DOMAIN = "dreamdiary.nicheblog.io";

    /* ------ */

    /**
     * 리플렉션을 이용해 모든 인터페이스에서 정의된 상수들을 Map으로 반환
     *
     * @return {@link Map} - URL 상수들을 key-value 형태로 담은 Map
     */
    public static Map<String, String> getUrlMap() {
        final Map<String, String> urlMap = new HashMap<>();

        // Url 클래스에서 상수들 가져오기
        addConstantsToMap(Url.class, urlMap);

        return urlMap;
    }

    /**
     * 리플렉션을 이용해 Url 클래스의 상수들을 Map으로 반환
     *
     * @param clazz 리플렉션을 수행할 대상 클래스
     * @param urlMap 클래스 상수를 저장할 `Map<String, String>` 객체
     */
    private static void addConstantsToMap(final Class<?> clazz, final Map<String, String> urlMap) {
        // 클래스가 인터페이스인 경우에도 적용
        final Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
            // static final 필드만 필터링
            boolean isStaticFinalStringField = Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getType() == String.class;
            if (!isStaticFinalStringField) continue;

            try {
                // 필드 값 얻기
                final String value = (String) field.get(null);  // static 필드는 null로 접근
                urlMap.put(field.getName(), value);  // 필드 이름을 key로, 필드 값을 value로
            } catch (final IllegalAccessException e) {
                MessageUtils.getExceptionMsg(e);
            }
        }

        // 만약 인터페이스가 있다면, 인터페이스 상수들도 포함해야 하므로
        final Class<?>[] interfaces = clazz.getInterfaces();
        for (final Class<?> iface : interfaces) {
            addConstantsToMap(iface, urlMap);
        }
    }
}