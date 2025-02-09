package io.nicheblog.dreamdiary.global._common.cache.service;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * CacheStrategy
 * <pre>
 *  다양한 캐시 구현체를 지원하기 위한 전략 패턴을 정의한다.
 * </pre>
 *
 * @author nichefish
 */
public interface CacheStrategy {

    /**
     * 주어진 캐시 이름과 키를 기반으로 캐시에서 객체를 조회한다.
     *
     * @param cacheName 캐시 이름
     * @param cacheKey  조회할 캐시 키
     * @return 캐시에서 조회된 객체, 없을 경우 {@code null} 반환
     */
    Object getObject(String cacheName, Object cacheKey);

    /**
     * 캐시 내용을 {@link Map} 형태로 조회하여 해당 캐시에 저장된 값을 추가한다.
     *
     * @param cacheName     캐시 이름
     * @param cacheValueMap 캐시 값이 저장될 {@link Map} 객체
     */
    void addCacheValue(String cacheName, Map<Object, Object> cacheValueMap);

    /**
     * 해당 캐시가 특정 전략에서 지원되는지 여부를 확인한다.
     *
     * @param cache {@link Cache} 객체
     * @return 지원되는 경우 {@code true}, 그렇지 않으면 {@code false}
     */
    boolean supports(Cache cache);

    /**
     * SimpleKey를 문자열로 변환
     *
     * @param key 캐시 키
     * @return 사람이 읽을 수 있는 문자열
     */
    default String stringifyKey(final Object key) throws NoSuchFieldException, IllegalAccessException {
        if (key instanceof SimpleKey) {
            // 리플렉션을 통해 params 필드 접근
            final Field paramsField = SimpleKey.class.getDeclaredField("params");
            paramsField.setAccessible(true);
            final Object[] params = (Object[]) paramsField.get(key);

            return "SimpleKey(" + String.join(", ", toStringArray(params)) + ")";
        }
        return key.toString();
    }

    /**
     * Object 배열을 문자열 배열로 변환
     *
     * @param objects Object 배열
     * @return 문자열 배열
     */
    default String[] toStringArray(final Object[] objects) {
        final String[] result = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            result[i] = (objects[i] != null) ? objects[i].toString() : "null";
        }
        return result;
    }
}
