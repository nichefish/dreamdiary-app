package io.nicheblog.dreamdiary.extension.cache.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
     * 주어진 키에 대한 데이터를 Redis에 저장합니다.
     * <p>이 메서드는 Redis에 문자열 데이터를 저장하며, 만료 시간을 설정할 수 있습니다.</p>
     *
     * @param key 저장할 데이터의 키
     * @param value 저장할 데이터 값
     * @param expiredTime 데이터의 만료 시간(밀리초 단위)
     */
    public static void setData(final String key, final String value, final Long expiredTime){
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 주어진 키에 해당하는 데이터를 Redis에서 조회합니다.
     *
     * @param key 조회할 데이터의 키
     * @return 키에 해당하는 데이터 값 (없으면 `null` 반환)
     */
    public static String getData(final String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 주어진 키에 해당하는 데이터를 Redis에서 삭제합니다.
     *
     * @param key 삭제할 데이터의 키
     */
    public static void deleteData(final String key){
        redisTemplate.delete(key);
    }
}
