package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.adapter.AdapterUrl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class Url
    implements SiteUrl, AdapterUrl {

    /** 도메인 */
    public static final String DOMAIN = "dreamdiary.nicheblog.io";

    /* ------ */

    /**
     * 리플렉션을 이용해 모든 인터페이스에서 정의된 상수들을 Map으로 반환
     *
     * @return Map<String, String> - URL 상수들을 key-value 형태로 담은 Map
     */
    public static Map<String, String> getUrlMap() {
        Map<String, String> urlMap = new HashMap<>();

        // Url 클래스에서 상수들 가져오기
        addConstantsToMap(Url.class, urlMap);

        return urlMap;
    }

    /**
     * 리플렉션을 이용해 Url 클래스의 상수들을 Map으로 반환
     */
    private static void addConstantsToMap(Class<?> clazz, Map<String, String> urlMap) {
        // 클래스가 인터페이스인 경우에도 적용
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // static final 필드만 필터링
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getType() == String.class) {
                try {
                    // 필드 값 얻기
                    String value = (String) field.get(null);  // static 필드는 null로 접근
                    urlMap.put(field.getName(), value);  // 필드 이름을 key로, 필드 값을 value로
                } catch (IllegalAccessException e) {
                    MessageUtils.getExceptionMsg(e);
                }
            }
        }

        // 만약 인터페이스가 있다면, 인터페이스 상수들도 포함해야 하므로
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> iface : interfaces) {
            addConstantsToMap(iface, urlMap);
        }
    }
}