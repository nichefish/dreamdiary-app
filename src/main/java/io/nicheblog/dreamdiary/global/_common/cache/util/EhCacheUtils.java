package io.nicheblog.dreamdiary.global._common.cache.util;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EhCacheUtils
 * <pre>
 *  ehCache 수동 적용 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class EhCacheUtils {

    @Resource(name="jCacheManager")
    CacheManager manager;
    @Resource(name="hibernateSessionFactory")
    private SessionFactory factory;

    private static CacheManager cacheManager;
    private static SessionFactory sessionFactory;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        cacheManager = manager;
        sessionFactory = factory;
    }

    /**
     * 캐시 목록 조회
     */
    public static List<String> chckActiveCacheNm() {
        return new ArrayList<>(cacheManager.getCacheNames());
    }

    /**
     * 캐시 목록 조회
     */
    public static List<Cache> getActiveCacheList() {
        final Collection<String> activeCacheNameList = cacheManager.getCacheNames();
        return activeCacheNameList
                .stream()
                .map(cacheName -> cacheManager.getCache(cacheName))
                .collect(Collectors.toList());
    }

    /**
     * 캐시 목록 조회
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Object> getActiveCacheMap() {
        final Map<String, Object> cacheContents = new HashMap<>();

        final List<Cache> activeCacheList = getActiveCacheList();
        activeCacheList
                .forEach(cache -> {
                    final String name = cache.getName();
                    final Object nativeCache = cache.getNativeCache();

                    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache caffeineCache) {
                        // Caffeine Cache 처리
                        final Map<Object, Object> cacheValue = new HashMap<>();
                        caffeineCache.asMap().forEach((key, value) -> {
                            try {
                                final String readableKey = stringifyKey(key);
                                cacheValue.put(readableKey, value);
                                log.debug("Caffeine Cache Key: {}", readableKey);
                            } catch (final NoSuchFieldException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        // 캐시 저장 개수가 0인 경우 건너뛰기
                        if (!cacheValue.isEmpty()) cacheContents.put(name, cacheValue);
                    } else if (nativeCache instanceof javax.cache.Cache) {
                        // Eh107 Cache 처리
                        final javax.cache.Cache<Object, Object> ehCache = (javax.cache.Cache<Object, Object>) nativeCache;

                        final Map<Object, Object> cacheValue = new HashMap<>();
                        ehCache.iterator().forEachRemaining(entry -> {
                            try {
                                final String readableKey = stringifyKey(entry.getKey());
                                cacheValue.put(readableKey, entry.getValue());
                                log.debug("EhCache Key: {}", entry.getKey());
                            } catch (final NoSuchFieldException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        // 캐시 저장 개수가 0인 경우 건너뛰기
                        if (!cacheValue.isEmpty()) cacheContents.put(name, cacheValue);
                    } else {
                        log.warn("Unknown Cache Type: {}", nativeCache.getClass());
                    }
                });
        return cacheContents;
    }

    /**
     * SimpleKey를 문자열로 변환
     *
     * @param key 캐시 키
     * @return 사람이 읽을 수 있는 문자열
     */
    private static String stringifyKey(final Object key) throws NoSuchFieldException, IllegalAccessException {
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
    private static String[] toStringArray(Object[] objects) {
        final String[] result = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            result[i] = (objects[i] != null) ? objects[i].toString() : "null";
        }
        return result;
    }


    /**
     * 캐시에서 키를 기반으로 오브젝트를 가져오는 메서드
     * @param cacheName 캐시 이름
     * @param cacheKey 캐시 키
     * @return 캐시에서 가져온 오브젝트, 캐시에 해당 키가 없는 경우 null 반환
     */
    public static Object getObjectFromCache(final String cacheName, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.debug("Cache with name {} does not exist.", cacheName);
            return null;
        }
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        if (valueWrapper == null) {
            log.debug("Object with key {} does not exist in cache {}.", cacheKey, cacheName);
            return null;
        }
        return valueWrapper.get();
    }

    /**
     * 캐시 이름의 특정 키 evict
     */
    public static void evictCache(final String cacheName, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }
        cache.evict(cacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, cacheKey);
    }

    /**
     * 내 캐시 evict
     * @param cacheName 캐시의 이름.
     */
    public static void evictMyCache(String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }
        final String myCacheKey = AuthUtils.getLgnUserId();
        cache.evict(myCacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, myCacheKey);
    }

    /**
     * 내 캐시 evict
     * @param cacheName 캐시의 이름.
     * @param cacheKey  제거할 캐시 항목을 식별하는 키.
     */
    public static void evictMyCache(String cacheName, Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null || cacheKey == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }
        final String myCacheKey = AuthUtils.getLgnUserId() + "_" + cacheKey;
        cache.evict(myCacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, myCacheKey);
    }

    /**
     * 캐시 이름으로 해당 캐시 evict
     */
    public static void evictCacheAll(final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }
        cache.clear();
        log.debug("cache name {} cleared.", cacheName);
    }

    /**
     * 캐시 이름으로 해당 캐시 evict
     */
    public static void evictMyCacheAll(final String cacheName) {
        // TODO:
        evictCacheAll(cacheName);
    }

    /**
     * 전체 캐시 evict
     */
    public static Boolean clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        return true;
    }

    /**
     * Hibernate Second Level 캐시 특정 엔티티 삭제
     */
    public static void clearL2Cache(final Class<?> clazz) {
        final org.hibernate.Cache cache = sessionFactory.getCache();
        cache.evictEntityData(clazz);
    }

    /**
     * Hibernate Second Level 캐시 전체 삭제
     */
    public static void clearL2Cache() {
        final org.hibernate.Cache cache = sessionFactory.getCache();
        cache.evictAllRegions();
    }
}