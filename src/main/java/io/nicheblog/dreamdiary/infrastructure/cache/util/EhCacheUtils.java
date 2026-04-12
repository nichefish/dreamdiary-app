package io.nicheblog.dreamdiary.infrastructure.cache.util;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.infrastructure.cache.model.CacheParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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
@RequiredArgsConstructor
@Log4j2
public class EhCacheUtils {

    @Resource(name="jCacheManager")
    CacheManager manager;
    @Resource(name="hibernateSessionFactory")
    private SessionFactory factory;
    private final List<CacheStrategy> autowiredCacheStrategies;  // 자동으로 모든 전략이 주입됨

    private static CacheManager cacheManager;
    private static List<CacheStrategy> cacheStrategies;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        cacheManager = manager;
        cacheStrategies = autowiredCacheStrategies;
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
     * 캐시 목록(Map) 조회
     */
    public static HashMap<String, Object> getActiveCacheMap() {
        final HashMap<String, Object> cacheContents = new HashMap<>();

        final List<Cache> activeCacheList = getActiveCacheList();
        activeCacheList.forEach(cache -> {
            final String cacheName = cache.getName();
            final Map<Object, Object> cacheValueMap = new HashMap<>();
            // 적절한 캐시 전략 찾기
            for (CacheStrategy strategy : cacheStrategies) {
                if (!strategy.supports(cache)) continue;

                strategy.addCacheValue(cacheName, cacheValueMap);
            }
            // 캐시 저장 개수가 0인 경우 건너뛰기
            if (!cacheValueMap.isEmpty()) cacheContents.put(cacheName, cacheValueMap);
        });
        return cacheContents;
    }

    /**
     * 캐시에서 키를 기반으로 오브젝트를 가져오는 메서드
     *
     * @param cacheParam 캐시 파라미터
     * @return 캐시에서 가져온 오브젝트, 캐시에 해당 키가 없는 경우 null 반환
     */
    public static Object getObjectFromCache(final CacheParam cacheParam) {
        final String cacheName = cacheParam.getCacheName();
        final String cacheKey = cacheParam.getCacheKey();

        return getObjectFromCache(cacheName, cacheKey);
    }

    /**
     * 캐시에서 기본 키 오브젝트를 가져오는 메서드
     *
     * @param cacheName 캐시 이름
     * @return 캐시에서 가져온 오브젝트
     */
    public static Object getObjectFromCache(final String cacheName) {
        return getObjectFromCache(cacheName, SimpleKey.EMPTY);
    }

    /**
     * 캐시에서 키를 기반으로 오브젝트를 가져오는 메서드
     *
     * @param cacheName 캐시 이름
     * @param cacheKey 캐시 키
     * @return 캐시에서 가져온 오브젝트, 캐시에 해당 키가 없는 경우 null 반환
     */
    public static Object getObjectFromCache(final String cacheName, Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return null;

        // SimpleKey 변환 처리 (키가 없을 경우 대비)
        if (cacheKey == null || Constant.SIMPLE_KEY.equals(cacheKey)) {
            cacheKey = SimpleKey.EMPTY;
        }

        // Spring Cache의 기본 조회 방식
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        if (valueWrapper != null) {
            Object value = valueWrapper.get();
            log.debug("Cache hit - Name: {}, Key: {}, Value: {}", cacheName, cacheKey, value);
            return value;
        }

        // miss시: Spring Cache 추상화가 아니라, 캐시 직접 접근 시도
        final Object nativeCache = cache.getNativeCache();
        log.debug("Checking native cache - Name: {}, Type: {}", cacheName, nativeCache.getClass());

        // 적절한 캐시 전략 찾기
        for (CacheStrategy strategy : cacheStrategies) {
            if (!strategy.supports(cache)) continue;

            return strategy.getObject(cacheName, cacheKey);
        }

        log.warn("Cache miss - Name: {}, Key: {}", cacheName, cacheKey);
        return null;
    }

