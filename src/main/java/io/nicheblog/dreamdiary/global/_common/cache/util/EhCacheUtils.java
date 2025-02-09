package io.nicheblog.dreamdiary.global._common.cache.util;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.cache.model.CacheParam;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
    private static SessionFactory sessionFactory;
    private static List<CacheStrategy> cacheStrategies;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        cacheManager = manager;
        sessionFactory = factory;
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
    public static Map<String, Object> getActiveCacheMap() {
        final Map<String, Object> cacheContents = new HashMap<>();

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
        if (cacheKey == null || Constant.SIMPLE_KEY.equals(cacheKey) || cacheKey instanceof SimpleKey) {
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
     * 캐시 이름의 특정 키 evict
     *
     * @param cacheParam 캐시 이름과 키 정보를 포함한 객체
     */
    public static void evictCache(final CacheParam cacheParam) {
        evictCache(cacheParam.getCacheName(), cacheParam.getCacheKey());
    }

    /**
     * 캐시 이름의 특정 키 evict
     *
     * @param cacheName 캐시의 이름
     * @param cacheKey  캐시에서 제거할 키
     */
    public static void evictCache(final String cacheName, final Object cacheKey) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        cache.evict(cacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, cacheKey);
    }

    /**
     * 내 캐시 evict
     *
     * @param cacheName 캐시의 이름.
     */
    public static void evictMyCache(final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

        final String myCacheKey = AuthUtils.getLgnUserId();
        cache.evict(myCacheKey);
        log.debug("cache name {} (key: {}) evicted.", cacheName, myCacheKey);
    }

    /**
     * 내 캐시 evict
     *
     * @param cacheName 캐시의 이름.
     * @param cacheKey  제거할 캐시 항목을 식별하는 키.
     */
    public static void evictMyCache(final String cacheName, final Object cacheKey) {
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
    public static void evictCacheAll(final CacheParam cacheParam) {
        evictCacheAll(cacheParam.getCacheName());
    }

    public static void evictCacheAll(final String cacheName) {
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;

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