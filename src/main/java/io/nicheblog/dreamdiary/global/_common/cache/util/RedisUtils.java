package io.nicheblog.dreamdiary.global._common.cache.util;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * RedisUtils
 * <pre>
 *  Redis 관련 유틸리티 클래스
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> autowiredRedisTemplate;

    private static RedisTemplate<String, Object> redisTemplate;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        redisTemplate = autowiredRedisTemplate;
    }

    /**
     * setData
     */
    public static void setData(final String key, final String value, final Long expiredTime){
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    /**
     * getData
     */
    public static String getData(final String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * deleteData
     */
    public static void deleteData(final String key){
        redisTemplate.delete(key);
    }
}