    /**
     * 캐시 엔트리를 갱신(put)한다.
     *
     * <p>
     * 주어진 캐시 영역(cacheNm)에서 cacheKey에 해당하는 값을 value로 갱신한다.
     * 캐시가 존재하지 않을 경우 아무 작업도 수행하지 않는다.
     * Spring Cache 추상화를 사용하므로 동일 key가 존재하면 overwrite된다.
     * </p>
     *
     * <p><b>주의:</b> value가 mutable 객체(Map, List 등)일 경우,
     * 캐시에서 반환한 객체를 수정하면 별도의 put 없이도 값이 변경될 수 있다.
     * 일관성을 위해 상태 변경 후 반드시 이 메서드를 통해 저장하는 것을 권장한다.
     * </p>
     *
     * @param cacheNm  갱신할 캐시 영역의 이름
     * @param cacheKey 캐시 엔트리를 식별하는 key
     * @param value    캐시에 저장할 객체
     */
    public static void put(final String cacheNm, final Object cacheKey, final Object value) {
        final Cache cache = cacheManager.getCache(cacheNm);
        if (cache == null) return;

        // 업데이트된 정보를 다시 캐시에 저장
        cache.put(cacheKey, value);
    }

    /**
     * 캐시 이름의 특정 키 evict
     *
     * @param cacheParam 캐시 이름과 키 정보를 포함한 객체
     */
    public static void evictCache(final CacheParam cacheParam) {
        evictCacheByKey(cacheParam.getCacheName(), cacheParam.getCacheKey());
    }

    /**
     * 캐시 이름의 특정 키 evict
     *
     * @param cacheName 캐시의 이름
     * @param cacheKey  캐시에서 제거할 키
     */
    public static void evictCacheByKey(final String cacheName, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null || cacheKey == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }

        cache.evict(cacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, cacheKey);
    }

    /**
     * 내 캐시 evict
     *
     * @param cacheName 캐시의 이름.
     */
    public static void evictMyCache(final String cacheName) {
        evictUserCache(cacheName, AuthUtils.getLgnUserId());
    }

    /**
     * 사용자 범위 캐시 evict
     *
     * @param cacheName 캐시명
     * @param userId 사용자 ID
     */
    public static void evictUserCache(final String cacheName, final String userId) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        if (StringUtils.isEmpty(userId)) {
            log.warn("UserId is empty. Skip user-scoped evict for cache: {}", cacheName);
            return;
        }
        cache.evict(userId);
        cache.evict(new SimpleKey(userId));
        log.debug("cache name {} (key: {}) evicted.", cacheName, userId);
    }

    /**
     * 내 캐시 evict
     *
     * @param cacheName 캐시의 이름.
     * @param cacheKey  제거할 캐시 항목을 식별하는 키.
     */
    public static void evictMyCacheByKey(final String cacheName, final Object cacheKey) {
        evictUserCacheByKey(cacheName, AuthUtils.getLgnUserId(), cacheKey);
    }

    /**
     * 사용자 범위 캐시 evict
     *
     * @param cacheName 캐시명
     * @param userId 사용자 ID
     * @param cacheKey 캐시 하위 키
     */
    public static void evictUserCacheByKey(final String cacheName, final String userId, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null || cacheKey == null) {
            log.debug("cache name {} does not exists.", cacheName);
            return;
        }
        if (StringUtils.isEmpty(userId)) {
            log.warn("User id is empty. Skip user-scoped evict for cache: {}", cacheName);
            return;
        }
        final Object scopedKey = new SimpleKey(userId, cacheKey);
        cache.evict(scopedKey);
        cache.evict(userId + "_" + cacheKey); // fallback for legacy keys
        log.debug("cache name {} (key: {}) evicted.", cacheName, scopedKey);
    }

    /**
     * 캐시 이름으로 해당 캐시 evict
     *
     * @param cacheParam CacheParam
     */
    public static void clearCache(final CacheParam cacheParam) {
        clearCache(cacheParam.getCacheName());
    }

    /**
     * 캐시 이름으로 해당 캐시 evict
     *
     * @param cacheName 캐시의 이름
     */
    public static void clearCache(final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        cache.clear();
        log.debug("cache name {} cleared.", cacheName);
    }

    /**
     * 캐시 이름으로 해당 캐시 evict
     *
     * @param cacheName String
     */
    public static void clearMyCache(final String cacheName) {
        clearUserCache(cacheName, AuthUtils.getLgnUserId());
    }

    /**
     * 사용자 범위 캐시 전체 clear
     *
     * @param cacheName 캐시명
     * @param userId 사용자 ID
     */
    public static void clearUserCache(final String cacheName, final String userId) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;
        if (StringUtils.isEmpty(userId)) {
            log.warn("User id is empty. Skip user-scoped clear for cache: {}", cacheName);
            return;
        }

        final Object nativeCache = cache.getNativeCache();
        int evictedCnt = 0;
        if (nativeCache instanceof javax.cache.Cache<?, ?> ehCache) {
            final List<Object> keysToEvict = new ArrayList<>();
            ehCache.iterator().forEachRemaining(entry -> {
                if (isMyScopeCacheKey(entry.getKey(), userId)) keysToEvict.add(entry.getKey());
            });
            for (final Object key : keysToEvict) {
                cache.evict(key);
                evictedCnt++;
            }
        } else if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
            final List<Object> keysToEvict = new ArrayList<>();
            caffeineCache.asMap().keySet().forEach(key -> {
                if (isMyScopeCacheKey(key, userId)) keysToEvict.add(key);
            });
            for (final Object key : keysToEvict) {
                cache.evict(key);
                evictedCnt++;
            }
        } else {
            log.warn("Unsupported native cache type. Skip user-scoped clear for cache: {}", cacheName);
            return;
        }
        log.debug("cache name {} user scoped clear done. userId={}, evicted={}", cacheName, userId, evictedCnt);
    }

    /**
     * 현재 로그인 사용자 범위에서 key prefix로 캐시를 제거한다.
     *
     * @param cacheName 캐시 이름
     * @param cacheKeyPrefix 사용자 prefix 이후 매칭할 key prefix
     */
    public static void evictMyCacheByPrefix(final String cacheName, final String cacheKeyPrefix) {
        evictUserCacheByPrefix(cacheName, AuthUtils.getLgnUserId(), cacheKeyPrefix);
    }

    /**
     * 사용자 범위 prefix 기반 캐시 제거
     *
     * @param cacheName 캐시명
     * @param userId 사용자 ID
     * @param cacheKeyPrefix 사용자 prefix 이후 key prefix
     */
    public static void evictUserCacheByPrefix(final String cacheName, final String userId, final String cacheKeyPrefix) {
        if (StringUtils.isEmpty(cacheKeyPrefix)) {
            clearUserCache(cacheName, userId);
            return;
        }

        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        if (StringUtils.isEmpty(userId)) {
            log.warn("User id is empty. Skip user-scoped prefix clear for cache: {}", cacheName);
            return;
        }

        final String normalizedPrefix = stripTrailingUnderscore(cacheKeyPrefix.trim());
        final String fullPrefix = userId + "_" + normalizedPrefix;
        final String[] prefixParts = normalizedPrefix.isEmpty() ? new String[0] : normalizedPrefix.split("_");
        final Object nativeCache = cache.getNativeCache();
        int evictedCnt = 0;
        if (nativeCache instanceof javax.cache.Cache<?, ?> ehCache) {
            final List<Object> keysToEvict = new ArrayList<>();
            ehCache.iterator().forEachRemaining(entry -> {
                final Object keyObj = entry.getKey();
                if (keyObj instanceof SimpleKey simpleKey) {
                    if (matchesSimpleKeyPrefix(simpleKey, userId, prefixParts)) keysToEvict.add(keyObj);
                    return;
                }
                final String keyStr = Objects.toString(keyObj, "");
                if (matchesPrefixBoundary(keyStr, fullPrefix)) keysToEvict.add(keyObj);
            });
            for (final Object key : keysToEvict) {
                cache.evict(key);
                evictedCnt++;
            }
        } else if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
            final List<Object> keysToEvict = new ArrayList<>();
            caffeineCache.asMap().keySet().forEach(key -> {
                if (key instanceof SimpleKey simpleKey) {
                    if (matchesSimpleKeyPrefix(simpleKey, userId, prefixParts)) keysToEvict.add(key);
                    return;
                }
                final String keyStr = Objects.toString(key, "");
                if (matchesPrefixBoundary(keyStr, fullPrefix)) keysToEvict.add(key);
            });
            for (final Object key : keysToEvict) {
                cache.evict(key);
                evictedCnt++;
            }
        } else {
            log.warn("Unsupported native cache type. Skip user-scoped prefix clear for cache: {}", cacheName);
            return;
        }
        log.debug("cache name {} user prefix clear done. prefix={}, evicted={}", cacheName, fullPrefix, evictedCnt);
    }

    /**
     * 문자열 끝에 붙은 '_'를 모두 제거한다.
     * - prefix 기반 키 비교 시 "userId_" 같은 불완전한 경계값을 정규화하기 위함
     *
     * @param value String
     */
    private static String stripTrailingUnderscore(final String value) {
        if (value == null || value.isBlank()) return "";
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '_') {
            end--;
        }
        return value.substring(0, end);
    }

    /**
     * key가 prefix와 "경계 단위"로 매칭되는지 확인한다.
     * 단순 startsWith가 아니라, "_“ 단위로 끊긴 prefix만 허용 (cache key namespace를 "_" 기반 계층 구조로 간주)
     *
     * @param key String
     * @param prefix String
     */
    private static boolean matchesPrefixBoundary(final String key, final String prefix) {
        if (key == null || StringUtils.isEmpty(prefix)) return false;
        return key.equals(prefix) || key.startsWith(prefix + "_");
    }

    /**
     * cacheKey가 특정 사용자(lgnUserId)의 scope에 속하는지 판단한다.
     * - 캐시 키를 "userId 기반 namespace"로 간주하고 필터링
     * - 동일 사용자 데이터만 선택적으로 invalidate / 조회하기 위함
     *
     * @param cacheKey Object
     * @param lgnUserId String
     */
    private static boolean isMyScopeCacheKey(final Object cacheKey, final String lgnUserId) {
        if (cacheKey == null || lgnUserId == null || lgnUserId.isBlank()) return false;

        if (cacheKey instanceof SimpleKey simpleKey) {
            final Object[] params = getSimpleKeyParams(simpleKey);
            if (params == null || params.length == 0) return false;
            final String firstParam = Objects.toString(params[0], null);
            return lgnUserId.equals(firstParam);
        }

        final String keyStr = Objects.toString(cacheKey, "");
        return lgnUserId.equals(keyStr) || keyStr.startsWith(lgnUserId + "_");
    }

    private static Object[] getSimpleKeyParams(final SimpleKey simpleKey) {
        try {
            final Field paramsField = SimpleKey.class.getDeclaredField("params");
            paramsField.setAccessible(true);
            return (Object[]) paramsField.get(simpleKey);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            log.debug("SimpleKey reflection failed: {}", e.getMessage());
            return null;
        }
    }

    private static boolean matchesSimpleKeyPrefix(final SimpleKey simpleKey, final String lgnUserId, final String[] prefixParts) {
        final Object[] params = getSimpleKeyParams(simpleKey);
        if (params == null || params.length == 0) return false;
        if (!lgnUserId.equals(Objects.toString(params[0], null))) return false;
        if (prefixParts == null || prefixParts.length == 0) return true;
        final String prefixToken = String.join("_", prefixParts);
        final String joinedParams = Arrays.stream(params, 1, params.length)
                .map(param -> Objects.toString(param, ""))
                .collect(Collectors.joining("_"));
        return matchesPrefixBoundary(joinedParams, prefixToken);
    }

    /**
     * 전체 캐시 evict
     */
    public static Boolean clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        return true;
    }
}
